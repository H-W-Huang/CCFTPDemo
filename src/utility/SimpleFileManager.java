package utility;

import java.io.File;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * Created by hw on 17/4/3.
 */
public class SimpleFileManager {


    public static File currentDir;
    public static String FILE_SEPARATOR ;

    static {
        currentDir = new File("/Users/hw/Desktop/FTPFiles");
        FILE_SEPARATOR = System.getProperty("file.separator");
    }

    /**
     * 显示本地当前的工作目录
     * @return
     */
    public static String lpwd(){
        String result = "";
        result = currentDir.getAbsolutePath();
        return result;
    }

    /**
     * 本地切换目录
     * @param path
     * @return
     */
    public static String lcd(String path){
        String result = "";
        File destination = null;

        String pwd = lpwd();
        String destinationPath = interpretPath(path,pwd);
        destination = new File(destinationPath);


        if(destination.exists()){
            currentDir = destination;
        }else{
            result = "No such Directory!";
        }
        return result;
    }


    /**
     * 显示当前目录
     * @return
     */
    public static String lls(String path){
        StringBuffer result = new StringBuffer();
        String[] recordsInCurrentDir = null ;
        String pwd = lpwd();
        result.append("");

        if(path == null){
            recordsInCurrentDir = currentDir.list();
        }else{
            recordsInCurrentDir = new File(interpretPath(path,pwd)).list();
        }
        for (String record :recordsInCurrentDir) {
            result.append(record);
            result.append("\n");
        }
        return result.toString();
    }

    /**
     * 本地创建目录
     * @return
     */
    public static String lmkdir(String path){
        String result = "";
        String pwd = lpwd();
        String newDirPath = interpretPath(path,pwd);
//        System.out.println("--->"+newDirPath);
        File newDir = new File(newDirPath);
        if(!newDir.exists()) {
            result = (newDir.mkdir())?"":"Cannot create the directory!";
        }else{
            result = "Directory "+newDir.getName()+" exists!";
        }
        return result;
    }


    /**
     *
     * @param path
     * @return
     */
    public static String lrm(String path){
        String result = "";
        String pwd = lpwd();
        String filePath = interpretPath(path,pwd);
        if(filePath!=null){
            File target = new File(filePath);
            if(!target.exists()){
                result = "Not such File/Directory!";
            }
            else if (target.isDirectory()){
                System.out.println(target.getName()+"is a directory?Are you sure to remove it(yes/no)?");
                Scanner in = new Scanner(System.in);
                String option = in.next();
                switch (option){
                    case "yes":
                        result = target.delete()?"":"Delete Failed!";
                        break;
                    case "no":
                        break;
                    default:
                        break;
                }
            }else if(target.isFile()){
                result = target.delete()?"":"Delete Failed!";
            }
        }
        return result;
    }




    /**
     * 解析路径
     * 最终得到绝对路径
     * @param path
     * @return
     */
    public static String interpretPath(String path,String pwd){
        String destinationPath = null;
        if (path != null) {
            //暂时只处理类UnixOS的文件结构,即目录树
            if(path.startsWith("/")){
                //处理绝对路径
                destinationPath = path;
            }else{
                //处理相对路径
                //去掉显式当前目录标识
                if(path.startsWith("./")){
                    path = path.substring(2);
                }
                //处理返回上一级目录的操作
                if(path.equals("../")){
                    if(pwd.equals("/")){
//                        result = "Cannot reach up anymore!";
                    }else{
                        destinationPath = pwd;
                        destinationPath = destinationPath.substring(0,destinationPath.lastIndexOf(FILE_SEPARATOR));
                    }
                }else {
                    String[] recordsInCurrentDir = currentDir.list();
                    if (recordsInCurrentDir != null) {
                        destinationPath = pwd + FILE_SEPARATOR + path;
                    }
                }
            }
        }
        return destinationPath;
    }


}
