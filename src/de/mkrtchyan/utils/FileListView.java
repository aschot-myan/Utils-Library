//package de.mkrtchyan.utils;
//
///*
// * Copyright (c) 2013 Ashot Mkrtchyan
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in
// * all copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// */
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.ListView;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Collections;
//
//interface onFileListViewClickListener {
//    void onFileClick (File selectedFile);
//    void onFolderClick(File selectedFolder);
//}
//
//public class FileListView extends ListView implements onFileListViewClickListener {
//
//    private FileListView thisFileList = this;
//    private final File StartFolder;
//    private File currentPath;
//    private boolean showHiddenFiles = false;
////    private File selectedFile;
//    private ArrayList<File> FileList = new ArrayList<File>();
//    private final Context mContext;
//    private String EXT = "";
//    private boolean warnAtChoose = true;
//    private boolean BrowseUpEnabled = false;
//    private onFileListViewClickListener listener;
//
//
//    public FileListView(Context mContext, File StartFolder) {
//        super(mContext);
//        this.StartFolder = StartFolder;
//        this.mContext = mContext;
//        this.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
//                                    long arg3) {
//                if (FileList.get(arg2).isDirectory()) {
//                    onFolderClick(FileList.get(arg2));
//                } else {
//                    fileSelected(FileList.get(arg2));
//                }
//            }
//        });
//    }
//
//    public void reload() {
//        FileList.clear();
//
//        if (!currentPath.equals(StartFolder) || BrowseUpEnabled) {
//            FileList.add(currentPath.getParentFile());
//        }
//        try {
//            for (File i : currentPath.listFiles()) {
//                if (showHiddenFiles || !i.getName().startsWith("."))
//                    if (!EXT.equals("") ) {
//                        if (i.getName().endsWith(EXT) || i.isDirectory()) {
//                            FileList.add(i);
//                        }
//                    } else {
//                        FileList.add(i);
//                    }
//            }
//            Collections.sort(FileList);
//            String[] tmp = new String[FileList.toArray(new File[FileList.size()]).length];
//            for (int i = 0 ; i < tmp.length ; i++) {
//
//                if (i == 0 && BrowseUpEnabled || i == 0 && currentPath != StartFolder) {
//                    tmp[0] = "/..";
//                } else {
//                    if (FileList.get(i).isDirectory()) {
//                        tmp[i] = FileList.get(i).getName() + "/";
//                    } else {
//                        tmp[i] = FileList.get(i).getName();
//                    }
//                }
//            }
//            this.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, tmp));
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void fileSelected(final File file) {
//        if (warnAtChoose) {
//            final AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(mContext);
//            mAlertDialog
//                    .setTitle(R.string.warning)
//                    .setMessage(String.format(mContext.getString(R.string.choose_message), file.getName()))
//                    .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            onFileClick(file);
//                            dialog.dismiss();
//                        }
//                    })
//                    .setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                        }
//                    })
//                    .show();
//        } else {
//            onFileClick(file);
//        }
//    }
//
//    public void setEXT(String EXT) {
//        if (!EXT.startsWith(".")) {
//            EXT = "." + EXT;
//        }
//        if (!this.EXT.equals(EXT)) {
//            this.EXT = EXT;
//        }
//        reload();
//    }
//
//    public void setWarnAtChoose(boolean warnAtChoose) {
//        this.warnAtChoose = warnAtChoose;
//    }
//    public void setBrowseUpEnabled(boolean BrowseUpEnabled) {
//        this.BrowseUpEnabled = BrowseUpEnabled;
//    }
//    public void showHiddenFiles(boolean showHiddenFiles) {
//        this.showHiddenFiles = showHiddenFiles;
//        reload();
//    }
//
//    @Override
//    public void onFileClick(File selectedFile) {
//
//    }
//
//    @Override
//    public void onFolderClick(File selectedFolder) {
//        currentPath = selectedFolder;
//        reload();
//    }
//}