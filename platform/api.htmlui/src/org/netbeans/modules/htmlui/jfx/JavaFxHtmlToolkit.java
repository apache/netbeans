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
package org.netbeans.modules.htmlui.jfx;

import org.netbeans.modules.htmlui.HtmlToolkit;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
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
import javax.swing.JComponent;
import org.netbeans.modules.htmlui.HTMLDialogImpl;
import org.openide.DialogDescriptor;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = HtmlToolkit.class)
public final class JavaFxHtmlToolkit extends HtmlToolkit {
    static final JavaFxHtmlToolkit INSTANCE = new JavaFxHtmlToolkit();

    @Override
    public Object initHtmlComponent(JComponent c, Consumer<String> titleDisplayer) {
        JFXPanel p = (JFXPanel) c;
        Platform.setImplicitExit(false);
        WebView webView = new WebView();
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
                    titleDisplayer.accept(title);
                }
            }
        }
        final X x = new X();
        webView.getEngine().titleProperty().addListener(x);
        HtmlToolkit.getDefault().execute(x);
        bp.setCenter(webView);
        p.setScene(scene);
        return webView;
    }

    @Override
    public Object initHtmlDialog(String url, DialogDescriptor dd, JComponent ourPanel, Runnable onPageLoad, List<String> techIds) {
        JFXPanel p = (JFXPanel) ourPanel;
        Platform.setImplicitExit(false);
        WebView webView = new WebView();
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
        HtmlToolkit.getDefault().execute(x);
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
        load(webView, pageUrl, onPageLoad, loader, techIds.toArray());
        return webView;
    }

    @Override
    public <C> C convertToComponent(Class<C> type, final URL pageUrl, ClassLoader loader, Runnable onPageLoad, List<String> techIds) throws IllegalStateException {
        if (type == Node.class) {
            WebView wv = new WebView();
            load(wv, pageUrl, onPageLoad, loader, techIds.toArray());
            return type.cast(wv);
        } else if (type == JComponent.class) {
            final JFXPanel tmp = new JFXPanel();
            final ClassLoader l = loader;
            HtmlToolkit.getDefault().execute(new Runnable() {
                @Override
                public void run() {
                    WebView wv = new WebView();
                    load(wv, pageUrl, onPageLoad, l, techIds.toArray());
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
            HtmlToolkit.LOG.log(Level.SEVERE,
                "Cannot initialize JavaFX Toolkit access. May cause deadlocks.",
                ex
            );
        }
        GET = g;
        ENTER = n;
        EXIT = x;
    }

    @Override
    public void enterNestedLoop(Object impl) {
        try {
            Object tk = GET.invoke(null);
            ENTER.invoke(tk, impl);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LOG.log(Level.SEVERE, "enterNestedEventLoop(" + impl + ")", ex);
        }
    }

    @Override
    public void exitNestedLoop(Object impl) {
        try {
            Object tk = GET.invoke(null);
            EXIT.invoke(tk, impl, null);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LOG.log(Level.SEVERE, "exitNestedEventLoop(" + impl + ", null)", ex);
        }
    }

    @Override
    public boolean isApplicationThread() {
        return Platform.isFxApplicationThread();
    }

    @Override
    public JComponent newPanel() {
        return new JFXPanel();
    }

    @Override
    public void load(Object webView, URL pageUrl, Runnable runnable, ClassLoader loader, Object[] ctx) {
        NbBrowsers.load((WebView)webView, pageUrl, runnable, loader, ctx);
    }

    @Override
    public void execute(Runnable command) {
        Platform.runLater(command);
    }
}
