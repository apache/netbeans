/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.js;

import junit.framework.Test;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;

public class JsFormatterEmbeddedTest extends PHPTestBase {

    public JsFormatterEmbeddedTest(String name) {
        super(name);
    }

    public static Test suite() {
        return NbModuleSuite.createConfiguration(JsFormatterEmbeddedTest.class).gui(false).suite();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        try {
            TestLanguageProvider.register(HTMLTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
        try {
            TestLanguageProvider.register(PHPTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
        HtmlVersion.DEFAULT_VERSION_UNIT_TESTS_OVERRIDE = HtmlVersion.HTML41_TRANSATIONAL;

        try {
            TestLanguageProvider.register(JsTokenId.javascriptLanguage());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
    }

    public void testEmbeddedSimple1() throws Exception {
        reformatFileContents("testfiles/js/embeddedSimple1.php", new IndentPrefs(4,4));
    }

    public void testEmbeddedSimple2() throws Exception {
        reformatFileContents("testfiles/js/embeddedSimple2.php", new IndentPrefs(4,4));
    }

    public void testEmbeddedSimple3() throws Exception {
        reformatFileContents("testfiles/js/embeddedSimple3.php", new IndentPrefs(4,4));
    }

    public void testEmbeddedSimple4() throws Exception {
        reformatFileContents("testfiles/js/embeddedSimple4.php", new IndentPrefs(4,4));
    }

    public void testEmbeddedSimple5() throws Exception {
        reformatFileContents("testfiles/js/embeddedSimple5.php", new IndentPrefs(4,4));
    }

    public void testEmbeddedMultipleSections1() throws Exception {
        reformatFileContents("testfiles/js/embeddedMultipleSections1.php", new IndentPrefs(4,4));
    }
}
