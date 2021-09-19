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
import org.openide.util.Lookup;

final class HtmlPair<HtmlView> {
    private final HtmlViewer<HtmlView> viewer;
    private final HtmlView view;

    HtmlPair(HtmlViewer<HtmlView> viewer, HtmlView view) {
        this.viewer = viewer;
        this.view = view;
    }

    static Class loadClass(String c) throws ClassNotFoundException {
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
        if (l == null) {
            l = Thread.currentThread().getContextClassLoader();
        }
        if (l == null) {
            l = Pages.class.getClassLoader();
        }
        return Class.forName(c, true, l);
    }

    static HtmlPair<?> newView() {
        for (HtmlViewer viewer : Lookup.getDefault().lookupAll(HtmlViewer.class)) {
            HtmlPair<?> pair = newView(viewer);
            if (pair != null) {
                return pair;
            }
        }
        return newView(HtmlComponent.VIEWER);
    }

    private static <HtmlView> HtmlPair<HtmlView> newView(HtmlViewer<HtmlView> viewer) {
        final HtmlView view = viewer.newView();
        return view == null ? null : new HtmlPair<>(viewer, view);
    }

    final void makeVisible(Runnable whenReady) {
        viewer.makeVisible(view, whenReady);
    }

    final void load(ClassLoader loader, URL pageUrl, Callable<Object> initialize, String... techIds) {
        viewer.load(view, loader, pageUrl, initialize, techIds);
    }
}
