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
