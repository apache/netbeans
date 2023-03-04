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

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.lookup.ServiceProvider;
import org.netbeans.modules.htmlui.impl.SwingFXViewer.SFXView;
import org.openide.util.Lookup;
import org.netbeans.spi.htmlui.HTMLViewerSpi;

@ServiceProvider(service = HTMLViewerSpi.class)
public class SwingFXViewer implements HTMLViewerSpi<SFXView, JButton> {
    @Override
    public SFXView newView(Context ctx) {
        return new SFXView(ctx);
    }

    @Override
    public JButton createButton(SFXView view, String id) {
        JButton b = new JButton();
        b.setName(id);
        if (view == null || view.buttons == null) {
            throw new NullPointerException("Am I: " + this + " view: " + view);
        }
        view.buttons.add(b);
        return b;
    }

    @Override
    public String getId(SFXView view, JButton b) {
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
    public <C> C component(SFXView view, Class<C> type) {
        if (type == Void.class) {
            view.makeVisible();
            return null;
        }

        ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
        if (loader == null) {
            loader = SwingFXViewer.class.getClassLoader();
        }
        return HtmlToolkit.getDefault().convertToComponent(
            type, view.ctx.getPage(), loader,
            view.ctx::onPageLoad, Arrays.asList(view.ctx.getTechIds())
        );
    }

    public static final class SFXView {
        final Context ctx;
        final HtmlComponent component;
        final ChromeWithButtons buttons;

        SFXView(Context ctx) {
            this.ctx = ctx;
            if (ctx.isDialog()) {
                this.component = null;
                this.buttons = new ChromeWithButtons(ctx);
            } else if (ctx.isWindow()) {
                this.component = new HtmlComponent();
                this.buttons = null;
            } else {
                this.component = null;
                this.buttons = null;
            }
        }

        void makeVisible() {
            if (ctx.isWindow()) {
                component.open();
                component.requestActive();
                HtmlToolkit.getDefault().execute(() -> {
                    component.loadFX(ctx.getClassLoader(), ctx.getPage(), ctx::onPageLoad, ctx.getTechIds());
                });
            } else if (ctx.isDialog()) {
                if (ctx.isBlocking()) {
                    buttons.showAndWait();
                } else {
                    buttons.showLater();
                }
            }

        }

        private static final class ChromeWithButtons {
            private final Context ctx;
            private final JComponent p;
            private final DialogDescriptor dd;
            private final List<JButton> buttons = new ArrayList<>();

            public ChromeWithButtons(Context ctx) {
                this.ctx = ctx;
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

            String showAndWait() {
                if (EventQueue.isDispatchThread()) {
                    initializationSequence(null).run();
                    showDialog();
                } else {
                    Runnable initSeq = initializationNestedLoop();
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
                Object val = dd.getValue();
                return val instanceof JButton ? ((JButton) val).getName() : null;

            }

            void showLater() {
                initializationSequence(this::showDialog).run();
            }

            void showDialog() {
                p.setPreferredSize(new Dimension(600, 400));
                Dialog modalDialog = DialogDisplayer.getDefault().createDialog(dd);
                dd.setButtonListener((ev) -> {
                    String id = null;
                    if (ev.getSource() instanceof JButton) {
                        id = ((JButton) ev.getSource()).getName();
                    }
                    if (!ctx.onSubmit(id)) {
                        return;
                    }
                    modalDialog.setVisible(false);
                });
                modalDialog.setVisible(true);
            }

            void initButtons() {
                dd.setOptions(this.buttons.toArray(new JButton[0]));
                dd.setClosingOptions(new Object[0]);
            }

            final Runnable initializationSequence(Runnable afterInitPage) {
                return () -> {
                    HtmlToolkit.getDefault().execute(() -> {
                        HtmlToolkit.getDefault().initHtmlDialog(ctx.getPage(), dd, p, () -> {
                            ctx.onPageLoad();
                            initButtons();
                            if (afterInitPage != null) {
                                EventQueue.invokeLater(afterInitPage);
                            }
                        }, ctx.getTechIds());
                    });
                };
            }

            final Runnable initializationNestedLoop() {
                return initializationSequence(() -> {
                    showDialog();
                    ctx.onSubmit(null);
                    HtmlToolkit.getDefault().execute(() -> {
                        HtmlToolkit.getDefault().exitNestedLoop(this);
                    });
                });
            }
        }
    }
}

