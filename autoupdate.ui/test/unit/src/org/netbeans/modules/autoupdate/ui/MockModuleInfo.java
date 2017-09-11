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

package org.netbeans.modules.autoupdate.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Manifest;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateLicense;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;

final class MockModuleInfo extends ModuleInfo {
    private final String cnb;
    private final String spec;
    private final Dependency[] deps;
    private final boolean enabled;

    private MockModuleInfo(String cnb, String spec, boolean enabled, Dependency... deps) {
        this.cnb = cnb;
        this.spec = spec;
        this.deps = deps;
        this.enabled = enabled;
    }
    
    public static MockModuleInfo create(String cnb, String spec, boolean enabled, MockModuleInfo... deps) {
        Dependency[] arr = new Dependency[deps.length];
        int i = 0;
        for (MockModuleInfo mmi : deps) {
            arr[i++] = Dependency.create(
                Dependency.TYPE_MODULE, 
                mmi.getCodeName() + " > " + mmi.getSpecificationVersion()
            ).iterator().next();
        }
        return new MockModuleInfo(cnb, spec, enabled, arr);
    }

    @Override
    public String getCodeNameBase() {
        return cnb;
    }

    @Override
    public int getCodeNameRelease() {
        return -1;
    }

    @Override
    public String getCodeName() {
        return cnb;
    }

    @Override
    public SpecificationVersion getSpecificationVersion() {
        return new SpecificationVersion(spec);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Object getAttribute(String attr) {
        return null;
    }

    @Override
    public Object getLocalizedAttribute(String attr) {
        return null;
    }

    @Override
    public Set<Dependency> getDependencies() {
        return new HashSet<Dependency>(Arrays.asList(deps));
    }

    @Override
    public boolean owns(Class<?> clazz) {
        return false;
    }

    UpdateItem toUpdateItem(String version) throws MalformedURLException {
        Manifest man = new Manifest();
        man.getMainAttributes().putValue("OpenIDE-Module", cnb);
        man.getMainAttributes().putValue("OpenIDE-Module-Specification-Version", version);
        return UpdateItem.createModule(
            getCodeNameBase(), 
            version, 
            new URL("http://unknown.com"), 
            "author", 
            "333", 
            "http://unknown.com", 
            "10/12/2010", 
            "mycategory", 
            man, 
            false, 
            false, 
            false, 
            false, 
            "unknown", 
            UpdateLicense.createUpdateLicense("mine", "use!")
        );
    }

}
