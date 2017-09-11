/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.autoupdate.services;

import java.util.Collections;
import java.util.List;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.modules.autoupdate.updateprovider.InstallInfo;
import org.netbeans.modules.autoupdate.updateprovider.LocalizationItem;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Jiri Rechtacek
 */
public class LocalizationUpdateElementImpl extends UpdateElementImpl {
    private String codeName;
    private String displayName;
    private SpecificationVersion specVersion;
    private String description;
    private String source;
    private String author;
    private String homepage;
    private int downloadSize;
    private String category;
    private InstallInfo installInfo;
    private LocalizationItem localizationItem;
    
    public LocalizationUpdateElementImpl (LocalizationItem item, String providerName) {
        super (item, providerName);
        codeName = item.getCodeName ();
        specVersion = item.getSpecificationVersion () == null ? null : new SpecificationVersion (item.getSpecificationVersion ());
        source = providerName;
        installInfo = new InstallInfo (item);
        displayName = item.getLocalizedModuleName ();
        description = item.getLocalizedModuleDescription ();
        category = item.getCategory ();
        this.localizationItem = item;
    }
    
    @Override
    public String getCodeName () {
        return codeName;
    }
    
    @Override
    public String getDisplayName () {
        return displayName;
    }
    
    @Override
    public SpecificationVersion getSpecificationVersion () {
        return specVersion;
    }
    
    @Override
    public String getDescription () {
        return description;
    }
    
    @Override
    public String getNotification() {
        return null;
    }
    
    @Override
    public String getAuthor () {
        return author;
    }
    
    @Override
    public String getHomepage () {
        return homepage;
    }
    
    @Override
    public int getDownloadSize () {
        return downloadSize;
    }
    
    @Override
    public String getSource () {
        return source;
    }
    
    @Override
    public String getCategory () {
        if (category == null) {
            category = UpdateUnitFactory.UNSORTED_CATEGORY;
        }
        return category;
    }
    
    @Override
    public String getDate () {
        return null;
    }
    
    @Override
    public String getLicenseId() {
        return localizationItem.getUpdateLicenseImpl().getName();
    }
    
    @Override
    public String getLicence () {
        return localizationItem.getAgreement ();
    }

    @Override
    public InstallInfo getInstallInfo () {
        return installInfo;
    }
    
    @Override
    public List<ModuleInfo> getModuleInfos () {
        // XXX: localization vs. modules
        return Collections.emptyList ();
    }
    
    @Override
    public UpdateManager.TYPE getType () {
        return UpdateManager.TYPE.LOCALIZATION;
    }

    @Override
    public boolean isEnabled () {
        // XXX: how to detect if localization is enabled?
        return false;
    }            
    
    @Override
    public boolean isAutoload () {
        return false;
    }

    @Override
    public boolean isEager () {
        return false;
    }
    
    @Override
    public boolean isFixed () {
        return false;
    }
    
    @Override
    public boolean isPreferredUpdate() {
        return false;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final LocalizationUpdateElementImpl other = (LocalizationUpdateElementImpl) obj;

        if (this.specVersion != other.specVersion &&
            (this.specVersion == null ||
             !this.specVersion.equals(other.specVersion)))
            return false;
        if (this.codeName != other.codeName &&
            (this.codeName == null || !this.codeName.equals(other.codeName)))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;

        hash = 61 * hash + (this.codeName != null ? this.codeName.hashCode()
                                                  : 0);
        hash = 61 * hash +
               (this.specVersion != null ? this.specVersion.hashCode()
                                         : 0);
        return hash;
    }

    
}
