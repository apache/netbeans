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
package org.netbeans.modules.refactoring.java.spi;

import java.io.IOException;
import java.lang.ref.WeakReference;
import javax.swing.text.Position;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.modules.refactoring.java.ui.tree.ElementGripFactory;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Implementatation of RefactoringElementImplementation specific to refactoring
 * in java files.
 * 
 * @author Jan Becicka
 */
 public final class DiffElement extends SimpleRefactoringElementImplementation {
    private PositionBounds bounds;
    private String displayText;
    private FileObject parentFile;
    private Difference diff;
    private ModificationResult modification;
    private WeakReference<String> newFileContent;
    
    private DiffElement(Difference diff, PositionBounds bounds, FileObject parentFile, ModificationResult modification) {
        this.bounds = bounds;
        final String description = diff.getDescription();
        if (description == null) {
            displayText = NbBundle.getMessage(DiffElement.class, "LBL_NotAvailable");
        } else {
            this.displayText = description;
        }
        this.parentFile = parentFile;
        this.diff = diff;
        this.modification = modification;
    }

    @Override
    public String getDisplayText() {
        return displayText;
    }

    @Override
    public Lookup getLookup() {
        Object composite = null;
        if (bounds!=null) {
            composite = ElementGripFactory.getDefault().get(parentFile, bounds.getBegin().getOffset());
        }
        if (composite==null) {
            composite = parentFile;
        }
        return Lookups.fixed(composite, diff);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        diff.exclude(!enabled);
        newFileContent = null;
        super.setEnabled(enabled);
    }

    @Override
    public PositionBounds getPosition() {
        return bounds;
    }

    @Override
    public String getText() {
        return displayText;
    }

    @Override
    public void performChange() {
    }

    @Override
    public FileObject getParentFile() {
        if (diff.getKind() == Difference.Kind.CREATE) {
            return parentFile.getParent();
        }
        return parentFile;
    }
    
    @Override
    protected String getNewFileContent() {
        String result;
        if (newFileContent !=null) {
            result = newFileContent.get();
            if (result!=null) {
                return result;
            }
        }
        try {
            if (diff.getKind()==Difference.Kind.CREATE) {
                result = diff.getNewText();
            } else {
                result = modification.getResultingSource(parentFile);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        newFileContent = new WeakReference<String>(result);
        return result;
    }
    
    /**
     * Factory method for DiffElement
     * @param diff diff instance corresponding to thid Element
     * @param fileObject fileObject corresponding to this Element
     * @param modification 
     * @return ModificationResult corresponding to this change
     */
    public static DiffElement create(Difference diff, FileObject fileObject, ModificationResult modification) {
        Position start = diff.getStartPosition();
        Position end = diff.getEndPosition();
        PositionBounds bounds = null;
        if (diff.getKind() != Difference.Kind.CREATE) {
            // FIXME - unnecessary dependency on openide.text
            bounds = new PositionBounds((PositionRef)start, (PositionRef)end);
        }
        return new DiffElement(diff, bounds, fileObject, modification);
    }    
}
