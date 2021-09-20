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
package org.netbeans.modules.htmlui;

import java.awt.EventQueue;
import java.util.concurrent.CountDownLatch;
import org.netbeans.api.htmlui.HTMLDialog;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class ShowDialogFromEDTTest implements Runnable {
    @BeforeClass public void initNbResLoc() {
        NbResloc.init();
    }
    
    private CountDownLatch cdl;
    
    @Test(timeOut = 9000)
    public void showDialog() throws InterruptedException {
        EnsureJavaFXPresent.checkAndThrow();
        cdl = new CountDownLatch(1);
        EventQueue.invokeLater(this);
        cdl.await();
    }
    
    @HTMLDialog(url = "simple.html", className = "TestPages") 
    static void displayedOKFromSwing(CountDownLatch cdl) {
        cdl.countDown();
    }

    @Override
    public void run() {
        TestPages.displayedOKFromSwing(cdl);
    }
}
