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

import java.io.Closeable;
import java.io.Reader;
import java.net.URL;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.spi.htmlui.HTMLViewerSpi;

public final class MockHtmlViewer implements HTMLViewerSpi<MockHtmlViewer.MockUI, Object> {
    @Override
    public MockUI newView(Context ctx) {
        return new MockUI(ctx);
    }

    @Override
    public Object createButton(MockUI view, String id) {
        return new MockButton();
    }

    static void selectButton(Fn.Presenter p, String id) {
        MockUI ui = (MockUI) p;
        ui.ctx.onSubmit(id);
    }

    @Override
    public String getId(MockUI view, Object b) {
        return null;
    }

    @Override
    public void setText(MockUI view, Object b, String text) {
    }

    @Override
    public void setEnabled(MockUI view, Object b, boolean enabled) {
    }

    @Override
    public void runLater(MockUI view, Runnable r) {
        try (Closeable c = Fn.activate(view)) {
            r.run();
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }

    @Override
    public <C> C component(MockUI view, Class<C> type) {
        if (type == Void.class) {
            try (Closeable c = Fn.activate(view)) {
                view.ctx.onPageLoad();
            } catch (Exception ex) {
                throw new AssertionError(ex);
            }
            return null;
        }
        throw new ClassCastException("" + type + " view: " + view);
    }

    public static final class MockButton {
    }

    static final class MockUI implements Fn.Presenter {
        private final HTMLViewerSpi.Context ctx;

        MockUI(Context ctx) {
            this.ctx = ctx;
        }

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
