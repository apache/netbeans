/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
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
        Iterator<? extends TypeUsage> iterator = types.iterator();
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
    
    public void testPersonRevert() throws Exception {
        FileObject fo = getTestFile("testfiles/model/person.js.model");
        BufferedReader reader = new BufferedReader(new InputStreamReader(fo.getInputStream()));
        try {
            Collection<JsObject> obj = Model.readModel(reader, null, null, null);
            assertEquals(1, obj.size());

            final StringWriter sw = new StringWriter();
            Model.Printer p = new Model.Printer() {
                @Override
                public void println(String str) {
                    sw.append(str).append("\n");
                }
            };
            Model.writeObject(p, obj.iterator().next(), null);
            assertDescriptionMatches(fo, sw.toString(), false, ".revert", true);
        } finally {
            reader.close();
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
}
