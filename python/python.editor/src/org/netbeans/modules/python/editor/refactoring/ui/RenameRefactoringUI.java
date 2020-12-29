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
package org.netbeans.modules.python.editor.refactoring.ui;

import java.io.IOException;
import java.text.MessageFormat;
import javax.swing.event.ChangeListener;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.python.editor.refactoring.PythonRefUtils;
import org.netbeans.modules.python.editor.refactoring.PythonElementCtx;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @todo There are a lot of constructors here; figure out which ones are unused, and
 *   nuke them!
 * 
 */
public class RenameRefactoringUI implements RefactoringUI, RefactoringUIBypass {
    private final AbstractRefactoring refactoring;
    private String oldName = null;
    private String dispOldName;
    private String newName;
    private RenamePanel panel;
    private boolean fromListener = false;
    private PythonElementCtx jmiObject; // TODO rename
    private FileObject byPassFolder;
    private boolean byPassPakageRename;
    private boolean pkgRename = true;
    private String stripPrefix;

    public RenameRefactoringUI(PythonElementCtx handle) {
        this.jmiObject = handle;
        stripPrefix = handle.getStripPrefix();
        this.refactoring = new RenameRefactoring(Lookups.singleton(handle));
        //oldName = handle.resolveElement(info).getSimpleName().toString();
        oldName = handle.getSimpleName();

//        ClasspathInfo classpath = PythonRefUtils.getClasspathInfoFor(handle);
//        if (classpath != null) {
//            refactoring.getContext().add(classpath);
//        }

        dispOldName = oldName;

        //this(jmiObject, (FileObject) null, true);

        // Force refresh!
        this.refactoring.getContext().add(UI.Constants.REQUEST_PREVIEW);
    }

    public RenameRefactoringUI(FileObject file, PythonElementCtx handle) {
        if (handle != null) {
            jmiObject = handle;
            this.refactoring = new RenameRefactoring(Lookups.fixed(file, handle));
            //oldName = jmiObject.resolveElement(info).getSimpleName().toString();
            oldName = jmiObject.getSimpleName();
        } else {
            this.refactoring = new RenameRefactoring(Lookups.fixed(file));
            oldName = file.getName();
        }
        dispOldName = oldName;
//        ClasspathInfo cpInfo = handle == null ? PythonRefUtils.getClasspathInfoFor(file) : PythonRefUtils.getClasspathInfoFor(handle);
//        if (cpInfo != null) {
//            refactoring.getContext().add(cpInfo);
//        }
        //this(jmiObject, (FileObject) null, true);

        // Force refresh!
        this.refactoring.getContext().add(UI.Constants.REQUEST_PREVIEW);
    }

    public RenameRefactoringUI(NonRecursiveFolder file) {
        this.refactoring = new RenameRefactoring(Lookups.singleton(file));
        oldName = PythonRefUtils.getPackageName(file.getFolder());
//        ClasspathInfo classpath = PythonRefUtils.getClasspathInfoFor(file.getFolder());
//        if (classpath != null) {
//            refactoring.getContext().add(classpath);
//        }
        dispOldName = oldName;
        pkgRename = true;
        //this(jmiObject, (FileObject) null, true);

        // Force refresh!
        this.refactoring.getContext().add(UI.Constants.REQUEST_PREVIEW);
    }

    RenameRefactoringUI(FileObject jmiObject, String newName, PythonElementCtx handle) {
        if (handle != null) {
            this.refactoring = new RenameRefactoring(Lookups.fixed(jmiObject, handle));
        } else {
            this.refactoring = new RenameRefactoring(Lookups.fixed(jmiObject));
        }
        //this.jmiObject = jmiObject;
        oldName = newName;
        //[FIXME] this should be oldName of refactored object
        this.dispOldName = newName;
//        ClasspathInfo cpInfo = handle == null ? PythonRefUtils.getClasspathInfoFor(jmiObject) : PythonRefUtils.getClasspathInfoFor(handle);
//        if (cpInfo != null) {
//            refactoring.getContext().add(cpInfo);
//        }
        fromListener = true;

        // Force refresh!
        this.refactoring.getContext().add(true);
    }

