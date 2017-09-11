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
package org.netbeans.api.editor.guards;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import junit.framework.TestCase;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.modules.editor.guards.GuardedSectionsImpl;
import org.netbeans.modules.editor.guards.GuardsAccessor;
import org.netbeans.modules.editor.guards.PositionBounds;
import org.openide.text.NbDocument;

/**
 *
 * @author Jan Pokorsky
 */
public final class GuardUtils {
    
    
    public static void verifyPositions(TestCase test, GuardedSection gs, int start, int end) {
        test.assertEquals("start position", start, gs.getStartPosition().getOffset());
        test.assertEquals("end position", end, gs.getEndPosition().getOffset());
    }
    
    public static void verifyPositions(TestCase test, InteriorSection gs, int headerStart, int headerEnd, int bodyStart, int bodyEnd, int footerStart, int footerEnd) {
        test.assertEquals("header begin position", headerStart, gs.getImpl().getHeaderBounds().getBegin().getOffset());
        test.assertEquals("header end position", headerEnd, gs.getImpl().getHeaderBounds().getEnd().getOffset());
        test.assertEquals("body begin position", bodyStart, gs.getBodyStartPosition().getOffset());
        test.assertEquals("body end position", bodyEnd, gs.getBodyEndPosition().getOffset());
        test.assertEquals("footer begin position", footerStart, gs.getImpl().getFooterBounds().getBegin().getOffset());
        test.assertEquals("footer end position", footerEnd, gs.getImpl().getFooterBounds().getEnd().getOffset());
    }
    
    public static void verifyGuardAttr(TestCase test, StyledDocument doc, SimpleSection gs) {
        verifyGuardAttr(test, (GuardedDocument) doc, gs.getStartPosition().getOffset(), gs.getEndPosition().getOffset());
    }
    
    public static void verifyGuardAttr(TestCase test, StyledDocument doc, InteriorSection gs) {
        PositionBounds bounds = gs.getImpl().getHeaderBounds();
        verifyGuardAttr(test, (GuardedDocument) doc, bounds.getBegin().getOffset(), bounds.getEnd().getOffset());
        bounds = gs.getImpl().getFooterBounds();
        verifyGuardAttr(test, (GuardedDocument) doc, bounds.getBegin().getOffset(), bounds.getEnd().getOffset());
        
    }
    
    public static void verifyGuardAttr(TestCase test, GuardedDocument doc, int startPosition, int endPosition) {
        for (int i = startPosition; i <= endPosition; i++) {
            test.assertTrue("element should be guarded; pos: " + i, doc.isPosGuarded(i));
        }
    }
    
    public static boolean isGuarded(StyledDocument doc, int offset) {
        GuardedDocument gdoc = (GuardedDocument) doc;
        return gdoc.isPosGuarded(offset);
    }
    
    public static void dumpGuardedAttr(StyledDocument doc) {
        System.out.println("" + ((GuardedDocument) doc).toStringDetail());
//        int start = 0;
//        int end = doc.getLength();
//        Element el = null;
//        System.out.println("Document.guards: " +
//                ", s: " + start + ", e: " + end);
//        do {
//            el = doc.getCharacterElement(start);
//            System.out.println("s: " + el.getStartOffset() + ", e: " + el.getEndOffset() +
//                    ", g: " + Boolean.TRUE.equals(el.getAttributes().getAttribute(NbDocument.GUARDED)));
//            start = el.getEndOffset() + 1;
//        } while (end > el.getEndOffset());
//        System.out.println("-------");
    }
    
    public static void dumpDocument(StyledDocument doc) throws BadLocationException {
        char[] cs = doc.getText(0, doc.getLength()).toCharArray();
        StringBuilder sb  = new StringBuilder();
        sb.append("0:");
        for (int i = 0; i < cs.length; i++) {
            char c = cs[i];
            if (c == ' ')
                c = '.';
            else if (c == '\n') {
                sb.append('$').append(c).append((i + 1) + ":");
                continue;
            }
            sb.append(c);
        }
        
        System.out.println("Document: "+ doc.getLength() + "\n" + sb.toString());
    }
    
    public static void print(SimpleSection gs) {
        System.out.println(
                "SimplS: " + gs.getName() +
                ", s: " + gs.getStartPosition().getOffset() +
                ", e: " + gs.getEndPosition().getOffset() +
                ", v: " + gs.isValid() +
                ", t: '" + gs.getText() + "'");
    }
    
    public static void print(InteriorSection gs) {
        System.out.println(
                "InterS: " + gs.getName() +
                ", hs: " + gs.getImpl().getHeaderBounds().getBegin().getOffset() +
                ", he: " + gs.getImpl().getHeaderBounds().getEnd().getOffset() +
                ", bs: " + gs.getImpl().getBodyBounds().getBegin().getOffset() +
                ", be: " + gs.getImpl().getBodyBounds().getEnd().getOffset() +
                ", fs: " + gs.getImpl().getFooterBounds().getBegin().getOffset() +
                ", fe: " + gs.getImpl().getFooterBounds().getEnd().getOffset() +
                ", v: " + gs.isValid() +
                ", t: '" + gs.getText() + "'");
    }
    
    static String toString(Element el) {
        return el.toString() + "{el s:" + el.getStartOffset() + ", e:" + el.getEndOffset() + "}";
    }
    
    public static void initManager(Editor editor) {
        initManager(editor, null);
    }
    
    public static void initManager(Editor editor, GuardedSectionsImpl gimpl) {
        if (gimpl == null) {
            gimpl = new GuardedSectionsImpl(editor);
        }
        GuardedSectionManager api = GuardsAccessor.DEFAULT.createGuardedSections(gimpl);
        editor.doc.putProperty(GuardedSectionManager.class, api);
    }
}
