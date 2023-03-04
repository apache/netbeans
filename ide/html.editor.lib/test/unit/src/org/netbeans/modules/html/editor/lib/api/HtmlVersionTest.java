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

package org.netbeans.modules.html.editor.lib.api;

import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author marekfukala
 */
public class HtmlVersionTest extends NbTestCase {

    public HtmlVersionTest(String name) {
        super(name);
    }

    public static void setDefaultHtmlVersion(HtmlVersion version) {
        HtmlVersion.DEFAULT_VERSION_UNIT_TESTS_OVERRIDE = version;
    }

    public void testDisplayName() {
        HtmlVersion v = HtmlVersion.HTML41_TRANSATIONAL;

        assertEquals("HTML 4.01 Transitional", v.getDisplayName());
        assertEquals("-//W3C//DTD HTML 4.01 Transitional//EN", v.getPublicID());
        assertNull(v.getDefaultNamespace());
        assertFalse(v.isXhtml());
        assertEquals("http://www.w3.org/TR/html4/loose.dtd", v.getSystemId());
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">", v.getDoctypeDeclaration());
    }

    public void testFinds() {
        assertSame(HtmlVersion.HTML41_STRICT, HtmlVersion.find("-//W3C//DTD HTML 4.01//EN", null));
    }
}