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

package org.netbeans.modules.autoupdate.services;

import java.util.List;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.modules.autoupdate.updateprovider.InstallInfo;
import org.netbeans.modules.autoupdate.updateprovider.UpdateItemImpl;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Jiri Rechtacek
 */
public abstract class UpdateElementImpl extends Object {
    private UpdateUnit unit;
    private UpdateElement element;
    
    public UpdateElementImpl (UpdateItemImpl item, String providerName) {}
    
    public UpdateUnit getUpdateUnit () {
        return unit;
    }
    
    public void setUpdateUnit (UpdateUnit unit) {
        assert unit != null : "UpdateUnit cannot for " + this + " cannot be null.";
        this.unit = unit;
    }
    
    public UpdateElement getUpdateElement () {
        return element;
    }
    
    public void setUpdateElement (UpdateElement element) {
        assert element != null : "UpdateElement cannot for " + this + " cannot be null.";
        this.element = element;
    }
    
    public abstract String getCodeName ();
    
    public abstract String getDisplayName ();
    
    public abstract SpecificationVersion getSpecificationVersion ();
    
    public abstract String getDescription ();
    
    public abstract String getNotification();
    
    public abstract String getAuthor ();
    
    public abstract String getHomepage ();
    
    public abstract int getDownloadSize ();
    
    public abstract String getSource ();
    
    public abstract String getDate ();
    
    public abstract String getCategory ();
    
    public abstract boolean isEnabled ();

    public abstract String getLicence ();
    public abstract String getLicenseId();
    
    public abstract UpdateManager.TYPE getType ();
    
    public abstract boolean isAutoload ();
    public abstract boolean isEager ();
    public abstract boolean isFixed ();
    public abstract boolean isPreferredUpdate();
    
   // XXX: try to rid of this
    public abstract List<ModuleInfo> getModuleInfos ();
    public List<ModuleInfo> getModuleInfos(boolean recursive) {
        return getModuleInfos();
    }
    
    // XXX: try to rid of this
    public abstract InstallInfo getInstallInfo ();

}
