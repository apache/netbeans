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

import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javax.swing.JComponent;
import javax.swing.JFrame;
import net.java.html.BrwsrCtx;
import org.netbeans.api.htmlui.HTMLComponent;
import org.netbeans.html.context.spi.Contexts;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class ComponentsTest {
    public ComponentsTest() {
    }
    
    @BeforeClass public void initNbResLoc() {
        NbResloc.init();
    }

    @Test(timeOut = 9000)
    public void loadSwing() throws Exception {
        EnsureJavaFXPresent.checkAndThrow();
        CountDownLatch cdl = new CountDownLatch(1);
        JComponent p = TestPages.getSwing(10, cdl);
        JFrame f = new JFrame();
        f.getContentPane().add(p);
        f.pack();
        f.setVisible(true);
        cdl.await();
    }

    @Test(timeOut = 9000)
    public void loadFX() throws Exception {
        EnsureJavaFXPresent.checkAndThrow();
        final CountDownLatch cdl = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(1);
        final JFXPanel p = new JFXPanel();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Node wv = TestPages.getFX(10, cdl);
                Scene s = new Scene(new Group(wv));
                p.setScene(s);
                done.countDown();
            }
        });
        done.await();
        JFrame f = new JFrame();
        f.getContentPane().add(p);
        f.pack();
        f.setVisible(true);
        cdl.await();
    }

    @HTMLComponent(
        url = "simple.html", className = "TestPages",
        type = JComponent.class, 
        techIds = "second"
    )
    static void getSwing(int param, CountDownLatch called) {
        assertEquals(param, 10, "Correct value passed in");
        called.countDown();
        ATech t = Contexts.find(BrwsrCtx.findDefault(ComponentsTest.class), ATech.class);
        assertNotNull(t, "A technology found");
        assertEquals(t.getClass(), ATech.Second.class);
    }

    @HTMLComponent(
        url = "simple.html", className = "TestPages",
        type = Node.class,
        techIds = "first"
    ) 
    static void getFX(int param, CountDownLatch called) {
        assertEquals(param, 10, "Correct value passed in");
        called.countDown();
        ATech t = Contexts.find(BrwsrCtx.findDefault(ComponentsTest.class), ATech.class);
        assertNotNull(t, "A technology found");
        assertEquals(t.getClass(), ATech.First.class);
    }
    
}
