import io.kovertx.sql.migrator.MigrationParser
import io.kovertx.sql.migrator.postgres.postgresParser
import org.junit.jupiter.api.Test

class PostgresParserTests {

    @Test
    fun test() {
        val steps = MigrationParser.splitIntoSteps("""
            -- migration: initial setup
            CREATE TYPE sex AS ENUM ('male', 'female', 'other');
            CREATE TABLE users (
                user_id UUID PRIMARY KEY,
                user_name VARCHAR(36) NOT NULL,
                email VARCHAR(254) UNIQUE NOT NULL,
                password_hash VARCHAR(255) NOT NULL
            );
            CREATE UNIQUE INDEX idx_users_email ON users((lower(user_name)));
            CREATE UNIQUE INDEX idx_users_name ON users((lower(email)));
            CREATE TABLE user_physical_profile (
                user_id UUID PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
                sex sex NULL,
                height FLOAT NULL,
                birth_date DATE NULL
            );
            CREATE TABLE user_daily_metrics (
                user_id UUID REFERENCES users(user_id) ON DELETE CASCADE,
                date DATE NOT NULL,

                mass FLOAT NULL,
                body_fat FLOAT NULL,
                resting_heart_rate FLOAT NULL,
                bp_systolic FLOAT NULL,
                bp_diastolic FLOAT NULL,
                step_count INTEGER NULL,
                PRIMARY KEY (user_id, date)
            );
            -- migration: creating meal logs and daily nutrition view
            CREATE TABLE meal_log (
                meal_id UUID PRIMARY KEY,
                user_id UUID REFERENCES users(user_id) ON DELETE CASCADE,
                title VARCHAR(200),
                time TIMESTAMP NOT NULL,
                entries JSONB NOT NULL
            );
            CREATE INDEX idx_meal_log_date ON meal_log (DATE(time));
            CREATE VIEW daily_nutrition AS
                SELECT
                    user_id,
                    DATE(time) AS date,
                    SUM(COALESCE((entry -> 'energy')::FLOAT, 0)) AS energy,
                    SUM(COALESCE((entry -> 'protein')::FLOAT, 0)) AS protein,
                    SUM(COALESCE((entry -> 'fat')::FLOAT, 0)) AS fat,
                    SUM(COALESCE((entry -> 'carbohydrate')::FLOAT, 0)) AS carbohydrate
                FROM
                    meal_log,
                    jsonb_array_elements(entries) AS entry
                GROUP BY user_id, DATE(time);
            -- migration: add modification timestamps to daily metrics and meal logs
            --add a (modified TIMESTAMP) column to log tables
            ALTER TABLE user_daily_metrics ADD COLUMN modified TIMESTAMP DEFAULT NOW();
            ALTER TABLE meal_log ADD COLUMN modified TIMESTAMP DEFAULT NOW();
            --used in triggers to apply current timestamp to log tables rows as they're inserted or updated
            CREATE FUNCTION trigger_set_modified()
            RETURNS TRIGGER AS '
            BEGIN
                NEW.modified = NOW();
                RETURN NEW;
            END;
            ' LANGUAGE plpgsql;
            --add triggers on log tables to update modified column
            CREATE TRIGGER trigger_set_modified_user_daily_metrics
            BEFORE INSERT OR UPDATE ON user_daily_metrics
            FOR EACH ROW
            EXECUTE PROCEDURE trigger_set_modified();
            CREATE TRIGGER trigger_set_modified_meal_log
            BEFORE INSERT OR UPDATE ON meal_log
            FOR EACH ROW
            EXECUTE PROCEDURE trigger_set_modified();
            -- migration: add energy intake column to daily metrics

            -- add columns to daily metrics
            ALTER TABLE user_daily_metrics ADD COLUMN energy_intake DECIMAL DEFAULT 0;
            ALTER TABLE user_daily_metrics ADD COLUMN energy_intake_manual BOOLEAN NOT NULL DEFAULT FALSE;
            -- copy in data from existing logged meals
            INSERT INTO user_daily_metrics (user_id, date, energy_intake)
            SELECT
                user_id,
                DATE(time) AS date,
                SUM(COALESCE((entry -> 'energy')::FLOAT, 0)) AS energy_intake
            FROM
                meal_log, jsonb_array_elements(entries) AS entry
            GROUP BY
                user_id, DATE(time)
            ON CONFLICT (user_id, date) DO UPDATE
                SET energy_intake = EXCLUDED.energy_intake;
            -- migration: users triggers to update energy intake daily metrics from meal log changes
            ALTER TABLE user_daily_metrics DROP COLUMN energy_intake_manual;
            ALTER TABLE user_daily_metrics ADD COLUMN energy_intake_unlogged DECIMAL NULL DEFAULT NULL;
            CREATE PROCEDURE upsert_user_daily_metrics_energy_intake(meal_user_id UUID, meal_date DATE)
                LANGUAGE plpgsql AS '
            DECLARE
                energy_intake_total DECIMAL;
            BEGIN
                -- Calculate the total energy intake for the user and date
                SELECT SUM(COALESCE((entry -> ''energy'')::FLOAT, 0)) INTO energy_intake_total
                FROM meal_log m, jsonb_array_elements(entries) AS entry
                WHERE m.user_id = meal_user_id AND DATE(m.time) = meal_date;

                -- Upsert into user_daily_metrics
                INSERT INTO user_daily_metrics (user_id, date, energy_intake)
                VALUES (meal_user_id, meal_date, energy_intake_total)
                ON CONFLICT (user_id, date)
                    DO UPDATE SET energy_intake = EXCLUDED.energy_intake;
            END;
            ';
            CREATE FUNCTION trigger_meal_log_changes()
                RETURNS TRIGGER AS '
            BEGIN
                CALL upsert_user_daily_metrics_energy_intake(NEW.user_id, DATE(NEW.time));
                IF (TG_OP = ''UPDATE'' AND DATE(OLD.time) != DATE(NEW.time)) THEN
                    CALL upsert_user_daily_metrics_energy_intake(OLD.user_id, DATE(OLD.time));
                END IF;
                RETURN NEW;
            END;
            ' LANGUAGE plpgsql;
            CREATE FUNCTION trigger_meal_log_deletes()
                RETURNS TRIGGER AS '
            BEGIN
                CALL upsert_user_daily_metrics_energy_intake(OLD.user_id, DATE(OLD.time));
                RETURN OLD;
            END;
            ' LANGUAGE plpgsql;
            -- Trigger to handle inserts and updates
            CREATE TRIGGER after_meal_log_insert_or_update
                AFTER INSERT OR UPDATE ON meal_log
                FOR EACH ROW
            EXECUTE FUNCTION trigger_meal_log_changes();
            -- Trigger to handle deletes
            CREATE TRIGGER after_meal_log_delete
                AFTER DELETE ON meal_log
                FOR EACH ROW
            EXECUTE FUNCTION trigger_meal_log_deletes();
        """.trimIndent())

        steps.forEach { step ->
            println("- Step: ${step.title}")
            System.err.println("-- Content: ${step.content}")
            postgresParser.split(step.content).forEach { stmt ->
                println("-- Stmt:\n${stmt}")
            }
        }
    }
}