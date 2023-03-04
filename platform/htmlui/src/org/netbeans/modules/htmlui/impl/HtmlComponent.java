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
package org.netbeans.modules.htmlui.impl;

import java.awt.BorderLayout;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import javax.swing.JComponent;
import net.java.html.js.JavaScriptBody;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;

import static org.netbeans.modules.htmlui.impl.HtmlToolkit.LOG;
import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@TopComponent.Description(
    persistenceType = TopComponent.PERSISTENCE_NEVER,
    preferredID = "browser"
)
public final class HtmlComponent extends TopComponent {
    private final JComponent panel = HtmlToolkit.getDefault().newPanel();
    private ProxyLookup.Controller controller;

    HtmlComponent() {
        setLayout(new BorderLayout());
        this.controller = new ProxyLookup.Controller();
        associateLookup(new ProxyLookup(controller));
        add(panel, BorderLayout.CENTER);
    }

    final void loadFX(ClassLoader loader, URL pageUrl, Callable<Lookup> init, String... ctx) {
        Object webView = HtmlToolkit.getDefault().initHtmlComponent(panel, this::setDisplayName);
        HtmlToolkit.getDefault().load(webView, pageUrl, () -> {
            try {
                Lookup lkp = init.call();
                if (lkp != null) {
                    controller.setLookups(lkp);
                }
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Can't load " + pageUrl, ex);
            }
        }, loader, ctx);
    }
}
