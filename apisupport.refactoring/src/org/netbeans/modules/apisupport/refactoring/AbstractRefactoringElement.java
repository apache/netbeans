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

package org.netbeans.modules.apisupport.refactoring;

import javax.swing.text.Position;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 *
 * @author Milos Kleint
 */
public abstract class AbstractRefactoringElement extends SimpleRefactoringElementImplementation implements RefactoringElementImplementation {
    
    private int status = RefactoringElementImplementation.NORMAL;

    protected String name;
    protected final FileObject parentFile;
    protected boolean enabled = true;

    protected AbstractRefactoringElement(@NonNull FileObject parentFile) {
        Parameters.notNull("parentFile", parentFile);
        this.parentFile = parentFile;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getText() {
        return getDisplayText();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public FileObject getParentFile() {
        return parentFile;
    }
    
    /** start and end positions of text (must be 2-element array); default [0, 0] */
    protected int[] location() {
        return new int[] {0, 0};
    }
    private int[] loc; // cached

    public PositionBounds getPosition() {
        try {
            DataObject dobj = DataObject.find(getParentFile());
            if (dobj != null) {
                EditorCookie.Observable obs = (EditorCookie.Observable)dobj.getCookie(EditorCookie.Observable.class);
                if (obs != null && obs instanceof CloneableEditorSupport) {
                    CloneableEditorSupport supp = (CloneableEditorSupport)obs;

                    if (loc == null) {
                        loc = location();
                    }
                PositionBounds bounds = new PositionBounds(
                        supp.createPositionRef(loc[0], Position.Bias.Forward),
                        supp.createPositionRef(Math.max(loc[0], loc[1]), Position.Bias.Forward)
                        );
                
                return bounds;
            }
            }
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void performChange() { }
    
    public void undoChange() { }
    
    public Lookup getLookup() {
        return Lookup.EMPTY;
    }
    
}
