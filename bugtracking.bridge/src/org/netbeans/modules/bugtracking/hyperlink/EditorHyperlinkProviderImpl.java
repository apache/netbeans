/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.hyperlink;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.bugtracking.api.Util;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * 
 * Provides hyperlink functionality on issue reference in code comments
 * 
 * @author Tomas Stupka
 */
public class EditorHyperlinkProviderImpl implements HyperlinkProviderExt {

    private static final Logger LOG = Logger.getLogger(EditorHyperlinkProviderImpl.class.getName());

    @Override
    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION); 
    }

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        return getIssueSpan(doc, offset, type) != null;
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        return getIssueSpan(doc, offset, type);
    }

    @Override
    public void performClickAction(final Document doc, final int offset, final HyperlinkType type) {
        final String issueId = getIssueId(doc, offset, type);
        if(issueId == null) {
            return;
        }

        class IssueDisplayer implements Runnable {
            @Override
            public void run() {
                DataObject dobj = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
                FileObject fileObject = null;
                if (dobj != null) {
                    fileObject = dobj.getPrimaryFile();
                }
                if(fileObject == null) {
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "EditorHyperlinkProviderImpl - no file found for given document");
                    return;
                }
                Util.openIssue(fileObject, issueId);
            }
        }
        RequestProcessor.getDefault().post(new IssueDisplayer());
    }

    @Override
    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        return NbBundle.getMessage(EditorHyperlinkProviderImpl.class, "LBL_OpenIssue", new Object[] { getIssueId(doc, offset, type) });
    }

    // XXX get/unify from/with hyperlink provider
    private String getIssueId(Document doc, int offset, HyperlinkType type) {
        int[] span = getIssueSpan(doc, offset, type);

        if (span == null) {
            return null;
        }

        String issueId = null;
        try {
            if ((span[0] <= offset) && (offset <= span[1])) {
                /* at first, check that it is a valid reference text: */
                int length = span[1] - span[0];
                String text = doc.getText(span[0], length);
                /* valid - now just retrieve the issue id: */
                issueId = Util.getIssueId(text);
                }
        } catch (BadLocationException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        if(issueId == null) {
            try {
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "No issue found for {0}", doc.getText(span[0], span[1] - span[0]));
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return issueId;
    }

    private int[] getIssueSpan(final Document doc, final int offset, final HyperlinkType type) {
        class C {
            CharSequence text;
            int offset;
            public C(CharSequence text, int offset) {
                this.text = text;
                this.offset = offset;
            }
        }
        final List<C> comments = new LinkedList<C>();
        doc.render(new Runnable() {
            @Override
            public void run() {
                TokenHierarchy th = TokenHierarchy.get(doc);
                List<TokenSequence> list = th.embeddedTokenSequences(offset, false);

                for (TokenSequence ts : list) {
                    if (ts == null) {
                        return;
                    }
                    ts.move(offset);
                    if (!ts.moveNext()) {
                        return;
                    }
                    Token t = ts.token();
                    TokenId tokenId;
                    String primCategory, name;

                    if (((tokenId = t.id()) == null)
                            || ((primCategory = tokenId.primaryCategory()) == null)
                            || ((name = tokenId.name()) == null)) {
                        continue;
                    }
                    if (primCategory.toUpperCase().contains("COMMENT") || // primaryCategory == commment should be more or less a convention // NOI18N
                        name.toUpperCase().contains("COMMENT"))           // consider this as a fallback // NOI18N
                    {
                        comments.add(new C(t.text(), ts.offset()));
                    }
                }
            }
        });
        int[][] ret = new int[1][];
        for (C c : comments) {
            int[] spans = Util.getIssueSpans(c.text.toString());
            for (int i = 1; i < spans.length; i += 2) {
                if (c.offset + spans[i - 1] <= offset && offset <= c.offset + spans[i]) {
                    ret[0] = new int[] {c.offset + spans[i - 1], c.offset + spans[i]};
                    break;
                }
            }
        }
        return ret[0];
    }

}
