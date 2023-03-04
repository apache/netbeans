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
package org.netbeans.modules.css.prep.editor.model;

import org.netbeans.modules.css.prep.editor.model.CPModel;
import org.netbeans.modules.css.prep.editor.model.CPElementType;
import org.netbeans.modules.css.prep.editor.model.CPElement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.css.lib.CssTestBase;
import org.netbeans.modules.css.lib.TestUtil;
import org.netbeans.modules.css.lib.api.CssParserResult;

/**
 *
 * @author marekfukala
 */
public class CPModelTest extends CssTestBase {

    public CPModelTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setScssSource();
        CPModel.topLevelSnapshotMimetype = "text/sass";
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        CPModel.topLevelSnapshotMimetype = null;
    }
    
    public void testVariables() {
        String source = "#navbar {\n"
                + "  $navbar-width: 800px;\n"
                + "  width: $navbar-width;\n"
                + "  border-bottom: 2px solid $navbar-color;\n"
                + "\n"
                + "  li {\n"
                + "    float: left;\n"
                + "    width: $navbar-width/$items - 10px;\n"
                + "\n"
                + "    background-color:\n"
                + "      lighten($navbar-color, 20%);\n"
                + "    &:hover {\n"
                + "      background-color:\n"
                + "        lighten($navbar-color, 10%);\n"
                + "    }\n"
                + "  }\n"
                + ".class {\n"
                + "  @include mixin($switch, #888);\n"
                + "}"
                + "}";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);

        CPModel model = CPModel.getModel(result);
        assertNotNull(model);

        Collection<String> vars = model.getVarNames();
        assertNotNull(vars);

        String[] expected = new String[]{"$navbar-color", "$items", "$switch", "$navbar-width"};

        Collection<String> expSet = Arrays.asList(expected);
        assertTrue(vars.containsAll(expSet));
        assertFalse(vars.retainAll(expSet));

    }

    public void testVariablesInMixin() {
        String source = "$var: 1;  @mixin my { $foo: $var + 1; }";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);

        CPModel model = CPModel.getModel(result);
        assertNotNull(model);

        Collection<String> vars = model.getVarNames();
        assertNotNull(vars);

        String[] expected = new String[]{"$var", "$foo"};

        Collection<String> expSet = Arrays.asList(expected);
        assertTrue(vars.containsAll(expSet));
        assertFalse(vars.retainAll(expSet));

    }

    public void testVariablesInMixinWithError() {
        String source = "$var: 1;  @mixin my { $foo: $ }";
        CssParserResult result = TestUtil.parse(source);

        CPModel model = CPModel.getModel(result);
        assertNotNull(model);

        Collection<String> vars = model.getVarNames();
        assertNotNull(vars);

        //the $foo var won't get to the model's list as it is not parsed
        //xxx possible solutions: improve err. recovery or make some lexer based heuristic for the error area
//        String[] expected = new String[]{"$var","$foo"};
        String[] expected = new String[]{"$var"};

        Collection<String> expSet = Arrays.asList(expected);
        assertTrue(vars.containsAll(expSet));
        assertFalse(vars.retainAll(expSet));

    }

    public void testMixins() {
        String source = ""
                + "@mixin mymixin() {\n"
                + "    .clz {}\n"
                + "}\n"
                + "@include mymixin;\n";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);

        CPModel model = CPModel.getModel(result);
        assertNotNull(model);

        Collection<CPElement> mixins = model.getMixins();
        assertNotNull(mixins);

        assertEquals(2, mixins.size());
        Iterator<CPElement> iterator = mixins.iterator();

        CPElement m1 = iterator.next();
        assertEquals("mymixin", m1.getName());
        assertEquals(CPElementType.MIXIN_DECLARATION, m1.getType());
        
        CPElement m2 = iterator.next();
        assertEquals("mymixin", m2.getName());
        assertEquals(CPElementType.MIXIN_USAGE, m2.getType());
        

    }
    
    public void testVariablesType() {
        String source =
                "$global: 1;\n"
                + "@mixin my($arg) {\n"
                + "    $local: 2;\n"
                + "    color: $usage;\n"
                + "}\n";

        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);

//        TestUtil.dumpResult(result);
        
        CPModel model = CPModel.getModel(result);
        assertNotNull(model);

        Collection<CPElement> variables = model.getVariables();
        assertNotNull(variables);

        Iterator<CPElement> vars = variables.iterator();

