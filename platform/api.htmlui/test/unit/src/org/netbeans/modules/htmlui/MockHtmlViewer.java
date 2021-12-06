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

import java.io.Closeable;
import java.io.Reader;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import org.netbeans.api.htmlui.HTMLDialog;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.spi.htmlui.HtmlViewer;

public final class MockHtmlViewer implements HtmlViewer<MockHtmlViewer.MockUI> {
    @Override
    public MockUI newView(Consumer<String> lifeCycleCallback) {
        return new MockUI();
    }

    @Override
    public void makeVisible(MockUI view, Runnable whenReady) {
        whenReady.run();
    }

    @Override
    public void load(MockUI view, ClassLoader loader, URL pageUrl, Callable<Object> initialize, String[] techIds) {
        try (Closeable c = Fn.activate(view)) {
            view.onSubmit = initialize.call();
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }

    @Override
    public Object createButton(MockUI view, String id) {
        return new MockButton();
    }

    static void selectButton(Fn.Presenter p, String id) {
        MockUI ui = (MockUI) p;
        if (ui.onSubmit instanceof HTMLDialog.OnSubmit) {
            ((HTMLDialog.OnSubmit) ui.onSubmit).onSubmit(id);
        }
    }

    public static final class MockButton {
    }

    static final class MockUI implements Fn.Presenter {
        private Object onSubmit;

        @Override
        public Fn defineFn(String fn, String... strings) {
            return new MockFn(fn);
        }

        @Override
        public void displayPage(URL url, Runnable r) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void loadScript(Reader reader) throws Exception {
            throw new UnsupportedOperationException();
        }

    }

    private static class MockFn extends Fn {
        private final String fn;

        MockFn(String fn) {
            this.fn = fn;
        }

        @Override
        public Object invoke(Object o, Object... os) throws Exception {
            if (fn.contains("getElementsByTagName('button')")) {
                return new Object[0];
            }
            return null;
        }
    }
}
