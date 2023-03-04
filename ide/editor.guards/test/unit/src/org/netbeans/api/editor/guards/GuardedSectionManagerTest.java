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

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import junit.framework.TestCase;
import org.netbeans.api.editor.guards.Editor;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;

/**
 *
 * @author Jan Pokorsky
 */
public class GuardedSectionManagerTest extends TestCase {
    
    private Editor editor;
    private GuardedSectionManager guards;
    
    /** Creates a new instance of GuardedSectionManagerTest */
    public GuardedSectionManagerTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        editor = new Editor();
        
        GuardUtils.initManager(editor);
        
        guards = GuardedSectionManager.getInstance(editor.doc);
        assertNotNull("missing manager", guards);
    }
    
    protected void tearDown() throws Exception {
    }
    
    public void testSimpleSection() throws BadLocationException {
        System.out.println("-- testSimpleSection -------------");
        
//        System.out.println("...init document");
        editor.doc.insertString(0, "aaa", null);
//        GuardUtils.dumpDocument(editor.doc);
        
//        System.out.println("...create simple section on 0");
        Position ss1Start = editor.doc.createPosition(0);
        int ss1StartI = ss1Start.getOffset();
        SimpleSection ss1 = guards.createSimpleSection(ss1Start, "ss1");
        assertTrue("ss1.valid", ss1.isValid());
        assertEquals("ss1.name", "ss1", ss1.getName());
        
        Position ss1End = ss1.getEndPosition();
//        GuardUtils.print(ss1);
//        GuardUtils.dumpDocument(editor.doc);
//        GuardUtils.dumpGuardedAttr(editor.doc);
        // posinions verification
        GuardUtils.verifyPositions(this, ss1, ss1StartI + 1, ss1StartI + 2);
        GuardUtils.verifyGuardAttr(this, editor.doc, ss1);
        
        
        Position ss2Start = editor.doc.createPosition(ss1End.getOffset() + 1);
        int ss2StartI = ss2Start.getOffset();
//        System.out.println("...create simple section on " + ss2StartI);
        SimpleSection ss2 = guards.createSimpleSection(ss2Start, "ss2");
        assertTrue("ss2.valid", ss2.isValid());
        assertEquals("ss2.name", "ss2", ss2.getName());
        
//        GuardUtils.print(ss1);
//        GuardUtils.print(ss2);
//        GuardUtils.dumpDocument(editor.doc);
//        GuardUtils.dumpGuardedAttr(editor.doc);
        // posinions verification
        GuardUtils.verifyPositions(this, ss1, ss1StartI + 1, ss1StartI + 2);
        GuardUtils.verifyPositions(this, ss2, ss2StartI + 1, ss2StartI + 2);
        GuardUtils.verifyGuardAttr(this, editor.doc, ss1);
        GuardUtils.verifyGuardAttr(this, editor.doc, ss2);
        
        // test insertion to document beginning
//        System.out.println("...insert text to beginning");
        String txt = "X\n";
//        String txt = "text before sections\n";
        ss1StartI = ss1.getStartPosition().getOffset();
        int ss1EndI = ss1.getEndPosition().getOffset();
        ss2StartI = ss2.getStartPosition().getOffset();
        int ss2EndI = ss2.getEndPosition().getOffset();
        editor.doc.insertString(0, txt, null);
//        GuardUtils.print(ss1);
//        GuardUtils.print(ss2);
//        GuardUtils.dumpDocument(editor.doc);
        GuardUtils.verifyPositions(this, ss1, ss1StartI + txt.length(), ss1EndI + txt.length());
        GuardUtils.verifyPositions(this, ss2, ss2StartI + txt.length(), ss2EndI + txt.length());
        GuardUtils.verifyGuardAttr(this, editor.doc, ss1);
        GuardUtils.verifyGuardAttr(this, editor.doc, ss2);
        
        
        // test adding text to section
//        System.out.println("...set text to ss1");
        txt = "XX";
//        txt = "ss1 simple text\n";
        ss1StartI = ss1.getStartPosition().getOffset();
        ss1EndI = ss1.getEndPosition().getOffset();
        ss2StartI = ss2.getStartPosition().getOffset();
        ss2EndI = ss2.getEndPosition().getOffset();
        assertTrue(ss1EndI < ss2StartI);
        
        ss1.setText(txt);
        
//        GuardUtils.print(ss1);
//        GuardUtils.print(ss2);
//        GuardUtils.dumpDocument(editor.doc);
        assertEquals("ss1.setText", txt, ss1.getText());
        GuardUtils.verifyPositions(this, ss1, ss1StartI, ss1StartI + txt.length());
        GuardUtils.verifyPositions(this, ss2,
                ss1.getEndPosition().getOffset() + 2,
                ss1.getEndPosition().getOffset() + 2 + 1);
//        GuardUtils.dumpGuardedAttr(editor.doc);
        GuardUtils.verifyGuardAttr(this, editor.doc, ss1);
        GuardUtils.verifyGuardAttr(this, editor.doc, ss2);
        
//        System.out.println("...set text to ss2");
        ss2.setText("XX");
//        GuardUtils.print(ss1);
//        GuardUtils.print(ss2);
//        GuardUtils.dumpDocument(editor.doc);
        GuardUtils.verifyGuardAttr(this, editor.doc, ss1);
        GuardUtils.verifyGuardAttr(this, editor.doc, ss2);

//        System.out.println("...delete ss1");
        ss1.deleteSection();
//        GuardUtils.print(ss1);
//        GuardUtils.print(ss2);
//        GuardUtils.dumpDocument(editor.doc);
//        GuardUtils.dumpGuardedAttr(editor.doc);
        assertTrue("ss1.valid", !ss1.isValid());
        GuardUtils.verifyGuardAttr(this, editor.doc, ss2);
    }
    
    public void testDeleteSimpleSection() throws BadLocationException {
        System.out.println("-- testDeleteSimpleSection -------------");
        
        editor.doc.insertString(0, "aaa", null);
        SimpleSection ss = guards.createSimpleSection(editor.doc.createPosition(1), "ss1");
        
        assertEquals("ss1.content", "a\n \naa", editor.doc.getText(0, editor.doc.getLength()));
        ss.deleteSection();
        assertEquals("ss1.content2", "aaa", editor.doc.getText(0, editor.doc.getLength()));
        assertTrue("valid", !ss.isValid());
    }
    
    public void testRemoveSimpleSection() throws BadLocationException {
        System.out.println("-- testRemoveSimpleSection -------------");
        
        editor.doc.insertString(0, "aaa", null);
        SimpleSection ss = guards.createSimpleSection(editor.doc.createPosition(1), "ss1");
        
        assertEquals("ss1.content", "a\n \naa", editor.doc.getText(0, editor.doc.getLength()));
        assertTrue("'\\n'", !GuardUtils.isGuarded(editor.doc, 1));
        assertTrue("' '", GuardUtils.isGuarded(editor.doc, 2));
        assertTrue("'\\n'", GuardUtils.isGuarded(editor.doc, 3));
        assertTrue("'a'", !GuardUtils.isGuarded(editor.doc, 4));
        ss.removeSection();
        assertEquals("ss1.content2", "a\n \naa", editor.doc.getText(0, editor.doc.getLength()));
        assertTrue("valid", !ss.isValid());
        assertTrue("'\\n'", !GuardUtils.isGuarded(editor.doc, 1));
        assertTrue("' '", !GuardUtils.isGuarded(editor.doc, 2));
        assertTrue("'\\n'", !GuardUtils.isGuarded(editor.doc, 3));
        assertTrue("'a'", !GuardUtils.isGuarded(editor.doc, 4));
    }
    
    public void testDeleteInteriorSection() throws BadLocationException {
        System.out.println("-- testDeleteInteriorSection -------------");
        
        editor.doc.insertString(0, "aaa", null);
        InteriorSection is = guards.createInteriorSection(editor.doc.createPosition(1), "is1");
        
        assertEquals("is1.content", "a\n \n \n \naa", editor.doc.getText(0, editor.doc.getLength()));
        is.deleteSection();
        assertEquals("ss1.content2", "aaa", editor.doc.getText(0, editor.doc.getLength()));
        assertTrue("valid", !is.isValid());
    }
    
    public void testRemoveInteriorSection() throws BadLocationException {
        System.out.println("-- testRemoveInteriorSection -------------");
        
        editor.doc.insertString(0, "aaa", null);
        InteriorSection is = guards.createInteriorSection(editor.doc.createPosition(1), "is1");
        
        assertEquals("is1.content", "a\n \n \n \naa", editor.doc.getText(0, editor.doc.getLength()));
        assertTrue("'\\n'", !GuardUtils.isGuarded(editor.doc, 1));
        assertTrue("header.' '", GuardUtils.isGuarded(editor.doc, 2));
        assertTrue("header.'\\n'", GuardUtils.isGuarded(editor.doc, 3));
        assertTrue("body.' '", !GuardUtils.isGuarded(editor.doc, 4));
        assertTrue("body.'\\n'", !GuardUtils.isGuarded(editor.doc, 5));
        assertTrue("footer.' '", GuardUtils.isGuarded(editor.doc, 6));
        assertTrue("footer.'\\n'", GuardUtils.isGuarded(editor.doc, 7));
        assertTrue("'a'", !GuardUtils.isGuarded(editor.doc, 8));
        is.removeSection();
        assertEquals("is1.content2", "a\n \n \n \naa", editor.doc.getText(0, editor.doc.getLength()));
        assertTrue("'\\n'", !GuardUtils.isGuarded(editor.doc, 1));
        assertTrue("header.' '", !GuardUtils.isGuarded(editor.doc, 2));
        assertTrue("header.'\\n'", !GuardUtils.isGuarded(editor.doc, 3));
        assertTrue("body.' '", !GuardUtils.isGuarded(editor.doc, 4));
        assertTrue("body.'\\n'", !GuardUtils.isGuarded(editor.doc, 5));
        assertTrue("footer.' '", !GuardUtils.isGuarded(editor.doc, 6));
        assertTrue("footer.'\\n'", !GuardUtils.isGuarded(editor.doc, 7));
        assertTrue("'a'", !GuardUtils.isGuarded(editor.doc, 8));
    }
    
    public void testInteriorSection() throws BadLocationException {
        System.out.println("-- testInteriorSection -------------");
        
//        System.out.println("...init document");
        editor.doc.insertString(0, "aaa", null);
//        GuardUtils.dumpDocument(editor.doc);
        
//        System.out.println("...create interior section on 0");
        Position s1Start = editor.doc.createPosition(0);
        int s1StartI = s1Start.getOffset();
        InteriorSection s1 = guards.createInteriorSection(s1Start, "s1");
        assertTrue("s1.valid", s1.isValid());
        assertEquals("s1.name", "s1", s1.getName());
        
        Position s1End = s1.getEndPosition();
//        GuardUtils.print(s1);
//        GuardUtils.dumpDocument(editor.doc);
//        GuardUtils.dumpGuardedAttr(editor.doc);
        // posinions verification
        GuardUtils.verifyPositions(this, s1, s1StartI + 1, s1StartI + 6);
        GuardUtils.verifyPositions(this, s1, s1StartI + 1, s1StartI + 2, s1StartI + 3, s1StartI + 4, s1StartI + 5, s1StartI + 6);
        GuardUtils.verifyGuardAttr(this, editor.doc, s1);
        
        
        Position s2Start = editor.doc.createPosition(s1End.getOffset() + 1);
        int s2StartI = s2Start.getOffset();
//        System.out.println("...create interior section on " + s2StartI);
        InteriorSection s2 = guards.createInteriorSection(s2Start, "s2");
        assertTrue("s2.valid", s2.isValid());
        assertEquals("s2.name", "s2", s2.getName());
        
//        GuardUtils.print(s1);
//        GuardUtils.print(s2);
//        GuardUtils.dumpDocument(editor.doc);
        // posinions verification
        GuardUtils.verifyPositions(this, s1, s1StartI + 1, s1StartI + 6);
        GuardUtils.verifyPositions(this, s1, s1StartI + 1, s1StartI + 2, s1StartI + 3, s1StartI + 4, s1StartI + 5, s1StartI + 6);
        GuardUtils.verifyPositions(this, s2, s2StartI + 1, s2StartI + 6);
        GuardUtils.verifyPositions(this, s2, s2StartI + 1, s2StartI + 2, s2StartI + 3, s2StartI + 4, s2StartI + 5, s2StartI + 6);
        GuardUtils.verifyGuardAttr(this, editor.doc, s1);
        GuardUtils.verifyGuardAttr(this, editor.doc, s2);
        
        // test insertion to document beginning
//        System.out.println("...insert text to beginning");
        String txt = "text before sections\n";
        int s1HeaderBegin = s1.getImpl().getHeaderBounds().getBegin().getOffset();
        int s1HeaderEnd = s1.getImpl().getHeaderBounds().getEnd().getOffset();
        int s1BodyBegin = s1.getImpl().getBodyBounds().getBegin().getOffset();
        int s1BodyEnd = s1.getImpl().getBodyBounds().getEnd().getOffset();
        int s1FooterBegin = s1.getImpl().getFooterBounds().getBegin().getOffset();
        int s1FooterEnd = s1.getImpl().getFooterBounds().getEnd().getOffset();
        int s2HeaderBegin = s2.getImpl().getHeaderBounds().getBegin().getOffset();
        int s2HeaderEnd = s2.getImpl().getHeaderBounds().getEnd().getOffset();
        int s2BodyBegin = s2.getImpl().getBodyBounds().getBegin().getOffset();
        int s2BodyEnd = s2.getImpl().getBodyBounds().getEnd().getOffset();
        int s2FooterBegin = s2.getImpl().getFooterBounds().getBegin().getOffset();
        int s2FooterEnd = s2.getImpl().getFooterBounds().getEnd().getOffset();
        s1StartI = s1.getStartPosition().getOffset();
        int s1EndI = s1.getEndPosition().getOffset();
        
        editor.doc.insertString(0, txt, null);
//        GuardUtils.print(s1);
//        GuardUtils.print(s2);
//        GuardUtils.dumpDocument(editor.doc);
        GuardUtils.verifyPositions(this, s1, s1StartI + txt.length(), s1EndI + txt.length());
        GuardUtils.verifyPositions(this, s1,
                s1HeaderBegin + txt.length(), s1HeaderEnd + txt.length(),
                s1BodyBegin + txt.length(), s1BodyEnd + txt.length(),
                s1FooterBegin + txt.length(), s1FooterEnd + txt.length()
                );
        GuardUtils.verifyPositions(this, s2,
                s2HeaderBegin + txt.length(), s2HeaderEnd + txt.length(),
                s2BodyBegin + txt.length(), s2BodyEnd + txt.length(),
                s2FooterBegin + txt.length(), s2FooterEnd + txt.length()
                );
        GuardUtils.verifyGuardAttr(this, editor.doc, s1);
        GuardUtils.verifyGuardAttr(this, editor.doc, s2);
        
        
        // test adding content to section
        s1HeaderBegin = s1.getImpl().getHeaderBounds().getBegin().getOffset();
//        GuardUtils.dumpGuardedAttr(editor.doc);
        assertTrue("is not guarded", GuardUtils.isGuarded(editor.doc, s1HeaderBegin));
        s1HeaderEnd = s1.getImpl().getHeaderBounds().getEnd().getOffset();
        s1BodyBegin = s1.getImpl().getBodyBounds().getBegin().getOffset();
        s1BodyEnd = s1.getImpl().getBodyBounds().getEnd().getOffset();
        s1FooterBegin = s1.getImpl().getFooterBounds().getBegin().getOffset();
        s1FooterEnd = s1.getImpl().getFooterBounds().getEnd().getOffset();
        s2HeaderBegin = s2.getImpl().getHeaderBounds().getBegin().getOffset();
        s2HeaderEnd = s2.getImpl().getHeaderBounds().getEnd().getOffset();
        s2BodyBegin = s2.getImpl().getBodyBounds().getBegin().getOffset();
        s2BodyEnd = s2.getImpl().getBodyBounds().getEnd().getOffset();
        s2FooterBegin = s2.getImpl().getFooterBounds().getBegin().getOffset();
        s2FooterEnd = s2.getImpl().getFooterBounds().getEnd().getOffset();
        String s1Header = "HEADER";
        String s1Body = "BODY";
        String s1Footer = "FOOTER";
        s1.setHeader(s1Header);
        s1.setBody(s1Body);
        s1.setFooter(s1Footer);
//        GuardUtils.print(s1);
//        GuardUtils.print(s2);
//        GuardUtils.dumpDocument(editor.doc);
        GuardUtils.verifyPositions(this, s1,
                s1HeaderBegin, s1HeaderEnd + s1Header.length() - 1,
                s1BodyBegin + s1Header.length() - 1, s1BodyEnd + s1Header.length() + s1Body.length() - 2,
                s1FooterBegin + s1Header.length() + s1Body.length() - 2, s1FooterEnd + s1Header.length() + s1Body.length() + s1Footer.length() - 3
                );
        
//        GuardUtils.dumpGuardedAttr(editor.doc);
        
        int length = s1Header.length() + s1Body.length() + s1Footer.length() - 3;
        GuardUtils.verifyPositions(this, s2,
                s2HeaderBegin + length, s2HeaderEnd + length,
                s2BodyBegin + length, s2BodyEnd + length,
                s2FooterBegin + length, s2FooterEnd + length
                );
        GuardUtils.verifyGuardAttr(this, editor.doc, s1);
        GuardUtils.verifyGuardAttr(this, editor.doc, s2);
        
        
        s2.setHeader("HEADER2");
        s2.setBody("BODY2");
        s2.setFooter("FOOTER2");
//        GuardUtils.print(s1);
//        GuardUtils.print(s2);
//        GuardUtils.dumpDocument(editor.doc);
//        System.out.println("...set text to ss1");
//        txt = "ss1 simple text\n";
//        s1StartI = s1.getStartPosition().getOffset();
//        s2StartI = s2.getStartPosition().getOffset();
//        s2EndI = s2.getEndPosition().getOffset();
//        s1.setBody(txt);
//        GuardUtils.dumpDocument(editor.doc);
//        assertEquals("s1.setText", txt, s1.getText());
//        GuardUtils.verifyPositions(this, s1, s1StartI, s1StartI + txt.length());
//        GuardUtils.verifyPositions(this, s2, s2StartI + txt.length(), s2EndI + txt.length());
        
        // delete interior section
        s1.deleteSection();
//        GuardUtils.print(s1);
//        GuardUtils.print(s2);
//        GuardUtils.dumpDocument(editor.doc);
        assertTrue("ss1.valid", !s1.isValid());
        GuardUtils.verifyGuardAttr(this, editor.doc, s2);

    }
    
    public void testManagement() throws BadLocationException {
        System.out.println("testManagement");
        
        List<GuardedSection> wanted = new ArrayList<GuardedSection>();
        
        // add simple section
        String gs1Name = "gs1";
        GuardedSection gs1 = guards.createSimpleSection(editor.doc.createPosition(0), gs1Name);
        assertNotNull(gs1Name, gs1);
        assertEquals("gs1 not found", gs1, guards.findSimpleSection(gs1Name));
        assertNull("wrong section type", guards.findInteriorSection(gs1Name));
        wanted.add(gs1);
        verifyGuards(wanted, guards.getGuardedSections());
        
        // add simple section
        String gs2Name = "gs2";
        GuardedSection gs2 = guards.createSimpleSection(
                editor.doc.createPosition(gs1.getEndPosition().getOffset() + 1),
                gs2Name);
        assertNotNull(gs2Name, gs2);
        assertEquals("gs2 not found", gs2, guards.findSimpleSection(gs2Name));
        assertNull("wrong section type", guards.findInteriorSection(gs2Name));
        assertEquals("gs1 not found", gs1, guards.findSimpleSection(gs1Name));
        wanted.add(gs2);
        verifyGuards(wanted, guards.getGuardedSections());
        
        // add interior section
        String gs3Name = "gs3";
        GuardedSection gs3 = guards.createInteriorSection(
                editor.doc.createPosition(gs2.getEndPosition().getOffset() + 1),
                gs3Name);
        assertNotNull(gs3Name, gs3);
        assertEquals("gs3 not found", gs3, guards.findInteriorSection(gs3Name));
        assertNull("wrong section type", guards.findSimpleSection(gs3Name));
        assertEquals("gs1 not found", gs1, guards.findSimpleSection(gs1Name));
        assertEquals("gs2 not found", gs2, guards.findSimpleSection(gs2Name));
        wanted.add(gs3);
        verifyGuards(wanted, guards.getGuardedSections());
        
        // insert simple section after gs1
        String gs4Name = "gs4";
        GuardedSection gs4 = guards.createSimpleSection(
                editor.doc.createPosition(gs1.getEndPosition().getOffset() + 1),
                gs4Name);
        assertNotNull(gs4Name, gs4);
        assertEquals("gs4 not found", gs4, guards.findSimpleSection(gs4Name));
        assertNull("wrong section type", guards.findInteriorSection(gs4Name));
        assertEquals("gs1 not found", gs1, guards.findSimpleSection(gs1Name));
        assertEquals("gs2 not found", gs2, guards.findSimpleSection(gs2Name));
        assertEquals("gs3 not found", gs3, guards.findInteriorSection(gs3Name));
        wanted.add(1, gs4);
        verifyGuards(wanted, guards.getGuardedSections());
        
        // remove gs4
        gs4.deleteSection();
        assertNull("gs4 found", guards.findSimpleSection(gs4Name));
        assertNull("wrong section type", guards.findInteriorSection(gs4Name));
        assertEquals("gs1 not found", gs1, guards.findSimpleSection(gs1Name));
        assertEquals("gs2 not found", gs2, guards.findSimpleSection(gs2Name));
        assertEquals("gs3 not found", gs3, guards.findInteriorSection(gs3Name));
        wanted.remove(gs4);
        verifyGuards(wanted, guards.getGuardedSections());
        
        // add gs4
        gs4 = guards.createSimpleSection(
                editor.doc.createPosition(gs1.getEndPosition().getOffset() + 1),
                gs4Name);
        assertNotNull(gs4Name, gs4);
        assertEquals("gs4 not found", gs4, guards.findSimpleSection(gs4Name));
        assertNull("wrong section type", guards.findInteriorSection(gs4Name));
        assertEquals("gs1 not found", gs1, guards.findSimpleSection(gs1Name));
        assertEquals("gs2 not found", gs2, guards.findSimpleSection(gs2Name));
        assertEquals("gs3 not found", gs3, guards.findInteriorSection(gs3Name));
        wanted.add(1, gs4);
        verifyGuards(wanted, guards.getGuardedSections());
        
        // remove gs1
        gs1.deleteSection();
        assertNull("gs1 found", guards.findSimpleSection(gs1Name));
        assertNull("wrong section type", guards.findInteriorSection(gs1Name));
        assertEquals("gs2 not found", gs2, guards.findSimpleSection(gs2Name));
        assertEquals("gs3 not found", gs3, guards.findInteriorSection(gs3Name));
        assertEquals("gs4 not found", gs4, guards.findSimpleSection(gs4Name));
        wanted.remove(gs1);
        verifyGuards(wanted, guards.getGuardedSections());
        
        // remove gs3
        gs3.deleteSection();
        assertNull("gs3 found", guards.findSimpleSection(gs3Name));
        assertNull("wrong section type", guards.findInteriorSection(gs3Name));
        assertEquals("gs2 not found", gs2, guards.findSimpleSection(gs2Name));
        assertEquals("gs4 not found", gs4, guards.findSimpleSection(gs4Name));
        wanted.remove(gs3);
        verifyGuards(wanted, guards.getGuardedSections());
        
        // remove gs2
        gs2.deleteSection();
        assertNull("gs2 found", guards.findSimpleSection(gs2Name));
        assertNull("wrong section type", guards.findInteriorSection(gs2Name));
        assertEquals("gs4 not found", gs4, guards.findSimpleSection(gs4Name));
        wanted.remove(gs2);
        verifyGuards(wanted, guards.getGuardedSections());
        
        // remove gs4
        gs4.deleteSection();
        assertNull("gs4 found", guards.findSimpleSection(gs4Name));
        assertNull("wrong section type", guards.findInteriorSection(gs4Name));
        wanted.remove(gs4);
        verifyGuards(wanted, guards.getGuardedSections());
        
    }
    
    // test rename sections
    
    public void testRenameSimpleSection() throws BadLocationException, PropertyVetoException {
        System.out.println("-- testRenameSimpleSection -------------");
        
        editor.doc.insertString(0, "aaa", null);
        SimpleSection ss1 = guards.createSimpleSection(editor.doc.createPosition(1), "ss1");
        assertEquals("name", "ss1", ss1.getName());
        ss1.setName("ssNewName");
        assertTrue("valid", ss1.isValid());
        assertEquals("new name", "ssNewName", ss1.getName());
        // set the same name
        ss1.setName("ssNewName");
        
        SimpleSection ss2 = guards.createSimpleSection(
                editor.doc.createPosition(ss1.getEndPosition().getOffset() + 1),
                "ss2");
        
        // rename to existing name
        try {
            ss1.setName("ss2");
            fail("accepted already existing name");
        } catch (PropertyVetoException ex) {
            assertTrue("valid", ss1.isValid());
            assertEquals("name", "ssNewName", ss1.getName());
        }
    }
    
    public void testRenameInteriorSection() throws BadLocationException, PropertyVetoException {
        System.out.println("-- testRenameInteriorSection -------------");
        
        editor.doc.insertString(0, "aaa", null);
        InteriorSection is1 = guards.createInteriorSection(editor.doc.createPosition(1), "is1");
        assertEquals("name", "is1", is1.getName());
        is1.setName("isNewName");
        assertTrue("valid", is1.isValid());
        assertEquals("new name", "isNewName", is1.getName());
        // set the same name
        is1.setName("isNewName");
        
        InteriorSection is2 = guards.createInteriorSection(
                editor.doc.createPosition(is1.getEndPosition().getOffset() + 1),
                "is2");
        
        // rename to existing name
        try {
            is1.setName("is2");
            fail("accepted already existing name");
        } catch (PropertyVetoException ex) {
            assertTrue("valid", is1.isValid());
            assertEquals("name", "isNewName", is1.getName());
        }
    }
    
    public void testCreateSimpleSection() throws BadLocationException {
        System.out.println("-- testCreateSimpleSection -------------");
        
        editor.doc.insertString(0, "aaa", null);
        SimpleSection ss1 = guards.createSimpleSection(editor.doc.createPosition(1), "ss1");
        assertEquals("doc.content", "a\n \naa", editor.doc.getText(0, editor.doc.getLength()));
        
        try {
            guards.createSimpleSection(editor.doc.createPosition(2), "ss2");
            fail("section created inside another section!");
        } catch (Throwable t) {
            assertEquals("wrong exception", IllegalArgumentException.class, t.getClass());
        }
        
        try {
            guards.createSimpleSection(
                    editor.doc.createPosition(ss1.getEndPosition().getOffset() + 1),
                    "ss1");
            fail("section with duplicate name created!");
        } catch (Throwable t) {
            assertEquals("wrong exception", IllegalArgumentException.class, t.getClass());
        }
        
        guards.createSimpleSection(
                editor.doc.createPosition(ss1.getEndPosition().getOffset() + 1),
                "ss2");
        assertEquals("doc.content", "a\n \n\n \naa", editor.doc.getText(0, editor.doc.getLength()));
    }
    
    public void testCreateInteriorSection() throws BadLocationException {
        System.out.println("-- testCreateInteriorSection -------------");
        
        editor.doc.insertString(0, "aaa", null);
        InteriorSection is1 = guards.createInteriorSection(editor.doc.createPosition(1), "is1");
        assertEquals("doc.content", "a\n \n \n \naa", editor.doc.getText(0, editor.doc.getLength()));
        
        try {
            guards.createInteriorSection(editor.doc.createPosition(4), "is2");
            fail("section created inside another section!");
        } catch (Throwable t) {
            assertEquals("wrong exception", IllegalArgumentException.class, t.getClass());
        }
        
        try {
            guards.createInteriorSection(
                    editor.doc.createPosition(is1.getEndPosition().getOffset() + 1),
                    "is1");
            fail("section with duplicate name created!");
        } catch (Throwable t) {
            assertEquals("wrong exception", IllegalArgumentException.class, t.getClass());
        }
        
        guards.createInteriorSection(
                editor.doc.createPosition(is1.getEndPosition().getOffset() + 1),
                "is2");
        assertEquals("doc.content", "a\n \n \n \n\n \n \n \naa", editor.doc.getText(0, editor.doc.getLength()));
    }
    
    public void testModifyInteriorSection() throws BadLocationException {
        System.out.println("-- testModifyInteriorSection -------------");
        
        editor.doc.insertString(0, "aaa", null);
        InteriorSection is1 = guards.createInteriorSection(editor.doc.createPosition(1), "is1");
        assertEquals("doc.content", "a\n \n \n \naa", editor.doc.getText(0, editor.doc.getLength()));
        assertTrue(GuardUtils.isGuarded(editor.doc, 3)); // end of header \n
        assertTrue(!GuardUtils.isGuarded(editor.doc, 4)); // start of body
        assertTrue(!GuardUtils.isGuarded(editor.doc, 5)); // end of body
        assertTrue(GuardUtils.isGuarded(editor.doc, 6)); // start of footer
        
        editor.doc.insertString(4, "X", null);
        assertEquals("doc.content", "a\n \nX \n \naa", editor.doc.getText(0, editor.doc.getLength()));
        assertTrue(GuardUtils.isGuarded(editor.doc, 3)); // end of header \n
        assertTrue(!GuardUtils.isGuarded(editor.doc, 4)); // start of body X
        assertTrue(!GuardUtils.isGuarded(editor.doc, 5)); // body
        assertTrue(!GuardUtils.isGuarded(editor.doc, 6)); // end of body
        assertTrue(GuardUtils.isGuarded(editor.doc, 7)); // start of footer
        
        is1.setHeader("HEADER");
        is1.setFooter("FOOTER");
        is1.setBody("BODY");
        assertEquals("doc.content", "a\nHEADER\nBODY\nFOOTER\naa", editor.doc.getText(0, editor.doc.getLength()));
        
        String content = "a\nHEADER\nBODY\nFOOTER\naa";
        editor.doc.insertString(content.indexOf("BODY"), "X", null);
        content = "a\nHEADER\nXBODY\nFOOTER\naa";
        assertTrue(GuardUtils.isGuarded(editor.doc, content.indexOf("\nXBODY"))); // end of header \n
        assertTrue(!GuardUtils.isGuarded(editor.doc, content.indexOf("XBODY"))); // start of body X
        assertTrue(!GuardUtils.isGuarded(editor.doc, content.indexOf("BODY"))); // body
        assertTrue(!GuardUtils.isGuarded(editor.doc, content.indexOf("\nFOOTER"))); // end of body
        assertTrue(GuardUtils.isGuarded(editor.doc, content.indexOf("FOOTER"))); // start of footer
    }
    
    private void verifyGuards(Iterable<GuardedSection> wanted, Iterable<GuardedSection> queried) {
        Iterator<GuardedSection> itWanted = wanted.iterator();
        Iterator<GuardedSection> itQueried = queried.iterator();
        for(int i = 0; ; i++) {
            if (itWanted.hasNext()) {
                assertTrue(i + ": missing guard" , itQueried.hasNext());
                assertEquals(i + ": wrong guard", itWanted.next(), itQueried.next());
            } else {
                assertFalse(i + ": extra guard ", itQueried.hasNext());
                break;
            }
        }
    }
}
