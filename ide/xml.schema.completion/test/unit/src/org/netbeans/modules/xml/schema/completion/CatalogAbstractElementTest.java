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
package org.netbeans.modules.xml.schema.completion;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.xml.catalog.settings.CatalogSettings;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;

/**
 * Tests for element substitution.
 * @author Daniel Bell (dbell@netbeans.org)
 */
public class CatalogAbstractElementTest extends AbstractTestCase {

    static final String COMPLETION_DOCUMENT = "resources/CatalogAbstractElementCompletion.xml";

    public CatalogAbstractElementTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new CatalogAbstractElementTest("shouldNotSuggestAbstractElement"));
        suite.addTest(new CatalogAbstractElementTest("shouldExpandSubstitutionGroup"));
        return suite;
    }
    
    @Override
    public void setUp() throws Exception {
        CatalogSettings.getDefault().addCatalog(new ResolvedSchemaProvider());
        setupCompletion(COMPLETION_DOCUMENT);
    }

    /**
     * Elements with {@code abstract="true"} should not be suggested
     */
    public void shouldNotSuggestAbstractElement() {
        List<CompletionResultItem> items = query(951);
        assertDoesNotContainSuggestions(items, false, "child");
    }

    /**
     * All available elements that can be substituted for elements in the 
     * completion context should be presented as completion options
     */
    public void shouldExpandSubstitutionGroup() throws Exception {
        List<CompletionResultItem> items = query(951);
        assertContainSuggestions(items, true, "child-two");
    }

    public static class ResolvedSchemaProvider implements CatalogReader {

        private static final Logger LOG = Logger.getLogger(ResolvedSchemaProvider.class.getName());

        @Override
        public Iterator getPublicIDs() {
            return Collections.EMPTY_LIST.iterator();
        }

        @Override
        public void refresh() {
        }

        @Override
        public String getSystemID(String string) {
            return null;
        }

        @Override
        public String resolveURI(String uri) {
            LOG.log(Level.INFO, "Resolve: {0}", uri);
            switch (uri) {
                case "urn:parent":
                    return ResolvedSchemaProvider.class.getResource("resources/CatalogAbstractElementParent.xsd").toString();
                default:
                    return null;
            }
        }

        @Override
        public String resolvePublic(String string) {
            return null;
        }

        @Override
        public void addCatalogListener(CatalogListener cl) {
        }

        @Override
        public void removeCatalogListener(CatalogListener cl) {
        }
    }
}
