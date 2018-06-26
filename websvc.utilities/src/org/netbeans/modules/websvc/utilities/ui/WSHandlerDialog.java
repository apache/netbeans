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

package org.netbeans.modules.websvc.utilities.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Roderico Cruz, Milan Kuchtiak
 * Displays a Dialog for selecting web service message handler classes
 * that are in a project.
 */
public class WSHandlerDialog {
    private Dialog dialog;
    private SelectHandlerPanel sPanel;
    private AddMessageHandlerDialogDesc dlgDesc;
    private boolean isJaxWS;
    private Map<String, Integer> handlerMap;
    
    //Handler types
    public static final int JAXWS_LOGICAL_HANDLER = 1;
    public static final int JAXWS_MESSAGE_HANDLER = 2;
    public static final int JAXRPC_MESSAGE_HANDLER = 3;
    public static final int INVALID_HANDLER = -1;
    
    /**
     * Creates a new instance of WSHandlerDialog
     */
    public WSHandlerDialog(Project project, boolean isJaxWS) {
        this.isJaxWS = isJaxWS;
        sPanel = new SelectHandlerPanel(project);
        dlgDesc = new AddMessageHandlerDialogDesc(sPanel);
        dialog = DialogDisplayer.getDefault().createDialog(dlgDesc);
        dialog.getAccessibleContext().setAccessibleDescription(dialog.getTitle());
        handlerMap = new TreeMap<String, Integer>();
    }
    
    public void show(){
        dialog.setVisible(true);
    }
    
    public boolean okButtonPressed(){
        return dlgDesc.getValue() == DialogDescriptor.OK_OPTION;
    }
    
    public Map<String, Integer> getSelectedClasses(){
        return handlerMap;
    }
    
    private FileObject getFileObjectFromNode(Node n) {
        DataObject dObj = n.getCookie(DataObject.class);
        if (dObj!=null) return dObj.getPrimaryFile();
        return null;
    }
    
    class AddMessageHandlerDialogDesc extends DialogDescriptor{
        Project project;
        final SelectHandlerPanel sPanel;
        
        private Object[] closingOptionsWithoutOK = {DialogDescriptor.CANCEL_OPTION,
        DialogDescriptor.CLOSED_OPTION};
        private Object[] closingOptionsWithOK = {DialogDescriptor.CANCEL_OPTION,
        DialogDescriptor.CLOSED_OPTION, DialogDescriptor.OK_OPTION};
        
        /**
         * Creates a new instance of AddMessageHandlerDialogDesc
         */
        public AddMessageHandlerDialogDesc(SelectHandlerPanel sPanel) {
            super(sPanel, NbBundle.getMessage(WSHandlerDialog.class, "TTL_SelectHandler"));
            this.sPanel = sPanel;
            this.setButtonListener(new AddMessageActionListener(sPanel));
        }
        
        class AddMessageActionListener implements ActionListener{
            SelectHandlerPanel sPanel;
            public AddMessageActionListener(SelectHandlerPanel sPanel){
                this.sPanel = sPanel;
            }
            public void actionPerformed(ActionEvent evt) {
                if(evt.getSource() == NotifyDescriptor.OK_OPTION){
                    boolean accepted = true;
                    String errMsg = null;
                    Node[] selectedNodes = sPanel.getSelectedNodes();
                    for(int i = 0; i < selectedNodes.length; i++){
                        Node node = selectedNodes[i];
                        FileObject javaClassFo = getFileObjectFromNode(node);
                        //FIX-ME: Improve this by filtering the Tree View to only include handlers
                        if(javaClassFo == null){
                            errMsg = NbBundle.getMessage(WSHandlerDialog.class,
                                    "NotJavaClass_msg");
                            accepted = false;
                            break;
                        }
                        final int[] handlerType = new int[]{JAXWS_LOGICAL_HANDLER};
                        
                        final JavaSource javaSource = JavaSource.
                            forFileObject(javaClassFo);
                        if(javaSource == null){
                            errMsg = NbBundle.getMessage(WSHandlerDialog.class,
                                    "NotJavaClass_msg");
                            accepted = false;
                            break;
                        }
                        
                        final CancellableTask<CompilationController> task = 
                            new CancellableTask<CompilationController>() 
                        {
                            @Override
                            public void run(CompilationController controller) 
                                throws IOException 
                            {
                                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                                handlerType[0] = getHandlerType(controller, isJaxWS);
                            }
                            @Override
                            public void cancel() {
                            }
                        };
                        
                        runTask( i==0, javaSource , task );
                        
                        if(handlerType[0] == INVALID_HANDLER) {
                            errMsg = NbBundle.getMessage(WSHandlerDialog.class,
                                    "NotHandlerClass_msg");
                            accepted = false;
                            break;
                        } else{
                            FileObject fo = getFileObjectFromNode(node);
                            if (fo!=null) {
                                ClassPath classPath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
                                String handlerClassName = classPath.getResourceName(fo, '.', false);
                                handlerMap.put(handlerClassName, handlerType[0]);
                            }
                        }
                    }
                    if (!accepted) {
                        NotifyDescriptor.Message notifyDescr =
                                new NotifyDescriptor.Message(errMsg,
                                NotifyDescriptor.ERROR_MESSAGE );
                        DialogDisplayer.getDefault().notify(notifyDescr);
                        AddMessageHandlerDialogDesc.this.setClosingOptions(closingOptionsWithoutOK);
                    } else {
                        // Everything was fine so allow OK
                        AddMessageHandlerDialogDesc.this.setClosingOptions(closingOptionsWithOK);
                    }
                }
            }
        }
    }
    
