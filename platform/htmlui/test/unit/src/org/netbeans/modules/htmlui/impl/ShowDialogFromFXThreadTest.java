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
package org.netbeans.modules.htmlui.impl;

import java.awt.EventQueue;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javax.swing.JFrame;
import org.netbeans.api.htmlui.HTMLDialog;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class ShowDialogFromFXThreadTest implements Runnable {
    private volatile boolean returned;
    
    @BeforeClass(timeOut = 9000)
    public static void initFX() {
        if (!EnsureJavaFXPresent.check()) {
            return;
        }
        JFXPanel p = new JFXPanel();
        JFrame f = new JFrame();
        f.getContentPane().add(p);
        f.setVisible(true);
    }
    
    @BeforeClass public void initNbResLoc() {
        NbResloc.init();
    }
    
    
    private volatile CountDownLatch cdl;

    @Test(timeOut = 9000)
    public void showDialog() throws Exception {
        EnsureJavaFXPresent.checkAndThrow();
        cdl = new CountDownLatch(1);
        Platform.runLater(this);
        cdl.await();
        assertFalse(returned, "displayedOK method has not returned yet");
        cdl = new CountDownLatch(1);
        
        waitAWT();
        
        closeAllDialogs();
        cdl.await();
        assertTrue(returned, "now the method returned OK");
    }
    
    @HTMLDialog(url = "simple.html", className = "TestPages") 
    static void displayedOK(CountDownLatch cdl) {
        cdl.countDown();
    }

    @Override
    public void run() {
        TestPages.displayedOK(cdl);
        returned = true;
        cdl.countDown();
    }
    
    private void closeAllDialogs() throws InterruptedException {
        while (!returned) {
            for (java.awt.Window w : java.awt.Window.getWindows()) {
                w.setVisible(false);
            }
            Thread.sleep(100);
        }
    }
    
    private static void waitAWT() throws Exception {
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
            }
        });
    }
}
