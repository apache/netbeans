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

package org.netbeans.modules.hudson.mercurial;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.hudson.ui.api.HudsonSCMHelper;
import org.netbeans.modules.hudson.spi.HudsonJobChangeItem.HudsonJobChangeFile;
import org.netbeans.modules.hudson.spi.HudsonJobChangeItem.HudsonJobChangeFile.EditType;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

import static org.netbeans.modules.hudson.mercurial.Bundle.*;

/**
 * Creates a hyperlink to a Mercurial change.
 * Assumes hgweb/hgwebdir.cgi.
 */
class MercurialHyperlink implements OutputListener {

    private static final Logger LOG = Logger.getLogger(MercurialHyperlink.class.getName());
    private static final RequestProcessor RP =
            new RequestProcessor(MercurialHyperlink.class);

    private final URI repo;
    private final String node;
    private final HudsonJobChangeFile file;

    MercurialHyperlink(URI repo, String node, HudsonJobChangeFile file) {
        this.repo = repo;
        this.node = node;
        this.file = file;
    }

    public void outputLineAction(OutputEvent ev) {
        HudsonSCMHelper.noteWillShowDiff(file.getName());
        RP.post(new Runnable() {
            public void run() {
                try {
                    final StreamSource before = makeSource(false);
                    final StreamSource after = makeSource(true);
                    HudsonSCMHelper.showDiff(before, after, file.getName());
                } catch (IOException x) {
                    LOG.log(Level.INFO, null, x);
                }
            }
        });
    }

    public void outputLineSelected(OutputEvent ev) {
        // XXX could focus diff window if open
    }

    public void outputLineCleared(OutputEvent ev) {}

    @Messages({"# {0} - file basename", "# {1} - revision hash (or \"null\")", "MercurialHyperlink.title={0} @{1}"})
    private StreamSource makeSource(boolean after) throws IOException {
        Reader r;
        String rev;
        if (file.getEditType() == (after ? EditType.delete : EditType.add)) {
            r = new StringReader("");
            rev = null;
        } else {
            rev = after ? node : findParent(repo, node);
            InputStream is = repo.resolve("raw-file/" + rev + "/" + file.getName()).toURL().openStream(); // NOI18N
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                FileUtil.copy(is, baos);
                r = new StringReader(baos.toString());
            } finally {
                is.close();
            }
        }
        String mimeType = "text/plain"; // NOI18N // XXX use FileUtil.getMIMETypeExtensions
        String name = file.getName();
        String title = MercurialHyperlink_title(name.replaceFirst(".+/", ""), rev != null ? rev.substring(0, 12) : "null"); // NOI18N
        return StreamSource.createSource(name, title, mimeType, r);
    }

    private static final Map<String,String> parents = new HashMap<String,String>();
    private static final Pattern PARENT_COMMENT = Pattern.compile("# Parent +([0-9a-f]{40})"); // NOI18N
    private static synchronized String findParent(URI repo, String node) throws IOException {
        String parent = parents.get(node);
        if (parent == null) {
            URL rawrev = repo.resolve("raw-rev/" + node).toURL(); // NOI18N
            try {
                InputStream is = rawrev.openStream();
                try {
                    BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.ISO_8859_1));
                    String line;
                    while ((line = r.readLine()) != null) {
                        Matcher m = PARENT_COMMENT.matcher(line);
                        if (m.matches()) {
                            parent = m.group(1);
                            break;
                        }
                    }
                } finally {
                    is.close();
                }
                if (parent == null) {
                    throw new IOException("No parent rev spec found"); // NOI18N
                }
            } catch (IOException x) {
                throw (IOException) new IOException("Could not parse " + rawrev + ": "+ x).initCause(x); // NOI18N
            }
            parents.put(node, parent);
        }
        return parent;
    }

}
