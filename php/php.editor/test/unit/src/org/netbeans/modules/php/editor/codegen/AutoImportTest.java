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
package org.netbeans.modules.php.editor.codegen;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.text.Document;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.editor.indent.CodeStyle;
import org.netbeans.modules.php.editor.indent.FmtOptions;
import org.netbeans.modules.php.editor.model.UseScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class AutoImportTest extends PHPTestBase {

    public AutoImportTest(String testName) {
        super(testName);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), getTestFolderPath()))
            })
        );
    }

    public void testInSameNamespace01_Type() throws Exception {
        insertTest("^// test", "Same\\NS\\Test\\Class1", UseScope.Type.TYPE);
    }

    public void testNoExistingUses01_Type() throws Exception {
        insertTest("^// test", "In\\GlobalNS\\Class1", UseScope.Type.TYPE);
    }

    public void testNoExistingUses01_Function() throws Exception {
        insertTest("^// test", "In\\GlobalNS\\function1", UseScope.Type.FUNCTION);
    }

    public void testNoExistingUses01_CONST() throws Exception {
        insertTest("^// test", "In\\GlobalNS\\const1", UseScope.Type.CONST);
    }

    public void testNoExistingUses02_Type() throws Exception {
        insertTest("^// test", "In\\NS\\Class1", UseScope.Type.TYPE);
    }

    public void testNoExistingUses02_Function() throws Exception {
        insertTest("^// test", "In\\NS\\function1", UseScope.Type.FUNCTION);
    }

    public void testNoExistingUses02_CONST() throws Exception {
        insertTest("^// test", "In\\NS\\const1", UseScope.Type.CONST);
    }

    public void testNoExistingUses03_Type() throws Exception {
        insertTest("^// test", "In\\NS\\Class1", UseScope.Type.TYPE);
    }

    public void testNoExistingUses04a_Type() throws Exception {
        insertTest("^// test", "In\\NS\\Class1", UseScope.Type.TYPE);
    }

    public void testNoExistingUses04b_Type() throws Exception {
        insertTest("^// test", "In\\NS\\Class1", UseScope.Type.TYPE);
    }

    public void testNoExistingUses05_Type() throws Exception {
        insertTest("^// test", "In\\NS\\Class1", UseScope.Type.TYPE);
    }

    public void testNoExistingUses06a_Type() throws Exception {
        insertTest("^// test", "In\\NS\\Class1", UseScope.Type.TYPE);
    }

    public void testNoExistingUses06b_Type() throws Exception {
        insertTest("^// test", "In\\NS\\Class1", UseScope.Type.TYPE);
    }

    public void testNoExistingUses07_Type() throws Exception {
        insertTest("^// test", "PhpDoc\\Attributes\\Class1", UseScope.Type.TYPE);
    }

    public void testNoExistingUses08_Type() throws Exception {
        insertTest("^// test", "Comment\\LineComment\\Class1", UseScope.Type.TYPE);
    }

    public void testNoExistingUses09_Type() throws Exception {
        insertTest("^// test", "Comment\\LineComment\\Class1", UseScope.Type.TYPE);
    }

    public void testNoExistingUsesWithDeclare01a_Type() throws Exception {
        insertTest("^// test", "In\\NS\\Class1", UseScope.Type.TYPE);
    }

    public void testNoExistingUsesWithDeclare01b_Type() throws Exception {
        insertTest("^// test", "In\\NS\\Class1", UseScope.Type.TYPE);
    }

    public void testNoExistingUsesWithDeclare02a_Type() throws Exception {
        insertTest("^// test", "In\\NS\\Class1", UseScope.Type.TYPE);
    }

    public void testNoExistingUsesWithDeclare02b_Type() throws Exception {
        insertTest("^// test", "In\\NS\\Class1", UseScope.Type.TYPE);
    }

    public void testNoExistingUsesWithDeclare02c_Type() throws Exception {
        insertTest("^// test", "In\\NS\\Class1", UseScope.Type.TYPE);
    }

    public void testNoExistingUsesWithDeclare03_Type() throws Exception {
        insertTest("^// test", "In\\NS\\Class1", UseScope.Type.TYPE);
    }

    public void testNoExistingUsesWithDeclare04_Function() throws Exception {
        insertTest("^// test", "In\\NS\\function1", UseScope.Type.FUNCTION);
    }

    public void testTypeInGlobal01_01() throws Exception {
        insertTest("^// test", "Class1", UseScope.Type.TYPE);
    }

    public void testTypeInGlobal01_02() throws Exception {
        insertTest("^// test", "Vendor\\Pacage\\Class1", UseScope.Type.TYPE);
    }

    public void testTypeInGlobal02_01() throws Exception {
        insertTest("^// test", "Class1", UseScope.Type.TYPE);
    }

    public void testTypeInGlobal02_02() throws Exception {
        insertTest("^// test", "Vendor\\Pacage\\Class1", UseScope.Type.TYPE);
    }

    public void testTypeInGlobalWithBlock01_01() throws Exception {
        insertTest("^// test", "Class1", UseScope.Type.TYPE);
    }

    public void testTypeInGlobalWithBlock01_02() throws Exception {
        insertTest("^// test", "Vendor\\Package\\Class1", UseScope.Type.TYPE);
    }

    // only type singe uses
    public void testSingleUsesT01_Type01() throws Exception {
        insertTest("^// test", "Vendor\\Package\\Class1", UseScope.Type.TYPE);
    }

    public void testSingleUsesT01_Type02() throws Exception {
        insertTest("^// test", "Single\\Uses\\Class1", UseScope.Type.TYPE);
    }

    public void testSingleUsesT01_Type03() throws Exception {
        insertTest("^// test", "Single\\Uses\\Type0", UseScope.Type.TYPE);
    }

    public void testSingleUsesT01_Type04() throws Exception {
        insertTest("^// test", "Single\\Uses02\\Type0", UseScope.Type.TYPE);
    }

    public void testSingleUsesT01_Function01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function1", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesT01_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function1", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesT01_Function02a() throws Exception {
        insertTest("^// test", "Single\\Uses\\Type0", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesT01_Function02b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\Type0", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesT01_Const01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONST01", UseScope.Type.CONST);
    }

    public void testSingleUsesT01_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\CONST01", UseScope.Type.CONST, option);
    }

    public void testSingleUsesT01_Const02a() throws Exception {
        insertTest("^// test", "Single\\Uses\\Type0", UseScope.Type.CONST);
    }

    public void testSingleUsesT01_Const02b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\Type0", UseScope.Type.CONST, option);
    }

    public void testSingleUsesT02_Type01() throws Exception {
        insertTest("^// test", "Single\\Uses\\Type2", UseScope.Type.TYPE);
    }

    public void testSingleUsesT02_Type02() throws Exception {
        insertTest("^// test", "Single\\Uses\\Type6", UseScope.Type.TYPE);
    }

    public void testSingleUsesT02_Type03() throws Exception {
        insertTest("^// test", "Single\\Type1", UseScope.Type.TYPE);
    }

    public void testSingleUsesT02_Type04() throws Exception {
        insertTest("^// test", "Type1", UseScope.Type.TYPE);
    }

    public void testSingleUsesT02_Function01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesT02_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesT02_Const01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONST01", UseScope.Type.CONST);
    }

    public void testSingleUsesT02_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\CONST01", UseScope.Type.CONST, option);
    }

    public void testSingleUsesT03_Type01() throws Exception {
        insertTest("^// test", "Single\\Uses\\Type3", UseScope.Type.TYPE);
    }

    public void testSingleUsesT03_Function01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesT03_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesT03_Const01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONST01", UseScope.Type.CONST);
    }

    public void testSingleUsesT03_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\CONST01", UseScope.Type.CONST, option);
    }

    // only function singe uses
    public void testSingleUsesF01_Type01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\Type1", UseScope.Type.TYPE);
    }

    public void testSingleUsesF01_Type01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\Type1", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesF01_Function01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesF01_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesF01_Function02() throws Exception {
        insertTest("^// test", "Single\\function02", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesF01_Function03() throws Exception {
        insertTest("^// test", "Single\\Uses\\function1", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesF01_Const01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONST01", UseScope.Type.CONST);
    }

    public void testSingleUsesF01_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\CONST01", UseScope.Type.CONST, option);
    }

    public void testSingleUsesF02_Type01a() throws Exception {
        insertTest("^// test", "Single\\Type1", UseScope.Type.TYPE);
    }

    public void testSingleUsesF02_Type01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Type1", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesF02_Function01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesF02_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesF02_Function02a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesF02_Function02b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesF02_Function03() throws Exception {
        insertTest("^// test", "Single\\Uses\\function02", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesF02_Function04() throws Exception {
        insertTest("^// test", "function02", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesF02_Function05() throws Exception {
        insertTest("^// test", "Single\\test", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesF02_Function06() throws Exception {
        insertTest("^// test", "functions", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesF02_Function07() throws Exception {
        insertTest("^// test", "Single\\function02", UseScope.Type.FUNCTION);
    }

//    public void testSingleUsesF02_Function06b() throws Exception {
//        Option option = new Option()
//                .isPSR12(false)
//                .hasBlankLineBetweenUseTypes(true);
//        insertTest("^// test", "functions", UseScope.Type.FUNCTION, option);
//    }
//
//    public void testSingleUsesF02_Function06c() throws Exception {
//        Option option = new Option()
//                .isPSR12(true)
//                .hasBlankLineBetweenUseTypes(true);
//        insertTest("^// test", "functions", UseScope.Type.FUNCTION, option);
//    }

    public void testSingleUsesF02_Const01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONST01", UseScope.Type.CONST);
    }

    public void testSingleUsesF02_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\CONST01", UseScope.Type.CONST, option);
    }

    public void testSingleUsesF02_Const01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\CONST01", UseScope.Type.CONST, option);
    }

    public void testSingleUsesF03_Type01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.TYPE);
    }

    public void testSingleUsesF03_Type01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesF03_Function01() throws Exception {
        insertTest("^// test", "Single\\Uses\\function02", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesF03_Function02() throws Exception {
        insertTest("^// test", "Single\\Uses\\function05", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesF03_Function03() throws Exception {
        insertTest("^// test", "Single\\Uses\\function06", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesF03_Const01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.CONST);
    }

    public void testSingleUsesF03_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.CONST, option);
    }

    public void testSingleUsesF03_Const01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.CONST, option);
    }

    // only const singe uses
    public void testSingleUsesC01_Type01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\Type1", UseScope.Type.TYPE);
    }

    public void testSingleUsesC01_Type01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\Type1", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesC01_Function01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesC01_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesC01_Function01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesC01_Const01() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONST01", UseScope.Type.CONST);
    }

    public void testSingleUsesC01_Const02() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONST1", UseScope.Type.CONST);
    }

    public void testSingleUsesC01_Const03() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONST12", UseScope.Type.CONST);
    }

    public void testSingleUsesC01_Const04() throws Exception {
        insertTest("^// test", "CONSTANT", UseScope.Type.CONST);
    }

    public void testSingleUsesC02_Type01a() throws Exception {
        insertTest("^// test", "Single\\Type01", UseScope.Type.TYPE);
    }

    public void testSingleUsesC02_Type01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Type01", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesC02_Function01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesC02_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesC02_Function01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesC02_Function02a() throws Exception {
        insertTest("^// test", "count", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesC02_Function02b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "count", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesC02_Function02c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "count", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesC02_Const01() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONST01", UseScope.Type.CONST);
    }

    public void testSingleUsesC02_Const02() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONST02", UseScope.Type.CONST);
    }

    public void testSingleUsesC02_Const03() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONST03", UseScope.Type.CONST);
    }

    public void testSingleUsesC02_Const04() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONST05", UseScope.Type.CONST);
    }

    public void testSingleUsesC02_Const05() throws Exception {
        insertTest("^// test", "constant", UseScope.Type.CONST);
    }

    public void testSingleUsesC03_Type01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONST02", UseScope.Type.TYPE);
    }

    public void testSingleUsesC03_Type01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\CONST02", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesC03_Function01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesC03_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesC03_Function01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesC03_Function02() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONST01", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesC03_Const01() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONST02", UseScope.Type.CONST);
    }

    public void testSingleUsesC03_Const02() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONST03", UseScope.Type.CONST);
    }

    public void testSingleUsesTF01_Type01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\Type02", UseScope.Type.TYPE);
    }

    public void testSingleUsesTF01_Type01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\Type02", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesTF01_Type01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\Type02", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesTF01_Type02a() throws Exception {
        insertTest("^// test", "Single\\Uses\\Type00", UseScope.Type.TYPE);
    }

    public void testSingleUsesTF01_Type02b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\Type00", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesTF01_Type02c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\Type00", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesTF01_Function01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesTF01_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTF01_Function01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTF01_Function02a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function02", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesTF01_Function02b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function02", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTF01_Function02c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function02", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTF01_Function03() throws Exception {
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesTF01_Const01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\const01", UseScope.Type.CONST);
    }

    public void testSingleUsesTF01_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\const01", UseScope.Type.CONST, option);
    }

    public void testSingleUsesTF01_Const01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\const01", UseScope.Type.CONST, option);
    }

    public void testSingleUsesTF01_Const01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\const01", UseScope.Type.CONST, option);
    }

    public void testSingleUsesTF02_Type01() throws Exception {
        insertTest("^// test", "Single\\Uses\\Type00", UseScope.Type.TYPE);
    }

    public void testSingleUsesTF02_Type02() throws Exception {
        insertTest("^// test", "Single\\Uses\\Type01", UseScope.Type.TYPE);
    }

    public void testSingleUsesTF02_Type03a() throws Exception {
        insertTest("^// test", "Single\\Uses\\Type02", UseScope.Type.TYPE);
    }

    public void testSingleUsesTF02_Type03b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\Type02", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesTF02_Type03c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\Type02", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesTF02_Type03d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\Type02", UseScope.Type.TYPE, option);
    }


    public void testSingleUsesTF02_Function01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesTF02_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTF02_Function01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTF02_Function01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTF02_Function02a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function02", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesTF02_Function02b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function02", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTF02_Function02c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\function02", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTF02_Function02d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function02", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTF02_Function03() throws Exception {
        insertTest("^// test", "Single\\Uses\\function04", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesTF02_Function04() throws Exception {
        insertTest("^// test", "Single\\Uses\\function06", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesTF02_Const01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\const01", UseScope.Type.CONST);
    }

    public void testSingleUsesTF02_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\const01", UseScope.Type.CONST, option);
    }

    public void testSingleUsesTF02_Const01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\const01", UseScope.Type.CONST, option);
    }

    public void testSingleUsesTF02_Const01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\const01", UseScope.Type.CONST, option);
    }

    public void testSingleUsesTC01_Type01() throws Exception {
        insertTest("^// test", "Single\\Uses\\type00", UseScope.Type.TYPE);
    }

    public void testSingleUsesTC01_Type02() throws Exception {
        insertTest("^// test", "Single\\Uses\\type01", UseScope.Type.TYPE);
    }

    public void testSingleUsesTC01_Type03a() throws Exception {
        insertTest("^// test", "Single\\Uses\\type02", UseScope.Type.TYPE);
    }

    public void testSingleUsesTC01_Type03b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\type02", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesTC01_Type03c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\type02", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesTC01_Type03d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\type02", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesTC01_Function01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesTC01_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTC01_Function01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTC01_Function01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTC01_Const01() throws Exception {
        insertTest("^// test", "Single\\Uses\\const01", UseScope.Type.CONST);
    }

    public void testSingleUsesTC01_Const02a() throws Exception {
        insertTest("^// test", "Single\\Uses\\const00", UseScope.Type.CONST);
    }

    public void testSingleUsesTC01_Const02b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\const00", UseScope.Type.CONST, option);
    }

    public void testSingleUsesTC01_Const02c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\const00", UseScope.Type.CONST, option);
    }

    public void testSingleUsesTC01_Const02d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\const00", UseScope.Type.CONST, option);
    }

    public void testSingleUsesTC01_Const03() throws Exception {
        insertTest("^// test", "Single\\Uses\\const03", UseScope.Type.CONST);
    }

    public void testSingleUsesTC02_Type01() throws Exception {
        insertTest("^// test", "Single\\Uses\\type00", UseScope.Type.TYPE);
    }

    public void testSingleUsesTC02_Type02() throws Exception {
        insertTest("^// test", "Single\\Uses\\type05", UseScope.Type.TYPE);
    }

    public void testSingleUsesTC02_Type03a() throws Exception {
        insertTest("^// test", "Single\\Uses\\type06", UseScope.Type.TYPE);
    }

    public void testSingleUsesTC02_Type03b() throws Exception {
                Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\type06", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesTC02_Type03c() throws Exception {
                Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\type06", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesTC02_Type03d() throws Exception {
                Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\type06", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesTC02_Function01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesTC02_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTC02_Function01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTC02_Function01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTC02_Const01() throws Exception {
        insertTest("^// test", "Single\\Uses\\const01", UseScope.Type.CONST);
    }

    public void testSingleUsesTC02_Const02a() throws Exception {
        insertTest("^// test", "Single\\Uses\\const00", UseScope.Type.CONST);
    }

    public void testSingleUsesTC02_Const02b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\const00", UseScope.Type.CONST, option);
    }

    public void testSingleUsesTC02_Const02c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\const00", UseScope.Type.CONST, option);
    }

    public void testSingleUsesTC02_Const02d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\const00", UseScope.Type.CONST, option);
    }

    public void testSingleUsesTC02_Const03() throws Exception {
        insertTest("^// test", "Single\\Uses\\const03", UseScope.Type.CONST);
    }

    public void testSingleUsesTC02_Const04a() throws Exception {
        insertTest("^// test", "Single\\Uses\\const06", UseScope.Type.CONST);
    }

    public void testSingleUsesTC02_Const04b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\const06", UseScope.Type.CONST, option);
    }

    public void testSingleUsesTC02_Const04c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\const06", UseScope.Type.CONST, option);
    }

    public void testSingleUsesTC02_Const04d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\const06", UseScope.Type.CONST, option);
    }

    public void testSingleUsesFC01_Type01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\type00", UseScope.Type.TYPE);
    }

    public void testSingleUsesFC01_Type01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\type00", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesFC01_Function01() throws Exception {
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesFC01_Function02a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function02", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesFC01_Function02b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function02", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesFC01_Function02c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\function02", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesFC01_Function02d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function02", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesFC01_Const01() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONSTANT01", UseScope.Type.CONST);
    }

    public void testSingleUsesFC01_Const02a() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONSTANT00", UseScope.Type.CONST);
    }

    public void testSingleUsesFC01_Const02b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\CONSTANT00", UseScope.Type.CONST, option);
    }

    public void testSingleUsesFC01_Const02c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\CONSTANT00", UseScope.Type.CONST, option);
    }

    public void testSingleUsesFC01_Const02d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\CONSTANT00", UseScope.Type.CONST, option);
    }

    public void testSingleUsesFC01_Const03a() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONSTANT02", UseScope.Type.CONST);
    }

    public void testSingleUsesFC01_Const03b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\CONSTANT02", UseScope.Type.CONST, option);
    }

    public void testSingleUsesFC01_Const03c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\CONSTANT02", UseScope.Type.CONST, option);
    }

    public void testSingleUsesFC01_Const03d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\CONSTANT02", UseScope.Type.CONST, option);
    }

    public void testSingleUsesFC02_Type01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\type00", UseScope.Type.TYPE);
    }

    public void testSingleUsesFC02_Type01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\type00", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesFC02_Function01() throws Exception {
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesFC02_Function02() throws Exception {
        insertTest("^// test", "Single\\Uses\\function02", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesFC02_Function03a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function04", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesFC02_Function03b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function04", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesFC02_Function03c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\function04", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesFC02_Function03d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function04", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesFC02_Const01() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONSTANT03", UseScope.Type.CONST);
    }

    public void testSingleUsesFC02_Const02a() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONSTANT00", UseScope.Type.CONST);
    }

    public void testSingleUsesFC02_Const02b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\CONSTANT00", UseScope.Type.CONST, option);
    }

    public void testSingleUsesFC02_Const02c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\CONSTANT00", UseScope.Type.CONST, option);
    }

    public void testSingleUsesFC02_Const02d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\CONSTANT00", UseScope.Type.CONST, option);
    }

    public void testSingleUsesFC02_Const03a() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONSTANT05", UseScope.Type.CONST);
    }

    public void testSingleUsesFC02_Const03b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\CONSTANT05", UseScope.Type.CONST, option);
    }

    public void testSingleUsesFC02_Const03c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\CONSTANT05", UseScope.Type.CONST, option);
    }

    public void testSingleUsesFC02_Const03d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\CONSTANT05", UseScope.Type.CONST, option);
    }

    public void testSingleUsesCF01_Type01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\type00", UseScope.Type.TYPE);
    }

    public void testSingleUsesCF01_Type01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\type00", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesCF01_Function01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesCF01_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesCF01_Function01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesCF01_Function01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesCF01_Function02a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function02", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesCF01_Function02b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function02", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesCF01_Function02c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\function02", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesCF01_Function02d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function02", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesCF01_Const01() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONSTANT01", UseScope.Type.CONST);
    }

    public void testSingleUsesCF01_Const02a() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONSTANT00", UseScope.Type.CONST);
    }

    public void testSingleUsesCF01_Const02b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\CONSTANT00", UseScope.Type.CONST, option);
    }

    public void testSingleUsesCF01_Const02c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\CONSTANT00", UseScope.Type.CONST, option);
    }

    public void testSingleUsesCF01_Const02d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\CONSTANT00", UseScope.Type.CONST, option);
    }

    public void testSingleUsesCF01_Const03a() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONSTANT02", UseScope.Type.CONST);
    }

    public void testSingleUsesCF01_Const03b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\CONSTANT02", UseScope.Type.CONST, option);
    }

    public void testSingleUsesCF01_Const03c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\CONSTANT02", UseScope.Type.CONST, option);
    }

    public void testSingleUsesCF01_Const03d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\CONSTANT02", UseScope.Type.CONST, option);
    }

    public void testSingleUsesCF02_Type01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\type00", UseScope.Type.TYPE);
    }

    public void testSingleUsesCF02_Type01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\type00", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesCF02_Function01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesCF02_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesCF02_Function01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesCF02_Function01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesCF02_Function02() throws Exception {
        insertTest("^// test", "Single\\Uses\\function02", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesCF02_Function03a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function04", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesCF02_Function03b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function04", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesCF02_Function03c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\function04", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesCF02_Function03d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function04", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesCF02_Const01() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONSTANT03", UseScope.Type.CONST);
    }

    public void testSingleUsesCF02_Const02a() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONSTANT00", UseScope.Type.CONST);
    }

    public void testSingleUsesCF02_Const02b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\CONSTANT00", UseScope.Type.CONST, option);
    }

    public void testSingleUsesCF02_Const02c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\CONSTANT00", UseScope.Type.CONST, option);
    }

    public void testSingleUsesCF02_Const02d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\CONSTANT00", UseScope.Type.CONST, option);
    }

    public void testSingleUsesCF02_Const03a() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONSTANT05", UseScope.Type.CONST);
    }

    public void testSingleUsesCF02_Const03b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\CONSTANT05", UseScope.Type.CONST, option);
    }

    public void testSingleUsesCF02_Const03c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\CONSTANT05", UseScope.Type.CONST, option);
    }

    public void testSingleUsesCF02_Const03d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\CONSTANT05", UseScope.Type.CONST, option);
    }

    public void testSingleUsesTFC01_Type01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\Type00", UseScope.Type.TYPE);
    }

    public void testSingleUsesTFC01_Type01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\Type00", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesTFC01_Type02() throws Exception {
        insertTest("^// test", "Single\\Uses\\Type02", UseScope.Type.TYPE);
    }

    public void testSingleUsesTFC01_Type03a() throws Exception {
        insertTest("^// test", "Single\\Uses\\Type06", UseScope.Type.TYPE);
    }

    public void testSingleUsesTFC01_Type03b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\Type06", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesTFC01_Function01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesTFC01_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTFC01_Function01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTFC01_Function01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTFC01_Function02a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesTFC01_Function02b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTFC01_Function02c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTFC01_Function02d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTFC01_Function03a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function03", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesTFC01_Function03b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function03", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTFC01_Function03c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\function03", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTFC01_Function03d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function03", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTFC01_Function04a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function05", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesTFC01_Function04b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function05", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTFC01_Function04c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\function05", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTFC01_Function04d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function05", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTFC01_Function05a() throws Exception {
        insertTest("^// test", "function00", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesTFC01_Function05b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTFC01_Function05c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTFC01_Function05d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesTFC01_Const01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\const00", UseScope.Type.CONST);
    }

    public void testSingleUsesTFC01_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\const00", UseScope.Type.CONST, option);
    }

    public void testSingleUsesTFC01_Const01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\const00", UseScope.Type.CONST, option);
    }

    public void testSingleUsesTFC01_Const01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\const00", UseScope.Type.CONST, option);
    }

    public void testSingleUsesTFC01_Const02() throws Exception {
        insertTest("^// test", "Single\\Uses\\Const00", UseScope.Type.CONST);
    }

    public void testSingleUsesTFC01_Const03() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONST00", UseScope.Type.CONST);
    }

    public void testSingleUsesTFC01_Const04a() throws Exception {
        insertTest("^// test", "Single\\Uses\\const02", UseScope.Type.CONST);
    }

    public void testSingleUsesTFC01_Const04b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\const02", UseScope.Type.CONST, option);
    }

    public void testSingleUsesTFC01_Const04c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\const02", UseScope.Type.CONST, option);
    }

    public void testSingleUsesTFC01_Const04d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\const02", UseScope.Type.CONST, option);
    }

    public void testSingleUsesFCT01_Type01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\Type00", UseScope.Type.TYPE);
    }

    public void testSingleUsesFCT01_Type01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\Type00", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesFCT01_Type01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\Type00", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesFCT01_Type01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\Type00", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesFCT01_Type02a() throws Exception {
        insertTest("^// test", "Single\\Uses\\Type06", UseScope.Type.TYPE);
    }

    public void testSingleUsesFCT01_Type02b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\Type06", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesFCT01_Function01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesFCT01_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesFCT01_Function01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesFCT01_Function01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesFCT01_Function02a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function05", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesFCT01_Function02b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function05", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesFCT01_Function02c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\function05", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesFCT01_Function02d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function05", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesFCT01_Function03() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function04", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesFCT01_Const01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\const00", UseScope.Type.CONST);
    }

    public void testSingleUsesFCT01_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\const00", UseScope.Type.CONST, option);
    }

    public void testSingleUsesFCT01_Const02a() throws Exception {
        insertTest("^// test", "const00", UseScope.Type.CONST);
    }

    public void testSingleUsesFCT01_Const02b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "const00", UseScope.Type.CONST, option);
    }

    public void testSingleUsesFCT01_Const03a() throws Exception {
        insertTest("^// test", "Single\\Uses\\const05", UseScope.Type.CONST);
    }

    public void testSingleUsesFCT01_Const03b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\const05", UseScope.Type.CONST, option);
    }

    public void testSingleUsesCFT01_Type01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\Type00", UseScope.Type.TYPE);
    }

    public void testSingleUsesCFT01_Type01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\Type00", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesCFT01_Type02a() throws Exception {
        insertTest("^// test", "type00", UseScope.Type.TYPE);
    }

    public void testSingleUsesCFT01_Type02b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "type00", UseScope.Type.TYPE, option);
    }

    public void testSingleUsesCFT01_Function01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesCFT01_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesCFT01_Function01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesCFT01_Function01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesCFT01_Function02a() throws Exception {
        insertTest("^// test", "Test\\function05", UseScope.Type.FUNCTION);
    }

    public void testSingleUsesCFT01_Function02b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Test\\function05", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesCFT01_Function02c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Test\\function05", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesCFT01_Function02d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Test\\function05", UseScope.Type.FUNCTION, option);
    }

    public void testSingleUsesCFT01_Const01a() throws Exception {
        insertTest("^// test", "Single\\Uses\\const00", UseScope.Type.CONST);
    }

    public void testSingleUsesCFT01_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\const00", UseScope.Type.CONST, option);
    }

    public void testSingleUsesCFT01_Const02a() throws Exception {
        insertTest("^// test", "Single\\Uses\\const03", UseScope.Type.CONST);
    }

    public void testSingleUsesCFT01_Const02b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Single\\Uses\\const03", UseScope.Type.CONST, option);
    }

    public void testSingleUsesCFT01_Const03() throws Exception {
        insertTest("^// test", "Single\\Uses\\CONST04", UseScope.Type.CONST);
    }

    public void testSingleUsesCFT01_Const04a() throws Exception {
        insertTest("^// test", "test\\Uses\\CONST04", UseScope.Type.CONST);
    }

    public void testSingleUsesCFT01_Const04b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "test\\Uses\\CONST04", UseScope.Type.CONST, option);
    }

    public void testSingleUsesCFT01_Const05a() throws Exception {
        insertTest("^// test", "CONST00", UseScope.Type.CONST);
    }

    public void testSingleUsesCFT01_Const05b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "CONST00", UseScope.Type.CONST, option);
    }

    // multiple
    public void testMultipleUsesT01_Type01() throws Exception {
        insertTest("^// test", "Multiple\\Uses01\\Type00", UseScope.Type.TYPE);
    }

    public void testMultipleUsesT01_Type02() throws Exception {
        insertTest("^// test", "Multiple\\Uses00\\Type01", UseScope.Type.TYPE);
    }

    public void testMultipleUsesT01_Type03() throws Exception {
        insertTest("^// test", "Multiple\\Uses01\\Type01", UseScope.Type.TYPE);
    }

    public void testMultipleUsesT01_Function01a() throws Exception {
        insertTest("^// test", "Multiple\\Uses01\\function00", UseScope.Type.FUNCTION);
    }

    public void testMultipleUsesT01_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Multiple\\Uses01\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testMultipleUsesT01_Const01a() throws Exception {
        insertTest("^// test", "Multiple\\Uses01\\const00", UseScope.Type.CONST);
    }

    public void testMultipleUsesT01_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Multiple\\Uses01\\const00", UseScope.Type.CONST, option);
    }

    public void testMultipleUsesT02_Type01() throws Exception {
        insertTest("^// test", "Multiple\\Uses01\\Type00", UseScope.Type.TYPE);
    }

    public void testMultipleUsesT02_Type02() throws Exception {
        insertTest("^// test", "Multiple\\Uses00\\Type01", UseScope.Type.TYPE);
    }

    public void testMultipleUsesT02_Type03() throws Exception {
        insertTest("^// test", "Multiple\\Uses01\\Type01", UseScope.Type.TYPE);
    }

    public void testMultipleUsesT02_Type04() throws Exception {
        insertTest("^// test", "Multiple\\Uses01\\Type04", UseScope.Type.TYPE);
    }

    public void testMultipleUsesT02_Type05() throws Exception {
        insertTest("^// test", "Multiple\\Uses01\\Type06", UseScope.Type.TYPE);
    }

    public void testMultipleUsesF01_Type01a() throws Exception {
        insertTest("^// test", "Multiple\\Uses01\\Type00", UseScope.Type.TYPE);
    }

    public void testMultipleUsesF01_Type01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Multiple\\Uses01\\Type00", UseScope.Type.TYPE, option);
    }

    public void testMultipleUsesF01_Function01() throws Exception {
        insertTest("^// test", "Multiple\\Uses01\\function00", UseScope.Type.FUNCTION);
    }

    public void testMultipleUsesF01_Function02a() throws Exception {
        insertTest("^// test", "Multiple\\Uses01\\function01", UseScope.Type.FUNCTION);
    }

    public void testMultipleUsesF01_Function02b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Multiple\\Uses01\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testMultipleUsesF01_Function02c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Multiple\\Uses01\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testMultipleUsesF01_Function02d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Multiple\\Uses01\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testMultipleUsesF01_Function03a() throws Exception {
        insertTest("^// test", "Multiple\\Uses00\\function01", UseScope.Type.FUNCTION);
    }

    public void testMultipleUsesF01_Function03b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Multiple\\Uses00\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testMultipleUsesF01_Const01a() throws Exception {
        insertTest("^// test", "Multiple\\Uses01\\const00", UseScope.Type.CONST);
    }

    public void testMultipleUsesF01_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Multiple\\Uses01\\const00", UseScope.Type.CONST, option);
    }

    public void testMultipleUsesF01_Const01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Multiple\\Uses01\\const00", UseScope.Type.CONST, option);
    }

    public void testMultipleUsesF01_Const01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Multiple\\Uses01\\const00", UseScope.Type.CONST, option);
    }

    public void testMultipleUsesC01_Type01a() throws Exception {
        insertTest("^// test", "Multiple\\Uses01\\Type00", UseScope.Type.TYPE);
    }

    public void testMultipleUsesC01_Type01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Multiple\\Uses01\\Type00", UseScope.Type.TYPE, option);
    }

    public void testMultipleUsesC01_Function01a() throws Exception {
        insertTest("^// test", "Multiple\\Uses01\\function01", UseScope.Type.FUNCTION);
    }

    public void testMultipleUsesC01_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Multiple\\Uses01\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testMultipleUsesC01_Function01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Multiple\\Uses01\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testMultipleUsesC01_Function01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Multiple\\Uses01\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testMultipleUsesC01_Const01() throws Exception {
        insertTest("^// test", "Multiple\\Uses01\\const03", UseScope.Type.CONST);
    }

    public void testMultipleUsesC01_Const02a() throws Exception {
        insertTest("^// test", "Multiple\\Uses00\\const01", UseScope.Type.CONST);
    }

    public void testMultipleUsesC01_Const02b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Multiple\\Uses00\\const01", UseScope.Type.CONST, option);
    }

    public void testMultipleUsesC01_Const03a() throws Exception {
        insertTest("^// test", "Multiple\\Uses01\\const01", UseScope.Type.CONST);
    }

    public void testMultipleUsesC01_Const03b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Multiple\\Uses01\\const01", UseScope.Type.CONST, option);
    }

    public void testMultipleUsesC01_Const03c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Multiple\\Uses01\\const01", UseScope.Type.CONST, option);
    }

    public void testMultipleUsesC01_Const03d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Multiple\\Uses01\\const01", UseScope.Type.CONST, option);
    }

    public void testMultipleUsesC01_Const04a() throws Exception {
        insertTest("^// test", "Multiple\\Uses01\\const04", UseScope.Type.CONST);
    }

    public void testMultipleUsesC01_Const04b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Multiple\\Uses01\\const04", UseScope.Type.CONST, option);
    }

    public void testMultipleUsesC01_Const04c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Multiple\\Uses01\\const04", UseScope.Type.CONST, option);
    }

    public void testMultipleUsesC01_Const04d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Multiple\\Uses01\\const04", UseScope.Type.CONST, option);
    }

    // Group uses
    public void testGroupUsesT01_Type01() throws Exception {
        insertTest("^// test", "Group\\Uses\\Type01", UseScope.Type.TYPE);
    }

    public void testGroupUsesT01_Type02a() throws Exception {
        insertTest("^// test", "Group\\Uses\\Type00", UseScope.Type.TYPE);
    }

    public void testGroupUsesT01_Type02b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses\\Type00", UseScope.Type.TYPE, option);
    }

    public void testGroupUsesT01_Type03a() throws Exception {
        insertTest("^// test", "Group\\Uses\\Type02", UseScope.Type.TYPE);
    }

    public void testGroupUsesT01_Type03b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses\\Type02", UseScope.Type.TYPE, option);
    }

    public void testGroupUsesT01_Type04() throws Exception {
        insertTest("^// test", "Group\\Uses\\test01", UseScope.Type.TYPE);
    }

    public void testGroupUsesT01_Type05() throws Exception {
        insertTest("^// test", "Group\\Type01", UseScope.Type.TYPE);
    }

    public void testGroupUsesT01_Type06a() throws Exception {
        insertTest("^// test", "Type01", UseScope.Type.TYPE);
    }

    public void testGroupUsesT01_Type06b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Type01", UseScope.Type.TYPE, option);
    }

    public void testGroupUsesT01_Function01a() throws Exception {
        insertTest("^// test", "Group\\Uses\\function01", UseScope.Type.FUNCTION);
    }

    public void testGroupUsesT01_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesT01_Const01a() throws Exception {
        insertTest("^// test", "Group\\Uses\\const01", UseScope.Type.CONST);
    }

    public void testGroupUsesT01_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses\\const01", UseScope.Type.CONST, option);
    }

    public void testGroupUsesT02_Type01() throws Exception {
        insertTest("^// test", "Example\\Type00", UseScope.Type.TYPE);
    }

    public void testGroupUsesT02_Type02() throws Exception {
        insertTest("^// test", "Group\\Uses\\Type00", UseScope.Type.TYPE);
    }

    public void testGroupUsesT02_Type03() throws Exception {
        insertTest("^// test", "Group\\Uses\\Type02", UseScope.Type.TYPE);
    }

    public void testGroupUsesT02_Type04() throws Exception {
        insertTest("^// test", "Group\\Uses\\Test\\Type00", UseScope.Type.TYPE);
    }

    public void testGroupUsesT02_Type05() throws Exception {
        insertTest("^// test", "Group\\Uses\\Type05", UseScope.Type.TYPE);
    }

    public void testGroupUsesT02_Type06a() throws Exception {
        insertTest("^// test", "Uses\\Type00", UseScope.Type.TYPE);
    }

    public void testGroupUsesT02_Type06b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Uses\\Type00", UseScope.Type.TYPE, option);
    }

    public void testGroupUsesT02_Function01a() throws Exception {
        insertTest("^// test", "Group\\Uses\\function01", UseScope.Type.FUNCTION);
    }

    public void testGroupUsesT02_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesT02_Const01a() throws Exception {
        insertTest("^// test", "Group\\Uses\\const01", UseScope.Type.CONST);
    }

    public void testGroupUsesT02_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses\\const01", UseScope.Type.CONST, option);
    }

    public void testGroupUsesT03_Type01() throws Exception {
        insertTest("^// test", "Group\\Uses00\\Type01", UseScope.Type.TYPE);
    }

    public void testGroupUsesT03_Type02() throws Exception {
        insertTest("^// test", "Group\\Uses01\\Type00", UseScope.Type.TYPE);
    }

    public void testGroupUsesT03_Type03() throws Exception {
        insertTest("^// test", "Group\\Uses02\\Type00", UseScope.Type.TYPE);
    }

    public void testGroupUsesT03_Type04() throws Exception {
        insertTest("^// test", "Group\\Uses03\\Type05", UseScope.Type.TYPE);
    }

    public void testGroupUsesT03_Type05() throws Exception {
        insertTest("^// test", "Group\\Uses04\\Type00", UseScope.Type.TYPE);
    }

    public void testGroupUsesT03_Function01a() throws Exception {
        insertTest("^// test", "Group\\Uses\\function01", UseScope.Type.FUNCTION);
    }

    public void testGroupUsesT03_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesT03_Const01a() throws Exception {
        insertTest("^// test", "Group\\Uses\\const01", UseScope.Type.CONST);
    }

    public void testGroupUsesT03_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses\\const01", UseScope.Type.CONST, option);
    }

    public void testGroupUsesT04_Type01() throws Exception {
        insertTest("^// test", "Group\\Uses00\\Type01", UseScope.Type.TYPE);
    }

    public void testGroupUsesT04_Type02() throws Exception {
        insertTest("^// test", "Group\\Uses01\\Type01", UseScope.Type.TYPE);
    }

    public void testGroupUsesT04_Type03a() throws Exception {
        insertTest("^// test", "Group\\Uses01\\Type02", UseScope.Type.TYPE);
    }

    public void testGroupUsesT04_Type03b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true)
                .wrapGroupUse(false);
        insertTest("^// test", "Group\\Uses01\\Type02", UseScope.Type.TYPE, option);
    }

    public void testGroupUsesT04_Type04() throws Exception {
        insertTest("^// test", "Group\\Uses02\\Type02", UseScope.Type.TYPE);
    }

    public void testGroupUsesT04_Type05() throws Exception {
        insertTest("^// test", "Group\\Uses03\\Type05", UseScope.Type.TYPE);
    }

    public void testGroupUsesT04_Type06() throws Exception {
        insertTest("^// test", "Group\\Uses04\\Type00", UseScope.Type.TYPE);
    }

    public void testGroupUsesT04_Function01a() throws Exception {
        insertTest("^// test", "Group\\Uses\\function01", UseScope.Type.FUNCTION);
    }

    public void testGroupUsesT04_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesT04_Const01a() throws Exception {
        insertTest("^// test", "Group\\Uses\\const01", UseScope.Type.CONST);
    }

    public void testGroupUsesT04_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses\\const01", UseScope.Type.CONST, option);
    }

    public void testGroupUsesTC01a_Type01() throws Exception {
        insertTest("^// test", "Group\\Uses00\\Type00", UseScope.Type.TYPE);
    }

    public void testGroupUsesTC01a_Type02a() throws Exception {
        insertTest("^// test", "Group\\Uses01\\Type00", UseScope.Type.TYPE);
    }

    public void testGroupUsesTC01a_Type02b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses01\\Type00", UseScope.Type.TYPE, option);
    }

    public void testGroupUsesTC01a_Type03a() throws Exception {
        insertTest("^// test", "Group\\Uses02\\Type00", UseScope.Type.TYPE);
    }

    public void testGroupUsesTC01a_Type03b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses02\\Type00", UseScope.Type.TYPE, option);
    }

    public void testGroupUsesTC01a_Function01a() throws Exception {
        insertTest("^// test", "Group\\Uses00\\function00", UseScope.Type.FUNCTION);
    }

    public void testGroupUsesTC01a_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses00\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesTC01a_Function01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Group\\Uses00\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesTC01a_Function01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses00\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesTC01a_Const01a() throws Exception {
        insertTest("^// test", "Group\\Uses00\\const00", UseScope.Type.CONST);
    }

    public void testGroupUsesTC01a_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses00\\const00", UseScope.Type.CONST, option);
    }

    public void testGroupUsesTC01a_Const01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Group\\Uses00\\const00", UseScope.Type.CONST, option);
    }

    public void testGroupUsesTC01a_Const01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses00\\const00", UseScope.Type.CONST, option);
    }

    public void testGroupUsesTC01a_Const02a() throws Exception {
        insertTest("^// test", "Group\\Uses01\\const00", UseScope.Type.CONST);
    }

    public void testGroupUsesTC01a_Const02b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses01\\const00", UseScope.Type.CONST, option);
    }

    public void testGroupUsesTC01a_Const02c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Group\\Uses01\\const00", UseScope.Type.CONST, option);
    }

    public void testGroupUsesTC01a_Const02d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses01\\const00", UseScope.Type.CONST, option);
    }

    public void testGroupUsesTC01a_Const03() throws Exception {
        insertTest("^// test", "Group\\Uses01\\const02\\const02", UseScope.Type.CONST);
    }

    public void testGroupUsesTC01a_Const04() throws Exception {
        insertTest("^// test", "Group\\Uses01\\const07", UseScope.Type.CONST);
    }

    public void testGroupUsesTC01a_Const05a() throws Exception {
        insertTest("^// test", "Group\\Uses02\\const00", UseScope.Type.CONST);
    }

    public void testGroupUsesTC01a_Const05b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses02\\const00", UseScope.Type.CONST, option);
    }

    public void testGroupUsesTC01b_Type01() throws Exception {
        insertTest("^// test", "Group\\Uses00\\Type00", UseScope.Type.TYPE);
    }

    public void testGroupUsesTC01b_Type02a() throws Exception {
        insertTest("^// test", "Group\\Uses01\\Type00", UseScope.Type.TYPE);
    }

    public void testGroupUsesTC01b_Type02b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses01\\Type00", UseScope.Type.TYPE, option);
    }

    public void testGroupUsesTC01b_Type03a() throws Exception {
        insertTest("^// test", "Group\\Uses02\\Type00", UseScope.Type.TYPE);
    }

    public void testGroupUsesTC01b_Type03b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses02\\Type00", UseScope.Type.TYPE, option);
    }

    public void testGroupUsesTC01b_Function01a() throws Exception {
        insertTest("^// test", "Group\\Uses00\\function00", UseScope.Type.FUNCTION);
    }

    public void testGroupUsesTC01b_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses00\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesTC01b_Function01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Group\\Uses00\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesTC01b_Function01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses00\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesTC01b_Const01a() throws Exception {
        insertTest("^// test", "Group\\Uses00\\const00", UseScope.Type.CONST);
    }

    public void testGroupUsesTC01b_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses00\\const00", UseScope.Type.CONST, option);
    }

    public void testGroupUsesTC01b_Const01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Group\\Uses00\\const00", UseScope.Type.CONST, option);
    }

    public void testGroupUsesTC01b_Const01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses00\\const00", UseScope.Type.CONST, option);
    }

    public void testGroupUsesTC01b_Const02a() throws Exception {
        insertTest("^// test", "Group\\Uses01\\const00", UseScope.Type.CONST);
    }

    public void testGroupUsesTC01b_Const02b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses01\\const00", UseScope.Type.CONST, option);
    }

    public void testGroupUsesTC01b_Const03() throws Exception {
        insertTest("^// test", "Group\\Uses01\\const02\\const02", UseScope.Type.CONST);
    }

    public void testGroupUsesTC01b_Const04() throws Exception {
        insertTest("^// test", "Group\\Uses01\\const07", UseScope.Type.CONST);
    }

    public void testGroupUsesTC01b_Const05a() throws Exception {
        insertTest("^// test", "Group\\Uses02\\const00", UseScope.Type.CONST);
    }

    public void testGroupUsesTC01b_Const05b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses02\\const00", UseScope.Type.CONST, option);
    }

    public void testGroupUsesTF01_Type01() throws Exception {
        insertTest("^// test", "Group\\Uses00\\Type00", UseScope.Type.TYPE);
    }

    public void testGroupUsesTF01_Type02() throws Exception {
        insertTest("^// test", "Group\\Uses01\\Type00", UseScope.Type.TYPE);
    }

    public void testGroupUsesTF01_Type03() throws Exception {
        insertTest("^// test", "Group\\Uses02\\Type00", UseScope.Type.TYPE);
    }

    public void testGroupUsesTF01_Function01a() throws Exception {
        insertTest("^// test", "Group\\Uses00\\function00", UseScope.Type.FUNCTION);
    }

    public void testGroupUsesTF01_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses00\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesTF01_Function01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Group\\Uses00\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesTF01_Function01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses00\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesTF01_Function02a() throws Exception {
        insertTest("^// test", "Group\\Uses01\\function03", UseScope.Type.FUNCTION);
    }

    public void testGroupUsesTF01_Function02b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses01\\function03", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesTF01_Function03a() throws Exception {
        insertTest("^// test", "Group\\Uses02\\function00", UseScope.Type.FUNCTION);
    }

    public void testGroupUsesTF01_Function03b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses02\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesTF01_Function04() throws Exception {
        insertTest("^// test", "Group\\Uses03\\function11", UseScope.Type.FUNCTION);
    }

    public void testGroupUsesTF01_Function05() throws Exception {
        insertTest("^// test", "Group\\Uses03\\Sub\\function11", UseScope.Type.FUNCTION);
    }

    public void testGroupUsesTF01_Function06() throws Exception {
        insertTest("^// test", "Group\\Uses03\\Sub\\Sub\\function11", UseScope.Type.FUNCTION);
    }

    public void testGroupUsesTFC01_Type01() throws Exception {
        insertTest("^// test", "Group\\Uses00\\Type00", UseScope.Type.TYPE);
    }

    public void testGroupUsesTFC01_Type02() throws Exception {
        insertTest("^// test", "Group\\Uses01\\Type00", UseScope.Type.TYPE);
    }

    public void testGroupUsesTFC01_Type03() throws Exception {
        insertTest("^// test", "Group\\Uses01\\Sub01\\Sub02\\Sub03\\Type00", UseScope.Type.TYPE);
    }

    public void testGroupUsesTFC01_Type04() throws Exception {
        insertTest("^// test", "Group\\Uses01\\Sub01\\Sub02\\Sub03\\Sub04\\Type00", UseScope.Type.TYPE);
    }

    public void testGroupUsesTFC01_Type05() throws Exception {
        insertTest("^// test", "Group\\Uses01\\Sub01\\Sub02\\Sub03\\Type02", UseScope.Type.TYPE);
    }

    public void testGroupUsesTFC01_Type06() throws Exception {
        insertTest("^// test", "Group\\Uses01\\Sub04\\Sub05\\Type04", UseScope.Type.TYPE);
    }

    public void testGroupUsesTFC01_Type07a() throws Exception {
        insertTest("^// test", "Group\\Uses02\\Type00", UseScope.Type.TYPE);
    }

    public void testGroupUsesTFC01_Type07b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses02\\Type00", UseScope.Type.TYPE, option);
    }

    public void testGroupUsesTFC01_Function01a() throws Exception {
        insertTest("^// test", "Group\\Uses00\\function00", UseScope.Type.FUNCTION);
    }

    public void testGroupUsesTFC01_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses00\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesTFC01_Function01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Group\\Uses00\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesTFC01_Function01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses00\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesTFC01_Function02a() throws Exception {
        insertTest("^// test", "Group\\Uses01\\function00", UseScope.Type.FUNCTION);
    }

    public void testGroupUsesTFC01_Function02b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses01\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesTFC01_Function03() throws Exception {
        insertTest("^// test", "Group\\Uses01\\Sub01\\function00", UseScope.Type.FUNCTION);
    }

    public void testGroupUsesTFC01_Function04() throws Exception {
        insertTest("^// test", "Group\\Uses01\\Sub01\\function02", UseScope.Type.FUNCTION);
    }

    public void testGroupUsesTFC01_Function05() throws Exception {
        insertTest("^// test", "Group\\Uses01\\Sub02\\function04", UseScope.Type.FUNCTION);
    }

    public void testGroupUsesTFC01_Function06() throws Exception {
        insertTest("^// test", "Group\\Uses01\\Sub03\\Sub04\\function00", UseScope.Type.FUNCTION);
    }

    public void testGroupUsesTFC01_Function07a() throws Exception {
        insertTest("^// test", "Group\\Uses02\\function00", UseScope.Type.FUNCTION);
    }

    public void testGroupUsesTFC01_Function07b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses02\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesTFC01_Function07c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Group\\Uses02\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesTFC01_Function07d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses02\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesTFC01_Const01a() throws Exception {
        insertTest("^// test", "Group\\Uses00\\const00", UseScope.Type.CONST);
    }

    public void testGroupUsesTFC01_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses00\\const00", UseScope.Type.CONST, option);
    }

    public void testGroupUsesTFC01_Const01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Group\\Uses00\\const00", UseScope.Type.CONST, option);
    }

    public void testGroupUsesTFC01_Const01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses00\\const00", UseScope.Type.CONST, option);
    }

    public void testGroupUsesTFC01_Const02() throws Exception {
        insertTest("^// test", "Group\\Uses01\\const00", UseScope.Type.CONST);
    }

    public void testGroupUsesTFC01_Const03() throws Exception {
        insertTest("^// test", "Group\\Uses01\\Sub01\\Sub02\\const02", UseScope.Type.CONST);
    }

    public void testGroupUsesTFC01_Const04() throws Exception {
        insertTest("^// test", "Group\\Uses01\\Sub01\\Sub02\\Sub03\\const02", UseScope.Type.CONST);
    }

    public void testGroupUsesTFC01_Const05() throws Exception {
        insertTest("^// test", "Group\\Uses01\\Sub03\\Sub04\\const00", UseScope.Type.CONST);
    }

    public void testGroupUsesTFC01_Const06() throws Exception {
        insertTest("^// test", "Group\\Uses01\\Sub04\\const03", UseScope.Type.CONST);
    }

    public void testGroupUsesTFC01_Const07() throws Exception {
        insertTest("^// test", "Group\\Uses01\\Sub04\\const04", UseScope.Type.CONST);
    }

    public void testGroupUsesTFC01_Const08() throws Exception {
        insertTest("^// test", "Group\\Uses01\\Sub05\\Sub06\\const06", UseScope.Type.CONST);
    }

    public void testGroupUsesTFC01_Const09() throws Exception {
        insertTest("^// test", "Group\\Uses01\\Sub05\\Sub06\\Sub07\\const06", UseScope.Type.CONST);
    }

    public void testGroupUsesTFC01_Const10a() throws Exception {
        insertTest("^// test", "Group\\Uses02\\const00", UseScope.Type.CONST);
    }

    public void testGroupUsesTFC01_Const10b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses02\\const00", UseScope.Type.CONST, option);
    }

    public void testGroupUsesTFC01_Const10c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Group\\Uses02\\const00", UseScope.Type.CONST, option);
    }

    public void testGroupUsesTFC01_Const10d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses02\\const00", UseScope.Type.CONST, option);
    }

    public void testGroupUsesF01_Type01a() throws Exception {
        insertTest("^// test", "Group\\Uses00\\Type01", UseScope.Type.TYPE);
    }

    public void testGroupUsesF01_Type01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses00\\Type01", UseScope.Type.TYPE, option);
    }

    public void testGroupUsesF01_Function01() throws Exception {
        insertTest("^// test", "Group\\Uses01\\function01", UseScope.Type.FUNCTION);
    }

    public void testGroupUsesF01_Function02a() throws Exception {
        insertTest("^// test", "Group\\Uses01\\function00", UseScope.Type.FUNCTION);
    }

    public void testGroupUsesF01_Function02b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses01\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesF01_Function02c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Group\\Uses01\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesF01_Function02d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses01\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesF01_Function03a() throws Exception {
        insertTest("^// test", "Group\\Uses00\\function00", UseScope.Type.FUNCTION);
    }

    public void testGroupUsesF01_Function03b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses00\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesF01_Const01a() throws Exception {
        insertTest("^// test", "Group\\Uses00\\const01", UseScope.Type.CONST);
    }

    public void testGroupUsesF01_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses00\\const01", UseScope.Type.CONST, option);
    }

    public void testGroupUsesF01_Const01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Group\\Uses00\\const01", UseScope.Type.CONST, option);
    }

    public void testGroupUsesF01_Const01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses00\\const01", UseScope.Type.CONST, option);
    }

    public void testGroupUsesC01_Type01a() throws Exception {
        insertTest("^// test", "Group\\Uses00\\Type01", UseScope.Type.TYPE);
    }

    public void testGroupUsesC01_Type01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses00\\Type01", UseScope.Type.TYPE, option);
    }

    public void testGroupUsesC01_Function01a() throws Exception {
        insertTest("^// test", "Group\\Uses01\\function01", UseScope.Type.FUNCTION);
    }

    public void testGroupUsesC01_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses01\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesC01_Function01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Group\\Uses01\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesC01_Function01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses01\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testGroupUsesC01_Const01a() throws Exception {
        insertTest("^// test", "Group\\Uses00\\const00", UseScope.Type.CONST);
    }

    public void testGroupUsesC01_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses00\\const00", UseScope.Type.CONST, option);
    }

    public void testGroupUsesC01_Const02() throws Exception {
        insertTest("^// test", "Group\\Uses01\\const00", UseScope.Type.CONST);
    }

    public void testGroupUsesC01_Const03() throws Exception {
        insertTest("^// test", "Group\\Uses01\\Sub\\SubConst02", UseScope.Type.CONST);
    }

    public void testGroupUsesC01_Const04a() throws Exception {
        insertTest("^// test", "Group\\Uses02\\const00", UseScope.Type.CONST);
    }

    public void testGroupUsesC01_Const04b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses02\\const00", UseScope.Type.CONST, option);
    }

    // mixed
    public void testMixedUsesT01_Type01() throws Exception {
        insertTest("^// test", "Group\\Uses01\\Type00", UseScope.Type.TYPE);
    }

    public void testMixedUsesT01_Type02() throws Exception {
        insertTest("^// test", "Group\\Uses01\\Type02", UseScope.Type.TYPE);
    }

    public void testMixedUsesT01_Type03a() throws Exception {
        insertTest("^// test", "Group\\Uses03\\Type02", UseScope.Type.TYPE);
    }

    public void testMixedUsesT01_Type03b() throws Exception {
        insertTest("^// test", "Group\\Uses03\\Type04", UseScope.Type.TYPE);
    }

    public void testMixedUsesT01_Type04() throws Exception {
        insertTest("^// test", "Group\\Uses04\\Type04", UseScope.Type.TYPE);
    }

    public void testMixedUsesT01_Type05() throws Exception {
        insertTest("^// test", "Group\\Uses05\\Type06", UseScope.Type.TYPE);
    }

    public void testMixedUsesT01_Function01a() throws Exception {
        insertTest("^// test", "Group\\Uses\\function01", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesT01_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses\\function01", UseScope.Type.FUNCTION, option);
    }

    public void testMixedUsesT01_Const01a() throws Exception {
        insertTest("^// test", "Group\\Uses\\const01", UseScope.Type.CONST);
    }

    public void testMixedUsesT01_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Group\\Uses\\const01", UseScope.Type.CONST, option);
    }

    public void testMixedUsesTF01_Type01() throws Exception {
        insertTest("^// test", "Mixed\\Uses01\\Type00", UseScope.Type.TYPE);
    }

    public void testMixedUsesTF01_Type02() throws Exception {
        insertTest("^// test", "Mixed\\Uses03\\Type00", UseScope.Type.TYPE);
    }

    public void testMixedUsesTF01_Type03() throws Exception {
        insertTest("^// test", "Mixed\\Uses03\\Sub01\\Type00", UseScope.Type.TYPE);
    }

    public void testMixedUsesTF01_Type04() throws Exception {
        insertTest("^// test", "Mixed\\Uses03\\Sub01\\Sub02\\Type00", UseScope.Type.TYPE);
    }

    public void testMixedUsesTF01_Type05() throws Exception {
        insertTest("^// test", "Mixed\\Uses03\\Sub01\\Sub02\\Sub03\\Type00", UseScope.Type.TYPE);
    }

    public void testMixedUsesTF01_Type06() throws Exception {
        insertTest("^// test", "Mixed\\Uses05\\Type00", UseScope.Type.TYPE);
    }

    public void testMixedUsesTF01_Type07a() throws Exception {
        insertTest("^// test", "Mixed\\Uses06\\Sub01\\Type00", UseScope.Type.TYPE);
    }

    public void testMixedUsesTF01_Type07b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Mixed\\Uses06\\Sub01\\Type00", UseScope.Type.TYPE, option);
    }

    public void testMixedUsesTF01_Function01a() throws Exception {
        insertTest("^// test", "Mixed\\Uses00\\function00", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesTF01_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Mixed\\Uses00\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testMixedUsesTF01_Function02() throws Exception {
        insertTest("^// test", "Mixed\\Uses01\\function00", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesTF01_Function03() throws Exception {
        insertTest("^// test", "Mixed\\Uses05\\function00", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesTF01_Function04() throws Exception {
        insertTest("^// test", "Mixed\\Uses05\\Sub01\\function00", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesTF01_Function05() throws Exception {
        insertTest("^// test", "Mixed\\Uses05\\Sub01\\Sub02\\function00", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesTF01_Function06() throws Exception {
        insertTest("^// test", "Mixed\\Uses05\\Sub01\\Sub02\\Sub03\\function00", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesTF01_Function07() throws Exception {
        insertTest("^// test", "Mixed\\Uses05\\Sub01\\Sub07\\Sub08\\function00", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesTF01_Function08() throws Exception {
        insertTest("^// test", "Mixed\\Uses05\\Sub04\\Sub07\\Sub08\\function00", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesTF01_Function09() throws Exception {
        insertTest("^// test", "Mixed\\Uses08\\Sub04\\Sub07\\Sub08\\function00", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesTF01_Const01a() throws Exception {
        insertTest("^// test", "Mixed\\Uses00\\const01", UseScope.Type.CONST);
    }

    public void testMixedUsesTF01_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Mixed\\Uses00\\const01", UseScope.Type.CONST, option);
    }

    public void testMixedUsesTF01_Const01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Mixed\\Uses00\\const01", UseScope.Type.CONST, option);
    }

    public void testMixedUsesTF01_Const01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Mixed\\Uses00\\const01", UseScope.Type.CONST, option);
    }

    public void testMixedUsesTC01_Type01() throws Exception {
        insertTest("^// test", "Mixed\\Uses00\\Type00", UseScope.Type.TYPE);
    }

    public void testMixedUsesTC01_Type02() throws Exception {
        insertTest("^// test", "Mixed\\Uses03\\Type00", UseScope.Type.TYPE);
    }

    public void testMixedUsesTC01_Type03() throws Exception {
        insertTest("^// test", "Mixed\\Uses03\\Sub01\\Sub02\\Type00", UseScope.Type.TYPE);
    }

    public void testMixedUsesTC01_Type04() throws Exception {
        insertTest("^// test", "Mixed\\Uses03\\Sub01\\Sub02\\Sub03\\Type00", UseScope.Type.TYPE);
    }

    public void testMixedUsesTC01_Type05() throws Exception {
        insertTest("^// test", "Mixed\\Uses03\\Sub01\\Sub02\\Sub06\\Type00", UseScope.Type.TYPE);
    }

    public void testMixedUsesTC01_Type06a() throws Exception {
        insertTest("^// test", "Mixed\\Uses04\\Sub01\\Sub02\\Sub03\\Type00", UseScope.Type.TYPE);
    }

    public void testMixedUsesTC01_Type06b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Mixed\\Uses04\\Sub01\\Sub02\\Sub03\\Type00", UseScope.Type.TYPE, option);
    }

    public void testMixedUsesTC01_Function01a() throws Exception {
        insertTest("^// test", "Mixed\\Uses00\\function00", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesTC01_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Mixed\\Uses00\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testMixedUsesTC01_Function01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Mixed\\Uses00\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testMixedUsesTC01_Function01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Mixed\\Uses00\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testMixedUsesTC01_Const01a() throws Exception {
        insertTest("^// test", "Mixed\\Uses00\\const01", UseScope.Type.CONST);
    }

    public void testMixedUsesTC01_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Mixed\\Uses00\\const01", UseScope.Type.CONST, option);
    }

    public void testMixedUsesTC01_Const01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Mixed\\Uses00\\const01", UseScope.Type.CONST, option);
    }

    public void testMixedUsesTC01_Const01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Mixed\\Uses00\\const01", UseScope.Type.CONST, option);
    }

    public void testMixedUsesTC01_Const02() throws Exception {
        insertTest("^// test", "Mixed\\Uses01\\Sub01\\const00", UseScope.Type.CONST);
    }

    public void testMixedUsesTC01_Const03() throws Exception {
        insertTest("^// test", "Mixed\\Uses01\\Sub01\\Sub02\\const00", UseScope.Type.CONST);
    }

    public void testMixedUsesTC01_Const04() throws Exception {
        insertTest("^// test", "Mixed\\Uses01\\Sub01\\Sub03\\Sub04\\const00", UseScope.Type.CONST);
    }

    public void testMixedUsesTC01_Const05() throws Exception {
        insertTest("^// test", "Mixed\\Uses01\\Sub01\\Sub03\\Sub04\\Sub05\\const00", UseScope.Type.CONST);
    }

    public void testMixedUsesTC01_Const06() throws Exception {
        insertTest("^// test", "Mixed\\Uses03\\const00", UseScope.Type.CONST);
    }

    public void testMixedUsesTC01_Const07() throws Exception {
        insertTest("^// test", "Mixed\\Uses03\\const32", UseScope.Type.CONST);
    }

    public void testMixedUsesTC01_Const08() throws Exception {
        insertTest("^// test", "Mixed\\Uses04\\const00", UseScope.Type.CONST);
    }

    public void testMixedUsesTC01_Const09() throws Exception {
        insertTest("^// test", "Mixed\\Uses06\\const00", UseScope.Type.CONST);
    }

    public void testMixedUsesTCF01_Type01() throws Exception {
        insertTest("^// test", "Mixed\\Uses00\\Type00", UseScope.Type.TYPE);
    }

    public void testMixedUsesTCF01_Type02() throws Exception {
        insertTest("^// test", "Mixed\\Uses01\\Sub01\\Sub02\\Type00", UseScope.Type.TYPE);
    }

    public void testMixedUsesTCF01_Type03() throws Exception {
        insertTest("^// test", "Mixed\\Uses01\\Sub01\\Sub02\\Sub03\\Type00", UseScope.Type.TYPE);
    }

    public void testMixedUsesTCF01_Type04() throws Exception {
        insertTest("^// test", "Mixed\\Uses01\\Sub01\\Sub02\\Sub04\\Type00", UseScope.Type.TYPE);
    }

    public void testMixedUsesTCF01_Type05() throws Exception {
        insertTest("^// test", "Mixed\\Uses01\\Sub01\\Sub02\\Sub04\\Sub05\\Type00", UseScope.Type.TYPE);
    }

    public void testMixedUsesTCF01_Type06() throws Exception {
        insertTest("^// test", "Mixed\\Uses02\\Sub01\\Type00", UseScope.Type.TYPE);
    }

    public void testMixedUsesTCF01_Type07a() throws Exception {
        insertTest("^// test", "Mixed\\Uses04\\Sub01\\Type00", UseScope.Type.TYPE);
    }

    public void testMixedUsesTCF01_Type07b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Mixed\\Uses04\\Sub01\\Type00", UseScope.Type.TYPE, option);
    }

    public void testMixedUsesTCF01_Function01a() throws Exception {
        insertTest("^// test", "Mixed\\Uses00\\function00", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesTCF01_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Mixed\\Uses00\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testMixedUsesTCF01_Function01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Mixed\\Uses00\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testMixedUsesTCF01_Function01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Mixed\\Uses00\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testMixedUsesTCF01_Function02() throws Exception {
        insertTest("^// test", "Mixed\\Uses01\\Sub01\\Sub02\\function00", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesTCF01_Function03() throws Exception {
        insertTest("^// test", "Mixed\\Uses01\\Sub01\\Sub02\\Sub03\\function00", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesTCF01_Function04() throws Exception {
        insertTest("^// test", "Mixed\\Uses01\\Sub01\\Sub02\\Sub03\\Sub04\\Sub05\\function00", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesTCF01_Function05() throws Exception {
        insertTest("^// test", "Mixed\\Uses01\\Sub05\\function00", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesTCF01_Function06() throws Exception {
        insertTest("^// test", "Mixed\\Uses04\\Sub01\\function00", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesTCF01_Const01a() throws Exception {
        insertTest("^// test", "Mixed\\Uses00\\const01", UseScope.Type.CONST);
    }

    public void testMixedUsesTCF01_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Mixed\\Uses00\\const01", UseScope.Type.CONST, option);
    }

    public void testMixedUsesTCF01_Const01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Mixed\\Uses00\\const01", UseScope.Type.CONST, option);
    }

    public void testMixedUsesTCF01_Const01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Mixed\\Uses00\\const01", UseScope.Type.CONST, option);
    }

    public void testMixedUsesTCF01_Const02() throws Exception {
        insertTest("^// test", "Mixed\\Uses02\\const01", UseScope.Type.CONST);
    }

    public void testMixedUsesTCF01_Const03() throws Exception {
        insertTest("^// test", "Mixed\\Uses03\\const01", UseScope.Type.CONST);
    }

    public void testMixedUsesTCF01_Const04() throws Exception {
        insertTest("^// test", "Mixed\\Uses03\\Sub01\\const01", UseScope.Type.CONST);
    }

    public void testMixedUsesTCF01_Const05() throws Exception {
        insertTest("^// test", "Mixed\\Uses03\\Sub01\\Sub02\\const01", UseScope.Type.CONST);
    }

    public void testMixedUsesTCF01_Const06() throws Exception {
        insertTest("^// test", "Mixed\\Uses03\\Sub01\\Sub05\\const01", UseScope.Type.CONST);
    }

    public void testMixedUsesTCF01_Const07a() throws Exception {
        insertTest("^// test", "Mixed\\Uses04\\const01", UseScope.Type.CONST);
    }

    public void testMixedUsesTCF01_Const07b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Mixed\\Uses04\\const01", UseScope.Type.CONST, option);
    }

    public void testMixedUsesTCF01_Const07c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Mixed\\Uses04\\const01", UseScope.Type.CONST, option);
    }

    public void testMixedUsesTCF01_Const07d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Mixed\\Uses04\\const01", UseScope.Type.CONST, option);
    }

    public void testMixedUsesF01_Type01a() throws Exception {
        insertTest("^// test", "Mixed\\Uses01\\Type00", UseScope.Type.TYPE);
    }

    public void testMixedUsesF01_Type01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Mixed\\Uses01\\Type00", UseScope.Type.TYPE, option);
    }

    public void testMixedUsesF01_Function01a() throws Exception {
        insertTest("^// test", "Mixed\\Uses00\\function00", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesF01_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Mixed\\Uses00\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testMixedUsesF01_Function02() throws Exception {
        insertTest("^// test", "Mixed\\Uses03\\function00", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesF01_Function03() throws Exception {
        insertTest("^// test", "Mixed\\Uses03\\Sub01\\Sub02\\function00", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesF01_Function04() throws Exception {
        insertTest("^// test", "Mixed\\Uses03\\Sub03\\function00", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesF01_Function05() throws Exception {
        insertTest("^// test", "Mixed\\Uses03\\Sub04\\function00", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesF01_Function06() throws Exception {
        insertTest("^// test", "Mixed\\Uses05\\function00", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesF01_Function07() throws Exception {
        insertTest("^// test", "Mixed\\Uses06\\Sub01\\function00", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesF01_Function08() throws Exception {
        insertTest("^// test", "Mixed\\Uses06\\Sub01\\Sub02\\function00", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesF01_Function09() throws Exception {
        insertTest("^// test", "Mixed\\Uses07\\Sub01\\Sub02\\function00", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesF01_Const01a() throws Exception {
        insertTest("^// test", "Mixed\\Uses00\\const00", UseScope.Type.CONST);
    }

    public void testMixedUsesF01_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Mixed\\Uses00\\const00", UseScope.Type.CONST, option);
    }

    public void testMixedUsesF01_Const01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Mixed\\Uses00\\const00", UseScope.Type.CONST, option);
    }

    public void testMixedUsesF01_Const01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Mixed\\Uses00\\const00", UseScope.Type.CONST, option);
    }

    public void testMixedUsesC01_Type01a() throws Exception {
        insertTest("^// test", "Mixed\\Uses00\\Type00", UseScope.Type.TYPE);
    }

    public void testMixedUsesC01_Type01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Mixed\\Uses00\\Type00", UseScope.Type.TYPE, option);
    }

    public void testMixedUsesC01_Function01a() throws Exception {
        insertTest("^// test", "Mixed\\Uses00\\function00", UseScope.Type.FUNCTION);
    }

    public void testMixedUsesC01_Function01b() throws Exception {
        Option option = new Option()
                .isPSR12(false)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Mixed\\Uses00\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testMixedUsesC01_Function01c() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(false);
        insertTest("^// test", "Mixed\\Uses00\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testMixedUsesC01_Function01d() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Mixed\\Uses00\\function00", UseScope.Type.FUNCTION, option);
    }

    public void testMixedUsesC01_Const01a() throws Exception {
        insertTest("^// test", "Mixed\\Uses00\\const00", UseScope.Type.CONST);
    }

    public void testMixedUsesC01_Const01b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Mixed\\Uses00\\const00", UseScope.Type.CONST, option);
    }

    public void testMixedUsesC01_Const02() throws Exception {
        insertTest("^// test", "Mixed\\Uses01\\Sub01\\const00", UseScope.Type.CONST);
    }

    public void testMixedUsesC01_Const03() throws Exception {
        insertTest("^// test", "Mixed\\Uses01\\Sub01\\const02", UseScope.Type.CONST);
    }

    public void testMixedUsesC01_Const04() throws Exception {
        insertTest("^// test", "Mixed\\Uses02\\Sub01\\const02", UseScope.Type.CONST);
    }

    public void testMixedUsesC01_Const05() throws Exception {
        insertTest("^// test", "Mixed\\Uses04\\const00", UseScope.Type.CONST);
    }

    public void testMixedUsesC01_Const06() throws Exception {
        insertTest("^// test", "Mixed\\Uses05\\const00", UseScope.Type.CONST);
    }

    public void testMixedUsesC01_Const07() throws Exception {
        insertTest("^// test", "Mixed\\Uses05\\Sub01\\Sub02\\CONST00", UseScope.Type.CONST);
    }

    public void testMixedUsesC01_Const08() throws Exception {
        insertTest("^// test", "Mixed\\Uses05\\Sub02\\Sub03\\CONST00", UseScope.Type.CONST);
    }

    public void testMixedUsesC01_Const09() throws Exception {
        insertTest("^// test", "Mixed\\Uses05\\Sub04\\Sub05\\Sub06\\CONST00", UseScope.Type.CONST);
    }

    public void testMixedUsesC01_Const10() throws Exception {
        insertTest("^// test", "Mixed\\Uses06\\CONST00", UseScope.Type.CONST);
    }

    public void testMixedUsesC01_Const11a() throws Exception {
        insertTest("^// test", "Mixed\\Uses06\\Sub01\\CONST00", UseScope.Type.CONST);
    }

    public void testMixedUsesC01_Const11b() throws Exception {
        Option option = new Option()
                .isPSR12(true)
                .hasBlankLineBetweenUseTypes(true);
        insertTest("^// test", "Mixed\\Uses06\\Sub01\\CONST00", UseScope.Type.CONST, option);
    }

    private void insertTest(final String caretLine, String fqName, UseScope.Type useType) throws Exception {
        insertTest(caretLine, fqName, useType, new Option());
    }

    private void insertTest(final String caretLine, String fqName, UseScope.Type useType, Option option) throws Exception {
        String exactFileName = getTestPath();
        String result = getTestResult(exactFileName, caretLine, fqName, useType, option);
        assertDescriptionMatches(exactFileName, result, true, ".autoimport");
    }

    private String getTestResult(final String fileName, final String caretLine, String fqName, UseScope.Type useType, Option option) throws Exception {
        FileObject testFile = getTestFile(fileName);
        String text = readFile(testFile);
        final BaseDocument doc = getDocument(text);
        assertNotNull(doc);
        option.setCodeStyle(doc);
        Source testSource = Source.create(doc);
        final int caretOffset;
        if (caretLine != null) {
            caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(testSource, caretOffset);
        } else {
            caretOffset = -1;
        }
        final PHPParseResult[] result = new PHPParseResult[1];
        ParserManager.parse(Collections.singleton(testSource), new UserTask() {

            @Override
            public void run(final ResultIterator resultIterator) throws Exception {
                Parser.Result res = caretOffset == -1 ? resultIterator.getParserResult() : resultIterator.getParserResult(caretOffset);
                if (res != null) {
                    assertTrue(res instanceof ParserResult);
                    PHPParseResult phpResult = (PHPParseResult) res;
                    result[0] = phpResult;
                }
            }
        });
        PHPParseResult phpParseResult = result[0];
        AutoImport autoImport = AutoImport.get(phpParseResult);
        autoImport.insert(fqName, useType, caretOffset);
        return doc.getText(0, doc.getLength());
    }

    private String getTestFolderPath() {
        return "testfiles/autoimport/" + getTestDirName();
    }

    private String getTestPath() {
        return getTestFolderPath() + "/" + getTestDirName() + ".php";
    }

    private String getTestDirName() {
        String name = getName();
        int indexOf = name.indexOf("_");
        if (indexOf != -1) {
            name = name.substring(0, indexOf);
        }
        return name;
    }

    private static final class Option {

        private boolean isPSR12 = false;
        private boolean hasBlankLineBetweenUseTypes = false;
        private boolean wrapGroupUse = true;

        public static Option create() {
            return new Option();
        }

        public Option isPSR12(boolean isPSR12) {
            this.isPSR12 = isPSR12;
            return this;
        }

        public Option hasBlankLineBetweenUseTypes(boolean hasBlankLineBetweenUseTypes) {
            this.hasBlankLineBetweenUseTypes = hasBlankLineBetweenUseTypes;
            return this;
        }

        public Option wrapGroupUse(boolean wrapGroupUse) {
            this.wrapGroupUse = wrapGroupUse;
            return this;
        }

        public void setCodeStyle(Document document) {
            Map<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
            options.put(FmtOptions.PUT_IN_PSR12_ORDER, isPSR12);
            options.put(FmtOptions.BLANK_LINES_BETWEEN_USE_TYPES, (hasBlankLineBetweenUseTypes ? 1 : 0));
            options.put(FmtOptions.WRAP_GROUP_USE_LIST, (wrapGroupUse ? FmtOptions.WRAP_ALWAYS : FmtOptions.WRAP_NEVER));
            Preferences prefs = CodeStylePreferences.get(document).getPreferences();
            for (Map.Entry<String, Object> entry : options.entrySet()) {
                String option = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Integer) {
                    prefs.putInt(option, ((Integer) value));
                } else if (value instanceof String) {
                    prefs.put(option, (String) value);
                } else if (value instanceof Boolean) {
                    prefs.put(option, ((Boolean) value).toString());
                } else if (value instanceof CodeStyle.BracePlacement) {
                    prefs.put(option, ((CodeStyle.BracePlacement) value).name());
                } else if (value instanceof CodeStyle.WrapStyle) {
                    prefs.put(option, ((CodeStyle.WrapStyle) value).name());
                }
            }
        }
    }
}
