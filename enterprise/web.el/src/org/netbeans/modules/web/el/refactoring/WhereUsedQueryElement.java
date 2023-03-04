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
package org.netbeans.modules.web.el.refactoring;

import com.sun.el.parser.Node;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.web.el.ELElement;
import org.netbeans.modules.web.el.refactoring.ELRefactoringPlugin.ParserResultHolder;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

final class WhereUsedQueryElement extends SimpleRefactoringElementImplementation {

    private final FileObject file;
    private final String reference;
    private final ELElement eLElement;
    private final ParserResultHolder parserResult;
    private final Node targetNode;

    public WhereUsedQueryElement(FileObject file, String reference, ELElement eLElement, Node targetNode, ParserResultHolder parserResult) {
        this.file = file;
        this.reference = reference;
        this.eLElement = eLElement;
        this.parserResult = parserResult;
        this.targetNode = targetNode;
    }

    @Override
    public String getText() {
        return reference;
    }

    @Override
    public String getDisplayText() {
        try {
            CharSequence text = parserResult.topLevelSnapshot.getText();
            OffsetRange orig = eLElement.getOriginalOffset();
            int astLineStart = GsfUtilities.getRowStart(text, orig.getStart());
            int astLineEnd = GsfUtilities.getRowEnd(text, orig.getEnd());
            OffsetRange nodeOffset = new OffsetRange(
                    eLElement.getExpression().getOriginalOffset(targetNode.startOffset()), 
                    eLElement.getExpression().getOriginalOffset(targetNode.endOffset())); 
            int expressionStart = orig.getStart() - astLineStart;
            int expressionEnd = expressionStart + (orig.getEnd() - orig.getStart());
            OffsetRange expressionOffset = new OffsetRange(expressionStart, expressionEnd);
            CharSequence line = text.subSequence(astLineStart, astLineEnd);
            return RefactoringUtil.encodeAndHighlight(line.toString(), expressionOffset, nodeOffset).trim();
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return eLElement.getExpression().getOriginalExpression(); //show the original expression in the text
        }
    }

    @Override
    public void performChange() {
    }

    @Override
    public Lookup getLookup() {
        return Lookups.singleton(file);
    }

    @Override
    public FileObject getParentFile() {
        return file;
    }

    @Override
    public PositionBounds getPosition() {
        PositionRef[] position = RefactoringUtil.getPostionRefs(eLElement, targetNode);
        return new PositionBounds(position[0], position[1]);
    }
}
