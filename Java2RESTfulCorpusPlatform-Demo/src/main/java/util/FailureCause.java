package util;

public enum FailureCause{
    FILE_NOT_FOUND(1, "File not found"),
    HASH_NOT_MATCH(2, "Hash does not match"),
    ALREADY_EXIST(3, "File with the same MD5 already exists"),
    // if there're some errors when connecting with database (was frequently used when testing the program)
    DB_ERROR(4, "Exception occurs when connecting database");

    int code;
    String message;

    FailureCause(int code, String message) {
        this.code = code;
        this.message = message;
    }
}