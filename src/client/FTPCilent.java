package client;

import entity.ProcessPercentage;
import myInterfaces.Feedbackable;
import utility.FTPRawCommand;
import utility.SimpleFileManager;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by hw on 17/4/3.
 */
public class FTPCilent implements Feedbackable {

//    private String ftpServerIP = "10.211.55.7";
    private String ftpServerIP = null;
    private Socket controlSocket;
    private Scanner in;
    private ProcessPercentage processPercentage = null;



    public FTPCilent(){

    }

    public FTPCilent(String ftpServerIP){
        this.ftpServerIP = ftpServerIP;
    }


    public void run(){

        in = new Scanner(System.in);
        processPercentage = new ProcessPercentage();
        String inputCommand = "";


        if(ftpServerIP == null){
            System.out.print("Please enter the ip of your FTP Server:");
            ftpServerIP = in.nextLine().trim();
        }

        if(!ftpServerIP.isEmpty()) {
            try {

                boolean isLoginSuccessful = false;
                int loginAttemptCount = 3;  //允许尝试登陆三次
                //尝试登陆
                System.out.println("FTP server IP:"+ftpServerIP);

                while( loginAttemptCount > 0 && !isLoginSuccessful ) {

                    System.out.print("username:");
                    String username = in.nextLine();
//                    String username = "hw";
                    System.out.print("password:");
                    String password = in.nextLine();
//                    String password = "root123";

                    // 判断用户名 用户名为空时，使用匿名登录
                    if(username.isEmpty()){
                        username = "anonymous";
                    }

                    isLoginSuccessful = login(username,password);
                    if(!isLoginSuccessful){
                        System.out.println("Login Failed!");
                    }
                    loginAttemptCount--;
                }


                outer:
                while (isLoginSuccessful) {
                    System.out.print("CCFtp>");
                    inputCommand = in.nextLine();

                    if (inputCommand.isEmpty()) continue;

                    String[] commands = intepretCommand(inputCommand);
                    String mainCommand = commands[0]; //主命令
                    String subCommands[] = new String[10];
                    for (int i = 1; i < commands.length; i++) {
                        subCommands[i-1] = commands[i];
                    }

                    String subCommand = subCommands[0];
                    

                    switch (mainCommand) {

                        //本地操作组
                        case "lpwd":
                            System.out.println(SimpleFileManager.lpwd());
                            break;
                        case "lls":
                            System.out.println(SimpleFileManager.lls(subCommand));
                            break;
                        case "lcd":
                            if(subCommand!=null) System.out.println(SimpleFileManager.lcd(subCommand));
                            break;
                        case "lmkdir":
                            if(subCommand!=null) System.out.println(SimpleFileManager.lmkdir(subCommand));
                            break;
                        case "lrm":
                            if(subCommand!=null) System.out.println(SimpleFileManager.lrm(subCommand));
                            else System.out.println("Prop Usage:lrm <file>");
                            break;

                        //服务器操作组
                        case "pwd":
                            System.out.println(getPDW(controlSocket));
                            break;
                        case "ls":
                            dealWithls(controlSocket,subCommand);
                            break;
                        case "cd":
                            dealWithcd(controlSocket,subCommand);
                            break;
                        case "rm":
                            dealWithrm(controlSocket,subCommand);
                            break;
                        case "mkdir":
                            dealWithmkdir(controlSocket,subCommand);
                            break;
                        case "rmdir":
                            dealWithrmdir(controlSocket,subCommand);
                            break;
                        case "get":
                            String destination = subCommands[1];
                            System.out.println("destination:"+destination);
                            if(destination == null)
                                dealWithget(controlSocket,subCommand);
                            else
                                dealWithget(controlSocket,subCommand,destination);
                            break;
                        case "send":
                            destination = subCommands[1];
                            System.out.println("destination:"+destination);
                            if(destination == null)
                                dealWithsend(controlSocket,subCommand);
                            else
                                dealWithsend(controlSocket,subCommand,destination);
                            break;
                        //其他操作组
                        case "help":
                            dealWithhelp();
                            break;
                        case "quit":
                            if(dealWithQuit(controlSocket)){
                                System.out.println("Bye-Bye!");
                            }else{
                                System.out.println("Exit improperly!");
                            }
                            break outer;
                        default:
                            System.out.println("Invalid command.Please use 'help' command to see the usage of CCFtp.");
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally{
                try {
                    controlSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else{
            System.out.println("Invalid IP.Please rerun the program.");
        }
    }



    /**
     * 切分命令
     * @param command
     * @return
     */
    private String[] intepretCommand(String command){
        String[] subCommands = command.split(" ");
        return subCommands;
    }

    /**
     * 获取响应中的响应码
     * @param response
     * @return
     */
    private String getResponseCode(String response){
        String responseCode = response.split(" ")[0].trim();
        return responseCode;
    }

    /**
     * 获取响应中的响应描述
     * @param response
     * @return
     */
    private String getResponseInfo(String response){
        String responseInfo = response.split(" ")[1].trim();
        return responseInfo;
    }




    /**
     * 连接FTP服务器
     * @param ip
     * @param port
     * @param username
     * @param password
     * @return
     * @throws Exception
     */
    public String connect(String ip,int port,String username,String password) throws Exception{
        String result = "";
        //连接服务器
        controlSocket = new Socket(ip,port);
        read(controlSocket);
        //用户登录
        if(controlSocket!=null && controlSocket.isConnected()){
            write(controlSocket, FTPRawCommand.USER(username));
            read(controlSocket);
            write(controlSocket, FTPRawCommand.PASS(password));
            result =  read(controlSocket);
        }else{
            result = "Cannot connect to the FTP server!";
        }
        return result;
    }


    /**
     * 根据FTP服务器返回的信息,得到IP和端口号<br/>
     * 并返回对应的Socket
     * @param info
     * @return
     * @throws Exception
     */
    private Socket getDataTransferSocket(String info) throws Exception{
        String[] responseData = info.substring(info.indexOf("(")+1,info.indexOf(")")).split(",");
        String ip = responseData[0]+"."+responseData[1]+"."+responseData[2]+"."+responseData[3];
        int port = Integer.valueOf(responseData[4])*256+Integer.valueOf(responseData[5]);
        Socket dataSocket = new Socket(ip,port);
        return dataSocket;
    }

    public String read(Socket socket) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
        byte[] buffer = new byte[1024];
        int count = bis.read(buffer,0,buffer.length);
        String info = count==-1 ? "":new String(buffer,0,count,"UTF-8");
//        System.out.println(info);
        return info;
    }



    public String read(File file) throws IOException {
        Scanner fin = new Scanner(file);
        StringBuilder infoBuffer = new StringBuilder();
        while(fin.hasNextLine()){
            infoBuffer.append(fin.nextLine());
            infoBuffer.append("\n");
        }
        return infoBuffer.toString();
    }

    public void write(Socket socket,String content) throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        out.print(content);
        out.flush();
    }


    /**
     * 登陆
     */
    private boolean login(String username,String password) throws Exception {
        boolean isLoginSuccessful = false;
        //尝试登陆
        String response = connect(ftpServerIP, 21, username, password);
        String responseCode = getResponseCode(response);

        if(responseCode.equals("230")){
            System.out.println("Login successfully!");
            isLoginSuccessful = true;
        }else{
            controlSocket.close();
        }

        return isLoginSuccessful;
    }


    //--------------------- 各个case对应的处理方法 ------------------------

    /**
     * 获取服务器当前工作路径
     * @param controlSocket
     * @return
     * @throws Exception
     */
    private String getPDW(Socket controlSocket) throws Exception{
        write(controlSocket,FTPRawCommand.PWD());
        String response = read(controlSocket);
        String resonseInfo = getResponseInfo(response);
        String pwd = resonseInfo.substring(1,resonseInfo.lastIndexOf("\""));
        return pwd;
    }


    /**
     * 处理ls命令
     * 可以用ls
     * 也可以用ls -l
     * @param controlSocket
     * @param subCommand
     * @throws Exception
     */
    private void dealWithls(Socket controlSocket,String subCommand) throws Exception{
        String ftpRawCommand = null;
        String pwd =  getPDW(controlSocket);

        if(subCommand != null){
            if(subCommand.equals("-l")) {
                ftpRawCommand = FTPRawCommand.LIST(pwd);
            }
        }else{
            ftpRawCommand = FTPRawCommand.NLST(pwd);
        }

        if(ftpRawCommand!=null){
            //使用NLST命令
            write(controlSocket,FTPRawCommand.PASV());
            String info = read(controlSocket);
            Socket dataSocket = getDataTransferSocket(info);
            write(controlSocket,ftpRawCommand);
            read(controlSocket);
            String response = read(dataSocket);
            String lsResult = response.isEmpty()?"":response.replaceAll(pwd+"/","");
            System.out.println(lsResult);
            read(controlSocket);//读取完最后的响应
            dataSocket.close();
        }
    }


    /**
     * 处理cd命令
     * @param controlSocket
     * @param subCommand
     */
    private void dealWithcd(Socket controlSocket,String subCommand) throws Exception {
        if(subCommand!=null){
            String pwd = getPDW(controlSocket);
            String fullPath = SimpleFileManager.interpretPath(subCommand,pwd);
            write(controlSocket,FTPRawCommand.CWD(fullPath));
            read(controlSocket);

            //新旧pwd比较,用来确定输入的路径是否存在
            String newPwd = getPDW(controlSocket);
            if(newPwd.equals(pwd)){
                System.out.println(subCommand+" doesn't exist!");
            }
        }else{
            System.out.println("Proper Usage:cd <directory>");
        }
    }

    /**
     * 处理mkdir
     * @param controlSocket
     * @param subCommand
     * @throws Exception
     */
    private void dealWithmkdir(Socket controlSocket, String subCommand) throws Exception{
        if(subCommand!=null){
            String pwd = getPDW(controlSocket);
            String fullPath = SimpleFileManager.interpretPath(subCommand,pwd);
//            System.out.println(fullPath);
            write(controlSocket,FTPRawCommand.MKD(fullPath));
            String response = read(controlSocket);
            String responseCode = getResponseCode(response);
            if(responseCode.equals("550")){
                System.out.println("Cannot create directory "+subCommand);
            }
        }else{
            System.out.println("Proper Usage:mkdir <directory>");
        }
    }


    /**
     * 处理rm命令
     * @param controlSocket
     * @param subCommand
     * @throws Exception
     */
    private void dealWithrm(Socket controlSocket, String subCommand) throws Exception{
        if(subCommand!=null){
            String pwd = getPDW(controlSocket);
            String fullPath = SimpleFileManager.interpretPath(subCommand,pwd);
            write(controlSocket,FTPRawCommand.DELE(fullPath));
            String response = read(controlSocket);
            String responseCode = getResponseCode(response);
            if(responseCode.equals("550")){
                System.out.println("Cannot remove file "+subCommand);
            }
        }else{
            System.out.println("Proper Usage:rm <file>");
        }
    }

    /**
     * 处理rmdir命令
     * @param controlSocket
     * @param subCommand
     * @throws Exception
     */
    private void dealWithrmdir(Socket controlSocket, String subCommand) throws Exception{
        if (subCommand != null){
            String pwd = getPDW(controlSocket);
            String fullPath = SimpleFileManager.interpretPath(subCommand,pwd);
            write(controlSocket,FTPRawCommand.RMD(fullPath));
            String response = read(controlSocket);
            String responseCode = getResponseCode(response);
            if(responseCode.equals("550")){
                System.out.println("Cannot remove directory "+subCommand);
            }
        }else{
            System.out.println("Proper Usage:rmdir <directory>");
        }
    }




    private void dealWithget(Socket controlSocket, String subCommand) throws Exception{
        if(subCommand != null){
            dealWithget(controlSocket,subCommand,subCommand);
        }else{
            System.out.println("Proper Usage:get <file> or get <source> <destination>");
        }
    }

    private void dealWithget(Socket controlSocket, String source,String destination) throws Exception{
        if (source != null){
            //获取文件的全路径
            String pwd = getPDW(controlSocket);
            String fullPath = SimpleFileManager.interpretPath(source,pwd);
            //首先获取文件大小
            write(controlSocket,FTPRawCommand.SIZE(fullPath));
            String response = read(controlSocket);
            String responseCode = getResponseCode(response);
            String responseInfo = getResponseInfo(response);
            long filesize = 0l;

            //判断是否有文件大小
            if(responseCode.equals("213")){
                //确定FTP服务器存在文件
                filesize = Long.valueOf(responseInfo);
//                System.out.println("fileSize:"+responseInfo);
                processPercentage.setFullsize(filesize);

                //检查本地当前目录是否有同名文件
                String lpwd = SimpleFileManager.lpwd();
                String targetPath = SimpleFileManager.interpretPath(destination,lpwd);
                File target = new File(targetPath);
                if(target.exists() && target.isFile()){
                    System.out.println("File "+destination+" has already existed!");
                }else {
                    //确定不存在同名文件
                    //进入被动模式
                    write(controlSocket, FTPRawCommand.PASV());
                    response = read(controlSocket);
                    Socket dataSocket = getDataTransferSocket(response);

                    //开始获取文件
                    write(controlSocket, FTPRawCommand.RETR(fullPath));
                    read(controlSocket);
                    BufferedInputStream bis = new BufferedInputStream(dataSocket.getInputStream());

                    //开启一个新的线程来获取文件
                    GetFileHandler handler = new GetFileHandler(dataSocket,target,this);
                    Thread downloadThread = new Thread(handler);
                    downloadThread.run(); //此处暂时不使用多线程

                    dataSocket.close();
                    read(controlSocket);

                }
            }else{
                System.out.println("File "+source+" dosen't exist!");
            }

        }else{
            System.out.println("Proper Usage:get <source> <destination>");
        }
    }




    private void dealWithsend(Socket controlSocket, String subCommand) throws  Exception {
        if(subCommand!=null){
            dealWithsend(controlSocket,subCommand,subCommand);
        }else{
            System.out.println("Proper Usage:send <file> or send <source> <destination>");
        }
    }

    private void dealWithsend(Socket controlSocket, String source,String destination) throws  Exception{

        if(source != null){
            //1.获取文件路径
            String lpwd = SimpleFileManager.lpwd();
            String fullPath = SimpleFileManager.interpretPath(source,lpwd);
            File sourceFile = new File(fullPath);

            //2.检查文件
            if(sourceFile.exists()){
                //确认了本地文件的存在
                String response = null;
                String responseCode = null;
                String responseInfo = null;
                boolean rewrite = true;

                processPercentage.setFullsize(sourceFile.length());

                //获取FTP服务其目标文件名
                String pwd = getPDW(controlSocket);
                String fullPathOfTarget =SimpleFileManager.interpretPath(destination,pwd);

                //检查FTP服务器的pwd是否存在同名文件
                write(controlSocket,FTPRawCommand.SIZE(fullPathOfTarget));
                response = read(controlSocket);
//                System.out.println("SIZE:"+response);
                responseCode = getResponseCode(response);
                if(responseCode.equals("213")){
                    System.out.print(destination+" has already been in the server.Do you want to rewrite it?(y/n)");
                    String option = in.nextLine();
                    if(option.trim().equals("n")){
                        rewrite = false;
                    }else if(!option.trim().equals("y")){
                        rewrite = false;
                        System.out.println("Unkonw Ooption.Prosess will be terminated.");
                    }
                }

                if(rewrite) {
                    //进入被动模式
                    write(controlSocket, FTPRawCommand.PASV());
                    response = read(controlSocket);
//                    System.out.println(response);
                    //开始发送文件
                    write(controlSocket, FTPRawCommand.STOR(fullPathOfTarget));
                    Socket dataSocket = getDataTransferSocket(response);
                    response = read(controlSocket);
//                    System.out.println(response);
                    responseCode = getResponseCode(response);
                    if (responseCode.equals("150")) {
                        //150 Ok to send data.
                        SendFileHandler handler = new SendFileHandler(dataSocket, sourceFile,this);
                        Thread uploadThread = new Thread(handler);
                        uploadThread.run();
//                        读取响应226 File receive OK.
                        response = read(controlSocket);
//                        System.out.println(response);
                    }else if (responseCode.equals("550")){
                        System.out.println("Permission denied.");
                    }else if (responseCode.equals("553")){
                        System.out.println("Could not create file.");
                    }
                    dataSocket.close();
                }
            }else{
                System.out.println("File "+source+" dosen't exist.");
            }

        }else{
            System.out.println("Proper Usage:send <source> <destination>");
        }
    }

    /**
     * 处理help命令
     */
    private void dealWithhelp() throws Exception {
        String path = System.getProperty("user.dir");
        String separator = System.getProperty("file.separator");
        String helpFilePath = path+separator+"usage.txt";
        System.out.println(path);
        File helpFile = new File(helpFilePath);
        if(helpFile.exists()){
            String helpMessage = read(helpFile);
            System.out.println(helpMessage);
        }else{
            System.out.println("Help file not found!");
        }
    }




    /**
     * 处理quit命令
     * @param controlSocket
     * @throws IOException
     */
    private boolean dealWithQuit(Socket controlSocket) throws IOException {
        write(controlSocket,FTPRawCommand.QUIT());
        String response = read(controlSocket);
        System.out.println("Quit:"+response);
        String responseCode = getResponseCode(response);
        boolean result =  responseCode.equals("221");
        //421 Time Out
        return result;
    }

    @Override
    public void feedback(long currentsize) {
        processPercentage.setCurrentsize(currentsize);
        double percentage = processPercentage.getPercentage();
//        int processBarFullLen = 20;
//        System.out.println("current:"+processPercentage.getCurrentsize());
//        System.out.println("full:"+processPercentage.getFullsize());

        int processBarcurrentLen = (int) Math.ceil(percentage/5);
//        System.out.println(processBarcurrentLen);

        System.out.print("\r");
        for (int i = 0; i < processBarcurrentLen; i++) {
            System.out.print("=");
        }
        System.out.print(percentage+"%");

    }



}
