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
package org.netbeans.modules.python.editor.codegen;

import java.util.Collections;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.modules.python.source.PythonUtils;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Create a constructor.
 *
 */
public class ConstructorGenerator implements CodeGenerator {
    private JTextComponent target;

    private ConstructorGenerator(Lookup context) { // Good practice is not to save Lookup outside ctor
        target = context.lookup(JTextComponent.class);
    }

    public static class Factory implements CodeGenerator.Factory {
        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            return Collections.singletonList(new ConstructorGenerator(context));
        }
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ConstructorGenerator.class, "Constructor");
    }

    @Override
    public void invoke() {
        final BaseDocument doc = (BaseDocument)target.getDocument();
        final CodeTemplateManager ctm = CodeTemplateManager.get(doc);
        if (ctm != null) {
            String template = CodeGenUtils.getCodeTemplate(ctm, "init", "def __init__", null); // NOI18N
            if (template == null) {
                template = "def __init__(self${1 default=\", parameters\"}):\n        ${cursor})"; // NOI18N
            }
            ctm.createTemporary(template).insert(target);
        }
    }
}
