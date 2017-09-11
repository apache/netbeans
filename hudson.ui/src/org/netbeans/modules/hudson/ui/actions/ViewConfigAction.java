/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
