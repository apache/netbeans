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

package org.netbeans.modules.ant.grammar;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.w3c.dom.Element;

// XXX testSpecials (what does this mean actually?)
// XXX testAddTarget
// XXX testDescriptionCanBeAddedOnlyOnce

/**
 * Test functionality of AntGrammar.
 * @author Jesse Glick
 */
public class AntGrammarTest extends NbTestCase {

    public AntGrammarTest(String name) {
        super(name);
    }

    private AntGrammar g;

    protected @Override void setUp() throws Exception {
        super.setUp();
        g = new AntGrammar();
    }
    
    public void testTypeOf() throws Exception {
        String simpleProject = "<project default='all'><target name='all'/></project>";
        Element e = TestUtil.createElementInDocument(simpleProject, "project", null);
        AntGrammar.ElementType type = AntGrammar.typeOf(e);
        assertEquals(AntGrammar.Kind.PROJECT, type.kind);
        // XXX other specials...
        String projectWithTasks = "<project default='all'><target name='all'><echo>hello</echo></target></project>";
        e = TestUtil.createElementInDocument(projectWithTasks, "echo", null);
        type = AntGrammar.typeOf(e);
        assertEquals(AntGrammar.Kind.TASK, type.kind);
        assertEquals("org.apache.tools.ant.taskdefs.Echo", type.name);
        String projectWithTypes = "<project default='all'><path id='foo'/><target name='all'/></project>";
        e = TestUtil.createElementInDocument(projectWithTypes, "path", null);
        type = AntGrammar.typeOf(e);
        assertEquals(AntGrammar.Kind.TYPE, type.kind);
        assertEquals("org.apache.tools.ant.types.Path", type.name);
        // XXX more...
    }
    
    public void testTaskCompletion() throws Exception {
        String p = "<project default='x'><target name='x'><ecHERE/></target></project>";
        List<String> l = TestUtil.grammarResultValues(g.queryElements(TestUtil.createCompletion(p)));
        assertTrue("matched <echo>", l.contains("echo"));
        // XXX more...
    }
    
    public void testTypeCompletion() throws Exception {
        String p = "<project default='x'><target name='x'><paHERE/></target></project>";
        List<String> l = TestUtil.grammarResultValues(g.queryElements(TestUtil.createCompletion(p)));
        assertTrue("matched <path>", l.contains("path"));
        p = "<project default='x'><filHERE/><target name='x'/></project>";
        l = TestUtil.grammarResultValues(g.queryElements(TestUtil.createCompletion(p)));
        assertTrue("matched <fileset>", l.contains("fileset"));
        // XXX more...
    }
    
    public void testRegularAttrCompletion() throws Exception {
        String p = "<project default='x'><target name='x'><javac srcdHERE=''/></target></project>";
        List<String> l = TestUtil.grammarResultValues(g.queryAttributes(TestUtil.createCompletion(p)));
        assertTrue("matched srcdir on <javac>: " + l, l.contains("srcdir"));
        // XXX more...
    }
    
    public void testSpecialAttrCompletion() throws Exception {
        String p = "<project default='x'><target nHERE=''/></project>";
        List<String> l = TestUtil.grammarResultValues(g.queryAttributes(TestUtil.createCompletion(p)));
        assertEquals("matched name on <target>", Collections.singletonList("name"), l);
        p = "<project default='x'><target dHERE=''/></project>";
        l = TestUtil.grammarResultValues(g.queryAttributes(TestUtil.createCompletion(p)));
        Collections.sort(l);
        assertEquals("matched depends and description on <target>", Arrays.asList("depends", "description"), l);
        // XXX more...
    }
    
    public void testEnumeratedValueCompletion() throws Exception {
        String p = "<project default='x'><target><echo level='vHERE'/></target></project>";
        List<String> l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        assertEquals("matched level='verbose' on <echo>", Collections.singletonList("verbose"), l);
    }
    
    public void testBooleanValueCompletion() throws Exception {
        String p = "<project default='x'><target><echo append='HERE'/></target></project>";
        List<String> l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        Collections.sort(l);
        assertEquals("true or false for append on <echo>", Arrays.asList("false", "true"), l);
    }
    
    public void testStockProperties() throws Exception {
        String p = "<project default='x'><target><echo message='${HERE'/></target></project>";
        List<String> l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        assertTrue("matched ${ant.home}: " + l, l.contains("${ant.home}"));
        assertTrue("matched ${basedir}: " + l, l.contains("${basedir}"));
        assertTrue("matched ${java.home}: " + l, l.contains("${java.home}"));
    }
    
    public void testPropertiesWithoutBrace() throws Exception {
        String p = "<project default='x'><target><echo message='$HERE'/></target></project>";
        List<String> l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        assertTrue("matched ${basedir}: " + l, l.contains("${basedir}"));
    }
    
    public void testPropertiesInText() throws Exception {
        String p = "<project default='x'><target><echo>basedir=${baseHERE</echo></target></project>";
        List<String> l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        assertTrue("matched ${basedir}: " + l, l.contains("dir}"));
    }
    
    public void testPropertiesInInterior() throws Exception {
        String p = "<project default='x'><target><echo message='basedir=${baseHERE'/></target></project>";
        List<String> l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        assertTrue("matched ${basedir} after prefix: " + l, l.contains("basedir=${basedir}"));
        p = "<project default='x'><target><echo message='foo=${foo} basedir=${baseHERE'/></target></project>";
        l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        assertTrue("matched ${basedir} after other props: " + l, l.contains("foo=${foo} basedir=${basedir}"));
        p = "<project default='x'><target><echo>foo=${foo} basedir=${baseHERE</echo></target></project>";
        l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        assertTrue("matched ${basedir} after other props in text: " + l, l.contains("dir}"));
    }
    
