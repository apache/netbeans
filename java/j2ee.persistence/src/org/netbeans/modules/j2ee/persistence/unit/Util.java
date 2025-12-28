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
package org.netbeans.modules.j2ee.persistence.unit;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * 
 * @author Dongmei Cao
 */
public class Util {


    /*
     * return all properties for specific provider, except some handled specially
     */
    public static List<String> getAllPropNames(Provider propCat) {
        return PersistenceCfgProperties.getKeys(propCat);
    }

    public static List<String> getPropsNamesExceptGeneral(Provider propCat) {
        List<String> propsList = getAllPropNames(propCat);
        if(propCat != null){
            propsList.remove(propCat.getJdbcDriver());
            propsList.remove(propCat.getJdbcUsername());
            propsList.remove(propCat.getJdbcUrl());
            propsList.remove(propCat.getJdbcPassword());
            propsList.remove(propCat.getTableGenerationPropertyName());
        }
        return propsList;
    }

    /**
     * Gets the properties that are not defined in the configuration file yet
     * 
     * @param propCat The property category(persistence provider)
     * @param pu persistence unit that contains the properties
     * @return Array of property names
     */
    public static ArrayList<String> getAvailPropNames(Persistence persistence, Provider propCat, PersistenceUnit pu) {

        List<String> propsList = getPropsNamesExceptGeneral(propCat);

        if (pu != null) {
            ArrayList<String> availProps = new ArrayList<>(propsList);
            if(pu.getProperties() != null) {
                for (int i = 0; i < pu.getProperties().sizeProperty2(); i++) {
                    String propName = pu.getProperties().getProperty2(i).getName();
                    if (!availProps.remove(propName)
                            && availProps.contains("javax.persistence." + propName)) {
                        availProps.remove(propName);
                    }
                }
            }
            if(Persistence.VERSION_3_2.equals(persistence.getVersion())
                    || Persistence.VERSION_3_1.equals(persistence.getVersion())
                    || Persistence.VERSION_3_0.equals(persistence.getVersion())) {
                availProps.replaceAll(s -> s.replace(PersistenceUnit.JAVAX_NAMESPACE, PersistenceUnit.JAKARTA_NAMESPACE));
            }
            return availProps;
        }

        return new ArrayList<String>();
    }

    public static SourceGroup[] getJavaSourceGroups(PUDataObject dObj) throws java.io.IOException {
        Project proj = FileOwnerQuery.getOwner(dObj.getPrimaryFile());
        if (proj == null) {
            return new SourceGroup[]{};
        }
        Sources sources = ProjectUtils.getSources(proj);
        SourceGroup[] toRet = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
        if (toRet != null && toRet.length != 0) {
            return toRet;
        }
        return sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
    }

    public static String getResourcePath(SourceGroup[] groups, FileObject fo) {
        return getResourcePath(groups, fo, '.', false);
    }

    public static String getResourcePath(SourceGroup[] groups, FileObject fo, char separator) {
        return getResourcePath(groups, fo, separator, false);
    }

    public static String getResourcePath(SourceGroup[] groups, FileObject fo, char separator, boolean withExt) {
        for (int i = 0; i < groups.length; i++) {
            FileObject root = groups[i].getRootFolder();
            if (FileUtil.isParentOf(root, fo)) {
                String relativePath = FileUtil.getRelativePath(root, fo);
                if (relativePath != null) {
                    if (separator != '/') {
                        relativePath = relativePath.replace('/', separator);
                    }
                    if (!withExt) {
                        int index = relativePath.lastIndexOf((int) '.');
                        if (index > 0) {
                            relativePath = relativePath.substring(0, index);
                        }
                    }
                    return relativePath;
                } else {
                    return "";
                }
            }
        }
        return "";
    }
}
