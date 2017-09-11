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
