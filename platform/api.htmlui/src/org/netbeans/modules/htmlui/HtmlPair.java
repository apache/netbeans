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

import org.netbeans.spi.htmlui.HtmlViewer;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import org.netbeans.api.htmlui.HTMLDialog.OnSubmit;
import org.openide.util.Lookup;

final class HtmlPair<HtmlView, HtmlButton> {
    private final HtmlViewer<HtmlView, HtmlButton> viewer;
    private final HtmlView view;

    HtmlPair(HtmlViewer<HtmlView, HtmlButton> viewer, HtmlView view) {
        this.viewer = viewer;
        this.view = view;
    }

    static Class<?> loadClass(String c) throws ClassNotFoundException {
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
        if (l == null) {
            l = Thread.currentThread().getContextClassLoader();
        }
        if (l == null) {
            l = Pages.class.getClassLoader();
        }
        return Class.forName(c, true, l);
    }

    static HtmlPair<?, ?> newView(Consumer<String> life) {
        for (HtmlViewer<?, ?> viewer : Lookup.getDefault().lookupAll(HtmlViewer.class)) {
            HtmlPair<?, ?> pair = newView(viewer, life);
            if (pair != null) {
                return pair;
            }
        }
        return newView(null, life); // XXX
    }

    private static <HtmlView, HtmlButton> HtmlPair<HtmlView, HtmlButton> newView(HtmlViewer<HtmlView, HtmlButton> viewer, Consumer<String> life) {
        final HtmlView view = viewer.newView(life);
        return view == null ? null : new HtmlPair<>(viewer, view);
    }

    final void makeVisible(OnSubmit callback, Runnable whenReady) {
        viewer.makeVisible(view, callback, whenReady);
    }

    final void load(ClassLoader loader, URL pageUrl, Callable<Object> initialize, String... techIds) {
        viewer.load(view, loader, pageUrl, initialize, techIds);
    }

    final boolean isDefault() {
        return this.viewer == null; // XXX
    }

    Object createButton(String id) {
        return viewer.createButton(view, id);
    }

    <C> C component(Class<C> type, String url, ClassLoader classLoader, Runnable onPageLoad, String[] techIds) {
        return viewer.component(view, type, url, classLoader, onPageLoad, techIds);
    }

    HtmlViewer<HtmlView, HtmlButton> viewer() {
        return viewer;
    }

    HtmlView view() {
        return view;
    }
}
