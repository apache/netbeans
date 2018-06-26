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
/*
 * ResourceConfigHelperHolder.java
 *
 * Created on October 17, 2002, 12:11 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.util.Vector;

import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;


/**
 *
 * @author  shirleyc
 */
public class ResourceConfigHelperHolder implements WizardConstants {
    private ResourceConfigHelper mainHelper = null;
    private Vector associated = new Vector();
    private boolean hasCP = false;
    private boolean hasDS = false;

    public ResourceConfigHelperHolder() {
        mainHelper = new ResourceConfigHelper(1);
    }
    
    public ResourceConfigHelperHolder (ResourceConfigHelper helper) {
        mainHelper = helper;
    }
    
    public ResourceConfigHelper addAssociatedHelper() {
        ResourceConfigHelper helper = new ResourceConfigHelper(1);
        associated.add(helper);
        return helper;
    }
     
    public ResourceConfigHelper getMainHelper() {
        return mainHelper;
    }
    
    public ResourceConfigHelper getConnPoolHelper() {
        mainHelper.getData().setResourceName(__JdbcConnectionPool);
        return mainHelper;
    }
    
    public ResourceConfigHelper getDataSourceHelper() {
        mainHelper.getData().setResourceName(__JdbcResource);
        return mainHelper;
    }
    
    public ResourceConfigHelper getJMSHelper() {
        mainHelper.getData().setResourceName(__JmsResource);
        return mainHelper;
    }
    
    public ResourceConfigHelper getMailHelper() {
        mainHelper.getData().setResourceName(__MailResource);
        return mainHelper;
    }
    
    public ResourceConfigHelper getPMFHelper() {
        mainHelper.getData().setResourceName(__PersistenceManagerFactoryResource);
        return mainHelper;
    }
    
    public Vector getAssociatedHelpers() {
        return associated;
    }
    
    public ResourceConfigHelper getCPHelper() {
        for (int i = 0; i < associated.size(); i++) {
            ResourceConfigHelper item = (ResourceConfigHelper)associated.elementAt(i);
            if (item.getData().getResourceName().equals(__JdbcConnectionPool)) {
                return item;
            }
        }
        return null;
    }
    
    public ResourceConfigHelper getDSHelper() {
        for (int i = 0; i < associated.size(); i++) {
            ResourceConfigHelper item = (ResourceConfigHelper)associated.elementAt(i);
            if (item.getData().getResourceName().equals(__JdbcResource)) {
                return item;
            }
        }
        return null;
    }
    
    public void removeAssociatedHelpers() {
        if (associated.size() > 0)
            associated = new Vector();
    }
    
    public void removeLastAssociatedHelper() {
        if (associated.size() > 0)
            associated.remove(associated.size() - 1);
    }
    
    public boolean hasAssociatedHelpers() {
        return hasCP || hasDS;
    }
    
    public void setHasCPHelper(boolean value) {
        hasCP = value;
    }
    
    public boolean hasCPHelper() {
        return hasCP;
    }
    
    public boolean hasDSHelper() {
        return hasDS;
    }
    
    public void setHasDSHelper(boolean value) {
        hasDS = value;
    }
}
