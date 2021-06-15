package com.visiontek.Mantra.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.FileOutputStream;

public class MyFTPClientFunctions {

    public int Toknow, working;
    public FileOutputStream desFileStream;
    public String fdate, name;
    public String network;
    public FTPClient mFTPClient = new FTPClient();

    public boolean ftpConnect(String host, String username, String password, int port) {
        boolean status=false;
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

    public boolean Ffinding(String hname, String uname, String pword, String dir_path, String file) {
        boolean find = false;
        boolean state = ftpConnect(hname, uname, pword, 21);
        if (state) {
            try {
                mFTPClient.setDataTimeout(45000);
                mFTPClient.setSoTimeout(45000);
                FTPFile[] ftpFiles = mFTPClient.listFiles(dir_path);
                for (FTPFile ftpFile : ftpFiles) {
                    String name = ftpFile.getName();
                    if (file.equals(name)) {
                        /*fsize = ftpFile.getSize();
                        fdate = String.valueOf(ftpFile);*/
                        return true;
                    }
                }
            } catch (Exception e) {
                return find;
            }
        }
        return find;
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
                System.out.println("Error occurred while disconnecting from ftp server." + Toknow);
            }
        }
    }


}
