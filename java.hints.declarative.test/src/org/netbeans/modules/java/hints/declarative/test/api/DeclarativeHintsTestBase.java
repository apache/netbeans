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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.declarative.test.api;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.hints.declarative.test.TestParser;
import org.netbeans.modules.java.hints.declarative.test.TestParser.TestCase;
import org.netbeans.modules.java.hints.declarative.test.TestPerformer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author lahvac
 */
public class DeclarativeHintsTestBase extends NbTestCase {

    private final FileObject hintFile;
    private final FileObject testFile;
    private final TestCase test;

    public DeclarativeHintsTestBase() {
        super(null);
        throw new IllegalStateException();
    }

    public DeclarativeHintsTestBase(FileObject hintFile, FileObject testFile, TestCase test) {
        super(FileUtil.getFileDisplayName(testFile) + "/" + test.getName());
        this.hintFile = hintFile;
        this.testFile = testFile;
        this.test = test;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        super.setUp();
    }

    public static TestSuite suite(Class<?> clazz) {
        return suite(clazz, ".*");
    }

    public static TestSuite suite(Class<?> clazz, String filePattern) {
        NbTestSuite result = new NbTestSuite();
        Pattern patt = Pattern.compile(filePattern);

        for (String test : listTests(clazz)) {
            if (!patt.matcher(test).matches()) {
                continue;
            }
            
            //TODO:
            URL testURL = clazz.getClassLoader().getResource(test);
            
            assertNotNull(testURL);

            FileObject testFO = URLMapper.findFileObject(testURL);

            assertNotNull(testFO);

            String hint = test.substring(0, test.length() - ".test".length()) + ".hint";
            URL hintURL = clazz.getClassLoader().getResource(hint);

            assertNotNull(hintURL);
            
            FileObject hintFO = URLMapper.findFileObject(hintURL);

            assertNotNull(hintFO);

            try {
                for (TestCase tc : TestParser.parse(testFO.asText("UTF-8"))) {
                    result.addTest(new DeclarativeHintsTestBase(hintFO, testFO, tc));
                }
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }

        return result;
    }

    @Override
    protected void runTest() throws Throwable {
        Map<TestCase, Collection<String>> result = TestPerformer.performTest(hintFile, testFile, new TestCase[]{test}, new AtomicBoolean());
        
        assert result != null;
        
        Collection<String> actualResults = result.get(test);

        assertNotNull(actualResults);
        assertEquals(Arrays.asList(test.getResults()), actualResults);
    }

    private static Collection<String> listTests(Class<?> clazz) {
        File dirOrArchive = FileUtil.archiveOrDirForURL(clazz.getProtectionDomain().getCodeSource().getLocation());

        assertTrue(dirOrArchive.exists());

        if (dirOrArchive.isFile()) {
            return listTestsFromJar(dirOrArchive);
        } else {
            Collection<String> result = new LinkedList<String>();

            listTestsFromFilesystem(dirOrArchive, "", result);

            return result;
        }
    }
    
    private static Collection<String> listTestsFromJar(File archive) {
        Collection<String> result = new LinkedList<String>();

        try {
            JarFile jf = new JarFile(archive);
            Enumeration<JarEntry> entries = jf.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                if (entry.getName().endsWith(".test")) {
                    result.add(entry.getName());
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        
        return result;
    }

    private static void listTestsFromFilesystem(File file, String prefix, Collection<String> output) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                listTestsFromFilesystem(f, (prefix.length() > 0 ? (prefix + "/") : "") + f.getName(), output);
            }
        } else {
            if (file.getName().endsWith(".test")) {
                output.add(prefix);
            }
        }
    }
}
