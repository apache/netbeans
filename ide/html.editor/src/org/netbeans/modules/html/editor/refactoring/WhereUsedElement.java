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
package org.netbeans.modules.html.editor.refactoring;


import java.util.Collections;
import javax.swing.Icon;
import javax.swing.text.Position.Bias;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.html.editor.indexing.Entry;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * An element in the refactoring preview list which holds information about the find-usages-match
 *
 * <p>Copy of {@link org.netbeans.modules.css.refactoring.WhereUsedElement}
 * to not add more API in CSS module or add circular dependency</p>
 *
 * @author Tor Norbye
 * @author mfukala@netbeans.org
 */

public class WhereUsedElement extends SimpleRefactoringElementImplementation {
    private final PositionBounds bounds;
    private final String displayText;
    private final FileObject parentFile;

    public WhereUsedElement(PositionBounds bounds, String displayText, FileObject parentFile, String name,
        OffsetRange range, Icon icon) {
        this.bounds = bounds;
        this.displayText = displayText;
        this.parentFile = parentFile;
    }

    @Override
    public String getDisplayText() {
        return displayText;
    }

    @Override
    public Lookup getLookup() {
        Object composite = null;

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

    public static WhereUsedElement create(FileObject fileObject, Entry entry, ElementKind kind) {
        return create(fileObject, entry, kind, true);
    }

    public static WhereUsedElement create(FileObject fileObject, Entry entry, ElementKind kind, boolean related) {
        Icon icon = UiUtils.getElementIcon(kind, Collections.<Modifier>emptyList());
        String name = entry.getName();
        OffsetRange range = entry.getDocumentRange();

        int start = range.getStart();
        int end = range.getEnd();
        
        int sta = start;
        int en = start; // ! Same line as start
        String content = null;
        
        BaseDocument bdoc = GsfUtilities.getDocument(fileObject, true);
        try {
            bdoc.readLock();

            // I should be able to just call tree.getInfo().getText() to get cached
            // copy - but since I'm playing fast and loose with compilationinfos
            // for for example find subclasses (using a singly dummy FileInfo) I need
            // to read it here instead
            content = bdoc.getText(0, bdoc.getLength());
            sta = Utilities.getRowFirstNonWhite(bdoc, start);

            if (sta == -1) {
                sta = LineDocumentUtils.getLineStartOffset(bdoc, start);
            }

            en = Utilities.getRowLastNonWhite(bdoc, start);

            if (en == -1) {
                en = LineDocumentUtils.getLineEndOffset(bdoc, start);
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
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            bdoc.readUnlock();
        }

        StringBuilder sb = new StringBuilder();
        if (end < sta) {
            // XXX Shouldn't happen, but I still have AST offset errors
            sta = end;
        }
        if (start < sta) {
            // XXX Shouldn't happen, but I still have AST offset errors
            start = sta;
        }
        if (en < end) {
            // XXX Shouldn't happen, but I still have AST offset errors
            en = end;
        }
        sb.append(encodeCharRefs(content.subSequence(sta, start).toString()));
        sb.append("<b>"); // NOI18N
        sb.append(content.subSequence(start, end));
        sb.append("</b>"); // NOI18N
        sb.append(encodeCharRefs(content.subSequence(end, en).toString()));
        if(!related) {
            sb.append(NbBundle.getMessage(WhereUsedElement.class, "MSG_Unrelated_Where_Used_Occurance")); //NOI18N
        }


        CloneableEditorSupport ces = GsfUtilities.findCloneableEditorSupport(fileObject);
        PositionRef ref1 = ces.createPositionRef(start, Bias.Forward);
        PositionRef ref2 = ces.createPositionRef(end, Bias.Forward);
        PositionBounds bounds = new PositionBounds(ref1, ref2);

        return new WhereUsedElement(bounds, sb.toString().trim(), 
                fileObject, name, new OffsetRange(start, end), icon);
    }

    private static final String LT = "&lt;";// < //NOI18N
    private static final String GT = "&gt;";// > //NOI18N
    //encode some character references
    private static String encodeCharRefs(String htmlCode) {
        StringBuilder buf = new StringBuilder();
        for(int i = 0; i < htmlCode.length(); i++) {
            char c = htmlCode.charAt(i);
            String repl;
            switch (c) {
                case '<' :
                    repl = LT;
                    break;
                case '>' :
                    repl = GT;
                    break;
                default:
                    repl = null;
            }
            buf.append(repl == null ? c : repl);
        }
        return buf.toString();
    }

    public static WhereUsedElement create(CloneableEditorSupport ces, FileObject fo, String name, String html, OffsetRange range, Icon icon) {
        int start = range.getStart();
        int end = range.getEnd();

        PositionRef ref1 = ces.createPositionRef(start, Bias.Forward);
        PositionRef ref2 = ces.createPositionRef(end, Bias.Forward);
        PositionBounds bounds = new PositionBounds(ref1, ref2);

        return new WhereUsedElement(bounds, html,
               fo, name, new OffsetRange(start, end), icon);
    }
}