//        for(Element v : variables) {
//            System.out.println(v.getName() + "; " + v.getRange() + "; " + v.getType());
//        }
        
        assertTrue(vars.hasNext());
        CPElement v = vars.next();
        assertNotNull(v);
        assertEquals("$global", v.getName().toString());
        assertEquals(CPElementType.VARIABLE_GLOBAL_DECLARATION, v.getType());

        assertTrue(vars.hasNext());
        v = vars.next();
        assertNotNull(v);
        assertEquals("$arg", v.getName().toString());
        assertEquals(CPElementType.VARIABLE_DECLARATION_IN_BLOCK_CONTROL, v.getType());

        assertTrue(vars.hasNext());
        v = vars.next();
        assertNotNull(v);
        assertEquals("$local", v.getName().toString());
        assertEquals(CPElementType.VARIABLE_LOCAL_DECLARATION, v.getType());

        assertTrue(vars.hasNext());
        v = vars.next();
        assertNotNull(v);
        assertEquals("$usage", v.getName().toString());
        assertEquals(CPElementType.VARIABLE_USAGE, v.getType());

        assertFalse(vars.hasNext());
        
    }
    
     public void testScopeOfVariableDeclaredAsMixinArgument() {
        String source =
                  "@mixin my($arg) {\n"
                // 012345678901234567
                + "    color: $arg;\n"
                + "}\n";

        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);

//        TestUtil.dumpResult(result);
        
        CPModel model = CPModel.getModel(result);
        assertNotNull(model);

        CPElement var = model.getVariableAtOffset(12); 
        assertNotNull(var);
        assertEquals("$arg", var.getName().toString());
        assertEquals(CPElementType.VARIABLE_DECLARATION_IN_BLOCK_CONTROL, var.getType());
        
        OffsetRange range = var.getRange();
        assertEquals(10, range.getStart());
        assertEquals(14, range.getEnd());

        //the scope should be the declarations node scope
        OffsetRange scope = var.getScope();
        assertNotNull(scope);
        assertEquals(22, scope.getStart());
        assertEquals(35, scope.getEnd());

     }
     
      public void testScopeOfVariableDeclaredAsMixinArgumentFollowedByMoreArgs() {
        String source =
                  "@mixin my($arg, $arg2) {\n"
                // 012345678901234567
                + "    color: $arg;\n"
                + "}\n";

        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);

//        TestUtil.dumpResult(result);
        
        CPModel model = CPModel.getModel(result);
        assertNotNull(model);

        CPElement var = model.getVariableAtOffset(12); 
        assertNotNull(var);
        assertEquals("$arg", var.getName().toString());
        assertEquals(CPElementType.VARIABLE_DECLARATION_IN_BLOCK_CONTROL, var.getType());
        
        OffsetRange range = var.getRange();
        assertEquals(10, range.getStart());
        assertEquals(14, range.getEnd());

        //the scope should be the declarations node scope
        OffsetRange scope = var.getScope();
        assertNotNull(scope);
        assertEquals(29, scope.getStart());
        assertEquals(42, scope.getEnd());
        
        //try the second argument
        var = model.getVariableAtOffset(18); 
        assertNotNull(var);
        assertEquals("$arg2", var.getName().toString());
        assertEquals(CPElementType.VARIABLE_DECLARATION_IN_BLOCK_CONTROL, var.getType());
        
        range = var.getRange();
        assertEquals(16, range.getStart());
        assertEquals(21, range.getEnd());

        //the scope should be the declarations node scope
        scope = var.getScope();
        assertNotNull(scope);
        assertEquals(29, scope.getStart());
        assertEquals(42, scope.getEnd());

     }
      
     public void testSassMapVariableDeclaration() {
        String source = 
                "$colors: (\n"
                + "  header: #b06,\n"
                + "  footer: red\n"
                + ")";

        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);

        TestUtil.dumpResult(result);
        
        CPModel model = CPModel.getModel(result);
        assertNotNull(model);

        Collection<CPElement> variables = model.getVariables();
        assertNotNull(variables);
        assertEquals(1, variables.size());
        
        CPElement var = variables.iterator().next();
        
        assertEquals("$colors", var.getName());
        assertEquals(CPElementType.VARIABLE_GLOBAL_DECLARATION, var.getType());
        
        OffsetRange range = var.getRange();
        assertEquals(0, range.getStart());
        assertEquals(7, range.getEnd());

     }
      
     
}