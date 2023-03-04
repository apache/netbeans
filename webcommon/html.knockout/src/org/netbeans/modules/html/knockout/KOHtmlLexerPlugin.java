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
package org.netbeans.modules.html.knockout;

import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.html.lexer.HtmlLexerPlugin;

/**
 *
 * @author marekfukala
 */
@MimeRegistration(mimeType = "text/html", service = HtmlLexerPlugin.class)
public class KOHtmlLexerPlugin extends HtmlLexerPlugin {

    @Override
    public String createAttributeEmbedding(String elementName, String attributeName) {
        return KOUtils.KO_DATA_BIND_ATTR_NAME.equalsIgnoreCase(attributeName)
                || KOUtils.KO_PARAMS_ATTR_NAME.equalsIgnoreCase(attributeName)
                        ? KOUtils.KO_DATA_BIND_MIMETYPE
                        : null;
    }
    
}
