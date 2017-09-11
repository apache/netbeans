/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
