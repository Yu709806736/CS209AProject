package fxml;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import main.ClientUI;
import model.DocPreview;
import model.Document;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

/**
 * GUI controller class
 */
public class Controller {
    @FXML
    private TableView<DocPreview> fileList;
    @FXML
    private TableColumn<DocPreview, String> md5;
    @FXML
    private TableColumn<DocPreview, String> length;
    @FXML
    private TableColumn<DocPreview, String> preview;
    @FXML
    private Button uploadButton;
    @FXML
    private Button compareButton;
    @FXML
    private Button existButton;
    @FXML
    private Button downloadButton;
    @FXML
    private Button refreshButton;
    /**
     * the two checkBoxes here is for comparison:
     *     if only one of them is selected ---- one file chosen from TableView, the other chosen from FileChooser(local)
     *     if none of them is selected ---- both files would be chosen from the TableView
     *     if both of them is selected ---- both files would be chosen from local directories
     * the two boolean variables below are to check whether the corresponding checkBox is selected
     */
    @FXML
    private CheckBox checkBox1 = new CheckBox("local file1");
    @FXML
    private CheckBox checkBox2 = new CheckBox("local file1");
    private boolean local1;
    private boolean local2;
    /**
     * label to show the response message
     */
    @FXML
    private Label label = new Label(" ");
    /**
     * to let user enter the download file name
     */
    @FXML
    private TextArea textArea = new TextArea("");

    private ClientUI clientUI;
    private static DocPreview previousSelection;
    private static DocPreview currentSelection;
    private static String[] supportSuffix = {"txt", "java", "py", "c", "cpp", "csv"};

    public void setMainUI(ClientUI clientUI){
        this.clientUI = clientUI;
    }

