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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.autoupdate.updateprovider;

import org.netbeans.modules.autoupdate.services.*;
import java.util.Set;
import org.netbeans.spi.autoupdate.CustomInstaller;
import org.netbeans.spi.autoupdate.CustomUninstaller;

/**
 *
 * @author Jiri Rechtacek
 */
public class NativeComponentItem extends UpdateItemImpl {
    
    private boolean isInstalled;
    private String codeName;
    private String specificationVersion;
    private Set<String> dependencies;
    private String displayName;
    private String description;
    private String downloadSize;

    private UpdateItemDeploymentImpl deployImpl;
    private UpdateLicenseImpl licenseImpl;
    
    public NativeComponentItem (
            boolean isInstalled,
            String codeName,
            String specificationVersion,
            String downloadSize,
            Set<String> dependencies,
            String displayName,
            String description,
            Boolean needsRestart,
            Boolean isGlobal,
            String targetCluster,
            CustomInstaller installer,
            CustomUninstaller uninstaller,
            UpdateLicenseImpl license) {
        this.isInstalled = isInstalled;
        this.codeName = codeName;
        this.specificationVersion = specificationVersion;
        this.dependencies = dependencies;
        this.displayName = displayName;
        this.description = description;
        this.licenseImpl = license;
        this.downloadSize = downloadSize;
        this.deployImpl = new UpdateItemDeploymentImpl (needsRestart, isGlobal, targetCluster, installer, uninstaller);
    }
    
    public String getCodeName () {
        return this.codeName;
    }
    
    public String getSpecificationVersion () {
        return this.specificationVersion;
    }
    
    public String getDisplayName () {
        return this.displayName;
    }
    
    public String getDescription () {
        return this.description;
    }
    
    public Set<String> getDependenciesToModules () {
        return this.dependencies;
    }
    
    public int getDownloadSize () {
        return isInstalled ? 0 : Integer.parseInt (downloadSize);
    }
    
    public UpdateItemDeploymentImpl getUpdateItemDeploymentImpl () {
        return this.deployImpl;
    }
    
    public UpdateLicenseImpl getUpdateLicenseImpl () {
        assert false : "Invalid call getUpdateLicenseImpl() on NativeComponentItem.";
        return this.licenseImpl;
    }
    
    public String getAgreement () {
        return null;
        //return licenseImpl.getAgreement ();
    }

    public String getCategory () {
        throw new UnsupportedOperationException ("Not supported yet.");
    }

    @Override
    public void setUpdateLicenseImpl (UpdateLicenseImpl licenseImpl) {
        this.licenseImpl = licenseImpl;
    }

    public boolean isInstalled () {
        return isInstalled;
    }
    
    @Override
    public void setNeedsRestart(Boolean needsRestart) {
        deployImpl.setNeedsRestart(needsRestart);
    }
}
