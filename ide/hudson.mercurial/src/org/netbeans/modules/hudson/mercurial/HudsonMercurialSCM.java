/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.hudson.mercurial;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.netbeans.modules.hudson.api.ConnectionBuilder;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.Utilities;
import static org.netbeans.modules.hudson.mercurial.Bundle.*;
import org.netbeans.modules.hudson.spi.HudsonJobChangeItem;
import org.netbeans.modules.hudson.spi.HudsonJobChangeItem.HudsonJobChangeFile.EditType;
import org.netbeans.modules.hudson.spi.HudsonSCM;
import org.netbeans.modules.hudson.spi.HudsonSCM.ConfigurationStatus;
import org.netbeans.modules.hudson.ui.api.HudsonSCMHelper;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.OutputListener;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Permits use of Mercurial to create and view Hudson projects.
 */
@ServiceProvider(service=HudsonSCM.class, position=200)
public class HudsonMercurialSCM implements HudsonSCM {

    static final Logger LOG = Logger.getLogger(HudsonMercurialSCM.class.getName());

    @Messages({
        "# {0} - repository location",
        "warning.local_repo={0} will only be accessible from a Jenkins server on the same machine.",
        "error.repo.in.parent=Cannot find repository metadata in the project folder. Please configure the build manually."
    })
    public Configuration forFolder(File folder) {
        // XXX could also permit projects as subdirs of Hg repos (lacking SPI currently)
        final FindDefaultPullResult result = findDefaultPull(folder);
        if (!result.found()) {
            return null;
        }
        final URI source = result.getDefaultPull();
        final String repo;
        if ("file".equals(source.getScheme())) { // NOI18N
            repo = org.openide.util.Utilities.toFile(source).getAbsolutePath();
        } else {
            repo = source.toString();
        }
        return new Configuration() {
            public void configure(Document doc) {
                Element root = doc.getDocumentElement();
                Element configXmlSCM = (Element) root.appendChild(doc.createElement("scm")); // NOI18N
                configXmlSCM.setAttribute("class", "hudson.plugins.mercurial.MercurialSCM"); // NOI18N
                configXmlSCM.appendChild(doc.createElement("source")).appendChild(doc.createTextNode(repo)); // NOI18N
                configXmlSCM.appendChild(doc.createElement("modules")).appendChild(doc.createTextNode("")); // NOI18N
                configXmlSCM.appendChild(doc.createElement("clean")).appendChild(doc.createTextNode("true")); // NOI18N
                HudsonSCMHelper.addTrigger(doc);
            }
            public ConfigurationStatus problems() {
                if (result.isFoundInParenFolder()) {
                    return ConfigurationStatus.withError(error_repo_in_parent());
                } else if (!source.isAbsolute() || "file".equals(source.getScheme())) { // NOI18N
                    return ConfigurationStatus.withWarning(warning_local_repo(repo));
                } else {
                    return null;
                }
            }
        };
    }

    /**
     * Find default pull URL in a (project) folder and its ancestor folders.
     */
    private FindDefaultPullResult findDefaultPull(File folder) {
        boolean inParent = false;
        URI defaultPull = null;
        File repoRoot = folder;
        while (repoRoot != null) {
            defaultPull = getDefaultPull(
                    org.openide.util.Utilities.toURI(repoRoot));
            if (defaultPull != null) {
                break;
            } else {
                repoRoot = repoRoot.getParentFile();
                inParent = true;
            }
        }
        return new FindDefaultPullResult(repoRoot, defaultPull, inParent);
    }

    public String translateWorkspacePath(HudsonJob job, String workspacePath, File localRoot) {
        // XXX find repo at or above localRoot, assume workspacePath is repo-relative
        // XXX check whether job's repo matches that of repo, by looking at e.g. head of 00changelog.i
        return null; // XXX
    }

