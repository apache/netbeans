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
package org.netbeans.modules.css.lib.properties;

import java.util.Arrays;
import java.util.Collection;
import org.netbeans.modules.css.lib.CssTestBase;
import org.netbeans.modules.css.lib.api.properties.*;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author marekfukala
 */
public class GrammarParseTreeBuilderTest extends CssTestBase {

    public GrammarParseTreeBuilderTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        PRINT_INFO_IN_ASSERT_RESOLVE = true;
//        GrammarResolver.setLogging(GrammarResolver.Log.DEFAULT, true);
//        GrammarParseTreeBuilder.DEBUG = true;
    }

    public void testAnotherNiceFundamentalDesignFlaw() {
        //REQUIRES: TestPropertyDefinitionProvider installed as system service
        String grammar = "<ref>";

        GroupGrammarElement tree = GrammarParser.parse(grammar);
        GrammarResolver resolver = new GrammarResolver(tree);
        resolver.setFeature(GrammarResolver.Feature.keepAnonymousElementsInParseTree, null);

        ResolvedProperty rp = new ResolvedProperty(resolver, "a");
        Node parseTree = rp.getParseTree();

        assertNotNull(parseTree);

        NodeUtil.dumpTree(parseTree);

    }

    @ServiceProvider(service = PropertyDefinitionProvider.class)
    public static class TestPropertyDefinitionProvider implements PropertyDefinitionProvider {

        @Override
        public Collection<String> getPropertyNames(FileObject context) {
            return Arrays.asList(new String[]{"ref"});
        }

        @Override
        public PropertyDefinition getPropertyDefinition(String propertyName) {
            if(propertyName.equals("ref")) {
                return new PropertyDefinition("ref", "[ [ a | b ] | [ a | b ] ]", null);
            }
            return null;
        }
    }
}
