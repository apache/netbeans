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
                refactoring.getContext().add(JavaRefactoringUtils.getClasspathInfoFor(javaObjects.toArray(new FileObject[javaObjects.size()])));
            } else {
                refactoring = new CopyRefactoring(Lookups.fixed(resources.toArray()));
                refactoring.getContext().add(JavaRefactoringUtils.getClasspathInfoFor(resources.toArray(new FileObject[resources.size()])));
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
