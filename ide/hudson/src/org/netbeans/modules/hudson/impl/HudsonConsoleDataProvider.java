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
package org.netbeans.modules.hudson.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
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
                    BufferedReader r = new BufferedReader(new InputStreamReader(isToUse, StandardCharsets.UTF_8));
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
