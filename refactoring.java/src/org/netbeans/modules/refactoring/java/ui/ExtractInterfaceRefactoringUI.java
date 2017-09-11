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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

import com.sun.source.util.TreePath;
import javax.swing.event.ChangeListener;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.ui.ElementHeaders;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.ExtractInterfaceRefactoring;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Refactoring UI object for Extract Interface refactoring.
 *
 * @author Martin Matula, Jan Becicka, Jan Pokorsky
 */
public final class ExtractInterfaceRefactoringUI implements RefactoringUI, JavaRefactoringUIFactory {
    // reference to extract interface refactoring this UI object corresponds to
    private ExtractInterfaceRefactoring refactoring;
    // source type
    private TreePathHandle sourceType;
    // UI panel for collecting parameters
    private ExtractInterfacePanel panel;
    private String name;

    private ExtractInterfaceRefactoringUI() {
    }
    
    /** Creates a new instance of ExtractInterfaceRefactoringUI
     * @param selectedElement Elements the refactoring action was invoked on.
     */
    @Override
    public RefactoringUI create(CompilationInfo info, TreePathHandle[] handles, FileObject[] files, NonRecursiveFolder[] packages) {
        TreePath path = handles[0].resolve(info);

        path = JavaRefactoringUtils.findEnclosingClass(info, path, true, true, true, true, false);

        if (path != null) {
            return new ExtractInterfaceRefactoringUI(path, info);
        }

        return null;
    }

    private ExtractInterfaceRefactoringUI(TreePath path, CompilationInfo info) {
        // compute source type
        this.name = ElementHeaders.getHeader(path, info, ElementHeaders.NAME);
        this.sourceType = TreePathHandle.create(path, info);
        // create an instance of pull up refactoring object
        refactoring = new ExtractInterfaceRefactoring(sourceType);
        refactoring.getContext().add(info.getClasspathInfo());
    }
    
    // --- IMPLEMENTATION OF RefactoringUI INTERFACE ---------------------------
    
    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new ExtractInterfacePanel(refactoring, parent);
        }
        return panel;
    }

    @Override
    public Problem setParameters() {
        captureParameters();
        return refactoring.checkParameters();
    }
    
    @Override
    public Problem checkParameters() {
        captureParameters();
        return refactoring.fastCheckParameters();
    }

    @Override
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(ExtractInterfaceAction.class, "DSC_ExtractInterface", name); // NOI18N
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ExtractInterfaceAction.class, "LBL_ExtractInterface"); // NOI18N
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.refactoring.java.ui.ExtractInterfaceRefactoringUI"); // NOI18N
    }
    
    // --- PRIVATE HELPER METHODS ----------------------------------------------
    
    /** Gets parameters from the refactoring panel and sets them
     * to the refactoring object.
     */
    private void captureParameters() {
        panel.storeSettings();
    }
    
    public static JavaRefactoringUIFactory factory() {
        return new ExtractInterfaceRefactoringUI();
    }

    
}