    public static void runTask( boolean firstTask , final JavaSource javaSource, 
            final CancellableTask<CompilationController> task )
    {
        if ( firstTask ){
            ScanDialog.runWhenScanFinished( new Runnable() {
                
                @Override
                public void run() {
                    try {
                        javaSource.runUserActionTask(task, true);
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }                                    
                }
            }, NbBundle.getMessage(WSHandlerDialog.class, 
                    "LBL_SearchHandlers"));         // NOI18N
        }
        else {
            try {
                javaSource.runUserActionTask(task, true);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
    public static int getHandlerType(CompilationController cc, boolean isJaxWS) throws IOException {
        TypeElement typeElement = SourceUtils.getPublicTopLevelElement(cc);
        if (typeElement!=null) {
            TypeMirror classMirror = typeElement.asType();
            
            if(isJaxWS) {
                // test if class extends "javax.xml.ws.handler.LogicalHandler<C extends javax.xml.ws.handler.LogicalMessageContext>"
                TypeElement handlerElement = cc.getElements().getTypeElement("javax.xml.ws.handler.LogicalHandler"); //NOI18N
                DeclaredType handlerType=null;
  
                if (handlerElement!=null) {
                    TypeElement messageContextElement = cc.getElements().getTypeElement("javax.xml.ws.handler.LogicalMessageContext"); //NOI18N
                    WildcardType wildcardType = cc.getTypes().getWildcardType(messageContextElement.asType(), null);
                    handlerType = cc.getTypes().getDeclaredType(handlerElement, wildcardType);
                }
                if (handlerType!=null && cc.getTypes().isAssignable(classMirror, handlerType)) {
                    return JAXWS_LOGICAL_HANDLER;
                }
 
                // test if class extends "javax.xml.ws.handler.Handler<C extends javax.xml.ws.handler.MessageContext>"
                handlerElement = cc.getElements().getTypeElement("javax.xml.ws.handler.Handler"); //NOI18N
                handlerType=null;
  
                if (handlerElement!=null) {
                    TypeElement messageContextElement = cc.getElements().getTypeElement("javax.xml.ws.handler.MessageContext"); //NOI18N
                    WildcardType wildcardType = cc.getTypes().getWildcardType(messageContextElement.asType(), null);
                    handlerType = cc.getTypes().getDeclaredType(handlerElement, wildcardType);
                }
                if (handlerType!=null && cc.getTypes().isAssignable(classMirror, handlerType)) {
                    return JAXWS_MESSAGE_HANDLER;
                }

            } else {
                // test if class extends "javax.xml.rpc.handler.Handler"
                TypeElement handlerElement = cc.getElements().getTypeElement("javax.xml.rpc.handler.Handler"); //NOI18N
                if (handlerElement!=null && cc.getTypes().isSubtype(classMirror, handlerElement.asType())) {
                    return JAXRPC_MESSAGE_HANDLER;
                }
            }
        }
        return INVALID_HANDLER;
    }
    // }
    
}
