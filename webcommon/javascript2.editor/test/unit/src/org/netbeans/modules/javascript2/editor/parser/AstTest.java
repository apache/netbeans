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
package org.netbeans.modules.javascript2.editor.parser;

import com.oracle.js.parser.ir.FunctionNode;
import com.oracle.js.parser.ir.LexicalContext;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class AstTest extends CslTestBase {
    
    public AstTest(String testName) {
        super(testName);
    }

    public void testAdditive01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/additive/additive01.js");
    }

    public void testAdditive02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/additive/additive02.js");
    }

    public void testAdditive03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/additive/additive03.js");
    }

    public void testAssignment01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/assignment/assignment01.js");
    }

    public void testAssignment02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/assignment/assignment02.js");
    }

    public void testAssignment03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/assignment/assignment03.js");
    }

    public void testAssignment04() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/assignment/assignment04.js");
    }

    public void testAssignment05() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/assignment/assignment05.js");
    }

    public void testAssignment06() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/assignment/assignment06.js");
    }

    public void testAssignment07() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/assignment/assignment07.js");
    }

    public void testAssignment08() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/assignment/assignment08.js");
    }

    public void testAssignment09() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/assignment/assignment09.js");
    }

    public void testAssignment10() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/assignment/assignment10.js");
    }

    public void testAssignment11() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/assignment/assignment11.js");
    }

    public void testAssignment12() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/assignment/assignment12.js");
    }

    public void testAssignment13() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/assignment/assignment13.js");
    }

    public void testAssignment14() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/assignment/assignment14.js");
    }

    public void testAssignment15() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/assignment/assignment15.js");
    }

    public void testAsyncFunction01() throws Exception {
        checkAstResult("testfiles/parser/asyncFunctions/asyncFunctions1.js");
    }

    public void testAsyncFunction02() throws Exception {
        checkAstResult("testfiles/parser/asyncFunctions/asyncFunctions2.js");
    }

    public void testAsyncFunction03() throws Exception {
        checkAstResult("testfiles/parser/asyncFunctions/asyncFunctions3.js");
    }

    public void testAsyncFunction04() throws Exception {
        checkAstResult("testfiles/parser/asyncFunctions/asyncFunctions4.js");
    }

    public void testAsyncFunction05() throws Exception {
        checkAstResult("testfiles/parser/asyncFunctions/asyncFunctions5.js");
    }

    public void testAsyncFunction06() throws Exception {
        checkAstResult("testfiles/parser/asyncFunctions/asyncFunctions6.js");
    }

    public void testAsyncFunction07() throws Exception {
        checkAstResult("testfiles/parser/asyncFunctions/asyncFunctions7.js");
    }

    public void testBinary01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/binary01.js");
    }

    public void testBinary02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/binary02.js");
    }

    public void testBinary03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/binary03.js");
    }

    public void testBinary04() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/binary04.js");
    }

    public void testBinary05() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/binary05.js");
    }

    public void testBinary06() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/binary06.js");
    }

    public void testBinary07() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/binary07.js");
    }

    public void testBinary08() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/binary08.js");
    }

    public void testBinary09() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/binary09.js");
    }

    public void testBinary10() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/binary10.js");
    }

    public void testBinary11() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/binary11.js");
    }

    public void testBinary12() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/binary12.js");
    }

    public void testBinary13() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/binary13.js");
    }

    public void testBinary14() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/binary14.js");
    }

    public void testBinary15() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/binary15.js");
    }

    public void testBinary16() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/binary16.js");
    }

    public void testBinary17() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/binary17.js");
    }

    public void testBinary18() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/binary18.js");
    }

    public void testBinaryBitwise01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/bitwise01.js");
    }

    public void testBinaryBitwise02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/bitwise02.js");
    }

    public void testBinaryBitwise03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/bitwise03.js");
    }

    public void testBinaryBitwiseShift01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/bitwiseShift01.js");
    }

    public void testBinaryBitwiseShift02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/bitwiseShift02.js");
    }

    public void testBinaryBitwiseShift03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/bitwiseShift03.js");
    }

    public void testBinaryLogical01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/logical01.js");
    }

    public void testBinaryLogical02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/logical02.js");
    }

    public void testBinaryLogical03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/logical03.js");
    }

    public void testBinaryLogical04() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/logical04.js");
    }

    public void testBinaryLogical05() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/logical05.js");
    }

    public void testBinaryLogical06() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/binary/logical06.js");
    }

    public void testComplexExpression01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/complex/complex01.js");
    }

    public void testConditional01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/conditional/conditional01.js");
    }

    public void testConditional02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/conditional/conditional02.js");
    }

    public void testConditional03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/conditional/conditional03.js");
    }
    
    public void testConditional04() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/conditional/conditional04.js");
    }
    
    public void testConditional05() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/conditional/conditional05.js");
    }
    
    public void testConditional06() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/conditional/conditional06.js");
    }

    public void testDecorators01() throws Exception {
        checkAstResult("testfiles/parser/decorators/decorators1.js");
    }

    public void testDecorators02() throws Exception {
        checkAstResult("testfiles/parser/decorators/decorators2.js");
    }

    public void testDecorators03() throws Exception {
        checkAstResult("testfiles/parser/decorators/decorators3.js");
    }

    public void testDecorators04() throws Exception {
        checkAstResult("testfiles/parser/decorators/decorators4.js");
    }

    public void testDecorators05() throws Exception {
        checkAstResult("testfiles/parser/decorators/decorators5.js");
    }

    public void testDecorators06() throws Exception {
        checkAstResult("testfiles/parser/decorators/decorators6.js");
    }

    public void testDecorators07() throws Exception {
        checkAstResult("testfiles/parser/decorators/decorators7.js");
    }

    public void testDecorators08() throws Exception {
        checkAstResult("testfiles/parser/decorators/decorators8.js");
    }

    public void testDecorators09() throws Exception {
        checkAstResult("testfiles/parser/decorators/decorators9.js");
    }

    public void testDecorators10() throws Exception {
        checkAstResult("testfiles/parser/decorators/decorators10.js");
    }

    public void testEquality01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/equality/equality01.js");
    }

    public void testEquality02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/equality/equality02.js");
    }

    public void testEquality03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/equality/equality03.js");
    }

    public void testEquality04() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/equality/equality04.js");
    }

    public void testExponentiation() throws Exception {
        checkAstResult("testfiles/parser/exponentiation.js");
    }

    public void testGrouping01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/grouping/grouping01.js");
    }

    public void testGrouping02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/grouping/grouping02.js");
    }

    public void testLeftHandSide01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/leftHandSide/leftHandSide01.js");
    }

    public void testLeftHandSide02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/leftHandSide/leftHandSide02.js");
    }

    public void testLeftHandSide03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/leftHandSide/leftHandSide03.js");
    }

    public void testLeftHandSide04() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/leftHandSide/leftHandSide04.js");
    }

    public void testLeftHandSide05() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/leftHandSide/leftHandSide05.js");
    }

    public void testLeftHandSide06() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/leftHandSide/leftHandSide06.js");
    }

    public void testLeftHandSide07() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/leftHandSide/leftHandSide07.js");
    }

    public void testLeftHandSide08() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/leftHandSide/leftHandSide08.js");
    }

    public void testLeftHandSide09() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/leftHandSide/leftHandSide09.js");
    }

    public void testLeftHandSide10() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/leftHandSide/leftHandSide10.js");
    }

    public void testLeftHandSide11() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/leftHandSide/leftHandSide11.js");
    }

    public void testLeftHandSide12() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/leftHandSide/leftHandSide12.js");
    }

    public void testLeftHandSide13() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/leftHandSide/leftHandSide13.js");
    }

    public void testLeftHandSide14() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/leftHandSide/leftHandSide14.js");
    }

    public void testLeftHandSide15() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/leftHandSide/leftHandSide15.js");
    }

    public void testLeftHandSide16() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/leftHandSide/leftHandSide16.js");
    }

    public void testLeftHandSide17() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/leftHandSide/leftHandSide17.js");
    }

    public void testLeftHandSide18() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/leftHandSide/leftHandSide18.js");
    }

    public void testLeftHandSide19() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/leftHandSide/leftHandSide19.js");
    }

    public void testLeftHandSide20() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/leftHandSide/leftHandSide20.js");
    }

    public void testLeftHandSide21() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/leftHandSide/leftHandSide21.js");
    }

    public void testLeftHandSide22() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/leftHandSide/leftHandSide22.js");
    }

    public void testLeftHandSide23() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/leftHandSide/leftHandSide23.js");
    }

    public void testLeftHandSide24() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/leftHandSide/leftHandSide24.js");
    }

    public void testPrimaryArray01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/primary/array01.js");
    }

    public void testPrimaryArray02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/primary/array02.js");
    }

    public void testPrimaryArray03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/primary/array03.js");
    }

    public void testPrimaryArray04() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/primary/array04.js");
    }

    public void testPrimaryArray05() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/primary/array05.js");
    }

    public void testPrimaryArray06() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/primary/array06.js");
    }

    public void testPrimaryArray07() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/primary/array07.js");
    }

    public void testPrimaryArray08() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/primary/array08.js");
    }

    public void testRelational01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/relational/relational01.js");
    }

    public void testRelational02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/relational/relational02.js");
    }

    public void testRelational03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/relational/relational03.js");
    }

    public void testRelational04() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/relational/relational04.js");
    }

    public void testRelational05() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/relational/relational05.js");
    }

    public void testRelational06() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/relational/relational06.js");
    }

    public void testRelational07() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/relational/relational07.js");
    }

    public void testUnary01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/unary/unary01.js");
    }

    public void testUnary02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/unary/unary02.js");
    }

    public void testUnary03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/unary/unary03.js");
    }

    public void testUnary04() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/unary/unary04.js");
    }

    public void testUnary05() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/unary/unary05.js");
    }

    public void testUnary06() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/unary/unary06.js");
    }

    public void testUnary07() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/unary/unary07.js");
    }

    public void testUnary08() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/unary/unary08.js");
    }

    public void testUnary09() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/unary/unary09.js");
    }

    public void testUnary10() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/unary/unary10.js");
    }

    public void testUnary11() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/unary/unary11.js");
    }

    public void testUnary12() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/unary/unary12.js");
    }

    public void testUnary13() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/expression/unary/unary13.js");
    }

    // statements
    public void testBlock01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/block/block01.js");
    }

    public void testBlock02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/block/block02.js");
    }

    public void testBlock03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/block/block03.js");
    }
    
    public void testBlock04() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/block/block04.js");
    }

    public void testBreak01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/break/break01.js");
    }

    public void testBreak02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/break/break02.js");
    }

    public void testBreak03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/break/break03.js");
    }

    public void testBreak04() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/break/break04.js");
    }

    public void testBreak05() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/break/semicolon_newline.js");
    }

    public void testContinue01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/continue/continue01.js");
    }

    public void testContinue02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/continue/continue02.js");
    }

    public void testContinue03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/continue/continue03.js");
    }

    public void testContinue04() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/continue/continue04.js");
    }

    public void testContinue05() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/continue/continue05.js");
    }

    public void testDebugger01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/debugger/debugger01.js");
    }

    public void testDebugger02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/debugger/debugger02.js");
    }

    public void testEmpty01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/empty/empty01.js");
    }

    public void testExpression01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/expression/expression01.js");
    }

    public void testExpression02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/expression/expression02.js");
    }

    public void testIf01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/if/if01.js");
    }

    public void testIf02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/if/if02.js");
    }

    public void testIf03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/if/if03.js");
    }

    public void testIf04() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/if/if04.js");
    }

    public void testIf05() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/if/if05.js");
    }

    public void testIf06() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/if/if06.js");
    }

    public void testIf07() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/if/if07.js");
    }

    public void testDoWhileBlock01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/doWhileBlock01.js");
    }
    
    public void testIteration01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration01.js");
    }

    public void testIteration02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration02.js");
    }

    public void testIteration03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration03.js");
    }

    public void testIteration04() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration04.js");
    }

    public void testIteration05() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration05.js");
    }

    public void testIteration06() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration06.js");
    }

    public void testIteration07() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration07.js");
    }

    public void testIteration08() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration08.js");
    }

    public void testIteration09() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration09.js");
    }

    public void testIteration10() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration10.js");
    }

    public void testIteration11() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration11.js");
    }

    public void testIteration12() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration12.js");
    }

    public void testIteration13() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration13.js");
    }

    public void testIteration14() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration14.js");
    }

    public void testIteration15() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration15.js");
    }

    public void testIteration16() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration16.js");
    }

    public void testIteration17() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration17.js");
    }

    public void testIteration18() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration18.js");
    }

    public void testIteration19() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration19.js");
    }

    public void testIteration20() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration20.js");
    }

    public void testIteration21() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration21.js");
    }

    public void testIteration22() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration22.js");
    }

    public void testIteration23() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration23.js");
    }

    public void testIteration24() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration24.js");
    }

    public void testIteration25() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration25.js");
    }

    public void testIteration26() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration26.js");
    }

    public void testIteration27() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration27.js");
    }

    public void testIteration28() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/const_forin.js");
    }

    public void testIteration29() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/for-in-let.js");
    }

    public void testIteration30() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/for-let-let.js");
    }

    public void testIteration31() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/for-statement-with-seq.js");
    }

    public void testIteration32() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/invalid-strict-for-in-let.js");
    }

    public void testIteration33() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/pattern-in-for-in.js");
    }

    public void testIteration34() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/iteration/iteration34.js");
    }

    public void testLabelled01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/labelled/labelled01.js");
    }

    public void testLabelled02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/labelled/labelled02.js");
    }

    public void testLabelled03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/labelled/labelled03.js");
    }

    public void testReturn01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/return/return01.js");
    }

    public void testReturn02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/return/return02.js");
    }

    public void testReturn03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/return/return03.js");
    }

    public void testSwitch01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/switch/switch01.js");
    }

    public void testSwitch02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/switch/switch03.js");
    }

    public void testSwitch03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/switch/switch02.js");
    }

    public void testRestSpreadProperties01() throws Exception {
        checkAstResult("testfiles/parser/restSpreadProperties/restSpreadProperties1.js");
    }

    public void testRestSpreadProperties02() throws Exception {
        checkAstResult("testfiles/parser/restSpreadProperties/restSpreadProperties2.js");
    }

    public void testRestSpreadProperties03() throws Exception {
        checkAstResult("testfiles/parser/restSpreadProperties/restSpreadProperties3.js");
    }

    public void testRestSpreadProperties04() throws Exception {
        checkAstResult("testfiles/parser/restSpreadProperties/restSpreadProperties4.js");
    }

    public void testRestSpreadProperties05() throws Exception {
        checkAstResult("testfiles/parser/restSpreadProperties/restSpreadProperties5.js");
    }

    public void testReturn04() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/return/return04.js");
    }

    public void testThrow01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/throw/throw01.js");
    }

    public void testThrow02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/throw/throw02.js");
    }

    public void testThrow03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/throw/throw03.js");
    }

    public void testTry01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/try/try01.js");
    }

    public void testTry02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/try/try02.js");
    }

    public void testTry03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/try/try03.js");
    }

    public void testTry04() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/try/try04.js");
    }

    public void testTry05() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/try/try05.js");
    }

    public void testTry06() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/try/try06.js");
    }

    public void testTry07() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/try/try07.js");
    }

    public void testVariable01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/variable/variable01.js");
    }

    public void testVariable02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/variable/variable02.js");
    }

    public void testVariable03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/variable/variable03.js");
    }

    public void testVariable04() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/variable/variable04.js");
    }

    public void testVariable05() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/variable/variable05.js");
    }

    public void testVariable06() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/variable/variable06.js");
    }

    public void testVariable07() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/variable/variable07.js");
    }

    public void testVariable08() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/variable/var_let.js");
    }

    public void testVariable09() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/variable/complex-pattern-requires-init.js");
    }

    public void testVariable10() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/variable/variable10.js");
    }

    public void testWith01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/with/with01.js");
    }

    public void testWith02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/with/with02.js");
    }

    public void testWith03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/statement/with/with03.js");
    }

    //Declaration
    public void testConst01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/declaration/const/const01.js");
    }

    public void testConst02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/declaration/const/const02.js");
    }

    public void testConst03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/declaration/const/const03.js");
    }

    public void testFunction01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/declaration/function/function01.js");
    }

    public void testFunction02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/declaration/function/function02.js");
    }

    public void testFunction03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/declaration/function/function03.js");
    }

    public void testFunction04() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/declaration/function/function04.js");
    }

    public void testFunction05() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/declaration/function/function05.js");
    }

    public void testFunction06() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/declaration/function/function06.js");
    }

    public void testFunction07() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/declaration/function/function07.js");
    }

    public void testFunction08() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/declaration/function/function08.js");
    }

    public void testFunction09() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/declaration/function/function09.js");
    }

    public void testFunction10() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/declaration/function/function10.js");
    }

    public void testFunction11() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/declaration/function/function11.js");
    }

    public void testFunction12() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/declaration/function/function12.js");
    }

    public void testFunction13() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/declaration/function/function13.js");
    }

    public void testFunction14() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/declaration/function/function14.js");
    }

    public void testFunction15() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/declaration/function/function15.js");
    }

    public void testFunction16() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/declaration/function/function16.js");
    }

    public void testFunctionDupeParam() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/declaration/function/dupe-param.js");
    }

    public void testFunctionEmptyParam() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/declaration/function/empty-param.js");
    }

    public void testLet01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/declaration/let/let01.js");
    }

    public void testLet02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/declaration/let/let02.js");
    }

    public void testLet03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/declaration/let/let03.js");
    }

    public void testLet04() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/declaration/let/let04.js");
    }

    public void testArrayBindingPattern01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/array-binding-pattern/array-binding-pattern-01.js");
    }

    public void testArrayBindingPattern02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/array-binding-pattern/array-binding-pattern-02.js");
    }

    public void testArrayBindingPattern03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/array-binding-pattern/array-binding-pattern-03.js");
    }

    public void testArrayBindingPatternEmpty() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/array-binding-pattern/array-binding-pattern-empty.js");
    }

    public void testElision() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/array-binding-pattern/elision.js");
    }

    public void testInvalidDupParam() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/array-binding-pattern/invalid-dup-param.js");
    }

    public void testInvalidElisionAfterRest() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/array-binding-pattern/invalid-elision-after-rest.js");
    }

    public void testArrowFunction01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/arrow-function01.js");
    }

    public void testArrowFunction02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/arrow-function02.js");
    }

    public void testArrowFunction03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/arrow-function03.js");
    }

    public void testArrowFunction04() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/arrow-function04.js");
    }

    public void testArrowFunction05() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/arrow-function05.js");
    }

    public void testArrowFunction06() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/arrow-function06.js");
    }

    public void testArrowFunction07() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/arrow-function07.js");
    }

    public void testArrowFunction08() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/arrow-function08.js");
    }

    public void testArrowFunction09() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/arrow-function09.js");
    }

    public void testArrowFunction10() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/arrow-function10.js");
    }

    public void testArrowFunction11() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/arrow-function11.js");
    }

    public void testArrowFunction12() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/arrow-function12.js");
    }

    public void testArrowFunction13() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/arrow-function13.js");
    }

    public void testArrowFunction14() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/arrow-function14.js");
    }

    public void testArrowFunction15() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/arrow-function15.js");
    }

    public void testArrowFunction16() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/arrow-function16.js");
    }

    public void testArrowFunction17() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/arrow-function17.js");
    }

    public void testArrowFunction18() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/arrow-function18.js");
    }

    public void testArrowFunction19() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/arrow-function19.js");
    }

    public void testArrowFunction20() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/arrow-function20.js");
    }

    public void testArrowFunction21() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/arrow-function21.js");
    }

    public void testArrowRestForgettingComma() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/arrow-rest-forgetting-comma.js");
    }

    public void testArrowWithMultipleArgAndRest() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/arrow-with-multiple-arg-and-rest.js");
    }

    public void testArrowWithMultipleRest() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/arrow-with-multiple-rest.js");
    }

    public void testArrowWithOnlyRest() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/arrow-with-only-rest.js");
    }

    public void testComplexRestInArrowNotAllowed() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/complex-rest-in-arrow-not-allowed.js");
    }

    // TODO create error message that the params are duplicated
    public void testInvalidDuplicatedParams() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/invalid-duplicated-params.js");
    }

    // TODO create error message that the LineTerminator is not allowed here
    public void testInvalidLineTerminatorArrow() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/invalid-line-terminator-arrow.js");
    }

    // TODO create error message that this parameter can not be used in strinct mode
    public void testInvalidParamStrictMode() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/invalid-param-strict-mode.js");
    }

    public void testNonArrowParamFollowedByArrow() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/non-arrow-param-followed-by-arrow.js");
    }

    public void testNonArrowParamFollowedByRest() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/non-arrow-param-followed-by-rest.js");
    }

    public void testInvalidMemberExpr() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/object-binding-pattern/invalid-member-expr.js");
    }

    public void testInvalidMethodInPattern() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/object-binding-pattern/invalid-method-in-pattern.js");
    }

    // TODO this test is disabled due performance problem. Currently it takes almost 40 seconds. Need to be fixed. 
