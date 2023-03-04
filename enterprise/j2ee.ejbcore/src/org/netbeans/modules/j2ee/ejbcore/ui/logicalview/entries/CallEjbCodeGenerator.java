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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.javaee.specs.support.api.EjbSupport;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;

/**
 * Provide action for calling another EJB
 * @author Chris Webster
 * @author Martin Adamek
 */
public class CallEjbCodeGenerator implements CodeGenerator {

    private FileObject srcFile;
    private TypeElement beanClass;

    public static class Factory implements CodeGenerator.Factory {

        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> ret = new ArrayList<CodeGenerator>();
            JTextComponent component = context.lookup(JTextComponent.class);
            CompilationController controller = context.lookup(CompilationController.class);
            TreePath path = context.lookup(TreePath.class);
            path = path != null ? SendEmailCodeGenerator.getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, path) : null;
            if (component == null || controller == null || path == null)
                return ret;
            try {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                Element elem = controller.getTrees().getElement(path);
                if (elem != null) {
                    CallEjbCodeGenerator gen = createCallEjbAction(component, controller, elem);
                    if (gen != null)
                        ret.add(gen);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            return ret;
        }

    }

    static CallEjbCodeGenerator createCallEjbAction(JTextComponent component, CompilationController cc, Element el) throws IOException {
        if (el.getKind() != ElementKind.CLASS)
            return null;
        TypeElement typeElement = (TypeElement)el;
        if (!isEnable(cc.getFileObject(), typeElement)) {
            return null;
        }
        return new CallEjbCodeGenerator(cc.getFileObject(), typeElement);
    }

    public CallEjbCodeGenerator(FileObject srcFile, TypeElement beanClass) {
        this.srcFile = srcFile;
        this.beanClass = beanClass;
    }

    public void invoke() {
        try {
            CallEjbDialog callEjbDialog = new CallEjbDialog();
            callEjbDialog.open(srcFile, beanClass.getQualifiedName().toString(), NbBundle.getMessage(CallEjbCodeGenerator.class, "LBL_CallEjbActionTitle")); //NOI18N
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    public String getDisplayName() {
        return NbBundle.getMessage(CallEjbCodeGenerator.class, "LBL_CallEjbAction");
    }
    
    private static boolean isEnable(FileObject srcFile, TypeElement typeElement) {
        Project project = FileOwnerQuery.getOwner(srcFile);
        if (project == null) {
            return false;
        }
        J2eeModuleProvider j2eeModuleProvider = project.getLookup ().lookup (J2eeModuleProvider.class);
        if (j2eeModuleProvider != null) {
            if (project.getLookup().lookup(EnterpriseReferenceContainer.class) == null) {
                return false;
            }
            String serverInstanceId = j2eeModuleProvider.getServerInstanceID();
            if (serverInstanceId == null) {
                return true;
            }
            J2eePlatform platform = null;
            try {
                platform = Deployment.getDefault().getServerInstance(serverInstanceId).getJ2eePlatform();
            } catch (InstanceRemovedException ex) {
                Logger.getLogger(CallEjbCodeGenerator.class.getName()).log(Level.FINE, null, ex);
            }
            if (platform == null) {
                return true;
            }
            if (!EjbSupport.getInstance(platform).isEjb31LiteSupported(platform)
                    && !platform.getSupportedTypes().contains(J2eeModule.Type.EJB)) {
                return false;
            }
        } else {
            return false;
        }

        return ElementKind.INTERFACE != typeElement.getKind();
    }
    
}
