/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.refactoring.findusages;

import javax.swing.text.Position;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.refactoring.findusages.model.RefactoringElement;
import org.netbeans.modules.groovy.refactoring.utils.GroovyProjectUtil;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.Line;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Lookup;

/**
 *
 * @author Martin Janicek
 */
public class FindUsagesElement extends SimpleRefactoringElementImplementation implements Comparable<FindUsagesElement> {

    private final RefactoringElement usageElement;
    private final BaseDocument doc;
    private final Line line;
    private final int lineNumber;


    public FindUsagesElement(RefactoringElement element, BaseDocument doc) {
        this.usageElement = element;
        this.doc = doc;
        this.line = GroovyProjectUtil.getLine(element.getFileObject(), element.getNode().getLineNumber() - 1);
        this.lineNumber = line.getLineNumber();
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String getText() {
        return usageElement.getName() + " -";
    }

    @Override
    public String getDisplayText() {
        return FindUsagesPainter.colorASTNode(usageElement.getNode(), line);
    }

    public String getName() {
        return usageElement.getName();
    }

    @Override
    public void performChange() {
    }

    @Override
    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    @Override
    public FileObject getParentFile() {
        return usageElement.getFileObject();
    }

    @Override
    public PositionBounds getPosition() {
        OffsetRange range = ASTUtils.getRange(usageElement.getNode(), doc);
        if (range == OffsetRange.NONE) {
            return null;
        }

        CloneableEditorSupport ces = GroovyProjectUtil.findCloneableEditorSupport(usageElement.getFileObject());
        PositionRef ref1 = ces.createPositionRef(range.getStart(), Position.Bias.Forward);
        PositionRef ref2 = ces.createPositionRef(range.getEnd(), Position.Bias.Forward);
        return new PositionBounds(ref1, ref2);
    }

    @Override
    public int compareTo(FindUsagesElement comparedElement) {
        return this.lineNumber - comparedElement.lineNumber;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.usageElement != null ? this.usageElement.hashCode() : 0);
        hash = 29 * hash + (this.doc != null ? this.doc.hashCode() : 0);
        hash = 29 * hash + (this.line != null ? this.line.hashCode() : 0);
        hash = 29 * hash + this.lineNumber;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FindUsagesElement other = (FindUsagesElement) obj;
        if (this.usageElement != other.usageElement && (this.usageElement == null || !this.usageElement.equals(other.usageElement))) {
            return false;
        }
        if (this.doc != other.doc && (this.doc == null || !this.doc.equals(other.doc))) {
            return false;
        }
        if (this.line != other.line && (this.line == null || !this.line.equals(other.line))) {
            return false;
        }
        if (this.lineNumber != other.lineNumber) {
            return false;
        }
        return true;
    }
}