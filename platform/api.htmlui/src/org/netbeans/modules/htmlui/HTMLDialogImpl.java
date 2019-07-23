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
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.openide.*;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public final class HTMLDialogImpl implements Runnable {
    private static final Logger LOG = Logger.getLogger(HTMLDialogImpl.class.getName());
    
    private volatile int state;
    private NbFxPanel p;
    private DialogDescriptor dd;
    private WebView webView;
    
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
                initFX();
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
                Platform.runLater(this);
                break;
            case 4:
                state = -1;
                exitNestedLoop(this);
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
            if (Platform.isFxApplicationThread()) {
                nestedLoop = true;
                EventQueue.invokeLater(this);
                enterNestedLoop(this);
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
        p = new NbFxPanel();
        dd = new DialogDescriptor(p, "");
        dd.setOptions(new Object[0]);
        state = 1;
        Platform.runLater(this);
    }
    
    private void initFX() {
        Platform.setImplicitExit(false);
        webView = new WebView();
        BorderPane bp = new BorderPane();
        Scene scene = new Scene(bp, Color.ALICEBLUE);

        class X implements ChangeListener<String>, Runnable {

            private String title;

            public X() {
                super();
            }

            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                title = webView.getEngine().getTitle();
                EventQueue.invokeLater(this);
            }

            @Override
            public void run() {
                if (title != null) {
                    dd.setTitle(title);
                }
            }
        }
        final X x = new X();
        webView.getEngine().titleProperty().addListener(x);
        Platform.runLater(x);
        bp.setCenter(webView);
        p.setScene(scene);

        ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
        if (loader == null) {
            loader = HTMLDialogImpl.class.getClassLoader();
        }
        URL pageUrl;
        try {
            pageUrl = new URL(url);
        } catch (MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
        state = 2;
        NbBrowsers.load(webView, pageUrl, this, loader, techIds.toArray());
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
        if (type == Node.class) {
            WebView wv = new WebView();
            NbBrowsers.load(wv, pageUrl, onPageLoad, loader, techIds.toArray());
            return type.cast(wv);
        } else if (type == JComponent.class) {
            final JFXPanel tmp = new JFXPanel();
            final ClassLoader l = loader;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    WebView wv = new WebView();
                    NbBrowsers.load(wv, pageUrl, onPageLoad, l, techIds.toArray());
                    Scene s = new Scene(wv);
                    tmp.setScene(s);
                }
            });
            return type.cast(tmp);
        } else {
            throw new IllegalStateException("Unsupported type: " + type);
        }
    }

    private static final Method GET;
    private static final Method ENTER;
    private static final Method EXIT;
    static {
        Method g = null;
        Method n = null;
        Method x = null;
        try {
            Class<?> tC;
            final String toolkitCN = "com.sun.javafx.tk.Toolkit"; // NOI18N
            try {
                tC = Class.forName(toolkitCN);
            } catch (ClassNotFoundException ex) {
                tC = Stage.class.getClassLoader().loadClass(toolkitCN);
            }
            g = tC.getMethod("getToolkit"); // NOI18N
            n = tC.getMethod("enterNestedEventLoop", Object.class); // NOI18N
            x = tC.getMethod("exitNestedEventLoop", Object.class, Object.class); // NOI18N
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException ex) {
            LOG.log(Level.SEVERE, 
                "Cannot initialize JavaFX Toolkit access. May cause deadlocks.", 
                ex
            );
        }
        GET = g;
        ENTER = n;
        EXIT = x;
    }

    private static void enterNestedLoop(HTMLDialogImpl impl) {
        try {
            Object tk = GET.invoke(null);
            ENTER.invoke(tk, impl);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LOG.log(Level.SEVERE, "enterNestedEventLoop(" + impl + ")", ex);
        }
    }
    
    private static void exitNestedLoop(HTMLDialogImpl impl) {
        try {
            Object tk = GET.invoke(null);
            EXIT.invoke(tk, impl, null);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LOG.log(Level.SEVERE, "exitNestedEventLoop(" + impl + ", null)", ex);
        }
    }
}
