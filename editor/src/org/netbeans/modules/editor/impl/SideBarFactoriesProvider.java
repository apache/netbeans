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


package org.netbeans.modules.editor.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.editor.impl.CustomizableSideBar.SideBarPosition;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;
import org.netbeans.spi.editor.mimelookup.MimeLocation;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author Martin Roskanin
 */
@MimeLocation(subfolderName=SideBarFactoriesProvider.SIDEBAR_COMPONENTS_FOLDER_NAME, instanceProviderClass=SideBarFactoriesProvider.class)
public final class SideBarFactoriesProvider implements InstanceProvider<SideBarFactoriesProvider> {

    private static final Logger LOG = Logger.getLogger(SideBarFactoriesProvider.class.getName());
    
    public static final String SIDEBAR_COMPONENTS_FOLDER_NAME = "SideBar"; //NOI18N
    
    private final List<FileObject> instanceFiles;
    private Map<CustomizableSideBar.SideBarPosition, List> factories;

    public SideBarFactoriesProvider() {
        this(Collections.<FileObject>emptyList());
    }

    private SideBarFactoriesProvider(List<FileObject> instanceFiles) {
        this.instanceFiles = instanceFiles;
    }
    
    public Map<CustomizableSideBar.SideBarPosition, List> getFactories() {
        if (factories == null) {
            factories = computeInstances();
        }
        return factories;
    }
    
    public SideBarFactoriesProvider createInstance(List<FileObject> fileObjectList) {
        return new SideBarFactoriesProvider(fileObjectList);
    }
    
    @SuppressWarnings({"deprecation", "unchecked"})
    private Map<CustomizableSideBar.SideBarPosition, List> computeInstances() {
        Map <CustomizableSideBar.SideBarPosition, List> factoriesMap = new HashMap<CustomizableSideBar.SideBarPosition, List>();
        
        for(FileObject f : instanceFiles) {
            org.netbeans.editor.SideBarFactory factory = null;
            org.netbeans.spi.editor.SideBarFactory factory2 = null;
            
            if (!f.isValid() || !f.isData()) {
                continue;
            }
            
            try {
                DataObject dob = DataObject.find(f);
                InstanceCookie.Of ic = dob.getCookie(InstanceCookie.Of.class);
                if (ic != null) {
                    if (ic.instanceOf(org.netbeans.editor.SideBarFactory.class)) {
                        factory = (org.netbeans.editor.SideBarFactory) ic.instanceCreate();
                    } else if (ic.instanceOf(org.netbeans.spi.editor.SideBarFactory.class)) {
                        factory2 = (org.netbeans.spi.editor.SideBarFactory) ic.instanceCreate();
                    }
                }
            } catch (ClassNotFoundException cnfe) {
                LOG.log(Level.INFO, null, cnfe);
                continue;
            } catch (Exception e) {
                LOG.log(Level.WARNING, null, e);
                continue;
            }

            if (factory != null || factory2 != null) {
                SideBarPosition position = new SideBarPosition(f);
                List factoriesList = factoriesMap.get(position);

                if (factoriesList == null) {
                    factoriesList = new ArrayList();
                    factoriesMap.put(position, factoriesList);
                }
                if (factory != null) {
                    factoriesList.add(factory);
                } else {
                    factoriesList.add(factory2);
                }
            }
        }
        
        return factoriesMap;
    }
}
