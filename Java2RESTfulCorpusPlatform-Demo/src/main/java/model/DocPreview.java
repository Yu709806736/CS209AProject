package model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * This class stands for a class stored the value of md5, length, and preview.
 * This class can be easily resolved to TableColumn value in JavaFX.
 * It can be understood as the JavaFX-used Document class(replace the content value by preview).
 */
public class DocPreview{
    StringProperty md5;
    StringProperty length;
    StringProperty preview;
    public DocPreview(String md5, int len, String preview){
        this.length = new SimpleStringProperty(String.valueOf(len));
        this.md5 = new SimpleStringProperty(md5);
        this.preview = new SimpleStringProperty(preview);
    }

    public DocPreview(Document document){
        this.length = new SimpleStringProperty(String.valueOf(document.getLen()));
        this.md5 = new SimpleStringProperty(document.getMd5());
        this.preview = new SimpleStringProperty(document.getSimplePreview());
    }

    public void setLength(int length) {
        this.length.set(String.valueOf(length));
    }

    public int getLength() {
        return Integer.parseInt(length.get());
    }

    public void setMd5(String md5) {
        this.md5.set(md5);
    }

    public String getMd5() {
        return md5.get();
    }

    public void setPreview(String preview) {
        this.preview.set(preview);
    }

    public String getPreview() {
        return preview.get();
    }

    public StringProperty lengthProperty() {
        return length;
    }

    public StringProperty md5Property() {
        return md5;
    }

    public StringProperty previewProperty() {
        return preview;
    }

    @Override
    public String toString() {
        return "model.DocPreview{" +
                "md5='" + md5 + '\'' +
                ", length=" + length +
                ", preview='" + preview + '\'' +
                '}';
    }
}
