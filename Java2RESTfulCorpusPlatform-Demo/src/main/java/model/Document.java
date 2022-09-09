package model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import util.Utils;

import java.nio.charset.StandardCharsets;

public class Document {
    private String md5;  //md5
    private int len;
    private String content;

    public Document(){
        this.md5 = "";
        this.content = "";
    }

    //create Document Object given a single variable content
    public Document(String content){
        this.md5 = Utils.calculateMD5(content);
        this.content = content;
        this.len = content.length();
    }

    //create Document Object given two variables content and md5
    public Document(String md5, String content){
        this.md5 = md5;
        this.content = content;
        this.len = content.length();
    }


    public String getMd5() {
        return md5;
    }

    public void setMd5(String name) {
        this.md5 = name;
    }

    public String getContent() {
        return content;
    }

    //get the preview by String.substring()
    public String getSimplePreview(){
        return ((content.length() > 100) ?
                new String(content.substring(0, 100).getBytes(StandardCharsets.UTF_8)) : content);
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getLen(){
        return content.length();
    }

    //store the md5, length, and preview into an ObjectNode
    public ObjectNode getPreview(){
        String preview = getSimplePreview();
        ObjectNode result = (new ObjectMapper()).createObjectNode();
        result.put("md5", md5);
        result.put("length", content.length());
        result.put("preview", preview);
        return result;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("md5 = " + md5 + "\n");
        sb.append("length = " + len + "\n");
        sb.append("content = " + content);
        return sb.toString();
    }

}
