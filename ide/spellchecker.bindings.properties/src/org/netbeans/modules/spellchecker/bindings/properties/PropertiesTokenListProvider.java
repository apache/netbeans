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
package org.netbeans.modules.spellchecker.bindings.properties;

import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.spellchecker.spi.language.TokenList;
import org.netbeans.modules.spellchecker.spi.language.TokenListProvider;

/**
 *
 * @author Jan Jancura
 */
public class PropertiesTokenListProvider implements TokenListProvider {

    /** Creates a new instance of RubyTokenListProvider */
    public PropertiesTokenListProvider() {
    }

    public TokenList findTokenList(Document doc) {
        if (!(doc instanceof BaseDocument)) {
            return null;
        }
        BaseDocument bdoc = (BaseDocument) doc;
        if ("text/x-properties".equals (bdoc.getProperty ("mimeType")))
            return new PropertiesTokenList (bdoc);
        return null;
    }
}
