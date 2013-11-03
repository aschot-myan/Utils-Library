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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Downloader extends AsyncTask<Void, Integer, Boolean> {

    private static final String TAG = "Downloader";

    private Context mContext;
    private ProgressDialog downloadDialog;
    private boolean first_start = true;
    private Runnable AfterDownload;
    private String URL;
    private String FileName;
    private File outputFile;
    private boolean checkFile = false;
	private boolean overrideFile = false;
    private boolean hide = false;
	private boolean retry = false;
    private boolean cancelable = true;
    private File ChecksumFile = null;
    private Downloader thisDownloader = this;
    private boolean askBeforeDownload = false;

    private IOException ioException;
    private MalformedURLException urlException;

    public Downloader(Context mContext, String URL, String FileName, File outputFile) {
        this.mContext = mContext;
        this.URL = URL;
        this.FileName = FileName;
        this.outputFile = outputFile;
    }

	public Downloader(Context mContext, String URL, String FileName, File outputFile, Runnable AfterDownload) {
		this.mContext = mContext;
		this.URL = URL;
		this.FileName = FileName;
		this.outputFile = outputFile;
		this.AfterDownload = AfterDownload;
	}

	public Downloader(Context mContext, String URL, File outputFile) {
		this.mContext = mContext;
		this.URL = URL;
		this.FileName = outputFile.getName();
		this.outputFile = outputFile;
	}

	public Downloader(Context mContext, String URL, File outputFile, Runnable AfterDownload) {
		this.mContext = mContext;
		this.URL = URL;
		this.FileName = outputFile.getName();
		this.outputFile = outputFile;
		this.AfterDownload = AfterDownload;
	}

    public void ask() {
        final Downloader thisDownloader = this;
        if (askBeforeDownload) {
            new Notifyer(mContext).createAlertDialog(R.string.info, String.format(mContext.getString(R.string.download_now), outputFile.getName()), new Runnable() {
                @Override
                public void run() {
                    thisDownloader.execute();
                }
            }).show();
        } else {
            thisDownloader.execute();
        }
    }

    protected void onPreExecute() {
        if (overrideFile)
            outputFile.delete();
        if (!hide) {
            if (!URL.endsWith("/"))
                URL = URL + "/";
            if (!URL.startsWith("http://")
                    && !URL.startsWith("https://"))
                URL = "http://" + URL;
            downloadDialog = new ProgressDialog(mContext);
            downloadDialog.setTitle(mContext.getResources().getString(R.string.connecting));
            downloadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            downloadDialog.setCancelable(false);
            downloadDialog.setMessage(URL);
            if (cancelable)
                downloadDialog.setButton(DialogInterface.BUTTON_NEGATIVE, mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        thisDownloader.cancel(false);
                        if (outputFile.exists())
                            outputFile.delete();
                    }
                });
            downloadDialog.show();
        }
    }

    protected Boolean doInBackground(Void... params) {

        if (!outputFile.exists() || overrideFile) {
            ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null
                    && networkInfo.isConnected()) {
                try {

                    Log.i(TAG, "Connecting to " + URL);
                    URLConnection connection = new URL(URL + FileName).openConnection();

                    connection.setDoOutput(true);
                    connection.connect();

                    FileOutputStream fileOutput = new FileOutputStream(outputFile);

                    InputStream inputStream = connection.getInputStream();

                    byte[] buffer = new byte[1024];
                    int fullLength = connection.getContentLength();

                    int bufferLength;
                    int downloaded = 0;

                    Log.i(TAG, "Downloading " + outputFile.getName());

                    while ((bufferLength = inputStream.read(buffer)) > 0) {
                        fileOutput.write(buffer, 0, bufferLength);
                        downloaded += bufferLength;
                        if (!hide)
                            publishProgress(downloaded, fullLength);
                    }

                    fileOutput.close();

                    Log.i(TAG, "Download finished!");

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    urlException = e;
                    Log.i(TAG, e.getMessage());
                    return false;
                } catch (IOException e) {
                    e.printStackTrace();
                    ioException = e;
                    Log.i(TAG, e.getMessage());
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onProgressUpdate(final Integer... progress) {
        super.onProgressUpdate(progress);
        if (first_start) {
            downloadDialog.dismiss();
            downloadDialog = new ProgressDialog(mContext);
            downloadDialog.setTitle(R.string.Downloading);
            downloadDialog.setMessage(FileName);
            downloadDialog.setCancelable(false);
            if (cancelable)
                downloadDialog.setButton(DialogInterface.BUTTON_NEGATIVE, mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        thisDownloader.cancel(false);
                        if (outputFile.exists())
                            outputFile.delete();
                    }
                });
            if (progress[1] >= 0) {
                downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            } else {
                downloadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            }
            downloadDialog.show();
            downloadDialog.setMax(progress[1]);
            downloadDialog.setCancelable(false);
            first_start = false;
        }
        downloadDialog.setProgress(progress[0]);

    }

    protected void onPostExecute(Boolean success) {
        if (!hide)
            if (downloadDialog.isShowing())
                downloadDialog.dismiss();
        if (success) {
            if (checkFile) {
                if (isDowloadCorrupt()) {
                    outputFile.delete();
                    loop();
                }
            }
            if (AfterDownload != null)
                AfterDownload.run();
        } else {
	        outputFile.delete();
            if (!hide) {
                if (ioException != null)
                    Notifyer.showExceptionToast(mContext, TAG, ioException);
                if (urlException != null)
                    Notifyer.showExceptionToast(mContext, TAG, urlException);
            }
	        if (retry) {
		        loop();
	        }
        }

    }

    public boolean isDowloadCorrupt() {
        try {
            return !SHA1.verifyChecksum(outputFile, ChecksumFile);
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        } catch (SHA1.SHA1SumNotFound sha1SumNotFound) {
            Log.d(TAG, sha1SumNotFound.getMessage());
            sha1SumNotFound.printStackTrace();
        }
        return true;
    }

    private void loop() {
        final Downloader loop_downloader = thisDownloader;
        if (!hide) {
            final AlertDialog.Builder tryAgain = new AlertDialog.Builder(mContext);
            tryAgain
                    .setMessage(String.format(mContext.getString(R.string.failed_download), outputFile.getName()))
                    .setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            loop_downloader.execute();
                        }
                    })
                    .setNegativeButton(R.string.later, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    })
                    .setTitle(R.string.warning)
                    .show();
        } else {
            loop_downloader.execute();
        }
    }

    public void setAfterDownload (Runnable AfterDownload) {
        this.AfterDownload = AfterDownload;
    }
    public void setChecksumFile(File ChecksumFile) {
        this.ChecksumFile = ChecksumFile;
        checkFile = ChecksumFile != null;
    }
	public void setOverrideFile(boolean overrideFile) {
		this.overrideFile = overrideFile;
	}
    public void setHidden(boolean hide) {
        this.hide = hide;
    }
	public void setRetry (boolean retry) {
		this.retry = retry;
	}
    public boolean isCancelable() {
        return cancelable;
    }
    public void setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
    }
    public void setAskBeforeDownload(boolean askBeforeDownload) {this.askBeforeDownload = askBeforeDownload;}
}
