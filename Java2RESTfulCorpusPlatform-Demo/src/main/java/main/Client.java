package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.fluent.Request;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import util.Utils;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.Scanner;

public class Client{

    static String endpoint = "http://localhost:7001";
    /**
     * download path value
     */
    private static String dlPath = ".\\download";
    /**
     * file extensions that can be read
     */
    private static String[] supportSuffix = {"txt", "java", "py", "c", "cpp", "csv"};
    public static final String INVALID = "invalid command";
    public static final String BADFORMAT = "bad format";
    public static final String ERROR = "error occurs";

    public Client(){}
    public Client(String dlPath){  // personalize download directory
        this.dlPath = dlPath;
    }

    public void setDlPath(String dlPath){
        this.dlPath = dlPath;
    }

    enum Operation{
        UPLOAD, DOWNLOAD, COMPARE, EXISTS, LIST
    }

    public static Operation parseOperation(String op){
        //TODO: convert String to Operation - finished
        switch(op.toLowerCase()){
            case "upload":
                return Operation.UPLOAD;
            case "download":
                return Operation.DOWNLOAD;
            case "compare":
                return Operation.COMPARE;
            case "exists":
                return Operation.EXISTS;
            case "list":
                return Operation.LIST;
            default:
                System.out.println("Available Operation: upload, download, compare, exists, list");
                return null;
        }
    }

    public static void main(String[] args) throws IOException {

        while(true) {
            printUsage();

            Scanner in = new Scanner(System.in);
            args = in.nextLine().split("\\s+");

            Operation operation = parseOperation(args[0]);
            if (operation == null) {
                System.err.println("Unknown operation");
                printUsage();
                return;
            }

            switch (operation) {
                case UPLOAD:
                    handleUpload(args);
                    break;
                case DOWNLOAD:
                    handleDownload(args);
                    break;
                case COMPARE:
                    handleCompare(args);
                    break;
                case EXISTS:
                    handleExists(args);
                    break;
                case LIST:
                    handleList();
            }
        }
    }

