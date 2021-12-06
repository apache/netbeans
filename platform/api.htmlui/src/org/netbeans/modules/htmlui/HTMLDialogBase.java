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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.htmlui.HTMLDialog;

public abstract class HTMLDialogBase {
    List<String> techIds = new ArrayList<>();
    final String url;
    Runnable onPageLoad;

    HTMLDialogBase(String url) {
        this.url = url;
    }


    public abstract String showAndWait();
    public abstract void show(HTMLDialog.OnSubmit onSubmit);
    public abstract <C> C component(Class<C> type);
    protected abstract void onSubmit(String id);

    public void addTechIds(String[] ids) {
        this.techIds.addAll(Arrays.asList(ids));
    }

    public void setOnPageLoad(Runnable onPageLoad) {
        this.onPageLoad = onPageLoad;
    }

    public static HTMLDialogBase create(String url) {
        HTMLDialogBase[] base = { null };
        HtmlPair<?> view = HtmlPair.newView((id) -> {
            base[0].onSubmit(id);
        });
        final HTMLDialogBase dialog = view.isDefault() ? new HTMLDialogImpl(url) : new HTMLDialogView(url, view);
        base[0] = dialog;
        return dialog;
    }
}
