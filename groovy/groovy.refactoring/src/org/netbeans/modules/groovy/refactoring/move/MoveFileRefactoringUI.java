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

package org.netbeans.modules.groovy.refactoring.move;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.groovy.refactoring.findusages.model.RefactoringElement;
import static org.netbeans.modules.groovy.refactoring.move.Bundle.*;
import org.netbeans.modules.groovy.refactoring.ui.MoveClassPanel;
import org.netbeans.modules.groovy.refactoring.utils.IdentifiersUtil;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Martin Janicek
 */
public class MoveFileRefactoringUI  implements RefactoringUI, RefactoringUIBypass {

    private final AbstractRefactoring refactoring;
    private final FileObject fo;
    private MoveClassPanel panel;


    public MoveFileRefactoringUI(RefactoringElement element) {
        fo = element.getFileObject();
        Collection<Object> lookupContent = new ArrayList<Object>();
        lookupContent.add(element);
        refactoring = new MoveRefactoring(Lookups.fixed(lookupContent.toArray()));
    }

    @Override
    @Messages("LBL_Move=Move")
    public String getName() {
        return LBL_Move();
    }

    @Override
    @Messages("LBL_Move_Descr=Groovy Elements Move")
    public String getDescription() {
        return LBL_Move_Descr();
    }

    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    @Messages("LBL_MoveClassNamed=Move class {0}")
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            final String packageName = IdentifiersUtil.getPackageName(fo);
            final String sourceName = fo.getName();

            panel = new MoveClassPanel(packageName, LBL_MoveClassNamed(sourceName), fo);
        }
        return panel;
    }

    @Override
    public Problem setParameters() {
        return null;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.groovy.refactoring.move.MoveRefactoringUI"); //NOI18N
    }

    @Override
    public boolean isRefactoringBypassRequired() {
        return false;
    }

    @Override
    public void doRefactoringBypass() throws IOException {
    }
}
