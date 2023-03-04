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

import org.openide.util.Lookup;
import org.netbeans.spi.htmlui.HTMLViewerSpi;

final class HtmlPair<HtmlView, HtmlButton> {
    private final HTMLViewerSpi<HtmlView, HtmlButton> viewer;
    private final HtmlView view;

    HtmlPair(HTMLViewerSpi<HtmlView, HtmlButton> viewer, HtmlView view) {
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

    static HtmlPair<?, ?> newView(HTMLViewerSpi.Context ctx) {
        for (HTMLViewerSpi<?, ?> viewer : Lookup.getDefault().lookupAll(HTMLViewerSpi.class)) {
            HtmlPair<?, ?> pair = newView(viewer, ctx);
            if (pair != null) {
                return pair;
            }
        }
        return newView(FallbackViewer.DEFAULT, ctx);
    }

    private static <HtmlView, HtmlButton> HtmlPair<HtmlView, HtmlButton> newView(HTMLViewerSpi<HtmlView, HtmlButton> viewer, HTMLViewerSpi.Context ctx) {
        final HtmlView view = viewer.newView(ctx);
        return view == null ? null : new HtmlPair<>(viewer, view);
    }

    Object createButton(String id) {
        return viewer.createButton(view, id);
    }

    <C> C component(Class<C> type) {
        return viewer.component(view, type);
    }

    HTMLViewerSpi<HtmlView, HtmlButton> viewer() {
        return viewer;
    }

    HtmlView view() {
        return view;
    }
}
