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
package org.netbeans.modules.css.refactoring;


import java.util.Collections;
import javax.swing.Icon;
import javax.swing.text.Position.Bias;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.css.refactoring.api.Entry;
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
 * @author Tor Norbye
 * @author mfukala@netbeans.org
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
//        ElementGripFactory.getDefault().put(parentFile, name, range, icon);
    }

    @Override
    public String getDisplayText() {
        return displayText;
    }

    @Override
    public Lookup getLookup() {
        Object composite = null;
//            ElementGripFactory.getDefault().get(parentFile, bounds.getBegin().getOffset());

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
