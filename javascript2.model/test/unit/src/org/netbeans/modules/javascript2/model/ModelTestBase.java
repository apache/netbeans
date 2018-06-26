/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.model;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.lexer.Language;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.model.api.Model;
import org.netbeans.modules.javascript2.types.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class ModelTestBase extends CslTestBase {
    
    public ModelTestBase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {        
        TestLanguageProvider.register(getPreferredLanguage().getLexerLanguage());
        super.setUp();
    }

    public void checkModel(String file) throws Exception {
        checkModel(file, false);
    }

    public void checkModel(String file, boolean resolve) throws Exception {
        FileObject fo = getTestFile(file);
        Model model = getModel(file);

        final StringWriter sw = new StringWriter();
        Model.Printer p = new Model.Printer() {

            @Override
            public void println(String str) {
                sw.append(str).append("\n");
            }
        };
        model.writeModel(p, resolve);
        assertDescriptionMatches(fo, sw.toString(), false, ".model", true);
    }

    public Model getModel(String file) throws Exception {
        final Model[] globals = new Model[1];
        Source source = getTestSource(getTestFile(file));
        
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                ParserResult parameter = (ParserResult) resultIterator.getParserResult();
                Model model = Model.getModel(parameter, false);
                globals[0] = model;
            }
        });        
        return globals[0];
    }
    
    @Override
    protected boolean runInEQ() {
        // Must run in AWT thread (BaseKit.install() checks for that)
        return true;
    }
    
    @Override
    protected String getPreferredMimeType() {
        return JsTokenId.JAVASCRIPT_MIME_TYPE;
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new DefaultLanguageConfig() {
            @Override
            public Language getLexerLanguage() {
                return JsTokenId.javascriptLanguage();
            }

            @Override
            public String getDisplayName() {
                return "Model";
            }

            @Override
            public Set<String> getSourcePathIds() {
                return Collections.emptySet();
            }       
        };
    }
}
