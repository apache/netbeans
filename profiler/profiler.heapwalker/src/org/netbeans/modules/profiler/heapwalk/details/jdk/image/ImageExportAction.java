/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.profiler.heapwalk.details.jdk.image;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Action used to export images.
 *
 * @author Jan Taus
 */
@NbBundle.Messages({
    "ImageExportAction_Title=Export Image", // NOI18N
    "ImageExportAction_Ok=Export", // NOI18N
    "ImageExportAction_PNG=Portable Network Graphics (*.png)", // NOI18N
    "ImageExportAction_OverwriteFileCaption=Overwrite Existing File", // NOI18N
    "ImageExportAction_OverwriteFileMsg=<html><b>File {0} already exists.</b><br><br>Do you want to replace it?</html>", // NOI18N
    "ImageExportAction_WrongFormat=File type {0} is not supported.", // NOI18N
    "ImageExportAction_InvalidLoc=Invalid location for file.", // NOI18N
    "ImageExportAction_Failed=<html>Failed to export File.<br>Reason: {0}</html>" // NOI18N
})
class ImageExportAction extends AbstractAction {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final String DEFAULT_EXPORT_TYPE = "png"; //NOI18N
    private static File exportDir;
    //~ Instance fields ----------------------------------------------------------------------------------------------------------
    private JFileChooser fileChooser;
    private final Image image;

    //~ Constructors -------------------------------------------------------------------------------------------------------------
    ImageExportAction(Image image) {
        super();
        putValue(Action.NAME, Bundle.ImageDetailProvider_Action_Export());
        putValue(Action.SMALL_ICON, Icons.getIcon(GeneralIcons.EXPORT));
        putValue(Action.SHORT_DESCRIPTION, Bundle.ImageDetailProvider_Action_Export());
        putValue("iconBase", Icons.getResource(GeneralIcons.EXPORT)); // NOI18N
        this.image = image;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    @Override
    public void actionPerformed(ActionEvent e) {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
            fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setDialogTitle(Bundle.ImageExportAction_Title());
            fileChooser.setApproveButtonText(Bundle.ImageExportAction_Ok());
            fileChooser.addChoosableFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith("." + DEFAULT_EXPORT_TYPE);
                }

                @Override
                public String getDescription() {
                    return Bundle.ImageExportAction_PNG();
                }
            });
        }
        if (exportDir != null) {
            fileChooser.setCurrentDirectory(exportDir);
        }
        int result = fileChooser.showSaveDialog(WindowManager.getDefault().getRegistry().getActivated());
        if (result == JFileChooser.APPROVE_OPTION) {
            exportDir = fileChooser.getCurrentDirectory();
            File target = fileChooser.getSelectedFile();
            if (target.exists()) {
                if (!ProfilerDialogs.displayConfirmation(
                        Bundle.ImageExportAction_OverwriteFileMsg(target.getName()),
                        Bundle.ImageExportAction_OverwriteFileCaption())) {
                    return;
                }
            }

            try {
                writeImage(fileChooser.getSelectedFile());
            } catch (IOException ex) {
                ProfilerDialogs.displayError(Bundle.ImageExportAction_Failed(ex.getMessage()));
            }
        }
    }

    private void writeImage(File file) throws IOException {
        String type;
        int idx = file.getName().lastIndexOf('.');
        if (idx == -1) {
            file = new File(file.getPath() + '.' + DEFAULT_EXPORT_TYPE);
            type = DEFAULT_EXPORT_TYPE;
        } else {
            type = file.getName().substring(idx + 1);
        }
        BufferedImage bi;
        if (image instanceof BufferedImage) {
            bi = (BufferedImage) image;
        } else {
            bi = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            bi.createGraphics().drawImage(image, 0, 0, null);
        }
        FileObject fo = FileUtil.createData(file);
        if (fo == null) {
            throw new IOException(Bundle.ImageExportAction_InvalidLoc());
        }
        OutputStream output = fo.getOutputStream();
        try {
            if (!ImageIO.write(bi, type, output)) {
                throw new IOException(Bundle.ImageExportAction_WrongFormat(type));
            }
        } finally {
            output.close();
        }
    }
}
