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
package org.netbeans.modules.java.hints.errors;

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.editor.codegen.ImplementGeneratorAccessor;
import org.netbeans.modules.java.hints.infrastructure.HintsTestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * @author Jan Lahoda
 */
public class ErrorHintsTest extends HintsTestBase {
    
    /** Need to be defined because of JUnit */
    public ErrorHintsTest(String name) {
        super(name);
        
    }
    
//    public static TestSuite suite() {
//        NbTestSuite suite = new NbTestSuite();
//        
//        suite.addTest(new JavaHintsTest("testImplementAbstractMethodsHint9"));
//        suite.addTest(new JavaHintsTest("testImplementAbstractMethodsHint10"));
//        suite.addTest(new JavaHintsTest("testImplementAbstractMethodsHint11"));
////        suite.addTest(new JavaHintsTest("testMakeClassAbstract3"));
//        
//        return suite;
//    }
        
//

    public void testImplementAbstractMethodsHint1() throws Exception {
        performTest("ImplementAbstractMethods1", "LBL_FIX_Impl_Abstract_Methods", 16, 60);
    }

    public void testImplementAbstractMethodsHint2() throws Exception {
        performTest("ImplementAbstractMethods2", "LBL_FIX_Impl_Abstract_Methods", 17, 15);
    }

    public void testImplementAbstractMethodsHint3() throws Exception {
        performTest("ImplementAbstractMethods3", "LBL_FIX_Impl_Abstract_Methods", 17, 25);
    }

    public void testImplementAbstractMethodsHint4() throws Exception {
        performTest("ImplementAbstractMethods4", "LBL_FIX_Impl_Abstract_Methods", 16, 30);
    }

    public void testImplementAbstractMethodsHint5() throws Exception {
        performTest("ImplementAbstractMethods5", "LBL_FIX_Impl_Abstract_Methods", 16, 30);
    }

    public void testImplementAbstractMethodsHint6() throws Exception {
        performTest("ImplementAbstractMethods6", "LBL_FIX_Impl_Abstract_Methods", 8, 5);
    }

    public void testImplementAbstractMethodsHint7() throws Exception {
        performTest("ImplementAbstractMethods7", "LBL_FIX_Impl_Abstract_Methods", 9, 25);
    }

    public void testImplementAbstractMethodsHint8() throws Exception {
        performTest("ImplementAbstractMethods8", "LBL_FIX_Impl_Abstract_Methods", 12, 43);
    }

    public void testImplementAbstractMethodsHint9() throws Exception {
        performTestDoNotPerform("ImplementAbstractMethods9", 8, 15);
    }

    public void testImplementAbstractMethodsHint10() throws Exception {
        performTestDoNotPerform("ImplementAbstractMethods10", 8, 15);
    }

    public void testImplementAbstractMethodsHint11() throws Exception {
        performTest("ImplementAbstractMethods11", "LBL_FIX_Impl_Abstract_Methods", 8, 15);
    }
    
    private List<ElementHandle<? extends Element>> findElementHandles(CompilationInfo info, String... specs) {
        List<ElementHandle<? extends Element>> result = new ArrayList<>(specs.length);
        for (String spec : specs) {
            Element el = info.getElementUtilities().findElement(spec);
            if (el != null) {
                result.add(ElementHandle.create(el));
            }
        }
        return result;
    }

    public void testImplementDefaultMethods1() throws Exception {
        ImplementGeneratorAccessor.setOverrideSelection((info, type) -> 
                findElementHandles(info,
                        "java.util.Iterator.hasNext()",
                        "java.util.Iterator.next()"
                )
        );
        performTest("ImplementDefaultMethods1", "ImplementDefaultMethods1", "LBL_FIX_Impl_Abstract_Methods", 7, 15, true, "1.8");
    }

    public void testImplementDefaultMethods2() throws Exception {
        ImplementGeneratorAccessor.setOverrideSelection((info, type) ->  {
                assertEquals("Wrong number of default overridable methods", 
                        4, info.getElementUtilities().findUnimplementedMethods(type, true).size());
                return findElementHandles(info,
                        "java.util.Iterator.hasNext()",
                        "java.util.Iterator.next()",
                        "java.util.Iterator.remove()"
                );
        });
        performTest("ImplementDefaultMethods2", "ImplementDefaultMethods2", "LBL_FIX_Impl_Abstract_Methods", 7, 15, true, "1.8");
    }

    public void testImplementEnumMethods() throws Exception {
        performTest("ImplementEnumMethods", "ImplementEnumMethods", "LBL_FIX_Impl_Methods_Enum_Values2", 8, 11, true, "1.8");
    }
    
//    public void testAddSemicolonHint() throws Exception {
//        performTest("org.netbeans.test.java.hints.AddSemicolon", "semicolon", 17, 15);
//    }
//

