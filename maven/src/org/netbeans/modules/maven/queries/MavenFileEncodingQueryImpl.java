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
