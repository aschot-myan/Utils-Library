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

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.rootcommands.Shell;
import org.rootcommands.Toolbox;
import org.rootcommands.command.SimpleCommand;
import org.rootcommands.util.BrokenBusyboxException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.TimeoutException;


public class Common {

    public static final String TAG = "Common";
	public static final String Logs = "commands.log";
	public static final String PREF_NAME = "de_mkrtchyan_utils_common";
	public static final String PREF_LOG = "log_commands";

    public void pushFileFromRAW(Context mContext, File outputFile, int RAW) throws IOException {
        if (!outputFile.exists()) {
            InputStream is = mContext.getResources().openRawResource(RAW);
            OutputStream os = new FileOutputStream(outputFile);
            byte[] data = new byte[is.available()];
            is.read(data);
            os.write(data);
            is.close();
            os.close();
        }
    }

    public boolean suRecognition() {
        try {
            return new Toolbox(Shell.startRootShell()).isRootAccessGiven();
        } catch (Exception e) {
	        e.printStackTrace();
        }
	    return false;
    }

    public void checkFolder(File Folder) {
        if (!Folder.exists()
                || !Folder.isDirectory()) {
            Folder.mkdir();
        }
    }

    public boolean chmod(File file, String mod) {

        try {
            new Toolbox(Shell.startRootShell()).setFilePermissions(file.getAbsolutePath(), mod);
            return true;
        } catch (NullPointerException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        } catch (TimeoutException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public void deleteFolder(File Folder, boolean AndFolder) throws IOException{
        if (Folder.exists()
                && Folder.isDirectory()) {
            File[] files = Folder.listFiles();
            for (File i :files) {
                if (i.isDirectory()) {
                    deleteFolder(i, AndFolder);
                } else {
                    i.delete();
                }
            }
            if (AndFolder)
                Folder.delete();
        } else {
            throw new IOException(Folder.getName() + "not exists!");
        }
    }

    public void mountDir(File Dir, String mode) throws Exception {
        try {
            new Toolbox(Shell.startRootShell()).remount(Dir.getAbsolutePath(), mode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void move(File Source, File Destination, boolean Mount) throws Exception {
        if (Mount)
            mountDir(Destination, "RW");
        File[] files = Source.listFiles();
        for (File i : files) {
            if (i.isDirectory()
                    && i.exists()) {
                if (Mount)
                    mountDir(new File(Destination.getAbsolutePath(), i.getName()), "RW");
            }
        }
        executeSuShell("busybox mv -f " + Source.getAbsolutePath() + " " + Destination.getAbsolutePath());
        if (Mount)
            mountDir(Destination, "RO");
    }

    public String executeShell(String Command) throws Exception {

        try {
            SimpleCommand command = new SimpleCommand(Command);
            Shell.startShell().add(command).waitForFinish();
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
	    throw new Exception("Error while executing " + Command);
    }

    public String executeShell(Context mContext, String Command) throws Exception {
        try {
            SimpleCommand command = new SimpleCommand(Command);
            Shell.startShell().add(command).waitForFinish();
            String output = command.getOutput();
	        Log.i(TAG, Command);
            if (getBooleanPerf(mContext, PREF_NAME, PREF_LOG)) {

                String CommandLog = "\nCommand:\n" + Command + "\n\nOutput:\n" + output;

                FileOutputStream fo = mContext.openFileOutput(Logs, Context.MODE_APPEND);
                fo.write(CommandLog.getBytes());
            }
            return output;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
	    throw new Exception("Error while executing " + Command);
    }

    public String executeSuShell(String Command) throws Exception {
        try {
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
	    throw new Exception("Error while executing " + Command);
    }

    public String executeSuShell(Context mContext, String Command) throws Exception {

        try {
            SimpleCommand command = new SimpleCommand(Command);
            Shell.startRootShell().add(command).waitForFinish();
            String output = command.getOutput();
	        Log.i(TAG, Command);
            if (getBooleanPerf(mContext, PREF_NAME, PREF_LOG)) {
                String CommandLog = "\nCommand:\n\nsu -c " + Command + "\n\nOutput:\n" + output;
                File log = new File(mContext.getFilesDir(), Logs);
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
	    throw new Exception("Error while executing " + Command);
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

	public Integer getIntegerPerf(Context mContext, String PrefName, String key) {
		return mContext.getSharedPreferences(PrefName, Context.MODE_PRIVATE).getInt(key, 0);
	}

	public void setIntegerPerf(Context mContext, String PrefName, String key, int value) {
		SharedPreferences.Editor editor = mContext.getSharedPreferences(PrefName, Context.MODE_PRIVATE).edit();
		editor.putInt(key, value);
		editor.commit();
	}

	public void showLogs(final Context mContext) {
		final Notifyer mNotifyer = new Notifyer(mContext);
		final Dialog LogDialog = mNotifyer.createDialog(R.string.logs_title, R.layout.dialog_command_logs, false, true);
		final TextView tvLog = (TextView) LogDialog.findViewById(R.id.tvSuLogs);
		final Button bClearLog = (Button) LogDialog.findViewById(R.id.bClearLog);
		bClearLog.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				new File(mContext.getFilesDir(), Common.Logs).delete();
				tvLog.setText("");
			}
		});
		String sLog = "";

		try {
			String line;
			BufferedReader br = new BufferedReader(new InputStreamReader(mContext.openFileInput(Common.Logs)));
			while ((line = br.readLine()) != null) {
				sLog = sLog + line + "\n";
			}
			br.close();
			tvLog.setText(sLog);
		} catch (FileNotFoundException e) {
			LogDialog.dismiss();
		} catch (IOException e) {
			LogDialog.dismiss();
		}

		LogDialog.show();
	}
}
