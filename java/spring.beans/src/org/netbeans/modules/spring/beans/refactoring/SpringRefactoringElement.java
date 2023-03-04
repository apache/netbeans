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

package org.netbeans.modules.spring.beans.refactoring;

import java.io.IOException;
import java.lang.ref.WeakReference;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.spring.beans.refactoring.Modifications.Difference;
import org.netbeans.modules.spring.beans.refactoring.Modifications.Difference.Kind;
import org.netbeans.modules.spring.beans.refactoring.Occurrences.Occurrence;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class SpringRefactoringElement extends SimpleRefactoringElementImplementation {

    private final Occurrence occurrence;

    public static SpringRefactoringElement create(Occurrence occurrence) {
        return new SpringRefactoringElement(occurrence);
    }

    public static SpringRefactoringElement createJavaElementRefModification(Occurrence occurrence, Modifications mods, String oldSimpleName, String newBinaryName) {
        return new JavaElementRefModification(occurrence, mods, oldSimpleName, newBinaryName);
    }
    
    public static SpringRefactoringElement createPropertyRefModification(Occurrence occurrence, Modifications mods, String oldName, String newName) {
        return new PropertyRefModification(occurrence, mods, oldName, newName);
    }

    private SpringRefactoringElement(Occurrence occurrence) {
        this.occurrence = occurrence;
    }

    public String getText() {
        return getDisplayText();
    }

    public String getDisplayText() {
        return occurrence.getDisplayText();
    }

    public void performChange() {
        // Nothing to do.
    }

    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    public FileObject getParentFile() {
        return occurrence.getFileObject();
    }

    public PositionBounds getPosition() {
        return occurrence.getPosition();
    }

    @Override
    protected String getNewFileContent() {
        // Nothing to do.
        return null;
    }

    private static class JavaElementRefModification extends SpringRefactoringElement {

        protected final Modifications mods;
        private final Difference diff;
        private final String oldSimpleName;

        private WeakReference<String> newFileContent;

        public JavaElementRefModification(Occurrence occurrence, Modifications mods, String oldSimpleName, String newBinaryName) {
            super(occurrence);
            this.mods = mods;
            this.oldSimpleName = oldSimpleName;
            diff = new Difference(Kind.CHANGE, occurrence.getPosition(), newBinaryName);
            mods.addDifference(occurrence.getFileObject(), diff);
        }

        @Override
        public String getDisplayText() {
            if (oldSimpleName != null) {
                return NbBundle.getMessage(JavaElementRefModification.class, "MSG_UpdateReference", oldSimpleName);
            } else {
                return NbBundle.getMessage(JavaElementRefModification.class, "MSG_Update");
            }
        }

        @Override
        public void setEnabled(boolean enabled) {
            diff.setExcluded(!enabled);
            newFileContent = null;
            super.setEnabled(enabled);
        }

        @Override
        protected String getNewFileContent() {
            String content;
            if (newFileContent != null) {
                content = newFileContent.get();
                if (content != null)
                    return content;
            }
            try {
                content = mods.getResultingSource(getParentFile());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
            newFileContent = new WeakReference<String>(content);
            return content;
        }
    }
    
    private static class PropertyRefModification extends SpringRefactoringElement {
        protected final Modifications mods;
        private final Difference diff;
        private final String oldSimpleName;

        private WeakReference<String> newFileContent;
        
        public PropertyRefModification(Occurrence occurrence, Modifications mods, String oldSimpleName, String newBinaryName) {
            super(occurrence);
            this.mods = mods;
            this.oldSimpleName = oldSimpleName;
            diff = new Difference(Kind.CHANGE, occurrence.getPosition(), newBinaryName);
            mods.addDifference(occurrence.getFileObject(), diff);
        }
        
        @Override
        public String getDisplayText() {
            if (oldSimpleName != null) {
                return NbBundle.getMessage(JavaElementRefModification.class, "MSG_UpdateReference", oldSimpleName);
            } else {
                return NbBundle.getMessage(JavaElementRefModification.class, "MSG_Update");
            }
        }

        @Override
        public void setEnabled(boolean enabled) {
            diff.setExcluded(!enabled);
            newFileContent = null;
            super.setEnabled(enabled);
        }

        @Override
        protected String getNewFileContent() {
            String content;
            if (newFileContent != null) {
                content = newFileContent.get();
                if (content != null)
                    return content;
            }
            try {
                content = mods.getResultingSource(getParentFile());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
            newFileContent = new WeakReference<String>(content);
            return content;
        }
    }
}
