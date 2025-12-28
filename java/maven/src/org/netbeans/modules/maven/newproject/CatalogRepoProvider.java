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

package org.netbeans.modules.maven.newproject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.repository.MirrorSelector;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.netbeans.modules.maven.api.archetype.ArchetypeProvider;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.openide.modules.Places;
import org.openide.util.NbCollections;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * list of archetypes coming from an archetype catalog
 * @author mkleint
 */
public abstract class CatalogRepoProvider implements ArchetypeProvider {

    private static final String EL_ARCHETYPES = "archetypes"; //NOI18N
    private static final String EL_ARCHETYPE = "archetype"; //NOI18N
    private static final String EL_ARTIFACTID = "artifactId"; //NOI18N
    private static final String EL_DESCRIPTION = "description";
    private static final String EL_GROUPID = "groupId"; //NOI18N
    private static final String EL_REPOSITORY = "repository"; //NOI18N
    private static final String EL_VERSION = "version"; //NOI18N

    private static final Logger LOG = Logger.getLogger(CatalogRepoProvider.class.getName());

    protected CatalogRepoProvider() {}

    protected abstract URL file() throws IOException;

    protected abstract String repository();

    public @Override List<Archetype> getArchetypes() {
        try {
            return getArchetypes(file(), repository());
        } catch (IOException ex) {
            LOG.log(Level.INFO, null, ex);
            return Collections.emptyList();
        }
    }
    
    static List<Archetype> getArchetypes(URL file, String repository) {
        List<Archetype> toRet = new ArrayList<Archetype>();
        try {
            if (file == null) {
                return toRet;
            }
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(file);
            Element root = doc.getRootElement();
            Namespace ns = root.getNamespace(); // should be http://maven.apache.org/plugins/maven-archetype-plugin/archetype-catalog/1.0.0 but missing in older copies
            Element list = root.getChild(EL_ARCHETYPES, ns);
            if (list != null) {
                for (Element el : NbCollections.checkedListByCopy(list.getChildren(EL_ARCHETYPE, ns), Element.class, true)) {
                    String grId = el.getChildText(EL_GROUPID, ns);
                    String artId = el.getChildText(EL_ARTIFACTID, ns);
                    String ver = el.getChildText(EL_VERSION, ns);
                    String repo = el.getChildText(EL_REPOSITORY, ns);
                    String desc = el.getChildText(EL_DESCRIPTION, ns);
                    Archetype archetype = new Archetype(false);
                    if (grId != null && artId != null && ver != null) {
                        archetype.setArtifactId(artId);
                        archetype.setGroupId(grId);
                        archetype.setVersion(ver);
                        if (repo != null) {
                            archetype.setRepository(repo);
                        } else {
                            archetype.setRepository(repository);
                        }
                        if (desc != null) {
                            archetype.setDescription(desc);
                        }
                        toRet.add(archetype);
                    }
                }
            }
        } catch (IOException exc) {
            LOG.log(Level.INFO, null, exc);
        } catch (JDOMException exc) {
            LOG.log(Level.INFO, null, exc);
        }
        return toRet;
    }

    @ServiceProvider(service=ArchetypeProvider.class, position=100)
    public static class LocalCatalogRepoProvider extends CatalogRepoProvider {

        @Override protected URL file() throws IOException {
            File f = new File(RepositorySystem.userMavenConfigurationHome, "archetype-catalog.xml"); //NOI18N
            return f.isFile() ? Utilities.toURI(f).toURL() : null;
        }

        @Override protected String repository() {
            return RepositorySystem.DEFAULT_LOCAL_REPO_ID;
        }

    }

    // one day period for updating downloaded/cached archetype catalogs
    private static final long ARCHETYPE_TIMEOUT = 1000 * 60 * 60 * 24; // * 7;
    
    @ServiceProvider(service=ArchetypeProvider.class, position=50) //has to come before the localCatalogProvider one..
    public static class RemoteCatalogProvider implements ArchetypeProvider {

        @Override
        public List<Archetype> getArchetypes() {
            File root = Places.getCacheSubdirectory("mavenarchetypes"); //NOI18N
            ArrayList<Archetype> toRet = new ArrayList<Archetype>();
            MavenEmbedder embedder = EmbedderFactory.getOnlineEmbedder();
            SettingsDecryptionResult settings = embedder.lookupComponent(SettingsDecrypter.class).decrypt(new DefaultSettingsDecryptionRequest(embedder.getSettings()));
            
            for (RepositoryInfo info : RepositoryPreferences.getInstance().getRepositoryInfos()) {
                if (info.isRemoteDownloadable()) {
                    File catalog = new File(new File( root, info.getId()), "archetype-catalog.xml"); //NOI18N
                    boolean download = false;
                    if (!catalog.exists()) {
                        download = true;
                    } else {
                        long lastM = catalog.lastModified();
                        if (lastM == 0) {
                            download = true;
                        } else if (lastM - System.currentTimeMillis() > ARCHETYPE_TIMEOUT) {
                            download = true;
                        }
                    }
                    
                    if (download) {
                        download(info.getId(), info.getRepositoryUrl(), catalog, settings, embedder);
                    }
                    
                    if (catalog.exists()) {
                        try {
                            toRet.addAll(CatalogRepoProvider.getArchetypes(Utilities.toURI(catalog).toURL(), info.getRepositoryUrl()));
                        } catch (MalformedURLException ex) {
                            LOG.log(Level.INFO, null, ex);
                        }
                    }
                }
            }
            
            return toRet;
        }

