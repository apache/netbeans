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

import java.util.Iterator;
import org.netbeans.junit.NbTestCase;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Dafe Simonek
 */
public class CategoryResultTest extends NbTestCase {
    
    private Exception exc = null;
    
    public CategoryResultTest(String testName) {
        super(testName);
    }
    
    /** Tests if CategoryResult data access is properly synchronized */
    public void testThreadSafe () {
        System.out.println("Testing thread safe behaviour of CategoryResult...");
        
        final CategoryResult cr = new CategoryResult(
                new ProviderModel.Category(null, "Testing Threading", null),
                false);

        // reader thread
        RequestProcessor.Task reader = RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                for (int i = 0; i < 4; i++) {
                    for (Iterator<ResultsModel.ItemResult> it = cr.getItems().iterator(); it.hasNext();) {
                        try {
                            ResultsModel.ItemResult itemResult = it.next();
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException ex) {
                                // ignore...
                            }
                        } catch (Exception exc) {
                            CategoryResultTest.this.exc = exc;
                            return;
                        }
                    }
                }
            }
        }, 10);

        // writer thread
        RequestProcessor.Task writer = RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                for (int i = 0; i < 7; i++) {
                    cr.addItem(new ResultsModel.ItemResult(null, null, this, String.valueOf(i)));
                    try {
                        Thread.sleep(6);
                    } catch (InterruptedException ex) {
                        return;
                    }
                }
            }
        });
        
        writer.waitFinished();
        reader.waitFinished();
        
        String errText = null;
        
        if (exc != null) {
            exc.printStackTrace();
            errText = exc.toString();
        }
        assertNull("Synchronization problem occurred: " + errText, exc);
        
    }


}
