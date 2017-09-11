/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
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

import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.Module;
import org.netbeans.modules.autoupdate.services.*;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Jiri Rechtacek
 */
public class ModuleItem extends UpdateItemImpl {
    
    private String codeName;
    private String specificationVersion;
    private ModuleInfo info;
    private String author;
    private String downloadSize;
    private String homepage;
    private String category;
    private Date publishDate;
    private boolean isEager;
    private boolean isAutoload;
    private boolean isPreferedUpdate;
    private String moduleNotification = null;
    
    private URL distribution;
    private Manifest manifest;

    private UpdateItemDeploymentImpl deployImpl;
    private UpdateLicenseImpl licenseImpl;
    
    protected ModuleItem () {}

    public ModuleItem (
            String codeName,
            String specificationVersion,
            URL distribution,
            String author,
            String publishDateString,
            String downloadSize,
            String homepage,
            String category,
            Manifest manifest, 
            Boolean isEager,
            Boolean isAutoload,
            Boolean needsRestart,
            Boolean isGlobal,
            Boolean isPrefered,
            String targetCluster,
            UpdateLicenseImpl licenseImpl) {
        this.codeName = codeName;
        this.specificationVersion = specificationVersion;
        this.distribution = distribution;
        this.manifest = manifest;
        this.deployImpl = new UpdateItemDeploymentImpl (needsRestart, isGlobal, targetCluster, null, null);
        if (publishDateString != null && publishDateString.length () > 0) {
            try {
                this.publishDate = Utilities.parseDate(publishDateString);
            } catch (ParseException pe) {
                Logger.getLogger (ModuleItem.class.getName ()).log (Level.INFO, "Parsing \"" + publishDateString + "\" of " + codeName + " throws " + pe.getMessage (), pe);
            } catch (RuntimeException re) {
                Logger.getLogger (ModuleItem.class.getName ()).log (Level.INFO, "Parsing \"" + publishDateString + "\" of " + codeName + " throws " + re.getMessage (), re);
            }
        }
        this.licenseImpl = licenseImpl;
        this.author = author;
        this.downloadSize = downloadSize;
        this.homepage = homepage;
        this.category = category;
        this.isEager = isEager;
        this.isAutoload = isAutoload;
        this.isPreferedUpdate = isPrefered;
    }
    
    @Override
    public String getCodeName () {
        return codeName;
    }
    
    public String getSpecificationVersion () {
        return specificationVersion;
    }
    
    public URL getDistribution () {
        return this.distribution;
    }
    
    public String getAuthor () {
        return author;
    }
    
    public String getHomepage () {
        return homepage;
    }
    
    public int getDownloadSize () {
        int parseInt = 0;
        if (downloadSize == null || downloadSize.length() == 0) {
            return parseInt;
        }
        try {
            parseInt = Integer.parseInt (downloadSize);
        } catch (NumberFormatException ex) {
            Logger.getLogger(ModuleItem.class.getName()).log(Level.WARNING, "Module {0} has invalid value of downloadSize: {1}",
                    new Object[]{this.codeName, downloadSize});
        }
        return parseInt;
    }
    
    public UpdateItemDeploymentImpl getUpdateItemDeploymentImpl () {
        return this.deployImpl;
    }
    
    @Override
    public UpdateLicenseImpl getUpdateLicenseImpl () {
        return this.licenseImpl;
    }
    
    public ModuleInfo getModuleInfo () {        
        if (info == null) {
            Module m = Utilities.toModule (codeName, specificationVersion == null ? null : new SpecificationVersion (specificationVersion));
            info = (m != null) ? m : new DummyModuleInfo (manifest.getMainAttributes ());
        }
        return info;
    }
    
    public String getAgreement() {
        return getUpdateLicenseImpl ().getAgreement();
    }

    @Override
    public String getCategory () {
        return category;
    }
    
    public String getDate () {
        return publishDate == null ? null : Utilities.formatDate(publishDate);
    }
    
    public boolean isAutoload () {
        return isAutoload;
    }

    public boolean isEager () {
        return isEager;
    }
    
    public boolean isPreferredUpdate() {
        return isPreferedUpdate;
    }
    
    public String getModuleNotification () {
        return moduleNotification;
    }
    
    void setModuleNotification (String notification) {
        this.moduleNotification = notification;
    }

    @Override
    public void setUpdateLicenseImpl (UpdateLicenseImpl licenseImpl) {
        this.licenseImpl = licenseImpl;
    }
        
    @Override
    public void setNeedsRestart(Boolean needsRestart) {
        deployImpl.setNeedsRestart(needsRestart);
    }
}
