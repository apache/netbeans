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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import junit.framework.TestCase;
import org.netbeans.api.editor.guards.GuardedSection;

/**
 *
 * @author Jan Pokorsky
 */
public class JavaGuardedWriterTest extends TestCase {
    
    public JavaGuardedWriterTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    /**
     * Test of translate method, of class org.netbeans.modules.java.guards.JavaGuardedWriter.
     */
    public void testTranslatePlain() {
        System.out.println("write plain");
        
        char[] writeBuff = "\nclass A {\n}\n".toCharArray();
        JavaGuardedWriter instance = new JavaGuardedWriter();
        List<GuardedSection> sections = Collections.<GuardedSection>emptyList();
        
        char[] expResult = writeBuff;
        instance.setGuardedSection(sections);
        char[] result = instance.translate(writeBuff);
        assertEquals(String.valueOf(expResult), String.valueOf(result));
    }
    
    public void testTranslateLINE() throws BadLocationException {
        System.out.println("write //" + "GEN-LINE:");
        
        String writeStr = "\nclass A {" +              "\n}\n";
        String expStr =   "\nclass A {//" + "GEN-LINE:hu\n}\n";
        char[] writeBuff = writeStr.toCharArray();
        
        JavaGuardedWriter instance = new JavaGuardedWriter();
        JavaGuardedSectionsProvider provider = new JavaGuardedSectionsProvider(new Editor());
        List<GuardedSection> sections = new ArrayList<GuardedSection>();
        sections.add(provider.createSimpleSection("hu", 1, writeStr.indexOf("\n}")));
        
        instance.setGuardedSection(sections);
        char[] result = instance.translate(writeBuff);
        assertEquals(expStr, String.valueOf(result));
    }
    
    public void testTranslateLINEWithSpaces() throws BadLocationException {
        System.out.println("write with spaces //" + "GEN-LINE:");
        
        String writeStr = "\nclass A {  " + "           \n}\n";
        String expStr =   "\nclass A {//" + "GEN-LINE:hu\n}\n";
        char[] writeBuff = writeStr.toCharArray();
        
        JavaGuardedWriter instance = new JavaGuardedWriter();
        JavaGuardedSectionsProvider provider = new JavaGuardedSectionsProvider(new Editor());
        List<GuardedSection> sections = new ArrayList<GuardedSection>();
        sections.add(provider.createSimpleSection("hu", 1, writeStr.indexOf("\n}")));
        
        instance.setGuardedSection(sections);
        char[] result = instance.translate(writeBuff);
        assertEquals(expStr, String.valueOf(result));
    }
    
    public void testTranslateBEGIN_END() throws BadLocationException {
        System.out.println("write //" + "GEN-BEGIN_END:");
        
        String writeStr = "\nclass A {" +               "\n\n}" +             "\n";
        String expStr =   "\nclass A {//" + "GEN-BEGIN:hu\n\n}//" + "GEN-END:hu\n";
        char[] writeBuff = writeStr.toCharArray();
        
        JavaGuardedWriter instance = new JavaGuardedWriter();
        JavaGuardedSectionsProvider provider = new JavaGuardedSectionsProvider(new Editor());
        List<GuardedSection> sections = new ArrayList<GuardedSection>();
        sections.add(provider.createSimpleSection("hu", 1, writeStr.length() - 1));
        
        instance.setGuardedSection(sections);
        char[] result = instance.translate(writeBuff);
        assertEquals(expStr, String.valueOf(result));
    }
    
    public void testTranslateBEGIN_ENDWithSpaces() throws BadLocationException {
        System.out.println("write with spaces //" + "GEN-BEGIN_END:");
        
        String writeStr = "\nclass A {  " + "            \n\n}  " + "          \n";
        String expStr =   "\nclass A {//" + "GEN-BEGIN:hu\n\n}//" + "GEN-END:hu\n";
        char[] writeBuff = writeStr.toCharArray();
        
        JavaGuardedWriter instance = new JavaGuardedWriter();
        JavaGuardedSectionsProvider provider = new JavaGuardedSectionsProvider(new Editor());
        List<GuardedSection> sections = new ArrayList<GuardedSection>();
        sections.add(provider.createSimpleSection("hu", 1, writeStr.length() - 1));
        
        instance.setGuardedSection(sections);
        char[] result = instance.translate(writeBuff);
        assertEquals(expStr, String.valueOf(result));
    }
    
    public void testTranslateFIRST_LAST() throws BadLocationException {
        System.out.println("write //" + "GEN-FIRST_LAST:");
        
        String writeStr = "\nclass A {  " +             "\n  statement;\n}" +              "\n";
        String expStr =   "\nclass A {//" + "GEN-FIRST:hu\n  statement;\n}//" + "GEN-LAST:hu\n";
        char[] writeBuff = writeStr.toCharArray();
        
        JavaGuardedWriter instance = new JavaGuardedWriter();
        JavaGuardedSectionsProvider provider = new JavaGuardedSectionsProvider(new Editor());
        List<GuardedSection> sections = new ArrayList<GuardedSection>();
        sections.add(provider.createInteriorSection("hu",
                1, writeStr.indexOf("\n  statement;"),
                writeStr.indexOf("}"), writeStr.length() - 1));
        
        instance.setGuardedSection(sections);
        char[] result = instance.translate(writeBuff);
        assertEquals(expStr, String.valueOf(result));
    }
    
