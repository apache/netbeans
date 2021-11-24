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
import java.util.List;
import java.util.function.Consumer;
import net.java.html.js.JavaScriptBody;
import org.openide.util.Exceptions;

final class HTMLDialogView extends HTMLDialogBase {
    private final HtmlPair<?> view;
    private final FooterButtons buttons;

    public HTMLDialogView(String url, HtmlPair<?> view) {
        super(url);
        this.view = view;
        this.buttons = new FooterButtons();
    }

    @Override
    public String showAndWait() {
        view.makeVisible(() -> {
            try {
                view.load(getClass().getClassLoader(), new URL(url), () -> {
                    onPageLoad.run();
                    List<Object> b = buttons.buttons();
                    return null;
                }, this.techIds.toArray(new String[0]));
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
        return this.buttons.obtainResult();
    }

    @Override
    public <C> C component(Class<C> type) {
        return null;
    }
    
    private final class FooterButtons extends Buttons<Object> implements Consumer<String> {
        private static final String PREFIX = "dialog-buttons-";
        private String result;
        
        public synchronized String obtainResult() {
            while (result == null) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    // ignore
                }
            }
            return result;
        }

        @Override
        public synchronized void accept(String t) {
            if (t.startsWith(PREFIX)) {
                result = t.substring(PREFIX.length());
                notifyAll();
            }
        }
        
        @Override
        protected Object createButton(String name) {
            return view.createButton(PREFIX + name, this);
        }

        @Override
        protected String getName(Object b) {
            String id = buttonName0(b);
            if (id.startsWith(PREFIX)) {
                return id.substring(PREFIX.length());
            }
            return null;
        }

        @Override
        protected void setText(Object b, String text) {
            buttonText0(b, text);
        }

        @Override
        protected void setEnabled(Object b, boolean enabled) {
            buttonDisabled0(b, !enabled);
        }

        @Override
        protected void runLater(Runnable r) {
            r.run();
        }
    }
    
    @JavaScriptBody(args = { "b" }, body = "return b.id;")
    native static String buttonName0(Object b);

    @JavaScriptBody(args = { "b", "text" }, body = "b.innerHTML = text;")
    native static void buttonText0(Object b, String text);

    @JavaScriptBody(args = { "b", "disabled" }, body = "return b.disabled = disabled;")
    native static String buttonDisabled0(Object b, boolean disabled);
}
