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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import org.netbeans.api.htmlui.HTMLDialog;
import org.netbeans.spi.htmlui.HTMLViewerSpi;
import org.openide.util.Lookup;

public final class HTMLDialogBase {
    private final Buttons<?, ?> buttons;
    private final HtmlPair<?, ?> view;

    private HTMLDialogBase(HtmlPair<?, ?> view, Buttons<?, ?> buttons) {
        this.buttons = buttons;
        this.view = view;
    }

    public static HTMLDialogBase create(String url, String[] resources, Runnable onPageLoad, HTMLDialog.OnSubmit onSubmit, String[] techIds, Class<?> component) {
        ClassLoader loader = onPageLoad.getClass().getClassLoader();
        final URL u;
        try {
            u = new URL(url);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(url, ex);
        }
        class AcceptAndInit implements Consumer<String>, Callable<Lookup> {
            private Buttons<?, ?> buttons;

            synchronized <A, B> Buttons<A, B> assignButtons(Buttons<A, B> b) {
                if (this.buttons != null) {
                    throw new IllegalStateException();
                }
                buttons = b;
                notifyAll();
                return b;
            }

            private Buttons<?, ?> awaitButtons() {
                assert Thread.holdsLock(this);
                while (buttons == null) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        // OK
                    }
                }
                return buttons;
            }

            @Override
            public synchronized void accept(String t) {
                awaitButtons().accept(t);
            }

            @Override
            public Lookup call() throws Exception {
                onPageLoad.run();
                initializeButtons();
                return null;
            }

            private synchronized void initializeButtons() {
                if (component != null) {
                    return;
                }
                awaitButtons().buttons();
            }
        }
        AcceptAndInit init = new AcceptAndInit();
        HTMLViewerSpi.Context c = ContextAccessor.getDefault().newContext(
            loader, u, resources, techIds, onSubmit, init, init, component
        );
        HtmlPair<?, ?> view = HtmlPair.newView(c);
        final Buttons<?, ?> buttons = component == null ? new Buttons<>(view, onSubmit) : null;
        HTMLDialogBase base = new HTMLDialogBase(view, init.assignButtons(buttons));
        view.component(Void.class);
        return base;
    }

    public <C> C component(Class<C> type) {
        return view.component(type);
    }

    protected void onSubmit(String id) {
        this.buttons.accept(id);
    }

    public void show(HTMLDialog.OnSubmit onSubmit) {
    }

    public String showAndWait() {
        return this.buttons.obtainResult();
    }
}
