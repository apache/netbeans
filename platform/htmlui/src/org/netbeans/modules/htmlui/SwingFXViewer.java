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
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.netbeans.api.htmlui.HTMLDialog;
import org.netbeans.api.htmlui.HTMLDialog.OnSubmit;
import org.netbeans.spi.htmlui.HtmlViewer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.lookup.ServiceProvider;
import org.netbeans.modules.htmlui.SwingFXViewer.SFXView;
import org.openide.util.Lookup;

@ServiceProvider(service = HtmlViewer.class)
public final class SwingFXViewer implements HtmlViewer<SFXView, JButton> {
    @Override
    public SFXView newView(Consumer<String> lifeCycleCallback) {
        return new SFXView(lifeCycleCallback);
    }

    @Override
    public void makeVisible(SFXView view, OnSubmit callback, Runnable whenReady) {
        view.makeVisible(callback, whenReady);
    }

    @Override
    public void load(SFXView view, ClassLoader loader, URL pageUrl, Callable<Object> initialize, String[] techIds) {
        if (view.component != null) {
            view.component.loadFX(loader, pageUrl, initialize, techIds);
        } else {
            view.buttons.loadFX(loader, pageUrl, initialize, techIds);
        }
    }

    @Override
    public JButton createButton(SFXView view, String id) {
        JButton b = new JButton();;
        b.setName(id);
        view.buttons.add(b);
        return b;
    }

    @Override
    public String getName(SFXView view, JButton b) {
        return b.getName();
    }

    @Override
    public void setText(SFXView view, JButton b, String text) {
        b.setText(text);
    }

    @Override
    public void setEnabled(SFXView view, JButton b, boolean enabled) {
        b.setEnabled(enabled);
    }

    @Override
    public void runLater(SFXView view, Runnable r) {
        EventQueue.invokeLater(r);
    }

    @Override
    public <C> C component(SFXView view, Class<C> type, String url, ClassLoader classLoader, Runnable onPageLoad, String[] techIds) {
        ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
        if (loader == null) {
            loader = SwingFXViewer.class.getClassLoader();
        }
        final URL pageUrl;
        try {
            pageUrl = new URL(url);
        } catch (MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
        return HtmlToolkit.getDefault().convertToComponent(type, pageUrl, loader, onPageLoad, Arrays.asList(techIds));
    }

    static final class SFXView {
        final HtmlComponent component;
        final ChromeWithButtons buttons;

        SFXView(Consumer<String> life) {
            if (life == null) {
                this.component = new HtmlComponent();
                this.buttons = null;
            } else {
                this.component = null;
                this.buttons = new ChromeWithButtons(life);
            }
        }

        void makeVisible(OnSubmit onSubmit, Runnable whenReady) {
            if (component != null) {
                component.open();
                component.requestActive();
                HtmlToolkit.getDefault().execute(whenReady);
            } else {
                buttons.onSubmit = onSubmit;
//                if (onSubmit == null) {
//                    // showAndWait
//                } else {
//                    // show
//                }
                if (HtmlToolkit.getDefault().isApplicationThread()) {
                    whenReady.run();
                } else {
                    HtmlToolkit.getDefault().execute(whenReady);
                }
            }
        }

        private static final class ChromeWithButtons {
            final Consumer<String> life;
            final JComponent p;
            final DialogDescriptor dd;
            private List<JButton> buttons = new ArrayList<>();
            OnSubmit onSubmit;

            public ChromeWithButtons(Consumer<String> life) {
                this.life = life;
                this.p = HtmlToolkit.getDefault().newPanel();
                this.dd = new DialogDescriptor(p, "");
                this.dd.setOptions(new Object[0]);
            }

            void add(JButton b) {
                this.buttons.add(b);
            }

            String getValueName() {
                Object val = dd.getValue();
                return val instanceof JButton ? ((JButton)val).getName() : null;
            }

            void showDialog(HTMLDialog.OnSubmit onSubmit) {
                p.setPreferredSize(new Dimension(600, 400));
                Dialog modalDialog = DialogDisplayer.getDefault().createDialog(dd);
                dd.setButtonListener((ev) -> {
                    String id = null;
                    if (ev.getSource() instanceof JButton) {
                        id = ((JButton) ev.getSource()).getName();
                    }
                    if (onSubmit != null && id != null) {
                        if (!onSubmit.onSubmit(id)) {
                            return;
                        }
                    }
                    modalDialog.setVisible(false);
                    this.life.accept(id);
                });
                modalDialog.setVisible(true);
            }

            void initButtons() {
                dd.setOptions(this.buttons.toArray(new JButton[0]));
                dd.setClosingOptions(new Object[0]);
            }

            private void loadFX(ClassLoader loader, URL pageUrl, Callable<Object> initialize, String[] techIds) {
                if (onSubmit != null || EventQueue.isDispatchThread()) {
                    Runnable initSeq = initializationSequence(pageUrl.toString(), Arrays.asList(techIds), initialize, (__) -> {
                        showDialog(onSubmit);
                    });
                    EventQueue.invokeLater(initSeq);
                } else {
                    Runnable initSeq = initializationNestedLoop(pageUrl.toString(), initialize, Arrays.asList(techIds));
                    if (HtmlToolkit.getDefault().isApplicationThread()) {
                        EventQueue.invokeLater(initSeq);
                        HtmlToolkit.getDefault().enterNestedLoop(this);
                    } else {
                        try {
                            EventQueue.invokeAndWait(initSeq);
                        } catch (InterruptedException | InvocationTargetException ex) {
                            throw new IllegalStateException(ex);
                        }
                    }
                }
            }

            final Runnable initializationSequence(
                String url, List<String> techIds,
                Callable<Object> initialize, Consumer<OnSubmit> afterInitPage
            ) {
                return () -> {
                    HtmlToolkit.getDefault().execute(() -> {
                        HtmlToolkit.getDefault().initHtmlDialog(url, dd, p, () -> {
                            OnSubmit onSubmit;
                            try {
                                Object ret = initialize.call();
                                onSubmit = ret instanceof OnSubmit ? (OnSubmit) ret : null;
                            } catch (Exception ex) {
                                throw new IllegalStateException(ex);
                            }
                            initButtons();
                            if (afterInitPage != null) {
                                EventQueue.invokeLater(() -> {
                                    afterInitPage.accept(onSubmit);
                                });
                            }
                        }, techIds);
                    });
                };
            }

            final Runnable initializationNestedLoop(String url, Callable<Object> initialize, List<String> techIds) {
                return initializationSequence(url, techIds, initialize, (__) -> {
                    showDialog(null);
                    HtmlToolkit.getDefault().execute(() -> {
                        HtmlToolkit.getDefault().exitNestedLoop(this);
                    });
                });
            }
        }
    }
}

