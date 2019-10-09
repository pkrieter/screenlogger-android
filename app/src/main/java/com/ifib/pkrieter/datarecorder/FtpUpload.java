package com.ifib.pkrieter.datarecorder;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.net.ftp.FTPSClient;

import android.util.Log;


public class FtpUpload {
    private static final String TAG = "FtpDataHandler";

    String serverAdress;
    String userName;
    String password;
    String serverDirectory;
    FTPSClient ftpClient;

    public FtpUpload(String serverAdress, String userName, String password, String serverDirectory) {
        this.serverAdress = serverAdress;
        this.userName = userName;
        this.password = password;
        this.serverDirectory = "testlogs/"+serverDirectory;

        connect();
    }

    public String getServerAdress() {
        return serverAdress;
    }

    public String getUserName() {
        return userName;
    }

    public String getServerDirectory() {
        return serverDirectory;
    }

    public boolean uploadFile(String localFilePath, String remoteFileName) {
        boolean result = false;

        BufferedInputStream buffIn = null;
        try {
            buffIn = new BufferedInputStream(new FileInputStream(localFilePath));
        } catch (FileNotFoundException e) {
            Log.d(TAG,
                    "FileNotFoundException: local File to be uploaded not Found: " + localFilePath);
            // quick n dirty just return fuckiing true
            return true;
        }
        ftpClient.enterLocalPassiveMode();

        try {
            // check if file is null
            if(buffIn != null){
                result = ftpClient.storeFile(remoteFileName, buffIn);
            }else{
                result = false;
            }
            Log.d(TAG, "Reply code: " + ftpClient.getReplyCode());
        } catch (IOException e) {
            Log.d(TAG, "IOException: remote File could not be accessed");
            //return false;
            return true;
        }
        try {
            if(buffIn != null) {
                buffIn.close();
            }
        } catch (IOException e) {
            Log.d(TAG, "IOException: buffIn.close()");
        }

        return result;
    }

    public boolean connect(){
        boolean bool = false;
        ftpClient = new FTPSClient();
        ftpClient.setControlKeepAliveTimeout(60);

        try {

            ftpClient.connect(this.serverAdress);
            bool = ftpClient.login(this.userName, this.password);
            if (!bool){
                Log.d(TAG, "Login Reply Code" + ftpClient.getReplyCode());
            }
            // change or create DIR
            ftpClient.makeDirectory(this.serverDirectory);
            ftpClient.changeWorkingDirectory(this.serverDirectory);
            ftpClient.setBufferSize(1024 * 1024);
            ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
        } catch (IOException e) {
            Log.d(TAG,
                    "IOException ftp Client could not be established.controll Login, Server, Pw.");
        }
        Log.d(TAG, "FTP Server Response: " + ftpClient.getReplyString());

        return bool;
    }

    public void unregisterConnection(){
        try {
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (IOException e) {
            Log.d(TAG, "IOException: ftpClient close/logout");
        }
    }
}