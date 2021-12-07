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
import org.netbeans.api.htmlui.HTMLDialog;
import org.openide.util.Exceptions;

public final class HTMLDialogBase {
    private final String url;
    private final Runnable onPageLoad;
    private final String[] techIds;
    private final Buttons buttons;
    private final HtmlPair<?, ?> view;

    private HTMLDialogBase(HtmlPair<?, ?> view, String url, Runnable onPageLoad, String[] techIds, Buttons buttons) {
        this.url = url;
        this.onPageLoad = onPageLoad;
        this.techIds = techIds;
        this.buttons = buttons;
        this.view = view;
    }

    public static HTMLDialogBase create(String url, Runnable onPageLoad, HTMLDialog.OnSubmit onSubmit, String[] techIds) {
        Buttons[] base = { null };
        HtmlPair<?, ?> view = HtmlPair.newView((id) -> {
            base[0].accept(id);
        });
        base[0] = Buttons.create(view);
        return new HTMLDialogBase(view, url, onPageLoad, techIds, base[0]);
    }

    public <C> C component(Class<C> type) {
        return view.component(type, url, getClass().getClassLoader(), onPageLoad, this.techIds);
    }

    protected void makeVisible(HTMLDialog.OnSubmit onSubmit) {
        view.makeVisible(onSubmit, () -> {
            try {
                view.load(getClass().getClassLoader(), new URL(url), () -> {
                    onPageLoad.run();
                    List<Object> b = buttons.buttons();
                    return onSubmit;
                }, this.techIds);
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }

    protected void onSubmit(String id) {
        this.buttons.accept(id);
    }

    public void show(HTMLDialog.OnSubmit onSubmit) {
        buttons.onSubmit(onSubmit);
        makeVisible(onSubmit);
    }

    public String showAndWait() {
        makeVisible(null);
        return this.buttons.obtainResult();
    }
}
