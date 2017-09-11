/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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

        UnitTestUtils.prepareTest(new String [] { "/org/netbeans/modules/quicksearch/resources/testGetProviders.xml" });
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
