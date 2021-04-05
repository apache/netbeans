/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.analysis.parsers;

import java.io.File;
import java.util.List;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.analysis.results.Result;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class PHPStanReportParserTest extends NbTestCase {

    public PHPStanReportParserTest(String name) {
        super(name);
    }

    public void testParse() throws Exception {
        FileObject root = getDataDir("phpstan/PHPStanSupport");
        FileObject workDir = root;
        List<Result> results = PHPStanReportParser.parse(getLogFile("phpstan-log.xml"), root, workDir);
        assertNotNull(results);

        assertEquals(4, results.size());
        Result result = results.get(0);
        assertEquals(FileUtil.toFile(root.getFileObject("HelloWorld.php")).getAbsolutePath(), result.getFilePath());
        assertEquals(5, result.getLine());
        assertEquals("error: Parameter $date of method HelloWorld::sayHello() has invalid typehint type DateTimeImutable.", result.getCategory());
        assertEquals("Parameter $date of method HelloWorld::sayHello() has invalid typehint type DateTimeImutable.", result.getDescription());


        result = results.get(2);
        assertEquals(FileUtil.toFile(root.getFileObject("vendor/nette/php-generator/src/PhpGenerator/Traits/CommentAware.php")).getAbsolutePath(), result.getFilePath());
        assertEquals(28, result.getLine());
        assertEquals("error: Casting to string something that's already string.", result.getCategory());
        assertEquals("Casting to string something that's already string.", result.getDescription());

        result = results.get(3);
        assertEquals(FileUtil.toFile(root.getFileObject("vendor/nikic/php-parser/test/PhpParser/Builder/ClassTest.php")).getAbsolutePath(), result.getFilePath());
        assertEquals(1, result.getLine());
        assertEquals("error: Class PhpParser\\Builder\\ClassTest was not found while trying to analyse it - autoloading is probably not configured properly.", result.getCategory());
        assertEquals("Class PhpParser\\Builder\\ClassTest was not found while trying to analyse it - autoloading is probably not configured properly.", result.getDescription());
    }

    public void testParseWithOtherOutput() throws Exception {
        FileObject root = getDataDir("phpstan/PHPStanSupport");
        FileObject workDir = root;
        List<Result> results = PHPStanReportParser.parse(getLogFile("phpstan-log-with-other-output.xml"), root, workDir);
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    public void testParseNetBeans3022() throws Exception {
        FileObject root = getDataDir("phpstan/PHPStanSupport/netbeans3022");
        FileObject workDir = getDataDir("phpstan/PHPStanSupport");
        List<Result> results = PHPStanReportParser.parse(getLogFile("phpstan-log-netbeans-3022.xml"), root, workDir);
        assertNotNull(results);
        assertEquals(3, results.size());
    }

    public void testParseNetBeans3022Win() throws Exception {
        FileObject root = getDataDir("phpstan/PHPStanSupport/netbeans3022");
        FileObject workDir = getDataDir("phpstan/PHPStanSupport");
        List<Result> results = PHPStanReportParser.parse(getLogFile("phpstan-log-netbeans-3022-win.xml"), root, workDir);
        assertNotNull(results);
        assertEquals(3, results.size());
    }

    public void testParseNetBeans3022WithoutWorkDir() throws Exception {
        FileObject root = getDataDir("phpstan/PHPStanSupport/netbeans3022");
        FileObject workDir = null;
        List<Result> results = PHPStanReportParser.parse(getLogFile("phpstan-log-netbeans-3022-without-workdir.xml"), root, workDir);
        assertNotNull(results);
        assertEquals(3, results.size());
    }

    private File getLogFile(String name) throws Exception {
        assertNotNull(name);
        File phpstan = new File(getDataDir(), "phpstan");
        File xmlLog = new File(phpstan, name);
        assertTrue(xmlLog.isFile());
        return xmlLog;
    }

    private FileObject getDataDir(String name) {
        assertNotNull(name);
        FileObject dataDir = FileUtil.toFileObject(getDataDir());
        return dataDir.getFileObject(name);
    }

}