    /**
     * initialize the UI
     */
    @FXML
    public void initialize() {
        clientUI = new ClientUI();
        //initialize TableView of fileList
        md5.setCellValueFactory(cellData -> cellData.getValue().md5Property());
        length.setCellValueFactory(cellData -> cellData.getValue().lengthProperty());
        preview.setCellValueFactory(cellData -> cellData.getValue().previewProperty());
        clientUI.handleList();
        setTable();
        fileList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (currentSelection == null) {
                        currentSelection = newValue;
                    } else {
                        previousSelection = currentSelection;
                        currentSelection = newValue;
                    }
                }
        );
        //initialize textArea
        textArea.setPromptText("Enter download file name here. The extension should be in one of: " + support() +
                ".");
        //initialize checkBoxes
        checkBox1.selectedProperty().addListener((observable, oldValue, newValue) -> local1 = newValue);
        checkBox2.selectedProperty().addListener((observable, oldValue, newValue) -> local2 = newValue);
    }

    /**
     * Handle when clicking "refresh" button:
     *     execute "list" operation
     *     put values in ObservableList<> of ClientUI to TableView
     */
    @FXML
    public void handleRefresh(){
        if(clientUI == null){
            clientUI = new ClientUI();
        }
        clientUI.handleList();
        setTable();
    }

    /**
     * Handle when clicking "Upload" button:
     *     send arguments to clientUI ---- file name from fileChooser
     *     get Map values from ClientUI
     * put response result on the label
     * alert if error occurs
     * @throws IOException
     */
    @FXML
    private void handleUpload() throws IOException {
        FileChooser fc = addExten();
        fc.setTitle("Choose upload file");
        File file = new File(".");
        boolean dirExist = true;
        if(!file.exists()){
            dirExist = file.mkdir();
        }
        if(dirExist){
            fc.setInitialDirectory(file.getCanonicalFile());
        }
        File f = fc.showOpenDialog(clientUI.getStage());
        if(f != null) {
            Map<String, Object> response = clientUI.handleUpload(f.getAbsolutePath());
            if (noneResponse(response)){
                return;
            }
            if (errorResponse(response)){
                return;
            }
            int code = (int) response.get("code");
            String message = (String) response.get("message");
            if (code == 0) {
                label.setText("Upload success.");
                CharsetDetector detector = new CharsetDetector();
                try {
                    byte[] bytes = Files.readAllBytes(f.toPath());
                    detector.setText(bytes);
                    CharsetMatch charsetMatch = detector.detect();
                    String content = charsetMatch.getString();
                    DocPreview dp = new DocPreview(new Document(content));
                    clientUI.getPreviewList().add(dp);
                    clientUI.handleList();
                    setTable();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                label.setText("Upload failed.\n" + message);
            }
        }
        currentSelection = null;
        previousSelection = null;
    }

    /**
     * @param response - response from ClientUI
     * @return true if response is null
     */
    private boolean noneResponse(Map<String, Object> response) {
        if(response == null){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(clientUI.getStage());
            alert.setTitle("File Format Forbidden");
            alert.setHeaderText("Format Not Support.");
            alert.setContentText("Please select a file with a file extension as one of: " + support() + ".");
            alert.showAndWait();
            return true;
        }
        return false;
    }

    /**
     * @param response - response from ClientUI
     * @return true if response is {error -> 1} (manually defined)
     */
    private boolean errorResponse(Map<String, Object> response) {
        if(response.get("error") != null){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(clientUI.getStage());
            alert.setTitle("Error");
            alert.setHeaderText("Error occurs");
            alert.setContentText("Error occurs when connecting to server");
            alert.showAndWait();
            return true;
        }
        return false;
    }

    /**
     * Handle when clicking "Download" button:
     *     send arguments to clientUI ---- file name from textArea / md5 from TableView
     *     get Map values from ClientUI
     * put response result on the label
     * alert if error occurs
     * @throws IOException
     */
    @FXML
    private void handleDownload() throws IOException {
        if(currentSelection == null){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(clientUI.getStage());
            alert.setTitle("No Selection");
            alert.setHeaderText("No File Selected");
            alert.setContentText("Please select a file in the table before clicking the download button.");
            alert.showAndWait();
            return;
        }
        String md5 = currentSelection.getMd5();
        String dlName = textArea.getText();
        String[] args;
        if(dlName == null){
            args = new String[]{md5};
        }else if(dlName.equals("")){
            args = new String[]{md5};
        }else {
            args = new String[]{md5, dlName};
        }
        Map<String, Object> response = clientUI.handleDownload(args);
        if(response == null){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(clientUI.getStage());
            alert.setTitle("Error");
            alert.setHeaderText("Error occurs");
            alert.setContentText("Error occurs when downloading file " + md5 + ".");
            alert.showAndWait();
            return;
        }
        int code = (int) response.get("code");
        String message = (String) response.get("message");
        if(code == 0){
            label.setText("Download succeeded.");
        }else {
            label.setText("Download failed.\n" + message);
        }
        previousSelection = null;
        currentSelection = null;
    }

    /**
     * Handle when clicking "Exists" button:
     *     send arguments to clientUI ---- file name from FileChooser
     *     get Map values from ClientUI
     * put response result on the label
     * alert if error occurs
     * @throws IOException
     */
    @FXML
    private void handleExists() throws IOException {
        FileChooser fc = addExten();
        fc.setTitle("Choose a local file to check whether it exists in the database");
        File file = new File(".");
        boolean dirExist = true;
        if(!file.exists()){
            dirExist = file.mkdir();
        }
        if(dirExist){
            fc.setInitialDirectory(file.getCanonicalFile());
        }
        File f = fc.showOpenDialog(clientUI.getStage());
        if(f != null) {
            Map<String, Object> response = clientUI.handleExists(f.getAbsolutePath());
            if (noneResponse(response)){
                return;
            }
            if(errorResponse(response)){
                return;
            }
            Map<String, Object> result = (Map<String, Object>) response.get("result");
            boolean exist = (boolean) result.get("exists");
            if (exist) {
                label.setText("File \"" + f.getName() + "\" exists.");
            } else {
                label.setText("File \"" + f.getName() + "\" does not exist.");
            }
        }
        currentSelection = null;
        previousSelection = null;
    }

    /**
     * Handle when clicking "Compare" button:
     *     send arguments to clientUI ---- md5 from TableView or file name from FileChooser
     *     get Map values from ClientUI
     * put response result on the label
     * alert if error occurs
     * @throws IOException
     */
    @FXML
    private void handleCompare() throws IOException {
        Map<String, Object> response;
        if(local1 || local2){
            FileChooser fc = addExten();
            File file = new File(".");
            boolean dirExist = true;
            if(!file.exists()){
                dirExist = file.mkdir();
            }
            if(dirExist){
                fc.setInitialDirectory(file.getCanonicalFile());
            }
            File f1 = fc.showOpenDialog(clientUI.getStage());
            if(local1 && local2) {
                File f2 = fc.showOpenDialog(clientUI.getStage());
                response = clientUI.handleCompare(f1.getAbsolutePath(), f2.getAbsolutePath());
            }else {
                if(currentSelection == null){
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.initOwner(clientUI.getStage());
                    alert.setTitle("No Selection");
                    alert.setHeaderText("No File Selected");
                    alert.setContentText("Please select a file in the table before clicking the compare button. If" +
                            "you want to compare two local files, please select both of the checkboxes.");
                    alert.showAndWait();
                    return;
                }
                String md5 = currentSelection.getMd5();
                response = clientUI.handleCompare(f1.getAbsolutePath(), md5);
            }
        }else {
            if (previousSelection == null || currentSelection == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.initOwner(clientUI.getStage());
                alert.setTitle("No Selection");
                alert.setHeaderText("No File Selected");
                alert.setContentText("Please select two files (by click them one by one)" +
                        " in the table before clicking the compare button. If you want to compare with local files," +
                        " please select one of the checkboxes for 1 local file, or select both for 2 local files.");
                alert.showAndWait();
                return;
            }
            String md51 = previousSelection.getMd5();
            String md52 = currentSelection.getMd5();
            response = clientUI.handleCompare(md51, md52);
        }
        if(noneResponse(response)){
            return;
        }
        if(errorResponse(response)){
            return;
        }
        int code = (int) response.get("code");
        String message = (String) response.get("message");
        if(code == 0){
            Map<String, Object> result = (Map<String, Object>) response.get("result");
            double simpleSim = (double) result.get("simple_similarity");
            int levDis = (int) result.get("levenshtein_distance");
            label.setText(String.format("Compare succeeded.\nSimple similarity: %.5f\n" +
                    "Levenshtein distance: %d.", simpleSim, levDis));
        }else {
            label.setText("Compare failed.\n" + message);
        }
        currentSelection = null;
        previousSelection = null;
    }

    private void setTable(){
        fileList.setItems(clientUI.getPreviewList());
    }

    /**
     * same as ClientUI.support() ---- used for textArea prompt text
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

    private FileChooser addExten(){
        FileChooser fc = new FileChooser();
        for(String s: supportSuffix){
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                    s.toUpperCase() + " files (*." + s + ")", "*." + s);
            fc.getExtensionFilters().add(extFilter);
        }
        return fc;
    }
}