//    public void testInvalidNestedParam() throws Exception {
//        checkParserResult("testfiles/ecmascript6/parser/ES6/arrow-function/object-binding-pattern/invalid-nested-param.js");
//    }

    public void testInvalidPatternWithoutParenthesis() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/object-binding-pattern/invalid-pattern-without-parenthesis.js");
    }

    public void testInvalidRestInObjectPattern() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/object-binding-pattern/invalid-rest-in-object-pattern.js");
    }

    public void testNestedCoverGrammar() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/object-binding-pattern/nested-cover-grammar.js");
    }

    public void testObjectBindingPattern01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/object-binding-pattern/object-binding-pattern-01.js");
    }

    public void testObjectBindingPatternEmpty() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/object-binding-pattern/object-binding-pattern-empty.js");
    }

    public void testParamWithRestWithoutArrow() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/param-with-rest-without-arrow.js");
    }

    public void testRestWithoutArrow() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/arrow-function/rest-without-arrow.js");
    }

    public void testBinaryIntegerLiteral01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binary-integer-literal/binary-integer-literal01.js");
    }

    public void testBinaryIntegerLiteral02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binary-integer-literal/binary-integer-literal02.js");
    }

    public void testBinaryIntegerLiteral03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binary-integer-literal/binary-integer-literal03.js");
    }

    public void testBinaryIntegerLiteral04() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binary-integer-literal/binary-integer-literal04.js");
    }

    public void testBinaryIntegerLiteral05() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binary-integer-literal/binary-integer-literal05.js");
    }

    public void testBinaryIntegerLiteral06() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binary-integer-literal/binary-integer-literal06.js");
    }

    public void testArrayPattern01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/array-pattern/arrayPattern01.js");
    }
    
    public void testArrayPattern02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/array-pattern/arrayPattern02.js");
    }
    
    public void testArrayPattern03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/array-pattern/arrayPattern03.js");
    }
    
    public void testDupeParam() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/array-pattern/dupe-param.js");
    }

    public void testElision01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/array-pattern/elision.js");
    }

    public void testEmptyPatternCatchParam() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/array-pattern/empty-pattern-catch-param.js");
    }

    public void testEmptyPatternFn() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/array-pattern/empty-pattern-fn.js");
    }

    public void testEmptyPatternLexical() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/array-pattern/empty-pattern-lexical.js");
    }

    public void testEmptyPatternVar() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/array-pattern/empty-pattern-var.js");
    }

    public void testForLetLet() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/array-pattern/for-let-let.js");
    }

    public void testHole() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/array-pattern/hole.js");
    }

    public void testInvalidStrictForLetLet() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/array-pattern/invalid-strict-for-let-let.js");
    }

    public void testNestedPattern() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/array-pattern/nested-pattern.js");
    }

    public void testPatternedCatchDupe() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/array-pattern/patterned-catch-dupe.js");
    }

    public void testPatternedCatch() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/array-pattern/patterned-catch.js");
    }

    public void testRestElision() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/array-pattern/rest.elision.js");
    }

    public void testRest() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/array-pattern/rest.js");
    }

    public void testTailingHold() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/array-pattern/tailing-hold.js");
    }

    public void testTrailingComma01() throws Exception {
        checkAstResult("testfiles/parser/trailingCommas/trailingCommas1.js");
    }

    public void testTrailingComma02() throws Exception {
        checkAstResult("testfiles/parser/trailingCommas/trailingCommas2.js");
    }

    public void testTrailingComma03() throws Exception {
        checkAstResult("testfiles/parser/trailingCommas/trailingCommas3.js");
    }
    
    public void testTrailingComma04() throws Exception {
        checkAstResult("testfiles/parser/trailingCommas/trailingCommas4.js");
    }
    
    public void testTrailingComma05() throws Exception {
        checkAstResult("testfiles/parser/trailingCommas/trailingCommas5.js");
    }
    
    public void testTrailingComma06() throws Exception {
        checkAstResult("testfiles/parser/trailingCommas/trailingCommas6.js");
    }
    
    public void testTrailingComma07() throws Exception {
        checkAstResult("testfiles/parser/trailingCommas/trailingCommas7.js");
    }
    
    public void testTrailingComma08() throws Exception {
        checkAstResult("testfiles/parser/trailingCommas/trailingCommas8.js");
    }

    public void testTrailingComma09() throws Exception {
        checkAstResult("testfiles/parser/trailingCommas/trailingCommas9.js");
    }

    public void testVarForIn() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/array-pattern/var-for-in.js");
    }

    public void testVarLetArray() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/array-pattern/var_let_array.js");
    }

    public void testWithDefaultCatchParamFail() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/array-pattern/with-default-catch-param-fail.js");
    }

    public void testWithDefaultCatchParam() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/array-pattern/with-default-catch-param.js");
    }

    public void testWithDefaultFn() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/array-pattern/with-default-fn.js");
    }

    public void testWithObjectPattern() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/array-pattern/with-object-pattern.js");
    }

    public void testElision02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/object-pattern/elision.js");
    }

    public void testEmptyCatchParam() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/object-pattern/empty-catch-param.js");
    }

    public void testEmptyFn() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/object-pattern/empty-fn.js");
    }

    public void testEmptyForLex() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/object-pattern/empty-for-lex.js");
    }

    public void testEmptyLexical() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/object-pattern/empty-lexical.js");
    }

    public void testEmptyVar() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/object-pattern/empty-var.js");
    }

    public void testForLetLet01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/object-pattern/for-let-let.js");
    }

    public void testInvalidStrictForLetLet01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/object-pattern/invalid-strict-for-let-let.js");
    }

    public void testNested() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/object-pattern/nested.js");
    }

    public void testProperties() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/object-pattern/properties.js");
    }

    public void testVarForIn01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/binding-pattern/object-pattern/var-for-in.js");
    }

    public void testClass01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class01.js");
    }

    public void testClass02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class02.js");
    }

    public void testClass03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class03.js");
    }

    public void testClass04() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class04.js");
    }

    public void testClass05() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class05.js");
    }

    public void testClass06() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class06.js");
    }

    public void testClass07() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class07.js");
    }

    public void testClass08() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class08.js");
    }

    public void testClass09() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class09.js");
    }

    // TODO check whether name of the method can be reserved keyword. 
    public void testClass10() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class10.js");
    }

    public void testClass11() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class11.js");
    }

    public void testClass12() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class12.js");
    }

    public void testClass13() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class13.js");
    }

    public void testClass14() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class14.js");
    }

    public void testClass15() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class15.js");
    }

    // TODO check whether name of the method can be reserved keyword. 
    public void testClass16() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class16.js");
    }

    public void testClass17() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class17.js");
    }

    public void testClass18() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class18.js");
    }

    public void testClass19() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class19.js");
    }

    public void testClass20() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class20.js");
    }

    public void testClass21() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class21.js");
    }

    public void testClass22() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class22.js");
    }

    public void testClass23() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class23.js");
    }

    public void testClass24() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class24.js");
    }

    public void testClass25() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class25.js");
    }

    public void testClass26() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class26.js");
    }

    public void testClass27() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class27.js");
    }
    
    public void testClass28() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/class/class28.js");
    }
    
    public void testClassProperty1() throws Exception {
        checkAstResult("testfiles/parser/classProperty1.js");
    }

    public void testDefaultParameterValue01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/default-parameter-value/default-parameter-value01.js");
    }

    public void testDefaultParameterValue02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/default-parameter-value/default-parameter-value02.js");
    }

    public void testDefaultParameterValue03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/default-parameter-value/default-parameter-value03.js");
    }

    // TODO shouldn't be such assignments on the left side illagal?
    public void testDupAssignment() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/destructuring-assignment/array-pattern/dup-assignment.js");
    }

    public void testElision03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/destructuring-assignment/array-pattern/elision.js");
    }

    public void testMemberExprInRest() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/destructuring-assignment/array-pattern/member-expr-in-rest.js");
    }

    public void testNestedAssignment() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/destructuring-assignment/array-pattern/nested-assignment.js");
    }

    public void testNestedCoverGrammar01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/destructuring-assignment/array-pattern/nested-cover-grammar.js");
    }

    public void testSimpleAssignment() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/destructuring-assignment/array-pattern/simple-assignment.js");
    }

    public void testInvalidCoverGrammar() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/destructuring-assignment/invalid-cover-grammar.js");
    }

    public void testInvalidGroupAssignment() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/destructuring-assignment/invalid-group-assignment.js");
    }

    public void testEmptyObjectPatternAssignment() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/destructuring-assignment/object-pattern/empty-object-pattern-assignment.js");
    }

    public void testInvalidLhs01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/destructuring-assignment/object-pattern/invalid-lhs-01.js");
    }

    public void testInvalidLhs02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/destructuring-assignment/object-pattern/invalid-lhs-02.js");
    }

    public void testInvalidPatternWithMethod() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/destructuring-assignment/object-pattern/invalid-pattern-with-method.js");
    }

    public void testNestedCoverGrammar02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/destructuring-assignment/object-pattern/nested-cover-grammar.js");
    }

    public void testObjectPatternAssignment() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/destructuring-assignment/object-pattern/object-pattern-assignment.js");
    }

    public void testExportConstNumber() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-const-number.js");
    }

    public void testExportDefaultArray() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-default-array.js");
    }

    public void testExportDefaultArrayNoSemicolon() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-default-array-no-semicolon.js");
    }

    public void testExportDefaultClass() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-default-class.js");
    }

    public void testExportDefaultExpression() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-default-expression.js");
    }

    public void testExportDefaultFunction() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-default-function.js");
    }

    public void testExportDefaultNamedFunction() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-default-named-function.js");
    }

    public void testExportDefaultNumber() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-default-number.js");
    }

    public void testExportDefaultObject() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-default-object.js");
    }

    public void testExportDefaultValue() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-default-value.js");
    }

    public void testExportDefaultValueNoSemicolon() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-default-value-no-semicolon.js");
    }

    public void testExportFromBatch() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-from-batch.js");
    }

    public void testExportFromDefault() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-from-default.js");
    }

    public void testExportFromNamedAsDefault() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-from-named-as-default.js");
    }

    public void testExportFromNamedAsSpecifier() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-from-named-as-specifier.js");
    }

    public void testExportFromNamedAsSpecifiers() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-from-named-as-specifiers.js");
    }

    public void testExportFromSpecifier() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-from-specifier.js");
    }

    public void testExportFromSpecifierNoSemicolon() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-from-specifier-no-semicolon.js");
    }

    public void testExportFromSpecifiers() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-from-specifiers.js");
    }

    public void testExportFunctionDeclaration() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-function-declaration.js");
    }

    public void testExportFunction() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-function.js");
    }

    public void testExportLetNumber() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-let-number.js");
    }

    public void testExportNamedAsDefault() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-named-as-default.js");
    }

    public void testExportNamedAsSpecifier() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-named-as-specifier.js");
    }

    public void testExportNamedAsSpecifiers() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-named-as-specifiers.js");
    }

    public void testExportNamedEmpty() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-named-empty.js");
    }

    public void testExportNamedSpecifier() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-named-specifier.js");
    }

    public void testExportNamedSpecifiersComma() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-named-specifiers-comma.js");
    }

    public void testExportNamedSpecifiers() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-named-specifiers.js");
    }

    public void testExportVarAnonymousFunction() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-var-anonymous-function.js");
    }

    public void testExportVarNumber() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-var-number.js");
    }

    public void testExportVar() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/export-var.js");
    }

    public void testInvalidExportBatchMissingFromClause() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/invalid-export-batch-missing-from-clause.js");
    }

    public void testInvalidExportBatchToken() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/invalid-export-batch-token.js");
    }

    public void testInvalidExportDefaultEqual() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/invalid-export-default-equal.js");
    }

    public void testInvalidExportDefaultToken() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/invalid-export-default-token.js");
    }

    public void testInvalidExportDefault() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/invalid-export-default.js");
    }

    public void testInvalidExportNamedDefault() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/export-declaration/invalid-export-named-default.js");
    }

    public void testForOfArrayPatternLet() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/for-of/for-of-array-pattern-let.js");
    }

    public void testForOfArrayPatternVar() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/for-of/for-of-array-pattern-var.js");
    }

    public void testForOfArrayPattern() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/for-of/for-of-array-pattern.js");
    }

    public void testForOfLet() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/for-of/for-of-let.js");
    }

    public void testForOfObjectPatternConst() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/for-of/for-of-object-pattern-const.js");
    }

    public void testForOfObjectPatternVar() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/for-of/for-of-object-pattern-var.js");
    }

    public void testForOfObjectPattern() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/for-of/for-of-object-pattern.js");
    }

    public void testForOfWithConst() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/for-of/for-of-with-const.js");
    }

    public void testForOfWithLet() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/for-of/for-of-with-let.js");
    }

    public void testForOfWithVar() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/for-of/for-of-with-var.js");
    }

    public void testForOf() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/for-of/for-of.js");
    }

    public void testInvalidConstInit() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/for-of/invalid-const-init.js");
    }

    public void testInvalidForOfArrayPattern() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/for-of/invalid-for-of-array-pattern.js");
    }

    public void testInvalidForOfObjectPattern() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/for-of/invalid-for-of-object-pattern.js");
    }

    public void testInvalidLetInit() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/for-of/invalid-let-init.js");
    }

    public void testInvalidLhsInit() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/for-of/invalid-lhs-init.js");
    }

    public void testInvalidStrictForOfLet() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/for-of/invalid-strict-for-of-let.js");
    }

    public void testInvalidVarInit() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/for-of/invalid-var-init.js");
    }

    public void testInvalidConstLet() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/for-of/invalid_const_let.js");
    }

    public void testInvalidLetLet() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/for-of/invalid_let_let.js");
    }

    public void testJsx01() throws Exception {
        checkAstResult("testfiles/parser/jsx/jsx1.js");
    }

    public void testJsx02() throws Exception {
        checkAstResult("testfiles/parser/jsx/jsx2.js");
    }

    public void testJsx03() throws Exception {
        checkAstResult("testfiles/parser/jsx/jsx3.js");
    }

    public void testJsx04() throws Exception {
        checkAstResult("testfiles/parser/jsx/jsx4.js");
    }

    public void testJsx05() throws Exception {
        checkAstResult("testfiles/parser/jsx/jsx5.js");
    }

    public void testJsx06() throws Exception {
        checkAstResult("testfiles/parser/jsx/jsx6.js");
    }

    public void testJsx07() throws Exception {
        checkAstResult("testfiles/parser/jsx/jsx7.js");
    }

    public void testJsx08() throws Exception {
        checkAstResult("testfiles/parser/jsx/jsx8.js");
    }

    public void testJsx09() throws Exception {
        checkAstResult("testfiles/parser/jsx/jsx9.js");
    }

    public void testJsx10() throws Exception {
        checkAstResult("testfiles/parser/jsx/jsx10.js");
    }

    public void testJsx11() throws Exception {
        checkAstResult("testfiles/parser/jsx/jsx11.js");
    }

    public void testJsx12() throws Exception {
        checkAstResult("testfiles/parser/jsx/jsx12.js");
    }

    public void testJsx13() throws Exception {
        checkAstResult("testfiles/parser/jsx/jsx13.js");
    }

    public void testLetOfOf() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/for-of/let-of-of.js");
    }

    public void testUnexpectedNumber() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/for-of/unexpected-number.js");
    }

    public void testGeneratorDeclarationWithParams() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/generator-declaration-with-params.js");
    }

    public void testGeneratorDeclarationWithYieldDelegate() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/generator-declaration-with-yield-delegate.js");
    }

    public void testGeneratorDeclarationWithYield() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/generator-declaration-with-yield.js");
    }

    public void testGeneratorDeclaration() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/generator-declaration.js");
    }

    public void testGeneratorExpressionRestParam() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/generator-expression-rest-param.js");
    }

    public void testGeneratorExpressionWithParams() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/generator-expression-with-params.js");
    }

    public void testGeneratorExpressionWithYieldDelegate() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/generator-expression-with-yield-delegate.js");
    }

    public void testGeneratorExpressionWithYield() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/generator-expression-with-yield.js");
    }

    public void testGeneratorExpression() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/generator-expression.js");
    }

    public void testGeneratorMethodWithComputedName() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/generator-method-with-computed-name.js");
    }

    public void testGeneratorMethodWithInvalidComputedName() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/generator-method-with-invalid-computed-name.js");
    }

    public void testGeneratorMethodWithParams() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/generator-method-with-params.js");
    }

    public void testGeneratorMethodWithYieldDelegate() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/generator-method-with-yield-delegate.js");
    }

    public void testGeneratorMethodWithYieldExpression() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/generator-method-with-yield-expression.js");
    }

    public void testGeneratorMethodWithYieldLineTerminator() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/generator-method-with-yield-line-terminator.js");
    }

    public void testGeneratorMethodWithYield() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/generator-method-with-yield.js");
    }

    public void testGeneratorMethod() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/generator-method.js");
    }

    public void testGeneratorParameterBindingElement() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/generator-parameter-binding-element.js");
    }

    public void testGeneratorParameterBindingPropertyReserved() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/generator-parameter-binding-property-reserved.js");
    }

    public void testGeneratorParameterBindingProperty() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/generator-parameter-binding-property.js");
    }

    public void testGeneratorParameterComputedPropertyName() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/generator-parameter-computed-property-name.js");
    }

    public void testGeneratorParameterInvalidBindingElement() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/generator-parameter-invalid-binding-element.js");
    }

    public void testGeneratorParameterInvalidBindingProperty() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/generator-parameter-invalid-binding-property.js");
    }

    public void testGeneratorParameterInvalidComputedPropertyName() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/generator-parameter-invalid-computed-property-name.js");
    }

    public void testIncompleteYieldDelegate() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/incomplete-yield-delegate.js");
    }

    public void testMalformedGeneratorMethod2() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/malformed-generator-method-2.js");
    }

    public void testMalformedGeneratorMethod() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/malformed-generator-method.js");
    }

    public void testStaticGeneratorMethodWithComputedName() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/static-generator-method-with-computed-name.js");
    }

    public void testStaticGeneratorMethod() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/generator/static-generator-method.js");
    }

    public void testDakutenHandakuten() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/identifier/dakuten_handakuten.js");
    }

    public void testEscapedAll() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/identifier/escaped_all.js");
    }

    public void testEscapedMathAlef() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/identifier/escaped_math_alef.js");
    }

    public void testEscapedMathDalPart() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/identifier/escaped_math_dal_part.js");
    }

    public void testEscapedMathKafLam() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/identifier/escaped_math_kaf_lam.js");
    }

    public void testEscapedMathZainStart() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/identifier/escaped_math_zain_start.js");
    }

    public void testEscapedPart() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/identifier/escaped_part.js");
    }

    public void testEscapedStart() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/identifier/escaped_start.js");
    }

    public void testEstimated() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/identifier/estimated.js");
    }

    public void testEthiopicDigits() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/identifier/ethiopic_digits.js");
    }

    public void testInvalidEscapedSurrogatePairs() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/identifier/invalid_escaped_surrogate_pairs.js");
    }

    public void testInvalidExpressionAwait() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/identifier/invalid_expression_await.js");
    }

    public void testInvalidFunctionWait() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/identifier/invalid_function_wait.js");
    }

    //TODO This test fails because the used char is converted in two chars and the lexer generates two error chars, which can not be converted in UTF-8