    public void testAddCastHint1() throws Exception {
        performTest("org.netbeans.test.java.hints.AddCast1", "Cast", 18, 15);
    }

    public void testAddCastHint2() throws Exception {
        performTest("org.netbeans.test.java.hints.AddCast2", "Cast", 20, 13);
    }

    public void testAddCastHint3() throws Exception {
        performTest("org.netbeans.test.java.hints.AddCast3", "Cast", 20, 20);
    }

    public void testAddCastHint4() throws Exception {
        performTest("org.netbeans.test.java.hints.AddCast4", "Cast", 22, 10);
    }

    public void testAddCastHint5() throws Exception {
        performTest("org.netbeans.test.java.hints.AddCast5", "Cast", 12, 1);
    }

    public void testAddCastHint6() throws Exception {
        performTest("org.netbeans.test.java.hints.AddCast6", "Cast", 13, 23);
    }

    public void testAddCastHint7() throws Exception {
        performTest("org.netbeans.test.java.hints.AddCast7", "Cast", 12, 18);
    }

    public void testAddCastHint8() throws Exception {
        performTest("org.netbeans.test.java.hints.AddCast8", "Cast", 13, 18);
    }

    public void testAddCastHint9() throws Exception {
        //should not cause exception
        //also tests Create field hint, which should not be proposed in this case:
        performTestDoNotPerform("org.netbeans.test.java.hints.AddCast9", 11, 18);
    }

    public void testAddCastHint10() throws Exception {
        //should not cause exception
        performTest("org.netbeans.test.java.hints.AddCast10", "Cast", 13, 18);
    }

    public void testAddCastHint11() throws Exception {
        performTestDoNotPerform("org.netbeans.test.java.hints.AddCast11", 12, 18);
    }
    
    //randomly fails, likely because of a bug in the change declaration hint:
//    public void testAddCastHint12() throws Exception {
//        performTestDoNotPerform("org.netbeans.test.java.hints.AddCast12", 12, 18);
//        performTestDoNotPerform("org.netbeans.test.java.hints.AddCast12", 14, 18);
//        performTestDoNotPerform("org.netbeans.test.java.hints.AddCast12", 16, 18);
//    }

    public void testAddCastHintDoNotPropose() throws Exception {
        //should not propose "cast to ..." hint if the actual problem is an undefined method
        performTestDoNotPerform("org.netbeans.test.java.hints.AddCastDoNotPropose", 9, 18);
    }

    public void testAddThrowsClauseHint1() throws Exception {
        performTest("org.netbeans.test.java.hints.AddThrowsClause1", "throws", 19, 30);
    }

    public void testAddThrowsClauseHint2() throws Exception {
        performTest("org.netbeans.test.java.hints.AddThrowsClause2", "throws", 22, 30);
    }

    public void testAddThrowsClauseHint3() throws Exception {
        performTest("org.netbeans.test.java.hints.AddThrowsClause3", "throws", 11, 30);
    }

    public void testAddThrowsClauseHint4() throws Exception {
        performTest("org.netbeans.test.java.hints.AddThrowsClause4", "throws", 11, 30);
    }
    
    /**tests only if an exception is thrown during hints creation of errors for this file
     */
 
    public void testCreateElementException() throws Exception {
        performTestDoNotPerform("org.netbeans.test.java.hints.CreateElementException", 10, 27);
    }
    
//    /**tests if an exception is thrown when the hint is approved:
//     */
//    public void testCreateFieldException() throws Exception {
//        performTest("org.netbeans.test.java.hints.CreateFieldException", "Field", 17, 31);
//    }
    
    //XXX: fails because of a bug in create constructor hint:
//    public void testCreateFieldException2() throws Exception {
//        performTestDoNotPerform("org.netbeans.test.java.hints.CreateFieldException2", 8, 15);
//    }

    public void testCreateFieldException3() throws Exception {
        performTestDoNotPerform("org.netbeans.test.java.hints.CreateFieldException3", 9, 15);
    }

    public void testCreateField1() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateField1", "Field", 23, 18);
    }

    public void testCreateField2() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateField2", "Field", 23, 20);
    }

    public void testCreateField3() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateField3", "Field", 23, 20);
    }
    
//    public void testCreateField4() throws Exception {
//        performTest("org.netbeans.test.java.hints.CreateField4", "Field", 23, 20);
//    }

    public void testCreateField5() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateField5", "Field", 23, 18);
    }

    public void testCreateField6() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateField6", "Field", 25, 18);
    }

    public void testCreateField7() throws Exception {
        performTestDoNotPerform("org.netbeans.test.java.hints.CreateField7", 9, 18);
    }
    
    //Create field which type is a type varaible, does not work yet:
