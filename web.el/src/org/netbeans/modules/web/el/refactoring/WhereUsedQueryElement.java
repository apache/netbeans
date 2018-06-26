/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