        private void download(String id, String url, File catalog, SettingsDecryptionResult settings, MavenEmbedder embedder) {
            String baseurl = url;
            String repoId = id;
            try {
                catalog.getParentFile().mkdirs();
                AuthenticationInfo wagonAuth = null;
//        //this comes from maven-compat
                MirrorSelector mrri = embedder.lookupComponent(MirrorSelector.class);

                // handle mirroring
                ArtifactRepositoryLayout layout = (ArtifactRepositoryLayout) embedder.getPlexus().lookup(ArtifactRepositoryLayout.ROLE, "default"); //NOI18N
                ArtifactRepositoryFactory arf = embedder.lookupComponent(ArtifactRepositoryFactory.class);

                //what is the update policy?
                ArtifactRepositoryPolicy policy = new ArtifactRepositoryPolicy(true, ArtifactRepositoryPolicy.UPDATE_POLICY_DAILY, ArtifactRepositoryPolicy.UPDATE_POLICY_DAILY);
                ArtifactRepository art = arf.createArtifactRepository(repoId, baseurl, layout, policy, policy);
                Mirror mirr = mrri.getMirror(art, embedder.getSettings().getMirrors());
                if (mirr != null) {
                    baseurl = mirr.getUrl();
                    repoId = mirr.getId();
                }

                for (Server server : settings.getServers()) {
                    if (repoId.equals(server.getId())) {
                        wagonAuth = new AuthenticationInfo();
                        wagonAuth.setUserName(server.getUsername());
                        wagonAuth.setPassword(server.getPassword());
                        wagonAuth.setPassphrase(server.getPassphrase());
                        wagonAuth.setPrivateKey(server.getPrivateKey());
                        break;
                    }
                }
                String protocol = URI.create(baseurl).getScheme();
                ProxyInfo wagonProxy = null;
                for (Proxy proxy : settings.getProxies()) {
                    if (proxy.isActive()) {
                        wagonProxy = new ProxyInfo();
                        wagonProxy.setHost(proxy.getHost());
                        wagonProxy.setPort(proxy.getPort());
                        wagonProxy.setNonProxyHosts(proxy.getNonProxyHosts());
                        wagonProxy.setUserName(proxy.getUsername());
                        wagonProxy.setPassword(proxy.getPassword());
                        wagonProxy.setType(protocol);
                        break;
                    }
                }
                Wagon wagon = embedder.getPlexus().lookup(Wagon.class, protocol);
                assert wagon != null;

                Repository repository = new Repository(repoId, baseurl);

                try {

                    // when working in the context of Maven, the WagonManager is already
                    // populated with proxy information from the Maven environment

                    if (wagonAuth != null) {
                        if (wagonProxy != null) {
                            wagon.connect(repository, wagonAuth, wagonProxy);
                        } else {
                            wagon.connect(repository, wagonAuth);
                        }
                    } else {
                        if (wagonProxy != null) {
                            wagon.connect(repository, wagonProxy);
                        } else {
                            wagon.connect(repository);
                        }
                    }

                    File temp = Files.createTempFile("maven", "catalog").toFile(); //NOI18N
                    try {
                        wagon.get("archetype-catalog.xml", temp); //NOI18N
                        //only overwrite the old file or create file if the content is there.
                        if (temp.exists() && temp.length() > 0) {
                            FileUtils.copyFile(temp, catalog);
                            temp.delete();
                        }
                    } finally {
                        wagon.disconnect();
                    }

                } catch (AuthenticationException ex) {
                    String msg = "Authentication exception connecting to " + repository;  //NOI18N
                    throw new IOException(msg, ex);
                } catch (WagonException ex) {
                    String msg = "Wagon exception connecting to " + repository;  //NOI18N
                    throw new IOException(msg, ex);
                } finally {
                    try {
                        wagon.disconnect();
                    } catch (ConnectionException ex) {
                        String msg = "Wagon exception disconnecting from " + repository; //NOI18N
                        throw new IOException(msg, ex);
                    }
                }
            } catch (ComponentLookupException ex) {
                LOG.log(Level.FINE, null, ex);
            } catch (IOException io) {
                LOG.log(Level.INFO, null, io);
            }

        }
    }
}
