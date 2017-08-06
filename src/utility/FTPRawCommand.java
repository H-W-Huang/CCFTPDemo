package utility;

/**
 * Created by hw on 17/4/3.
 */
public class FTPRawCommand {

    public static String PASV(){
        return "PASV\r\n";
    }

    public static String USER(String username){
        return "USER "+username+"\r\n";
    }

    public static String PASS(String password){
        return "PASS "+password+"\r\n";
    }

    public static String LIST(String path){
        return "LIST "+path+"\r\n";
    }

    public static String NLST(String path){
        return "NLST "+path+"\r\n";
    }

    public static String PWD(){
        return "PWD\r\n";
    }

    public static String CWD(String path){
        return "CWD "+path+"\r\n";
    }

    public static String MKD(String path){
        return "MKD "+path+"\r\n";
    }

    public static String RMD(String path){
        return "RMD "+path+"\r\n";
    }

    public static String DELE(String path){
        return "DELE "+path+"\r\n";
    }

    public static String RETR(String path){
        return "RETR "+path+"\r\n";
    }

    public static String STOR(String path){
        return "STOR "+path+"\r\n";
    }

    public static String SIZE(String path){
        return "SIZE "+path+"\r\n";
    }

    public static String QUIT(){
        return "QUIT\r\n";
    }




}
