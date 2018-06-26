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
package org.netbeans.modules.web.jsf.refactoring;

import java.io.IOException;
import java.lang.ref.WeakReference;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.web.jsf.refactoring.Modifications.Difference;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Petr Pisl
 */
public abstract class DiffElement extends SimpleRefactoringElementImplementation {
    private final Difference diff;
    private final Modifications modification;
    private WeakReference<String> newFileContent;
    private final FileObject parentFile;
    
    public DiffElement(Difference diff, FileObject parentFile, Modifications modification) {
        this.diff = diff;
        this.modification = modification;
        this.parentFile = parentFile;
    }
    
    public String getDisplayText(){
        return diff.getDesription();
    }
    
    public String getText(){
        return diff.getDesription();
    }
    
    public Lookup getLookup() {
        return Lookups.fixed(parentFile, diff);
    }
    
    public void setEnabled(boolean enabled) {
        diff.setExclude(!enabled);
        newFileContent = null;
        super.setEnabled(enabled);
    }
    
    public FileObject getParentFile() {
        return parentFile;
    }
    
    @Override
    protected String getNewFileContent() {
        String result;
        if (newFileContent !=null) {
            result = newFileContent.get();
            if (result!=null)
                return result;
        }
        try {
            result = modification.getResultingSource(parentFile);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        newFileContent = new WeakReference(result);
        return result;
    }
    
    
    public static class ChangeFQCNElement extends DiffElement{
        private final Occurrences.OccurrenceItem occurence;
        
        public ChangeFQCNElement(Difference diff, Occurrences.OccurrenceItem occurence, Modifications modification) {
            super(diff, occurence.getFacesConfig(), modification);
            this.occurence = occurence;
        }
        
        public void performChange() {
            try {
                occurence.performChange();
            } catch (IllegalStateException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        @Override
        public void undoChange() {
            try {
                occurence.undoChange();
            } catch (IllegalStateException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        public PositionBounds getPosition() {
            return occurence.getChangePosition();
        }
    }
}
