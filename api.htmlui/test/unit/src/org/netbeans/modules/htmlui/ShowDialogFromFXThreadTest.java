/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.htmlui;

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
    
    @BeforeClass public static void initFX() {
        JFXPanel p = new JFXPanel();
        JFrame f = new JFrame();
        f.getContentPane().add(p);
        f.setVisible(true);
    }
    
    @BeforeClass public void initNbResLoc() {
        NbResloc.init();
    }
    
    
    private volatile CountDownLatch cdl;
    @Test public void showDialog() throws Exception {
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
