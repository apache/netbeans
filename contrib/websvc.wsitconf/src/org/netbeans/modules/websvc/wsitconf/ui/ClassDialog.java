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

package org.netbeans.modules.websvc.wsitconf.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.util.AbstractTask;
import org.netbeans.modules.websvc.wsitconf.util.SourceUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac
 * Displays a Dialog for selecting classes that are in a project.
 */
public class ClassDialog {
    
    private Dialog dialog;
    private SelectClassPanel sPanel;
    private SelectClassDialogDesc dlgDesc;
    
    /**
     * Creates a new instance of ClassDialog
     */
    public ClassDialog(Project project, String extendingClass) {
        sPanel = new SelectClassPanel(project);
        dlgDesc = new SelectClassDialogDesc(sPanel, extendingClass);
        dialog = DialogDisplayer.getDefault().createDialog(dlgDesc);
    }
    
    public void show(){
        dialog.setVisible(true);
    }
    
    public boolean okButtonPressed(){
        return dlgDesc.getValue() == DialogDescriptor.OK_OPTION;
    }
    
    public Set<String> getSelectedClasses(){
        Set<String> selectedClasses = new HashSet<String>();
        Node[] nodes = sPanel.getSelectedNodes();
        for(int i = 0; i < nodes.length; i++){
            String name = getClassNameFromNode(nodes[i]);
            selectedClasses.add(name);
        }
        return selectedClasses;
    }

    private String getClassNameFromNode(Node node) {
        final String[] name = new String[1];

        FileObject classElement = node.getLookup().lookup(FileObject.class);            
        JavaSource js = JavaSource.forFileObject(classElement);
        try {
            js.runUserActionTask(new AbstractTask<CompilationController>() {
                 public void run(CompilationController controller) throws java.io.IOException {
                     controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                     SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                     name[0] = sourceUtils.getTypeElement().getQualifiedName().toString();
                 }
             }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return name[0];
    }
    
    static class SelectClassDialogDesc extends DialogDescriptor{
        String extendingClass;
        final SelectClassPanel sPanel;
        
        private Object[] closingOptionsWithoutOK = {DialogDescriptor.CANCEL_OPTION,
        DialogDescriptor.CLOSED_OPTION};
        private Object[] closingOptionsWithOK = {DialogDescriptor.CANCEL_OPTION,
        DialogDescriptor.CLOSED_OPTION, DialogDescriptor.OK_OPTION};
        
        /**
         * Creates a new instance of SelectClassDialogDesc
         */
        public SelectClassDialogDesc(SelectClassPanel sPanel, String extendingClass) {
            super(sPanel, "Select Class");  //NOI18N
            this.extendingClass = extendingClass;
            this.sPanel = sPanel;
            this.setButtonListener(new AddClassActionListener(sPanel));
        }
        
        class AddClassActionListener implements ActionListener{
            SelectClassPanel sPanel;
            public AddClassActionListener(SelectClassPanel sPanel){
                this.sPanel = sPanel;
            }
            public void actionPerformed(ActionEvent evt){
                if(evt.getSource() == NotifyDescriptor.OK_OPTION){
                    boolean accepted = false;
                    String errMsg = null;

                    Node[] selectedNodes = sPanel.getSelectedNodes();
                    if (selectedNodes.length != 1) {
                        errMsg = NbBundle.getMessage(ClassDialog.class, "TXT_SelectOnlyOne_msg");    //NOI18N
                    } else {
                        Node node = selectedNodes[0];
                        FileObject classElement = node.getLookup().lookup(FileObject.class);
                        if (classElement == null) {
                            errMsg = NbBundle.getMessage(ClassDialog.class, "TXT_NoFileObject_msg");    //NOI18N
                            Logger.global.log(Level.INFO, errMsg + ", " + node);
                        } else {
                            JavaSource js = JavaSource.forFileObject(classElement);
                            if (js == null) {
                                errMsg = NbBundle.getMessage(ClassDialog.class, "TXT_NotJavaClass_msg");    //NOI18N
                            } else {
                                if (!isWantedClass(js)) {
                                    errMsg = NbBundle.getMessage(ClassDialog.class, "TXT_NotWantedClass_msg",   //NOI18N
                                            classElement.getName(), extendingClass);
                                } else {
                                    accepted = true;
                                }
                            }
                        }
                    }
                    
                    if (!accepted) {
                        NotifyDescriptor.Message notifyDescr =
                                new NotifyDescriptor.Message(errMsg,
                                NotifyDescriptor.ERROR_MESSAGE );
                        DialogDisplayer.getDefault().notifyLater(notifyDescr);
                        SelectClassDialogDesc.this.setClosingOptions(closingOptionsWithoutOK);
                    } else {
                        // Everything was fine so allow OK
                        SelectClassDialogDesc.this.setClosingOptions(closingOptionsWithOK);
                    }
                }
            }
        }

        private boolean isWantedClass(final JavaSource js) {
            if (extendingClass == null) {
                return true;
            }
            final Boolean[] subType = new Boolean[1];
            subType[0] = false;
            ScanDialog.runWhenScanFinished( new Runnable() {
                
                @Override
                public void run() {
                    try {
                        js.runUserActionTask(new AbstractTask<CompilationController>() {
                            @Override
                             public void run(CompilationController controller) throws java.io.IOException {
                                 controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                                 SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                                 subType[0] = Boolean.valueOf(sourceUtils.isSubtype(extendingClass));
                             }
                         }, true);
                    } catch (Throwable t) { // we don't care about anything else happening here - either the file is recognize, or not
                        Logger.global.log(Level.INFO, t.getMessage());
                    }                    
                }
            }, NbBundle.getMessage(ClassDialog.class, "LBL_AnalyzeClass"));     // NOI18N
            
            return subType[0];
        }
    }
}
