/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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