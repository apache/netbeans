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

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Dafe Simonek
 */
public class SlowProviderTest extends NbTestCase {
    
    private static final int MAX_TIME = 1000;
    private static final int WAIT_TIME = 4000;

    public SlowProviderTest(String testName) {
        super(testName);
    }

    @RandomlyFails
    public void testResponsiveness () throws Exception {
        UnitTestUtils.prepareTest(new String [] { "/org/netbeans/modules/quicksearch/resources/testSlowProvider.xml" });

        System.out.println("Testing resposiveness against slow providers...");
        
        long startTime = System.currentTimeMillis();
        
        org.openide.util.Task t =
                CommandEvaluator.evaluate("sample text", ResultsModel.getInstance());
        
        long endTime = System.currentTimeMillis();

        assertFalse("Evaluator is slower then expected, max allowed time is " +
                MAX_TIME + " millis, but was " + (endTime - startTime) + " millis.",
                (endTime - startTime) > 1000);

        System.out.println("Testing CommandEvaluator.Wait4AllTask...");
        RequestProcessor.getDefault().post(t);

        // should be still running
        assertFalse(t.isFinished());

        // wait for all providers
        t.waitFinished();

        long waitTime = System.currentTimeMillis();

        assertTrue("Waiting for slow providers doesn't work, waited " +
                (waitTime - startTime) + " millis, but should at least " +
                WAIT_TIME + " millis.", (waitTime - startTime) > WAIT_TIME);


    }

    
    public static class SlowProvider implements SearchProvider {

        public void evaluate(SearchRequest request, SearchResponse response) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                System.err.println("SlowProvider interrupted...");
            }
        }
        
    }

}
