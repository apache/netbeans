/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.css.indexing;

import org.netbeans.modules.css.editor.CssTestBase;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;

public class CssIndexerTest extends CssTestBase {

    public CssIndexerTest(String testName) {
        super(testName);
    }

    public void testIdentifiersWithColons() throws Exception {
        checkIndexer("identifiers-with-colon.css");
    }

    public void testManyClasses() throws Exception {
        checkIndexer("many-classes.css");
    }

    // TODO: add more tests

    @Override
    protected void checkIndexer(String relativePath) throws Exception {
        super.checkIndexer("testfiles/index/" + relativePath);
    }

    @Override
    public EmbeddingIndexerFactory getIndexerFactory() {
        return new CssIndexer.Factory();
    }

}
