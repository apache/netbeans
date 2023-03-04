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
package org.netbeans.modules.html.editor;

import java.util.Collections;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter;

public class HtmlCodeTemplateFilter implements CodeTemplateFilter {

    private static final String HTML_CODE = "html-code"; // NOI18N

    @Override
    public boolean accept(CodeTemplate template) {
        return true;
    }

    @MimeRegistrations({
        @MimeRegistration(mimeType = "text/html", service = CodeTemplateFilter.ContextBasedFactory.class), // NOI18N
        @MimeRegistration(mimeType = "text/xhtml", service = CodeTemplateFilter.ContextBasedFactory.class) // NOI18N
    })
    public static final class Factory implements CodeTemplateFilter.ContextBasedFactory {

        @Override
        public CodeTemplateFilter createFilter(JTextComponent component, int offset) {
            return new HtmlCodeTemplateFilter();
        }

        @Override
        public List<String> getSupportedContexts() {
            return Collections.singletonList(HTML_CODE);
        }
    }
}