    /**
     * Handle "exists" command, send "endpoint/files/:md5/exists" to Server
     *
     * @param args: exists [filename or md5], for UI only filename would be passed in
     * @return response String received from Server if successfully executed
     *         INVALID if "args" doesn't match the correct form or the file extension isn't able to be supported
     *         ERROR if error occurs when reading the file
     * @throws IOException
     */
    public static String handleExists(String[] args) throws IOException {
        if(args.length > 2){
            System.out.println("exist usage: exists [filename or md5]");
            return INVALID;
        }
        String[] filename = args[1].split("\\.");
        String md5 = "";
        if(filename.length < 2){
            md5 = args[1];
        }else if(checkSupport(args[1])){
            File file = new File(args[1]);
            CharsetDetector detector = new CharsetDetector();
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                detector.setText(bytes);
                CharsetMatch charsetMatch = detector.detect();
                String content = charsetMatch.getString();
                md5 = Utils.calculateMD5(content);
            }catch (Exception e) {
                e.printStackTrace();
                return ERROR;
            }
        }else{
            return INVALID;
        }
        String responseStr = Request.Get(endpoint + "/files/" + md5 + "/exists")
                .execute().returnContent().asString(StandardCharsets.UTF_8);
        System.out.println(responseStr);
        return responseStr;
    }

    /**
     * Handle "compare" command, send "endpoint/files/:md5_1/compare/:md5_2" to the Server
     *
     * @param args: compare [md5_1 or file1] [md5_2 or file2]
     * @return response String if successfully executed
     *         INVALID if wrong argument number or file extension cannot be supported
     *         ERROR if error occurs when reading files
     * @throws IOException
     */
    public static String handleCompare(String[] args) throws IOException {
        if(args.length != 3){
            System.out.println("compare usage: [compare] [param1] [param2]");
            return INVALID;
        }
        String md51 = args[1];
        String md52 = args[2];
        String[] name1 = args[1].split("\\.");
        String[] name2 = args[2].split("\\.");
        if(checkSupport(args[1])){
            File file = new File(args[1]);
            CharsetDetector detector = new CharsetDetector();
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                detector.setText(bytes);
                CharsetMatch charsetMatch = detector.detect();
                String content = charsetMatch.getString();
                md51 = Utils.calculateMD5(content);
            }catch (Exception e){
                e.printStackTrace();
                return ERROR;
            }
        }else if(name1.length != 1){
            return INVALID;
        }
        if(checkSupport(args[2])) {
            File file = new File(args[2]);
            CharsetDetector detector = new CharsetDetector();
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                detector.setText(bytes);
                CharsetMatch charsetMatch = detector.detect();
                String content = charsetMatch.getString();
                md52 = Utils.calculateMD5(content);
            }catch (Exception e){
                e.printStackTrace();
                return ERROR;
            }
        }else if(name2.length != 1){
            return INVALID;
        }
        String responseStr = Request.Get(endpoint + "/files/" + md51 + "/compare/" + md52)
                .execute().returnContent().asString(StandardCharsets.UTF_8);
        System.out.println(responseStr);
        return responseStr;
    }

    /**
     * Handle "download" command, send "endpoint/files/:md5" to the Server
     *
     * @param args: download [md5] [(optional)filename]
     * @return response String if successfully executed
     *         INVALID if wrong number of arguments
     *         response String + BADFORMAT if the file extension isn't supported (the file name to store download file
     *             would be set as "md5.txt")
     *         ERROR if error occurs when creating directories, creating files, or writing to files
     * @throws IOException
     */
    public static String handleDownload(String[] args) throws IOException {
        String filename = "";
        String exten = "";
        if(args.length == 3){
            //set download file name
            String[] name = args[2].split("\\.");
            if(name.length < 2){
                System.out.println("Filename should have a suffix \".txt\", will use default name this time");
                exten = BADFORMAT;
            }else if(checkSupport(args[2])) {
                filename = args[2];
            }else{
                System.out.println("Filename should have a suffix \".txt\", will use default name this time");
                exten = BADFORMAT;
            }
        }else if(args.length != 2){
            System.out.println("download usage: download [md5] [(option)download file name]");
            return INVALID;
        }
        File f = new File(dlPath);
        if(!f.exists()){
            if(!f.mkdir()){
                System.out.println("Exception occurs when creating directory.");
                System.out.println("Download fail.");
                return ERROR;
            }
        }
        String md5 = args[1];
        File dl;
        if(filename.equals("")){
            dl = new File(dlPath + File.separator + md5 + ".txt");
        }else{
            dl = new File(dlPath + File.separator + filename);
        }

        if(!dl.exists()){
            if(!dl.createNewFile()){
                System.out.println("Exception occurs when creating file.");
                System.out.println("Download fail.");
                return ERROR;
            }
        }
        try(BufferedWriter br = new BufferedWriter(new OutputStreamWriter(
                (new FileOutputStream(dl.getCanonicalPath())), StandardCharsets.UTF_8))){
            ObjectMapper objectMapper = new ObjectMapper();
            String responseStr = Request.Get(endpoint + "/files/" + md5).execute().returnContent().asString(StandardCharsets.UTF_8);
            System.out.println(responseStr);
            Map<String, Object> response = (Map<String, Object>) objectMapper.readValue(responseStr, Map.class);
            Map<String, Object> result = (Map<String, Object>) response.get("result");
            String content = (String) result.get("content");
            br.write(content);
            if(filename.equals("")){
                System.out.printf("File written to %s.txt, download success\n", md5);
            }else {
                System.out.printf("File written to %s, download success\n", filename);
            }
            return responseStr + exten;
        }catch (Exception e){
            e.printStackTrace();
            return ERROR;
        }
    }

    /**
     * Handle "upload" command, post "endpoint/files/:md5" with a body of the file
     *
     * @param args: upload [filename]
     * @return response String if successfully executed
     *         INVALID if wrong number of arguments or file extension not supported
     *         ERROR if error occurs when reading the file
     * @throws IOException
     */
    public static String handleUpload(String[] args) throws IOException {
        if(args.length > 2){
            System.out.println("upload usage: upload [filename]");
            return INVALID;
        }
        if(!checkSupport(args[1])){
            System.out.println("File format not support.");
            return INVALID;
        }
        File file = new File(args[1]);
        if(file.exists()){
            CharsetDetector detector = new CharsetDetector();
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                detector.setText(bytes);
                CharsetMatch charsetMatch = detector.detect();
                String encoding = charsetMatch.getName();
                String content = charsetMatch.getString();
                System.out.println("Detected encoding: "+ encoding);
                String md5 = Utils.calculateMD5(content);
                byte[] contentBytes = content.getBytes(encoding);
                String responseStr = Request.Post(endpoint + "/files/" + md5).bodyByteArray(contentBytes)
                        .execute().returnContent().asString(StandardCharsets.UTF_8);
                System.out.println(responseStr);
                return responseStr;
            } catch (Exception e) {
                e.printStackTrace();
                return ERROR;
            }
        }else{
            System.out.println("File " + args[1] + " not found.");
            return ERROR;
        }
    }

    /**
     * check whether the file extension is supported
     * @param arg: file name
     * @return true: support
     *         false: not support
     */
    private static boolean checkSupport(String arg) {
        String[] filename = arg.split("\\.");
        String suffix = filename[filename.length-1];
        for(String s: supportSuffix){
            if(suffix.equals(s)){
                return true;
            }
        }
        return false;
    }

    /**
     * Handle "list" command, send "endpoint/files" to the Server
     *
     * @return response String if executed successfully
     *         ERROR if error occurs
     */
    public static String handleList(){
        try {
            String responseStr = Request.Get(endpoint + "/files").execute().returnContent().asString(StandardCharsets.UTF_8);
            System.out.println(responseStr);
            return responseStr;
        } catch (IOException e) {
            e.printStackTrace();
            return ERROR;
        }
    }

    private static void printUsage() {
        System.out.println("Usage: [op] [params]");
        System.out.println("Available Operation: upload, download, compare, exists, list(no params)");
    }

    // source: https://www.baeldung.com/java-md5
    // not used
    public static String calculateMD5(byte[] bytes){
        return "";
    }
}