//    public void testCreateField8() throws Exception {
//        performTest("org.netbeans.test.java.hints.CreateField8", "Field", 6, 18);
//    }

    public void testCreateField10() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateField10", "Field", 9, 10);
    }

    public void testCreateFieldPrimitive() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateFieldPrimitive", "Field", 23, 13);
    }
    
//    public void testCreateFieldMethod1() throws Exception {
//        performTest("org.netbeans.test.java.hints.CreateFieldMethod1", "Field", 23, 13);
//    }

    public void testCreateFieldMethod2() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateFieldMethod2", "Field", 23, 13);
    }
    
//    public void testDoNotProposeCreateField1() throws Exception {
//        performTestDoNotPerform("org.netbeans.test.java.hints.DoNotProposeCreateField1",9, 18);
//    }
//

    public void testDoNotProposeCreateField2() throws Exception {
        performTestDoNotPerform("org.netbeans.test.java.hints.DoNotProposeCreateField2", 7, 10);
    }
    
//    public void testTryWrapper1() throws Exception {
//        performTest("org.netbeans.test.java.hints.TryWrapper1", "try-catch", 19, 30);
//    }
//    public void testTryWrapper2() throws Exception {
//        performTest("org.netbeans.test.java.hints.TryWrapper2", "try-catch", 19, 30);
//    }
//    public void testTryWrapper3() throws Exception {
//        performTest("org.netbeans.test.java.hints.TryWrapper3", "try-catch", 20, 30);
//    }
//    public void testTryWrapper4() throws Exception {
//        performTest("org.netbeans.test.java.hints.TryWrapper4", "try-catch", 19, 30);
//    }
//    public void testTryWrapper5() throws Exception {
//        performTest("org.netbeans.test.java.hints.TryWrapper5", "try-catch", 19, 30);
//    }
//    public void testTryWrapper6() throws Exception {
//        performTest("org.netbeans.test.java.hints.TryWrapper6", "try-catch", 19, 30);
//    }
//    public void testTryWrapper7() throws Exception {
//        performTest("org.netbeans.test.java.hints.TryWrapper7", "try-catch", 19, 30);
//    }
//    public void testTryWrapper8() throws Exception {
//        performTest("org.netbeans.test.java.hints.TryWrapper8", "try-catch", 21, 30);
//    }
//    public void testTryWrapper9() throws Exception {
//        performTest("org.netbeans.test.java.hints.TryWrapper9", "try-catch", 19, 30);
//    }
//
//    public void testLocalAndParamIncorrect57990a() throws Exception {
//        performTestDoNotPerform("org.netbeans.test.java.hints.LocalVarParam57990a", 23, 20);
//    }
//
////    public void testLocalAndParamIncorrect57990b() throws Exception {
////        performTestDoNotPerform("org.netbeans.test.java.hints.LocalVarParam57990b", 23, 20);
////    }
//
//    public void testLocalAndParamIncorrect57990c() throws Exception {
//        performTestDoNotPerform("org.netbeans.test.java.hints.LocalVarParam57990c", 23, 20);
//    }

    public void testCreateLocalVariable1() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateLocalVariable1", "Local Variable", 23, 15);
    }
    
//    public void testCreateLocalVariable2() throws Exception {
//        performTest("org.netbeans.test.java.hints.CreateLocalVariable2", "Local", 19, 20);
//    }

    public void testCreateLocalVariable3() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateLocalVariable3", "Local Variable ", 20, 20);
    }

    public void testCreateLocalVariable4() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateLocalVariable4", "Local Variable", 8, 18);
    }

    public void testCreateLocalVariable5() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateLocalVariable5", "Local Variable", 8, 18);
    }

    public void testCreateLocalVariable6() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateLocalVariable6", "Local Variable", 8, 18);
    }

    public void testCreateLocalVariable7() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateLocalVariable7", "Local Variable", 10, 18);
    }

    public void testCreateLocalVariable8() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateLocalVariable8", "Local Variable", 9, 18);
    }

    public void testCreateLocalVariable9() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateLocalVariable9", "Local Variable", 11, 18);
    }

    public void testCreateLocalVariable10() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateLocalVariable10", "Local Variable", 10, 18);
    }

    public void testCreateParam1() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateParam1", "Parameter", 23, 15);
    }

    public void testCreateParam2() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateParam2", "Parameter", 20, 15);
    }

    public void testCreateParam3() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateParam3", "Parameter", 20, 15);
    }

    public void testCreateParam4() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateParam4", "Parameter", 9, 15);
    }

    public void testCreateParam5() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateParam5", "Parameter", 10, 15);
    }
    
