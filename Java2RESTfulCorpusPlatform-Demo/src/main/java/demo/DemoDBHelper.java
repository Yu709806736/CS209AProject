package demo;

import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.List;

public class DemoDBHelper {
    public static void main(String[] args) throws ClassNotFoundException {
        // This is a small quickstart guide to database operations with Sql2o.
        // Sql2o is a wrapper of JDBC, but with easier APIs to use
        // Read more about Sql2o at: https://github.com/aaberg/sql2o/wiki

        // First load the JDBC driver for SQLite
        Class.forName("org.sqlite.JDBC");
        // Then create a Sql2o object.
        // Just like using JDBC, if the database does not exist, it will be created.
        Sql2o sql2o = new Sql2o("jdbc:sqlite:demo.db", null, null);


        // Executing SQL is simple
        String dropTable = "DROP TABLE IF EXISTS \"courses\"";
        String initSql = "CREATE TABLE IF NOT EXISTS \"courses\" (\n" +
                "\t\"courseName\"\tTEXT NOT NULL UNIQUE,\n" +
                "\t\"SAcount\"\tINTEGER NOT NULL\n" +
                ")";
        try (Connection con = sql2o.open()) {
            // We are modifying, instead of retrieving, so use `executeUpdate()`
            con.createQuery(dropTable).executeUpdate();
            con.createQuery(initSql).executeUpdate();
            con.commit();
        }catch (Exception e){
            e.printStackTrace();
        }

        // You can try some INSERT
        // use :VarName to indicate a variable
        String insertSQL = "insert into courses (courseName, SAcount) values (:courseName, :SACount)";
        Course course = new Course("Java2", 4);
        try (Connection con = sql2o.open()) {
            con.createQuery(insertSQL)
                    // Then replace the variables with actual values
                    .addParameter("courseName", course.getCourseName())
                    .addParameter("SACount", course.getSAcount())
                    .executeUpdate();
            con.commit();
        }catch (Exception e){
            e.printStackTrace();
        }

        // Then you can SELECT data back
        String selectSQL = "select courseName, SACount from courses";
        List<Course> courseList = null;
        try (Connection con = sql2o.open()) {
            courseList = con.createQuery(selectSQL)
                    // You can just map the rows back to Java objects
                    .executeAndFetch(Course.class);
            con.commit();
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println(courseList);

        // Finally, you can get scalars from database
        // Scalar here means a single value, e.g. the count of all records
        String countSQL = "select count(*) from courses";
        Integer count = null;
        try (Connection con = sql2o.open()) {
            count = (Integer) con.createQuery(countSQL)
                    .executeScalar();
            con.commit();
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("Count of items in DB: " + count);
    }
}