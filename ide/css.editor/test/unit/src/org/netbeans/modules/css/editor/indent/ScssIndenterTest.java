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
package org.netbeans.modules.css.editor.indent;

import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.css.editor.csl.CssLanguage;
import org.netbeans.modules.css.lib.TestUtil;
import org.netbeans.modules.parsing.api.Source;
import org.openide.filesystems.FileObject;


public class ScssIndenterTest extends CslTestBase {

    private static final String PROP_MIME_TYPE = "mimeType"; //NOI18N

    public ScssIndenterTest(String name) {
        super(name);
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new CssLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return "text/scss";
    }

    @Override
    public Formatter getFormatter(IndentPrefs preferences) {
        return null;
    }

    public void testFormattingCase6() throws Exception {
        reformatFileContents("testfiles/case006.scss", new IndentPrefs(4, 4));
    }

}
