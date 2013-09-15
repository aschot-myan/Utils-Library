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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;

public class FileChooser extends Dialog {

    private static File Start;
    private static File currentPath;
    public boolean use = false;
    public File selectedFile;
    final private TextView tvPath;
    final private ListView lvFiles;
    private Context mContext;
    private String[] files;
    private int select;
    private Runnable runAtChoose;
    private Notifyer mNotifyer;
    private String EXT;

    public FileChooser(final Context mContext, String StartPath, String EXT, Runnable runAtChoose) {
        super(mContext);

        mNotifyer = new Notifyer(mContext);
        Start = new File(StartPath);
        currentPath = Start;
        this.mContext = mContext;
        this.EXT = EXT;
        this.runAtChoose = runAtChoose;
        setContentView(R.layout.dialog_file_chooser);
        setTitle(R.string.file_chooser);

        tvPath = (TextView) findViewById(R.id.tvPath);
        lvFiles = (ListView) findViewById(R.id.lvFiles);

        lvFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                select = arg2;
                selectedFile = new File(currentPath, files[select]);
                if (selectedFile.isDirectory()) {
                    if (selectedFile.list().length > 0) {
                        currentPath = selectedFile;
                        reload();
                    } else {
                        mNotifyer.showToast(currentPath.getAbsolutePath() + " is empty!");
                        currentPath = Start;
                        reload();
                    }
                } else {
                    fileSelected();
                }
            }
        });
    }

    private void reload() {
        try {
            tvPath.setText(currentPath.getAbsolutePath());
            files = currentPath.list();
            if (files.length <= 0) {
                mNotifyer.showToast(currentPath.getAbsolutePath() + " is empty!");
            }
            lvFiles.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, files));
        } catch (NullPointerException e) {
            mNotifyer.showExceptionToast(e);
            this.dismiss();
        }
    }

    private void fileSelected() {

        if (selectedFile.getName().endsWith(EXT)) {
            AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(mContext);
            mAlertDialog
                    .setTitle(R.string.warning)
                    .setMessage(String.format(mContext.getString(R.string.choose_message), selectedFile.getName()))
                    .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            use = true;
                            runAtChoose.run();
                            use = false;
                            dismiss();
                        }
                    })
                    .setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            use = false;
                            currentPath = Start;
                            reload();
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            use = false;
                        }
                    })
                    .show();
        } else if (!EXT.equals("")) {
                selectedFile = null;
                mNotifyer.createDialog(R.string.warning, String.format(mContext.getString(R.string.wrong_format), EXT), true).show();
        }
    }

    public void show() {
        super.show();
        reload();
    }
}