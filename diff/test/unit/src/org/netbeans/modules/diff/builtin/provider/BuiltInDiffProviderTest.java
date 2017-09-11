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

package org.netbeans.modules.diff.builtin.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import org.netbeans.api.diff.Difference;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.diff.DiffProvider;

/**
 * The test of the built-in diff provider
 *
 * @author Martin Entlicher
 */
public class BuiltInDiffProviderTest extends NbTestCase {

    private static final String[] SIMPLE1 = {
        "Hi",
        "there!",
        "  ",
        "Oops,",
        "end."
    };
    
    /** Creates a new instance of BuiltInDiffProviderTest */
    public BuiltInDiffProviderTest(String name) {
        super(name);
    }
    
    private static DiffProvider createDiffProvider() {
        BuiltInDiffProvider provider = new BuiltInDiffProvider();
        BuiltInDiffProvider.Options options = new BuiltInDiffProvider.Options();
        options.ignoreCase = false;
        options.ignoreInnerWhitespace = false;
        options.ignoreLeadingAndtrailingWhitespace = false;
        provider.setOptions(options);
        return provider;
        // Use CmdlineDiffProvider as a reference to check the test is O.K.
        //return org.netbeans.modules.diff.cmdline.CmdlineDiffProvider.createDefault();
    }
    
    // A simple ADD difference
    public void testSimpleAdd() throws Exception {
        DiffProvider bdp = createDiffProvider();
        String s1 = linesToString(SIMPLE1);
        String[] simple2add = new String[SIMPLE1.length + 1];
        String added = "Added Line";
        for (int i = 0; i <= SIMPLE1.length; i++) {
            for (int j = 0; j < simple2add.length; j++) {
                if (i == j) {
                    simple2add[j] = added;
                } else if (j < i) {
                    simple2add[j] = SIMPLE1[j];
                } else {
                    simple2add[j] = SIMPLE1[j-1];
                }
            }
            String s2 = linesToString(simple2add);
            Difference[] diff = bdp.computeDiff(new StringReader(s1),
                                                new StringReader(s2));
            assertEquals("WAS COMPARING:\n"+s1+"WITH:\n"+s2, 1, diff.length);
            String rightDiff = "Difference(ADD, "+i+", "+0+", "+(i+1)+", "+(i+1)+")";
            assertEquals(diff[0].toString()+" != "+rightDiff+"\nWAS COMPARING:\n"+s1+"WITH:\n"+s2, rightDiff, diff[0].toString());
        }
    }
    
    // A simple DELETE difference
    public void testSimpleDelete() throws Exception {
        DiffProvider bdp = createDiffProvider();
        String s1 = linesToString(SIMPLE1);
        String[] simple2delete = new String[SIMPLE1.length - 1];
        for (int i = 0; i < SIMPLE1.length; i++) {
            for (int j = 0; j < simple2delete.length; j++) {
                if (j < i) {
                    simple2delete[j] = SIMPLE1[j];
                } else {
                    simple2delete[j] = SIMPLE1[j+1];
                }
            }
            String s2 = linesToString(simple2delete);
            Difference[] diff = bdp.computeDiff(new StringReader(s1),
                                                new StringReader(s2));
            assertEquals("WAS COMPARING:\n"+s1+"WITH:\n"+s2, 1, diff.length);
            String rightDiff = "Difference(DELETE, "+(i+1)+", "+(i+1)+", "+i+", "+0+")";
            assertEquals(diff[0].toString()+" != "+rightDiff+"\nWAS COMPARING:\n"+s1+"WITH:\n"+s2, rightDiff, diff[0].toString());
        }
    }
    
