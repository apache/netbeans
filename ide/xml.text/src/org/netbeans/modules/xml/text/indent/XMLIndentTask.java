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

package org.netbeans.modules.xml.text.indent;

import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;

/**
 * Implementation of IndentTask for text/html mimetype.
 *
 * @author Marek Fukala
 */
public class XMLIndentTask implements IndentTask {

    private Context context;

    XMLIndentTask(Context context) {
        this.context = context;
    }

    public void reindent() throws BadLocationException {
        if(context == null)
            return;
        MimePath mimePath = MimePath.parse (context.mimePath ());
        if(mimePath == null)
            return;        
        Language language = Language.find (mimePath.getMimeType (0));
        if(language == null)
            return;
        LanguagePath languagePath = LanguagePath.get (language);
        if(languagePath == null)
            return;
        for (int i = 1; i < mimePath.size(); i++) {
            languagePath = languagePath.embedded(Language.find(mimePath.getMimeType(i)));
        }
        XMLLexerFormatter formatter = new XMLLexerFormatter(languagePath);
        formatter.reformat(context);
    }

    public ExtraLock indentLock() {
        return null;
    }
}
