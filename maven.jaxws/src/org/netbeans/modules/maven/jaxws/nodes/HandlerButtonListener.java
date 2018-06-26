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
package org.netbeans.modules.maven.jaxws.nodes;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.swing.ListModel;
import javax.swing.table.TableModel;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.maven.jaxws._RetoucheUtil;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.support.java.GenerationUtils;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.modules.websvc.spi.support.MessageHandlerPanel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Handler;
import org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChain;
import org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChains;
import org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChainsProvider;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Roderico Cruz, Milan Kuchtiak
 */
public class HandlerButtonListener implements ActionListener{
    MessageHandlerPanel panel;
    HandlerChains handlerChains;
    HandlerChain chain;
    FileObject handlerFO;
    //JavaClass implBeanClass;
    FileObject implBeanClass;
    JaxWsService service;
    boolean isNew;
    
    /**
     * If there is no HandlerChain annotation, handlerChains and handlerFO
     * will both be null;
     */
    public HandlerButtonListener(MessageHandlerPanel panel,
            HandlerChains handlerChains, FileObject handlerFO,
            FileObject implBeanClass, JaxWsService service,
            boolean isNew){
        this.panel = panel;
        this.handlerChains = handlerChains;
        this.handlerFO = handlerFO;
        this.implBeanClass = implBeanClass;
        this.service = service;
        this.isNew = isNew;
    }
    @Override
    public void actionPerformed(ActionEvent evt) {
        if(evt.getSource() == NotifyDescriptor.OK_OPTION) {
            if(!panel.isChanged()) return;
            if (isNew) {
                //add annotation
                String servicehandlerFileName = service.getServiceName() + "_handler"; //NOI18N
                FileObject parent = implBeanClass.getParent();
                final String handlerFileName = FileUtil.findFreeFileName(parent, servicehandlerFileName, "xml");
                CancellableTask<WorkingCopy> modificationTask = new CancellableTask<WorkingCopy>() {
                    @Override
                    public void run(WorkingCopy workingCopy) throws IOException {
                        workingCopy.toPhase(Phase.RESOLVED);
                        ClassTree javaClass = SourceUtils.getPublicTopLevelTree(workingCopy);
                        if (javaClass!=null) {
                            TreeMaker make = workingCopy.getTreeMaker();
                            List<ExpressionTree> attrs = new ArrayList<ExpressionTree>();
                            AssignmentTree attr1 = make.Assignment(make.Identifier("file"), 
                                    make.Literal(handlerFileName + ".xml"));        //NOI18N
                            attrs.add(attr1);
                            AnnotationTree chainAnnotation = make.Annotation(
                                    make.QualIdent("javax.jws.HandlerChain"),       //NOI18N
                                    attrs
                                    );
                            GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                            ClassTree modifiedClass = genUtils.addAnnotation(javaClass, chainAnnotation);
                            workingCopy.rewrite(javaClass, modifiedClass);
                        }
                    }
                    @Override
                    public void cancel() {
                    }
                };
                JavaSource targetSource = JavaSource.forFileObject(implBeanClass);
                try {
                    targetSource.runModificationTask(modificationTask).commit();
                } catch(IOException exp) {
                    ErrorManager.getDefault().notify(exp);
                }
                
                handlerFO = parent.getFileObject(handlerFileName, "xml");
                if(handlerFO == null) {
                    //create handler file
                    try {
                        WSUtils.retrieveHandlerConfigFromResource(parent,handlerFileName + ".xml");
                        handlerFO = parent.getFileObject(handlerFileName, "xml");
                    }catch(Exception exp){
                        ErrorManager.getDefault().notify(exp);
                    }
                }
                //initialize handlerChains
                try{
                    handlerChains =
                            HandlerChainsProvider.getDefault().getHandlerChains(handlerFO);
                }catch(Exception e){
                    ErrorManager.getDefault().notify(e);
                    return; //TODO handle this
                }
            }
            
            
            chain = handlerChains.getHandlerChains()[0];
            
            //refresh handlers
            Handler[] handlers = chain.getHandlers();
            for(int i = 0; i < handlers.length; i++){
                chain.removeHandler(handlers[i].getHandlerClass());
            }
            
            TableModel tableModel = panel.getHandlerTableModel();
            if(tableModel.getRowCount() > 0){
                for(int i = 0; i < tableModel.getRowCount(); i++){
                    String className = (String)tableModel.getValueAt(i, 0);
                    chain.addHandler(className, className);
                }
            }
            
            //if handler chain has no handlers, delete the annotation
            // and delete the handler xml file
            FileLock lock = null;
            OutputStream out = null;
            if(chain.getHandlers().length == 0) {
                
                CancellableTask<WorkingCopy> modificationTask = new CancellableTask<WorkingCopy>() {
                    @Override
                    public void run(WorkingCopy workingCopy) throws IOException {
                        workingCopy.toPhase(Phase.RESOLVED);
                        TypeElement typeElement = SourceUtils.getPublicTopLevelElement(workingCopy);
                        if (typeElement!=null) {
                            TreeMaker make = workingCopy.getTreeMaker();
                            AnnotationMirror chainAnnotation = _RetoucheUtil.
                                getAnnotation(workingCopy, typeElement, 
                                        "javax.jws.HandlerChain"); //NOI18N
                            if (chainAnnotation!=null) {
                                ClassTree classTree = workingCopy.getTrees().
                                    getTree(typeElement);
                                AnnotationTree anotTree = 
                                    (AnnotationTree)workingCopy.getTrees().
                                        getTree(typeElement,chainAnnotation);
                                ClassTree modifiedClass = make.Class(
                                        make.removeModifiersAnnotation(classTree.
                                                getModifiers(), anotTree),
                                        classTree.getSimpleName(),
                                        classTree.getTypeParameters(),
                                        classTree.getExtendsClause(),
                                        classTree.getImplementsClause(),
                                        classTree.getMembers());
                                workingCopy.rewrite(classTree, modifiedClass);
                            }
                        }
                    }
                    @Override
                    public void cancel() {
                    }
                };
                JavaSource targetSource = JavaSource.forFileObject(implBeanClass);
                try {
                    targetSource.runModificationTask(modificationTask).commit();
                } catch(IOException exp) {
                    ErrorManager.getDefault().notify(exp);
                }
                
                //delete the handler xml file
                try{
                    lock = handlerFO.lock();
                    handlerFO.delete(lock);
                }catch(Exception e){
                    ErrorManager.getDefault().notify(e);
                } finally{
                    if(lock != null){
                        lock.releaseLock();
                    }
                }
            } else{
                try{
                    lock = handlerFO.lock();
                    out = handlerFO.getOutputStream(lock);
                    handlerChains.write(out);
                }catch(Exception e){
                    ErrorManager.getDefault().notify(e);
                }finally{
                    if(lock != null){
                        lock.releaseLock();
                    }
                    if (out != null){
                        try{
                            out.close();
                        } catch(IOException ioe){
                            ErrorManager.getDefault().notify(ioe);
                        }
                    }
                }
            }
            
        }
    }
    
    private boolean isInModel(String className, ListModel model) {
        for(int i = 0; i < model.getSize(); i++){
            String cls = (String)model.getElementAt(i);
            if(className.equals(cls)){
                return true;
            }
        }
        return false;
    }
    
    private boolean isNewHandler(String className, HandlerChain handlerChain){
        if(handlerChain != null){
            Handler[] handlers = handlerChain.getHandlers();
            for(int i = 0; i < handlers.length; i++){
                if(handlers[i].getHandlerClass().equals(className)){
                    return false;
                }
            }
        }
        return true;
    }
    
    private static String readResource(InputStream is) throws IOException {
        // read the config from resource first
        StringBuffer sb = new StringBuffer();
        String lineSep = System.getProperty("line.separator");//NOI18N
        BufferedReader br = new BufferedReader(new InputStreamReader(is, 
                Charset.forName("UTF-8")));         // NOI18N
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append(lineSep);
            line = br.readLine();
        }
        br.close();
        return sb.toString();
    }
}