    // A simple CHANGE difference
    public void testSimpleChange() throws Exception {
        DiffProvider bdp = createDiffProvider();
        String s1 = linesToString(SIMPLE1);
        String[] simple2delete = new String[SIMPLE1.length];
        for (int i = 0; i < SIMPLE1.length; i++) {
            for (int j = 0; j < simple2delete.length; j++) {
                if (i == j) {
                    simple2delete[j] = "Changed Line";
                } else if (j < i) {
                    simple2delete[j] = SIMPLE1[j];
                } else {
                    simple2delete[j] = SIMPLE1[j];
                }
            }
            String s2 = linesToString(simple2delete);
            Difference[] diff = bdp.computeDiff(new StringReader(s1),
                                                new StringReader(s2));
            assertEquals("WAS COMPARING:\n"+s1+"WITH:\n"+s2, 1, diff.length);
            String rightDiff = "Difference(CHANGE, "+(i+1)+", "+(i+1)+", "+(i+1)+", "+(i+1)+")";
            assertEquals(diff[0].toString()+" != "+rightDiff+"\nWAS COMPARING:\n"+s1+"WITH:\n"+s2, rightDiff, diff[0].toString());
        }
    }
    
    public void testFile1() throws Exception {
        //System.out.println("Stream = "+BuiltInDiffProviderTest.class.getResourceAsStream(
        //        "/org/netbeans/modules/diff/builtin/provider/DiffTestFile1a.txt"));
        BufferedReader r1 = new BufferedReader(new InputStreamReader(
            BuiltInDiffProviderTest.class.getResourceAsStream(
                "/org/netbeans/modules/diff/builtin/provider/DiffTestFile1a.txt")
        ));
        BufferedReader r2 = new BufferedReader(new InputStreamReader(
            BuiltInDiffProviderTest.class.getResourceAsStream(
                "/org/netbeans/modules/diff/builtin/provider/DiffTestFile1b.txt")
        ));
        DiffProvider bdp = createDiffProvider();
        Difference[] diff = bdp.computeDiff(r1, r2);
        BufferedReader differences = new BufferedReader(new InputStreamReader(
            BuiltInDiffProviderTest.class.getResourceAsStream(
                "/org/netbeans/modules/diff/builtin/provider/DiffTestFile1d.txt")
        ));
        assertTrue(checkDifferences(diff, differences));
    }
    
    public void testFile2() throws Exception {
        //System.out.println("Stream = "+BuiltInDiffProviderTest.class.getResourceAsStream(
        //        "/org/netbeans/modules/diff/builtin/provider/DiffTestFile2a.txt"));
        BufferedReader r1 = new BufferedReader(new InputStreamReader(
            BuiltInDiffProviderTest.class.getResourceAsStream(
                "/org/netbeans/modules/diff/builtin/provider/DiffTestFile2a.txt")
        ));
        BufferedReader r2 = new BufferedReader(new InputStreamReader(
            BuiltInDiffProviderTest.class.getResourceAsStream(
                "/org/netbeans/modules/diff/builtin/provider/DiffTestFile2b.txt")
        ));
        DiffProvider bdp = createDiffProvider();
        Difference[] diff = bdp.computeDiff(r1, r2);
        BufferedReader differences = new BufferedReader(new InputStreamReader(
            BuiltInDiffProviderTest.class.getResourceAsStream(
                "/org/netbeans/modules/diff/builtin/provider/DiffTestFile2d.txt")
        ));
        assertTrue(checkDifferences(diff, differences));
    }
    
    private static String linesToString(String[] lines) {
        String newline = System.getProperty("line.separator");
        StringBuffer sb1 = new StringBuffer();
        for (int i = 0; i < lines.length; i++) {
            sb1.append(lines[i]);
            sb1.append(newline);
        }
        return sb1.toString();
    }
    
