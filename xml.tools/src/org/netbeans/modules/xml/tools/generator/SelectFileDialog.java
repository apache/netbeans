/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.xml.tools.generator;

import java.io.IOException;
import java.io.File;
import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.*;

import org.openide.*;
import org.openide.loaders.DataObject;
import org.openide.filesystems.*;
import org.openide.util.*;

import org.netbeans.modules.xml.lib.GuiUtil;

/**
 * Extremely simple dialog with one input line. Invoke using
 * {@link #getFileObject} method.
 */
public final class SelectFileDialog {

    private Util.NameCheck check;

    private Prompt selectDD;
    private FileObject folder;
    private String ext;

    /**
     *
     * @param folder parent folder that will host created file
     * @param name default file name
     * @param ext default file.extension
     */
    public SelectFileDialog (FileObject folder, String name, String ext) {

        this (folder, name, ext, Util.JAVA_CHECK);
    }

    public SelectFileDialog (FileObject folder, String name, String ext, Util.NameCheck check) {
        this.folder = folder;
        this.ext = ext;
        this.check = check;
        this.selectDD = new Prompt (
                NbBundle.getMessage(SelectFileDialog.class, "PROP_fileNameTitle") + " *." + ext, // NOI18N
                NbBundle.getMessage(SelectFileDialog.class, "PROP_fileName"),
                name
        );
    }

    /**
     * Get file object that have user selected
     * @throws IOException if cancelled or invalid data entered
     */
    public FileObject getFileObject () throws IOException {
        FileObject newFO = null;

        while ( newFO == null ) {
            DialogDisplayer.getDefault().notify(selectDD);
            if (selectDD.getValue() != NotifyDescriptor.OK_OPTION) {
                throw new UserCancelException();
            }
            final String newName = selectDD.getInputText();

            newFO = folder.getFileObject (newName, ext);
        
            if ( ( newFO == null ) ||
                 ( newFO.isVirtual() == true ) ) {

                FileSystem fs = folder.getFileSystem();
                final FileObject tempFile = newFO;
                
                fs.runAtomicAction (new FileSystem.AtomicAction () {
                        public void run () throws IOException {

                            if ( ( tempFile != null ) &&
                                 tempFile.isVirtual() ) {
                                tempFile.delete();
                            }

                            try {
                                folder.createData (newName, ext);                                
                            } catch (IOException exc) {
                                NotifyDescriptor desc = new NotifyDescriptor.Message
                                    (NbBundle.getMessage(SelectFileDialog.class, "MSG_cannot_create_data", newName + "." + ext), NotifyDescriptor.WARNING_MESSAGE);
                                DialogDisplayer.getDefault().notify (desc);

                                //if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug (exc);
                            }
                        }
                    });
                
                newFO = folder.getFileObject (newName, ext);

            } else if (newFO != null) {
                DataObject data = DataObject.find(newFO);
                if (data.isModified() || data.isValid() == false) {
                    NotifyDescriptor message = new NotifyDescriptor.Message(NbBundle.getMessage(SelectFileDialog.class, "BK0001"), NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(message);
                    throw new UserCancelException();
                } else if (! GuiUtil.confirmAction (NbBundle.getMessage(SelectFileDialog.class, "PROP_replaceMsg",
                                                                newName, ext ) )) {
                    throw new UserCancelException();
                }
            }
        } // while

        return newFO;
    }


    /** One input line, verified on content change. */
    private class Prompt extends NotifyDescriptor.InputLine implements DocumentListener{

        public Prompt(String title, String label, String text) {
            super(label, title);
            setInputText(text);
            textField.getDocument().addDocumentListener(this);
        }

        public void changedUpdate(DocumentEvent e) {
            verifyInput();
        }

        public void insertUpdate(DocumentEvent e) {
            verifyInput();
        }

        public void removeUpdate(DocumentEvent e) {
            verifyInput();
        }

        private void verifyInput() {
            String typedText = textField.getText();
            // no relative paths allowed #24693
            if ((typedText.indexOf(File.separatorChar) != -1) ||
                // fix for #165043 on Windows: a file path similar "c:wsdl.dtd" is forbidden
                (typedText.indexOf(":") != -1) ||  
                // fix for #165043 on Windows: a file path similar "c:/wsdl.dtd" is forbidden
                (typedText.indexOf("/") != -1) ||
                // fix for #165043 on Windows: a file path similar "c:\wsdl.dtd" is forbidden
                (typedText.indexOf("\\") != -1)) {
                selectDD.setValid(false);
            } else {
                selectDD.setValid(check.checkName(typedText));
            }
        }

    }
    
}
