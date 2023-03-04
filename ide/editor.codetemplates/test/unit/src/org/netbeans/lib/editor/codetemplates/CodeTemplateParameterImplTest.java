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
package org.netbeans.lib.editor.codetemplates;

import java.util.Collections;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.lib.editor.codetemplates.storage.CodeTemplateSettingsImpl;
import org.netbeans.modules.editor.NbEditorKit;

/**
 *
 * @author: Arthur Sadykov
 */
public class CodeTemplateParameterImplTest extends NbTestCase {

    private CodeTemplateInsertHandler handler;

    public CodeTemplateParameterImplTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception  {
        JEditorPane editor = new JEditorPane();
        SwingUtilities.invokeAndWait(() -> editor.setEditorKit(new NbEditorKit()));
        Document document = editor.getDocument();
        CodeTemplateManager manager = CodeTemplateManager.get(document);
        CodeTemplate codeTemplate = manager.createTemporary("${param}");
        handler = new CodeTemplateInsertHandler(
                codeTemplate, editor, Collections.emptyList(), CodeTemplateSettingsImpl.OnExpandAction.FORMAT);
    }

    public void test_WhenParameterNotContainsCompletionInvokeHint_ThenCompletionInvokeFieldMustBeSetToFalse() {
        assertCompletionInvokeFieldIsSetToFalse("${param editable=true default=\"name\"}");
    }

    public void test_WhenParameterContainsCompletionInvokeHintSetToTrue_ThenCompletionInvokeFieldMustBeSetToTrue() {
        assertCompletionInvokeFieldIsSetToTrue("${param editable=true completionInvoke}");
        assertCompletionInvokeFieldIsSetToTrue("${param editable=true completionInvoke=true default=\"name\"}");
    }

    public void test_WhenParameterContainsCompletionInvokeHintSetToFalse_ThenCompletionInvokeFieldMustBeSetToFalse() {
        assertCompletionInvokeFieldIsSetToFalse("${param editable=false completionInvoke=false}");
        assertCompletionInvokeFieldIsSetToFalse("${param editable=true completionInvoke=\"maybe\" default=\"name\"}");
    }

    private void assertCompletionInvokeFieldIsSetToTrue(String parametrizedText) {
        CodeTemplateParameterImpl parameterImpl = new CodeTemplateParameterImpl(handler, parametrizedText, 0);
        assertTrue("Parameter must have completionInvoke hint set to true", parameterImpl.isCompletionInvoke());
    }

    private void assertCompletionInvokeFieldIsSetToFalse(String parametrizedText) {
        CodeTemplateParameterImpl parameterImpl = new CodeTemplateParameterImpl(handler, parametrizedText, 0);
        assertFalse("Parameter must have completionInvoke hint set to false", parameterImpl.isCompletionInvoke());
    }
}
