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

package org.netbeans.modules.maven.apisupport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.SuppressWarnings;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.AccessibilityQueryImplementation;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
@ProjectServiceProvider(service=AccessibilityQueryImplementation.class, projectType="org-netbeans-modules-maven/" + NbMavenProject.TYPE_NBM)
public class AccessQueryImpl implements AccessibilityQueryImplementation {
    private final Project project;
    private WeakReference<List<Pattern>> ref;
    private PropertyChangeListener projectListener;
    
    private static final String MANIFEST_PATH = "src/main/nbm/manifest.mf"; //NOI18N
    static final String ATTR_PUBLIC_PACKAGE = "OpenIDE-Module-Public-Packages"; //NOI18N
    
    public AccessQueryImpl(Project prj) {
        project = prj;
    }
    
    /**
     *
     * @param pkg
     * @return
     */
    @SuppressWarnings("NP_BOOLEAN_RETURN_NULL")
    @Override public Boolean isPubliclyAccessible(FileObject pkg) {
        FileObject srcdir = org.netbeans.modules.maven.api.FileUtilities.convertStringToFileObject(project.getLookup().lookup(NbMavenProject.class).getMavenProject().getBuild().getSourceDirectory());
        if (srcdir != null) {
            String path = FileUtil.getRelativePath(srcdir, pkg);
            if (path != null) {
                String name = path.replace('/', '.');
                //TODO cache somehow..
                List<Pattern> pp = getPublicPackagesPatterns();
                return check(pp, name);
            }
        }
        return null;
    }
    
    static boolean check(List<Pattern> patt, String value) {
        boolean matches = false;
        for (Pattern pattern : patt) {
            matches = pattern.matcher(value).matches();
            if (matches) {
                break;
            }
        }
        return matches;
    }
    
    
    synchronized List<Pattern> getPublicPackagesPatterns() {
        if (projectListener == null) {
            projectListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                        synchronized (AccessQueryImpl.this) {
                            ref = null;
                        }
                    }
                }
            };
            project.getLookup().lookup(NbMavenProject.class).addPropertyChangeListener(projectListener);
        }
        if (ref != null) {
            List<Pattern> patterns = ref.get();
            if (patterns != null) {
                return patterns;
            }
        }
        List<Pattern> toRet = loadPublicPackagesPatterns(project);
        ref = new WeakReference<List<Pattern>>(toRet);
        return toRet;
    }
    
    /** as defined in nbm-maven-plugin **/
    static List<Pattern> prepareMavenPublicPackagesPatterns(String[] values) {
        List<Pattern> toRet = new ArrayList<Pattern>();
        for (String token : values) {
                token = token.trim();
                boolean recursive = false;
                if (token.endsWith(".*")) { //NOI18N
                    token = token.substring(0, token.length() - ".*".length()); //NOI18N
                    recursive = true;
                }
                token = token.replace(".","\\."); //NOI18N
                if (recursive) {
                    token = token + ".*"; //NOI18N
                }
                toRet.add(Pattern.compile(token));
            }
        return toRet;
    }
    
    /** as defined in module manifest */
    static List<Pattern> prepareManifestPublicPackagesPatterns(String value) {
        List<Pattern> toRet = new ArrayList<Pattern>();
        if (value != null) {
            StringTokenizer tok = new StringTokenizer(value, " ,", false); //NOI18N
            while (tok.hasMoreTokens()) {
                String token = tok.nextToken();
                token = token.trim();
                boolean recursive = false;
                if (token.endsWith(".*")) { //NOI18N
                    token = token.substring(0, token.length() - ".*".length()); //NOI18N
                    recursive = false;
                } else if (token.endsWith(".**")) { //NOI18N
                    token = token.substring(0, token.length() - ".**".length()); //NOI18N
                    recursive = true;
                }
                token = token.replace(".","\\."); //NOI18N
                if (recursive) {
                    token = token + ".*"; //NOI18N
                }
                toRet.add(Pattern.compile(token));
            }
        }
        return toRet;
    }

    private static List<Pattern> loadPublicPackagesPatterns(Project project) {
        List<Pattern> toRet = new ArrayList<Pattern>();
        String[] params = PluginBackwardPropertyUtils.getPluginPropertyList(project,
                "publicPackages", "publicPackage", "manifest"); //NOI18N
        if (params != null) {
            toRet = prepareMavenPublicPackagesPatterns(params);
        } else {
            FileObject obj = project.getProjectDirectory().getFileObject(MANIFEST_PATH);
            if (obj != null) {
                try (InputStream in = obj.getInputStream()) {
                    Manifest man = new Manifest();
                    man.read(in);
                    String value = man.getMainAttributes().getValue(ATTR_PUBLIC_PACKAGE);
                    toRet = prepareManifestPublicPackagesPatterns(value);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return toRet;
    }
    
}
