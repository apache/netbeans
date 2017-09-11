/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.hints.other;

import java.util.Collections;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.CloseTag;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.util.NbBundle;

/**
 *
 * @author mfukala@netbeans.org
 */
@NbBundle.Messages("MSG_RemoveSurroundingTag=Remove Surrounding Tag")
public class RemoveSurroundingTag extends Hint {

    public static final Rule RULE = new RemoveSurroundingTagRule();

    public RemoveSurroundingTag(RuleContext context, OffsetRange range) {
        super(RULE,
                Bundle.MSG_RemoveSurroundingTag(),
                context.parserResult.getSnapshot().getSource().getFileObject(),
                range,
                Collections.<HintFix>singletonList(new SurroundWithTagHintFix(context)),
                10);
    }

    private static class SurroundWithTagHintFix implements HintFix {

        RuleContext context;

        public SurroundWithTagHintFix(RuleContext context) {
            this.context = context;
        }

        @Override
        public String getDescription() {
            return Bundle.MSG_RemoveSurroundingTag();
        }

        @Override
        public void implement() throws Exception {

            context.doc.runAtomic(new Runnable() {

                @Override
                public void run() {
                    try {
                        Element[] surroundingPair = findPairNodesAtSelection(context);
                        if (surroundingPair == null) {
                            return;
                        }
                        Snapshot s = context.parserResult.getSnapshot();
                        
                        int otfrom = s.getOriginalOffset(surroundingPair[0].from());
                        int otto = s.getOriginalOffset(surroundingPair[0].to());
                        int otlen = otto - otfrom;

                        int ctfrom = s.getOriginalOffset(surroundingPair[1].from());
                        int ctto = s.getOriginalOffset(surroundingPair[1].to());
                        
                        if(otfrom == -1 || otto == -1 || ctfrom == -1 || ctto == -1) {
                            return ;
                        }

                        context.doc.remove(otfrom, otlen);
                        context.doc.remove(ctfrom - otlen, ctto - ctfrom);

                    } catch (BadLocationException ex) {
                        //ignore
                    }

                }
            });
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }
    }

    private static class RemoveSurroundingTagRule implements Rule {

        @Override
        public boolean appliesTo(RuleContext context) {
            return findPairNodesAtSelection(context) != null;
        }

        @Override
        public String getDisplayName() {
            return Bundle.MSG_RemoveSurroundingTag();
        }

        @Override
        public boolean showInTasklist() {
            return false;
        }

        @Override
        public HintSeverity getDefaultSeverity() {
            return HintSeverity.INFO;
        }
    }

    private static Element[] findPairNodesAtSelection(RuleContext context) {
        HtmlParserResult result = (HtmlParserResult) context.parserResult;
        if (context.selectionStart == -1 || context.selectionEnd == -1) {
            //no selection - find the containing element
            Snapshot snap = result.getSnapshot();
            int embeddedCaret = snap.getEmbeddedOffset(context.caretOffset);
            if (embeddedCaret != -1) {
                Node containing = result.findBySemanticRange(embeddedCaret, false);
                if (containing != null) {
                    if (containing.type() == ElementType.OPEN_TAG) {
                        OpenTag ot = (OpenTag) containing;
                        CloseTag ct = ot.matchingCloseTag();
                        if (ct != null) {
                            return new Element[]{ot, ct};
                        }
                    }
                }
            }
        } else {
            //selection - find the element inside selection

            //check whether the selection starts at a tag and ends at a tag
            //open tag
            Element open = result.findByPhysicalRange(context.selectionStart, true);
            if (open == null || open.type() != ElementType.OPEN_TAG) {
                return null;
            }

            //close tag
            Element close = result.findByPhysicalRange(context.selectionEnd, false);
            if (close == null || close.type() != ElementType.CLOSE_TAG) {
                return null;
            }

            //is the end tag really a pair node of the open tag?
            OpenTag openTag = (OpenTag) open;
            if (openTag.matchingCloseTag() != close) { //same AST ... reference test is ok
                return null;
            }

            return new Element[]{open, close};

        }

        return null;
    }

}
