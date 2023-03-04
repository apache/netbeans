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
package org.netbeans.modules.web.jsf.editor.el;

import java.util.Iterator;
import java.util.SortedSet;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.jsf.editor.TestBaseForTestProject;

/**
 *
 * @author marekfukala
 */
public class JsfVariablesModelTest extends TestBaseForTestProject {

    public JsfVariablesModelTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
//        JsfVariablesModel.inTest = true; //do not require JsfSupport since we have no project
    }

    private String getTestFilePath() {
        return "testWebProject/web/test.xhtml";
    }

    private JsfVariablesModel getModel(final ParseResultInfo result) throws ParseException {
        final JsfVariablesModel[] model = new JsfVariablesModel[1];
        ParserManager.parse("text/xhtml", new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                model[0] = JsfVariablesModel.getModel(result.result, result.topLevelSnapshot);
            }
        });
        return model[0];
    }

    public void testCreateModel() throws Exception {
        ParseResultInfo result = parse(getTestFilePath());
        JsfVariablesModel model = getModel(result);
        assertNotNull(model);
    }

    public void testModel() throws Exception {
        ParseResultInfo result = parse(getTestFilePath());
        JsfVariablesModel model = getModel(result);
        assertNotNull(model);

        SortedSet<JsfVariableContext> contextsList = model.getContexts();
        assertNotNull(contextsList);

//        for(JsfVariableContext context : contextsList) {
//            System.out.println(context);
//        }

//     Original code: look like source file for this test is not valid.
//      <h:form> element does not contain "var" or "value" attributes.
//        assertEquals(3, contextsList.size());
        assertEquals(1, contextsList.size());

        Iterator<JsfVariableContext> contexts = contextsList.iterator();
        JsfVariableContext first = contexts.next();
//        JsfVariableContext second = contexts.next();
//        JsfVariableContext third = contexts.next();

        assertEquals(385, first.getFrom());
        assertEquals(1073, first.getTo());

//        assertEquals(645, second.getFrom());
//        assertEquals(1026, second.getTo());

//        assertEquals(1082, third.getFrom());
//        assertEquals(1384, third.getTo());

        //test get element by offset

        //out of the contexts regions
        assertNull(model.getContainingContext(0));
        assertNull(model.getContainingContext(1600));
        assertNull(model.getContainingContext(300));

        //inside
        assertEquals(first, model.getContainingContext(390));
        assertEquals(first, model.getContainingContext(640));
        assertEquals(first, model.getContainingContext(1050));

//        assertEquals(second, model.getContainingContext(700));
//        assertEquals(second, model.getContainingContext(1000));

//        assertEquals(third, model.getContainingContext(1200));

        //boundaries - start is inclusive, end exclusive
        assertEquals(first, model.getContainingContext(385));
        assertEquals(first, model.getContainingContext(1072));
        assertNull(model.getContainingContext(1384));
    }

    public void testGetAncestors() throws Exception {
        ParseResultInfo result = parse(getTestFilePath());
        JsfVariablesModel model = getModel(result);
        assertNotNull(model);

        SortedSet<JsfVariableContext> contextsList = model.getContexts();
        assertNotNull(contextsList);

//      Original code: look like source file for this test is not valid.
//      <h:form> element does not contain "var" or "value" attributes.
//        assertEquals(3, contextsList.size());
        assertEquals(1, contextsList.size());

        Iterator<JsfVariableContext> contexts = contextsList.iterator();
        JsfVariableContext first = contexts.next();
//        JsfVariableContext second = contexts.next();
//        JsfVariableContext third = contexts.next();

        //test ancestors
        //second is embedded in first
//        List<JsfVariableContext> ancestors = model.getAncestors(second, false);
//        assertNotNull(ancestors);
//        assertEquals(1, ancestors.size());
//        JsfVariableContext parent = ancestors.get(0);
//        assertSame(first, parent);

        //third is standalone
//        ancestors = model.getAncestors(third, false);
//        assertNotNull(ancestors);
//        assertEquals(0, ancestors.size());
    }

    public void testResolveProperties() throws Exception {
        ParseResultInfo result = parse(getTestFilePath());
        JsfVariablesModel model = getModel(result);
        assertNotNull(model);

        SortedSet<JsfVariableContext> contextsList = model.getContexts();
        assertNotNull(contextsList);
        
//      Original code: look like source file for this test is not valid.
//      <h:form> element does not contain "var" or "value" attributes.
//        assertEquals(3, contextsList.size());
        assertEquals(1, contextsList.size());

        Iterator<JsfVariableContext> contexts = contextsList.iterator();
        JsfVariableContext first = contexts.next();
//        JsfVariableContext second = contexts.next();
//        JsfVariableContext third = contexts.next();

        //test resolving of expressions
//        assertEquals("ProductMB.all.name", model.resolveVariable(second, false));

    }

    public void testExpression_parse() {
        JsfVariablesModel.Expression.parse("");
        JsfVariablesModel.Expression.parse("#");
        JsfVariablesModel.Expression.parse("#{");
        JsfVariablesModel.Expression.parse("#{ddd");
        JsfVariablesModel.Expression.parse("#{ddd}");
    }
   
}
