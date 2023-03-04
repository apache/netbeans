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
package org.netbeans.modules.htmlui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import org.netbeans.spi.htmlui.HTMLViewerSpi;
import org.openide.awt.Actions;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "MSG_NoWebView=Cannot perform the action - missing supporting modules",
    "MSG_InstallJavaFX=Install JavaFX Implementation module!"
})
final class FallbackViewer implements HTMLViewerSpi<FallbackViewer.UI, JButton> {
    static final FallbackViewer DEFAULT = new FallbackViewer();
    private static final String ICON = "org/netbeans/modules/htmlui/fallback.png"; // NOI18N

    private FallbackViewer() {
    }

    @Override
    public UI newView(Context context) {
        return new UI(context);
    }

    @Override
    public <C> C component(UI view, Class<C> type) {
        if (type == Void.class) {
            Icon icon = ImageUtilities.loadImageIcon(ICON, true);
            Action action = Actions.forID(
                "System", // NOI18N
                "org.netbeans.modules.autoupdate.ui.actions.PluginManagerAction" // NOI18N
            );
            NotificationDisplayer.getDefault().notify(
                Bundle.MSG_NoWebView(), icon, Bundle.MSG_InstallJavaFX(), (ev) -> {
                    action.actionPerformed(
                        new ActionEvent(ev.getSource(), 100, "available") // NOI18N
                    );
                }, NotificationDisplayer.Priority.NORMAL, NotificationDisplayer.Category.ERROR
            );
            return null;
        }
        throw new ClassCastException(type.getCanonicalName());
    }

    @Override
    public JButton createButton(UI view, String id) {
        JButton b = new JButton();
        b.setName(id);
        return b;
    }

    @Override
    public String getId(UI view, JButton b) {
        return b.getName();
    }

    @Override
    public void setText(UI view, JButton b, String text) {
        b.setText(text);
    }

    @Override
    public void setEnabled(UI view, JButton b, boolean enabled) {
        b.setEnabled(enabled);
    }

    @Override
    public void runLater(UI view, Runnable r) {
        EventQueue.invokeLater(r);
    }

    static final class UI {
        final Context context;

        UI(Context context) {
            this.context = context;
        }
    }
}
