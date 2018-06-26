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
