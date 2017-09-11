/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.apisupport.project.suite;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.apisupport.project.spi.BrandingSupport;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.PropertyUtils;

class SuiteBrandingSupport extends BrandingSupport {

    private final SuiteProject project;
    private NbPlatform platform;

    SuiteBrandingSupport(SuiteProject project, String brandingPath, Locale locale) throws IOException {
        super(project, brandingPath);
        this.project = project;
        this.locale = locale;
    }

    private NbPlatform getActivePlatform() {
        return project.getPlatform(true);
    }

    @Override protected BrandableModule findBrandableModule(String moduleCodeNameBase) {
        NbPlatform p = getActivePlatform();
        if (p == null) {
            return null;
        }
        ModuleEntry me = p.getModule(moduleCodeNameBase);
        return me != null ? new ModuleEntryWrapper(me) : null;
    }

    @Override public Set<File> getBrandableJars() {
        NbPlatform platf = getActivePlatform();
        Set<ModuleEntry> modules = platf.getModules();
        Set<File> jars = new HashSet<File>(modules.size());
        for (ModuleEntry m : modules) {
            File j = m.getJarLocation();
            if (j != null) {
                jars.add(j);
            }
        }
        return jars;
    }

    @Override protected Set<BrandableModule> loadModules() {
        NbPlatform newPlatform = getActivePlatform();
        if (newPlatform == null) {
            return null;
        }
        if (newPlatform.equals(platform)) {
            return null;
        }
        platform = newPlatform;
        Set<BrandableModule> r = new HashSet<BrandableModule>();
        for (ModuleEntry me : platform.getModules()) {
            r.add(new ModuleEntryWrapper(me));
        }
        return r;
    }

    @Override protected Map<String,String> localizingBundle(BrandableModule wrapper) {
        ModuleEntry mEntry = ((ModuleEntryWrapper) wrapper).mEntry;
        return ModuleList.loadBundleInfo(mEntry.getSourceLocation()).toEditableProperties();
    }

    private static class ModuleEntryWrapper implements BrandableModule {

        private final ModuleEntry mEntry;

        ModuleEntryWrapper(ModuleEntry mEntry) {
            this.mEntry = mEntry;
        }
        
        @Override public String getCodeNameBase() {
            return mEntry.getCodeNameBase();
        }

        @Override public File getJarLocation() {
            return mEntry.getJarLocation();
        }

        @Override public String getRelativePath() {
            return PropertyUtils.relativizeFile(mEntry.getClusterDirectory(), mEntry.getJarLocation());
        }

        @Override public boolean equals(Object obj) {
            if (!(obj instanceof ModuleEntryWrapper)) {
                return false;
            }
            return mEntry == ((ModuleEntryWrapper) obj).mEntry;
        }

        @Override public int hashCode() {
            return mEntry.hashCode();
        }

        @Override public String toString() {
            return mEntry.toString();
        }

    }

}
