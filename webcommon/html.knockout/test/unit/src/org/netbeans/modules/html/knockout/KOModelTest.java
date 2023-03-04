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
package org.netbeans.modules.html.knockout;

import org.netbeans.modules.html.knockout.model.KOModel;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.WebUtils;

/**
 *
 * @author marekfukala
 */
public class KOModelTest extends CslTestBase {
    
    public KOModelTest(String name) {
        super(name);
    }

    public void testBasic() {
        KOModel model = createModel("<div data-bind=\"text: name\"></div>");
        Collection<Attribute> bindings = model.getBindings();
        assertNotNull(bindings);
        assertEquals(1, bindings.size());
        Attribute a = bindings.iterator().next();
        assertEquals("text: name", a.unquotedValue().toString());
        assertTrue(model.containsKnockout());
    }
    
    private KOModel createModel(String code) {
        try {        
            BaseDocument document = getDocument(code, "text/html");
            Source source = Source.create(document);
            final AtomicReference<KOModel> modelRef = new AtomicReference<>();
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ResultIterator htmlResult = WebUtils.getResultIterator(resultIterator, "text/html");
                    assertNotNull(htmlResult);
                    modelRef.set(KOModel.getModel((HtmlParserResult)htmlResult.getParserResult()));
                }
            });
            assertNotNull(modelRef.get());
            return modelRef.get();
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
        
    }
    
}