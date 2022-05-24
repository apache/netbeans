/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.profiler.heapwalk.details.api;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.lib.profiler.results.ExportDataDumper;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.ui.NBSwingWorker;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author Petr Cyhelsky
 */


@NbBundle.Messages({
    "ExportAction_BasicExportActionName=Export to...",
    "ExportAction_BasicExportActionDescr=Export to...",
    "ExportAction_ExportDialogTitle=Select File or Directory",
    "ExportAction_ExportDialogButton=Export",
    "ExportAction_OverwriteFileCaption=Overwrite Existing File",
    "ExportAction_OverwriteFileMsg=<html><b>File {0} already exists.</b><br><br>Do you want to replace it?</html>",
    "ExportAction_ExportDialogCSVFilter=CSV File (*.csv)",
    "ExportAction_ExportDialogTXTFilter=Text File (*.txt)",
    "ExportAction_ExportDialogBINFilter=Binary File (*.bin)",
    "ExportAction_ExportingViewMsg=Exporting...",
    "ExportAction_NoViewMsg=No view to export.",
    "ExportAction_OomeExportingMsg=<html><b>Not enough memory to save the file.</b><br><br>To avoid this error increase the -Xmx<br>value in the etc/netbeans.conf file in NetBeans IDE installation.</html>",
    "ExportAction_IOException_Exporting_Msg=<html>IOException occurred during export, see IDE log for details</html>",
    "ExportAction_CannotWriteFileMsg=Failed to export File. Reason: {0}."})
public final class ExportAction extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger(ExportAction.class.getName());