//    public void testIncorrectType57991() throws Exception {
//        performTest("org.netbeans.test.java.hints.IncorrectType57991", "Field", 23, 20);
//    }
//
//    public void testImportBeforeCreateElement() throws Exception {
//        performTestDoNotPerform("org.netbeans.test.java.hints.ImportBeforeCreateElement", 10, 21);
//    }
//
//    public void testIncorrectType58119a() throws Exception {
//        performTest("org.netbeans.test.java.hints.IncorrectType58119a", "Field", 11, 9);
//    }
//
//    public void testIncorrectType58119b() throws Exception {
//        performTest("org.netbeans.test.java.hints.IncorrectType58119b", "Parameter", 9, 9);
//    }
//
//    public void testIncorrectType58119c() throws Exception {
//        performTest("org.netbeans.test.java.hints.IncorrectType58119c", "Local", 9, 9);
//    }
//
//    public void testIncorrectType58119d() throws Exception {
//        performTest(
//                "org.netbeans.test.java.hints.IncorrectType58119d",
//                "org.netbeans.test.java.hints.IncorrectType58119d", "Create", 13, 37, false);
//    }
//
//    public void testIncorrectType58119e() throws Exception {
//        performTest(
//                "org.netbeans.test.java.hints.IncorrectType58119f",
//                "org.netbeans.test.java.hints.IncorrectType58119e", "Create", 12, 24, false);
//    }
//
//    public void testTypeFromParama() throws Exception {
//        performTest(
//                "org.netbeans.test.java.hints.TypeFromParama",
//                "org.netbeans.test.java.hints.TypeFromParama", "Field", 12, 9, false);
//    }
//
//    public void testTypeFromParamb() throws Exception {
//        performTest(
//                "org.netbeans.test.java.hints.TypeFromParamb",
//                "org.netbeans.test.java.hints.TypeFromParamb", "Field", 12, 9, false);
//    }
//
//    public void testCastOrMethodInvocation58494a() throws Exception {
//        performTest("org.netbeans.test.java.hints.CastOrMethodInvocation58494a", "Cast ...", 12, 14);
//    }
//
//    public void testCastOrMethodInvocation58494b() throws Exception {
//        performTest("org.netbeans.test.java.hints.CastOrMethodInvocation58494b", "Cast ...", 13, 18);
//    }
//
//    public void testCastOrMethodInvocation58494e() throws Exception {
//        performTestDoNotPerform("org.netbeans.test.java.hints.CastOrMethodInvocation58494e", 11, 24);
//    }
//
//    public void testCastOrMethodInvocation58494g() throws Exception {
//        performTestDoNotPerform("org.netbeans.test.java.hints.CastOrMethodInvocation58494g", 13, 9);
//    }
//
//    public void testCastOrMethodInvocation58494h() throws Exception {
//        performTestDoNotPerform("org.netbeans.test.java.hints.CastOrMethodInvocation58494h", 11, 22);
//    }
//
//    public void testInitializeVariable1() throws Exception {
//        performTest("org.netbeans.test.java.hints.InitializeVariable1", "Initialize", 13, 17);
//    }
//
//    public void testInitializeVariable2() throws Exception {
//        performTest("org.netbeans.test.java.hints.InitializeVariable2", "Initialize", 12, 17);
//    }
//
//    public void testInitializeVariable3() throws Exception {
//        performTest("org.netbeans.test.java.hints.InitializeVariable3", "Initialize", 10, 1);
//    }

    public void testNonAbstractClass85806() throws Exception {
        performTestDoNotPerform("org.netbeans.test.java.hints.AbstractClass4", 8, 1);
    }

    public void testMakeClassAbstract1() throws Exception {
        performTest("org.netbeans.test.java.hints.MakeClassAbstract1", "LBL_FIX_Make_Class_Abstract", 3, 1);
    }

    public void testMakeClassAbstract2() throws Exception {
        performTest("org.netbeans.test.java.hints.MakeClassAbstract2", "LBL_FIX_Make_Class_Abstract", 3, 1);
    }

    public void testMakeClassAbstract3() throws Exception {
        performTest("org.netbeans.test.java.hints.MakeClassAbstract3", "LBL_FIX_Make_Class_Abstract", 3, 1);
    }

    @Override
    protected void copyAdditionalData() throws Exception {
        super.copyAdditionalData();
        FileObject out = packageRoot;
        FileObject src = FileUtil.toFileObject(getDataDir());
        src = src.getFileObject("org/netbeans/test/java/hints/pkg");
        FileUtil.copyFile(src, out, src.getName());
    }
    
    
    
    static {
        NbBundle.setBranding("test");
    }

}
