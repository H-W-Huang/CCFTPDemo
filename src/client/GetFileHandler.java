package client;

import myInterfaces.Feedbackable;

import java.io.*;
import java.net.Socket;

/**
 * Created by hw on 17/4/4.
 */
public class GetFileHandler implements Runnable {

    private Socket dataSocket;
    private File target;
    private long byteHadGet;
    private Feedbackable feedBack;

    public GetFileHandler(Socket dataSocket, File target, Feedbackable feedBack) {
        this.dataSocket = dataSocket;
        this.target = target;
        this.feedBack = feedBack;
    }

    @Override
    public void run() {

        try {
            BufferedInputStream bis = new BufferedInputStream(dataSocket.getInputStream());
            FileOutputStream fos = new FileOutputStream(this.target);
            byte[] buffer  = new byte[1024];
            int count = 1;
            System.out.println("retrieving...");
            while( count !=-1 || bis.available()!=0 ){
                count = bis.read(buffer,0,buffer.length);
                if(count == -1) break;
                byteHadGet+=count;
                feedBack.feedback(byteHadGet);
                fos.write(buffer,0,count);
                fos.flush();
            }
            fos.flush();
            fos.close();
            bis.close();
            System.out.println();
            System.out.println("Download Completed!");
        } catch (FileNotFoundException e) {
            System.out.println("Encounter Error!Failed!");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Encounter Error!Failed!");
            e.printStackTrace();
        }


    }





}
