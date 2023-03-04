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
package org.netbeans.modules.php.smarty.editor;

import org.netbeans.modules.php.smarty.TplTestBase;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class TplDataObjectTest extends TplTestBase {

    public TplDataObjectTest(String testName) {
        super(testName);
    }

    public void testFindEncoding() {
        assertEquals("UTF-8",
                TplDataObject.findEncoding(
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>"));

        assertEquals("UTF-8",
                TplDataObject.findEncoding(
                "<meta http-equiv=\"Content-Type\" content='text/html; charset=UTF-8'/>"));

        assertEquals("UTF-8",
                TplDataObject.findEncoding(
                "<meta http-equiv=\"Content-Type\" content='charset=UTF-8; text/html'/>"));

        assertEquals("UTF-8",
                TplDataObject.findEncoding(
                "<meta http-equiv=\"Content-Type\" content='charset=UTF-8'/>"));

        assertEquals("UTF-8",
                TplDataObject.findEncoding(
                "<meta charset=\"UTF-8\"/>"));

        assertEquals("UTF-8",
                TplDataObject.findEncoding(
                "<meta CHARSET=\"UTF-8\"/>"));

        assertEquals(null,
                TplDataObject.findEncoding(
                "<meta blabla"));

        assertEquals(null,
                TplDataObject.findEncoding(
                "<meta http-equiv=\"Content-Type\" content=\"text/html\"/>"));

        assertEquals(null,
                TplDataObject.findEncoding(
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=\"/>"));
    }

    public void testIssue234945() {
        assertEquals(null,
                TplDataObject.findEncoding(
                "<script type=\"text/javascript\" language=\"javascript1.2\" charset=\"ISO-8859-2\"></script>"));
    }
}
