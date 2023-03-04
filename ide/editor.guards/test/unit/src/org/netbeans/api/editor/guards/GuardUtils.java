/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