//    public void testInvalidIdSmp() throws Exception {
//            checkParserResult ("testfiles/ecmascript6/parser/ES6/identifier/invalid_id_smp.js");
//    }
//    public void testInvalidLoneSurrogateSource() throws Exception {
//        checkAstResult("testfiles/ecmascript6/parser/ES6/identifier/invalid_lone_surrogate.source.js");
//    }

    public void testInvalidVarAwait() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/identifier/invalid_var_await.js");
    }

    //TODO These 4 tests fail because there is used char, which is converted in two chars in the lexer and it generates two error chars (instead of one), which can not be converted in UTF-8
/*    public void testMathAlef() throws Exception {
            checkParserResult ("testfiles/ecmascript6/parser/ES6/identifier/math_alef.js");
    }

   public void testMathDalPart() throws Exception {
            checkParserResult ("testfiles/ecmascript6/parser/ES6/identifier/math_dal_part.js");
   }

    public void testMathKafLam() throws Exception {
            checkParserResult ("testfiles/ecmascript6/parser/ES6/identifier/math_kaf_lam.js");
    }
    
    public void testMathZainStart() throws Exception {
            checkParserResult ("testfiles/ecmascript6/parser/ES6/identifier/math_zain_start.js");
    }
     */
    public void testModuleAwait() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/identifier/module_await.js");
    }

    public void testValidAwait() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/identifier/valid_await.js");
    }

    public void testWeierstrass() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/identifier/weierstrass.js");
    }

    public void testWeierstrassWeierstrass() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/identifier/weierstrass_weierstrass.js");
    }

    public void testImportDefaultAndNamedSpecifiers() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/import-default-and-named-specifiers.js");
    }

    public void testImportDefaultAndNamespaceSpecifiers() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/import-default-and-namespace-specifiers.js");
    }

    public void testImportDefaultAs() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/import-default-as.js");
    }

    public void testImportDefaultAsNoSemicolon() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/import-default-as-no-semicolon.js");
    }

    public void testImportDefault() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/import-default.js");
    }

    public void testImportDefaultNoSemicolon() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/import-default-no-semicolon.js");
    }

    public void testImportJquery() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/import-jquery.js");
    }

    public void testImportModule() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/import-module.js");
    }

    public void testImportModuleNoSemicolon() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/import-module-no-semicolon.js");
    }

    public void testImportNamedAsSpecifier() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/import-named-as-specifier.js");
    }

    public void testImportNamedAsSpecifiers() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/import-named-as-specifiers.js");
    }

    public void testImportNamedEmpty() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/import-named-empty.js");
    }

    public void testImportNamedSpecifier() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/import-named-specifier.js");
    }

    public void testImportNamedSpecifiersComma() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/import-named-specifiers-comma.js");
    }

    public void testImportNamedSpecifiers() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/import-named-specifiers.js");
    }

    public void testImportNamespaceSpecifier() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/import-namespace-specifier.js");
    }

    public void testImportNullAsNil() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/import-null-as-nil.js");
    }

    public void testInvalidImportDefaultAfterNamedAfterDefault() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/invalid-import-default-after-named-after-default.js");
    }

    public void testInvalidImportDefaultAfterNamed() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/invalid-import-default-after-named.js");
    }

    public void testInvalidImportDefaultMissingModuleSpecifier() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/invalid-import-default-missing-module-specifier.js");
    }

    public void testInvalidImportDefaultModuleSpecifier() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/invalid-import-default-module-specifier.js");
    }

    public void testInvalidImportDefault() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/invalid-import-default.js");
    }

    public void testInvalidImportMissingComma() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/invalid-import-missing-comma.js");
    }

    public void testInvalidImportMissingModuleSpecifier() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/invalid-import-missing-module-specifier.js");
    }

    public void testInvalidImportModuleSpecifier() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/invalid-import-module-specifier.js");
    }

    public void testInvalidImportNamedAfterNamed() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/invalid-import-named-after-named.js");
    }

    public void testInvalidImportNamedAfterNamespace() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/invalid-import-named-after-namespace.js");
    }

    public void testInvalidImportNamedAsMissingFrom() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/invalid-import-named-as-missing-from.js");
    }

    public void testInvalidImportNamespaceAfterNamed() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/invalid-import-namespace-after-named.js");
    }

    public void testInvalidImportNamespaceMissingAs() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/invalid-import-namespace-missing-as.js");
    }

    public void testInvalidImportSpecifiers() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/import-declaration/invalid-import-specifiers.js");
    }

    public void testForLetIn() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/lexical-declaration/for_let_in.js");
    }

    public void testInvalidComplexBindingWithoutInit() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/lexical-declaration/invalid_complex_binding_without_init.js");
    }

    public void testInvalidConstConst() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/lexical-declaration/invalid_const_const.js");
    }

    public void testInvalidConstForin() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/lexical-declaration/invalid_const_forin.js");
    }

    public void testInvalidConstLet01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/lexical-declaration/invalid_const_let.js");
    }

    public void testInvalidForConstDeclarations() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/lexical-declaration/invalid_for_const_declarations.js");
    }

    public void testInvalidForConstLet() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/lexical-declaration/invalid_for_const_let.js");
    }

    public void testInvalidForLetDeclarations() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/lexical-declaration/invalid_for_let_declarations.js");
    }

    public void testInvalidForLetInit() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/lexical-declaration/invalid_for_let_init.js");
    }

    public void testInvalidForLetLet() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/lexical-declaration/invalid_for_let_let.js");
    }

    public void testInvalidForLetPattern() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/lexical-declaration/invalid_for_let_pattern.js");
    }

    public void testInvalidForinConstLet() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/lexical-declaration/invalid_forin_const_let.js");
    }

    public void testInvalidForinLetLet() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/lexical-declaration/invalid_forin_let_let.js");
    }

    public void testInvalidLetDeclarations() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/lexical-declaration/invalid_let_declarations.js");
    }

    public void testInvalidLetForIn() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/lexical-declaration/invalid_let_for_in.js");
    }

    public void testInvalidLetForin() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/lexical-declaration/invalid_let_forin.js");
    }

    public void testInvalidLetInit01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/lexical-declaration/invalid_let_init.js");
    }

    public void testInvalidLetLet01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/lexical-declaration/invalid_let_let.js");
    }

    public void testInvalidStrictConstConst() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/lexical-declaration/invalid_strict_const_const.js");
    }

    public void testInvalidStrictConstLet() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/lexical-declaration/invalid_strict_const_let.js");
    }

    public void testLetIdentifier() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/lexical-declaration/let_identifier.js");
    }

    public void testLetMember() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/lexical-declaration/let_member.js");
    }

    public void testLexicalDeclaration01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/lexical-declaration/lexical-declaration01.js");
    }

    public void testModuleLet() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/lexical-declaration/module_let.js");
    }

    public void testAssignNewTarget() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/meta-property/assign-new-target.js");
    }

    public void testInvalidDots() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/meta-property/invalid-dots.js");
    }

    public void testInvalidNewTarget() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/meta-property/invalid-new-target.js");
    }

    public void testNewNewTarget() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/meta-property/new-new-target.js");
    }

    public void testNewTargetDeclaration() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/meta-property/new-target-declaration.js");
    }

    public void testNewTargetExpression() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/meta-property/new-target-expression.js");
    }

    public void testNewTargetInvoke() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/meta-property/new-target-invoke.js");
    }

    public void testNewTargetPrecedence() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/meta-property/new-target-precedence.js");
    }

    public void testUnknownProperty() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/meta-property/unknown-property.js");
    }

    public void testMethodDefinition01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/method-definition/method-definition01.js");
    }

    public void testMethodDefinition02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/method-definition/method-definition02.js");
    }

    public void testMethodDefinition03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/method-definition/method-definition03.js");
    }

    public void testMethodDefinition04() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/method-definition/method-definition04.js");
    }

    public void testMethodDefinition05() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/method-definition/method-definition05.js");
    }

    public void testInvalidProtoGetterLiteralIdentifier() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/object-initialiser/invalid-proto-getter-literal-identifier.js");
    }

    public void testInvalidProtoIdentifierLiteral() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/object-initialiser/invalid-proto-identifier-literal.js");
    }

    public void testInvalidProtoIdentifierShorthand() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/object-initialiser/invalid-proto-identifier-shorthand.js");
    }

    public void testInvalidProtoIdentifiers() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/object-initialiser/invalid-proto-identifiers.js");
    }

    public void testInvalidProtoLiteralIdentifier() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/object-initialiser/invalid-proto-literal-identifier.js");
    }

    public void testInvalidProtoLiteralShorthand() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/object-initialiser/invalid-proto-literal-shorthand.js");
    }

    public void testInvalidProtoLiterals() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/object-initialiser/invalid-proto-literals.js");
    }

    public void testInvalidProtoSetterLiteralIdentifier() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/object-initialiser/invalid-proto-setter-literal-identifier.js");
    }

    public void testInvalidProtoShorthandIdentifier() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/object-initialiser/invalid-proto-shorthand-identifier.js");
    }

    public void testInvalidProtoShorthandLiteral() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/object-initialiser/invalid-proto-shorthand-literal.js");
    }

    public void testInvalidProtoShorthands() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/object-initialiser/invalid-proto-shorthands.js");
    }

    public void testProtoIdentifierGetterSetter() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/object-initialiser/proto-identifier-getter-setter.js");
    }

    public void testProtoIdentifierGetter() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/object-initialiser/proto-identifier-getter.js");
    }

    public void testProtoIdentifierMethod() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/object-initialiser/proto-identifier-method.js");
    }

    public void testProtoIdentifierSetter() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/object-initialiser/proto-identifier-setter.js");
    }

    public void testProtoLiteralGetterSetter() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/object-initialiser/proto-literal-getter-setter.js");
    }

    public void testProtoLiteralGetter() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/object-initialiser/proto-literal-getter.js");
    }

    public void testProtoLiteralMethod() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/object-initialiser/proto-literal-method.js");
    }

    public void testProtoLiteralSetter() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/object-initialiser/proto-literal-setter.js");
    }

    public void testObjectLiteralPropertyValueShorthand01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/object-literal-property-value-shorthand/object-literal-property-value-shorthand01.js");
    }

    public void testOctalIntegerLiteral01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/octal-integer-literal/octal-integer-literal01.js");
    }

    public void testOctalIntegerLiteral02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/octal-integer-literal/octal-integer-literal02.js");
    }

    public void testOctalIntegerLiteral03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/octal-integer-literal/octal-integer-literal03.js");
    }

    public void testOctalIntegerLiteral04() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/octal-integer-literal/octal-integer-literal04.js");
    }

    public void testOctalIntegerLiteral05() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/octal-integer-literal/octal-integer-literal05.js");
    }

    public void testOctalIntegerLiteral06() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/octal-integer-literal/octal-integer-literal06.js");
    }

    public void testOctalIntegerLiteral07() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/octal-integer-literal/octal-integer-literal07.js");
    }

    public void testFunctionDeclaration() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/rest-parameter/function-declaration.js");
    }

    public void testFunctionExpression() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/rest-parameter/function-expression.js");
    }

    public void testObjectMethod() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/rest-parameter/object-method.js");
    }

    public void testObjectShorthandMethod() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/rest-parameter/object-shorthand-method.js");
    }

    public void testCallMultiSpread() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/spread-element/call-multi-spread.js");
    }

    public void testCallSpreadDefault() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/spread-element/call-spread-default.js");
    }

    public void testCallSpreadFirst() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/spread-element/call-spread-first.js");
    }

    public void testCallSpreadNumber() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/spread-element/call-spread-number.js");
    }

    public void testCallSpread() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/spread-element/call-spread.js");
    }

    public void testInvalidCallDotDot() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/spread-element/invalid-call-dot-dot.js");
    }

    public void testInvalidCallDots() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/spread-element/invalid-call-dots.js");
    }

    public void testInvalidCallSpreads() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/spread-element/invalid-call-spreads.js");
    }

    public void testInvalidNewDotDot() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/spread-element/invalid-new-dot-dot.js");
    }

    public void testInvalidNewDots() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/spread-element/invalid-new-dots.js");
    }

    public void testInvalidNewSpreads() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/spread-element/invalid-new-spreads.js");
    }

    public void testNewMultiSpread() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/spread-element/new-multi-spread.js");
    }

    public void testNewSpreadDefault() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/spread-element/new-spread-default.js");
    }

    public void testNewSpreadFirst() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/spread-element/new-spread-first.js");
    }

    public void testNewSpreadNumber() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/spread-element/new-spread-number.js");
    }

    public void testNewSpread() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/spread-element/new-spread.js");
    }

    public void testArrowSuper() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/super-property/arrow_super.js");
    }

    public void testConstructorSuper() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/super-property/constructor_super.js");
    }

    public void testInvalidSuperAccess() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/super-property/invalid_super_access.js");
    }

    public void testInvalidSuperId() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/super-property/invalid_super_id.js");
    }

    public void testInvalidSuperNotInsideFunction() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/super-property/invalid_super_not_inside_function.js");
    }

    public void testNewSuper() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/super-property/new_super.js");
    }

    public void testSuperComputed() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/super-property/super_computed.js");
    }

    public void testSuperMember() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/super-property/super_member.js");
    }

    public void testAfterSwitch() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/template-literals/after-switch.js");
    }

    public void testDollarSign() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/template-literals/dollar-sign.js");
    }

