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

import org.netbeans.spi.htmlui.HtmlToolkit;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.netbeans.api.htmlui.HTMLDialog;
import org.openide.*;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

final class HTMLDialogImpl extends HTMLDialogBase {
    private final ChromeWithButtons panel;

    HTMLDialogImpl(String url) {
        super(url);
        this.panel = new ChromeWithButtons();
    }

    final Runnable initializationSequence(Runnable afterInitPage) {
        return () -> {
            HtmlToolkit.getDefault().execute(() -> {
                HtmlToolkit.getDefault().initHtmlDialog(url, panel.dd, panel.p, () -> {
                    initPage();
                    if (afterInitPage != null) {
                        EventQueue.invokeLater(afterInitPage);
                    }
                }, techIds);
            });
        };
    }

    final Runnable initializationNestedLoop() {
        return initializationSequence(() -> {
            panel.showDialog(null);
            HtmlToolkit.getDefault().execute(() -> {
                HtmlToolkit.getDefault().exitNestedLoop(this);
            });
        });
    }

    @Override
    public String showAndWait() {
        if (EventQueue.isDispatchThread()) {
            initializationSequence(null).run();
            panel.showDialog(null);
        } else {
            if (HtmlToolkit.getDefault().isApplicationThread()) {
                EventQueue.invokeLater(initializationNestedLoop());
                HtmlToolkit.getDefault().enterNestedLoop(this);
            } else {
                try {
                    EventQueue.invokeAndWait(initializationSequence(null));
                } catch (InterruptedException | InvocationTargetException ex) {
                    throw new IllegalStateException(ex);
                }
                panel.showDialog(null);
            }
        }
        return panel.getValueName();
    }

    @Override
    public void show(HTMLDialog.OnSubmit onSubmit) {
        EventQueue.invokeLater(initializationSequence(() -> {
            panel.showDialog(onSubmit);
        }));
    }

    @Override
    protected void onSubmit(String id) {
    }

    private void initPage() {
        try {
            onPageLoad.run();
        } catch (Throwable t) {
            Exceptions.printStackTrace(t);
        }
        panel.initButtons();
    }

    @Override
    public <C> C component(Class<C> type) {
        ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
        if (loader == null) {
            loader = HTMLDialogImpl.class.getClassLoader();
        }
        final URL pageUrl;
        try {
            pageUrl = new URL(url);
        } catch (MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
        return HtmlToolkit.getDefault().convertToComponent(type, pageUrl, loader, onPageLoad, techIds);
    }

    private static final class ChromeWithButtons extends Buttons<JButton> {
        final JComponent p;
        final DialogDescriptor dd;

        public ChromeWithButtons() {
            this.p = HtmlToolkit.getDefault().newPanel();
            this.dd = new DialogDescriptor(p, "");
            this.dd.setOptions(new Object[0]);
        }

        @Override
        protected JButton createButton(String name) {
            JButton b = new JButton();
            b.setName(name);
            return b;
        }

        @Override
        protected String getName(JButton b) {
            return b.getName();
        }

        @Override
        protected void setText(JButton b, String text) {
            b.setText(text);
        }

        @Override
        protected void setEnabled(JButton b, boolean enabled) {
            b.setEnabled(enabled);
        }

        @Override
        protected void runLater(Runnable r) {
            EventQueue.invokeLater(r);
        }

        String getValueName() {
            Object val = dd.getValue();
            return val instanceof JButton ? ((JButton)val).getName() : null;
        }

        void showDialog(HTMLDialog.OnSubmit onSubmit) {
            p.setPreferredSize(new Dimension(600, 400));
            Dialog d = DialogDisplayer.getDefault().createDialog(dd);
            dd.setButtonListener((ev) -> {
                if (onSubmit != null && ev.getSource() instanceof JButton) {
                    JButton src = (JButton) ev.getSource();
                    if (!onSubmit.onSubmit(src.getName())) {
                        return;
                    }
                }
                d.setVisible(false);
            });
            d.setVisible(true);
        }

        void initButtons() {
            List<JButton> buttons = buttons();
            dd.setOptions(buttons.toArray(new JButton[0]));
            dd.setClosingOptions(new Object[0]);
        }
    }
}
