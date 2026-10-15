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

package org.netbeans.modules.hudson.git;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.hudson.api.ConnectionBuilder;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.Utilities;
import static org.netbeans.modules.hudson.git.Bundle.*;
import org.netbeans.modules.hudson.ui.api.HudsonSCMHelper;
import org.netbeans.modules.hudson.spi.HudsonJobChangeItem;
import org.netbeans.modules.hudson.spi.HudsonSCM;
import org.netbeans.modules.hudson.spi.HudsonSCM.ConfigurationStatus;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.OutputListener;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@ServiceProvider(service=HudsonSCM.class, position=300)
public class HudsonGitSCM implements HudsonSCM {

    private static final Logger LOG = Logger.getLogger(HudsonGitSCM.class.getName());

    @Messages({
        "# {0} - original URL", "# {1} - replacement URL", "ro_replacement=Replacing {0} with {1} in Jenkins configuration.",
        "# {0} - repository location", "warning.local_repo={0} will only be accessible from a Jenkins server on the same machine."
    })
    @Override public Configuration forFolder(File folder) {
        if (!new File(folder, ".git").isDirectory()) {
            return null;
        }
        final URI origin = getRemoteOrigin(org.openide.util.Utilities.toURI(folder), null);
        final String replacement = origin != null ? roReplacement(origin.toString()) : folder.getAbsolutePath();
        return new Configuration() {
            @Override public void configure(Document doc) {
                Element root = doc.getDocumentElement();
                Element configXmlSCM = (Element) root.appendChild(doc.createElement("scm"));
                configXmlSCM.setAttribute("class", "hudson.plugins.git.GitSCM");
                // GitSCM config is horribly complex. Let readResolve do the hard work for now.
                // Note that all this will be wrong if the local repo is using a nondefault remote, or a branch, etc. etc.
                configXmlSCM.appendChild(doc.createElement("source")).appendChild(doc.createTextNode(replacement != null ? replacement : origin.toString()));
                // XXX consider <clean>true</clean> (though it does not seem to work)
                HudsonSCMHelper.addTrigger(doc);
            }
            @Override public ConfigurationStatus problems() {
                if (origin == null) {
                    return ConfigurationStatus.withWarning(warning_local_repo(replacement));
                } else if (replacement != null) {
                    return ConfigurationStatus.withWarning(ro_replacement(origin, replacement));
                } else {
                    return null;
                }
            }
        };
    }

    @Override public String translateWorkspacePath(HudsonJob job, String workspacePath, File localRoot) {
        return null; // XXX
    }

    @Override public List<? extends HudsonJobChangeItem> parseChangeSet(HudsonJobBuild build) {
        final Element changeSet;
        try {
            changeSet = XMLUtil.findElement(new ConnectionBuilder().job(build.getJob()).url(build.getUrl() + "api/xml?tree=changeSet[kind,items[id,author[fullName],msg,paths[file,editType]]]").parseXML().getDocumentElement(), "changeSet", null);
        } catch (IOException x) {
            LOG.log(Level.WARNING, "could not parse changelog for {0}: {1}", new Object[] {build, x});
            return Collections.emptyList();
        }
        class GitItem implements HudsonJobChangeItem {
            final Element itemXML;
            GitItem(Element itemXML) {
                this.itemXML = itemXML;
            }
            @Override public String getUser() {
                return Utilities.xpath("author/fullName", itemXML);
            }
            @Override public String getMessage() {
                return Utilities.xpath("msg", itemXML);
            }
            @Override public Collection<? extends HudsonJobChangeFile> getFiles() {
                class GitFile implements HudsonJobChangeFile {
                    final Element fileXML;
                    GitFile(Element fileXML) {
                        this.fileXML = fileXML;
                    }
                    @Override public String getName() {
                        return Utilities.xpath("file", fileXML);
                    }
                    @Override public EditType getEditType() {
                        return EditType.valueOf(Utilities.xpath("editType", fileXML));
                    }
                    @Override public OutputListener hyperlink() {
                        return null; // XXX no idea how to look up remote content from a Git URL generally
                    }
                }
                List<GitFile> files = new ArrayList<GitFile>();
                NodeList nl = itemXML.getElementsByTagName("path");
                for (int i = 0; i < nl.getLength(); i++) {
                    files.add(new GitFile((Element) nl.item(i)));
                }
                return files;
            }
        }
        String kind = Utilities.xpath("kind", changeSet); // #224993    //NOI18N
        if (kind != null && !"git".equals(kind)) { //NOI18N
            return null; //not a git changelog
        }
        List<GitItem> items = new ArrayList<GitItem>();
        NodeList nl = changeSet.getElementsByTagName("item");
        for (int i = 0; i < nl.getLength(); i++) {
            Element itemXML = (Element) nl.item(i);
            if (kind == null && !looksLikeGitChangeLog(itemXML)) {
                return null; // does not look like a Git changelog
            }
            items.add(new GitItem(itemXML));
        }
        if (items.isEmpty()) {
            return null; // might not be a Git changelog
        }
        return items;
    }

