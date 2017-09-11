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
