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
package org.netbeans.modules.refactoring.php.findusages;

import javax.swing.text.Position.Bias;
import org.netbeans.modules.refactoring.php.RefactoringUtils;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Petr Pisl
 */
public class WarningFileElement implements RefactoringElementImplementation {

    private FileObject file;
    private PositionBounds bounds;

    public WarningFileElement (FileObject file) {
        this.file = file;
        bounds = null;
    }

    @Override
    public String getText() {
        return NbBundle.getMessage(WarningFileElement.class, "MSG_FILE_IS_NOT_OPEN");
    }

    @Override
    public String getDisplayText() {
        return "<font color=\"#cc0000\">" + getText() + "</font>"; //NOI18N
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void setEnabled(boolean enabled) {
    }

    @Override
    public void performChange() {
    }

    @Override
    public void undoChange() {
    }

    @Override
    public Lookup getLookup() {
        Object composite = file;
        return Lookups.singleton(composite);
    }

    @Override
    public FileObject getParentFile() {
        return file;
    }

    @Override
    public PositionBounds getPosition() {
        if (bounds == null) {
            CloneableEditorSupport ces = RefactoringUtils.findCloneableEditorSupport(file);
            PositionRef ref1 = ces.createPositionRef(0, Bias.Forward);
            PositionRef ref2 = ces.createPositionRef(0, Bias.Forward);
            bounds = new PositionBounds(ref1, ref2);
        }
        return bounds;
    }

    @Override
    public int getStatus() {
        return RefactoringElementImplementation.WARNING;
    }

    @Override
    public void setStatus(int status) {

    }

    @Override
    public void openInEditor() {
    }

    @Override
    public void showPreview() {
    }

}