    public void testTranslateFIRST_LASTWithSpaces() throws BadLocationException {
        System.out.println("write with spaces //" + "GEN-FIRST_LAST:");
        
        String writeStr = "\nclass A {  " + "            \n  statement;\n}  " + "           \n";
        String expStr =   "\nclass A {//" + "GEN-FIRST:hu\n  statement;\n}//" + "GEN-LAST:hu\n";
        char[] writeBuff = writeStr.toCharArray();
        
        JavaGuardedWriter instance = new JavaGuardedWriter();
        JavaGuardedSectionsProvider provider = new JavaGuardedSectionsProvider(new Editor());
        List<GuardedSection> sections = new ArrayList<GuardedSection>();
        sections.add(provider.createInteriorSection("hu",
                1, writeStr.indexOf("\n  statement;"),
                writeStr.indexOf("}"), writeStr.length() - 1));
        
        instance.setGuardedSection(sections);
        char[] result = instance.translate(writeBuff);
        assertEquals(expStr, String.valueOf(result));
    }
    
    public void testTranslateFIRST_HEADEREND_LAST() throws BadLocationException {
        System.out.println("write //" + "GEN-FIRST_HEADEREND_LAST:");
        
        String writeStr = "\nclass A  " + "            \n{  " + "                \n  statement;\n}  " + "           \n";
        String expStr =   "\nclass A//" + "GEN-FIRST:hu\n{//" + "GEN-HEADEREND:hu\n  statement;\n}//" + "GEN-LAST:hu\n";
        char[] writeBuff = writeStr.toCharArray();
        
        JavaGuardedWriter instance = new JavaGuardedWriter();
        JavaGuardedSectionsProvider provider = new JavaGuardedSectionsProvider(new Editor());
        List<GuardedSection> sections = new ArrayList<GuardedSection>();
        sections.add(provider.createInteriorSection("hu",
                1, writeStr.indexOf("\n  statement;"),
                writeStr.indexOf("}"), writeStr.length() - 1));
        
        instance.setGuardedSection(sections);
        char[] result = instance.translate(writeBuff);
        assertEquals(expStr, String.valueOf(result));
    }
    
    public void testTranslateFIRST_HEADEREND_LASTWithSpaces() throws BadLocationException {
        System.out.println("write with spaces //" + "GEN-FIRST_HEADEREND_LAST:");
        
        String writeStr = "\nclass A" +               "\n{" +                   "\n  statement;\n}" +              "\n";
        String expStr =   "\nclass A//" + "GEN-FIRST:hu\n{//" + "GEN-HEADEREND:hu\n  statement;\n}//" + "GEN-LAST:hu\n";
        char[] writeBuff = writeStr.toCharArray();
        
        JavaGuardedWriter instance = new JavaGuardedWriter();
        JavaGuardedSectionsProvider provider = new JavaGuardedSectionsProvider(new Editor());
        List<GuardedSection> sections = new ArrayList<GuardedSection>();
        sections.add(provider.createInteriorSection("hu",
                1, writeStr.indexOf("\n  statement;"),
                writeStr.indexOf("}"), writeStr.length() - 1));
        
        instance.setGuardedSection(sections);
        char[] result = instance.translate(writeBuff);
        assertEquals(expStr, String.valueOf(result));
    }

    public void testTranslateWithoutDuplicating() throws BadLocationException {
        System.out.println("write with guard comments already in place");

        String writeStr = "\nclass A {//"+"GEN-BEGIN:hu\n\n}//"+"GEN-END:hu\n";
        String expStr =   "\nclass A {//"+"GEN-BEGIN:hu\n\n}//"+"GEN-END:hu\n";
        char[] writeBuff = writeStr.toCharArray();
        JavaGuardedReader.setKeepGuardCommentsForTest(true);

        JavaGuardedWriter instance = new JavaGuardedWriter();
        JavaGuardedSectionsProvider provider = new JavaGuardedSectionsProvider(new Editor());
        List<GuardedSection> sections = new ArrayList<GuardedSection>();
        sections.add(provider.createSimpleSection("hu", 1, writeStr.length() - 1));
        
        instance.setGuardedSection(sections);
        try {
            char[] result = instance.translate(writeBuff);
            assertEquals(expStr, String.valueOf(result));
        } finally {
            JavaGuardedReader.setKeepGuardCommentsForTest(false);
        }
    }

    /**
     * This tests renaming a section when guarded comments are kept. When the
     * renaming is processed, there is still the original section name encoded
     * in the guard comment that is part of the current section text (document).
     * With the rename a new name needs to be written and the previous comment
     * removed.
     */
    public void testTranslateRenamed() throws BadLocationException {
        System.out.println("write new name with old guard comments in place");

        String writeStr = "\nclass A {//"+"GEN-BEGIN:oldName\n\n}//"+"GEN-END:hu\n";
        String expStr =   "\nclass A {//"+"GEN-BEGIN:hu\n\n}//"+"GEN-END:hu\n";
        char[] writeBuff = writeStr.toCharArray();
        JavaGuardedReader.setKeepGuardCommentsForTest(true);

        JavaGuardedWriter instance = new JavaGuardedWriter();
        JavaGuardedSectionsProvider provider = new JavaGuardedSectionsProvider(new Editor());
        List<GuardedSection> sections = new ArrayList<GuardedSection>();
        sections.add(provider.createSimpleSection("hu", 1, writeStr.length() - 1));
        
        instance.setGuardedSection(sections);
        try {
            char[] result = instance.translate(writeBuff);
            assertEquals(expStr, String.valueOf(result));
        } finally {
            JavaGuardedReader.setKeepGuardCommentsForTest(false);
        }
    }
}
