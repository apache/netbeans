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

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.openide.*;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public final class HTMLDialogImpl implements Runnable {
    private volatile int state;
    private JComponent p;
    private DialogDescriptor dd;
    private Object webView;
    
    private String url;
    private Runnable onPageLoad;
    private List<String> techIds = new ArrayList<>();
    private boolean nestedLoop;

    public void setUrl(String url) {
        this.url = url;
    }

    public void setOnPageLoad(Runnable onPageLoad) {
        this.onPageLoad = onPageLoad;
    }

    public void addTechIds(String[] ids) {
        this.techIds.addAll(Arrays.asList(ids));
    }
    
    @Override
    public void run() {
        switch (state) {
            case 0:
                initPanel();
                break;
            case 1:
                state = 2;
                webView = HtmlToolkit.getDefault().initHtmlDialog(url, dd, p, this, techIds);
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
        Object val = dd.getValue();
        return val instanceof JButton ? ((JButton)val).getName() : null;
    }

    private void showDialog() {
        p.setPreferredSize(new Dimension(600, 400));
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.setVisible(true);
    }
    
    private void initPanel() {
        p = HtmlToolkit.getDefault().newPanel();
        dd = new DialogDescriptor(p, "");
        dd.setOptions(new Object[0]);
        state = 1;
        HtmlToolkit.getDefault().execute(this);
    }
    
    private void initPage() {
        try {
            onPageLoad.run();
        } catch (Throwable t) {
            Exceptions.printStackTrace(t);
        }
        final JButton[] buttons = Buttons.buttons();
        dd.setOptions(buttons);
    }

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
}
