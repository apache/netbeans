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
package org.netbeans.modules.html.editor.lib.test;

import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.editor.NbEditorDocument;
import org.openide.filesystems.FileObject;

/**
 * @author Marek Fukala
 */
public class TestBase extends CslTestBase {

    private static final String PROP_MIME_TYPE = "mimeType"; //NOI18N

    public TestBase(String name) {
        super(name);
    }

    protected BaseDocument createDocument() {
        NbEditorDocument doc = new NbEditorDocument("text/html");
        doc.putProperty(PROP_MIME_TYPE, "text/html");
        doc.putProperty(Language.class, HTMLTokenId.language()); //hack for LanguageManager - shoudl be removed

        return doc;
    }

    @Override
    protected BaseDocument getDocument(FileObject fo) {
        return super.getDocument(fo, "text/html", HTMLTokenId.language());
    }

//    @Override
//    protected DefaultLanguageConfig getPreferredLanguage() {
//        return new HtmlLanguage();
//    }

    @Override
    protected String getPreferredMimeType() {
        return "text/html";
    }
}
