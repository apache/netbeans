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