//    public void testEscapeSequencesSource() throws Exception {
//        checkAstResult("testfiles/ecmascript6/parser/ES6/template-literals/escape-sequences.source.js");
//    }

    public void testInvalidEscape() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/template-literals/invalid-escape.js");
    }

//    public void testLineTerminatorsSource() throws Exception {
//        checkAstResult("testfiles/ecmascript6/parser/ES6/template-literals/line-terminators.source.js");
//    }

    public void testLiteralEscapeSequencesSource() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/template-literals/literal-escape-sequences.source.js");
    }

    public void testNewExpression() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/template-literals/new-expression.js");
    }

    public void testOctalLiteral() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/template-literals/octal-literal.js");
    }

    public void testStrictOctalLiteral() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/template-literals/strict-octal-literal.js");
    }

    public void testTaggedInterpolation() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/template-literals/tagged-interpolation.js");
    }

    public void testTaggedNestedWithObjectLiteral() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/template-literals/tagged-nested-with-object-literal.js");
    }

    public void testTagged() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/template-literals/tagged.js");
    }

    public void testUnclosedInterpolation() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/template-literals/unclosed-interpolation.js");
    }

//    public void testUnclosedNested() throws Exception {
//        checkAstResult("testfiles/ecmascript6/parser/ES6/template-literals/unclosed-nested.js");
//    }

    public void testUnclosed() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/template-literals/unclosed.js");
    }

    public void testUntagged() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/template-literals/untagged.js");
    }

    public void testUnicodeCodePointEscapeSequence01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/unicode-code-point-escape-sequence/unicode-code-point-escape-sequence01.js");
    }

    public void testUnicodeCodePointEscapeSequence02() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/unicode-code-point-escape-sequence/unicode-code-point-escape-sequence02.js");
    }

    public void testUnicodeCodePointEscapeSequence03() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/unicode-code-point-escape-sequence/unicode-code-point-escape-sequence03.js");
    }

    public void testInvalidYieldBindingProperty() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-binding-property.js");
    }

    public void testInvalidYieldExpression() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-expression.js");
    }

    public void testInvalidYieldGeneratorArrowDefault() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-generator-arrow-default.js");
    }

    public void testInvalidYieldGeneratorArrowParameter() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-generator-arrow-parameter.js");
    }

    public void testInvalidYieldGeneratorArrowParameters() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-generator-arrow-parameters.js");
    }

    public void testInvalidYieldGeneratorCatch() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-generator-catch.js");
    }

    public void testInvalidYieldGeneratorDeclaration() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-generator-declaration.js");
    }

    public void testInvalidYieldGeneratorExportDefault() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-generator-export-default.js");
    }

    public void testInvalidYieldGeneratorExpressionName() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-generator-expression-name.js");
    }

    public void testInvalidYieldGeneratorExpressionParameter() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-generator-expression-parameter.js");
    }

    public void testInvalidYieldGeneratorExpressionRest() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-generator-expression-rest.js");
    }

    public void testInvalidYieldGeneratorFunctionDeclaration() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-generator-function-declaration.js");
    }

    public void testInvalidYieldGeneratorLexicalDeclaration() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-generator-lexical-declaration.js");
    }

    public void testInvalidYieldGeneratorMemberExpression() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-generator-member-expression.js");
    }

    public void testInvalidYieldGeneratorParameter() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-generator-parameter.js");
    }

    public void testInvalidYieldGeneratorRest() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-generator-rest.js");
    }

    public void testInvalidYieldGeneratorStrictFunctionExpression() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-generator-strict-function-expression.js");
    }

    public void testInvalidYieldGeneratorStrictFunctionParameter() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-generator-strict-function-parameter.js");
    }

    public void testInvalidYieldGeneratorVariableDeclaration() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-generator-variable-declaration.js");
    }

    public void testInvalidYieldStrictArrayPattern() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-strict-array-pattern.js");
    }

    public void testInvalidYieldStrictArrowParameterDefault() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-strict-arrow-parameter-default.js");
    }

    public void testInvalidYieldStrictArrowParameterName() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-strict-arrow-parameter-name.js");
    }

    public void testInvalidYieldStrictBindingElement() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-strict-binding-element.js");
    }

    public void testInvalidYieldStrictCatchParameter() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-strict-catch-parameter.js");
    }

    public void testInvalidYieldStrictFormalParameter() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-strict-formal-parameter.js");
    }

    public void testInvalidYieldStrictFunctionDeclaration() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-strict-function-declaration.js");
    }

    public void testInvalidYieldStrictFunctionExpression() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-strict-function-expression.js");
    }

    public void testInvalidYieldStrictIdentifier() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-strict-identifier.js");
    }

    public void testInvalidYieldStrictLexicalDeclaration() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-strict-lexical-declaration.js");
    }

    public void testInvalidYieldStrictRestParameter() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-strict-rest-parameter.js");
    }

    public void testInvalidYieldStrictVariableDeclaration() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/invalid-yield-strict-variable-declaration.js");
    }

    public void testYieldArrayPattern() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-array-pattern.js");
    }

    public void testYieldArrowConciseBody() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-arrow-concise-body.js");
    }

    public void testYieldArrowFunctionBody() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-arrow-function-body.js");
    }

    public void testYieldArrowParameterDefault() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-arrow-parameter-default.js");
    }

    public void testYieldArrowParameterName() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-arrow-parameter-name.js");
    }

    public void testYieldBindingElement() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-binding-element.js");
    }

    public void testYieldBindingProperty() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-binding-property.js");
    }

    public void testYieldCallExpressionProperty() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-call-expression-property.js");
    }

    public void testYieldCatchParameter() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-catch-parameter.js");
    }

    public void testYieldExpressionPrecedence() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-expression-precedence.js");
    }

    public void testYieldFunctionDeclarationFormalParameter() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-function-declaration-formal-parameter.js");
    }

    public void testYieldFunctionDeclaration() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-function-declaration.js");
    }

    public void testYieldFunctionExpressionParameter() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-function-expression-parameter.js");
    }

    public void testYieldFunctionExpression() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-function-expression.js");
    }

    public void testYieldGeneratorArrowConciseBody() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-generator-arrow-concise-body.js");
    }

    public void testYieldGeneratorArrowDefault() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-generator-arrow-default.js");
    }

    public void testYieldGeneratorArrowFunctionBody() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-generator-arrow-function-body.js");
    }

    public void testYieldGeneratorDeclaration() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-generator-declaration.js");
    }

    public void testYieldGeneratorDefaultParameter() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-generator-default-parameter.js");
    }

    public void testYieldGeneratorFunctionExpression() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-generator-function-expression.js");
    }

    public void testYieldGeneratorFunctionParameter() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-generator-function-parameter.js");
    }

    public void testYieldGeneratorMethod() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-generator-method.js");
    }

    public void testYieldGeneratorParameterObjectPattern() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-generator-parameter-object-pattern.js");
    }

    public void testYieldLexicalDeclaration() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-lexical-declaration.js");
    }

    public void testYieldMemberExpressionProperty() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-member-expression-property.js");
    }

    public void testYieldMethod() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-method.js");
    }

    public void testYieldParameterObjectPattern() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-parameter-object-pattern.js");
    }

    public void testYieldRestParameter() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-rest-parameter.js");
    }

    public void testYieldStrictBindingProperty() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-strict-binding-property.js");
    }

    public void testYieldStrictMethod() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-strict-method.js");
    }

    public void testYieldSuperProperty() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-super-property.js");
    }

    public void testYieldVariableDeclaration() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-variable-declaration.js");
    }

    public void testYieldYieldExpressionDelegate() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-yield-expression-delegate.js");
    }

    public void testYieldYieldExpression() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/ES6/yield/yield-yield-expression.js");
    }
    
    public void testObject01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/declaration/objectLiteral/objectLiteral01.js");
    }
    
    public void testOtherMember01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/other/member01.js");
    }
    
    public void testBackQuote01() throws Exception {
        checkAstResult("testfiles/ecmascript6/parser/other/backQuote01.js");
    }
    
    public void testImportExport01() throws Exception {
        checkAstResult("testfiles/ecmascript6/importExport/importExport01.js");
    }
    
    public void testImport01() throws Exception {
        checkAstResult("testfiles/ecmascript6/importExport/importFindDeclaration01.js");
    }
    
    public void testExportDefalt01() throws Exception {
        checkAstResult("testfiles/ecmascript6/importExport/exportdefault01.js");
    }
    
    public void testObjectDestructuringAssignment01() throws Exception {
        checkAstResult("testfiles/markoccurences/destructuringAssignments/objectDestructuring01.js");
    }
    
    public void testDestructuringAssignmentExample01() throws Exception {
        checkAstResult("testfiles/markoccurences/destructuringAssignments/example01.js");
    }
    
    public void testDestructuringAssignmentArray01() throws Exception {
        checkAstResult("testfiles/markoccurences/destructuringAssignments/arrayDestructuring01.js");
    }
    
    public void testDestructuringAssignmentArray02() throws Exception {
        checkAstResult("testfiles/markoccurences/destructuringAssignments/arrayDestructuring02.js");
    }
    
    public void testIssue262590() throws Exception {
        checkAstResult("testfiles/ecmascript6/importExport/issue262590.js");
    }
    
