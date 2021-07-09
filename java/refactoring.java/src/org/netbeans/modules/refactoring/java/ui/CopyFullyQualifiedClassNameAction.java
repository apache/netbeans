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

package org.netbeans.modules.refactoring.java.ui;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action to copy fully qualified name of selected classes.
 * 
 * @author Benjamin Asbach
 */
@ActionID(id = "org.netbeans.modules.refactoring.java.java.ui.CopyFullyQualifiedClassNameAction", category = "Edit")
@ActionRegistration(displayName = "Copy Fully Qualified Class Name", lazy = false)
@ActionReferences( value = {
    @ActionReference(path = "Editors/text/x-java/Popup", position = 3500),
    @ActionReference(path = "Loaders/text/x-java/Actions", position = 610)
})
public class CopyFullyQualifiedClassNameAction extends NodeAction {

    private final String name;
    
    public CopyFullyQualifiedClassNameAction() {
        this.name = NbBundle.getMessage(this.getClass(), "LBL_CopyFullyQualifiedClassNameAction");
    }
    
    @Override
    protected void performAction(Node[] activatedNodes) {
        for (Node activatedNode : activatedNodes) {
            FileObject javaSource = activatedNode.getLookup().lookup(FileObject.class);
            if (javaSource == null) {
                continue;
            }

            ClassPath classPath = ClassPath.getClassPath(javaSource, ClassPath.SOURCE);
            if (classPath == null) {
                continue;
            }
            String packagePath = classPath.getResourceName(javaSource.getParent());
            String packageName = packagePath.replace("/", ".");

            String fullyQualifiedClassName;
            if (isDefaultPackage(packageName)) {
                fullyQualifiedClassName = javaSource.getName();
            } else {
                fullyQualifiedClassName = packageName + "." + javaSource.getName();
            }

            setClipboardContent(fullyQualifiedClassName);
        }
    }

    private boolean isDefaultPackage(String packageName) {
        return "".equals(packageName);
    }

    private void setClipboardContent(String content) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(content), null);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return containsAnyFileObject(activatedNodes);
    }

    private boolean containsAnyFileObject(Node[] activatedNodes) {
        for (Node activatedNod : activatedNodes) {
            FileObject fileObject = activatedNod.getLookup().lookup(FileObject.class);
            if (fileObject != null) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

}
