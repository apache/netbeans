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
package org.netbeans.modules.htmlui;

import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.concurrent.CountDownLatch;
import org.netbeans.api.htmlui.HTMLDialog;
import org.netbeans.api.htmlui.HTMLDialog.OnSubmit;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.junit.MockServices;
import org.openide.util.Lookup;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.Test;

public class DialogOnSubmitTest {
    static {
        URLStreamHandlerFactory f = Lookup.getDefault().lookup(URLStreamHandlerFactory.class);
        assertNotNull(f, "Factory found");
        URL.setURLStreamHandlerFactory(f);
    }

    @HTMLDialog(url = "simple.html", className = "DialogOnSubmitTestPages")
    static OnSubmit askQuestion(
        boolean yes, Fn.Presenter[] presenter, String[] usedButton,
        CountDownLatch loaded, CountDownLatch clicked
    ) {
        presenter[0] = Fn.activePresenter();
        loaded.countDown();
        return (id) -> {
            usedButton[0] = id;
            clicked.countDown();
            return true;
        };
    }

    @Test
    public void callbackDialog() throws Exception {
        MockServices.setServices(MockHtmlViewer.class);

        String[] usedButton = { null };
        Fn.Presenter[] presenter = { null };
        CountDownLatch loaded = new CountDownLatch(1);
        CountDownLatch clicked = new CountDownLatch(1);
        DialogOnSubmitTestPages.askQuestion(true, presenter, usedButton, loaded, clicked);
        loaded.await();
        assertNotNull(presenter[0], "Presenter found");
        MockHtmlViewer.selectButton(presenter[0], "MockOK");
        clicked.await();
        assertEquals(usedButton[0], "MockOK");
    }
}
