/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.python.editor.refactoring;

import java.util.Collections;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position.Bias;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.python.source.PythonAstUtils;
import org.netbeans.modules.python.source.PythonParserResult;
import org.netbeans.modules.python.source.lexer.PythonLexerUtils;
import org.netbeans.modules.python.editor.refactoring.ui.ElementGripFactory;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * An element in the refactoring preview list which holds information about the find-usages-match
 * 
 */
public class WhereUsedElement extends SimpleRefactoringElementImplementation {
    private PositionBounds bounds;
    private String displayText;
    private FileObject parentFile;

    public WhereUsedElement(PositionBounds bounds, String displayText, FileObject parentFile, String name,
            OffsetRange range, Icon icon) {
        this.bounds = bounds;
        this.displayText = displayText;
        this.parentFile = parentFile;
        ElementGripFactory.getDefault().put(parentFile, name, range, icon);
    }

    @Override
    public String getDisplayText() {
        return displayText;
    }

    @Override
    public Lookup getLookup() {
        Object composite =
                ElementGripFactory.getDefault().get(parentFile, bounds.getBegin().getOffset());

        if (composite == null) {
            composite = parentFile;
        }

        return Lookups.singleton(composite);
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
        return parentFile;
    }

    public static WhereUsedElement create(PythonElementCtx tree) {
        PythonParserResult info = tree.getInfo();
        OffsetRange range = PythonAstUtils.getNameRange(info, tree.getNode());
        assert range != OffsetRange.NONE;

        range = PythonLexerUtils.getLexerOffsets(info, range);
        assert range != OffsetRange.NONE : tree;

        Set<Modifier> modifiers = Collections.emptySet();
        if (tree.getElement() != null) {
            modifiers = tree.getElement().getModifiers();
        }
        Icon icon = UiUtils.getElementIcon(tree.getKind(), modifiers);
        return create(info, tree.getName(), range, icon);
    }

    public static WhereUsedElement create(PythonParserResult info, String name, OffsetRange range, Icon icon) {
        FileObject fo = info.getSnapshot().getSource().getFileObject();
        int start = range.getStart();
        int end = range.getEnd();

        int sta = start;
        int en = start; // ! Same line as start
        String content = null;

        BaseDocument bdoc = GsfUtilities.getDocument(info.getSnapshot().getSource().getFileObject(), false);
        try {
            bdoc.readLock();

            // I should be able to just call tree.getInfo().getText() to get cached
            // copy - but since I'm playing fast and loose with compilationinfos
            // for for example find subclasses (using a singly dummy FileInfo) I need
            // to read it here instead
            content = bdoc.getText(0, bdoc.getLength());
            sta = Utilities.getRowFirstNonWhite(bdoc, start);

            if (sta == -1) {
                sta = Utilities.getRowStart(bdoc, start);
            }

            en = Utilities.getRowLastNonWhite(bdoc, start);

            if (en == -1) {
                en = Utilities.getRowEnd(bdoc, start);
            } else {
                // Last nonwhite - left side of the last char, not inclusive
                en++;
            }

            // Sometimes the node we get from the AST is for the whole block
            // (e.g. such as the whole class), not the argument node. This happens
            // for example in Find Subclasses out of the index. In this case
            if (end > en) {
                end = start + name.length();

                if (end > bdoc.getLength()) {
                    end = bdoc.getLength();
                }
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            bdoc.readUnlock();
        }

        StringBuilder sb = new StringBuilder();
        if (start < sta) {
            System.out.println("Problem!");
            start = sta;
        }
        if (en < end) {
            System.out.println("Problem!");
            en = end;
        }
        final CharSequence subSequence = content.subSequence(sta, start);
        sb.append(PythonRefUtils.getHtml(subSequence.toString()));
        sb.append("<b>"); // NOI18N
        sb.append(content.subSequence(start, end));
        sb.append("</b>"); // NOI18N
        sb.append(PythonRefUtils.getHtml(content.subSequence(end, en).toString()));

        CloneableEditorSupport ces = PythonRefUtils.findCloneableEditorSupport(info);
        PositionRef ref1 = ces.createPositionRef(start, Bias.Forward);
        PositionRef ref2 = ces.createPositionRef(end, Bias.Forward);
        PositionBounds bounds = new PositionBounds(ref1, ref2);

        return new WhereUsedElement(bounds, sb.toString().trim(), fo, name,
                new OffsetRange(start, end), icon);
    }

    public static WhereUsedElement create(PythonParserResult info, String name, String html, OffsetRange range, Icon icon) {
        FileObject fo = info.getSnapshot().getSource().getFileObject();
        int start = range.getStart();
        int end = range.getEnd();

        CloneableEditorSupport ces = PythonRefUtils.findCloneableEditorSupport(info);
        PositionRef ref1 = ces.createPositionRef(start, Bias.Forward);
        PositionRef ref2 = ces.createPositionRef(end, Bias.Forward);
        PositionBounds bounds = new PositionBounds(ref1, ref2);

        return new WhereUsedElement(bounds, html, fo, name,
                new OffsetRange(start, end), icon);
    }
}
