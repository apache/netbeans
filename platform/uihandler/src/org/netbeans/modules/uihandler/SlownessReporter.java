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
package org.netbeans.modules.uihandler;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.openide.awt.HtmlRenderer;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.NotificationDisplayer.Priority;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jindrich Sedek
 */
class SlownessReporter {

    private final Queue<NotifySnapshot> pending;
    private static final String UI_ACTION_BUTTON_PRESS = "UI_ACTION_BUTTON_PRESS";  //NOI18N
    private static final String UI_ACTION_EDITOR = "UI_ACTION_EDITOR";  //NOI18N
    private static final String UI_ACTION_KEY_PRESS = "UI_ACTION_KEY_PRESS";    //NOI18N
    private static final String DELEGATE_PATTERN = "delegate=.*@";         // NOI18N
    static final long LATEST_ACTION_LIMIT = 1000;//ms
    private static final int PRIORITY;
    static {
        int defPrio = 20000;
        assert (defPrio = 10000) > 0;
        PRIORITY = Integer.getInteger("org.netbeans.modules.uihandler.SlownessReporter.priority", defPrio);
    } // NOI18N
    private static final int CLEAR = Integer.getInteger("org.netbeans.modules.uihandler.SlownessReporter.clear", 0); // NOI18N
    private static final RequestProcessor IO_RP = new RequestProcessor(SlownessReporter.class);
    
    public SlownessReporter() {
        pending = new LinkedList<NotifySnapshot>();
    }

    private String getParam(LogRecord rec, int index) {
        if (rec.getParameters() != null && rec.getParameters().length > index) {
            return rec.getParameters()[index].toString();
        } else {
            assert rec.getParameters() != null;
            assert rec.getParameters().length > index : Integer.toString(rec.getParameters().length);
        }
        return null;
    }

    String getLatestAction(final long time, final long slownessEndTime) {
        final String[] latestActionHolder = new String[1];
        
        Installer.readLogs(new Handler() {

            @Override
            public void publish(LogRecord rec) {
                if (slownessEndTime - rec.getMillis() - time > LATEST_ACTION_LIMIT) {
                    return;
                }
                String latestActionClassName = null;
                if (Installer.IDE_STARTUP.equals(rec.getMessage())) {
                    latestActionClassName = NbBundle.getMessage(SlownessReporter.class, "IDE_STARTUP");
                } else if (UI_ACTION_EDITOR.equals(rec.getMessage()) ||
                        (UI_ACTION_BUTTON_PRESS.equals(rec.getMessage())) ||
                        (UI_ACTION_KEY_PRESS.equals(rec.getMessage()))) {
                    latestActionClassName = getParam(rec, 4);
                }
                if (latestActionClassName != null) {
                    latestActionClassName = latestActionClassName.replace("&", ""); // NOI18N
                    Pattern p = Pattern.compile(DELEGATE_PATTERN);
                    Matcher m = p.matcher(latestActionClassName);
                    if (m.find()) {
                        String delegate = m.group();
                        latestActionClassName = delegate.substring(9, delegate.length() - 1);
                    }
                }
                if (latestActionClassName != null){
                    latestActionHolder[0] = latestActionClassName;
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        });
        return latestActionHolder[0];
    }

    void notifySlowness(final byte[] nps, final long time,
                        final long slownessEndTime, final String slownessType) {
        IO_RP.post(new Runnable() {
            @Override
            public void run() {
                final String latestActionName = getLatestAction(time, slownessEndTime);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        pending.add(new NotifySnapshot(new SlownessData(time, nps, slownessType, latestActionName)));
                        if (pending.size() > 5) {
                            pending.remove().clear();
                        }
                    }
                });
            }
        });
    }

    private static final class NotifySnapshot implements ActionListener, Runnable {

        private final Notification note;
        private final SlownessData data;

        NotifySnapshot(SlownessData data) {
            this.data = data;
            NotificationDisplayer.Priority priority = data.getTime() > PRIORITY ?
                Priority.LOW : Priority.SILENT;
            String message = NbBundle.getMessage(NotifySnapshot.class, data.getSlownessType());
            note = NotificationDisplayer.getDefault().notify(
                    message,
                    ImageUtilities.loadImageIcon("org/netbeans/modules/uihandler/vilik.png", true),
                    createPanel(), createPanel(),
                    priority,
                    NotificationDisplayer.Category.WARNING);
            if (CLEAR > 0) {
                Installer.RP.post(new Runnable() {

                    @Override
                    public void run() {
                        clear();
                    }
                }, CLEAR, Thread.MIN_PRIORITY);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Installer.RP.post(NotifySnapshot.this);
        }

        public void clear() {
            note.clear();
        }

        private JComponent createPanel() {
            JPanel result = new JPanel();
            result.setOpaque(false);
            result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
            result.add(new JLabel(NbBundle.getMessage(NotifySnapshot.class, "BlockedFor" + data.getSlownessType(), data.getTime(), data.getTime() / 1000)));
            result.add(createDetails(NbBundle.getMessage(NotifySnapshot.class, "Report")));
            return result;
        }

        private JButton createDetails(String text) {
            JButton btn = new HtmlButton(text);
            btn.setFocusable(false);
            btn.setBorder(BorderFactory.createEmptyBorder());
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.addActionListener(this);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return btn;
        }

        @Override
        public void run() {
            Installer.displaySummary("ERROR_URL", true, false, true, data); // NOI18N
        }

        private static class HtmlButton extends JButton {
            
            public HtmlButton(String text) {
                super(text);
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                HtmlRenderer.renderString("<html><a>" + getText() + "</a></html>",  // NOI18N
                        g, 0, getBaseline(Integer.MAX_VALUE, getFont().getSize()),
                        Integer.MAX_VALUE, getFont().getSize(),
                        getFont(), getForeground(), HtmlRenderer.STYLE_CLIP, true);
            }

        }
    }
}
