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
package org.netbeans.modules.cnd.callgraph.support;

import java.awt.Dialog;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.visual.widget.Scene;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 *
 */
public class ExportAction extends AbstractAction  implements Presenter.Popup {

    private static final String EXTENSION = "png"; // NOI18N
    private final Scene scene;
    private final JComponent parent;
    private final JMenuItem menuItem;
    
    public ExportAction(Scene scene, JComponent parent) {
        this.scene = scene;
        this.parent = parent;
        putValue(Action.NAME, getString("Export")); // NOI18N
        menuItem = new JMenuItem(this); 
        Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));

    }

    @Override
    public JMenuItem getPopupPresenter() {
        return menuItem;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        print();
    }

    private void print() {
        BufferedImage bi = new BufferedImage(scene.getBounds().width, scene.getBounds().height,
                BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics = bi.createGraphics();
        scene.paint(graphics);
        graphics.dispose();

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(getString("ExportGraph")); // NOI18N
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new MyFileFilter());
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith("."+EXTENSION)) { // NOI18N
            file = new File(file.getParentFile(), file.getName() + "."+EXTENSION); // NOI18N
        }
        if (file.exists()) {
            String message = getString("FileExistsMessage"); // NOI18N
            DialogDescriptor descriptor = new DialogDescriptor(
                    MessageFormat.format(message, new Object[]{file.getAbsolutePath()}),
                    getString("FileExists"), true, DialogDescriptor.YES_NO_OPTION, DialogDescriptor.NO_OPTION, null); // NOI18N
            Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            
            try {
                dialog.setVisible(true);
            } catch (Throwable th) {
                if (!(th.getCause() instanceof InterruptedException)) {
                    throw new RuntimeException(th);
                }
                descriptor.setValue(DialogDescriptor.CLOSED_OPTION);
            } finally {
                dialog.dispose();
            }
            
            if (descriptor.getValue() != DialogDescriptor.YES_OPTION) {
                return;
            }
        }

        try {
            ImageIO.write(bi, EXTENSION, file);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private String getString(String key) {
        return NbBundle.getMessage(getClass(), key);
    }

    private static class MyFileFilter extends FileFilter {
        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            return file.getName().toLowerCase().endsWith("."+EXTENSION); // NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(ExportAction.class, "PNG"); // NOI18N
        }
    }

}
