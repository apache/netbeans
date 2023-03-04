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

import java.net.URL;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.openide.DialogDescriptor;
import org.openide.util.Lookup;

/**
 *
 * @since 1.21
 */
public abstract class HtmlToolkit implements Executor {
    public static final Logger LOG = Logger.getLogger(HtmlToolkit.class.getName());

    public static HtmlToolkit getDefault() {
        HtmlToolkit toolkit = Lookup.getDefault().lookup(HtmlToolkit.class);
        return toolkit == null ? DefaultHtmlToolkit.DEFAULT : toolkit;
    }

    public abstract boolean isApplicationThread();
    public abstract JComponent newPanel();
    public abstract void load(Object webView, URL pageUrl, Runnable runnable, ClassLoader loader, Object[] ctx);
    public abstract Object initHtmlComponent(JComponent p, Consumer<String> titleDisplayer);
    public abstract Object initHtmlDialog(URL page, DialogDescriptor dd, JComponent p, Runnable onPageLoad, String[] techIds);
    public abstract <C> C convertToComponent(Class<C> type, URL pageUrl, ClassLoader loader, Runnable onPageLoad, List<String> techIds);
    public abstract void enterNestedLoop(Object aThis);
    public abstract void exitNestedLoop(Object aThis);
}
