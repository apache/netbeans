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

package org.openide.explorer.view;

import junit.framework.TestCase;

/**
 * TableSheetCell tests.
 *
 * @author Martin Krauskopf
 */
public class TableSheetCellTest extends TestCase {

    public TableSheetCellTest(String testName) {
        super(testName);
    }

    public void testHtmlTooltipCreation() {

        String text = ">\"main\" is not a known variable in current context<"; // NOI18N
        assertEquals("<html>&gt;\"main\" is not a known variable in current context&lt;</html>", // NOI18N
                TableSheetCell.createHtmlTooltip(text, null));
        
        // non-html should be escaped
        String noHtml = "\"<html><b>ahoj</b></html>\""; // NOI18N
        assertEquals("<html>\"&lt;html&gt;&lt;b&gt;ahoj&lt;/b&gt;&lt;/html&gt;\"</html>", // NOI18N
                TableSheetCell.createHtmlTooltip(noHtml, null));
        
        // html should be returned as html
        String html = "<html><b>ahoj</b></html>"; // NOI18N
        assertEquals("<html>&lt;html&gt;&lt;b&gt;ahoj&lt;/b&gt;&lt;/html&gt;</html>", // NOI18N
                TableSheetCell.createHtmlTooltip(html, null));
        
        // should return "null" for null values
        assertEquals("null", TableSheetCell.createHtmlTooltip(null, null)); // NOI18N
    }
}
