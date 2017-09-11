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