    RenameRefactoringUI(NonRecursiveFolder jmiObject, String newName) {
        this.refactoring = new RenameRefactoring(Lookups.singleton(jmiObject));
//        ClasspathInfo classpath = PythonRefUtils.getClasspathInfoFor(jmiObject.getFolder());
//        if (classpath != null) {
//            refactoring.getContext().add(classpath);
//        }
        //this.jmiObject = jmiObject;
        oldName = newName;
        //[FIXME] this should be oldName of refactored object
        this.dispOldName = newName;
        fromListener = true;
        pkgRename = true;

        // Force refresh!
        this.refactoring.getContext().add(UI.Constants.REQUEST_PREVIEW);
    }

    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            String name = oldName;

            if (stripPrefix != null && name.startsWith(stripPrefix)) {
                name = name.substring(stripPrefix.length());
            }

            String suffix = "";
            if (jmiObject != null) {
                ElementKind kind = PythonRefUtils.getElementKind(jmiObject);
                //if (kind.isClass() || kind.isInterface()) {
                if (kind == ElementKind.CLASS/* || kind == ElementKind.MODULE*/) {
                    suffix = /*kind.isInterface() ? getString("LBL_Interface") : */ getString("LBL_Class");
                } else if (kind == ElementKind.METHOD) {
                    suffix = getString("LBL_Method");
                } else if (kind == ElementKind.FIELD) {
                    suffix = getString("LBL_Field");
                } else if (kind == ElementKind.VARIABLE) {
                    suffix = getString("LBL_LocalVar");
                } else if (kind == ElementKind.MODULE || (jmiObject == null && fromListener)) {
                    suffix = pkgRename ? getString("LBL_Package") : getString("LBL_Folder");
                } else if (kind == ElementKind.PARAMETER) {
                    suffix = getString("LBL_Parameter");
                }
            }
            suffix = suffix + " " + name; // NOI18N

            // TODO: For dynamic variables and instance variables
            panel = new RenamePanel(name, parent, NbBundle.getMessage(RenamePanel.class, "LBL_Rename") + " " + suffix, !fromListener, fromListener && !byPassPakageRename);
        }
        return panel;
    }

    private static String getString(String key) {
        return NbBundle.getMessage(RenameRefactoringUI.class, key);
    }

    private String getPanelName() {
        String name = panel.getNameValue();

        if (stripPrefix != null && !name.startsWith(stripPrefix)) {
            name = stripPrefix + name;
        }

        return name;
    }

    @Override
    public org.netbeans.modules.refactoring.api.Problem setParameters() {
        newName = getPanelName();
        if (refactoring instanceof RenameRefactoring) {
            ((RenameRefactoring)refactoring).setNewName(newName);
            ((RenameRefactoring)refactoring).setSearchInComments(panel.searchJavadoc());
        }
        return refactoring.checkParameters();
    }

    @Override
    public org.netbeans.modules.refactoring.api.Problem checkParameters() {
        if (!panel.isUpdateReferences()) {
            return null;
        }
        newName = getPanelName();
        if (refactoring instanceof RenameRefactoring) {
            ((RenameRefactoring)refactoring).setNewName(newName);
        }
        return refactoring.fastCheckParameters();
    }

    @Override
    public org.netbeans.modules.refactoring.api.AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    @Override
    public String getDescription() {
        return new MessageFormat(NbBundle.getMessage(RenamePanel.class, "DSC_Rename")).format(
                new Object[]{dispOldName, newName});
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(RenamePanel.class, "LBL_Rename");
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public boolean isRefactoringBypassRequired() {
        return !panel.isUpdateReferences();
    }

    @Override
    public void doRefactoringBypass() throws IOException {
        DataObject dob = null;
        if (byPassFolder != null) {
            dob = DataFolder.findFolder(byPassFolder);
        } else {
            dob = DataObject.find(refactoring.getRefactoringSource().lookup(FileObject.class));
        }
        dob.rename(getPanelName());
    }
}
