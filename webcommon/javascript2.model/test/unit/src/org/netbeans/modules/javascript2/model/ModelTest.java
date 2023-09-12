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
package org.netbeans.modules.javascript2.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Collection;
import org.netbeans.modules.javascript2.model.api.JsElement;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.Model;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class ModelTest extends ModelTestBase {

    public ModelTest(String testName) {
        super(testName);
    }

    public void testObjectNames01() throws Exception {
        checkModel("testfiles/model/objectNames01.js");
    }

    public void testObjectMethods01() throws Exception {
        checkModel("testfiles/model/objectMethods01.js");
    }

    public void testStaticMethods01() throws Exception {
        checkModel("testfiles/model/staticMethods01.js");
    }

    public void testSimpleFunction() throws Exception {
        checkModel("testfiles/model/simpleFunction.js");
    }

    public void testVariables01() throws Exception {
        checkModel("testfiles/model/variables01.js");
    }

    public void testNamespaces01() throws Exception {
        checkModel("testfiles/model/namespaces01.js");
    }

    public void testProperty01() throws Exception {
        checkModel("testfiles/model/property01.js");
    }

    public void testProperty03() throws Exception {
        checkModel("testfiles/model/property03.js");
    }

    public void testKolo() throws Exception {
        checkModel("testfiles/model/kolo.js");
    }

    public void testRecursion() throws Exception {
        checkModel("testfiles/model/recursion.js", true);
    }

    public void testTernary() throws Exception {
        checkModel("testfiles/model/ternary.js");
    }

    public void testjQueryFragment01() throws Exception {
        checkModel("testfiles/model/jQueryFragment01.js");
    }

    public void testjQueryFragment02() throws Exception {
        checkModel("testfiles/model/jQueryFragment02.js");

    }

    public void testClosers01() throws Exception {
        checkModel("testfiles/model/closers01.js");
    }

    public void testSimleObject01() throws Exception {
        checkModel("testfiles/model/simpleObject.js");
    }

    public void testSimpleReturnTypes01() throws Exception {
        checkModel("testfiles/model/returnTypes01.js");
    }

    public void testReturnTypes02() throws Exception {
        checkModel("testfiles/model/returnTypes02.js");

    }

    public void testPropertyWithNew() throws Exception {
        checkModel("testfiles/model/propertyWithNew.js");
    }

    public void testissue230709() throws Exception {
        checkModel("testfiles/structure/issue230709.js");
    }

    public void testIssue241171() throws Exception {
        checkModel("testfiles/markoccurences/issue241171.js");
    }

    public void testIssue242421() throws Exception {
        checkModel("testfiles/markoccurences/issue242421.js");
    }

    public void testIssue248696_01() throws Exception {
        checkModel("testfiles/hints/issue248696_01.js");
    }

    public void testIssue252655() throws Exception {
        checkModel("testfiles/coloring/issue252655.js");
    }

    public void testPropertyWithNewAssignments() throws Exception {
        Model model = getModel("testfiles/model/propertyWithNew.js");
        assertNotNull(model);

        JsObject global = model.getGlobalObject();

        JsObject object = global.getProperty("propertyWithNewAnonym$0");
        assertEquals(JsElement.Kind.ANONYMOUS_OBJECT, object.getJSKind());

        JsObject property = object.getProperty("model");
        Collection<? extends TypeUsage> types = property.getAssignmentForOffset(property.getDeclarationName().getOffsetRange().getEnd());
        assertEquals(1, types.size());
    }

    public void testSimpleCall() throws Exception {
        checkModel("testfiles/model/simpleCall.js");
    }

    public void testObjectAsParameter() throws Exception {
        checkModel("testfiles/model/objectAsParameter.js");
    }

    public void testAnonymousFunction() throws Exception {
        checkModel("testfiles/model/anonymousFunction.js");
    }

    public void testAnonymousFunction2() throws Exception {
        checkModel("testfiles/model/anonymousFunction2.js");
    }

    public void testCzechChars() throws Exception {
        checkModel("testfiles/coloring/czechChars.js");
    }

    public void testTypeInferenceNew() throws Exception {
        checkModel("testfiles/completion/typeInferenceNew.js");
    }

    public void testArguments() throws Exception {
        checkModel("testfiles/completion/arguments/arguments.js");
    }

    public void testPerson() throws Exception {
        checkModel("testfiles/model/person.js");
    }

    public void testSelfPattern() throws Exception {
        checkModel("testfiles/model/issue229717.js");
    }

    public void testSemitypes() throws Exception {
        checkModel("testfiles/structure/semitypes/semiTypes.js");
    }

    public void testissue231782() throws Exception {
        checkModel("testfiles/markoccurences/issue231782.js");
    }

    public void testIssue236141_01() throws Exception {
        checkModel("testfiles/model/issue236141_01.js");
    }

    public void testIssue236141_02() throws Exception {
        checkModel("testfiles/model/issue236141_02.js");
    }

    public void testIssue238693() throws Exception {
        checkModel("testfiles/model/issue238693.js");
    }

    public void testIssue242408() throws Exception {
        checkModel("testfiles/model/issue242408.js");
    }

    public void testIssue242454() throws Exception {
        checkModel("testfiles/model/issue242454.js");
    }

    public void testIssue244861() throws Exception {
        checkModel("testfiles/markoccurences/issue244861.js");
    }

    public void testIssue243140_01() throws Exception {
        checkModel("testfiles/structure/issue243140_01.js");
    }

    public void testIssue247834() throws Exception {
        checkModel("testfiles/model/issue247834.js");
    }

    public void testIssue250392() throws Exception {
        checkModel("testfiles/structure/issue250392.js");
    }

    public void testIssue251911() throws Exception {
        checkModel("testfiles/model/issue251911.js");
    }

    public void testIssueGH5184_02() throws Exception {
        checkModel("testfiles/structure/issueGH5184_02.js");
    }

    public void testIssueGH5184_03() throws Exception {
        checkModel("testfiles/structure/issueGH5184_03.js");
    }

    public void testBogusGlobalThis() throws Exception {
        checkModel("testfiles/structure/bogusGlobalThis_01.js");
        checkModel("testfiles/structure/bogusGlobalThis_02.js");
    }

    public void testPersonRevert() throws Exception {
        FileObject fo = getTestFile("testfiles/model/person.js.model");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fo.getInputStream()))) {
            Collection<JsObject> obj = Model.readModel(reader, null, null, null);
            assertEquals(1, obj.size());

            final StringWriter sw = new StringWriter();
            Model.Printer p = (String str) -> {
                sw.append(str).append("\n");
            };
            Model.writeObject(p, obj.iterator().next(), null);
            assertDescriptionMatches(fo, sw.toString(), false, ".revert", true);
        }

        // bit hacky check that .model and .model.revert are the same
        String text = fo.asText();
        assertDescriptionMatches("testfiles/model/person.js.model", text, false, ".revert", true);
    }

    public void testIssue217679() throws Exception {
        checkModel("testfiles/model/testIssue217679.js");
    }

    public void testIssue238685_01() throws Exception {
        checkModel("testfiles/model/issue238685_01.js");
    }

    public void testIssue252022() throws Exception {
        checkModel("testfiles/hints/issue252022.js");
    }

    public void testIssue252135() throws Exception {
        checkModel("testfiles/markoccurences/issue252135.js");
    }

    public void testIssue231530() throws Exception {
        checkModel("testfiles/model/issue231530.js");
    }

    public void testComplexPrototype() throws Exception {
        // The unittest model contains multiple prototype entries, where only
        // two are defined by plain strings and the others are variables,
        // properties of objects and function calls. If at some later time these
        // cases are better supported, the extracted model needs to be rechecked
        // and regenerated.
        checkModel("testfiles/model/complexPrototype.js");
    }

    public void testObjectNameMatchingNestedFunction() throws Exception {
        checkModel("testfiles/model/objectNameMatchingNestedFunction.js");
    }

    public void testClassConstructor() throws Exception {
        checkModel("testfiles/model/classConstructor.js");
    }

    public void testClassInAnonymousFunction() throws Exception {
        checkModel("testfiles/model/classInAnonymousFunction.js");
    }

    public void testClassInAnonymousFunction2() throws Exception {
        checkModel("testfiles/model/classInAnonymousFunction2.js");
    }
}
