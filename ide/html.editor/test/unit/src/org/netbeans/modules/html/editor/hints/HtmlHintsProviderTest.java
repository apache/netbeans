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