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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.analysis.results.Result;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class CheckStyleReportParserTest extends NbTestCase {

    public CheckStyleReportParserTest(String name) {
        super(name);
    }

    public void testParse() throws Exception {
        FileObject root = getDataDir("phpstan/PHPStanSupport");
        FileObject workDir = root;
        List<Result> results = CheckStyleReportParser.parse(getLogFile("phpstan-log.xml"), root, workDir);
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
        List<Result> results = CheckStyleReportParser.parse(getLogFile("phpstan-log-with-other-output.xml"), root, workDir);
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    public void testParseNetBeans3022() throws Exception {
        FileObject root = getDataDir("phpstan/PHPStanSupport/netbeans3022");
        FileObject workDir = getDataDir("phpstan/PHPStanSupport");
        List<Result> results = CheckStyleReportParser.parse(getLogFile("phpstan-log-netbeans-3022.xml"), root, workDir);
        assertNotNull(results);
        assertEquals(3, results.size());
    }

    public void testParseNetBeans3022Win() throws Exception {
        FileObject root = getDataDir("phpstan/PHPStanSupport/netbeans3022");
        FileObject workDir = getDataDir("phpstan/PHPStanSupport");
        List<Result> results = CheckStyleReportParser.parse(getLogFile("phpstan-log-netbeans-3022-win.xml"), root, workDir);
        assertNotNull(results);
        assertEquals(3, results.size());
    }

    public void testParseNetBeans3022WithoutWorkDir() throws Exception {
        FileObject root = getDataDir("phpstan/PHPStanSupport/netbeans3022");
        FileObject workDir = null;
        File logFile = getLogFile("phpstan-log-netbeans-3022-without-workdir.xml");
        fixContent(logFile);
        List<Result> results = CheckStyleReportParser.parse(logFile, root, workDir);
        assertNotNull(results);
        assertEquals(3, results.size());
    }

    public void testParseWithHtmlEntities() throws Exception {
        FileObject root = getDataDir("phpstan/PHPStanSupport");
        FileObject workDir = root;
        List<Result> results = CheckStyleReportParser.parse(getLogFile("phpstan-log-html-entities.xml"), root, workDir);
        assertNotNull(results);

        assertEquals(1, results.size());
        Result result = results.get(0);
        assertEquals(FileUtil.toFile(root.getFileObject("HelloWorld.php")).getAbsolutePath(), result.getFilePath());
        assertEquals(7, result.getLine());
        assertEquals("error: Function count() should return int but returns array<string>.", result.getCategory());
        assertEquals("Function count() should return int but returns array&lt;string&gt;.", result.getDescription());
    }

    public void testParseWithHtmlErrorIdentifiers() throws Exception {
        FileObject root = getDataDir("phpstan/PHPStanSupport");
        FileObject workDir = root;
        List<Result> results = CheckStyleReportParser.parse(getLogFile("phpstan-log-with-error-identifiers.xml"), root, workDir);
        assertNotNull(results);

        assertEquals(2, results.size());

        Result result = results.get(0);
        assertEquals(FileUtil.toFile(root.getFileObject("HelloWorld.php")).getAbsolutePath(), result.getFilePath());
        assertEquals(8, result.getLine());
        assertEquals("error: nullCoalesce.expr: Expression on left side of ?? is not nullable.", result.getCategory());
        assertEquals("nullCoalesce.expr: Expression on left side of ?? is not nullable.", result.getDescription());

        result = results.get(1);
        assertEquals(FileUtil.toFile(root.getFileObject("HelloWorld.php")).getAbsolutePath(), result.getFilePath());
        assertEquals(13, result.getLine());
        assertEquals("error: return.missing: Method HelloWorld::readLength() should return float but return statement is missing.", result.getCategory());
        assertEquals("return.missing: Method HelloWorld::readLength() should return float but return statement is missing.", result.getDescription());
    }

    public void testPsalmParse() throws Exception {
        FileObject root = getDataDir("psalm/PsalmSupport");
        FileObject workDir = root;
        List<Result> results = CheckStyleReportParser.parse(getPsalmLogFile("nb-php-psalm-log.xml"), root, workDir);
        assertNotNull(results);

        assertEquals(40, results.size());
        Result result = results.get(0);
        assertEquals(FileUtil.toFile(root.getFileObject("src/Calculator.php")).getAbsolutePath(), result.getFilePath());
        assertEquals(32, result.getLine());
        assertEquals("error: MissingReturnType: Method Calculator::plus does not have a return type", result.getCategory());
        assertEquals("MissingReturnType: Method Calculator::plus does not have a return type", result.getDescription());

        result = results.get(23);
        assertEquals(FileUtil.toFile(root.getFileObject("test/src/CalculatorTest.php")).getAbsolutePath(), result.getFilePath());
        assertEquals(46, result.getLine());
        assertEquals("error: MissingReturnType: Method CalculatorTest::testPlus does not have a return type, expecting void", result.getCategory());
        assertEquals("MissingReturnType: Method CalculatorTest::testPlus does not have a return type, expecting void", result.getDescription());
    }

    private File getLogFile(String name) throws Exception {
        assertNotNull(name);
        File phpstan = new File(getDataDir(), "phpstan");
        File xmlLog = new File(phpstan, name);
        assertTrue(xmlLog.isFile());
        return xmlLog;
    }

    private File getPsalmLogFile(String name) throws Exception {
        assertNotNull(name);
        File psalm = new File(getDataDir(), "psalm");
        File xmlLog = new File(psalm, name);
        assertTrue(xmlLog.isFile());
        return xmlLog;
    }

    private FileObject getDataDir(String name) {
        assertNotNull(name);
        FileObject dataDir = FileUtil.toFileObject(getDataDir());
        return dataDir.getFileObject(name);
    }

    private void fixContent(File file) throws Exception {
        Path path = file.toPath();
        Charset charset = StandardCharsets.UTF_8;
        String content = new String(Files.readAllBytes(path), charset);
        content = content.replace("%WORKDIR%", getDataDir().getAbsolutePath());
        Files.write(path, content.getBytes(charset));
    }

}