//    public void testModelObject01() throws Exception {
//        checkAstResult("testfiles/model/objects/object01.js");
//    }
//    
//    public void testModelObject02() throws Exception {
//        checkAstResult("testfiles/model/objects/object02.js");
//    }
//    
//    public void testModelObject05() throws Exception {
//        checkAstResult("testfiles/model/objects/object05.js");
//    }
    
//    public void testModelMemberExpression01() throws Exception {
//        checkAstResult("testfiles/model/memberExpressions/memberExpression01.js");
//    }
    
//    public void testModelMemberExpression02() throws Exception {
//        checkAstResult("testfiles/model/memberExpressions/memberExpression02.js");
//    }
    
//    public void testModelMemberExpression03() throws Exception {
//        checkAstResult("testfiles/model/memberExpressions/memberExpression03.js");
//    }
    
//    public void testModelMemberExpression04() throws Exception {
//        checkAstResult("testfiles/model/memberExpressions/memberExpression04.js");
//    }
    
//    public void testModelMemberExpression06() throws Exception {
//        checkAstResult("testfiles/model/memberExpressions/memberExpression06.js");
//    }
    
//    public void testModelMemberExpression07() throws Exception {
//        checkAstResult("testfiles/model/memberExpressions/memberExpression07.js");
//    }
    
