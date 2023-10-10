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
package org.netbeans.modules.javascript2.editor;

import java.io.IOException;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class JsStructureScannerTest extends JsTestBase {
    
    public JsStructureScannerTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void assertDescriptionMatches(FileObject fileObject,
            String description, boolean includeTestName, String ext, boolean goldenFileInTestFileDir) throws IOException {
        super.assertDescriptionMatches(fileObject, description, includeTestName, ext, true);
    }
    
    public void testFolds01() throws Exception {
        checkFolds("testfiles/simple.js");
    }
    
    public void testFolds02() throws Exception {
        checkFolds("testfiles/coloring/czechChars.js");
    }
    
    public void testIssue226142() throws Exception {
        checkFolds("testfiles/structure/issue226142.js");
    }

    public void testIssue228186() throws Exception {
        checkFolds("testfiles/structure/issue228186.js");
    }

    public void testIssue240529() throws Exception {
        checkFolds("testfiles/structure/issue240529.js");
    }

    public void testSimpleMethodChain() throws Exception {
        checkStructure("testfiles/completion/simpleMethodChain/methodChainSimple.js");
    }
    
    public void testTypeInferenceNew() throws Exception {
        checkStructure("testfiles/completion/typeInferenceNew.js");
    }
    
    public void testGetterSettterInObjectLiteral() throws Exception {
        checkStructure("testfiles/model/getterSettterInObjectLiteral.js");
    }
    
    public void testPerson() throws Exception {
        checkStructure("testfiles/model/person.js");
    }
    
    public void testAnonymousFunction() throws Exception {
        checkStructure("testfiles/model/jQueryFragment01.js");
    }
    
    public void testIssue198032() throws Exception {
        checkStructure("testfiles/coloring/issue198032.js");
    }
    
    public void testFormatter() throws Exception {
        checkStructure("testfiles/coloring/Formatter.js");
    }
    
    public void testAssignmnets01() throws Exception {
        checkStructure("testfiles/coloring/assignments01.js");
    }
    
    public void testArrays() throws Exception {
        checkStructure("testfiles/completion/arrays/arrays1.js");
    }
    
    public void testLiteralObject01() throws Exception {
        checkStructure("testfiles/completion/resolvingThis.js");
    }
    
    public void testDisplayPrototypeProperties01() throws Exception {
        checkStructure("testfiles/coloring/issue215354.js"); 
    }
    
    public void testIssue217031() throws Exception {
        checkStructure("testfiles/completion/issue217031.js"); 
    }
    
    public void testIssue216851() throws Exception {
        checkStructure("testfiles/coloring/issue216851.js"); 
    }
    
    public void testWithWooman() throws Exception {
        checkStructure("testfiles/with/woman.js"); 
    }
    
    public void testIssue216640() throws Exception {
        checkStructure("testfiles/coloring/issue216640.js"); 
    }
    
    public void testIssue218070() throws Exception {
        checkStructure("testfiles/coloring/issue218070_01.js"); 
    }

    public void testIssue149408() throws Exception {
        checkStructure("testfiles/coloring/issue149408.js");
    }
    
    public void testIssue215764() throws Exception {
        checkStructure("testfiles/completion/general/issue215764.js");
    }
    
    public void testIssue22601() throws Exception {
        checkStructure("testfiles/completion/general/issue222601.js");
    }
    
    public void testIssue222691() throws Exception {
        checkStructure("testfiles/coloring/issue222691.js");
    }
    
    public void testIssue222852() throws Exception {
        checkStructure("testfiles/coloring/issue222852.js");
    }
    
    public void testIssue222893() throws Exception {
        checkStructure("testfiles/coloring/issue222893.js");
    }
    
    public void testIssue222910() throws Exception {
        checkStructure("testfiles/coloring/issue222910.js");
    }
    
    public void testIssue222954() throws Exception {
        checkStructure("testfiles/coloring/issue222954.js");
    }
    
    public void testIssue222977() throws Exception {
        checkStructure("testfiles/coloring/issue222977.js");
    }
    
    public void testIssue223037() throws Exception {
        checkStructure("testfiles/completion/general/issue223037.js");
    }
    
    public void testIssue223121() throws Exception {
        checkStructure("testfiles/coloring/issue223121.js");
    }
    
    public void testIssue223313() throws Exception {
        checkStructure("testfiles/coloring/issue223313.js");
    }
    
    public void testIssue223306() throws Exception {
        checkStructure("testfiles/coloring/issue223306.js");
    }
    
    public void testIssue223423() throws Exception {
        checkStructure("testfiles/coloring/issue223423.js");
    }
    
    public void testIssue223264() throws Exception {
        checkStructure("testfiles/coloring/issue223264.js");
    }
    
    public void testIssue223304() throws Exception {
        checkStructure("testfiles/coloring/issue223304.js");
    }
    
    public void testIssue217029() throws Exception {
        checkStructure("testfiles/completion/issue217029.js");
    }
    
    public void testIssue215756() throws Exception {
        checkStructure("testfiles/coloring/issue215756.js");
    }
    
    public void testIssue223699() throws Exception {
        checkStructure("testfiles/coloring/issue223699.js");
    }
    
    public void testIssue217938() throws Exception {
        checkStructure("testfiles/structure/issue217938.js");
    }
    
    public void testIssue205098() throws Exception {
        checkStructure("testfiles/structure/issue205098.js");
    }
    
    public void testIssue223814() throws Exception {
        checkStructure("testfiles/coloring/issue223814.js");
    }
    
    public void testIssue216855() throws Exception {
        checkStructure("testfiles/structure/issue216855.js");
    }
    
    public void testIssue217011() throws Exception {
        checkStructure("testfiles/structure/issue217011.js");
    }

    public void testIssue224090() throws Exception {
        checkStructure("testfiles/structure/issue224090.js");
    }
    
    public void testIssue224562() throws Exception {
        checkStructure("testfiles/coloring/issue224562.js");
    }
    
    public void testIssue225755() throws Exception {
        checkStructure("testfiles/structure/issue225755.js");
    }
    
    public void testIssue225399() throws Exception {
        checkStructure("testfiles/markoccurences/issue225399.js");
    }
    
    public void testIssue224520() throws Exception {
        checkStructure("testfiles/markoccurences/issue224520.js");
    }
    
    public void testIssue226480() throws Exception {
        checkStructure("testfiles/structure/issue226480.js");
    }
    
    public void testIssue226559() throws Exception {
        checkStructure("testfiles/structure/issue226559.js");
    }
    
    public void testIssue226930() throws Exception {
        checkStructure("testfiles/structure/issue226930.js");
    }
    
    public void testIssue227163() throws Exception {
        checkStructure("testfiles/structure/issue227153.js");
    }
    
    public void testIssue222177() throws Exception {
        checkStructure("testfiles/structure/issue222177.js");
    }
    
    public void testIssue226976() throws Exception {
        checkStructure("testfiles/structure/issue226976.js");
    }
    
    public void testIssue228564() throws Exception {
        checkStructure("testfiles/completion/issue228564.js");
    }
    
    public void testIssue222952() throws Exception {
        checkStructure("testfiles/structure/issue222952.js");
    }
    
    public void testIssue226627() throws Exception {
        checkStructure("testfiles/structure/issue226627.js");
    }
    
    public void testIssue226521() throws Exception {
        checkStructure("testfiles/completion/general/issue226521.js");
    }
    
    public void testIssue226490() throws Exception {
        checkStructure("testfiles/structure/issue226490.js");
    }
    
    public void testIssue223967() throws Exception {
        checkStructure("testfiles/completion/general/issue223967.js");
    }
    
    public void testIssue223933() throws Exception {
        checkStructure("testfiles/completion/issue223933.js");
    }
    
    public void testIssue230578() throws Exception {
        checkStructure("testfiles/structure/issue230578.js");
    }
    
    public void testIssue230709() throws Exception {
        checkStructure("testfiles/structure/issue230709.js");
    }
    
    public void testIssue230736() throws Exception {
        checkStructure("testfiles/completion/general/issue230736.js");
    }
    
    public void testIssue230784() throws Exception {
        checkStructure("testfiles/completion/general/issue230784.js");
    }
    
    public void testIssue229717() throws Exception {
        checkStructure("testfiles/model/issue229717.js");
    }
    
    public void testIssue231026() throws Exception {
        checkStructure("testfiles/structure/issue231026.js");
    }
    
    public void testIssue231048() throws Exception {
        checkStructure("testfiles/structure/issue231048.js");
    }
    
    public void testIssue231059() throws Exception {
        checkStructure("testfiles/structure/issue231059.js");
    }
    
    public void testIssue231025() throws Exception {
        checkStructure("testfiles/structure/issue231025.js");
    }
    
    public void testIssue231333() throws Exception {
        checkStructure("testfiles/structure/issue231333.js");
    }
    
    public void testIssue231292() throws Exception {
        checkStructure("testfiles/structure/issue231292.js");
    }
    
    public void testIssue231688() throws Exception {
        checkStructure("testfiles/structure/issue231688.js");
    }
    
    public void testIssue231262() throws Exception {
        checkStructure("testfiles/structure/issue231262.js");
    }
    
    public void testResolvingThis() throws Exception {
        checkStructure("testfiles/structure/resolvingThis.js");
    }
    
    public void testIssue231751() throws Exception {
        checkStructure("testfiles/structure/issue231751.js");
    }
    
    public void testIssue231841() throws Exception {
        checkStructure("testfiles/structure/issue231841.js");
    }
    
    public void testIssue231752() throws Exception {
        checkStructure("testfiles/coloring/issue231752.js");
    }
    
    public void testIssue231908() throws Exception {
        checkStructure("testfiles/structure/issue231908.js");
    }
    
    public void testIssue232549() throws Exception {
        checkStructure("testfiles/structure/issue232549.js");
    }
    
    public void testIssue232570() throws Exception {
        checkStructure("testfiles/completion/issue232570.js");
    }
    
    public void testIssue232792() throws Exception {
        checkStructure("testfiles/markoccurences/issue232792.js");
    }
    
    public void testIssue232783() throws Exception {
        checkStructure("testfiles/markoccurences/issue232804.js");
    }
    
    public void testIssue232910() throws Exception {
        checkStructure("testfiles/structure/issue232910.js");
    }
    
    public void testIssue232920() throws Exception {
        checkStructure("testfiles/structure/issue232920.js");
    }
    
    public void testIssue232942() throws Exception {
        checkStructure("testfiles/structure/issue232942.js");
    }
    
    public void testIssue219508() throws Exception {
        checkStructure("testfiles/structure/issue219508.js");
    }
    
    public void testIssue219508_01() throws Exception {
        checkStructure("testfiles/structure/issue219508_01.js");
    }
    
    public void testIssue222179() throws Exception {
        checkStructure("testfiles/structure/issue222179.js");
    }
    
    public void testIssue233062() throws Exception {
        checkStructure("testfiles/structure/issue233062.js");
    }
    
    public void testIssue223593() throws Exception {
        checkStructure("testfiles/completion/issue223593.js");
    }
    
    public void testIssue231744() throws Exception {
        checkStructure("testfiles/structure/issue231744.js");
    }
    
    public void testIssue233173() throws Exception {
        checkStructure("testfiles/structure/issue233173.js");
    }
    
    public void testIssue228556() throws Exception {
        checkStructure("testfiles/structure/issue228556.js");
    }
    
    public void testIssue228289() throws Exception {
        checkStructure("testfiles/structure/issue228289.js");
    }
    
    public void testIssue233237() throws Exception {
        checkStructure("testfiles/structure/issue233237.js");
    }
    
    public void testIssue231697() throws Exception {
        checkStructure("testfiles/structure/issue231697.js");
    }
    
    public void testIssue233719() throws Exception {
        checkStructure("testfiles/structure/issue233719.js");
    }
    
    public void testIssue233738() throws Exception {
        checkStructure("testfiles/structure/issue233738.js");
    }
    
    public void testIssue222964() throws Exception {
        checkStructure("testfiles/markoccurences/issue222964/issue222964.js"); 
    }
    
    public void testIssue234430() throws Exception {
        checkStructure("testfiles/structure/issue234430.js");
    }
    
    public void testIssue234453() throws Exception {
        checkStructure("testfiles/structure/issue234453.js");
    }
    
    public void testIssue234371() throws Exception {
        checkStructure("testfiles/structure/issue234371.js");
    }
    
    public void testIssue233630A() throws Exception {
        checkStructure("testfiles/structure/issue233630A.js");
    }
    
    public void testIssue233630B() throws Exception {
        checkStructure("testfiles/structure/issue233630B.js");
    }
    
    public void testIssue234359() throws Exception {
        checkStructure("testfiles/structure/issue234359.js");
    }
    
    public void testIssue242408() throws Exception {
        checkStructure("testfiles/model/issue242408.js");
    }
    
    public void testIssue242454() throws Exception {
        checkStructure("testfiles/model/issue242454.js");
    }
    
    public void testIssue243449() throws Exception {
        checkStructure("testfiles/model/issue243449.js");
    }
    
    public void testIssue244973A() throws Exception {
        checkStructure("testfiles/markoccurences/issue244973A.js"); 
    }
    
    public void testIssue244973B() throws Exception {
        checkStructure("testfiles/markoccurences/issue244973B.js"); 
    }
    
    public void testIssue244344() throws Exception {
        checkStructure("testfiles/markoccurences/issue244344.js"); 
    }
    
    public void testIssue245488() throws Exception {
        checkStructure("testfiles/markoccurences/issue245488.js"); 
    }
    
    public void testIssue245519() throws Exception {
        checkStructure("testfiles/structure/issue245519.js");
    }
    
    public void testIssue241963() throws Exception {
        checkStructure("testfiles/structure/issue241963.js");
    }
    
    public void testIssue243140_01() throws Exception {
        checkStructure("testfiles/structure/issue243140_01.js");
    }
    
    public void testIssue243140_02() throws Exception {
        checkStructure("testfiles/structure/issue243140_02.js");
    }
    
    public void testIssue246896() throws Exception {
        checkStructure("testfiles/structure/issue246896.js");
    }
    
    public void testIssue247365() throws Exception {
        checkStructure("testfiles/structure/issue247365.js");
    }
    
    public void testIssue247564() throws Exception {
        checkStructure("testfiles/structure/issue247564.js");
    }
    
    public void testIssue237878() throws Exception {
        checkStructure("testfiles/completion/issue237878.js");
    }
    
    public void testIssue190645() throws Exception {
        checkStructure("testfiles/markoccurences/issue190645.js"); 
    }
    
    public void testIssue249006() throws Exception {
        checkStructure("testfiles/coloring/issue249006.js");
    }
    
    public void testIssue249119() throws Exception {
        checkStructure("testfiles/coloring/issue249119.js");
    }
    
    public void testIssue250112() throws Exception {
        checkStructure("testfiles/markoccurences/issue250112.js"); 
    }
    
    public void testIssue250110() throws Exception {
        checkStructure("testfiles/markoccurences/issue250110.js"); 
    }
    
    public void testCallBackDeclaration1() throws Exception {
        checkStructure("testfiles/markoccurences/callbackDeclaration1.js"); 
    }
    
    public void testCallBackDeclaration2() throws Exception {
        checkStructure("testfiles/markoccurences/callbackDeclaration2.js"); 
    }
    
    public void testIssue250392() throws Exception {
        checkStructure("testfiles/structure/issue250392.js");
    }
    
    public void testIssue251758() throws Exception {
        checkStructure("testfiles/structure/issue251758.js");
    }
    
    public void testIssue245528() throws Exception {
        checkStructure("testfiles/structure/issue245528.js");
    }
    
    public void testIssue238685_01() throws Exception {
        checkStructure("testfiles/model/issue238685_01.js");
    }
    
    public void testIssue252022() throws Exception {
        checkStructure("testfiles/hints/issue252022.js");
    }
    
    public void testIssue249487() throws Exception {
        checkStructure("testfiles/markoccurences/issue249487.js");
    }
    
    public void testIssue252375() throws Exception {
        checkStructure("testfiles/markoccurences/issue252375.js");
    }
    
    public void testIssue252028() throws Exception {
        checkStructure("testfiles/structure/issue252028.js");
    }
    
    public void testIssue234480() throws Exception {
        checkStructure("testfiles/structure/issue234480.js");
    }
    
    public void testIssue224796() throws Exception {
        checkStructure("testfiles/structure/issue224796.js");
    }
    
    public void testIssue243566() throws Exception {
        checkStructure("testfiles/coloring/issue243566.js");
    }
    
    public void testIssue246451() throws Exception {
        checkStructure("testfiles/coloring/issue246451.js");
    }
    
    public void testIssue245916() throws Exception {
        checkStructure("testfiles/structure/issue245916.js");
    }
    
    public void testIssue253128() throws Exception {
        checkStructure("testfiles/structure/issue253128.js");
    }
    
    public void testIssue253147() throws Exception {
        checkStructure("testfiles/structure/issue253147.js");
    }
    
    public void testIssue253129() throws Exception {
        checkStructure("testfiles/coloring/issue253129.js");
    }
    
    public void testIssue224463() throws Exception {
        checkStructure("testfiles/structure/issue224463.js");
    }
    
    public void testIssue233155() throws Exception {
        checkStructure("testfiles/structure/issue233155.js");
    }
    
    public void testClass01() throws Exception {
        checkStructure("testfiles/markoccurences/classes/class01.js");
    }
    
    public void testClass02() throws Exception {
        checkStructure("testfiles/markoccurences/classes/class02.js");
    }
    
    public void testClass03() throws Exception {
        checkStructure("testfiles/markoccurences/classes/class03.js");
    }
    
    public void testClass04() throws Exception {
        checkStructure("testfiles/markoccurences/classes/class04.js");
    }
    
    public void testGenerator01() throws Exception {
        checkStructure("testfiles/ecmascript6/generators/generator01.js");
    }
    
    public void testGenerator02() throws Exception {
        checkStructure("testfiles/ecmascript6/generators/generator02.js");
    }
    
    public void testGenerator03() throws Exception {
        checkStructure("testfiles/ecmascript6/generators/generator03.js");
    }
    
    public void testGenerator04() throws Exception {
        checkStructure("testfiles/ecmascript6/generators/generator04.js");
    }
    
    public void testShorthandPropertyNames01() throws Exception {
        checkStructure("testfiles/ecmascript6/shorthands/shorthandPropertyNames.js");
    }
    
    public void testShorthandMethodNames01() throws Exception {
        checkStructure("testfiles/ecmascript6/shorthands/shorthandMethodNames.js");
    }
    
    public void testComputedPropertyNames01() throws Exception {
        checkStructure("testfiles/ecmascript6/shorthands/computedPropertyNames.js");
    }
    
    public void testConstants01() throws Exception {
        checkStructure("testfiles/ecmascript6/constant/constant01.js");
    }
    
    public void testArrayDestructuringAssing01() throws Exception {
        checkStructure("testfiles/markoccurences/destructuringAssignments/arrayDestructuring01.js");
    }
    
    public void testObjectDestructuringAssing01() throws Exception {
        checkStructure("testfiles/markoccurences/destructuringAssignments/objectDestructuring01.js");
    }
    
    public void testObjectDestructuringAssing02() throws Exception {
        checkStructure("testfiles/markoccurences/destructuringAssignments/objectDestructuring02.js");
    }
    
    public void testObjectDestructuringAssing03() throws Exception {
        checkStructure("testfiles/markoccurences/destructuringAssignments/objectDestructuring03.js");
    }
    
    public void testObjectDestructuringAssing04() throws Exception {
        checkStructure("testfiles/markoccurences/destructuringAssignments/objectDestructuring04.js");
    }
    
    public void testObjectDestructuringAssing05() throws Exception {
        checkStructure("testfiles/markoccurences/destructuringAssignments/objectDestructuring05.js");
    }
    
    public void testObjectDestructuringAssing06() throws Exception {
        checkStructure("testfiles/markoccurences/destructuringAssignments/objectDestructuring05.js");
    }
    
    public void testExample01() throws Exception {
        // testing when the destructuring assignment is used as parameter definition
        checkStructure("testfiles/markoccurences/destructuringAssignments/example01.js");
    }
    
    public void testObjectPropertyAssignment01() throws Exception {
        // testing when the destructuring assignment is used as parameter definition
        checkStructure("testfiles/ecmascript6/parser/other/objectPropertyAssignment.js");
    }
    
    public void testNode01() throws Exception {
        checkStructure("testfiles/parser/node01.js");
    }
    
    public void testStrangeMethodNames() throws Exception {
        checkStructure("testfiles/markoccurences/strangeMethodName.js");
    }
    
     public void testImport01() throws Exception {
        checkStructure("testfiles/ecmascript6/importExport/import01.js");
    }
     
    public void testIssue262549() throws Exception {
        checkStructure("testfiles/structure/issue262549.js");
    }
     
    public void testIssue262590() throws Exception {
        checkStructure("testfiles/ecmascript6/importExport/issue262590.js");
    }
    
    public void testIssue262590_1() throws Exception {
        checkStructure("testfiles/ecmascript6/importExport/issue262590_1.js");
    } 
    
    public void testIssue267974() throws Exception {
        checkStructure("testfiles/markoccurences/issue267974.js");
    }
    
    public void testIssue267974_01() throws Exception {
        checkStructure("testfiles/markoccurences/issue267974_01.js");
    }
    
    public void testIssue268377() throws Exception {
        checkStructure("testfiles/structure/issue268377.js");
    }
    
    public void testIssue269106() throws Exception {
        checkStructure("testfiles/structure/issue269106.js");
    }

    public void testIssueGH4371() throws Exception {
        checkStructure("testfiles/structure/issueGH4371.js");
    }

    public void testIssueGH4262() throws Exception {
        checkStructure("testfiles/structure/issueGH4262.js");
    }

    public void testObjectNameMatchingNestedFunction() throws Exception {
        checkStructure("testfiles/structure/objectNameMatchingNestedFunction.js");
    }

    public void testClassInAnonymousFunction() throws Exception {
        checkStructure("testfiles/structure/classInAnonymousFunction.js");
    }

    public void testClassInAnonymousFunction2() throws Exception {
        checkStructure("testfiles/structure/classInAnonymousFunction2.js");
    }
}
