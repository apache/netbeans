/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
