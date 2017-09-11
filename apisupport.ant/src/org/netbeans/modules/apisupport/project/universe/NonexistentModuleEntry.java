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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.apisupport.project.api.ManifestManager.PackageExport;

/**
 * A placeholder for a module which cannot be found.
 */
public class NonexistentModuleEntry implements ModuleEntry {

    private final String cnb;

    public NonexistentModuleEntry(String cnb) {
        this.cnb = cnb;
    }

    public @Override String getNetBeansOrgPath() {return null;}

    public @Override File getSourceLocation() {return null;}

    public @Override String getCodeNameBase() {return cnb;}
    
    public @Override File getClusterDirectory() {return new File(".");}
    
    public @Override File getJarLocation() {return new File(cnb.replace('.', '-') + ".jar");}
    
    public @Override String getClassPathExtensions() {return "";}
    
    public @Override String getReleaseVersion() {return null;}
    
    public @Override String getSpecificationVersion() {return null;}
    
    public @Override String[] getProvidedTokens() {return new String[0];}
    
    public @Override String getLocalizedName() {return cnb;}
    
    public @Override String getCategory() {return null;}
    
    public @Override String getLongDescription() {return null;}
    
    public @Override String getShortDescription() {return null;}
    
    public @Override PackageExport[] getPublicPackages() {return new PackageExport[0];}
    
    public @Override URL getJavadoc(NbPlatform platform) {return null;}
    
    public @Override Set<String> getAllPackageNames() {return Collections.emptySet();}
    
    public @Override boolean isDeclaredAsFriend(String cnb) {return false;}
    
    public @Override Set<String> getPublicClassNames() {return Collections.emptySet();}
    
    public @Override boolean isDeprecated() {return false;}
    
    public @Override String[] getRunDependencies() {return new String[0];}
    
    public @Override int compareTo(ModuleEntry o) {return cnb.compareTo(o.getCodeNameBase());}

}
