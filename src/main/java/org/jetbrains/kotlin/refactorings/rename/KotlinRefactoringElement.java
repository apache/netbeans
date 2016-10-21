/**
 * *****************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************
 */
package org.jetbrains.kotlin.refactorings.rename;

import java.io.IOException;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinRefactoringElement extends SimpleRefactoringElementImplementation {

    private final String newName;
    private final String oldName;
    private final PositionBounds bounds;
    private final FileObject fo;
    
    public KotlinRefactoringElement(FileObject fo, String newName, String oldName, PositionBounds bounds) {
        this.newName = newName;
        this.oldName = oldName;
        this.bounds = bounds;
        this.fo = fo;
    }
    
    @Override
    public String getText() {
        return newName;
    }

    @Override
    public String getDisplayText() {
        return newName;
    }

    @Override
    public void performChange() {
        try {
            bounds.setText(newName);
        } catch (IOException | BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void undoChange() {
        try {
            bounds.setText(oldName);
        } catch (IOException | BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    @Override
    public Lookup getLookup() {
        return Lookups.fixed();
    }

    @Override
    public FileObject getParentFile() {
        return fo;
    }

    @Override
    public PositionBounds getPosition() {
        return bounds;
    }
    
}