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
package org.netbeans.modules.javascript2.editor.index;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.netbeans.modules.javascript2.editor.navigation.OccurrencesSupport;
import org.netbeans.modules.javascript2.model.api.Model;
import org.netbeans.modules.javascript2.types.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;

/**
 * The JS support functions need to guard against self referential models. If
 * there is no guard, unlimited recursion can occur, leading to a stack
 * overflow.
 */
public class RecursiveStructuresTest extends JsTestBase {

    public RecursiveStructuresTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // org.netbeans.modules.javascript2.model.api.Model has an assert
        // detecting cycles. This must be deactivated for tests
        Field f = Model.class.getDeclaredField("assertFired");
        f.setAccessible(true);
        ((AtomicBoolean) f.get(null)).set(true);
    }

    public void testIndexingSelfreferencingObject() throws Exception {
        checkIndexer("/testfiles/indexer/SelfreferencingObject.js");
    }

    public void testIndexingSelfreferencingFunction() throws Exception {
        checkIndexer("/testfiles/indexer/SelfreferencingFunction.js");
    }

    public void testOccurrencesSelfreferencingObject() throws Exception {
        Model m = getModel("/testfiles/indexer/SelfreferencingObject.js");
        OccurrencesSupport os = new OccurrencesSupport(m);
        os.getOccurrence(0);
    }

    public void testOccurrencesSelfreferencingFunction() throws Exception {
        Model m = getModel("/testfiles/indexer/SelfreferencingFunction.js");
        OccurrencesSupport os = new OccurrencesSupport(m);
        os.getOccurrence(0);
    }

    public Model getModel(String file) throws Exception {
        final Model[] globals = new Model[1];
        Source source = getTestSource(getTestFile(file));

        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override
            void run(ResultIterator resultIterator) throws Exception {
                ParserResult parameter = (ParserResult) resultIterator.getParserResult();
                Model model = Model.getModel(parameter, false);
                globals[0] = model;
            }
        });
        return globals[0];
    }
}
