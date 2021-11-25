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
import org.openide.*;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

final class HTMLDialogImpl extends HTMLDialogBase implements Runnable {
    private volatile int state;
    private ChromeWithButtons panel;
    private Object webView;
    private boolean nestedLoop;

    HTMLDialogImpl(String url) {
        super(url);
    }

    @Override
    public void run() {
        switch (state) {
            case 0:
                initPanel();
                break;
            case 1:
                state = 2;
                webView = HtmlToolkit.getDefault().initHtmlDialog(url, panel.dd, panel.p, this, techIds);
                break;
            case 2:
                initPage();
                if (nestedLoop) {
                    state = 3;
                    EventQueue.invokeLater(this);
                } else {
                    state = -1;
                }
                break;
            case 3:
                showDialog();
                state = 4;
                HtmlToolkit.getDefault().execute(this);
                break;
            case 4:
                state = -1;
                HtmlToolkit.getDefault().exitNestedLoop(this);
                break;
            default:
                throw new IllegalStateException("State: " + state);
        }
    }

    @Override
    public String showAndWait() {
        if (EventQueue.isDispatchThread()) {
            run();
            showDialog();
        } else {
            if (HtmlToolkit.getDefault().isApplicationThread()) {
                nestedLoop = true;
                EventQueue.invokeLater(this);
                HtmlToolkit.getDefault().enterNestedLoop(this);
            } else {
                try {
                    EventQueue.invokeAndWait(this);
                } catch (InterruptedException | InvocationTargetException ex) {
                    throw new IllegalStateException(ex);
                }
                showDialog();
            }
        }
        return panel.getValueName();
    }

    @Override
    protected void onSubmit(String id) {
    }

    private void showDialog() {
        panel.showDialog();
    }

    private void initPanel() {
        state = 1;
        HtmlToolkit.getDefault().execute(this);
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
        state = -1;
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

        void showDialog() {
            p.setPreferredSize(new Dimension(600, 400));
            Dialog d = DialogDisplayer.getDefault().createDialog(dd);
            d.setVisible(true);
        }

        void initButtons() {
            List<JButton> buttons = buttons();
            dd.setOptions(buttons.toArray(new JButton[0]));
        }
    }
}
