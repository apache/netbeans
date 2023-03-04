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