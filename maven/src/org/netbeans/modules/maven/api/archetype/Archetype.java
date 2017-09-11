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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.api.archetype;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.repository.RepositorySystem;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.embedder.exec.ProgressTransferListener;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Simple model class to describe a Maven archetype. To be created by ArchetypeProvider 
 * implementations, consumed by the New Maven Project wizard.
 * @author mkleint
 */
public final class Archetype {

    private static final Logger LOG = Logger.getLogger(Archetype.class.getName());
    
    private String artifactId;
    private String groupId;
    private String version;
    private String name;
    private String description;
    private String repository;
    public final boolean deletable;
    private Artifact artifact;
    private Artifact pomArtifact;

    
    public Archetype(boolean deletable) {
        this.deletable = deletable;
        artifactId = "";
        groupId = "";
        version = "";
    }
    
    public Archetype() {
        this(true);
    }
    
    
    public String getArtifactId() {
        return artifactId;
    }
    
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }
    
    public String getGroupId() {
        return groupId;
    }
    
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public @NonNull String getName() {
        // #166884
        if ("${project.artifactId}".equals(name)) { //NOI18N
            return artifactId;
        }
        if (name == null || name.trim().length() == 0) {
            return artifactId;
        }
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * optional property.
     * @param repo
     */ 
    public void setRepository(String repo) {
        repository = repo;
    }
    
    /**
     * optional property.
     * @return 
     */
    public String getRepository() {
        return repository;
    }
    
    /**
     * initially non resolved artifact, need to call resolveArtifacts() before getArtifact().getFile() can be used.
     * @return 
     */
    public synchronized Artifact getArtifact() {
        if (artifact == null) {
            MavenEmbedder online = EmbedderFactory.getOnlineEmbedder();
            artifact = online.createArtifact(
                    getGroupId(),
                    getArtifactId(),
                    getVersion(),
                    "jar", //NOI18N
                    "maven-archetype"); //NOI18N

            //hack to get the right extension for the right packaging without the plugin.
            artifact.setArtifactHandler(new ArtifactHandler() {
                @Override
                public String getExtension() {
                    return "jar"; //NOI18N
                }

                @Override
                public String getDirectory() {
                    return null;
                }

                @Override
                public String getClassifier() {
                    return null;
                }

                @Override
                public String getPackaging() {
                    return "maven-archetype"; //NOI18N
                }

                @Override
                public boolean isIncludesDependencies() {
                    return false;
                }

                @Override
                public String getLanguage() {
                    return "java"; //NOI18N
                }

                @Override
                public boolean isAddedToClasspath() {
                    return false;
                }
            });
        }
        return artifact;
    }

    /**
     * initially non resolved artifact, need to call resolveArtifacts() before getArtifact().getFile() can be used.
     * @return 
     */
    public synchronized Artifact getPomArtifact() {
        if (pomArtifact == null) {
            MavenEmbedder online = EmbedderFactory.getOnlineEmbedder();
            pomArtifact = online.createArtifact(
                    getGroupId(),
                    getArtifactId(),
                    getVersion(),
                    "pom", //NOI18N
                    "pom"); //NOI18N
        }
        return pomArtifact;
    }
    
    /**
     * resolve the artifacts associated with the archetype (ideally downloads them to the local repository)
     * @param hndl
     * @throws ArtifactResolutionException
     * @throws ArtifactNotFoundException 
     */
    public void resolveArtifacts(AggregateProgressHandle hndl) throws ArtifactResolutionException, ArtifactNotFoundException {
        MavenEmbedder online = EmbedderFactory.getOnlineEmbedder();
        
        List<ArtifactRepository> repos;
        if (getRepository() == null) {
            repos = Collections.<ArtifactRepository>singletonList(online.createRemoteRepository(RepositorySystem.DEFAULT_REMOTE_REPO_URL, RepositorySystem.DEFAULT_REMOTE_REPO_ID));
        } else {
           repos = Collections.<ArtifactRepository>singletonList(online.createRemoteRepository(getRepository(), "custom-repo"));//NOI18N
           for (RepositoryInfo info : RepositoryPreferences.getInstance().getRepositoryInfos()) {
                if (getRepository().equals(info.getRepositoryUrl())) {
                    repos = Collections.<ArtifactRepository>singletonList(online.createRemoteRepository(getRepository(), info.getId()));//NOI18N
                    break;
                }
            }
        }
        try {
            ProgressTransferListener.setAggregateHandle(hndl);
            
            hndl.start();

//TODO how to rewrite to track progress?
//            try {
//                WagonManager wagon = online.getPlexusContainer().lookup(WagonManager.class);
//                wagon.setDownloadMonitor(new ProgressTransferListener());
//            } catch (ComponentLookupException ex) {
//                Exceptions.printStackTrace(ex);
//            }
            online.resolve(getPomArtifact(), repos, online.getLocalRepository());
            online.resolve(getArtifact(), repos, online.getLocalRepository());
        } catch (ThreadDeath d) { // download interrupted
        } catch (IllegalStateException ise) { //download interrupted in dependent thread. #213812
            if (!(ise.getCause() instanceof ThreadDeath)) {
                throw ise;
            }
        } finally {
            ProgressTransferListener.clearAggregateHandle();
            hndl.finish();
        }
    } 
    
    /**
     * parses the META-INF/maven/archetype-metadata.xml file within the archetype's jar
     * to get the additional required properties. Assumes resolveArtifacts() was called beforehand
     * @return required property name as key and default value as map value
     */
    public Map<String, String> loadRequiredProperties() {
        HashMap<String, String> map = new HashMap<String, String>();
        File fil = getArtifact().getFile();
        assert fil != null : "requires a resolved artifact";
        JarFile jf = null;
        try {
            jf = new JarFile(fil);
            ZipEntry entry = jf.getJarEntry("META-INF/maven/archetype-metadata.xml");
            if (entry == null) {
                entry = jf.getJarEntry("META-INF/maven/archetype.xml");
            }
            if (entry != null) {
                InputStream in = jf.getInputStream(entry);
                try {
                    Document doc = XMLUtil.parse(new InputSource(in), false, false, XMLUtil.defaultErrorHandler(), null);
                    NodeList nl = doc.getElementsByTagName("requiredProperty");
                    for (int i = 0; i < nl.getLength(); i++) {
                        Element rP = (Element) nl.item(i);
                        Element dV = XMLUtil.findElement(rP, "defaultValue", null);
                        map.put(rP.getAttribute("key"), dV != null ? XMLUtil.findText(dV) : null);
                    }
                } finally {
                    in.close();
                }
            }
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage(), ex);
        } catch (SAXException ex) {
            LOG.log(Level.INFO, ex.getMessage(), ex);
        } finally {
            if (jf != null) {
                try {
                    jf.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return map;
    }
    
    
    @Override
    public int hashCode() {
        return getGroupId().trim().hashCode() + 13 * getArtifactId().trim().hashCode() + 23 * getVersion().trim().hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Archetype)) {
            return false;
        }
        Archetype ar1 = (Archetype)obj;
        boolean gr = ar1.getGroupId().trim().equals(getGroupId().trim());
        if (!gr) {
            return false;
        }
        boolean ar = ar1.getArtifactId().trim().equals(getArtifactId().trim());
        if (!ar) {
            return false;
        }
        boolean ver =  ar1.getVersion().trim().equals(getVersion().trim());
        return ver;
    }

    @Override public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }

}
