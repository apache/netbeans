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

package org.netbeans.modules.java.guards;

import java.util.List;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.InteriorSection;
import org.netbeans.api.editor.guards.SimpleSection;

/**
 *
 * @author Jan Pokorsky
 */
public class JavaGuardedReaderTest extends TestCase {

    private JavaGuardedSectionsProvider provider;

    private JavaGuardedReader instance;

    private Editor editor;
    
    public JavaGuardedReaderTest(String testName) {
        super(testName);
    }

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new JavaGuardedReaderTest("testTranslatePlain"));
        suite.addTest(new JavaGuardedReaderTest("testTranslateLINE"));
        suite.addTest(new JavaGuardedReaderTest("testTranslateBEGIN_END1"));
        suite.addTest(new JavaGuardedReaderTest("testTranslateBEGIN_END2"));
        suite.addTest(new JavaGuardedReaderTest("testTranslateFIRST_LAST"));
        suite.addTest(new JavaGuardedReaderTest("testTranslateFIRST_HEADEREND_LAST"));
        return suite;
    }
    
    @Override
    protected void setUp() throws Exception {
        editor = new Editor();
        provider = new JavaGuardedSectionsProvider(editor);
        
        instance = new JavaGuardedReader(provider);
    }

    @Override
    protected void tearDown() throws Exception {
        provider = null;
        instance = null;
    }

    /**
     * Test of translateToCharBuff method, of class org.netbeans.modules.java.guards.JavaGuardedReader.
     */
    public void testTranslatePlain() {
        System.out.println("read plain");
        
        String expStr =   "\nclass A {\n}\n";
        char[] readBuff = expStr.toCharArray();
        
        char[] result = instance.translateToCharBuff(readBuff);
        List<GuardedSection> sections = instance.getGuardedSections();
        assertEquals(expStr, String.valueOf(result));
        assertTrue("sections not empty", sections.isEmpty());
    }
    
    public void testTranslateLINE() {
        System.out.println("read //" + "GEN-LINE:");
        
        String readStr = "\nclass A {//" + "GEN-LINE:hu\n}\n";
        editor.setStringContent(readStr);
        String expStr =  "\nclass A {  " + "           \n}\n";
        char[] readBuff = readStr.toCharArray();
        
        char[] result = instance.translateToCharBuff(readBuff);
        List<GuardedSection> sections = instance.getGuardedSections();
        
        assertEquals(expStr, String.valueOf(result));
        assertEquals("sections", 1, sections.size());
        
        GuardedSection expSection = sections.get(0);
        assertEquals(SimpleSection.class, expSection.getClass());
        assertEquals("section valid", true, expSection.isValid());
        assertEquals("section name", "hu", expSection.getName());
        assertEquals("begin", 1, expSection.getStartPosition().getOffset());
        assertEquals("end", expStr.indexOf("}") - 1, expSection.getEndPosition().getOffset());
    }
    
    public void testTranslateBEGIN_END1() {
        System.out.println("read //" + "GEN-BEGIN_END1:");
        
        String readStr = "\nclass A {//" + "GEN-BEGIN:hu\n\n}//" + "GEN-END:hu\n";
        editor.setStringContent(readStr);
        String expStr =  "\nclass A {//" + "GEN-BEGIN:hu\n\n}//" + "GEN-END:hu\n";
        char[] readBuff = readStr.toCharArray();

        JavaGuardedReader.setKeepGuardCommentsForTest(true);
        char[] result = instance.translateToCharBuff(readBuff);
        List<GuardedSection> sections = instance.getGuardedSections();
        
        assertEquals(expStr, String.valueOf(result));
        assertEquals("sections", 1, sections.size());
        
        GuardedSection expSection = sections.get(0);
        assertEquals(SimpleSection.class, expSection.getClass());
        assertEquals("section valid", true, expSection.isValid());
        assertEquals("section name", "hu", expSection.getName());
        assertEquals("begin", 1, expSection.getStartPosition().getOffset());
        assertEquals("end", expStr.length() - 1, expSection.getEndPosition().getOffset());
    }

    public void testTranslateBEGIN_END2() {
        System.out.println("read //" + "GEN-BEGIN_END2:");
        
        String readStr = "\nclass A {//" + "GEN-BEGIN:hu\n\n}//" + "GEN-END:hu\n";
        editor.setStringContent(readStr);
        String expStr =  "\nclass A {  " + "            \n\n}  " + "          \n";
        char[] readBuff = readStr.toCharArray();
        
        JavaGuardedReader.setKeepGuardCommentsForTest(false);
        char[] result = instance.translateToCharBuff(readBuff);
        List<GuardedSection> sections = instance.getGuardedSections();
        
        assertEquals(expStr, String.valueOf(result));
        assertEquals("sections", 1, sections.size());
        
        GuardedSection expSection = sections.get(0);
        assertEquals(SimpleSection.class, expSection.getClass());
        assertEquals("section valid", true, expSection.isValid());
        assertEquals("section name", "hu", expSection.getName());
        assertEquals("begin", 1, expSection.getStartPosition().getOffset());
        assertEquals("end", expStr.length() - 1, expSection.getEndPosition().getOffset());
    }
    
    public void testTranslateFIRST_LAST() {
        System.out.println("read //" + "GEN-FIRST_LAST:");
        
        String readStr = "\nclass A {//" + "GEN-FIRST:hu\n  statement;\n}//" + "GEN-LAST:hu\n";
        editor.setStringContent(readStr);
        String expStr =  "\nclass A {  " + "            \n  statement;\n}  " + "           \n";
        char[] readBuff = readStr.toCharArray();
        
        char[] result = instance.translateToCharBuff(readBuff);
        List<GuardedSection> sections = instance.getGuardedSections();
        
        assertEquals(expStr, String.valueOf(result));
        assertEquals("sections", 1, sections.size());
        
        GuardedSection expSection = sections.get(0);
        assertEquals(InteriorSection.class, expSection.getClass());
        assertEquals("section valid", true, expSection.isValid());
        assertEquals("section name", "hu", expSection.getName());
        assertEquals("begin", 1, expSection.getStartPosition().getOffset());
        assertEquals("end", expStr.length() - 1, expSection.getEndPosition().getOffset());
        InteriorSection iSection = (InteriorSection) expSection;
        assertEquals("body.begin", expStr.indexOf("  statement;"), iSection.getBodyStartPosition().getOffset());
        assertEquals("body.end", expStr.indexOf("\n}"), iSection.getBodyEndPosition().getOffset());
    }
    
    public void testTranslateFIRST_HEADEREND_LAST() {
        System.out.println("read //" + "GEN-FIRST_HEADEREND_LAST:");
        
        String readStr = "\nclass A //" + "GEN-FIRST:hu\n{//" + "GEN-HEADEREND:hu\n  statement;\n}//" + "GEN-LAST:hu\n";
        editor.setStringContent(readStr);
        String expStr =  "\nclass A   " + "            \n{  " + "                \n  statement;\n}  " + "           \n";
        char[] readBuff = readStr.toCharArray();
        
        char[] result = instance.translateToCharBuff(readBuff);
        List<GuardedSection> sections = instance.getGuardedSections();
        
        assertEquals(expStr, String.valueOf(result));
        assertEquals("sections", 1, sections.size());
        
        GuardedSection expSection = sections.get(0);
        assertEquals(InteriorSection.class, expSection.getClass());
        assertEquals("section valid", true, expSection.isValid());
        assertEquals("section name", "hu", expSection.getName());
        assertEquals("begin", 1, expSection.getStartPosition().getOffset());
        assertEquals("end", expStr.length() - 1, expSection.getEndPosition().getOffset());
        InteriorSection iSection = (InteriorSection) expSection;
        assertEquals("body.begin", expStr.indexOf("  statement;"), iSection.getBodyStartPosition().getOffset());
        assertEquals("body.end", expStr.indexOf("\n}"), iSection.getBodyEndPosition().getOffset());
    }
    
}
