package dao;

import model.Document;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;
import util.Utils;
import java.util.List;

/**
 * Act as a Storage to connect with database.
 */
public class TextDao {
    Sql2o sql2o = new Sql2o("jdbc:sqlite:Doc.db", null, null);
    public TextDao() {}

    /**
     * Check whether the given md5 exists
     * Notice: every method except this should be used after executing this method
     * @param md5: md5 sum of file
     * @return 0: the given md5 exists
     *         1: the given md5 doesn't exist and
     *         4: there're some errors when connecting with database
     */
    public int checkExist(String md5){
        try(Connection con = sql2o.open()){
            Integer count;
            String sql = "select count(*) from documents where md5 = :md5";
            count = (Integer) con.createQuery(sql).addParameter("md5", md5).executeScalar();
            return count.equals(0) ? 0 : 1;
        }catch (Sql2oException e){
            e.printStackTrace();
            return 4;
        }
    }

    /**
     * Insert the entry into database
     * Notice: the insert method should be used after checking the file doesn't exist
     * @param md5: the given md5 value of the file
     * @param content: the content of the file
     * @return 0: insert successfully
     *         2: insert aborted because md5 doesn't match the content (almost no use)
     *         4: error occurs when connecting with database
     */
    public int insert(String md5, String content){
        if(!md5.equals(Utils.calculateMD5(content))){
            return 2;
        }
        try(Connection con = sql2o.open()) {
            String sql = "insert into documents (md5, len, content) values (:md5, :len, :content)";
            con.createQuery(sql)
                    .addParameter("md5", md5)
                    .addParameter("len", content.length())
                    .addParameter("content", content)
                    .executeUpdate();
            return 0;
        }catch (Exception e){
            e.printStackTrace();
            return 4;
        }
    }

    /**
     * Get content by given the md5 sum
     * Notice: the insert method should be used after checking the file exists
     * @param md5: md5 sum of the file
     * @return String content: get content successfully
     *         "4": error occurs when connecting with the database (a magic number defined manually, can be
     *             understood as the failure code of DB_ERROR)
     */
    public String getContent(String md5){
        try(Connection con = sql2o.open()){
            String sql = "select content from documents where md5 = :md5";
            String content = (String) con.createQuery(sql).addParameter("md5", md5).executeScalar();
            return content;
        }catch (Exception e){
            e.printStackTrace();
            return "4";
        }
    }

    /**
     * Get all files stored in the database.
     * @return null: table "documents" is empty
     *         new Document[]{new Document("4", "error")}: error occurs when connecting with database (this return value
     *             is also manually defined, and "4" stands for the DB_ERROR code of failure cause)
     *         Document array: all files stored in the database
     */
    public Document[] getTable(){
        try(Connection con = sql2o.open()){
            String sql = "select * from documents";
            String str = (String) con.createQuery(sql).executeScalar();
            if(str == null){
                return null;
            }
            List<Document> documents = con.createQuery(sql).executeAndFetch(Document.class);
            int p = 0;
            //convert List to array ---- might be unnecessary but hard to modify ---- so just keep it
            Document[] result = new Document[documents.size()];
            for(Document doc: documents){
                result[p] = doc;
                p++;
            }
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return new Document[]{new Document("4", "error")};
        }
    }
}

