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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class DBSchemaFileList {

    private final Map<FileObject, String> dbschema2DisplayName = new HashMap<>();
    private final List<FileObject> dbschemaList;

    public DBSchemaFileList(Project project, FileObject configFilesFolder) {
        SourceGroup[] sourceGroups = SourceGroups.getJavaSourceGroups(project);

        // XXX this recursive search is a potential performance problem
        for (int i = 0; i < sourceGroups.length; i++) {
            searchRoot(sourceGroups[i].getRootFolder(), sourceGroups[i].getDisplayName());
        }

        if (configFilesFolder != null) {
            String configFilesDisplayName = NbBundle.getMessage(DBSchemaFileList.class, "LBL_Node_DocBase");
            searchRoot(configFilesFolder, configFilesDisplayName);
        }

        List<FileObject> tempDBSchemaList = new ArrayList<>(dbschema2DisplayName.keySet());
        tempDBSchemaList.sort(new DBSchemaComparator());

        dbschemaList = Collections.unmodifiableList(tempDBSchemaList);
    }

    private void searchRoot(FileObject root, String rootDisplayName) {
        Enumeration<? extends FileObject> ch = root.getChildren(true);
        while (ch.hasMoreElements()) {
            FileObject f = (FileObject) ch.nextElement();
            if (f.getExt().equals(DBSchemaManager.DBSCHEMA_EXT) && !f.isFolder()) {
                if (!dbschema2DisplayName.containsKey(f)) {
                    String relativeParent = FileUtil.getRelativePath(root, f.getParent()) + File.separator;
                    if (relativeParent.startsWith("/")) { // NOI18N
                        relativeParent = relativeParent.substring(1);
                    }
                    String relative = relativeParent + f.getName();
                    String displayName = NbBundle.getMessage(DBSchemaFileList.class,
                            "LBL_SchemaLocation", rootDisplayName, relative);
                    dbschema2DisplayName.put(f, displayName);
                }
            }
        }
    }

    public List<FileObject> getFileList() {
        return dbschemaList;
    }

    public String getDisplayName(FileObject dbschemaFile) {
        return dbschema2DisplayName.get(dbschemaFile);
    }

    private final class DBSchemaComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            FileObject f1 = (FileObject)o1;
            FileObject f2 = (FileObject)o2;

            String displayName1 = dbschema2DisplayName.get(f1);
            String displayName2 = dbschema2DisplayName.get(f2);

            return displayName1.compareTo(displayName2);
        }
    }
}
