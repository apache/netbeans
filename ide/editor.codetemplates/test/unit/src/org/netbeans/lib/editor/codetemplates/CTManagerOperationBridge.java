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

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessor;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessorFactory;

/**
 * Bridge to CodeTemplateManagerOperation to load explicit templates.
 *
 * @author mmetelka
 */
public class CTManagerOperationBridge {
    
    private static final Document staticDoc = new PlainDocument();
    static {
        staticDoc.putProperty("mimeType", "text/fake");
    }
    
    private static final CodeTemplateManager staticManager
            = CodeTemplateManagerOperation.getManager(staticDoc);
    
    private static final JTextComponent staticComponent = new JEditorPane();
    static {
        staticComponent.setDocument(staticDoc);
    }

    public static void test(String parametrizedText, CTProcessor processor) {
// XXX: this is broken and needs to be fixed somehow. Probably by using MockMimeLookup and MockServices
//        CodeTemplateApiPackageAccessor.get().getOperation(staticManager).testInstallProcessorFactory(new CTPFactory(processor));
        CodeTemplate template = staticManager.createTemporary(parametrizedText);
        template.insert(staticComponent);
    }

    private static final class CTPFactory implements CodeTemplateProcessorFactory {
        
        private CTProcessor processor;
        
        CTPFactory(CTProcessor processor) {
            this.processor = processor;
        }

        public CodeTemplateProcessor createProcessor(CodeTemplateInsertRequest request) {
            assert (request != null);
            processor.setRequest(request);
            return processor;
        }
        
    }
}
