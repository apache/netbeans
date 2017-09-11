/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.types.selectors.SelectorUtils;

/**
 * Packs override resources into branding JARs with the correct paths.
 * @author Jesse Glick
 */
public final class Branding extends Task {

    private File cluster;
    private File overrides;
    private String token;
    private String locales;
    
    public Branding() {}
    
    public void setCluster(File cluster) {
        this.cluster = cluster;
    }
    
    public void setOverrides(File overrides) {
        this.overrides = overrides;
    }
    
    public void setToken(String token) {
        this.token = token;
    }

    public void setLocales(String locales) {
        this.locales = locales;
    }
    
    public void execute() throws BuildException {
        if (cluster == null || !cluster.isDirectory()) {
            throw new BuildException("Must specify a valid cluster directory", getLocation());
        }
        if (overrides == null || !overrides.isDirectory()) {
            throw new BuildException("Must specify a valid overrides directory", getLocation());
        }
        if (token == null || !token.matches("[a-z][a-z0-9]*(_[a-z][a-z0-9]*)*")) { // cf. NbBundle.setBranding
            throw new BuildException("Must specify a valid branding token: " + token, getLocation());
        }
        try {
            if(locales != null) {
                StringTokenizer tokenizer = new StringTokenizer(locales, ",");
                while (tokenizer.hasMoreElements()) {
                    lookForBrandingJars(overrides, cluster, overrides.getAbsolutePath() + File.separatorChar, tokenizer.nextToken());
                }
            }
            lookForBrandingJars(overrides, cluster, overrides.getAbsolutePath() + File.separatorChar, null);
        } catch (IOException e) {
            throw new BuildException(e, getLocation());
        }
    }
    
    private boolean excluded(File f, String prefix) { // #68929
        String pathAbs = f.getAbsolutePath();
        if (!pathAbs.startsWith(prefix)) {
            throw new BuildException("Examined file " + f + " should have a path starting with " + prefix, getLocation());
        }
        // Cannot just call matchPath on the pathAbs because a relative pattern will *never* match an absolute path!
        String path = pathAbs.substring(prefix.length());
        for (String exclude : DirectoryScanner.getDefaultExcludes()) {
            if (SelectorUtils.matchPath(exclude.replace('/', File.separatorChar), path)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean lookForBrandingJars(File srcDir, File destDir, String srcPrefix, String locale) throws IOException {
        if (srcDir.getName().endsWith(".jar")) {
            packBrandingJar(srcDir, destDir, locale);
            return true;
        } else {
            String[] kids = srcDir.list();
            if (kids == null) {
                throw new IOException("Could not list children of " + srcDir);
            }
            boolean used = false;
            for (int i = 0; i < kids.length; i++) {
                File kid = new File(srcDir, kids[i]);
                if (excluded(kid, srcPrefix)) {
                    continue;
                }
                if (!kid.isDirectory()) {
                    log("Warning: stray file " + kid + " encountered; ignoring", Project.MSG_WARN);
                    continue;
                }
                used |= lookForBrandingJars(kid, new File(destDir, kids[i]), srcPrefix, locale);
            }
            if (!used) {
                log("Warning: stray directory " + srcDir + " with no brandables encountered; ignoring", Project.MSG_WARN);
            }
            return used;
        }
    }
    
    private void packBrandingJar(File srcDir, File destJarBase, String locale) throws IOException {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(srcDir);
        String localeToken = "";
        if(locale != null) {
            String [] includes = {"**/*_" + locale.toString() + ".*"};
            scanner.setIncludes(includes);
            localeToken = "_" + locale.toString();
        } else {
            String [] excludes = {"**/*_??_??.*", "**/*_??.*"};
            scanner.setExcludes(excludes);
        }
        scanner.addDefaultExcludes(); // #68929
        scanner.scan();
        String[] files = scanner.getIncludedFiles();
        if(files.length > 0) {
            Jar zip = (Jar) getProject().createTask("jar");
            String name = destJarBase.getName();
            String nameBase = name.substring(0, name.length() - ".jar".length());
            File destFolder = new File(destJarBase.getParentFile(), "locale");
            if (!destFolder.isDirectory() && !destFolder.mkdirs()) {
                throw new IOException("Could not create directory " + destFolder);
            }
            File destJar = new File(destFolder, nameBase + "_" + token + localeToken + ".jar");
            zip.setDestFile(destJar);
            zip.setCompress(true);
            for (int i = 0; i < files.length; i++) {
                ZipFileSet entry = new ZipFileSet();
                entry.setFile(new File(srcDir, files[i]));
                String basePath = files[i].replace(File.separatorChar, '/');
                int slash = basePath.lastIndexOf('/');
                int dot = basePath.lastIndexOf('.');
                String infix = "_" + token + localeToken;
                String brandedPath;
                if (dot == -1 || dot < slash) {
                    brandedPath = basePath + infix;
                } else {
                    brandedPath = basePath.substring(0, dot - localeToken.length()) + infix + basePath.substring(dot);
                }
                entry.setFullpath(brandedPath);
                zip.addZipfileset(entry);
            }
            zip.setLocation(getLocation());
            zip.init();
            zip.execute();
        }
    }
}
