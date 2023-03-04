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

package org.netbeans.modules.hudson.ui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import javax.swing.AbstractAction;
import org.netbeans.modules.hudson.api.ConnectionBuilder;
import org.netbeans.modules.hudson.api.HudsonJob;
import static org.netbeans.modules.hudson.ui.actions.Bundle.*;
import org.openide.filesystems.FileUtil;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.CloneableOpenSupport;

/**
 * Displays {@code config.xml} for a job.
 */
public class ViewConfigAction extends AbstractAction {

    private final HudsonJob job;

    @Messages("ViewConfigAction_label=View config.xml")
    public ViewConfigAction(HudsonJob job) {
        super(ViewConfigAction_label());
        this.job = job;
    }

    @Override public void actionPerformed(ActionEvent e) {
        new ConfigXmlEditor(new EnvImpl(job.getInstance().getUrl(), job.getUrl() + "config.xml")).open();
    }

    private static final class EnvImpl implements CloneableEditorSupport.Env {

        private static final long serialVersionUID = 1L;

        private final String home;
        private final String url;

        EnvImpl(String home, String url) {
            this.home = home;
            this.url = url;
        }
        
        @Messages({"# {0} - URL", "ViewConfigAction_could_not_connect=Could not retrieve: {0}"})
        @Override public InputStream inputStream() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                URLConnection conn = new ConnectionBuilder().homeURL(new URL(home)).url(url).connection();
                InputStream is = conn.getInputStream();
                try {
                    FileUtil.copy(is, baos);
                } finally {
                    is.close();
                }
            } catch (IOException x) {
                if (Exceptions.findLocalizedMessage(x) == null) {
                    Exceptions.attachLocalizedMessage(x, ViewConfigAction_could_not_connect(url));
                }
                throw x;
            }
            return new ByteArrayInputStream(baos.toByteArray());
        }

        @Override public OutputStream outputStream() throws IOException {
            // XXX could permit user to POST modifications... but what if new content is erroneous?
            throw new IOException();
        }

        @Override public Date getTime() {
            return new Date();
        }

        @Override public String getMimeType() {
            return "text/xml";
        }

        @Override public void addPropertyChangeListener(PropertyChangeListener l) {}

        @Override public void removePropertyChangeListener(PropertyChangeListener l) {}

        @Override public void addVetoableChangeListener(VetoableChangeListener l) {}

        @Override public void removeVetoableChangeListener(VetoableChangeListener l) {}

        @Override public boolean isValid() {
            return true;
        }

        @Override public boolean isModified() {
            return false;
        }

        @Override public void markModified() throws IOException {
            // #25762 - does not suffice to display as r/o, but does prevent actual edits
            throw new IOException();
        }

        @Override public void unmarkModified() {}

        @Override public CloneableOpenSupport findCloneableOpenSupport() {
            return new ConfigXmlEditor(this);
        }

    }

    private static final class ConfigXmlEditor extends CloneableEditorSupport {

        private final EnvImpl envImpl;

        ConfigXmlEditor(EnvImpl env) {
            super(env);
            this.envImpl = env;
        }

        @Override protected boolean asynchronousOpen() {
            return true;
        }

        @Override protected String messageSave() {
            return null;
        }

        @Override protected String messageName() {
            return envImpl.url.replaceFirst("^.+/([^/]+)/config[.]xml$", "$1");
        }

        @Override protected String messageToolTip() {
            return envImpl.url;
        }

        @Override protected String messageOpening() {
            return null;
        }

        @Override protected String messageOpened() {
            return null;
        }

    }

}
