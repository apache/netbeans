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
package org.netbeans.modules.html.editor.hints;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.Rule.AstRule;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.hints.infrastructure.GsfHintsManager;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.test.TestBase;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author marekfukala
 */
public class HtmlHintsProviderTest extends TestBase {
    
    public HtmlHintsProviderTest(String name) {
        super(name);
    }
    
    public void testOrderOfRegisteredRules() throws ParseException {
        Language htmlGsfLanguage = LanguageRegistry.getInstance().getLanguageByMimeType("text/html");
        GsfHintsManager manager = new GsfHintsManager("text/html", new HtmlHintsProvider(), htmlGsfLanguage);
        
        final RuleContext rc = new RuleContext();
        Document doc = getDocument("fake");
        Source source = Source.create(doc);
        ParserManager.parse(Collections.singleton(source), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                HtmlParserResult parserResult = (HtmlParserResult)resultIterator.getParserResult();
                assertNotNull(parserResult);
                rc.parserResult = parserResult;
                
            }
        });
        List<? extends HtmlRule> rules = HtmlHintsProvider.getSortedRules(manager, rc, false);
        //check if the last one is the "All Other" rule
        int rulesNum = 22;
        
        assertEquals(rulesNum, rules.size());
        
//        for(HtmlRule rule : rules) {
//            System.out.println(rule.getDisplayName());
//        }
        
        //uncomment once Bug 223793 - Order of hints for CSL languages gets fixed
        assertEquals("All Other", rules.get(rulesNum - 1).getDisplayName());
        assertEquals("Common But Not Valid", rules.get(0).getDisplayName());
        
        
    }
    
}