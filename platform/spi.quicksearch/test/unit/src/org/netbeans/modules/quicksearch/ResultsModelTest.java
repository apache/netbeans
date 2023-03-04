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

import java.util.Collections;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Dafe Simonek
 */
public class ResultsModelTest extends NbTestCase {
    
    private int changeCounter;
    
    public ResultsModelTest(String testName) {
        super(testName);
    }
    
    /** Tests behavior of model when providers are filling it quickly - changes
     * firing for listeners should be coalesced.
     */
    @RandomlyFails // NB-Core-Build #8071: Actual fire change count was 6 times, but expected count was in range of 3-5 times
    public void testFireCoalescing () {
        changeCounter = 0;
        
        ResultsModel model = ResultsModel.getInstance();
        
        final CategoryResult cr = new CategoryResult(
                new ProviderModel.Category(null, "Testing Fire Coalescing", null),
                false);
        
        model.setContent(Collections.singletonList(cr));
        
        model.addListDataListener(new ListDataListener() {

            public void intervalAdded(ListDataEvent e) {
                changeCounter++;
            }

            public void intervalRemoved(ListDataEvent e) {
                changeCounter++;
            }

            public void contentsChanged(ListDataEvent e) {
                changeCounter++;
            }
            
            
        });

        // writer thread, imitates provider which fills result model
        RequestProcessor.Task writer = RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                for (int i = 0; i < CategoryResult.MAX_RESULTS; i++) {
                    cr.addItem(new ResultsModel.ItemResult(null, null, this, String.valueOf(i)));
                    try {
                        // wait longer each second iteration
                        int waitTime = i % 2 == 0 ? ResultsModel.COALESCE_TIME * 2 
                                : ResultsModel.COALESCE_TIME / 10;
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ex) {
                        return;
                    }
                }
            }
        });
        
        writer.waitFinished();
        
        assertTrue("Actual fire change count was " + changeCounter + 
                " times, but expected count was in range of 3-5 times", 
                Math.abs(changeCounter - 4) <= 1);
        
    }

    public void testHTML() throws Exception { // #138728
        assertHTML("<html>a <b>key</b> finding", "a key finding", "key");
        assertHTML("<html><b>key</b>s", "keys", "key");
        assertHTML("<html>skeleton <b>key</b>", "skeleton key", "key");
        assertHTML("<html>a <b>key</b> of a key", "a key of a key", "key");
        assertHTML("<html>A <b>Key</b> Finding", "A Key Finding", "key");
        assertHTML("<html>a <b>key</b> finding", "a key finding", "Key");
        assertHTML("<html>a <b>key</b> &amp; stuff", "a key & stuff", "key");
        assertHTML("<html>a <b>key</b> \u0003", "a key \u0003", "key");
        assertHTML("<html>some other finding", "some other finding", "key");
        assertHTML("<html>leave my keys alone!", "<html>leave my keys alone!", "key");
    }
    private void assertHTML(String displayed, String provided, String searched) {
        assertEquals(displayed, new ResultsModel.ItemResult(null, Accessor.DEFAULT.createRequest(searched, null), null, provided).getDisplayName());
    }

}
