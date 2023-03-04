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
package org.netbeans.modules.java.lsp.server.ui;

import java.io.Closeable;
import java.io.Reader;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.java.html.BrwsrCtx;
import static org.junit.Assert.assertTrue;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.FunctionBinding;
import org.netbeans.html.json.spi.PropertyBinding;
import org.netbeans.html.json.spi.Technology;
import org.netbeans.modules.java.lsp.server.protocol.HtmlPageParams;
import org.netbeans.modules.java.lsp.server.protocol.UIContext;
import org.openide.util.lookup.ServiceProvider;
import org.netbeans.spi.htmlui.HTMLViewerSpi;

@ServiceProvider(service = HTMLViewerSpi.class)
public final class MockHtmlViewer extends AbstractLspHtmlViewer {
    private static final Map<String, BrwsrCtx> data = Collections.synchronizedMap(new HashMap<>());

    private void load(View view) {
        UIContext ui = UIContext.find();
        String key = UUID.randomUUID().toString();

        BrwsrCtx ctx = mockPresenter();
        ctx.execute(() -> {
            Object v;
            try (Closeable c = Fn.activate(Contexts.find(ctx, Fn.Presenter.class))) {
                v = view.ctx.onPageLoad();
            } catch (Exception ex) {
                MockTech.exception(ctx, ex);
            }
            data.put(key, ctx);
            ui.showHtmlPage(new HtmlPageParams(key, null));
        });
    }

    public static <T> T assertDialogShown(String id, Class<T> clazz) {
        Object v = MockTech.applied(data.get(id));
        if (v instanceof Exception) {
            throw new AssertionError((Exception)v);
        }
        assertTrue("Expecting " + clazz + " but was " + v, clazz.isInstance(v));
        return clazz.cast(v);
    }

    @Override
    public <C> C component(View view, Class<C> type) {
        if (type == Void.class) {
            load(view);
            return null;
        }
        throw new ClassCastException();
    }

    private BrwsrCtx mockPresenter() {
        final Fn.Presenter p = new Fn.Presenter() {
            @Override
            public Fn defineFn(String string, String... strings) {
                return new Fn() {
                    @Override
                    public Object invoke(Object o, Object... os) throws Exception {
                        return new Object[0];
                    }
                };
            }

            @Override
            public void displayPage(URL url, Runnable r) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void loadScript(Reader reader) throws Exception {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        return new MockTech().register(Contexts.newBuilder(p)).register(Fn.Presenter.class, p, 10).build();
    }

    private static class MockTech implements Technology {

        Object applied;

        public MockTech() {
        }

        Contexts.Builder register(Contexts.Builder b) {
            return b.register(MockTech.class, this, 10).register(Technology.class, this, 10);
        }

        static void exception(BrwsrCtx ctx, Exception ex) {
            Contexts.find(ctx, MockTech.class).applied = ex;
        }

        static Object applied(BrwsrCtx ctx) {
            return Contexts.find(ctx, MockTech.class).applied;
        }

        @Override
        public Object wrapModel(Object o) {
            return o;
        }

        @Override
        public Object toModel(Class type, Object o) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void bind(PropertyBinding pb, Object o, Object data) {
        }

        @Override
        public void valueHasMutated(Object data, String string) {
        }

        @Override
        public void expose(FunctionBinding fb, Object o, Object data) {
        }

        @Override
        public void applyBindings(Object data) {
            this.applied = data;
        }

        @Override
        public Object wrapArray(Object[] os) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void runSafe(Runnable r) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
}
