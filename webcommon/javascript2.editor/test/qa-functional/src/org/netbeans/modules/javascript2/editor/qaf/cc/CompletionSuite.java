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
package org.netbeans.modules.javascript2.editor.qaf.cc;

import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;

/**
 *
 * @author vriha
 */
public class CompletionSuite {

    public static Test suite() {
        return JellyTestCase.emptyConfiguration().
                addTest(BrowserObjectsTest.class, BrowserObjectsTest.tests).
                addTest(CssSelectorsTest.class, CssSelectorsTest.tests).
                addTest(DOMObjectsTest.class, DOMObjectsTest.tests).
                addTest(EmbeddedHTMLTest.class, EmbeddedHTMLTest.tests).
                addTest(ExtJSTest.class, ExtJSTest.tests).
                addTest(ExtendsTest.class, ExtendsTest.tests).
                addTest(JQueryPluginTest.class, JQueryPluginTest.tests).
                addTest(JQueryWidgetTest.class, JQueryWidgetTest.tests).
                addTest(JSObjectsTest.class, JSObjectsTest.tests).
                addTest(ParamDefSameTest.class, ParamDefSameTest.tests).
                addTest(ParamSameFileTest.class, ParamSameFileTest.tests).
                addTest(TestCC.class, TestCC.tests).
                addTest(TestCCInsideWith.class, TestCCInsideWith.tests).
                addTest(TestJQuery.class, TestJQuery.tests).
                addTest(TypeDefTest.class, TypeDefTest.tests).
                suite();
    }

}
