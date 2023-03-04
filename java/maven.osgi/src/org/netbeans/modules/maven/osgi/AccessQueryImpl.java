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

package org.netbeans.modules.maven.osgi;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.osgi.util.PackageDefinitionUtil;
import org.netbeans.spi.java.queries.AccessibilityQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mkleint
 */
public class AccessQueryImpl implements AccessibilityQueryImplementation {

    private final Project prj;
    private WeakReference<List<Pattern>> ref;
    private static final String DEFAULT_IMP = "*";
    
    public AccessQueryImpl(Project prj) {
        this.prj = prj;
    }
    
    /**
     *
     * @param pkg
     * @return
     */
    @Override
    public Boolean isPubliclyAccessible(FileObject pkg) {
        FileObject srcdir = org.netbeans.modules.maven.api.FileUtilities.convertStringToFileObject(prj.getLookup().lookup(NbMavenProject.class).getMavenProject().getBuild().getSourceDirectory());
        if (srcdir != null) {
            String path = FileUtil.getRelativePath(srcdir, pkg);
            if (path != null) {
                String name = path.replace('/', '.');
                return check(name);
            }
        }
        
        return null;
    }
    
    private Boolean check(String value) {
        String[] exps = PluginPropertyUtils.getPluginPropertyList(prj,
                OSGiConstants.GROUPID_FELIX, OSGiConstants.ARTIFACTID_BUNDLE_PLUGIN,
                OSGiConstants.PARAM_INSTRUCTIONS, OSGiConstants.EXPORT_PACKAGE,
                OSGiConstants.GOAL_MANIFEST);
        String[] imps = PluginPropertyUtils.getPluginPropertyList(prj,
                OSGiConstants.GROUPID_FELIX, OSGiConstants.ARTIFACTID_BUNDLE_PLUGIN,
                OSGiConstants.PARAM_INSTRUCTIONS, OSGiConstants.PRIVATE_PACKAGE,
                OSGiConstants.GOAL_MANIFEST);
        String exp = null;
        if (exps != null && exps.length == 1) {
            exp = exps[0];
        }
        String imp = null;
        if (imps != null && imps.length == 1) {
            imp = imps[0];
        }
        if (exp != null) {
            if (testPackagePatterns(exp, value)) {
                return Boolean.TRUE;
            }
        }
        Boolean result = null;
        if (testPackagePatterns(imp != null ? imp : DEFAULT_IMP, value)) {
            result = Boolean.FALSE;
        }
        if (exp == null) {
            //handle default behaviour if not defined..
            //TODO handle 1.x bundle plugin defaults..
            if (!value.contains(".impl") && !value.contains(".internal")) { //NOI18N
                result = Boolean.TRUE;
            }
        }
        return result;
    }
    
	static boolean testPackagePatterns(String patterns, String value) {
		boolean matches = false;
        if (patterns != null) {
			patterns = PackageDefinitionUtil.omitDirectives(patterns);
            StringTokenizer tok = new StringTokenizer(patterns, " ,", false); //NOI18N
            while (tok.hasMoreTokens() && !matches) {
                String token = tok.nextToken();
                token = token.trim();
				if ("*".equals(token)) { //NOI18N
					return true;
				}
					
                boolean recursive = false;
				boolean exclusivePattern = false;
				if (token.startsWith("!")) {
					token = token.substring(1);
					exclusivePattern = true;
				}
				if (token.endsWith("*")) { //NOI18N
					// The following cases are tested with maven-bundle-plugin
					// a.* or a* -> recursive
					// a. -> non-recursive
					token = token.substring(0, token.length() - "*".length()); //NOI18N
					recursive = true;
					if (token.endsWith(".")) {
						// Removes the last dot also
						token = token.substring(0, token.length() - 1);
					}
                }
				matches = recursive ? value.startsWith(token) : value.equals(token);
				if (matches && exclusivePattern) {
					// only excluding when it matches
					matches = !matches;
				}
            }
        }
		return matches;
	}

}
