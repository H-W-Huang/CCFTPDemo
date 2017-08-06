package main;


import client.FTPCilent;

/**
 * Created by hw on 17/4/3.
 */
public class Main {

    public static void main(String[] args) {

        FTPCilent ftpClient = new FTPCilent();
        try{
            ftpClient.run();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