    /**
     * @return false if the item does not look like a Git changelog, true
     * otherwise.
     */
    private boolean looksLikeGitChangeLog(Element itemXML) {
        Element idE = XMLUtil.findElement(itemXML, "id", null);         //NOI18N
        return idE != null
                && XMLUtil.findText(idE).matches("[0-9a-f]{40}");       //NOI18N
    }

    static @CheckForNull URI getRemoteOrigin(URI repository, @NullAllowed HudsonJob job) {
        assert repository.toString().endsWith("/");
        URI cfg = repository.resolve(".git/config");
        String origin = null;
        ClassLoader l = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(HudsonGitSCM.class.getClassLoader()); // #141364
        try {
            ConnectionBuilder cb = new ConnectionBuilder();
            if (job != null) {
                cb = cb.job(job);
            }
            InputStream is = cb.url(cfg.toURL()).connection().getInputStream();
            try {
                Ini ini = new Ini(is);
                Ini.Section section = ini.get("remote \"origin\"");
                if (section != null) {
                    origin = section.get("url");
                }
            } finally {
                is.close();
            }
        } catch (InvalidFileFormatException x) {
            LOG.log(Level.FINE, "{0} was malformed, perhaps no workspace: {1}", new Object[] {cfg, x});
            return null;
        } catch (FileNotFoundException x) {
            LOG.log(Level.FINE, "{0} not found", cfg);
            return null;
        } catch (Exception x) {
            LOG.log(Level.WARNING, "Could not parse " + cfg, x);
            return null;
        } finally {
            Thread.currentThread().setContextClassLoader(l);
        }
        if (origin == null) {
            LOG.log(Level.FINE, "{0} does not specify remote under name ''origin''", cfg);
            return null;
        }
        Matcher m = Pattern.compile("([^@:/]+@[^/:]+):(.+)").matcher(origin);
        if (m.matches()) {
            origin = "ssh://" + m.group(1) + "/" + m.group(2);
        }
        // XXX deal with local file paths; need to translate to file: URLs
        // XXX any further processing needed? absolutization, password stripping?
        try {
            return new URI(origin);
        } catch (URISyntaxException x) {
            LOG.log(Level.FINE, "could not load origin from {0}: {1}", new Object[] {cfg, x});
            return null;
        }
    }

    // http://stackoverflow.com/questions/3189520/hudson-git-plugin-wont-clone-repo-on-linux
    static @CheckForNull String roReplacement(String url) {
        String prefix = "ssh://git@github.com/";
        if (url.startsWith(prefix)) {
            return "git://github.com/" + url.substring(prefix.length());
        }
        Matcher m = Pattern.compile("ssh://.+@git[.]((?:kenai[.]com|java[.]net).+)").matcher(url);
        if (m.matches()) {
            return "git://" + m.group(1);
        }
        return null;
    }

}
