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
package org.netbeans.modules.xml.schema.model.visitor;

import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.TestCatalogModel;
import org.netbeans.modules.xml.schema.model.Util;
import org.netbeans.modules.xml.schema.model.impl.SchemaModelImpl;

/**
 * Tests for {@link FindSubstitutionsVisitor}
 * @author Daniel Bell (dbell@netbeans.org)
 */
public class FindSubstitutionsVisitorTest {
    public static final String PARENT_NS_URI = "urn:parent";
    public static final String SUBSTITUTION_GROUP_HEAD = "child";
    private static final String PARENT_SCHEMA = "resources/SubstitutionGroupParent.xsd";
    private static final String CHILD_SCHEMA_ONE = "resources/SubstitutionGroupChildOne.xsd";
    private static final String CHILD_SCHEMA_TWO = "resources/SubstitutionGroupChildTwo.xsd";
    private static final String SUBSTITUTION_ELEMENT_ONE = "child-one";
    private static final String SUBSTITUTION_ELEMENT_TWO = "child-two";
    
    @Rule
    public final TestRule catalogMaintainer = TestCatalogModel.maintainer();
    
    private SchemaModelImpl parentModel;
    private SchemaModelImpl childModelOne;
    private SchemaModelImpl childModelTwo;
    
    @Before
    public void setUp() throws Exception {
        childModelOne = load(CHILD_SCHEMA_ONE);
        childModelTwo = load(CHILD_SCHEMA_TWO);
        parentModel = load(PARENT_SCHEMA);
    }
    
    /**
     * Each schema imports a substitution group base, and defines a global 
     * element that is part of this substitution group.
     * FindSubstitutionsVisitor should resolve this substitution.
     */
    @Test
    public void shouldResolveSubstitutionsFromLinkedSchemas() {
        GlobalElement substitutionGroupHead = getCachedElement(parentModel, SUBSTITUTION_GROUP_HEAD);
        GlobalElement expectedSubstitutionOne = getCachedElement(childModelOne, SUBSTITUTION_ELEMENT_ONE);
        GlobalElement expectedSubstitutionTwo = getCachedElement(childModelTwo, SUBSTITUTION_ELEMENT_TWO);
        
        Set<GlobalElement> possibleSubstitutionsOne = FindSubstitutions.resolveSubstitutions(childModelOne, substitutionGroupHead);
        Set<GlobalElement> possibleSubstitutionsTwo = FindSubstitutions.resolveSubstitutions(childModelTwo, substitutionGroupHead);

        assertEquals(1, possibleSubstitutionsOne.size());
        assertSame("invalid substitution", expectedSubstitutionOne, possibleSubstitutionsOne.iterator().next());
        
        assertEquals(1, possibleSubstitutionsTwo.size());
        assertSame("invalid substitution", expectedSubstitutionTwo, possibleSubstitutionsTwo.iterator().next());
    }
        
    private static GlobalElement getCachedElement(SchemaModelImpl model, String localName) {
        return model.getGlobalComponentsIndexSupport().findByNameAndType(localName, GlobalElement.class);
    }

    private static SchemaModelImpl load(String schemaPath) throws Exception {
        return Util.toSchemaModelImpl(Util.loadSchemaModel2(schemaPath));
    }
}
