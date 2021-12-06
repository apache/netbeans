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
package org.netbeans.spi.htmlui;

import java.net.URL;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import org.netbeans.api.htmlui.HTMLDialog.OnSubmit;

/**
 *
 * @param <HtmlView>
 * @since 1.23
 */
public interface HtmlViewer<HtmlView, HtmlButton> {
    public HtmlView newView(Consumer<String> lifeCycleCallback);
    public void makeVisible(HtmlView view, OnSubmit callback, Runnable whenReady);
    public void load(HtmlView view, ClassLoader loader, URL pageUrl, Callable<Object> initialize, String[] techIds);

    public <C> C component(HtmlView view, Class<C> type, String url, ClassLoader classLoader, Runnable onPageLoad, String[] techIds);

    public HtmlButton createButton(HtmlView view, String id);
    public String getName(HtmlView view, HtmlButton b);
    public void setText(HtmlView view, HtmlButton b, String text);
    public void setEnabled(HtmlView view, HtmlButton b, boolean enabled);
    public void runLater(HtmlView view, Runnable r);
}
