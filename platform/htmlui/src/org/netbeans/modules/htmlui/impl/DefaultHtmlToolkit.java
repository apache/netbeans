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
import java.awt.EventQueue;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.DialogDescriptor;

public class DefaultHtmlToolkit extends HtmlToolkit {
    public static final HtmlToolkit DEFAULT = new DefaultHtmlToolkit();

    private DefaultHtmlToolkit() {
    }

    @Override
    public boolean isApplicationThread() {
        return EventQueue.isDispatchThread();
    }

    @Override
    public JComponent newPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(new JLabel("No JavaFX installed!"), BorderLayout.CENTER);
        return p;
    }

    @Override
    public void load(Object webView, URL pageUrl, Runnable runnable, ClassLoader loader, Object[] ctx) {
    }

    @Override
    public Object initHtmlComponent(JComponent p, Consumer<String> titleDisplayer) {
        return null;
    }

    @Override
    public Object initHtmlDialog(URL page, DialogDescriptor dd, JComponent p, Runnable onPageLoad, String[] techIds) {
        return null;
    }

    @Override
    public <C> C convertToComponent(Class<C> type, URL pageUrl, ClassLoader loader, Runnable onPageLoad, List<String> techIds) {
        return null;
    }

    @Override
    public void enterNestedLoop(Object aThis) {
    }

    @Override
    public void exitNestedLoop(Object aThis) {
    }

    @Override
    public void execute(Runnable command) {
        if (EventQueue.isDispatchThread()) {
            command.run();
        } else {
            EventQueue.invokeLater(command);
        }
    }
}
