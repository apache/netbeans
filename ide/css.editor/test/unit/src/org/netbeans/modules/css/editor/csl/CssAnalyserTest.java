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
package org.netbeans.modules.css.editor.csl;

import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.css.editor.module.main.CssModuleTestBase;
import org.netbeans.modules.css.lib.TestUtil;
import org.netbeans.modules.css.lib.api.*;
import org.netbeans.modules.parsing.spi.ParseException;

public class CssAnalyserTest extends CssModuleTestBase {

    public CssAnalyserTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        CssParserResult.IN_UNIT_TESTS = true;
    }

    public void testNoErrorInFontSize() throws ParseException, BadLocationException {
        String source = ".bigFont { font-size: 19px}";
        CssParserResult result = TestUtil.parse(source);
        CssAnalyser cssAnalyser = new CssAnalyser(); 
        List<? extends FilterableError> errors = cssAnalyser.getExtendedDiagnostics(result);
        assertTrue(errors.isEmpty());
        
        source = ".bigFont { font: 19px}";
        result = TestUtil.parse(source);
        cssAnalyser = new CssAnalyser(); 
        errors = cssAnalyser.getExtendedDiagnostics(result);
        assertFalse(errors.isEmpty());
    }
}