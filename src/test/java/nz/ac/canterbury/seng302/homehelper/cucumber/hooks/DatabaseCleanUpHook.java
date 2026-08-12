package nz.ac.canterbury.seng302.homehelper.cucumber.hooks;

import io.cucumber.java.After;
import jakarta.persistence.EntityManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import javax.sql.DataSource;
import org.assertj.core.api.Fail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Hook that resets the database after every scenario. Code taken from <a
 * href="https://stackoverflow.com/questions/8523423/reset-embedded-h2-database-periodically">stack
 * overflow</a>
 */
@SpringBootTest
public class DatabaseCleanUpHook {

    @Autowired
    private DataSource datasource;

    @Autowired
    private EntityManager entityManager;

    @After
    public void tearDown() {
        try {
            clearDatabase();
            entityManager.clear();
            entityManager.getEntityManagerFactory().getCache().evictAll();
        } catch (Exception e) {
            Fail.fail(e.getMessage());
        }
    }

    /**
     * Disables the referential integrity constraints between all the tables which would otherwise
     * prevent the truncate operation. Retrieves all the table names and truncates each table which
     * removes every row. Resets the DB sequences which are effectively counters. Finally enables
     * the FK constraints again.
     *
     * @throws SQLException if an SQL Exception occurs
     */
    public synchronized void clearDatabase() throws SQLException {
        Connection c = datasource.getConnection();
        Statement s = c.createStatement();

        // Disable FK
        s.execute("SET REFERENTIAL_INTEGRITY FALSE");

        // Find all tables and truncate them
        Set<String> tables = new HashSet<String>();
        ResultSet rs = s.executeQuery(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES  where TABLE_SCHEMA='PUBLIC'");
        while (rs.next()) {
            tables.add(rs.getString(1));
        }
        rs.close();
        for (String table : tables) {
            s.executeUpdate("TRUNCATE TABLE " + table
                    + " RESTART IDENTITY"); // NOTE: The restart identity key command is specific for h2 so not sure what the pipeline uses
        }

        // Idem for sequences
        Set<String> sequences = new HashSet<String>();
        rs = s.executeQuery(
                "SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_SCHEMA='PUBLIC'");
        while (rs.next()) {
            sequences.add(rs.getString(1));
        }
        rs.close();
        for (String seq : sequences) {
            s.executeUpdate("ALTER SEQUENCE " + seq + " RESTART WITH 1");
        }

        // Enable FK
        s.execute("SET REFERENTIAL_INTEGRITY TRUE");
        s.close();
        c.close();
    }
}