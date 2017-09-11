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

import org.netbeans.modules.autoupdate.updateprovider.UpdateItemImpl;
import org.netbeans.modules.autoupdate.services.*;
import java.net.URL;
import java.util.Locale;

/**
 *
 * @author Jiri Rechtacek
 */
public class LocalizationItem extends UpdateItemImpl {
    
    private String codeName;
    private String specificationVersion;
    
    private Locale locale;
    private String branding;
    private String moduleSpecificationVersion;
    private String localizedName;
    private String localizedDescription;
    private URL distribution;
    private String category;

    private UpdateItemDeploymentImpl deployImpl;
    private UpdateLicenseImpl licenseImpl;

    public LocalizationItem (
            String codeName,
            String specificationVersion,
            URL distribution,
            Locale locale,
            String branding,
            String moduleSpecificationVersion,
            String localizedName,
            String localizedDescription,
            String category,
            Boolean needsRestart,
            Boolean isGlobal,
            String targetCluster,
            UpdateLicenseImpl licenseImpl) {
        this.codeName = codeName;
        this.specificationVersion = specificationVersion;
        this.distribution = distribution;
        this.locale = locale;
        this.branding = branding;
        this.moduleSpecificationVersion = moduleSpecificationVersion;
        this.localizedName = localizedName;
        this.localizedDescription = localizedDescription;
        this.deployImpl = new UpdateItemDeploymentImpl (needsRestart, isGlobal, targetCluster, null, null);
        this.licenseImpl = licenseImpl;
        this.category = category;
    }
    
    public String getCodeName () {
        return this.codeName;
    }
    
    public String getSpecificationVersion () {
        return this.specificationVersion;
    }
    
    public URL getDistribution () {
        return this.distribution;
    }
    
    public Locale getLocale () {
        return this.locale;
    }
    
    public String getBranding () {
        return this.branding;
    }
    
    public String getMasterModuleSpecificationVersion () {
        return this.moduleSpecificationVersion;
    }
    
    public String getLocalizedModuleName () {
        return this.localizedName;
    }
    
    public String getLocalizedModuleDescription () {
        return this.localizedDescription;
    }
    
    public UpdateItemDeploymentImpl getUpdateItemDeploymentImpl () {
        return this.deployImpl;
    }
    
    public UpdateLicenseImpl getUpdateLicenseImpl () {
        return this.licenseImpl;
    }

    public String getAgreement() {
        return getUpdateLicenseImpl ().getAgreement();
    }

    public String getCategory () {
        return category;
    }

    @Override
    public void setUpdateLicenseImpl (UpdateLicenseImpl licenseImpl) {
        this.licenseImpl = licenseImpl;
    }

    @Override
    public void setNeedsRestart(Boolean needsRestart) {
        deployImpl.needsRestart();
    }
}
