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

package org.netbeans.modules.maven.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
@ProjectServiceProvider(service=FileEncodingQueryImplementation.class, projectType="org-netbeans-modules-maven")
public class MavenFileEncodingQueryImpl extends  FileEncodingQueryImplementation {

    private final Project project;
    private static final String ENCODING_PARAM = "encoding"; //NOI18N
    private final Map<FileObject, Charset> cache = new HashMap<FileObject, Charset>();
    private final AtomicBoolean listenerAttached = new AtomicBoolean(false);
    
    public MavenFileEncodingQueryImpl(Project proj) {
        project = proj;
        
    }

    @Override
    public Charset getEncoding(FileObject file) {
        if (listenerAttached.compareAndSet(false, true)) {
            project.getLookup().lookup(NbMavenProject.class).addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                        synchronized (cache) {
                            cache.clear();
                        }
                    }
                }
            });
        }
        MavenProject mp = project.getLookup().lookup(NbMavenProject.class).getMavenProject();
        synchronized (cache) {
            for (Map.Entry<FileObject, Charset> ent : cache.entrySet()) {
                if (ent.getKey().equals(file) || FileUtil.isParentOf(ent.getKey(), file)) {
                    return ent.getValue();
                }
            }
        }
        try {
            //TODO instead of SD
            FileObject src = FileUtilities.convertStringToFileObject(mp.getBuild().getSourceDirectory());
            if (src != null && (src.equals(file) || FileUtil.isParentOf(src, file))) {
                String compileEnc = PluginPropertyUtils.getPluginProperty(project,
                        Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, ENCODING_PARAM, "compile", Constants.ENCODING_PROP); //NOI18N;
                if (compileEnc != null && compileEnc.indexOf("${") == -1) { //NOI18N - guard against unresolved values.
                    Charset ch = Charset.forName(compileEnc);
                    addToCache(src, ch);
                    return ch;
                }
            }
            FileObject testsrc = FileUtilities.convertStringToFileObject(mp.getBuild().getTestSourceDirectory());
            if (testsrc != null && (testsrc.equals(file) || FileUtil.isParentOf(testsrc, file))) {
                String testcompileEnc = PluginPropertyUtils.getPluginProperty(project,
                        Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, ENCODING_PARAM, "testCompile", Constants.ENCODING_PROP); //NOI18N
                if (testcompileEnc != null && testcompileEnc.indexOf("${") == -1) {//NOI18N - guard against unresolved values.
                    Charset ch = Charset.forName(testcompileEnc);
                    addToCache(testsrc, ch);
                    return ch;
                }
            }
            //possibly more complicated with resources, one can have explicit declarations in the
            // pom plugin configuration.
            NbMavenProjectImpl impl = project.getLookup().lookup(NbMavenProjectImpl.class);
            try {
                FileObject root = isWithin(impl.getResources(false), file);
                if (root != null) {
                    String resourceEnc = PluginPropertyUtils.getPluginProperty(project,
                            Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_RESOURCES, ENCODING_PARAM, "resources", Constants.ENCODING_PROP); //NOI18N
                    if (resourceEnc != null && resourceEnc.indexOf("${") == -1) {//NOI18N - guard against unresolved values.
                        Charset ch = Charset.forName(resourceEnc);
                        addToCache(root, ch);
                        return ch;
                    }
                }

            } catch (MalformedURLException x) {
                Exceptions.printStackTrace(x);
            }

            try {
                FileObject root = isWithin(impl.getResources(true), file);
                if (root != null) {
                    String testresourceEnc = PluginPropertyUtils.getPluginProperty(project,
                            Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_RESOURCES, ENCODING_PARAM, "testResources", Constants.ENCODING_PROP); //NOI18N
                    if (testresourceEnc != null && testresourceEnc.indexOf("${") == -1) {//NOI18N - guard against unresolved values.
                        Charset ch = Charset.forName(testresourceEnc);
                        addToCache(root, ch);
                        return ch;
                    }
                }
            } catch (MalformedURLException malformedURLException) {
                Exceptions.printStackTrace(malformedURLException);
            }

            try {
                FileObject root = isWithin(new URI[]{impl.getSiteDirectory()}, file);
                if (root != null) {
                    String siteEnc = PluginPropertyUtils.getPluginProperty(project,
                            Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SITE, "inputEncoding", "site", Constants.ENCODING_PROP); //NOI18N
                    if (siteEnc != null && siteEnc.indexOf("${") == -1) {//NOI18N - guard against unresolved values.
                        Charset ch = Charset.forName(siteEnc);
                        addToCache(root, ch);
                        return ch;
                    }
                }
            } catch (MalformedURLException malformedURLException) {
                Exceptions.printStackTrace(malformedURLException);
            }
            String defEnc = mp.getProperties().getProperty(Constants.ENCODING_PROP);
            if (defEnc != null) {
                return Charset.forName(defEnc);
            }
        } catch (UnsupportedCharsetException uce) {
            Logger.getLogger(MavenFileEncodingQueryImpl.class.getName()).log(Level.FINE, uce.getMessage(), uce);
        } catch (IllegalCharsetNameException icne) {
            Logger.getLogger(MavenFileEncodingQueryImpl.class.getName()).log(Level.FINE, icne.getMessage(), icne);
        }
        return Charset.defaultCharset();
    }
    
    private FileObject isWithin(URI[] res, FileObject file) throws MalformedURLException {
        for (URI ur : res) {
            FileObject fo = URLMapper.findFileObject(ur.toURL());
            if (fo != null && (fo.equals(file) || FileUtil.isParentOf(fo, file))) {
                return fo;
            } 
        }
        return null;
        
    }

    private void addToCache(FileObject src, Charset ch) {
        synchronized (cache) {
            cache.put(src, ch);
        }
    }
    

}
