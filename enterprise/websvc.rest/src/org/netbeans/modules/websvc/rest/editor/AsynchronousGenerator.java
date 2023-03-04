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
package org.netbeans.modules.websvc.rest.editor;

import java.awt.Toolkit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.swing.text.JTextComponent;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import com.sun.source.util.TreePath;
import org.openide.util.Exceptions;

/**
 * @author ads
 */
public class AsynchronousGenerator extends AsyncConverter implements CodeGenerator {

    private AsynchronousGenerator( CompilationController controller,
            JTextComponent component )
    {
        this.controller = controller;
        this.textComponent = component;
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.editor.codegen.CodeGenerator#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(AsynchronousGenerator.class,"LBL_ConvertMethod");    // NOI18N
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.editor.codegen.CodeGenerator#invoke()
     */
    @Override
    public void invoke() {
        if (!isApplicable(controller.getFileObject())){
            Toolkit.getDefaultToolkit().beep();
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(AsynchronousGenerator.class, 
                            "MSG_NotJee7Profile"));                  // NOI18N
            return;
        }
        
        int position = textComponent.getCaret().getDot();
        TreePath tp = controller.getTreeUtilities().pathFor(position);
        Element contextElement = controller.getTrees().getElement(tp );
        if (contextElement == null || !isApplicable(contextElement)){
            Toolkit.getDefaultToolkit().beep();
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(AsynchronousGenerator.class, 
                            "MSG_NotRestMethod"));                  // NOI18N
            return;
        }        
        
        Element enclosingElement = contextElement.getEnclosingElement();
        TypeElement clazz = (TypeElement)enclosingElement;
        final String fqn = clazz.getQualifiedName().toString();
        
        if ( !checkRestMethod(fqn,contextElement, controller.getFileObject()) ){
            Toolkit.getDefaultToolkit().beep();
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(AsynchronousGenerator.class, 
                            "MSG_NotRestMethod"));                  // NOI18N
            return;
        }
        
        if (isAsync(contextElement)) {
            Toolkit.getDefaultToolkit().beep();
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(AsynchronousGenerator.class, 
                            "MSG_AsyncMethod"));                  // NOI18N
            return;
        }
        
        ElementHandle<Element> handle = ElementHandle.create(contextElement);
        try {
            convertMethod(handle,controller.getFileObject());
        }
        catch(IOException e ){
            Toolkit.getDefaultToolkit().beep();
            getLogger().log(Level.INFO, null , e);
        }
    }
    
    @Override
    protected Logger getLogger() {
        return Logger.getLogger(AsyncConverterTask.class.getName());
    }
    
    public static class Factory implements CodeGenerator.Factory {

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            CompilationController controller = context.lookup(CompilationController.class);

            List<CodeGenerator> ret = new ArrayList<CodeGenerator>();
            if (controller != null) {
                try {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    FileObject targetSource = controller.getFileObject();
                    if (targetSource != null) {
                        JTextComponent targetComponent = context.lookup(JTextComponent.class);
                        AsynchronousGenerator gen = new AsynchronousGenerator(controller, targetComponent);

                        int position = targetComponent.getCaret().getDot();
                        TreePath tp = controller.getTreeUtilities().pathFor(position);
                        Element contextElement = controller.getTrees().getElement(tp );
                        if (contextElement != null && gen.isApplicable(contextElement)) {
                            ret.add(gen);
                        }
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return ret;
        }
    }
    
    private final CompilationController controller;
    private final JTextComponent textComponent;

}
