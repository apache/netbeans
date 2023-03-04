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

package org.netbeans.modules.hudson.tasklist;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.hudson.api.ConnectionBuilder;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.Utilities;
import org.netbeans.spi.tasklist.Task;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Connects to the static code analysis plugin suite.
 * Uses exported API mainly from {@code hudson.plugins.analysis.util.model.FileAnnotation}.
 * @see https://wiki.jenkins-ci.org/display/JENKINS/Static+Code+Analysis+Plug-ins#StaticCodeAnalysisPlug-ins-RemoteAPI
 */
@ServiceProvider(service=JobScanner.class)
public class AnalysisPluginImpl implements JobScanner {

    private static final Logger LOG = Logger.getLogger(AnalysisPluginImpl.class.getName());

    /**
     * Exported item names for types of analysis we support.
     * See {@code PluginDescriptor.getPluginName} for syntax.
     * Could also include {@code tasks} but {@code message} is always null, which is not very nice for our purposes.
     */
    private static final String[] PLUGINS = {"checkstyle", "pmd", "warnings", "dry"};

    @Override public void findTasks(Project p, HudsonJob job, int buildNumber, TaskAdder callback) throws IOException {
        List<FileObject> roots = new ArrayList<FileObject>();
        roots.add(p.getProjectDirectory());
        // Also add Java source roots; otherwise e.g. /tmp/clover2054820708001846544.tmp/org/apache/hadoop/fs/TestFileSystemCaching.java would never be found:
        for (SourceGroup g : ProjectUtils.getSources(p).getSourceGroups(/* JavaProjectConstants.SOURCES_TYPE_JAVA */"java")) {
            roots.add(g.getRootFolder());
        }
        for (String plugin : PLUGINS) {
            if (Thread.interrupted()) {
                return;
            }
            String url = job.getUrl() + buildNumber + "/" + plugin + "Result/api/xml?tree=warnings[fileName,primaryLineNumber,priority,message]";
            Document doc;
            try {
                HttpURLConnection conn = new ConnectionBuilder().job(job).url(url).httpConnection();
                try {
                    InputSource input = new InputSource(conn.getInputStream());
                    input.setSystemId(url);
                    doc = XMLUtil.parse(input, false, false, XMLUtil.defaultErrorHandler(), null);
                } catch (SAXException x) {
                    LOG.log(Level.FINE, "parse error for " + url, x);
                    continue;
                } finally {
                    conn.disconnect();
                }
            } catch (FileNotFoundException x) {
                LOG.log(Level.FINE, "no {0} for {1}", new Object[] {plugin, job});
                continue;
            }
            LOG.log(Level.FINE, "found {0} for {1}", new Object[] {plugin, job});
            for (Element warning : XMLUtil.findSubElements(doc.getDocumentElement())) {
                if (Thread.interrupted()) {
                    return;
                }
                Element warningEl = XMLUtil.findElement(warning, "message", null);
                if (warningEl == null) {
                    LOG.log(Level.FINE, "skipping {0} since it may be pre-1.367", job.getInstance());
                    return;
                }
                String message = XMLUtil.findText(warningEl);
                if (message == null) {
                    LOG.log(Level.WARNING, "no message in <warning> from {0}", url);
                    continue;
                }
                // XXX perhaps create separate groups according to plugin?
                String group = "HIGH".equals(XMLUtil.findText(XMLUtil.findElement(warning, "priority", null))) ? "nb-tasklist-error" : "nb-tasklist-warning"; // else NORMAL or LOW
                String fileName = XMLUtil.findText(XMLUtil.findElement(warning, "fileName", null));
                FileObject f = locate(fileName, roots);
                if (f != null) {
                    LOG.log(Level.FINER, "successfully located {0}", f);
                    int primaryLineNumber = Integer.parseInt(XMLUtil.findText(XMLUtil.findElement(warning, "primaryLineNumber", null)));
                    callback.add(Task.create(f, group, message, primaryLineNumber));
                } else {
                    String workspacePath = workspacePath(fileName, job.getName());
                    if (workspacePath == null) {
                        LOG.log(Level.WARNING, "{0} does not look to be inside {1}", new Object[] {fileName, job});
                        continue;
                    } else {
                        LOG.log(Level.FINE, "did not find any local file for {0}", fileName);
                        callback.add(Task.create(new URL(job.getUrl() + "workspace/" + Utilities.uriEncode(workspacePath)), group, message));
                    }
                }
            }
        }
    }

    static @CheckForNull FileObject locate(String fileName, Collection<FileObject> roots) {
        String fileNameSlashes = fileName.replace('\\', '/');
        int pos = 0;
        while (true) {
            int i = fileNameSlashes.indexOf('/', pos);
            if (i == -1) {
                return null;
            }
            pos = i + 1;
            String path = fileNameSlashes.substring(pos);
            for (FileObject root : roots) {
                FileObject f = root.getFileObject(path);
                if (f != null) {
                    return f;
                }
            }
        }
    }

    static @CheckForNull String workspacePath(String fileName, String jobName) {
        String fileNameSlashes = fileName.replace('\\', '/');
        String infix = "/workspace/" + jobName + "/";
        int i = fileNameSlashes.indexOf(infix);
        if (i == -1) {
            infix = "/" + jobName + "/workspace/";
            i = fileNameSlashes.indexOf(infix);
        }
        if (i == -1) {
            return null;
        } else {
            return fileNameSlashes.substring(i + infix.length());
        }
    }

}
