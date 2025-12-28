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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.CopyRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 * @author Ralph Ruijs
 */
public class CopyClassesUI implements RefactoringUI, RefactoringUIBypass {

    private List<FileObject> resources;
    private Set<FileObject> javaObjects;
    private MoveClassPanel panel;
    private CopyRefactoring refactoring;
    private boolean disable;
    private FileObject targetFolder;
    private PasteType pasteType;

    private static String getString(String key) {
        return NbBundle.getMessage(CopyClassesUI.class, key);
    }

    public CopyClassesUI(Set<FileObject> javaObjects) {
        this(javaObjects, null, null);
    }

    public CopyClassesUI(Set<FileObject> javaObjects, FileObject targetFolder, PasteType paste) {
        this.disable = targetFolder != null;
        this.targetFolder = targetFolder;
        this.javaObjects = javaObjects;
        this.pasteType = paste;
        if (!disable) {
            resources = new ArrayList<FileObject>(javaObjects);
        }
    }

    @Override
    public String getName() {
        return getString("LBL_CopyClasses");
    }

    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            String pkgName = null;
            if (targetFolder != null) {
                ClassPath cp = ClassPath.getClassPath(targetFolder, ClassPath.SOURCE);
                if (cp != null) {
                    pkgName = cp.getResourceName(targetFolder, '.', false);
                }
            }
            panel = new MoveClassPanel(parent,
                    pkgName != null ? pkgName : getDOPackageName(javaObjects.iterator().next().getParent()),
                    getString("LBL_CopyClassesHeadline"),
                    getString("LBL_CopyWithoutRefactoring"),
                    targetFolder != null ? targetFolder : javaObjects.iterator().next(),
                    disable, getNodes());
        }
        return panel;
    }

    private static String getDOPackageName(FileObject f) {
        ClassPath cp = ClassPath.getClassPath(f, ClassPath.SOURCE);
        if (cp != null) {
            return cp.getResourceName(f, '.', false);
        } else {
            Logger.getLogger("org.netbeans.modules.refactoring.java").info("Cannot find classpath for " + f.getPath()); //NOI18N
            return f.getName();
        }
    }

    private Problem setParameters(boolean checkOnly) {
        if (panel == null) {
            return null;
        }
        URL url = URLMapper.findURL(panel.getRootFolder(), URLMapper.EXTERNAL);
        try {
            refactoring.setTarget(Lookups.singleton(new URL(url.toExternalForm() + panel.getPackageName().replace('.', '/')))); // NOI18N
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (checkOnly) {
            return refactoring.fastCheckParameters();
        } else {
            return refactoring.checkParameters();
        }
    }

    @Override
    public Problem checkParameters() {
        return setParameters(true);
    }

    @Override
    public Problem setParameters() {
        return setParameters(false);
    }

    @Override
    public AbstractRefactoring getRefactoring() {
        if (refactoring == null) {
            if (disable) {
                refactoring = new CopyRefactoring(Lookups.fixed(javaObjects.toArray()));
                refactoring.getContext().add(JavaRefactoringUtils.getClasspathInfoFor(javaObjects.toArray(new FileObject[0])));
            } else {
                refactoring = new CopyRefactoring(Lookups.fixed(resources.toArray()));
                refactoring.getContext().add(JavaRefactoringUtils.getClasspathInfoFor(resources.toArray(new FileObject[0])));
            }
        }
        return refactoring;
    }

    private final Vector getNodes() {
        Vector<Node> result = new Vector<Node>(javaObjects.size());
        LinkedList<FileObject> q = new LinkedList<FileObject>(javaObjects);
        while (!q.isEmpty()) {
            FileObject f = q.removeFirst();
            if (!VisibilityQuery.getDefault().isVisible(f)) {
                continue;
            }
            DataObject d = null;
            try {
                d = DataObject.find(f);
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (d instanceof DataFolder) {
                for (DataObject o : ((DataFolder) d).getChildren()) {
                    q.addLast(o.getPrimaryFile());
                }
            } else if(d != null) {
                result.add(d.getNodeDelegate());
            }
        }
        return result;
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.refactoring.java.ui.CopyClassesUI"); // NOI18N
    }

    @Override
    public boolean isRefactoringBypassRequired() {
        return panel != null && panel.isRefactoringBypassRequired();
    }

    @Override
    public void doRefactoringBypass() throws IOException {
        pasteType.paste();
    }
}
