package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Response {
    int code;
    String message;
    ObjectNode result;

    static ObjectMapper objectMapper = new ObjectMapper();

    public Response(int code, String message) {
        this.code = code;
        this.message = message;
        this.result = objectMapper.createObjectNode();
    }

    public Response(int code, String message, ObjectNode result) {
        this.code = code;
        this.message = message;
        this.result = result;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public ObjectNode getResult() {
        return result;
    }

    public Response setCode(int code) {
        this.code = code;
        return this;
    }

    public Response setMessage(String message) {
        this.message = message;
        return this;
    }

    public static void setObjectMapper(ObjectMapper objectMapper) {
        Response.objectMapper = objectMapper;
    }
}
