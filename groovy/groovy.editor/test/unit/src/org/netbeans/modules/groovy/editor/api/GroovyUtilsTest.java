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

package org.netbeans.modules.groovy.editor.api;

import org.netbeans.modules.groovy.editor.utils.GroovyUtils;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;

/**
 *
 * @author Matthias Schmidt
 */
public class GroovyUtilsTest extends GroovyTestBase {

    String TEST_LINE = "this is a testline\n";
    int meaningOfLife = 42;

    public GroovyUtilsTest(String testName) {
        super(testName);
    }

    public void testBadLocationExceptionThrown1() {
        try {
            GroovyUtils.getRowLastNonWhite(TEST_LINE, meaningOfLife);
        } catch (BadLocationException ex) {
            verifyException(ex);
            return;
        }
        assertTrue(false);
    }
    public void testBadLocationExceptionThrown2() {
        try {
            GroovyUtils.getRowStart(TEST_LINE, meaningOfLife);
        } catch (BadLocationException ex) {
            verifyException(ex);
            return;
        }
        assertTrue(false);
    }
    public void testBadLocationExceptionThrown3() {
        try {
            GroovyUtils.isRowEmpty(TEST_LINE, meaningOfLife);
        } catch (BadLocationException ex) {
            verifyException(ex);
            return;
        }
        assertTrue(false);
    }
    public void testBadLocationExceptionThrown4() {
        try {
            GroovyUtils.isRowWhite(TEST_LINE, meaningOfLife);
        } catch (BadLocationException ex) {
            verifyException(ex);
            return;
        }
        assertTrue(false);
    }

    void verifyException(BadLocationException ex){
        assertEquals("Unexpected error-message", ex.getMessage(), String.valueOf(meaningOfLife) + " out of " + TEST_LINE.length());
    }
    
}