    public @Override List<? extends HudsonJobChangeItem> parseChangeSet(final HudsonJobBuild build) {
        final Element changeSet;
        try {
            changeSet = XMLUtil.findElement(new ConnectionBuilder().job(build.getJob()).url(build.getUrl() + "api/xml?tree=changeSet[kind,items[author[fullName],msg,merge,node,addedPaths,modifiedPaths,deletedPaths]]").parseXML().getDocumentElement(), "changeSet", null);
        } catch (IOException x) {
            LOG.log(Level.WARNING, "could not parse changelog for {0}: {1}", new Object[] {build, x});
            return Collections.emptyList();
        }
        if (!"hg".equals(Utilities.xpath("kind", changeSet))) { // NOI18N
            return null;
        }
        URI repo = getDefaultPull(URI.create(build.getJob().getUrl() + "ws/"), build.getJob()); // NOI18N
        if (repo == null) {
            LOG.log(Level.FINE, "No known repo location for {0}", build.getJob());
        }
        if (repo != null && !"http".equals(repo.getScheme()) && !"https".equals(repo.getScheme())) { // NOI18N
            LOG.log(Level.FINE, "Need hgweb to show changes from {0}", repo);
            repo = null;
        }
        final URI _repo = repo;
        class HgItem implements HudsonJobChangeItem {
            final Element itemXML;
            HgItem(Element xml) {
                this.itemXML = xml;
            }
            public String getUser() {
                return Utilities.xpath("author/fullName", itemXML); // NOI18N
            }
            public String getMessage() {
                return Utilities.xpath("msg", itemXML); // NOI18N
            }
            public Collection<? extends HudsonJobChangeFile> getFiles() {
                if ("true".equals(Utilities.xpath("merge", itemXML))) { // NOI18N
                    return Collections.emptySet();
                }
                final String node = Utilities.xpath("node", itemXML); // NOI18N
                class HgFile implements HudsonJobChangeFile {
                    final String path;
                    final EditType editType;
                    HgFile(String path, EditType editType) {
                        this.path = path;
                        this.editType = editType;
                    }
                    public String getName() {
                        return path;
                    }
                    public EditType getEditType() {
                        return editType;
                    }
                    public OutputListener hyperlink() {
                        return _repo != null ? new MercurialHyperlink(_repo, node, this) : null;
                    }
                }
                List<HgFile> files = new ArrayList<HgFile>();
                NodeList nl = itemXML.getElementsByTagName("addedPath"); // NOI18N
                for (int i = 0; i < nl.getLength(); i++) {
                    files.add(new HgFile(Utilities.xpath("text()", (Element) nl.item(i)), EditType.add)); // NOI18N
                }
                nl = itemXML.getElementsByTagName("modifiedPath"); // NOI18N
                for (int i = 0; i < nl.getLength(); i++) {
                    files.add(new HgFile(Utilities.xpath("text()", (Element) nl.item(i)), EditType.edit)); // NOI18N
                }
                nl = itemXML.getElementsByTagName("deletedPath"); // NOI18N
                for (int i = 0; i < nl.getLength(); i++) {
                    files.add(new HgFile(Utilities.xpath("text()", (Element) nl.item(i)), EditType.delete)); // NOI18N
                }
                return files;
            }
        }
        List<HgItem> items = new ArrayList<HgItem>();
        NodeList nl = changeSet.getElementsByTagName("item"); // NOI18N
        for (int i = 0; i < nl.getLength(); i++) {
            items.add(new HgItem((Element) nl.item(i)));
        }
        return items;
    }

