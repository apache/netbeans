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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter;
import org.netbeans.lib.editor.codetemplates.storage.CodeTemplateSettingsImpl;
import org.netbeans.modules.editor.NbEditorKit;

/**
 *
 * @author markiewb
 */
public class CodeTemplateInsertHandlerTest  extends NbTestCase {
    
    private CodeTemplateInsertHandler handler;
    private CodeTemplateManager manager;
    private JEditorPane editor;

    public CodeTemplateInsertHandlerTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        editor = new JEditorPane();
        SwingUtilities.invokeAndWait(() -> editor.setEditorKit(new NbEditorKit()));
        Document document = editor.getDocument();
        manager = CodeTemplateManager.get(document);
    }
    
    @Test
    public void testPrioritizeParameters() {
        CodeTemplateSpiPackageAccessor get = CodeTemplateSpiPackageAccessor.get();
        CodeTemplateParameter paramA = get.createParameter(new CodeTemplateParameterImpl(null, "${paramA}", 0));
        CodeTemplateParameter paramB = get.createParameter(new CodeTemplateParameterImpl(null, "${paramB ordering=\"2\"}", 0));
        CodeTemplateParameter paramC = get.createParameter(new CodeTemplateParameterImpl(null, "${paramC ordering=\"1\"}", 0));

        List<CodeTemplateParameter> prioritizeParameters = CodeTemplateInsertHandler.prioritizeParameters(Arrays.asList(paramA, paramB, paramC));
        assertEquals(Arrays.asList(paramC, paramB, paramA), prioritizeParameters);
    }

    @Test
    public void test_WhenActiveTextRegionHasCompletionInvokeHint_ThenInvokeCodeCompletion() {
        CodeTemplate codeTemplate = manager.createTemporary("${param completionInvoke}");
        handler = new CodeTemplateInsertHandler(
                codeTemplate, editor, Collections.emptyList(), CodeTemplateSettingsImpl.OnExpandAction.FORMAT);
        assertFalse(handler.isCompletionInvoked());
        handler.insertTemplate();
        assertTrue(handler.isCompletionInvoked());
    }
    
    @Test
    public void test_WhenActiveTextRegionHasNoCompletionInvokeHint_ThenDoNotInvokeCodeCompletion() {
        CodeTemplate codeTemplate = manager.createTemporary("${param default=\"\"}");
        handler = new CodeTemplateInsertHandler(
                codeTemplate, editor, Collections.emptyList(), CodeTemplateSettingsImpl.OnExpandAction.FORMAT);
        assertFalse(handler.isCompletionInvoked());
        handler.insertTemplate();
        assertFalse(handler.isCompletionInvoked());
    }
    
}
