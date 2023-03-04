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

package org.netbeans.modules.hudson.subversion;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.hudson.api.ConnectionBuilder;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.ui.api.HudsonSCMHelper;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 * Creates diff hyperlink for one file delta.
 * Currently relies on a RESTful interface provided by SVN over HTTP(S).
 * Would better use {@link org.tigris.subversion.javahl} but this seems easier.
 * @see <a href="http://subversion.tigris.org/webdav-usage.html">SVN repo URL layout</a>
 */
class SubversionHyperlink implements OutputListener {

    private static final Logger LOG = Logger.getLogger(SubversionHyperlink.class.getName());

    private final String module;
    private final String path;
    private final int startRev;
    private final int endRev;
    private final HudsonJob job;

    /**
     * Creates a new potential hyperlink
     * @param module an HTTP(S) module path, which may need to be stripped down to repo root
     * @param path a file path presumed to be relative to repo root
     * @param startRev the starting revision to display, or 0 for nothing
     * @param endRev the ending revision to display, or 0 for nothing
     */
    SubversionHyperlink(String module, String path, int startRev, int endRev, HudsonJob job) {
        this.module = module;
        this.path = path;
        this.startRev = startRev;
        this.endRev = endRev;
        this.job = job;
    }

    public @Override void outputLineAction(OutputEvent ev) {
        HudsonSCMHelper.noteWillShowDiff(path);
        RequestProcessor.getDefault().post(new Runnable() {
            public @Override void run() {
                String repo = findRepo(module);
                if (repo == null) {
                    return;
                }
                try {
                    final StreamSource before = makeSource(repo, path, startRev);
                    final StreamSource after = makeSource(repo, path, endRev);
                    HudsonSCMHelper.showDiff(before, after, path);
                } catch (IOException x) {
                    LOG.log(Level.INFO, null, x);
                }
            }
        });
    }

    public @Override void outputLineSelected(OutputEvent ev) {
        // XXX could focus diff window if open
    }

    public @Override void outputLineCleared(OutputEvent ev) {}

    private static Set<String> knownRepos = new HashSet<String>();
    /**
     * Trial-and-error way to find the repository root.
     * @param module {@code http://whatever/svnroot/somerepo/trunk/subdir}
     * @return {@code http://whatever/svnroot/somerepo} or null
     */
    private synchronized String findRepo(String module) {
        for (String r : knownRepos) {
            if (module.startsWith(r)) {
                return r;
            }
        }
        LOG.log(Level.FINER, "looking for repo from {0}", module);
        STRIP: while (true) {
            try {
                InputStream is = new ConnectionBuilder().job(job).url(module).connection().getInputStream();
                try {
                    BufferedReader r = new BufferedReader(new InputStreamReader(is));
                    String line;
                    while ((line = r.readLine()) != null) {
                        if (line.equals("  <li><a href=\"../\">..</a></li>")) { // NOI18N
                            module = module.replaceFirst("/[^/]+$", ""); // NOI18N
                            continue STRIP;
                        }
                    }
                } finally {
                    is.close();
                }
                LOG.log(Level.FINER, "  => {0}", module);
                knownRepos.add(module);
                return module;
            } catch (IOException x) {
                LOG.log(Level.FINE, "trying to find repo for " + module, x);
                return null;
            }
        }
    }

    private StreamSource makeSource(String repo, String path, int rev) throws IOException {
        Reader r;
        if (rev == 0) {
            r = new StringReader("");
        } else {
            InputStream is = new URL(repo + "/!svn/ver/" + rev + path).openStream(); // NOI18N
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                FileUtil.copy(is, baos);
                r = new StringReader(baos.toString());
            } finally {
                is.close();
            }
        }
        String mimeType = "text/plain"; // NOI18N // XXX use FileUtil.getMIMETypeExtensions
        String name = path;
        String title = NbBundle.getMessage(SubversionHyperlink.class, "SubversionHyperlink.title", path.replaceFirst(".+/", ""), rev);
        return StreamSource.createSource(name, title, mimeType, r);
    }

}