    private static boolean checkDifferences(Difference[] diffs, BufferedReader differences) throws IOException {
        int i = 0;
        String diffLine;
        
        int type = 0;
        int from1 = 0;
        int end1 = 0;
        int from2 = 0;
        int end2 = 0;
        String text1 = "";
        String text2 = "";
        while ((diffLine = differences.readLine()) != null) {
            if ("---".equals(diffLine)) continue;
            if (diffLine.startsWith(">")) {
                text2 += diffLine.substring(2) + System.getProperty("line.separator");
                continue;
            } else if (diffLine.startsWith("<")) {
                text1 += diffLine.substring(2) + System.getProperty("line.separator");
                continue;
            } else {
                if (from1 != 0 || from2 != 0) {
                    if (text1.length() == 0) text1 = null;
                    if (text2.length() == 0) text2 = null;
                    assertEquals("Type of difference "+i+" does not match.", type, diffs[i].getType());
                    assertEquals("First start of difference "+i+" does not match.", from1, diffs[i].getFirstStart());
                    assertEquals("First end of difference "+i+" does not match.", end1, diffs[i].getFirstEnd());
                    assertEquals("Second start of difference "+i+" does not match.", from2, diffs[i].getSecondStart());
                    assertEquals("Second end of difference "+i+" does not match.", end2, diffs[i].getSecondEnd());
                    //assertEquals("First text of difference "+i+" does not match\nExpected:\n"+text1+"\nWas:\n"+diffs[i].getFirstText()+"\n", text1, diffs[i].getFirstText());
                    //assertEquals("Second text of difference "+i+" does not match\nExpected:\n"+text2+"\nWas:\n"+diffs[i].getSecondText()+"\n", text2, diffs[i].getSecondText());
                    i++;
                    from1 = from2 = end1 = end2 = 0;
                    text1 = text2 = "";
                }
            }
            char c = '\0';
            int index = 0;
            while (index < diffLine.length() && Character.isDigit(c = diffLine.charAt(index))) {
                index++;
                from1 = 10*from1 + Character.digit(c, 10);
            }
            if (c == ',') {
                index++;
                while (index < diffLine.length() && Character.isDigit(c = diffLine.charAt(index))) {
                    index++;
                    end1 = 10*end1 + Character.digit(c, 10);
                }
            } else {
                end1 = from1;
            }
            if (c == 'a') {
                type = Difference.ADD;
            } else if (c == 'd') {
                type = Difference.DELETE;
            } else if (c == 'c') {
                type = Difference.CHANGE;
            } else {
                fail("Unknown change '"+c+"' read at line '"+diffLine+"'");
            }
            index++;
            while (index < diffLine.length() && Character.isDigit(c = diffLine.charAt(index))) {
                index++;
                from2 = 10*from2 + Character.digit(c, 10);
            }
            if (c == ',') {
                index++;
                while (index < diffLine.length() && Character.isDigit(c = diffLine.charAt(index))) {
                    index++;
                    end2 = 10*end2 + Character.digit(c, 10);
                }
            } else {
                end2 = from2;
            }
            if (type == Difference.ADD) {
                end1 = 0;
            } else if (type == Difference.DELETE) {
                end2 = 0;
            }
        }
        if (text1.length() == 0) text1 = null;
        if (text2.length() == 0) text2 = null;
        assertEquals("Type of difference "+i+" does not match.", type, diffs[i].getType());
        assertEquals("First start of difference "+i+" does not match.", from1, diffs[i].getFirstStart());
        assertEquals("First end of difference "+i+" does not match.", end1, diffs[i].getFirstEnd());
        assertEquals("Second start of difference "+i+" does not match.", from2, diffs[i].getSecondStart());
        assertEquals("Second end of difference "+i+" does not match.", end2, diffs[i].getSecondEnd());
        //assertEquals("First text of difference "+i+" does not match.\nExpected:\n"+text1+"\nWas:\n"+diffs[i].getFirstText()+"\n", text1, diffs[i].getFirstText());
        //assertEquals("Second text of difference "+i+" does not match\nExpected:\n"+text2+"\nWas:\n"+diffs[i].getSecondText()+"\n", text2, diffs[i].getSecondText());
        i++;
        return i == diffs.length;
    }
}
