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
package org.netbeans.modules.remote.impl.fs.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.CharConversionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionListener;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.remote.api.ConnectionNotifier;
import org.netbeans.modules.remote.api.ConnectionNotifier.ExplicitConnectionListener;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;

/**
 *
 */
public class ConnectionNotifierDelegate implements ConnectionListener {


    private static final Map<ExecutionEnvironment, ConnectionNotifierDelegate> instances = new HashMap<>();
    private static final RequestProcessor RP_WORKER = new RequestProcessor("Connection worker", 2); //NOI18N
    private static final RequestProcessor RP_NOTIFIER = new RequestProcessor("Connection notifier", 1); //NOI18N

    static public ConnectionNotifierDelegate getInstance(ExecutionEnvironment env) {
        synchronized (instances) {
            ConnectionNotifierDelegate instance = instances.get(env);
            if (instance == null) {
                instance = new ConnectionNotifierDelegate(env);
                instances.put(env, instance);
            }
            return instance;
        }
    }

    private final ExecutionEnvironment env;
    private boolean shown;
    private Notification notification; // guards notification and shown fields
    private final Set<ConnectionNotifier.NamedRunnable> tasks = new HashSet<>();
    private final Object lock = new Object();
    private final List<ExplicitConnectionListener> explicitConnectionListeners = new ArrayList<>();

    public ConnectionNotifierDelegate(ExecutionEnvironment execEnv) {
        this.env = execEnv;
        shown = false;
    }

    public void addTask(ConnectionNotifier.NamedRunnable task) {
        synchronized (tasks) {
            tasks.add(task);
            showIfNeed();
        }
    }

    public void removeTask(ConnectionNotifier.NamedRunnable task) {
        synchronized (tasks) {
            tasks.remove(task);
        }
    }


    @Override
    public void connected(ExecutionEnvironment env) {
        if (this.env.equals(env)) {
            ConnectionManager.getInstance().removeConnectionListener(this);
            RP_WORKER.post(new ConnectionNotifier.NamedRunnable("Connection notifier for " + env.getDisplayName()) { //NOI18N
                @Override
                protected void runImpl() {
                    onConnect();
                }
            });
        }
    }

    @Override
    public void disconnected(ExecutionEnvironment env) {
    }

    private void onConnect() {
        Notification n;
        synchronized (lock) {
            n = notification;
            shown = false;
        }
        if (n != null) {
            n.clear();
        }
        List<ConnectionNotifier.NamedRunnable> toLaunch;
        synchronized (tasks) {
            toLaunch = new ArrayList<>(tasks);
            tasks.clear();
        }
        for (ConnectionNotifier.NamedRunnable task : toLaunch) {
            RP_NOTIFIER.post(task);
        }
    }

    public void showIfNeed() {
        synchronized (lock) {
            if (shown) {
                return;
            } else {
                shown = true;
            }
            ConnectionManager cm = ConnectionManager.getInstance();
            cm.addConnectionListener(this);
            show(null);
        }
    }

    private void show(final Exception error) {
        final ActionListener onClickAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RP_WORKER.post(new ConnectionNotifier.NamedRunnable("Requesting connection for " + env.getDisplayName()) { //NOI18N
                    @Override
                    protected void runImpl() {
                        connect();
                    }
                });
            }
        };
        Runnable edtRunner = new Runnable() {
            public void run() {
                String envString = env.getDisplayName(); // RemoteUtil.getDisplayName(env);

                String title, details;
                ImageIcon icon;
                NotificationDisplayer.Category category;

                String text = null;
                if (error == null) {
                    StringBuilder reasons = new StringBuilder();
                    synchronized (tasks) {
                        for (ConnectionNotifier.NamedRunnable task : tasks) {
                            reasons.append(task.getName());
                            reasons.append(' ');
                        }
                    }
                    text = reasons.toString();
                    title = NbBundle.getMessage(ConnectionNotifierDelegate.class, "ConnectionNotifier.TITLE", envString);
                    icon = ImageUtilities.loadImageIcon("org/netbeans/modules/remote/impl/fs/ui/exclamation.gif", false); // NOI18N
                    details = NbBundle.getMessage(ConnectionNotifierDelegate.class, "ConnectionNotifier.DETAILS", envString);
                    category = NotificationDisplayer.Category.INFO;
                } else {
                    title = NbBundle.getMessage(getClass(), "ConnectionNotifier.error.TITLE", envString);
                    icon = ImageUtilities.loadImageIcon("org/netbeans/modules/remote/impl/fs/ui/error.png", false); // NOI18N
                    String errMsg = (error.getMessage() == null) ? "" : error.getMessage();
                    details = NbBundle.getMessage(getClass(), "ConnectionNotifier.error.DETAILS", errMsg, envString);
                    category = NotificationDisplayer.Category.ERROR;
                }
                JComponent baloonComponent = createDetails(text, details, onClickAction);
                JComponent popupComponent = createDetails(text, details, onClickAction);
                Notification n = NotificationDisplayer.getDefault().notify(title, icon, baloonComponent,  popupComponent, NotificationDisplayer.Priority.HIGH, category);
                synchronized (lock) {
                    notification = n;
                }
            }
        };
        SwingUtilities.invokeLater(edtRunner);
                
    }
    
    private JComponent createDetails(String explanationText, String buttonText, ActionListener action) {
        Parameters.notNull("action", action); //NOI18N
        try {
            buttonText = "<html><u>" + XMLUtil.toElementContent(buttonText); //NOI18N
        } catch( CharConversionException ex ) {
            throw new IllegalArgumentException(ex);
        }
        JButton btn = new JButton(buttonText);
        btn.setFocusable(false);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.addActionListener(action);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setForeground(Color.blue);
        if(explanationText == null || explanationText.length() == 0) {
            return btn;
        } else {
            final JComponent res = new JPanel(new BorderLayout());
            JLabel text = new JLabel(explanationText);
            text.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
            res.add(text, BorderLayout.CENTER);
            res.add(btn, BorderLayout.SOUTH);
            res.setOpaque(false);
            return res;
        }
    }

    private void connect() {
        try {
            ConnectionManager.getInstance().connectTo(env);
            List<ExplicitConnectionListener> listeners;
            synchronized (explicitConnectionListeners) {
                listeners = new ArrayList<>(explicitConnectionListeners);
            }
            for (ExplicitConnectionListener l : listeners) {
                l.connected();
            }
        } catch (IOException ex) {
            reShow(ex);
        } catch (CancellationException ex) {
            // don't log cancellation exception
            reShow(null);
        }
    }

    private void reShow(Exception error) {
        synchronized (lock) {
            shown = false;
        }
        show(error);
        //notification.clear();
    }

    void addExplicitConnectionListener(ExplicitConnectionListener listener) {        
        synchronized (explicitConnectionListeners) {
            explicitConnectionListeners.add(listener);
        }
    }

    void removeExplicitConnectionListener(ExplicitConnectionListener listener) {
        synchronized(explicitConnectionListeners) {
            explicitConnectionListeners.remove(listener);
        }
    }
}
