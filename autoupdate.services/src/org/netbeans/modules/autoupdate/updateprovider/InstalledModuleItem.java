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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

import org.netbeans.Module;
import org.netbeans.modules.autoupdate.services.UpdateItemDeploymentImpl;
import org.netbeans.modules.autoupdate.services.Utilities;
import org.openide.modules.ModuleInfo;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstalledModuleItem extends ModuleItem {
    
    private String codeName;
    private String specificationVersion;
    private ModuleInfo info;
    private String author;
    private String source;
    private String installCluster;
    private String installDate;
    
    public InstalledModuleItem (
            String codeName,
            String specificationVersion,
            ModuleInfo info,
            String author,
            String installCluster,
            String installTime) {
        this.codeName = codeName;
        this.specificationVersion = specificationVersion;
        this.info = info;
        this.author = author;
        this.installCluster = installCluster;
        this.installDate = installTime;
    }
    
    @Override
    public final String getCodeName () {
        return codeName;
    }
    
    @Override
    public final String getSpecificationVersion () {
        return specificationVersion;
    }
    
    public String getSource () {
        if (source == null) {
            source = Utilities.readSourceFromUpdateTracking (info);
        }
        // fallback to product version
        if (source == null) {
            source = Utilities.getProductVersion ();
        }
        return source;
    }
    
    @Override
    public String getAuthor () {
        return author;
    }
    
    @Override
    public ModuleInfo getModuleInfo () {        
        return info;
    }
    
    @Override
    public String getAgreement () {
        assert false : "Don't call getAgreement() on InstalledModuleItem " + info;
        return null;
    }

    @Override
    public int getDownloadSize () {
        return 0;
    }
    
    @Override
    public String getDate () {
        return installDate;
    }
    
    @Override
    public UpdateItemDeploymentImpl getUpdateItemDeploymentImpl () {
        assert false : "Don't call getUpdateItemDeploymentImpl () on InstalledModuleItem.";
        return null;
    }
    
    @Override
    public boolean isAutoload () {
        return getModule () != null && getModule ().isAutoload ();
    }

    @Override
    public boolean isEager () {
        return getModule () != null && getModule ().isEager ();
    }
    
    private Module getModule () {
        if (info instanceof Module) {
            return (Module)info;
        }
        return null;
    }
    
    @Override
    public void setNeedsRestart(Boolean needsRestart) {
        // do nothing
    } 
   
    @Override
    public boolean isFragment() {
        return info.getAttribute("OpenIDE-Module-Fragment-Host") != null; // NOI18N
    }
}
