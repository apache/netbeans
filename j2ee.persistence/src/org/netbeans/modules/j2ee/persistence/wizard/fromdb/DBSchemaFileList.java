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

    private final Map<FileObject,String> dbschema2DisplayName = new HashMap<FileObject,String>();
    private final List dbschemaList;

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

        List tempDBSchemaList = new ArrayList(dbschema2DisplayName.keySet());
        Collections.sort(tempDBSchemaList, new DBSchemaComparator());

        dbschemaList = Collections.unmodifiableList(tempDBSchemaList);
    }

    private void searchRoot(FileObject root, String rootDisplayName) {
        Enumeration ch = root.getChildren(true);
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

        public int compare(Object o1, Object o2) {
            FileObject f1 = (FileObject)o1;
            FileObject f2 = (FileObject)o2;

            String displayName1 = dbschema2DisplayName.get(f1);
            String displayName2 = dbschema2DisplayName.get(f2);

            return displayName1.compareTo(displayName2);
        }
    }
}
