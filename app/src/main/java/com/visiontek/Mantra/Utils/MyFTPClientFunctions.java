package com.visiontek.Mantra.Utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.core.app.ActivityCompat;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.FileOutputStream;

import static androidx.core.content.ContextCompat.checkSelfPermission;

public class MyFTPClientFunctions {

    public FileOutputStream desFileStream;
    public FTPClient mFTPClient = new FTPClient();

    public boolean ftpConnect(String host, String username, String password, int port) {
        boolean status = false;
        try {
            mFTPClient = new FTPClient();
            mFTPClient.setConnectTimeout(30000);
            mFTPClient.connect(host, port);
            try {
                if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {
                    mFTPClient.setDataTimeout(45000);
                    mFTPClient.setSoTimeout(45000);
                    status = mFTPClient.login(username, password);
                    mFTPClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
                    mFTPClient.enterLocalPassiveMode();
                }
            } catch (Exception e) {
                return status;
            }
        } catch (Exception e) {
            return status;
        }
        return status;
    }

    public String Ffinding(String hname, String uname, String pword, String dir_path, int type) {
        String name = "NOFILE";
        boolean state = ftpConnect(hname, uname, pword, 21);
        if (state) {
            try {
                mFTPClient.setDataTimeout(45000);
                mFTPClient.setSoTimeout(45000);
                FTPFile[] ftpFiles = mFTPClient.listFiles(dir_path);
                for (FTPFile ftpFile : ftpFiles) {

                    if (ftpFile.isFile()) {

                        switch (type) {
                            case 1:

                                if (ftpFile.getName().contains("MantraPDS_")) {
                                    name = ftpFile.getName();
                                    return name;
                                }
                                break;
                            case 2:

                                if ( ftpFile.getName().contains("RDService_")) {
                                    name = ftpFile.getName();
                                    return name;
                                }
                                break;

                            case 3:

                                name = ftpFile.getName();
                                return name;
                            case 4:

                                if ( ftpFile.getName().contains("RD_Versions")) {
                                    name = ftpFile.getName();
                                    return name;
                                }
                                break;

                        }
                    }

                }
            } catch(Exception e){
                return "EXCEPTION";
            }
        }
        return name;
    }

    public boolean ftpDownload(String srcFilePath, String desFilePath) {
        boolean status = false;
        try {

            mFTPClient.setDataTimeout(30000);
            mFTPClient.setSoTimeout(15000);
            mFTPClient.setBufferSize(1024 * 1024);
            desFileStream = new FileOutputStream(desFilePath);
            status = mFTPClient.retrieveFile(srcFilePath, desFileStream);
            desFileStream.close();
        } catch (Exception e) {
            System.out.println(e.toString());
            return status;
        }
        return status;
    }


    public void ftpDisconnect() {
        if (mFTPClient.isConnected()) {
            try {
                mFTPClient.disconnect();
            } catch (Exception e) {
            }
        }
    }

    public String Ffinding1(String hname, String uname, String pword, String dir_path) {
        String name = "NOFILE";
        boolean state = ftpConnect(hname, uname, pword, 21);
        System.out.println("BOOLEAN ==="+state);
        if (state) {
            try {
                mFTPClient.setDataTimeout(45000);
                mFTPClient.setSoTimeout(45000);
                System.out.println("PATH>>>>>"+dir_path);
                FTPFile[] ftpFiles = mFTPClient.listFiles(dir_path);
                System.out.println("NAME>>>>oooooooooooooo"+ftpFiles.length);

                for (int i=0;i<ftpFiles.length;i++)
                {
                    System.out.println("NAME>>>>"+ftpFiles[i].getName());

                    if (ftpFiles[i].isFile()) {
                        if(ftpFiles[i].getName().contains("Password")){
                            name = ftpFiles[i].getName();
                            System.out.println("NAME>>>>"+name);
                            return name;

                        }


                    }
                }
            } catch(Exception e){
                System.out.println("TEJ EXCEPTIO");
                return "EXCEPTION";
            }
        }
        return name;
    }


}