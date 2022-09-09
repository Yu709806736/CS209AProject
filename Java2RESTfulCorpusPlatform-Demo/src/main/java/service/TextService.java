package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.TextDao;
import io.javalin.http.Context;
import model.Document;
import util.FailureCause;
import util.FailureResponse;
import util.Response;
import util.SuccessResponse;

/**
 * Play a role of Analyzer, handle the five operations.
 */
public class TextService {
    /**
     * connect with database
     */
    TextDao dao;

    public TextService(TextDao dao) {
        this.dao = dao;
    }

    /**
     * Handle "exists" operation, check whether a file exists in the database
     * (given the md5 sum of the file)
     * There are 3 situations:
     *   1. if the file exists ---- send "{"code": 0, "message": "", "result": {"exists": true}}" to client
     *   2. if the file doesn't exist ---- send "{"code": 0, "message": "", "result": {"exists": false}}" to client
     *   3. if there're problems when connecting with database ---- send failure response with code 4 and with message
     *      "Exception occurs when connecting database"
     * @param ctx: context received from client
     */
    public void handleExists(Context ctx){
        //TODO: finished
        String md5 = ctx.pathParam("md5");
        try{
            int exist = dao.checkExist(md5);
            Response response = new SuccessResponse();
            if(exist == 0){
                response.getResult().put("exists", false);
                ctx.json(response);
            }else if(exist == 1){
                response.getResult().put("exists", true);
                ctx.json(response);
            }else{
                response = new FailureResponse(FailureCause.DB_ERROR);
                ctx.json(response);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Handle "upload" operation, upload the file to database (given the md5 sum of the file)
     * There are 3 major situations:
     *   1. if the file already exists ---- send failure response with a code 3 and with a message "File with
     *      the same md5 already exists"
     *   2. if the file doesn't exists ---- 3 minor situations:
     *      a. if the md5 sum of the file cannot match the file content ---- send failure response with a code 2
     *          and with a message "Hash doesn't match"
     *      b. if there are some problems when inserting values to database ---- send failure response with a code 4
     *          and with a message "Exception occurs when connecting database"
     *      c. if inserting successfully ---- send success response
     *   3. if there are some problems when checking whether the file exists ---- send failure response with a code 4
     *      and with a message "Exception occurs when connecting database"
     * @param ctx: context received from client with a body of the file content
     */
    public void handleUpload(Context ctx){
        String md5 = ctx.pathParam("md5");
        String content = ctx.body();
        try {
            int exist = dao.checkExist(md5);
            if(exist == 0){
                int insert = dao.insert(md5, content);
                if (insert == 0) {
                    Response response = new SuccessResponse();
                    response.getResult().put("success", true);
                    ctx.json(response);
                }else if(insert == 2){
                    Response response = new FailureResponse(FailureCause.HASH_NOT_MATCH);
                    response.getResult().put("success", false);
                    ctx.json(response);
                }else if(insert == 4){
                    Response response = new FailureResponse(FailureCause.DB_ERROR);
                    response.getResult().put("success", false);
                    ctx.json(response);
                }
            }else if(exist == 1){
                Response response = new FailureResponse(FailureCause.ALREADY_EXIST);
                response.getResult().put("success", false);
                ctx.json(response);
            }else{
                Response response = new FailureResponse(FailureCause.DB_ERROR);
                response.getResult().put("success", false);
                ctx.json(response);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Handle "download" operation, download the file from database (given the md5 sum of file)
     * There are 3 major situations:
     *   1. if the file doesn't exists ---- send failure response with a code 1 and with a message "File not found"
     *   2. if the file exists ---- 2 minor situations:
     *      a. if there are some problems when inserting values to database ---- send failure response with a code 4
     *          and with a message "Exception occurs when connecting database"
     *      b. otherwise ---- send success response
     *   3. if there are some problems when checking whether the file exists ---- send failure response with a code 4
     *      and with a message "Exception occurs when connecting database"
     * @param ctx: context received from client
     */
    public void handleDownload(Context ctx){
        String md5 = ctx.pathParam("md5");
        try {
            int exist = dao.checkExist(md5);
            if (exist == 0) {
                Response response = new FailureResponse(FailureCause.FILE_NOT_FOUND);
                ctx.json(response);
            }else if(exist == 1){
                String content = dao.getContent(md5);
                if(content.equals("4")){
                    Response response = new FailureResponse(FailureCause.DB_ERROR);
                    ctx.json(response);
                }else{
                    Response response = new SuccessResponse();
                    response.getResult().put("content", content);
                    ctx.json(response);
                }
            }else {
                Response response = new FailureResponse(FailureCause.DB_ERROR);
                ctx.json(response);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Handle "compare" operation, compare 2 files by simple similarity and Levenshtein distance
     * (given md5 sums of files)
     * There are 3 major situations:
     *   1. if one of the files doesn't exists ---- send failure response with a code 1
     *      and with a message "File not found"
     *   2. if both of the files exist ---- send success response with a code 0, a message "", and the simple
     *      similarity and Levenshtein distance back to the client
     *   3. if problems occur when connecting with database ---- send failure response with a code 4
     *      and with a message "Exception occurs when connecting database"
     * @param ctx: context received from client
     */
    public void handleCompare(Context ctx){
        String md51 = ctx.pathParam("md51");
        String md52 = ctx.pathParam("md52");
        try {
            int exist1 = dao.checkExist(md51);
            int exist2 = dao.checkExist(md52);
            if(exist1 == 0 || exist2 == 0){
                if(exist1 == 0){
                    System.out.println(md51);
                }
                if(exist2 == 0){
                    System.out.println(md52);
                }
                Response response = new FailureResponse(FailureCause.FILE_NOT_FOUND);
                ctx.json(response);
            }else if (exist1 == 1 && exist2 == 1){
                String content1 = dao.getContent(md51);
                String content2 = dao.getContent(md52);
                Response response = new SuccessResponse();
                response.getResult().put("simple_similarity", getSimp(content1, content2));
                response.getResult().put("levenshtein_distance", getLDis(content1, content2));
                ctx.json(response);
            }else{
                Response response = new FailureResponse(FailureCause.DB_ERROR);
                ctx.json(response);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Calculate simple similarity
     * @param doc1: content of file 1
     * @param doc2: content of file 2
     * @return simple similarity: double
     */
    private static double getSimp(String doc1, String doc2){
        char[] chars1 = doc1.toCharArray();
        char[] chars2 = doc2.toCharArray();

        int n = chars1.length;
        int m = chars2.length;
        int minlen = Math.min(n, m);
        int maxlen = Math.max(n, m);
        int same = 0;

        for(int i=0; i<minlen; i++){
            if(chars1[i] == chars2[i]){
                same++;
            }
        }

        return ((double) same / (double) maxlen);
    }

    /**
     * Calculate Levenshtein distance by dynamic programming
     * @param doc1: content of file 1
     * @param doc2: content of file 2
     * @return Levenshtein distance: integer
     */
    public static int getLDis(String doc1, String doc2){
        char[] chars1 = doc1.toCharArray();
        char[] chars2 = doc2.toCharArray();

        int n = chars1.length;
        int m = chars2.length;

        int[][] lev = new int[n+1][m+1];
        for(int i=0; i<=n; i++){
            lev[i][0] = i;
        }
        for(int j=1; j<=m; j++){
            lev[0][j] = j;
        }
        for(int i=1; i<=n; i++){
            for(int j=1; j<=m; j++){
                int change = (chars1[i-1] == chars2[j-1]) ? 0 : 1;
                int value1 = lev[i-1][j] + 1;
                int value2 = lev[i][j-1] + 1;
                int value3 = lev[i-1][j-1] + change;
                int min12 = Math.min(value1, value2);
                lev[i][j]  = Math.min(min12, value3);
            }
        }
        return lev[n][m];
    }

    /**
     * Handle "list" operation, list all files from the database
     * There are 3 situations:
     *   1. if nothing is found on the database ---- send a success response with a result of "files: """
     *   2. if TextDao return a Document("4", "error") (which is manually defined as the specific error signal) ----
     *      send a failure response with a code 4 and with a message "Exception occurs when connecting database"
     *   3. otherwise ---- send all files to the client
     * @param ctx: context received from the client
     */
    public void handleList(Context ctx){
        try {
            Document[] documents = dao.getTable();
            Response response;
            if(documents == null){
                response = new SuccessResponse();
                ctx.json(response);
                return;
            }
            // a magic code for a symbol of error happening
            if(documents[0].equals(new Document("4", "error"))){
                response = new FailureResponse(FailureCause.DB_ERROR);
                ctx.json(response);
                return;
            }
            ObjectMapper objectMapper = new ObjectMapper();
            // get the preview of the documents and store them into an ObjectNode array
            ObjectNode[] result = new ObjectNode[documents.length];
            for(int i=0; i<documents.length; i++){
                result[i] = documents[i].getPreview();
            }
            response = new SuccessResponse();
            //https://www.it-swarm.dev/zh/java/jackson-json%EF%BC%9A%E5%A6%82%E4%BD%95%E5%B0%86%E6%95%B0%E7%BB%84%E8%BD%AC%E6%8D%A2%E4%B8%BAjsonnode%E5%92%8Cobjectnode%EF%BC%9F/1046170885/
            // convert the array into ArrayNode class and ...
            ArrayNode array = objectMapper.valueToTree(result);
            // put the ArrayNode into the value of the key "files"
            response.getResult().putArray("files").addAll(array);
            ctx.json(response);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
