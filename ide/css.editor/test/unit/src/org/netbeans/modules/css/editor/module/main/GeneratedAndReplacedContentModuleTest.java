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
package org.netbeans.modules.css.editor.module.main;

import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;

/**
 *
 * @author mfukala@netbeans.org
 */
public class GeneratedAndReplacedContentModuleTest extends CssModuleTestBase {

    public GeneratedAndReplacedContentModuleTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
//        GrammarResolver.setLogging(GrammarResolver.Log.DEFAULT, true);
//        PRINT_INFO_IN_ASSERT_RESOLVE = true;
    }

    public void testContent() {
        assertPropertyDeclaration("content: string(title)");
        assertPropertyDeclaration("content: \"Note: \" ");
        assertPropertyDeclaration("content: \"after1\" string(example1);");
        assertPropertyDeclaration("content: \"Chapter \" counter(chapter) \"\\A\"; ");
        assertPropertyDeclaration("content: counter(item, decimal) '.';");

        assertPropertyDeclaration("content: url(\"link\")");
        assertPropertyDeclaration("content: \" (\" attr(href) \")\"");
    }

    public void testContent2() {
        PropertyDefinition model = Properties.getPropertyDefinition( "content");

        assertResolve(model.getGrammarElement(null), "url(\"link\") normal");
        assertResolve(model.getGrammarElement(null), "url(\"link\") counter(anid, anotherid)");
    }

    public void testCounter() {
        assertPropertyDeclaration("counter-increment: chapter;");
        assertPropertyDeclaration("counter-increment: chapter 10;");
        assertPropertyDeclaration("counter-reset: chapter;");
        assertPropertyDeclaration("counter-reset: chapter 2;");
    }

    public void testQuotes() {
        assertPropertyDeclaration("quotes: 'arg1' 'arg2'");
        assertPropertyDeclaration("quotes: \"arg1\" 'arg2' 'arg3' 'arg4'");
    }
}
