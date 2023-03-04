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
package org.netbeans.api.html.lexer;

import org.netbeans.api.annotations.common.CheckForNull;

/**
 * <b>NOT FOR PUBLIC USE!!!</b> Prototype - not final version!!! An API review will run, this is a public API.
 * 
 * HtmlLexer extension - allows to inject custom expression languages into html content.
 * 
 * To be registered in mime lookup.
 *
 * @author marekfukala
 */
public abstract class HtmlLexerPlugin {
    
    /**
     * "{{"
     */
    public String getOpenDelimiter() {
        return null;
    }

    /**
     * "}}"
     */
    public String getCloseDelimiter() {
        return null;
    }
    
    /**
     * "text/javascript"
     */
    public String getContentMimeType() {
        return null;
    }
    
    /**
     * Can be used to create a language embedding on an attribute value token. 
     * 
     * Note: When more plugins creates an embedding for the same token then the embedding
     * provided by the first plugin is used.
     * 
     * @param elementName name of the tag enclosing the attribute
     * @param attributeName name of the tag attribute
     * @return mimetype of the lexer language or null if no embedding should be created.
     */
    @CheckForNull
    public String createAttributeEmbedding(String elementName, String attributeName) {
        return null;
    }

}
