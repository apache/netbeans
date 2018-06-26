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
package org.netbeans.modules.php.editor.parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.parser.astnodes.GroupUseStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeNode;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatement;
import org.netbeans.modules.php.editor.parser.astnodes.SingleUseStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class UnusedUsesCollector extends DefaultVisitor {
    private static final String NAMESPACE_SEPARATOR = "\\"; //NOI18N
    private final PHPParseResult parserResult;
    private final Map<String, UnusedOffsetRanges> unusedUsesOffsetRanges;

    public UnusedUsesCollector(PHPParseResult parserResult) {
        assert parserResult != null;
        this.parserResult = parserResult;
        unusedUsesOffsetRanges = new HashMap<>();
    }

    public Collection<UnusedOffsetRanges> collect() {
        Program program = parserResult.getProgram();
        if (program != null) {
            program.accept(this);
        }
        return unusedUsesOffsetRanges.values();
    }

    @Override
    public void visit(Program program) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        scan(program.getStatements());
        scan(program.getComments());
    }

    @Override
    public void visit(PHPDocTypeNode node) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        QualifiedName typeName = QualifiedName.create(node.getValue());
        if (unusedUsesOffsetRanges.size() > 0 && !typeName.getKind().isFullyQualified()) {
            String firstSegmentName = typeName.getSegments().getFirst();
            processFirstSegmentName(firstSegmentName);
        }
    }

    @Override
    public void visit(NamespaceName node) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        if (unusedUsesOffsetRanges.size() > 0 && !node.isGlobal()) {
            Identifier firstSegment = node.getSegments().get(0);
            String firstSegmentName = firstSegment.getName();
            processFirstSegmentName(firstSegmentName);
        }
    }

    private void processFirstSegmentName(final String firstSegmentName) {
        Set<String> namesToRemove = new HashSet<>();
        for (String name : unusedUsesOffsetRanges.keySet()) {
            QualifiedName qualifiedUseName = QualifiedName.create(name);
            if (qualifiedUseName.getSegments().getLast().equals(firstSegmentName)) {
                namesToRemove.add(name);
            }
        }
        for (String nameToRemove : namesToRemove) {
            unusedUsesOffsetRanges.remove(nameToRemove);
        }
    }

    @Override
    public void visit(UseStatement node) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        List<UseStatementPart> parts = node.getParts();
        if (parts.size() == 1
                && parts.get(0) instanceof SingleUseStatementPart) {
            String correctName = getCorrectName((SingleUseStatementPart) parts.get(0));
            OffsetRange offsetRange = new OffsetRange(node.getStartOffset(), node.getEndOffset());
            unusedUsesOffsetRanges.put(correctName, new UnusedOffsetRanges(offsetRange, offsetRange));
        } else {
            processUseStatementsParts(parts);
        }
    }

    private String getCorrectName(SingleUseStatementPart useStatementPart) {
        Identifier alias = useStatementPart.getAlias();
        String identifierName;
        if (alias != null) {
            identifierName = alias.getName();
        } else {
            NamespaceName name = useStatementPart.getName();
            identifierName = CodeUtils.extractQualifiedName(name);
            if (name.isGlobal()) {
                identifierName = NAMESPACE_SEPARATOR + identifierName;
            }
        }
        return identifierName;
    }

    // XXX endOffset should be start offset of the next UseStatementPart
    private void processUseStatementsParts(final List<UseStatementPart> parts) {
        int lastStartOffset = -1;
        int partsSize = parts.size();
        for (int i = 0; i < partsSize; i++) {
            UseStatementPart useStatementPart = parts.get(i);
            int endOffset;
            if (useStatementPart instanceof SingleUseStatementPart) {
                SingleUseStatementPart singleUseStatementPart = (SingleUseStatementPart) useStatementPart;
                if (lastStartOffset == -1) {
                    lastStartOffset = singleUseStatementPart.getStartOffset();
                }
                // XXX
//            if (i == 0) {
//                lastStartOffset = useStatementPart.getStartOffset();
//                assert i + 1 < parts.size();
//                SingleUseStatementPart nextPart = parts.get(i + 1);
//                endOffset = nextPart.getStartOffset();
//            }
                endOffset = singleUseStatementPart.getEndOffset();
                processSingleUseStatementPart(singleUseStatementPart, lastStartOffset, endOffset);
                lastStartOffset = singleUseStatementPart.getEndOffset();
            } else if (useStatementPart instanceof GroupUseStatementPart) {
                GroupUseStatementPart groupUseStatementPart = (GroupUseStatementPart) useStatementPart;
                List<SingleUseStatementPart> items = groupUseStatementPart.getItems();
                if (items.isEmpty()) {
                    continue;
                }
                if (lastStartOffset == -1) {
                    lastStartOffset = items.get(0).getStartOffset();
                }
                for (SingleUseStatementPart item : items) {
                    endOffset = item.getEndOffset();
                    processSingleUseStatementPart(item, lastStartOffset, endOffset);
                    lastStartOffset = item.getEndOffset();
                }
            } else {
                assert false : "Unexpected class type: " + useStatementPart.getClass().getName(); // NOI18N
            }
        }
    }

    private void processSingleUseStatementPart(SingleUseStatementPart singleUseStatementPart, int replaceStartOffset, int replaceEndOffset) {
        String correctName = getCorrectName(singleUseStatementPart);
        OffsetRange rangeToVisualise = new OffsetRange(singleUseStatementPart.getStartOffset(), singleUseStatementPart.getEndOffset());
        OffsetRange rangeToReplace = new OffsetRange(replaceStartOffset, replaceEndOffset);
        unusedUsesOffsetRanges.put(correctName, new UnusedOffsetRanges(rangeToVisualise, rangeToReplace));
    }

    public static final class UnusedOffsetRanges {
        private final OffsetRange rangeToVisualise;
        private final OffsetRange rangeToReplace;

        private UnusedOffsetRanges(final OffsetRange rangeToVisualise, final OffsetRange rangeToReplace) {
            this.rangeToVisualise = rangeToVisualise;
            this.rangeToReplace = rangeToReplace;
        }

        public OffsetRange getRangeToVisualise() {
            return rangeToVisualise;
        }

        public OffsetRange getRangeToReplace() {
            return rangeToReplace;
        }

    }

}
