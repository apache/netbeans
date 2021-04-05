/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.html.ojet.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.html.ojet.OJETUtils;
import org.netbeans.modules.javascript2.model.api.JsFunction;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class DataProviderImpl extends DataProvider {

    private static DataProviderImpl instance = null;
    private static final String zipFolder = "docs";
    private static final String ZIP_PREFIX = "ojetdocs-";
    private static final String ZIP_EXTENSION = ".zip";
    protected static final String DEFAULT_VERSION = "2.0.0";
    private static final HashMap<String, DataItemImpl.DataItemComponent> data = new HashMap<>();
    private static DataItemImpl.DataItemModule moduleData = null;
    private static FileObject docRoot = null;
    private static String currentVersion;

    synchronized public static DataProvider getInstance() {
        if (instance == null) {
            instance = new DataProviderImpl();
            currentVersion = DEFAULT_VERSION;
        }
        if (data.isEmpty()) {
            File zipFile = InstalledFileLocator.getDefault().locate(zipFolder + "/" + codeFileNameFromVersion(currentVersion), "org.netbeans.modules.html.ojet", false); //NOI18N
            if (zipFile != null && zipFile.exists()) {
                docRoot = FileUtil.toFileObject(zipFile);
                docRoot = FileUtil.getArchiveRoot(docRoot);
                if (docRoot != null) {
                    FileObject folder = docRoot.getFileObject("docs"); // NOI18N
                    if (folder != null && folder.isValid()) {
                        for (FileObject child : folder.getChildren()) {
                            String name = child.getName();
                            if (name.startsWith("oj.oj")) {
                                name = name.substring(3);
                                data.put(name, new DataItemImpl.DataItemComponent(name, child.toURL().toString()));
                            } else if (OJETUtils.OJ_MODULE.equals(name)) {
                                moduleData = new DataItemImpl.DataItemModule(child.toURL().toString());
                            }
                        }
                    }
                }
            }
        }
        return instance;
    }

    @Override
    public Collection<DataItem> getBindingOptions() {
        List<DataItem> result = new ArrayList<DataItem>(1);
        result.add((new DataItemImpl(OJETUtils.OJ_COMPONENT, null, OJETUtils.OJ_COMPONENT + ": {component: }"))); // NOI18N
        result.add(new DataItemImpl(OJETUtils.OJ_MODULE, null, OJETUtils.OJ_MODULE + ": "));
        return result;
    }

    @Override
    public Collection<DataItem> getComponents() {
        List<DataItem> result = new ArrayList<>();
        for (DataItem component : data.values()) {
            result.add(component);
        }
        return result;
    }

    @Override
    public Collection<DataItem> getComponentOptions(String compName) {
        DataItemImpl.DataItemComponent component = data.get(compName);
        if (component != null) {
            return component.getOptions();
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<DataItem> getModuleProperties() {
        if (moduleData != null) {
            return moduleData.getProperies();
        }
        return Collections.emptyList(); 
    }

    
    @Override
    public Collection<DataItem> getComponentEvents(String compName) {
        DataItemImpl.DataItemComponent component = data.get(compName);
        if (component != null) {
            return component.getEvents();
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<String> getAvailableVersions() {
        File folder = InstalledFileLocator.getDefault().locate(zipFolder, "org.netbeans.modules.html.ojet", false); //NOI18N
        List<String> versions = new ArrayList<String>();
        if (folder.exists()) {
            File[] files = folder.listFiles();
            for (File file : files) {
                String fileName = file.getName();
                if (file.isFile() && fileName.startsWith(ZIP_PREFIX) && fileName.endsWith(ZIP_EXTENSION)) {
                    versions.add(decodeVersionFromFileName(fileName));
                }
            }
        }
        return versions;
    }

    private static String decodeVersionFromFileName(String name) {
        String version = name.substring(ZIP_PREFIX.length());
        version = version.substring(0, version.length() - ZIP_EXTENSION.length());
        version = version.replace('_', '.');
        return version;
    }
    
    private static String codeFileNameFromVersion(String version) {
        StringBuilder sb = new StringBuilder();
        sb.append(ZIP_PREFIX);
        sb.append(version.replace('.', '_'));
        sb.append(ZIP_EXTENSION);
        return sb.toString();
    }
    
    @Override
    public String getCurrentVersion() {
        return currentVersion;
    }

    @Override
    public void setCurrentVersion(String version) {
        if (!getAvailableVersions().contains(version)) {
            throw new IllegalArgumentException(version + " is unknown version");
        } 
        if (!currentVersion.equals(version)) {
            currentVersion = version;
            // reset the cache
            data.clear();
        }
    }

    @Override
    @NbBundle.Messages("label_ojet=OJET")
    public Collection<JsObject> getGlobalObjects(ModelElementFactory factory) {
        JsFunction global = factory.newGlobalObject(null, 0);
        JsObject oj = factory.newObject(global, "oj", OffsetRange.NONE, true, Bundle.label_ojet()); //NOI18N
        factory.putGlobalProperty(global, oj);
        return Collections.singletonList((JsObject)global);
    }
}
