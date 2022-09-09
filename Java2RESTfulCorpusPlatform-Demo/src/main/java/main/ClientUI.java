package main;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fxml.Controller;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import model.DocPreview;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * class for the main UI stage control, and connect the fxml controller with the Client without GUI
 */
public class ClientUI extends Application {

    private AnchorPane index;
    private Stage primaryStage;
    private static Client client = null;
    /**
     * file extensions that can be read
     */
    private static String[] supportSuffix = {"txt", "java", "py", "c", "cpp", "csv"};

    /**
     * to store DocPreview Objects, would be used to set TableView in GUI
     */
    private ObservableList<DocPreview> previewList = FXCollections.observableArrayList();

    @FXML
    private void initialize(){
        client = new Client();
        try {
            FXMLLoader loader = new FXMLLoader();
            //get URL of the *.fxml file, "xxx.class.getResources(fxml_path)" might be ok but not for my laptop
            File file = new File("src\\main\\java\\fxml\\mainUI.fxml");
            URL url = file.toURI().toURL();
            loader.setLocation(url);
            System.out.println(loader.getLocation());
            index = loader.load();

            Scene scene = new Scene(index);
            primaryStage.setScene(scene);

            Controller controller = loader.getController();
            controller.setMainUI(this);

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage){
        primaryStage = stage;
        primaryStage.setTitle("Client");
        initialize();
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Handle "upload" command:
     *    get arguments from UI controller, and send them to Client
     *    get response String from Client, and send it to Controller
     * @param filename
     * @return Map of response if the response String is normal
     *         null if the response from Client is INVALID
     *         Map with an entry ("error": ?) if the response from Client is ERROR
     * @throws IOException
     */
    public Map<String, Object> handleUpload(String filename) throws IOException {
        String[] args = new String[]{"upload", filename};
        String res = client.handleUpload(args);
        if(res.equals(client.INVALID)){
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        if(res.equals(client.ERROR)) {
            return (Map<String, Object>) objectMapper.readValue("{\"error\": 1}", Map.class);
        }
        Map<String, Object> response = (Map<String, Object>) objectMapper.readValue(res, Map.class);
        return response;
    }

    /**
     * Handle "download" command:
     *    get arguments from UI controller, and send them to Client
     *    get response String from Client, and send it to Controller
     *    alert if the entered download file extension is not supported
     * @param arg: [md5] [(optional)file name]
     * @return Map of response if the response String is normal
     *         null if the response from Client is ERROR
     * @throws IOException
     */
    public Map<String, Object> handleDownload(String[] arg) throws IOException {
        setDlPath();
        String[] args;
        if(arg.length == 1){
            args = new String[]{"download", arg[0]};
        }else {
            args = new String[]{"download", arg[0], arg[1]};
        }
        String res = client.handleDownload(args);
        if(res.contains(client.BADFORMAT)){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(getStage());
            alert.setTitle("File Format Forbidden");
            alert.setHeaderText("Format Not Support.");
            alert.setContentText("Please write a file name with a file extension as one of: " + support() + ".");
            alert.showAndWait();
        }
        ObjectMapper objectMapper = new ObjectMapper();
        if(res.equals(client.ERROR)) {
            return null;
        }
        Map<String, Object> response = (Map<String, Object>) objectMapper.readValue(res, Map.class);
        return response;
    }

    /**
     * to set the download path, choose the download target directory
     */
    private void setDlPath(){
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choose download directory");
        File dl = new File("download\\");
        boolean dirExist = true;
        if(!dl.exists()){
            dirExist = dl.mkdir();
        }
        //if create directory failed then use the default initial directory
        if(dirExist){
            dc.setInitialDirectory(dl);
        }
        File f = dc.showDialog(primaryStage);
        if(f != null) {
            String path = f.getAbsolutePath();
            if(client == null) {
                client = new Client(path);
            }else{
                client.setDlPath(path);
            }
        }else{
            client = new Client();
        }
    }

    /**
     * Handle "exists" command:
     *    get arguments from UI controller, and send them to Client
     *    get response String from Client, and send it to Controller
     * @param filename
     * @return response String if the response from Client is normal
     *         null if response from Client is INVALID
     *         Map with an entry ("error": ?) if the response from Client is ERROR
     * @throws IOException
     */
    public Map<String, Object> handleExists(String filename) throws IOException {
        String[] args = new String[]{"exists", filename};
        String res = client.handleExists(args);
        if(res.equals(client.INVALID)){
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        if(res.equals(client.ERROR)){
            return (Map<String, Object>) objectMapper.readValue("{\"error\": 1}", Map.class);
        }
        Map<String, Object> response = (Map<String, Object>) objectMapper.readValue(res, Map.class);
        return response;
    }

    /**
     * Handle "compare" command:
     *      get arguments from UI controller, and send them to Client
     *      get response String from Client, and send it to Controller
     * @param param1: md5_1 or file_name_1
     * @param param2: md5_2 or file_name_2
     * @return response String if response from the Client is normal
     *         null if response is INVALID
     *         Map with an entry ("error": ?) if the response from Client is ERROR
     * @throws IOException
     */
    public Map<String, Object> handleCompare(String param1, String param2) throws IOException {
        String[] args = new String[]{"compare", param1, param2};
        String res = client.handleCompare(args);
        if(res.equals(client.INVALID)){
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        if(res.equals(client.ERROR)){
            return (Map<String, Object>) objectMapper.readValue("{\"error\": 1}", Map.class);
        }
        Map<String, Object> response = (Map<String, Object>) objectMapper.readValue(res, Map.class);
        return response;
    }

    /**
     * Handle "compare" command:
     *     get response String from Client, and set the ObservableList in this class for TableView of GUI
     *     terminate if response is ERROR
     */
    public void handleList(){
        String res = client.handleList();
        if(res.equals(client.ERROR)){
            System.out.println("Error occurs when sending get request to Server.");
            return;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        if(this.previewList != null) {
            this.previewList.clear();
        }
        try {
            Map<String, Object> response = (Map<String, Object>)objectMapper.readValue(res, Map.class);
            int code = (int) response.get("code");
            if(code != 0){
                System.out.println(response.get("message"));
            }else{
                Map<String, Object> result = (Map<String, Object>) response.get("result");
                if(result.get("files") == null){
                    return;
                }
                String s = result.get("files").toString();
                s = s.replace("[{", "").replace("}]", "");
                String[] previews = s.split("}, \\{");
                for(String p: previews) {
                    String[] previewItems = p.split(", ");
                    String md5, pre;
                    int length;
                    md5 = previewItems[0].split("=")[1];
                    pre = previewItems[2].split("=")[1];
                    length = Integer.parseInt(previewItems[1].split("=")[1]);
                    DocPreview dp = new DocPreview(md5, length, pre);
                    this.previewList.add(dp);
                }
            }
        } catch (JsonProcessingException e) {
            System.out.println(client.ERROR);
            e.printStackTrace();
        }
    }

    public Stage getStage(){
        return primaryStage;
    }

    public ObservableList<DocPreview> getPreviewList(){
        return previewList;
    }

    /**
     * get the supported extensions name ---- used for printing
     * @return String
     */
    private String support(){
        StringBuilder sb = new StringBuilder();
        for(String s: supportSuffix){
            sb.append("\"" + s + "\"");
            sb.append(", ");
        }
        sb.delete(sb.lastIndexOf(", "), sb.lastIndexOf(", ")+2);
        return sb.toString();
    }
}

