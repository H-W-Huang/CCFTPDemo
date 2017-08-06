package client;

import myInterfaces.Feedbackable;

import java.io.*;
import java.net.Socket;

/**
 * Created by hw on 17/4/4.
 */
public class SendFileHandler implements Runnable {

    private Socket dataSocket;
    private File target;
    private long byteHadSent;
    private Feedbackable feedBack;

    public SendFileHandler(Socket dataSocket, File target,Feedbackable feedBack) {
        this.dataSocket = dataSocket;
        this.target = target;
        this.feedBack = feedBack;
    }

    @Override
    public void run() {

        try {
            BufferedOutputStream bos = new BufferedOutputStream(dataSocket.getOutputStream());
            FileInputStream fis = new FileInputStream(target);
            byte[] buffer = new byte[1024];
            int count = 0;
            System.out.println("uploading...");
            while(count!=-1 || fis.available()!=0 ){
                count = fis.read(buffer,0,buffer.length);
                if(count == -1 ) {
                    break;
                }
                byteHadSent+=count;
                feedBack.feedback(byteHadSent);
                bos.write(buffer,0,count);
                bos.flush();
            }
            bos.flush();
            bos.close();
            fis.close();
            System.out.println();
            System.out.println("Upload Completed!");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public long getByteHadSent() {
        return byteHadSent;
    }
}
