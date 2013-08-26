package de.mkrtchyan.utils;

/*
 * Copyright (c) 2013 Ashot Mkrtchyan
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.Toolbox;
import org.sufficientlysecure.rootcommands.command.SimpleCommand;
import org.sufficientlysecure.rootcommands.util.BrokenBusyboxException;
import org.sufficientlysecure.rootcommands.util.RootAccessDeniedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class Common {

    public void pushFileFromRAW(Context mContext, File outputFile, int RAW) {
        if (!outputFile.exists()) {
            try {
                InputStream is = mContext.getResources().openRawResource(RAW);
                OutputStream os = new FileOutputStream(outputFile);
                byte[] data = new byte[is.available()];
                is.read(data);
                os.write(data);
                is.close();
                os.close();
            } catch (IOException e) {
                new Notifyer(mContext).showExceptionToast(e);
            }
        }
    }

    public boolean suRecognition() {
        try {
            return new Toolbox(Shell.startRootShell()).isRootAccessGiven();
        } catch (Exception e) {
            return false;
        }
    }

    public void checkFolder(File Folder) {
        if (!Folder.exists()
                || !Folder.isDirectory()) {
            Folder.mkdir();
        }
    }

    public void chmod(File file, String mod) {

        try {
            new Toolbox(Shell.startRootShell()).setFilePermissions(file.getAbsolutePath(), mod);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void unzip(File ZipFile, File OutputFolder) throws IOException {

        FileInputStream fin = new FileInputStream(ZipFile);
        ZipInputStream zin = new ZipInputStream(fin);
        ZipEntry ze;

        while ((ze = zin.getNextEntry()) != null) {

            if (ze.isDirectory()) {
                checkFolder(new File(OutputFolder, ze.getName()));
            } else {
                File file = new File(OutputFolder.getAbsolutePath(), ze.getName());
                FileOutputStream fout = new FileOutputStream(file);
                for (int c = zin.read(); c != -1; c = zin.read()) {
                    fout.write(c);
                }
                zin.closeEntry();
                fout.close();
            }
        }
        zin.close();
    }

    public void deleteFolder(File Folder, boolean AndFolder) {
        if (Folder.exists()
                && Folder.isDirectory()) {
            File[] files = Folder.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteFolder(files[i], AndFolder);
                } else {
                    files[i].delete();
                }
            }
            if (AndFolder)
                Folder.delete();
        }
    }

    public void mountDir(File Dir, String mode) throws RootAccessDeniedException {
        try {
            new Toolbox(Shell.startRootShell()).remount(Dir.getAbsolutePath(), mode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void move(File Source, File Destination, boolean Mount) throws RootAccessDeniedException {
        if (Mount)
            mountDir(Destination, "RW");
        File[] files = Source.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()
                    && files[i].exists()) {
                if (Mount)
                    mountDir(new File(Destination.getAbsolutePath(), files[i].getName()), "RW");
            }
        }
        executeSuShell("busybox mv -f " + Source.getAbsolutePath() + " " + Destination.getAbsolutePath());
        if (Mount)
            mountDir(Destination, "RO");
    }

    public String executeShell(String Command) {

        try {
            SimpleCommand command = new SimpleCommand(Command);
            Shell.startShell().add(command).waitForFinish();
            String output = command.getOutput();
            return output;
        } catch (BrokenBusyboxException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String executeShell(Context mContext, String Command) {
        try {
            SimpleCommand command = new SimpleCommand(Command);
            Shell.startShell().add(command).waitForFinish();
            String output = command.getOutput();
            if (getBooleanPerf(mContext, "mkrtchyan_utils_common", "log-commands")) {

                String CommandLog = "\nCommand:\n" + Command + "\n\nOutput:\n" + output;

                FileOutputStream fo = mContext.openFileOutput("su-logs.log", Context.MODE_APPEND);
                fo.write(CommandLog.getBytes());
            }
            return output;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        return "";
    }

    public String executeSuShell(String Command) throws RootAccessDeniedException {
        try {
            String TAG = "executeSuShell";
            SimpleCommand command = new SimpleCommand(Command);
            Shell.startRootShell().add(command).waitForFinish();
            String output = command.getOutput();
            Log.i(TAG, Command);
            return output;
        } catch (BrokenBusyboxException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public String executeSuShell(Context mContext, String Command) throws RootAccessDeniedException {

        try {
            SimpleCommand command = new SimpleCommand(Command);
            Shell.startRootShell().add(command).waitForFinish();
            String output = command.getOutput();
            if (getBooleanPerf(mContext, "mkrtchyan_utils_common", "log-commands")) {

                String CommandLog = "\nCommand:\n\nsu -c " + Command + "\n\nOutput:\n" + output;
                File log = new File(mContext.getFilesDir(), "command-logs.log");
                if (!log.exists())
                    log.createNewFile();
                FileOutputStream fo = mContext.openFileOutput(log.getName(), Context.MODE_APPEND);
                fo.write(CommandLog.getBytes());
            }
            return output;
        } catch (BrokenBusyboxException ex) {
            ex.printStackTrace();
        } catch (TimeoutException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public boolean getBooleanPerf(Context mContext, String PrefName, String key) {
        return mContext.getSharedPreferences(PrefName, Context.MODE_PRIVATE).getBoolean(key, false);
    }

    public void setBooleanPerf(Context mContext, String PrefName, String key, Boolean value) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(PrefName, Context.MODE_PRIVATE).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public String getStringPerf(Context mContext, String PrefName, String key) {
        return mContext.getSharedPreferences(PrefName, Context.MODE_PRIVATE).getString(key, "");
    }

    public void setStringPerf(Context mContext, String PrefName, String key, String value) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(PrefName, Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.commit();
    }
}