    /**
     * Try to find the default pull location for a possible Hg repository.
     * @param repository the repository location (checkout root)
     * @return its pull location as an absolute URI ending in a slash,
     *         or null in case it could not be determined
     */
    static URI getDefaultPull(URI repository) {
        return getDefaultPull(repository, null);
    }
    private static URI getDefaultPull(URI repository, HudsonJob job) {
        assert repository.toString().endsWith("/");
        URI hgrc = repository.resolve(".hg/hgrc"); // NOI18N
        String defaultPull = null;
        ClassLoader l = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(HudsonMercurialSCM.class.getClassLoader()); // #141364
        try {
            ConnectionBuilder cb = new ConnectionBuilder();
            if (job != null) {
                cb = cb.job(job);
            }
            InputStream is = cb.url(hgrc.toURL()).connection().getInputStream();
            try {
                Ini ini = new Ini(is);
                Ini.Section section = ini.get("paths"); // NOI18N
                if (section != null) {
                    defaultPull = section.get("default-pull"); // NOI18N
                    if (defaultPull == null) {
                        defaultPull = section.get("default"); // NOI18N
                    }
                }
            } finally {
                is.close();
            }
        } catch (InvalidFileFormatException x) {
            LOG.log(Level.FINE, "{0} was malformed, perhaps no workspace: {1}", new Object[] {hgrc, x});
            return null;
        } catch (FileNotFoundException x) {
            try {
                ConnectionBuilder cb = new ConnectionBuilder();
                if (job != null) {
                    cb = cb.job(job);
                }
                cb.url(repository.resolve(".hg/requires").toURL()).connection();
            } catch (IOException x2) {
                LOG.log(Level.FINE, "{0} is not an Hg repo", repository);
                return null;
            }
            LOG.log(Level.FINE, "{0} not found", hgrc);
            return repository;
        } catch (Exception x) {
            LOG.log(Level.WARNING, "Could not parse " + hgrc, x);
            return null;
        } finally {
            Thread.currentThread().setContextClassLoader(l);
        }
        if (defaultPull == null) {
            LOG.log(Level.FINE, "{0} does not specify paths.default or default-pull", hgrc);
            return repository;
        }
        if (!defaultPull.endsWith("/")) { // NOI18N
            defaultPull += "/"; // NOI18N
        }
        if (defaultPull.startsWith("/") || defaultPull.startsWith("\\")) { // NOI18N
            LOG.log(Level.FINE, "{0} looks like a local file location", defaultPull);
            return org.openide.util.Utilities.toURI(new File(defaultPull));
        } else {
            String defaultPullNoPassword = defaultPull.replaceFirst("//[^/]+(:[^/]+)?@", "//");
            try {
                return repository.resolve(stringToURI(defaultPullNoPassword));
            } catch (URISyntaxException x) {
                LOG.log(Level.FINE, "{0} is not a valid URI", defaultPullNoPassword);
                return null;
            }
        }
    }

    /**
     * Convert string to URI. Ensure that drive letters in paths like
     * C:/something are not used as protocol part of the URI.
     */
    private static URI stringToURI(String s) throws URISyntaxException {
        String withProtocol = org.openide.util.Utilities.isWindows()
                && s.matches("\\w:/[^/].*") //NOI18N
                ? "file:/" + s //NOI18N
                : s;
        return new URI(withProtocol);
    }

    /**
     * Result of finding the default pull. Instances of this class are created
     * by method {@link #findDefaultPull(java.io.File)}
     */
    private static class FindDefaultPullResult {

        private final File localRepoFolder;
        private final URI defaultPull;
        private final boolean foundInParenFolder;

        public FindDefaultPullResult(File localRepoFolder, URI defaultPull,
                boolean foundInParenFolder) {
            this.localRepoFolder = localRepoFolder;
            this.defaultPull = defaultPull;
            this.foundInParenFolder = foundInParenFolder;
        }

        public File getLocalRepoFolder() {
            return localRepoFolder;
        }

        public URI getDefaultPull() {
            return defaultPull;
        }

        /**
         * @return False if the folder is root of the repository, true if the
         * folder is a subfolder of the repository root.
         */
        public boolean isFoundInParenFolder() {
            return foundInParenFolder;
        }

        /**
         * @return True if default pull was found, false otherwise.
         */
        public boolean found() {
            return defaultPull != null;
        }
    }
}
