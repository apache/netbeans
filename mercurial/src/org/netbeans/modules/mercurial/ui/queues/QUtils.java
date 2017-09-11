/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
