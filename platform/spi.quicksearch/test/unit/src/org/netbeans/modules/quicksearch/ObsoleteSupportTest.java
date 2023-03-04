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

package org.netbeans.modules.quicksearch;

import java.util.List;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Dafe Simonek
 */
public class ObsoleteSupportTest extends NbTestCase {
    
    public ObsoleteSupportTest(String testName) {
        super(testName);
    }
    
    public void testObsoleteSupport () throws Exception {
        System.out.println("Testing obsolete support...");

        UnitTestUtils.prepareTest(new String [] { "org/netbeans/modules/quicksearch/resources/testGetProviders.xml" });
        ResultsModel rm = ResultsModel.getInstance();

        CommandEvaluator.evaluate("test obsolete 1", rm);
        List<? extends CategoryResult> categories = rm.getContent();

        CommandEvaluator.evaluate("test obsolete 2", rm);

        for (CategoryResult cr : categories) {
            assertTrue("Category " + cr.getCategory().getDisplayName() +
                    " should be obsolete", cr.isObsolete());
        }
    }

    /* AssertGC returns "Not found!!!", don't know why
    public void testResultsNotLeaking () throws Exception {
        System.out.println("Testing if results are not leaking...");

        UnitTestUtils.prepareTest(new String [] { "/org/netbeans/modules/quicksearch/resources/testResultsNotLeaking.xml" });
        ResultsModel rm = ResultsModel.getInstance();
        rm.setContent(null);

        CommandEvaluator.evaluate("test leak 1", rm);

        // wait for results, evaluation is run in RequestProcessor
        Task task = RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        task.waitFinished();

        // remember result items weakly
        List<WeakReference<ResultsModel.ItemResult>> wItems =
                new ArrayList<WeakReference<ResultsModel.ItemResult>>();
        List<? extends CategoryResult> cats = rm.getContent();
        for (CategoryResult cr : cats) {
            for (ItemResult ir : cr.getItems()) {
                wItems.add(new WeakReference<ItemResult>(ir));
            }
        }

        System.out.println(wItems.size() + " items remembered weakly...");

        // evaluate again, should free previous results
        CommandEvaluator.evaluate("test leak 2", rm);

        // wait for results, evaluation is run in RequestProcessor
        task = RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        task.waitFinished();

        for (WeakReference<ItemResult> wIr : wItems) {
            assertGC("Item result not GCed", wIr);
        }

    }

    public static class ResultsNotLeakingProvider implements SearchProvider, Runnable {

        public void evaluate(SearchRequest request, SearchResponse response) {
            for (int i = 0; i < 10; i++) {
                if (!response.addResult(this, "test result item # " + i)) {
                    return;
                }
                System.out.println("added result " + i + ", provider " +
                        System.identityHashCode(this));
            }
        }

        public void run() {
            // no operation
        }

    }*/

}
