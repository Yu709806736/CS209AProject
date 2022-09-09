package util;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {


    // source: https://www.baeldung.com/java-md5
    public static String calculateMD5(byte[] bytes){
        //TODO - finished
        String algo = "MD5";
        try{
            MessageDigest messageDigest = MessageDigest.getInstance(algo);
            messageDigest.update(bytes);
            return DatatypeConverter.printHexBinary(messageDigest.digest()).toUpperCase();
        }catch (NoSuchAlgorithmException e){
            System.out.println("There's no algorithm named " + algo);
        }
        return null;
    }

    public static String calculateMD5(String str){
        return calculateMD5(str.getBytes(StandardCharsets.UTF_8));
    }
}