    public void testAlreadyUsedProperties() throws Exception {
        String p = "<project default='x'><target><echo message='${foo}'/><echo message='${HERE'/></target></project>";
        List<String> l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        assertTrue("matched already used property ${foo}: " + l, l.contains("${foo}"));
        p = "<project default='x'><target><echo message='${HERE'/></target><target><echo message='${foo}'/></target></project>";
        l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        assertTrue("matched property ${foo} used later: " + l, l.contains("${foo}"));
        p = "<project default='x'><target><echo message='${HERE'/></target><target><echo>${foo}</echo></target></project>";
        l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        assertTrue("matched property ${foo} used in a text node: " + l, l.contains("${foo}"));
        p = "<project default='x'><target><echo message='prefix${foo}suffix'/><echo message='${HERE'/></target></project>";
        l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        assertTrue("matched property ${foo} used inside a value: " + l, l.contains("${foo}"));
        p = "<project default='x'><target><echo message='${foo}:${bar}'/><echo message='${HERE'/></target></project>";
        l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        assertTrue("matched property ${foo} used before another prop: " + l, l.contains("${foo}"));
        assertTrue("matched property ${bar} used after another prop: " + l, l.contains("${bar}"));
    }
    
    public void testAddedProperties() throws Exception {
        String p = "<project default='x'><property name='foo' value='whatever'/><target><echo message='${HERE'/></target></project>";
        List<String> l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        assertTrue("matched defined property ${foo}: " + l, l.contains("${foo}"));
    }
    
    public void testImpliedProperties() throws Exception {
        String p = "<project default='x'><target if='someprop'><echo message='${HERE'/></target></project>";
        List<String> l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        assertTrue("matched property ${someprop} from <target if>: " + l, l.contains("${someprop}"));
        p = "<project default='x'><target><junit errorproperty='failed'/><echo message='${HERE'/></target></project>";
        l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        assertTrue("matched property ${failed} from <junit errorproperty>: " + l, l.contains("${failed}"));
        // XXX could also test other standard names
    }
    
    public void testImplicitProperties() throws Exception {
        String p = "<project default='x'><target><buildnumber/><echo message='${HERE'/></target></project>";
        List<String> l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        assertTrue("matched property ${build.number} from <buildnumber>: " + l, l.contains("${build.number}"));
        // XXX could also test other standard names
    }
    
    public void testIndirectProperties() throws Exception {
        String p = "<project default='x'><target><property name='${foo}' value='bar'/><echo message='${HERE'/></target></project>";
        List<String> l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        assertFalse("did not match non-property ${${foo}}: " + l, l.contains("${${foo}}"));
    }
    
    public void testNonProperties() throws Exception {
        String p = "<project default='x'><target><echo>${foo</echo><echo message='${HERE'/></target></project>";
        List<String> l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        assertFalse("did not match broken property ref '${foo': " + l, l.contains("${foo}"));
        p = "<project default='x'><target><echo>$${foo}</echo><echo message='${HERE'/></target></project>";
        l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        assertFalse("did not match escaped property nonref '$${foo}': " + l, l.contains("${foo}"));
        p = "<project default='x'><target><echo>${}</echo><echo message='${HERE'/></target></project>";
        l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        assertFalse("did not match empty property name: " + l, l.contains("${}"));
        p = "<project default='x'><target><echo>$$${foo}</echo><echo message='${HERE'/></target></project>";
        l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        assertTrue("but '$$${foo}' is a property ref after an escaped shell: " + l, l.contains("${foo}"));
    }
    
    public void testNonCompletingProperties() throws Exception {
        String p = "<project default='x'><target><echo message='$${baseHERE'/></target></project>";
        List<String> l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        assertFalse("did not match property non-ref $${basedir}: " + l, l.contains("$${basedir}"));
        assertEquals("in fact there are no completions here", Collections.EMPTY_LIST, l);
        p = "<project default='x'><target><echo message='$$${baseHERE'/></target></project>";
        l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        assertTrue("but did match property ref $$${basedir}: " + l, l.contains("$$${basedir}"));
        p = "<project default='x'><target><echo message='${basedir}HERE'/></target></project>";
        l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        assertFalse("${basedir} is already complete: " + l, l.contains("${basedir}"));
        assertEquals("in fact there are no completions here", Collections.emptyList(), l);
    }
    
    public void testCompleteImpliedProperties() throws Exception {
        String p = "<project default='x'><target if='baseHERE'/></project>";
        List<String> l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        assertTrue("completing <target if>: " + l, l.contains("basedir"));
        p = "<project default='x'><target><condition><isset property='baseHERE'/></condition></target></project>";
        l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        assertTrue("completing <isset property>: " + l, l.contains("basedir"));
        // XXX could also test other standard names
    }
    
    public void testImport() throws Exception {
        String p = "<project default='x'><impHERE/></project>";
        List<String> l = TestUtil.grammarResultValues(g.queryElements(TestUtil.createCompletion(p)));
        assertTrue("matched <import>", l.contains("import"));
        p = "<project default='x'><import fHERE=''/></project>";
        l = TestUtil.grammarResultValues(g.queryAttributes(TestUtil.createCompletion(p)));
        assertTrue("matched file on <import>: " + l, l.contains("file"));
        p = "<project default='x'><import file='y' optHERE=''/></project>";
        l = TestUtil.grammarResultValues(g.queryAttributes(TestUtil.createCompletion(p)));
        assertTrue("matched optional on <import>: " + l, l.contains("optional"));
        p = "<project default='x'><import file='y' optional='HERE'/></project>";
        l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
        Collections.sort(l);
        assertEquals("true or false for optional on <import>", Arrays.asList("false", "true"), l);
    }

}
