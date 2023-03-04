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
package org.netbeans.modules.mercurial.ui.queues;

import java.awt.Color;
import java.awt.EventQueue;
import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.config.HgConfigFiles;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Vrabec
 */
class QUtils {
    
    private static final Set<File> acceptedQueuesWarning = Collections.synchronizedSet(new HashSet<File>(5));
    private static final String HG_EXTENSION_QUEUES = "mq"; //NOI18N
    private static final String HG_EXTENSION_QUEUES_WITH_PREFIX = "hgext.mq"; //NOI18N
    
    private QUtils () {
        
    }

    @NbBundle.Messages({
        "LBL_PreparingAction.disabled.mq.confirmation.title=MQ extension not enabled",
        "MSG_PreparingAction.disabled.mq.confirmation.text=<html><p>We strongly recommend you to enable the mq extension.<br>"
            + "See <a href=\"http://netbeans.org/kb/docs/ide/mercurial-queues.html#enable\">the guidance</a> on how to do it.</p>"
            + "<p>You can continue even without it but some commands may work unexpectedly.<br>"
            + "Do you want to continue without enabling the extension?</p></html>"
    })
    static boolean isMQEnabledExtension (final File root) {
        boolean accepted;
        if (acceptedQueuesWarning.contains(root)) {
            accepted = true;
        } else {
            assert !EventQueue.isDispatchThread();
            HgConfigFiles config = HgConfigFiles.getSysInstance();
            config.doReload();
            if (config.getException() != null) {
                Mercurial.LOG.log(Level.INFO, null, config.getException());
                accepted = true;
            } else {
                accepted = config.containsProperty(HgConfigFiles.HG_EXTENSIONS, HG_EXTENSION_QUEUES)
                        || config.containsProperty(HgConfigFiles.HG_EXTENSIONS, HG_EXTENSION_QUEUES_WITH_PREFIX);
            }
            if (!accepted) {
                config = new HgConfigFiles(root);
                if (config.getException() != null) {
                    Mercurial.LOG.log(Level.INFO, null, config.getException());
                    accepted = true;
                } else {
                    accepted = config.containsProperty(HgConfigFiles.HG_EXTENSIONS, HG_EXTENSION_QUEUES)
                        || config.containsProperty(HgConfigFiles.HG_EXTENSIONS, HG_EXTENSION_QUEUES_WITH_PREFIX);
                }
            }
            if (!accepted) {
                JTextPane textPane = new JTextPane();
                textPane.setOpaque(false);
                textPane.setEditable(false);
                textPane.setBackground(new Color(0, 0, 0, 0));
                textPane.setContentType("text/html"); //NOI18N
                textPane.setText(Bundle.MSG_PreparingAction_disabled_mq_confirmation_text());
                textPane.addHyperlinkListener(new HyperlinkListener() {
                    @Override
                    public void hyperlinkUpdate (HyperlinkEvent e) {
                        if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                            URL url = e.getURL();
                            HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault ();
                            displayer.showURL(url);
                        }
                    }
                });
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                        textPane,
                        Bundle.LBL_PreparingAction_disabled_mq_confirmation_title(),
                        NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.WARNING_MESSAGE);
                accepted = NotifyDescriptor.YES_OPTION == DialogDisplayer.getDefault().notify(nd);
            }
            if (accepted) {
                acceptedQueuesWarning.add(new File(root.getAbsolutePath()));
            }
        }
        return accepted;
    }
    
}
