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

package org.netbeans.modules.ant.grammar;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DefaultStyledDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

public class AntHighlightsContainerTest extends NbTestCase {

    public AntHighlightsContainerTest(String name) {
        super(name);
    }

    public void testHighlighting() throws Exception {
        assertEquals("<echo>whatever</echo>", highlight("<echo>whatever</echo>"));
        assertEquals("<echo>(${prop})</echo>", highlight("<echo>${prop}</echo>"));
        assertEquals("<echo>(${prop})</echo>", highlight("<ech|o>${prop}</e|cho>"));
        assertEquals("<echo>(${pr)(op})</echo>", highlight("<echo>${pr|op}</echo>"));
        assertEquals("<echo>(${prop})</echo>", highlight("<echo>|${prop}</echo>"));
        assertEquals("<echo>(${prop})</echo>", highlight("<echo>${prop}|</echo>"));
        assertEquals("<echo>(${prop})</echo>", highlight("<echo>|${prop}|</echo>"));
        assertEquals("<echo>$${foo}=(${foo})</echo>", highlight("<echo>$${foo}=${foo}</echo>"));
        assertEquals("<echo>(@{prop})</echo>", highlight("<echo>@{prop}</echo>"));
        assertEquals("<echo>${(@{prop})}</echo>", highlight("<echo>${@{prop}}</echo>"));
    }
    
    /**
     * @param text document which may be split at {@code |} to indicate where segments are split
     * @return same document with highlighted parts surrounded by {@code (...)} (not merged at split points)
     */
    private static String highlight(String text) throws Exception {
        List<Integer> splits = new ArrayList<Integer>();
        AbstractDocument doc = new DefaultStyledDocument();
        int lastSplit = 0;
        for (String piece : text.split("[|]")) {
            doc.insertString(doc.getLength(), piece, null);
            lastSplit += piece.length();
            splits.add(lastSplit);
        }
        HighlightsContainer hc = new AntHighlightsContainer(doc, null);
        int pos = 0;
        StringBuilder b = new StringBuilder();
        for (int split : splits) {
            // XXX can also test passing Integer.MAX_VALUE as endOffset when split == doc.length
            HighlightsSequence hs = hc.getHighlights(pos, split);
            while (hs.moveNext()) {
//                System.err.println("pos=" + pos + " split=" + split + " startOffset=" + hs.getStartOffset() + " endOffset=" + hs.getEndOffset());
                b.append(doc.getText(pos, hs.getStartOffset() - pos));
                pos = hs.getEndOffset();
                b.append('(');
                b.append(doc.getText(hs.getStartOffset(), pos - hs.getStartOffset()));
                b.append(')');
            }
            b.append(doc.getText(pos, split - pos));
            pos = split;
        }
        return b.toString();
    }

}
