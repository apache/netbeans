/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hudson.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.netbeans.modules.hudson.api.ConnectionBuilder;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.netbeans.modules.hudson.api.ui.ConsoleDataDisplayer;
import org.netbeans.modules.hudson.spi.BuilderConnector;
import org.openide.util.RequestProcessor;

/**
 *
 * @author jhavlin
 */
public class HudsonConsoleDataProvider extends BuilderConnector.ConsoleDataProvider {

    private static final Logger LOG = Logger.getLogger(
            HudsonConsoleDataProvider.class.getName());
    private boolean stopped = false;

    @Override
    public void showConsole(final HudsonJobBuild build,
            final ConsoleDataDisplayer displayer) {
        new RequestProcessor(build.getUrl() + "console").post( //NOI18N
                new Runnable() {
            @Override
            public void run() {
                showBuildConsole(build.getJob(), build.getUrl(),
                        build.getDisplayName(), displayer);
            }
        });
    }

    @Override
    public void showConsole(final HudsonMavenModuleBuild moduleBuild,
            final ConsoleDataDisplayer displayer) {
        new RequestProcessor(moduleBuild.getUrl() + "console").post( //NOI18N
                new Runnable() {
            @Override
            public void run() {
                showBuildConsole(moduleBuild.getBuild().getJob(),
                        moduleBuild.getUrl(), moduleBuild.getDisplayName(),
                        displayer);
            }
        });
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(value = "OS_OPEN_STREAM")
    @java.lang.SuppressWarnings(value = "SleepWhileInLoop")
    public void showBuildConsole(HudsonJob job, String url, String displayName,
            ConsoleDataDisplayer displayer) {

        LOG.log(Level.FINE, "{0} started", url);                        //NOI18N
        displayer.open();
        /* If any metadata is needed, e.g. whether it is running, could use:
         HudsonJobBuild build = instance.getConnector().getJobBuild(job, buildNumber);
         if (build == null) {
         return;
         }
         */
        int start = 0;
        String urlPrefix = url + "progressiveLog?start=";               //NOI18N
        boolean running = job.getLastBuild() > job.getLastCompletedBuild(); // XXX should also check that this is in fact the current build
        try {
            while (!stopped) {
                LOG.log(Level.FINE, "{0} polling", url);                //NOI18N
                URLConnection conn = new ConnectionBuilder().job(job).url(urlPrefix + start).header("Accept-Encoding", "gzip").connection(); //NOI18N
                boolean moreData = Boolean.parseBoolean(conn.getHeaderField("X-More-Data")); // NOI18N
                LOG.log(Level.FINE, "{0} retrieving text from {1}", new Object[]{url, start});
                start = conn.getHeaderFieldInt("X-Text-Size", start);  // NOI18N
                InputStream is = conn.getInputStream();
                try {
                    InputStream isToUse = is;
                    if ("gzip".equals(conn.getContentEncoding())) {     //NOI18N
                        LOG.log(Level.FINE, "{0} using GZIP", url);     //NOI18N
                        isToUse = new GZIPInputStream(is);
                    }
                    // XXX safer to check content type on connection, but in fact Stapler sets it to UTF-8
                    BufferedReader r = new BufferedReader(new InputStreamReader(isToUse, "UTF-8")); //NOI18N
                    String line;
                    while ((line = r.readLine()) != null) {
                        boolean success = displayer.writeLine(line);
                        if (!success) {
                            LOG.log(Level.FINE, "{0} stopped", url);    //NOI18N
                            stopped = true;
                            break;
                        }
                    }
                } finally {
                    is.close();
                }
                if (!moreData) {
                    LOG.log(Level.FINE, "{0} EOF", url);                //NOI18N
                    if (running) {
                        LOG.fine("was running, will resynchronize");    //NOI18N
                        HudsonInstance instance = job.getInstance();
                        if (instance instanceof HudsonInstanceImpl) {
                            ((HudsonInstanceImpl) instance).synchronize(false);
                        }
                    }
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException x) {
                    LOG.log(Level.FINE, "{0} interrupted", url);        //NOI18N
                    break;
                }
            }
        } catch (IOException x) {
            LOG.log(Level.INFO, null, x);
        }
        displayer.close();
    }
}
