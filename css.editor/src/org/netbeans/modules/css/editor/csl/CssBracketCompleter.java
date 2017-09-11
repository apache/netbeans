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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor.csl;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author marek.fukala@sun.com
 */
public class CssBracketCompleter implements KeystrokeHandler {

    @Override
    public OffsetRange findMatching(Document doc, int caretOffset) {
        //XXX returning null or the default should cause GSF to use the IDE default matcher
        return OffsetRange.NONE;
    }

    @Override
    public List<OffsetRange> findLogicalRanges(ParserResult info, int caretOffset) {
        ArrayList<OffsetRange> ranges = new ArrayList<>(2);

        Node root = ((CssParserResult) info).getParseTree();
        Snapshot snapshot = info.getSnapshot();

        if (root != null) {
            //find leaf at the position
            Node node = NodeUtil.findNodeAtOffset(root, snapshot.getEmbeddedOffset(caretOffset));
            if (node != null) {
                //go through the tree and add all parents with, eliminate duplicate nodes
                do {
                    //filter some unwanted node types
                    switch(node.type()) {
                        case declarations:
                        case token:
                            continue;
                    }
                    
                    //use trimmed node range so nodes containing whitespaces at the start
                    //or at the end of their range can be considered as duplicate nodes
                    //of their children.
                    int[] trimmedNodeRange = NodeUtil.getTrimmedNodeRange(node);
                    
                    int from = snapshot.getOriginalOffset(trimmedNodeRange[0]);
                    int to = snapshot.getOriginalOffset(trimmedNodeRange[1]);

                    if (from == -1 || to == -1) {
                        continue;
                    }

                    OffsetRange last = ranges.isEmpty() ? null : ranges.get(ranges.size() - 1);
                    //skip duplicated ranges
                    if (last == null || ((last.getEnd() - last.getStart()) < (to - from))) {
                        ranges.add(new OffsetRange(from, to));
                    }
                } while ((node = node.parent()) != null);
            }
        }


        return ranges;
    }

    @Override
    public boolean beforeCharInserted(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    @Override
    public boolean afterCharInserted(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    @Override
    public boolean charBackspaced(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    @Override
    public int beforeBreak(Document doc, int caretOffset, JTextComponent target) throws BadLocationException {
        return -1;
    }

    @Override
    public int getNextWordOffset(Document doc, int caretOffset, boolean reverse) {
        return -1;
    }

   
}
