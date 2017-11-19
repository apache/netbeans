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

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javax.swing.JFrame;
import net.java.html.BrwsrCtx;
import net.java.html.geo.OnLocation;
import net.java.html.geo.Position;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FreeGeoProviderTest {
    private static final CountDownLatch down = new CountDownLatch(1);
    private static BrwsrCtx ctx;

    private CountDownLatch done = new CountDownLatch(1);
    private Position position;
    private Exception error;

    @BeforeClass
    public static void initializeContext() throws Exception {
        final JFXPanel p = new JFXPanel();
        final URL u = DialogsTest.class.getResource("/org/netbeans/api/htmlui/empty.html");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                WebView v = new WebView();
                Scene s = new Scene(v);
                p.setScene(s);
                NbBrowsers.load(v, u, new Runnable() {
                    @Override
                    public void run() {
                        ctx = BrwsrCtx.findDefault(DialogsTest.class);
                        down.countDown();
                    }
                }, null);
            }
        });
        down.await();
        JFrame f = new JFrame();
        f.getContentPane().add(p);
        f.pack();
        f.setVisible(true);
    }


    @Test
    public void checkGeoLocation() throws InterruptedException {
        ctx.execute(new Runnable() {
            @Override
            public void run() {
                Position.Handle query = Loc.createQuery(FreeGeoProviderTest.this);
                query.setTimeout(10000);
                query.start();
            }
        });
        done.await(15, TimeUnit.SECONDS);
        if (error != null) {
            return;
        }
        assertNotNull(position);
    }

    @OnLocation(className = "Loc", onError = "noLocation")
    public void location(Position p) {
        this.position = p;
        done.countDown();
    }

    public void noLocation(Exception ex) {
        error = ex;
        done.countDown();
    }
}