//    public void testFunctionInGlobal() throws Exception {
//        checkAstResult("testfiles/model/functionInGlobal.js");
//    }
    
    public void testCzechChars() throws Exception {
        checkAstResult("testfiles/coloring/czechChars.js");
    }
    
    public void testIssue262469() throws Exception {
        checkAstResult("testfiles/markoccurences/issue262469.js");
    }
    
    public void testRegexpInTemplate() throws Exception {
        checkAstResult("testfiles/parser/regexpInTemplate.js");
    }
    
//    public void testModelClass01() throws Exception {
//        checkAstResult("testfiles/model/class/class01.js");
//    }
//    
//    public void testModelClass04() throws Exception {
//        checkAstResult("testfiles/model/class/class04.js");
//    }
//    
//    public void testModelClass05() throws Exception {
//        checkAstResult("testfiles/model/class/class05.js");
//    }
    
//    public void testModelReturnObjectLiteral01() throws Exception {
//        checkAstResult("testfiles/model/functions/returnObjectLiteral01.js");
//    }
    
    public void testIssue269061() throws Exception {
        checkAstResult("testfiles/parser/issue269061.js");
    }
    
    private void checkAstResult(String relFilePath) throws Exception {
        FileObject testFO = getTestFile(relFilePath);
        if (testFO == null) {
            NbTestCase.fail("File " + testFO.getNameExt() + " not found.");
        }

        Source source = getTestSource(testFO);
        Snapshot snapshot = source.createSnapshot();

        JsParser parser = new JsParser();
        SanitizingParser.Context context = new JsParser.Context(testFO.getNameExt(), snapshot, -1, JsTokenId.javascriptLanguage());
        JsErrorManager manager = new JsErrorManager(snapshot, JsTokenId.javascriptLanguage());

        JsParserResult result = parser.parseSource(context, manager);
        FunctionNode program = result.getRoot();
        LexicalContext lc = new LexicalContext();
        AstXmlVisitor visitor = new AstXmlVisitor(lc);
        String xmlTree = "";
        if (program != null) {
            program.accept(visitor);
            xmlTree = visitor.getXmTree();
        }

        assertDescriptionMatches(testFO, xmlTree, false, ".ast.xml", true);
    }
}
