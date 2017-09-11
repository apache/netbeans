/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.support;

import org.netbeans.modules.jshell.parsing.JShellParser;
import org.netbeans.modules.jshell.model.ConsoleSection;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import jdk.jshell.JShell;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.jshell.model.ConsoleModel;
import org.netbeans.modules.jshell.model.Rng;
import org.netbeans.modules.jshell.parsing.JShellParser2;

/**
 *
 * @author sdedic
 */
public class ContentParserTest extends NbTestCase {
    
    public ContentParserTest(String name) {
        super(name);
    }
    
    private JShell state;

//    public static Test suite() {
//        return NbModuleSuite.createConfiguration(ContentParserTest.class).enableModules(".*").clusters("java").gui(false).suite();
//    }
    
    @Override
    protected void setUp() throws Exception {
        ConsoleModel.initModel();
        super.setUp(); 
        state = createJShell();
    }

    @Override
    protected void tearDown() throws Exception {
        state.close();
        super.tearDown();
    }
    
    private JShell createJShell() throws Exception {
        File jarFile = new File(JShell.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        assertTrue(jarFile.exists() && jarFile.isFile());
        return JShell.builder().remoteVMOptions("-classpath", jarFile.toPath().toString()).build();
    }
    
    private void parseOutput() throws Exception {
        StringBuilder sb = new StringBuilder();
        try (InputStream istm = ContentParserTest.class.getResourceAsStream("reploutput.txt");
             InputStreamReader r1 = new InputStreamReader(istm);
                BufferedReader br = new BufferedReader(r1)) {
            String s;
            
            while ((s = br.readLine()) != null) {
                sb.append(s).append("\n");
            }
        }
        
        JShellParser2 parser = new JShellParser2(state, sb, 0);
        this.content = sb.toString();
        parser.execute();
        this.parser = parser;
    }
    
    private String content;
    
    private JShellParser2 parser;
    
    public void testParsedSections() throws Exception {
        parseOutput();
        Iterator<ConsoleSection> sects = parser.sections().iterator();
        try (InputStream istm = ContentParserTest.class.getResourceAsStream("reploutput_golden.txt");
                InputStreamReader r1 = new InputStreamReader(istm);
                BufferedReader br = new BufferedReader(r1)) {
            String s;
            ConsoleSection current = null;
            int pos = 0;
            int next = 0;
            org.netbeans.modules.jshell.model.ConsoleSection.Type lastType = null;
            
            while ((s = br.readLine()) != null) {
                pos = next;
                next = pos + s.length() + 1;
                if (s.trim().isEmpty()) {
                    continue;
                }
                if (s.charAt(0) == 'x') {
                    assertNotNull(current);
                    assertEquals(lastType, current.getType());
                } else {
                    switch (s.charAt(0)) {
                        case 'M':
                            lastType = org.netbeans.modules.jshell.model.ConsoleSection.Type.MESSAGE;
                            break;
                        case 'O':
                            lastType = org.netbeans.modules.jshell.model.ConsoleSection.Type.OUTPUT;
                            break;
                        case 'I':
                            lastType = org.netbeans.modules.jshell.model.ConsoleSection.Type.JAVA;
                            break;
                        case 'C':
                            lastType = org.netbeans.modules.jshell.model.ConsoleSection.Type.COMMAND;
                            break;
                        default:
                            fail();
                    }
                    current = sects.next();
                    assertEquals("Invalid section: " + s  + ", section: " + current, lastType, current.getType());
                }
                if (s.charAt(1) != ':') {
                    assertTrue(current.isIncomplete());
                }
            }
        }
    }
    
    Iterator<Rng>    ranges = null;
    Rng curRange = null;
    
    /**
     * X.
     * Note: back apostrophes stand for a space/whitechar.
     * 
     * @throws Exception 
     */
    public void testLineSnippets() throws Exception {
        parseOutput();
        Iterator<ConsoleSection> sects = parser.sections().iterator();
        try (InputStream istm = ContentParserTest.class.getResourceAsStream("reploutput_golden.txt");
                InputStreamReader r1 = new InputStreamReader(istm);
                BufferedReader br = new BufferedReader(r1)) {
            String s;
            ConsoleSection current = null;
            int pos = 0;
            int next = 0;
            org.netbeans.modules.jshell.model.ConsoleSection.Type lastType = null;
            while ((s = br.readLine()) != null) {
                pos = next;
                int start = s.indexOf(':') + 1;
                int len = s.length() - start + 1; // count the newline
                
                if (s.charAt(0) != 'x') {
                    if (current != null) {
                        // check that all snippets have been eaten up
                        assertFalse(ranges.hasNext());
                        assertNull(curRange);
                        assertEquals(pos, current.getStart() + current.getLen());
                    }
                    current = sects.next();
                    ranges = Arrays.asList(current.getPartRanges()).iterator();
                    assertEquals(pos, current.getStart());
                }
                
                int snipStart = s.indexOf('`', start);
                if (snipStart == -1) {
                    checkCurrentRange(
                            pos,
                            pos + len
                    );
                    next = pos + len;
                    continue;
                }
                
                int passed = snipStart - start;
                int nextSnippet = s.indexOf('`', snipStart + 1);
                int e = pos + s.length() - start + 1;
                e--; // for the first `
                if (nextSnippet != -1) {
                    int ss = pos + passed/* + 1 */;
                    int cnt = 0;
                    do {
                        e--; // additional `
                        cnt++;
                        int sl = nextSnippet - snipStart - 1;
                        checkCurrentRange(
                            ss,
                            ss + sl
                        );
                        snipStart = nextSnippet;
                        nextSnippet = s.indexOf('`', snipStart + 1);
                    } while (nextSnippet != -1);
                    passed = snipStart - start - cnt;
                }
                checkCurrentRange(
                        pos + passed,
                        e
                );
                next = e;
            }
        }
            
    }
    
    private void checkCurrentRange(int chkS, int chkE) {
        if (curRange == null) {
            curRange = ranges.next();
        }
        assertTrue("s = " + curRange.start + ", golden = " + chkS, chkS >= curRange.start);
        assertTrue("e = " + curRange.end + ", golden = " + chkE, chkE <= curRange.end);
        if (curRange.end == chkE) {
            curRange = null;
        }
    }
}
