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
