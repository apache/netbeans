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
package org.netbeans.modules.javascript2.editor;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class JsSemanticAnalyzerTest extends JsTestBase {

    public JsSemanticAnalyzerTest(String testName) {
        super(testName);
    }

    @Override
    protected Object[] createExtraMockLookupContent() {
        return new Object[]{
            new FileEncodingQueryImplementation() {
                @Override
                public Charset getEncoding(FileObject file) {
                    return StandardCharsets.UTF_8;
                }
            }
        };
    }
    
    public void testAsyncFunction01() throws Exception {
        checkSemantic("testfiles/parser/asyncFunctions/asyncFunctions1.js");
    }

    public void testAsyncFunction02() throws Exception {
        checkSemantic("testfiles/parser/asyncFunctions/asyncFunctions2.js");
    }

    public void testAsyncFunction03() throws Exception {
        checkSemantic("testfiles/parser/asyncFunctions/asyncFunctions3.js");
    }

    public void testAsyncFunction04() throws Exception {
        checkSemantic("testfiles/parser/asyncFunctions/asyncFunctions4.js");
    }

    public void testAsyncFunction05() throws Exception {
        checkSemantic("testfiles/parser/asyncFunctions/asyncFunctions5.js");
    }

    public void testAsyncFunction06() throws Exception {
        checkSemantic("testfiles/parser/asyncFunctions/asyncFunctions6.js");
    }

    public void testAsyncFunction07() throws Exception {
        checkSemantic("testfiles/parser/asyncFunctions/asyncFunctions7.js");
    }

    public void testObjectAsParam() throws Exception {
        // TODO arguments can not be handled as global 
        checkSemantic("testfiles/model/objectAsParameter.js");
    }
    
    public void testjQueryFragment01() throws Exception {
        checkSemantic("testfiles/model/jQueryFragment01.js");
    }
    
    public void testCzechChars() throws Exception {
        checkSemantic("testfiles/coloring/czechChars.js");
    }
    
    public void testGetterSetterInObjectLiteral() throws Exception {
        checkSemantic("testfiles/model/getterSettterInObjectLiteral.js");
    }
    
    public void testIssue209717_01() throws Exception {
        checkSemantic("testfiles/coloring/issue209717_01.js");
    }
    
    public void testIssue209717_02() throws Exception {
        checkSemantic("testfiles/coloring/issue209717_02.js");
    }
    
    public void testIssue209717_03() throws Exception {
        checkSemantic("testfiles/coloring/issue209717_03.js");
    }
    
    public void testIssue209717_04() throws Exception {
        checkSemantic("testfiles/coloring/issue209717_04.js");
    }
    
    public void testFormatter() throws Exception {
        checkSemantic("testfiles/coloring/Formatter.js"); 
    }
    
    public void testAssignments01() throws Exception {
        checkSemantic("testfiles/coloring/assignments01.js"); 
    }
    
    public void testIssue213968() throws Exception {
        checkSemantic("testfiles/coloring/issue213968.js"); 
    }
    
    public void testIssue215354() throws Exception {
        checkSemantic("testfiles/coloring/issue215354.js"); 
    }
    
    public void testIssue214982() throws Exception {
        checkSemantic("testfiles/coloring/issue214982.js"); 
    }
    
    public void testIssue215554() throws Exception {
        checkSemantic("testfiles/coloring/issue215554.js"); 
    }
    
    public void testIssue215755() throws Exception {
        checkSemantic("testfiles/coloring/issue215755.js"); 
    }
    
    public void testUnusedVariables01() throws Exception {
        checkSemantic("testfiles/hints/weirdAssignment.js"); 
    }
    
    public void testUnusedVariables02() throws Exception {
        checkSemantic("testfiles/coloring/unusedVariables.js"); 
    }
    
    public void testUnusedVariables03() throws Exception {
        checkSemantic("testfiles/coloring/unusedVariable02.js"); 
    }
    
    public void testUnusedVariables04() throws Exception {
        checkSemantic("testfiles/coloring/unusedVariable03.js"); 
    }
    
    public void testUnusedArrowParameter01() throws Exception {
        checkSemantic("testfiles/hints/arrowFunction.js"); 
    }
    
    public void testIssue217443() throws Exception {
        checkSemantic("testfiles/coloring/issue217443.js"); 
    }

    public void testIssue218230_01() throws Exception {
        checkSemantic("testfiles/coloring/issue218230.js");
    }

    public void testIssue218230_02() throws Exception {
        checkSemantic("testfiles/markoccurences/testDocumentation/testDocumentation.js");
    }

    public void testIssue218231() throws Exception {
        checkSemantic("testfiles/coloring/issue218231.js");
    }

    public void testIssue215839() throws Exception {
        checkSemantic("testfiles/coloring/issue215839.js");
    }

    public void testIssue137317_01() throws Exception {
        checkSemantic("testfiles/markoccurences/issue137317.js");
    }

    public void testIssue180919() throws Exception {
        checkSemantic("testfiles/coloring/issue180919.js");
    }

    public void testIssue188431() throws Exception {
        checkSemantic("testfiles/coloring/issue198431.js");
    }

    public void testIssue218561() throws Exception {
        checkSemantic("testfiles/coloring/issue218561.js");
    }

    public void testIssue219044() throws Exception {
        checkSemantic("testfiles/coloring/issue219044.js");
    }

    public void testIssue219634() throws Exception {
        checkSemantic("testfiles/coloring/issue219634.js");
    }

    public void testIssue220102() throws Exception {
        checkSemantic("testfiles/coloring/issue220102.js");
    }

    public void testIssue220735() throws Exception {
        checkSemantic("testfiles/coloring/issue220735.js");
    }

    public void testIssue220891() throws Exception {
        checkSemantic("testfiles/coloring/issue220891.js");
    }

    public void testIssue221464() throws Exception {
        checkSemantic("testfiles/coloring/issue221464.js");
    }
    
    public void testIssue222498() throws Exception {
        checkSemantic("testfiles/markoccurences/issue222498.js");
    }
    
    public void testIssue218191() throws Exception {
        checkSemantic("testfiles/markoccurences/issue218191.js");
    }
    
    public void testIssue218136() throws Exception {
        // this basically reflects also issue 222880
        checkSemantic("testfiles/markoccurences/issue218136.js");
    }
    
    public void testIssue218090() throws Exception {
        checkSemantic("testfiles/coloring/issue218090.js");
    }
    
    public void testIssue218041() throws Exception {
        checkSemantic("testfiles/coloring/issue218041.js");
    }
    
    public void testIssue223037() throws Exception {
        checkSemantic("testfiles/completion/general/issue223037.js");
    }
    
    public void testIssue218100() throws Exception {
        checkSemantic("testfiles/coloring/issue218100.js");
    }

    public void testIssue223109() throws Exception {
        checkSemantic("testfiles/coloring/issue223109.js");
    }

    public void testIssue218467() throws Exception {
        checkSemantic("testfiles/coloring/issue218467.js");
    }
    
    public void testIssue223465() throws Exception {
        checkSemantic("testfiles/markoccurences/issue223465.js");
    }
    
    public void testIssue223699() throws Exception {
        checkSemantic("testfiles/coloring/issue223699.js");
    }
    
    public void testIssue223823() throws Exception {
        checkSemantic("testfiles/markoccurences/issue223823.js");
    }
    
    public void testIssue216262() throws Exception {
        checkSemantic("testfiles/coloring/issue216262.js");
    }
    
    public void testIssue224036() throws Exception {
        checkSemantic("testfiles/coloring/issue224036.js"); 
    }
    
    public void testIssue224215() throws Exception {
        checkSemantic("testfiles/markoccurences/issue224215.js");
    }
    
    public void testIssue225399() throws Exception {
        checkSemantic("testfiles/markoccurences/issue225399.js");
    }
    
    public void testIssue224520() throws Exception {
        checkSemantic("testfiles/markoccurences/issue224520.js");
    }
    
    public void testIssue229838() throws Exception {
        checkSemantic("testfiles/coloring/issue229838.js"); 
    }
    
    public void testIssue225098() throws Exception {
        checkSemantic("testfiles/coloring/issue225098.js"); 
    }
    
    public void testArrayLiteral() throws Exception {
        checkSemantic("testfiles/completion/arrays/arrayliteral.js");
    }
    
    public void testIssue231430() throws Exception {
        checkSemantic("testfiles/coloring/issue231430.js"); 
    }
    
    public void testIssue231848() throws Exception {
        checkSemantic("testfiles/coloring/issue231848.js"); 
    }
    
    public void testIssue231752() throws Exception {
        checkSemantic("testfiles/coloring/issue231752.js"); 
    }
    
    public void testIssue231921() throws Exception {
        checkSemantic("testfiles/coloring/issue231921.js"); 
    }
    
    public void testIssue232595() throws Exception {
        checkSemantic("testfiles/markoccurences/issue232595.js"); 
    }
    
    public void testIssue212319() throws Exception {
        checkSemantic("testfiles/coloring/issue212319.js"); 
    }
    
    public void testIssue215757() throws Exception {
        checkSemantic("testfiles/coloring/issue215757.js"); 
    }
    
    public void testIssue217769() throws Exception {
        checkSemantic("testfiles/markoccurences/issue217769.js"); 
    }
    
    public void testIssue233057() throws Exception {
        checkSemantic("testfiles/structure/issue219508.js"); 
    }
    
    public void testIssue233567() throws Exception {
        checkSemantic("testfiles/coloring/issue233567.js"); 
    }
    
    public void testIssue233719() throws Exception {
        checkSemantic("testfiles/structure/issue233719.js"); 
    }
    
    public void testIssue233787() throws Exception {
        checkSemantic("testfiles/markoccurences/issue233787.js"); 
    }
    
    public void testIssue233720() throws Exception {
        checkSemantic("testfiles/markoccurences/issue233720.js"); 
    }
    
    public void testIssue222964() throws Exception {
        checkSemantic("testfiles/markoccurences/issue222964/issue222964.js"); 
    }
    
    public void testIssue234359() throws Exception {
        checkSemantic("testfiles/structure/issue234359.js"); 
    }
    
    public void testIssue235793() throws Exception {
        checkSemantic("testfiles/coloring/issue235793.js"); 
    }
    
    public void testIssue238465() throws Exception {
        checkSemantic("testfiles/coloring/issue238465.js"); 
    }
    
    public void testIssue238499() throws Exception {
        checkSemantic("testfiles/markoccurences/issue238499.js");
    }
    
    public void testIssue242408() throws Exception {
        checkSemantic("testfiles/model/issue242408.js");
    }
    
    public void testIssue242454() throws Exception {
        checkSemantic("testfiles/model/issue242454.js");
    }
    
    public void testIssue242421() throws Exception {
        checkSemantic("testfiles/markoccurences/issue242421.js");
    }
    
    public void testIssue243449() throws Exception {
        checkSemantic("testfiles/model/issue243449.js");
    }
    
    public void testIssue244973A() throws Exception {
        checkSemantic("testfiles/markoccurences/issue244973A.js"); 
    }
    
    public void testIssue244973B() throws Exception {
        checkSemantic("testfiles/markoccurences/issue244973B.js"); 
    }
    
    public void testIssue244989() throws Exception {
        checkSemantic("testfiles/coloring/issue244989.js"); 
    }
    
    public void testIssue244344() throws Exception {
        checkSemantic("testfiles/markoccurences/issue244344.js"); 
    }
    
    public void testIssue245445() throws Exception {
        checkSemantic("testfiles/markoccurences/issue245445.js"); 
    }
    
    public void testIssue246896() throws Exception {
        checkSemantic("testfiles/structure/issue246896.js");
    }
    
    public void testIssue246581() throws Exception {
        checkSemantic("testfiles/coloring/issue246581.js");
    }
    
    public void testIssue249006() throws Exception {
        checkSemantic("testfiles/coloring/issue249006.js");
    }
    
    public void testIssue249119() throws Exception {
        checkSemantic("testfiles/coloring/issue249119.js");
    }
    
    public void testIssue249619() throws Exception {
        checkSemantic("testfiles/markoccurences/issue249619.js");
    }
    
    public void testCallBackDeclaration1() throws Exception {
        checkSemantic("testfiles/markoccurences/callbackDeclaration1.js"); 
    }
    
    public void testCallBackDeclaration2() throws Exception {
        checkSemantic("testfiles/markoccurences/callbackDeclaration2.js"); 
    }
    
    public void testIssue248696_01() throws Exception {
        checkSemantic("testfiles/hints/issue248696_01.js");
    }

    public void testIssue250337() throws Exception {
        checkSemantic("testfiles/coloring/issue250337.js");
    }
    
    public void testIssue251778() throws Exception {
        checkSemantic("testfiles/coloring/issue251778.js");
    }
    
    public void testIssue251911() throws Exception {
        checkSemantic("testfiles/model/issue251911.js");
    }
    
    public void testIssue251819() throws Exception {
        checkSemantic("testfiles/coloring/issue251819.js");
    }
    
    public void testIssue242454A() throws Exception {
        checkSemantic("testfiles/completion/issue242454A.js");
    }
    
    public void testIssue251984() throws Exception {
        checkSemantic("testfiles/markoccurences/issue251984.js");
    }
    
    public void testIssue252022() throws Exception {
        checkSemantic("testfiles/hints/issue252022.js");
    }
 
    public void testIssue249487() throws Exception {
        checkSemantic("testfiles/markoccurences/issue249487.js");
    }
    
    public void testIssue252375() throws Exception {
        checkSemantic("testfiles/markoccurences/issue252375.js");
    }
    
    public void testIssue226977_01() throws Exception {
        checkSemantic("testfiles/coloring/issue226977_01.js");
    }
    
    public void testIssue226977_02() throws Exception {
        checkSemantic("testfiles/coloring/issue226977_02.js");
    }
    
    public void testIssue252469() throws Exception {
        checkSemantic("testfiles/coloring/issue252469.js");
    }
    
    public void testIssue224075() throws Exception {
        checkSemantic("testfiles/coloring/issue224075.js");
    }
    
    public void testIssue252655() throws Exception {
        checkSemantic("testfiles/coloring/issue252655.js");
    }
    
    public void testIssue252656() throws Exception {
        checkSemantic("testfiles/coloring/issue252656.js");
    }
    
    public void testIssue243566() throws Exception {
        checkSemantic("testfiles/coloring/issue243566.js");
    }
    
    public void testIssue237914() throws Exception {
        checkSemantic("testfiles/markoccurences/issue237914.js");
    }
    
    public void testIssue246451() throws Exception {
        checkSemantic("testfiles/coloring/issue246451.js");
    }
    
    public void testIssue253128() throws Exception {
        checkSemantic("testfiles/structure/issue253128.js");
    }
    
    public void testIssue253129() throws Exception {
        checkSemantic("testfiles/coloring/issue253129.js");
    }
    
    public void testIssue253348() throws Exception {
        checkSemantic("testfiles/coloring/issue253348.js");
    }
    
    public void testIssue253736() throws Exception {
        checkSemantic("testfiles/markoccurences/issue253736.js");
    }
    
    public void testIssue255494() throws Exception {
        checkSemantic("testfiles/coloring/issue255494.js");
    }
    
    public void testIssue258857() throws Exception {
        checkSemantic("testfiles/coloring/issue258857.js");
    }
    
    public void testIssue258968() throws Exception {
        checkSemantic("testfiles/coloring/issue258968.js");
    }
    
    public void testClass01() throws Exception {
        checkSemantic("testfiles/markoccurences/classes/class01.js");
    }
    
    public void testClass02() throws Exception {
        checkSemantic("testfiles/markoccurences/classes/class02.js");
    }
    
    public void testClass03() throws Exception {
        checkSemantic("testfiles/markoccurences/classes/class03.js");
    }
    
    public void testClass04() throws Exception {
        checkSemantic("testfiles/markoccurences/classes/class04.js");
    }
    
    public void testGenerator01() throws Exception {
        checkSemantic("testfiles/ecmascript6/generators/generator01.js");
    }
    
    public void testGenerator02() throws Exception {
        checkSemantic("testfiles/ecmascript6/generators/generator02.js");
    }
    
    public void testGenerator03() throws Exception {
        checkSemantic("testfiles/ecmascript6/generators/generator03.js");
    }
    
    public void testGenerator04() throws Exception {
        checkSemantic("testfiles/ecmascript6/generators/generator04.js");
    }
    
    public void testFunctionDeclaration05() throws Exception {
        checkSemantic("testfiles/markoccurences/functionDeclaration/functionDeclaration05.js");
    }
   
    public void testShorthandPropertyNames01() throws Exception {
        checkSemantic("testfiles/ecmascript6/shorthands/shorthandPropertyNames.js");
    }
    
    public void testShorthandMethodNames01() throws Exception {
        checkSemantic("testfiles/ecmascript6/shorthands/shorthandMethodNames.js");
    }
    
    public void testComputedPropertyNames01() throws Exception {
        checkSemantic("testfiles/ecmascript6/shorthands/computedPropertyNames.js");
    }
    
    public void testNumberLiterals() throws Exception {
        checkSemantic("testfiles/completion/general/numberLiterals01.js");
    }
    
    public void testArrayDestructuringAssing01() throws Exception {
        checkSemantic("testfiles/markoccurences/destructuringAssignments/arrayDestructuring01.js");
    }
    
    public void testObjectDestructuringAssing01() throws Exception {
        checkSemantic("testfiles/markoccurences/destructuringAssignments/objectDestructuring01.js");
    }
    
    public void testObjectDestructuringAssing02() throws Exception {
        checkSemantic("testfiles/markoccurences/destructuringAssignments/objectDestructuring02.js");
    }
    
    public void testObjectDestructuringAssing03() throws Exception {
        checkSemantic("testfiles/markoccurences/destructuringAssignments/objectDestructuring03.js");
    }
    
    public void testObjectDestructuringAssing04() throws Exception {
        checkSemantic("testfiles/markoccurences/destructuringAssignments/objectDestructuring04.js");
    }
    
    public void testObjectDestructuringAssing05() throws Exception {
        checkSemantic("testfiles/markoccurences/destructuringAssignments/objectDestructuring05.js");
    }
    
    public void testObjectDestructuringAssing06() throws Exception {
        checkSemantic("testfiles/markoccurences/destructuringAssignments/objectDestructuring06.js");
    }
    
    public void testExample01() throws Exception {
        // testing when the destructuring assignment is used as parameter definition
        checkSemantic("testfiles/markoccurences/destructuringAssignments/example01.js");
    }
    
    public void testObjectPropertyAssignment01() throws Exception {
        // testing when the destructuring assignment is used as parameter definition
        checkSemantic("testfiles/ecmascript6/parser/other/objectPropertyAssignment.js");
    }
    
    public void testSemanticalKeywords() throws Exception {
        checkSemantic("testfiles/coloring/semanticalKeywords.js");
    }
    
    public void testBlockScope05() throws Exception {
        checkSemantic("testfiles/markoccurences/blockscope/scope05.js");
    }
    
    public void testArrayLiteralInBlockScope01() throws Exception {
        checkSemantic("testfiles/markoccurences/blockscope/arrayLiteral01.js");
    }
    
    public void testDeclorators8() throws Exception {
        checkSemantic("testfiles/parser/decorators/decorators8.js");
    }

    public void testIssue262590() throws Exception {
        checkSemantic("testfiles/ecmascript6/importExport/issue262590.js");
    }
    
    public void testIssue262590_1() throws Exception {
        checkSemantic("testfiles/ecmascript6/importExport/issue262590_1.js");
    }
    
    public void testIssue257509() throws Exception {
        checkSemantic("testfiles/coloring/issue257509.js");
    }
    
    public void testIssue267423() throws Exception {
        checkSemantic("testfiles/coloring/issue267423.js");
    }
    
    public void testIssue254189() throws Exception {
        checkSemantic("testfiles/coloring/issue254189.js");
    }
    
    public void testIssue246239() throws Exception {
        checkSemantic("testfiles/markoccurences/issue246239.js");
    }
    
    public void testIssue267694() throws Exception {
        checkSemantic("testfiles/markoccurences/issue267694.js");
    }
    
    public void testIssue252755_01() throws Exception {
        checkSemantic("testfiles/markoccurences/issue252755_01.js");
    }
    
    public void testIssue252755_02() throws Exception {
        checkSemantic("testfiles/markoccurences/issue252755_02.js");
    }
    
    public void testIssue231627() throws Exception {
        checkSemantic("testfiles/markoccurences/issue231627.js");
    }
}
