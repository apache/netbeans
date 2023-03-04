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
package org.netbeans.modules.spring.beans.utils;

import junit.framework.TestCase;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils;

/**
 *
 * @author Sujit Nair
 */
public class StringUtilsTest extends TestCase {

    public StringUtilsTest(String testName) {
        super(testName);
    }

    public void testLastIndexOfAnyDelimiter() {
        String str = "bean1, bean2;bean3";
        int endPos = str.indexOf("an2");
        assertEquals(6, StringUtils.lastIndexOfAnyDelimiter(str, 0, endPos, SpringXMLConfigEditorUtils.BEAN_NAME_DELIMITERS));
        endPos = str.indexOf("bean2");
        assertEquals(6, StringUtils.lastIndexOfAnyDelimiter(str, 0, endPos, SpringXMLConfigEditorUtils.BEAN_NAME_DELIMITERS));
        assertEquals(-1, StringUtils.lastIndexOfAnyDelimiter(null, 5, -1, ":"));
        assertEquals(-1, StringUtils.lastIndexOfAnyDelimiter(null, 5, 15, ":"));
        assertEquals(-1, StringUtils.lastIndexOfAnyDelimiter(str, Integer.MAX_VALUE, SpringXMLConfigEditorUtils.BEAN_NAME_DELIMITERS));
    }
    
    public void testIndexOfAnyDelimiter() {
        String str = "bean1, bean2;bean3";
        int startPos = str.indexOf("an2");
        int endPos = str.length();
        assertEquals(12, StringUtils.indexOfAnyDelimiter(str, startPos, endPos, SpringXMLConfigEditorUtils.BEAN_NAME_DELIMITERS));
        startPos = str.indexOf("bean2");
        assertEquals(12, StringUtils.indexOfAnyDelimiter(str, startPos, endPos, SpringXMLConfigEditorUtils.BEAN_NAME_DELIMITERS));
        assertEquals(-1, StringUtils.indexOfAnyDelimiter(null, 5, -1, ":"));
        assertEquals(-1, StringUtils.indexOfAnyDelimiter(null, 5, 15, ":"));
        assertEquals(-1, StringUtils.indexOfAnyDelimiter(str, Integer.MAX_VALUE, SpringXMLConfigEditorUtils.BEAN_NAME_DELIMITERS));
    }
    
    
}
