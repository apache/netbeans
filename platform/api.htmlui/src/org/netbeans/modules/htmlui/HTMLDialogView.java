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

final class HTMLDialogView extends HTMLDialogBase {
    private final HtmlPair<?, ?> view;
    private final Buttons buttons;

    HTMLDialogView(String url, HtmlPair<?, ?> view) {
        super(url);
        this.view = view;
        this.buttons = Buttons.create(view);
    }

    @Override
    public void show(HTMLDialog.OnSubmit onSubmit) {
        buttons.onSubmit(onSubmit);
        makeVisible(onSubmit);
    }


    @Override
    public String showAndWait() {
        makeVisible(null);
        return this.buttons.obtainResult();
    }

    private void makeVisible(HTMLDialog.OnSubmit onSubmit) {
        view.makeVisible(onSubmit, () -> {
            try {
                view.load(getClass().getClassLoader(), new URL(url), () -> {
                    onPageLoad.run();
                    List<Object> b = buttons.buttons();
                    return onSubmit;
                }, this.techIds.toArray(new String[0]));
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }

    @Override
    public <C> C component(Class<C> type) {
        return view.component(type, url, getClass().getClassLoader(), onPageLoad, this.techIds.toArray(new String[0]));
    }

    @Override
    protected void onSubmit(String id) {
        this.buttons.accept(id);
    }
}
