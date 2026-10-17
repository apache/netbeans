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
package org.netbeans.modules.web.jsf.editor.hints;

import java.util.List;

import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.web.jsf.editor.TestBaseForTestProject;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.openide.util.NbBundle;

public class ComponentUsagesCheckerTest extends TestBaseForTestProject {

    public ComponentUsagesCheckerTest(String name) {
        super(name);
    }
    
    private List<Hint> computeHints(String relFilePath) throws Exception {
        ParseResultInfo info = parse(relFilePath);
        assertNotNull(info);
        assertNotNull(info.result);
        ComponentUsagesChecker checker = new ComponentUsagesChecker();
        RuleContext context = new RuleContext();
        context.parserResult = info.result;
        return checker.compute(context);
    }

    public void testMissingRequiredAttribute() throws Exception {
        List<Hint> hints = computeHints("testWebProject/web/hints/missing_attribute.xhtml");
        assertNotNull(hints);
        assertEquals(1, hints.size());
        Hint hint = hints.get(0);
        String expectedMessage = NbBundle.getMessage(HintsProvider.class, "MSG_MISSING_REQUIRED_ATTRIBUTE", "for");
        assertEquals(expectedMessage, hint.getDescription());
        assertEquals(HintSeverity.ERROR, hint.getRule().getDefaultSeverity());
        assertNotNull(hint.getRange());
    }

    public void testRepeatedAttribute() throws Exception {
        List<Hint> hints = computeHints("testWebProject/web/hints/repeated_attribute.xhtml");
        assertNotNull(hints);
        assertEquals(2, hints.size());
        Hint hint1 = hints.get(0); // inputText -> id
        String expectedMessage1 = NbBundle.getMessage(HintsProvider.class, "MSG_DUPLICATE_ATTRIBUTE", "id", "inputText");
        assertEquals(expectedMessage1, hint1.getDescription());
        assertEquals(HintSeverity.ERROR, hint1.getRule().getDefaultSeverity());
        assertNotNull(hint1.getRange());
        Hint hint2 = hints.get(1); // outputText -> pt:attr (prefixed)
        String expectedMessage2 = NbBundle.getMessage(HintsProvider.class, "MSG_DUPLICATE_ATTRIBUTE", "pt:attr", "outputText");
        assertEquals(expectedMessage2, hint2.getDescription());
        assertEquals(HintSeverity.ERROR, hint2.getRule().getDefaultSeverity());
        assertNotNull(hint2.getRange());
    }

    public void testUnknownAttribute() throws Exception {
        List<Hint> hints = computeHints("testWebProject/web/hints/unknown_attribute.xhtml");
        assertNotNull(hints);
        assertEquals(1, hints.size());
        Hint hint = hints.get(0);
        String expectedMessage = NbBundle.getMessage(HintsProvider.class, "MSG_UNKNOWN_ATTRIBUTE", "unknownAttribute", "inputText");
        assertEquals(expectedMessage, hint.getDescription());
        assertEquals(HintSeverity.ERROR, hint.getRule().getDefaultSeverity());
        assertNotNull(hint.getRange());
    }

    public void testUnknownComponent() throws Exception {
        List<Hint> hints = computeHints("testWebProject/web/hints/unknown_component.xhtml");
        assertNotNull(hints);
        assertEquals(3, hints.size());
        Library lib = getJsfSupportImpl().getLibrary("http://java.sun.com/jsf/html");
        assertNotNull(lib);
        String expectedUnknownComp = NbBundle.getMessage(HintsProvider.class, "MSG_UNKNOWN_CC_COMPONENT", lib.getDisplayName(), "unknownComponent");
        assertEquals(expectedUnknownComp, hints.get(0).getDescription()); // h:unknownComponent starting tag
        assertEquals(expectedUnknownComp, hints.get(1).getDescription()); // h:unknownComponent closing tag
        String expectedAnotherUnknownComp = NbBundle.getMessage(HintsProvider.class, "MSG_UNKNOWN_CC_COMPONENT", lib.getDisplayName(), "anotherUnknownComponent");
        assertEquals(expectedAnotherUnknownComp, hints.get(2).getDescription()); // <h:anotherUnknownComponent/>
        hints.forEach(hint -> {
            assertEquals(HintSeverity.ERROR, hint.getRule().getDefaultSeverity());
            assertNotNull(hint.getRange());
        });
    }

}
