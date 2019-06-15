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
package org.netbeans.modules.java.editor.base.semantic;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.test.support.MemoryValidator;
import org.netbeans.modules.java.editor.base.semantic.ColoringAttributes.Coloring;
import org.netbeans.modules.java.editor.options.MarkOccurencesSettings;
import org.netbeans.modules.java.editor.base.semantic.TestBase.Performer;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.openide.text.NbDocument;
import org.openide.util.Pair;

/**XXX: constructors throwing an exception are not marked as exit points
 *
 * @author Jan Lahoda
 */
public class MarkOccDetTest extends TestBase {
    
    public MarkOccDetTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return MemoryValidator.wrap(new TestSuite(MarkOccDetTest.class));
    }
    
    public void testExitPointForReturnTypesVoid() throws Exception {
        performTest("ExitPoints", 7, 12);
    }

    public void testExitPointForReturnTypesInt() throws Exception {
        performTest("ExitPoints", 17, 12);
    }

    public void testExitPointForReturnTypesObject() throws Exception {
        performTest("ExitPoints", 27, 12);
    }

    public void testExitPt4RetTypesVoid4MethThrowExc() throws Exception {
        performTest("ExitPoints", 37, 12);
    }

    public void testExitPointForThrowsNPE() throws Exception {
        performTest("ExitPoints", 37, 45);
    }

    public void testExitPointForThrowsBLE() throws Exception {
        performTest("ExitPoints", 37, 85);
    }

    public void testExitPointForArray() throws Exception {
        performTest("ExitPoints", 104, 13);
    }

    public void testExitPointStartedMethod() throws Exception {
        performTest("ExitPointsStartedMethod", 5, 13);
    }

    public void testExitPointEmptyMethod() throws Exception {
        performTest("ExitPointsEmptyMethod", 5, 13);
    }

    public void testExitPointForParametrizedReturnType() throws Exception {
        performTest("ExitPoints", 100, 20);
        performTest("ExitPoints", 100, 25);
        performTest("ExitPoints", 100, 30);
    }

    public void testThis() throws Exception {
        performTest("ExitPoints", 130, 10);
    }

    public void testSuper() throws Exception {
        performTest("ExitPoints", 138, 10);
    }

    public void testConstructorThrows() throws Exception {
        performTest("ExitPoints", 88, 90, true);
    }

    public void testUsagesField1() throws Exception {
        performTest("Usages", 7, 19);
        performTest("Usages", 14, 10);
        performTest("Usages", 17, 17);
        performTest("Usages", 26, 10);
    }

    public void testUsagesField2() throws Exception {
        performTest("Usages", 6, 18);
    }

    public void testUsagesField3() throws Exception {
        performTest("Usages", 10, 14);
        performTest("Usages", 13, 9);
        performTest("Usages", 20, 13);
        performTest("Usages", 25, 9);
    }

    public void testUsagesField4() throws Exception {
        performTest("Usages", 11, 16);
        performTest("Usages", 15, 10);
        performTest("Usages", 22, 14);
        performTest("Usages", 27, 10);
    }

    public void testUsagesField5() throws Exception {
        performTest("Usages", 18, 19);
        performTest("Usages", 21, 14);
    }

    public void testUsagesMethodTrivial() throws Exception {
        performTest("Usages", 9, 20);
        performTest("Usages", 29, 11);
    }

    public void testUsagesIgnore() throws Exception {
        performTest("Usages", 9, 24);
        performTest("Usages", 4, 21);
    }

    public void testUsagesClassTrivial() throws Exception {
        performTest("Usages", 4, 17);
        performTest("Usages", 31, 12);
    }

    public void testMemberSelect1() throws Exception {
        performTest("MemberSelect", 2, 29);
        performTest("MemberSelect", 6, 70);
        performTest("MemberSelect", 7, 30);
    }

    public void testMemberSelect2() throws Exception {
        performTest("MemberSelect", 14, 33);
    }

    public void testMemberSelect3() throws Exception {
        performTest("MemberSelect", 13, 30);
    }

    public void testSimpleFallThroughExitPoint() throws Exception {
        performTest("MethodFallThroughExitPoints", 4, 13);
    }

    public void testFallThroughExitPointWithIf() throws Exception {
        performTest("MethodFallThroughExitPoints", 7, 13);
    }

    public void testNotFallThroughIfWithElse() throws Exception {
        performTest("MethodFallThroughExitPoints", 12, 13);
    }

    public void testFallThroughExitPointWithTryCatch() throws Exception {
        performTest("MethodFallThroughExitPoints", 19, 13);
    }

    public void testNotFallThroughTryCatchWithReturns() throws Exception {
        performTest("MethodFallThroughExitPoints", 28, 13);
    }

    public void testNotFallThroughFinallyWithReturn() throws Exception {
        performTest("MethodFallThroughExitPoints", 38, 13);
    }

    public void testNotFallThroughThrow() throws Exception {
        performTest("MethodFallThroughExitPoints", 46, 13);
    }

    public void testMarkCorrespondingMethods1a() throws Exception {
        performTest("MarkCorrespondingMethods1", 7, 55);
        performTest("MarkCorrespondingMethods1", 7, 64);
    }

    public void testMarkCorrespondingMethods1b() throws Exception {
        performTest("MarkCorrespondingMethods1", 7, 83);
    }

    public void testMarkCorrespondingMethods1c() throws Exception {
        performTest("MarkCorrespondingMethods1", 74, 44);
        performTest("MarkCorrespondingMethods1", 74, 49);
        performTest("MarkCorrespondingMethods1", 74, 53);
        performTest("MarkCorrespondingMethods1", 74, 56);
        performTest("MarkCorrespondingMethods1", 74, 32);
    }

    public void testMarkCorrespondingMethods1d() throws Exception {
        performTest("MarkCorrespondingMethods1", 74, 70);
    }

    public void testMarkCorrespondingMethods1e() throws Exception {
        performTest("MarkCorrespondingMethods1", 98, 55);
    }

    public void testMarkCorrespondingMethods1f() throws Exception {
        performTest("MarkCorrespondingMethods1", 108, 40);
    }

    public void testBreakContinue1() throws Exception {
        performTest("BreakOrContinue",  9, 31);
        performTest("BreakOrContinue", 19, 27);
        performTest("BreakOrContinue", 29, 29);
    }

    public void testBreakContinue2() throws Exception {
        performTest("BreakOrContinue", 10, 31);
        performTest("BreakOrContinue", 12, 31);
        performTest("BreakOrContinue", 22, 31);
        performTest("BreakOrContinue", 31, 31);
        performTest("BreakOrContinue", 39, 25);
        performTest("BreakOrContinue", 44, 20);
    }

    public void testBreakContinue3() throws Exception {
        performTest("BreakOrContinue", 11, 31);
        performTest("BreakOrContinue", 16, 24);
        performTest("BreakOrContinue", 26, 24);
    }

    public void testBreakContinue4() throws Exception {
        performTest("BreakOrContinue", 8, 31);
    }

    public void testBreakContinue5() throws Exception {
        performTest("BreakOrContinue", 36, 20);
        performTest("BreakOrContinue", 42, 20);
    }

    public void testBreakContinue6() throws Exception {
        performTest("BreakOrContinue", 52, 32);
        performTest("BreakOrContinue", 56, 32);
    }

    public void testBreakContinue7() throws Exception {
        performTest("BreakOrContinue", 53, 32);
        performTest("BreakOrContinue", 55, 32);
    }

    public void testBreakContinue8() throws Exception {
        performTest("BreakOrContinue", 51, 32);
    }

    public void testBreakContinue9() throws Exception {
        performTest("BreakOrContinue", 54, 32);
    }

    public void testBreakContinue10() throws Exception {
        performTest("BreakOrContinue", 61, 20);
        performTest("BreakOrContinue", 68, 20);
    }

    public void testBreakContinue11() throws Exception {
        performTest("BreakOrContinue", 64, 23);
        performTest("BreakOrContinue", 71, 23);
    }

    public void testBreakContinue12() throws Exception {
        performTest("BreakOrContinue", 78, 20);
        performTest("BreakOrContinue", 81, 25);
    }

    public void testCaretPosition136665() throws Exception {
        performTest("CaretPosition136665", 7, 8);
    }

    public void testMarkConstructorOccurrence() throws Exception {
        performTest("MarkConstructorOccurrence", 8, 25);
        performTest("MarkConstructorOccurrence", 4, 25);
    }

    public void testConstructorIsNotAClass() throws Exception {
        performTest("MarkConstructorOccurrence", 2, 25);
    }

    public void testInsideConstructorInvocation() throws Exception {
        performTest("InsideConstructorInvocation", 9, 18);
    }

    public void testException144264() throws Exception {
        performTest("Exception144264", 7, 19);
    }

    public void testException227248() throws Exception {
         performTest("Exception227248", 7, 25);
    }

    public void testTypeParamMarkOccurrences() throws Exception {
        performTest("TypeParamMarkOccurrences", 3, 14);
        performTest("TypeParamMarkOccurrences", 3, 28);
    }
    
    public void testExitPointsAnnonymous162974a() throws Exception {
        performTest("ExitPoints", 108, 15);
    }

    public void testExitPointsAnnonymous162974b() throws Exception {
        performTest("ExitPoints", 115, 22);
    }

    public void testErroneousMethodNETBEANS_224() throws Exception {
        performTest("ErroneousMethod", 3, 24);
    }

    //Support for exotic identifiers has been removed 6999438
    public void REMOVEDtestExoticIdentifiers1() throws Exception {
        performTest("ExoticIdentifier", 3, 43);
        performTest("ExoticIdentifier", 4, 20);
        performTest("ExoticIdentifier", 5, 20);
    }

    //Support for exotic identifiers has been removed 6999438
    public void REMOVEDtestExoticIdentifiers2() throws Exception {
        performTest("ExoticIdentifier", 3, 27);
        performTest("ExoticIdentifier", 5, 12);
    }

    //Support for exotic identifiers has been removed 6999438
    public void REMOVEDtestExoticIdentifiers3() throws Exception {
        performTest("ExoticIdentifier", 4, 28);
    }

    private void performTest(String name, final int line, final int column) throws Exception {
        performTest(name, line, column, false);
    }
    
    private static final Coloring MARK_OCCURRENCES =
            ColoringAttributes.add(ColoringAttributes.empty(), ColoringAttributes.MARK_OCCURRENCES);

    private void performTest(String name, final int line, final int column, boolean doCompileRecursively) throws Exception {
        performTest(name,new Performer() {
            public void compute(CompilationController info, Document doc, SemanticHighlighterBase.ErrorDescriptionSetter setter) {
                int offset = NbDocument.findLineOffset((StyledDocument) doc, line) + column;
                List<int[]> spans = new MarkOccurrencesHighlighterBase() {
                    @Override
                    protected void process(CompilationInfo info, Document doc, SchedulerEvent event) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                }.processImpl(info, MarkOccurencesSettings.getCurrentNode(), doc, offset);
                
                if (spans != null) {
                    setter.setHighlights(doc, spans.stream()
                                                   .map(span -> Pair.of(span, MARK_OCCURRENCES))
                                                   .collect(Collectors.toList()),
                                         Collections.<int[], String>emptyMap());
                }
            }
        }, doCompileRecursively);
    }
    
}