//~ Inner Interfaces ---------------------------------------------------------------------------------------------------------

    public static interface ExportProvider {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void exportData(int exportedFileType, ExportDataDumper eDD);

        public String getViewName();

        public boolean isExportable();

        public boolean hasRawData();
        
        public boolean hasBinaryData();

        public boolean hasText();

    }

    //~ Inner Classes ------------------------------------------------------------------------------------------------------------


    private static class SelectedFile {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        File folder;
        String fileExt;
        String fileName;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        SelectedFile(File folder, String fileName, String fileExt) {
            this.folder = folder;
            this.fileName = fileName;
            this.fileExt = fileExt;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        File getSelectedFile() {
            String folderPath=folder.getAbsolutePath();
            if (folderPath.endsWith(File.separator)) {
                folderPath=folderPath.substring(0, folderPath.length()-1);
            }
            return new File(folderPath + File.separator + fileName+ "." + fileExt);
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final Icon ICON = Icons.getIcon(GeneralIcons.EXPORT);
    private static final String FILE_EXTENSION_CSV = "csv"; // NOI18N
    private static final String FILE_EXTENSION_TXT = "txt"; // NOI18N
    private static final String FILE_EXTENSION_BIN = "bin"; // NOI18N
    public static final int MODE_CSV = 1;
    public static final int MODE_TXT = 2;
    public static final int MODE_BIN = 3;
    private static File exportDir;


    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private JFileChooser fileChooser;
    private final ExportProvider exportProvider;
    private int exportedFileType;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ExportAction(ExportProvider exportProvider) {
        putValue(Action.NAME, Bundle.ExportAction_BasicExportActionName());
        putValue(Action.SHORT_DESCRIPTION, Bundle.ExportAction_BasicExportActionDescr());
        putValue(Action.SMALL_ICON, ICON);
        putValue("iconBase", Icons.getResource(GeneralIcons.EXPORT)); // NOI18N
        this.exportProvider = exportProvider;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    private void setFilters() {
        fileChooser.removeChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
        if (exportProvider.hasRawData()) {
            fileChooser.addChoosableFileFilter(new FileFilterImpl(FILE_EXTENSION_CSV));
        }
        if (exportProvider.hasText()) {
            fileChooser.addChoosableFileFilter(new FileFilterImpl(FILE_EXTENSION_TXT));
        }
        if (exportProvider.hasBinaryData()) {
            fileChooser.addChoosableFileFilter(new FileFilterImpl(FILE_EXTENSION_BIN));
        }
    }


    private JFileChooser getFileChooser() {
        if (fileChooser == null) {
            // File chooser
            fileChooser = new JFileChooser();
            fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setDialogTitle(Bundle.ExportAction_ExportDialogTitle());
            fileChooser.setApproveButtonText(Bundle.ExportAction_ExportDialogButton());
        }
        fileChooser.resetChoosableFileFilters();
        setFilters();
        return fileChooser;
    }

    private boolean checkFileExists(File target) {
        if (target.exists()) {
            if (!ProfilerDialogs.displayConfirmation(
                    Bundle.ExportAction_OverwriteFileMsg(target.getName()),
                    Bundle.ExportAction_OverwriteFileCaption())) {  // choose whether to overwrite
                  return false; // user chose not to overwrite
              }
          }
          return true;
      }

    private SelectedFile selectExportTargetFile(final ExportProvider exportProvider) {
        File targetDir;
        String targetName;
        String defaultName = exportProvider.getViewName();

        // 1. let the user choose file or directory
        final JFileChooser chooser = getFileChooser();
        if (exportDir != null) {
            chooser.setCurrentDirectory(exportDir);
        }
        int result = chooser.showSaveDialog(WindowManager.getDefault().getRegistry().getActivated());
        if (result != JFileChooser.APPROVE_OPTION) {
            return null; // cancelled by the user
        }

        // 2. process both cases and extract file name and extension to use and set exported file type
        File file = chooser.getSelectedFile();
        String targetExt = null;
        FileFilter selectedFileFilter = chooser.getFileFilter();
        if (selectedFileFilter==null  // workaround for #227659
                ||  selectedFileFilter.getDescription().equals(Bundle.ExportAction_ExportDialogCSVFilter())) {
            targetExt=FILE_EXTENSION_CSV;
            exportedFileType=MODE_CSV;
        } else if (selectedFileFilter.getDescription().equals(Bundle.ExportAction_ExportDialogTXTFilter())) {
            targetExt=FILE_EXTENSION_TXT;
            exportedFileType=MODE_TXT;
        } else if (selectedFileFilter.getDescription().equals(Bundle.ExportAction_ExportDialogBINFilter())) {
            targetExt=FILE_EXTENSION_BIN;
            exportedFileType=MODE_BIN;
        }

        if (file.isDirectory()) { // save to selected directory under default name
            exportDir = file;
            targetDir = file;
            targetName = defaultName;
        } else { // save to selected file
            targetDir = fileChooser.getCurrentDirectory();
            String fName = file.getName();

            // divide the file name into name and extension
            if (fName.endsWith("."+targetExt)) {  // NOI18N
                int idx = fName.lastIndexOf('.'); // NOI18N
                targetName = fName.substring(0, idx);
            } else {            // no extension
                targetName=fName;
            }
        }

        // 3. set type of exported file and return a newly created FileObject

        return new ExportAction.SelectedFile(targetDir, targetName, targetExt);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (!exportProvider.hasRawData() && !exportProvider.hasText()) { // nothing to export
            ProfilerDialogs.displayError(Bundle.ExportAction_NoViewMsg());
            return;
        }

        SelectedFile saveFile = selectExportTargetFile(exportProvider);

        if (saveFile == null) return; // cancelled

        final File file = saveFile.getSelectedFile();
        if (!checkFileExists(file)) return; // user doesn't want to overwrite existing file or it can't be overwritten

        new NBSwingWorker(true) {
            private final ProgressHandle ph = ProgressHandle.createHandle(Bundle.ExportAction_ExportingViewMsg());
            @Override
            protected void doInBackground() {
                ph.setInitialDelay(500);
                ph.start();

                try {
                    FileOutputStream fo;
                    fo = new FileOutputStream(file);
                    ExportDataDumper eDD = new ExportDataDumper(fo);
                    exportProvider.exportData(exportedFileType, eDD);
                    if (eDD.getCaughtException()!=null) {
                        ProfilerDialogs.displayError(eDD.getNumExceptions()+Bundle.ExportAction_IOException_Exporting_Msg());
                    }
                } catch (OutOfMemoryError e) {
                    ProfilerDialogs.displayError(Bundle.ExportAction_OomeExportingMsg()+e.getMessage());
                } catch (IOException e1) {
                    ProfilerDialogs.displayError(Bundle.ExportAction_CannotWriteFileMsg(e1.getLocalizedMessage()));
                    LOGGER.log(Level.WARNING, e1.toString());
                }
            }

            @Override
            protected void done() {
                ph.finish();
            }
        }.execute();
    }

    private static class FileFilterImpl extends FileFilter {
        
        private final String extension;

        public FileFilterImpl(String ext) {
            extension = ext;
        }

        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase(Locale.US).endsWith("."+extension); //NOI18N
        }

        @Override
        public String getDescription() {
            if (FILE_EXTENSION_CSV.equals(extension)) {
                return Bundle.ExportAction_ExportDialogCSVFilter();
            } else if (FILE_EXTENSION_TXT.equals(extension)) {
                return Bundle.ExportAction_ExportDialogTXTFilter();
            } else if (FILE_EXTENSION_BIN.equals(extension)) {
                return Bundle.ExportAction_ExportDialogBINFilter();
            } else {
                return null;
            }
            
        }
    }
}

