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
package org.netbeans.modules.html.angular;

//import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.html.lexer.HtmlLexerPlugin;
import org.netbeans.modules.html.angular.model.Directive;

/**
 *
 * @author marekfukala
 */
@MimeRegistration(mimeType = "text/html", service = HtmlLexerPlugin.class)
//@ServiceProvider(service = HtmlLexerPlugin.class)
public class AngularHtmlLexerPlugin extends HtmlLexerPlugin {

    @Override
    public String getOpenDelimiter() {
        return "{{"; //NOI18N
    }

    @Override
    public String getCloseDelimiter() {
        return "}}"; //NOI18N
    }

    @Override
    public String getContentMimeType() {
        return Constants.JAVASCRIPT_MIMETYPE; //NOI18N
    }

    @Override
    public String createAttributeEmbedding(String elementName, String attributeName) {
        //TODO take the element into account!
        Directive directive = Directive.getDirective(attributeName);
        if(directive != null) {
            switch(directive.getType()) {
                case expression:
                case repeatExpression:
                case comprehensionExpression:
                case object:
                    return Constants.JAVASCRIPT_MIMETYPE;
            }
        }
        return null;
    }
    
}
