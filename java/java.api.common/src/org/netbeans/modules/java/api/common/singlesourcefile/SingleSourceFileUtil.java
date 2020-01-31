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
package org.netbeans.modules.java.api.common.singlesourcefile;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

/**
 *
 * @author Arunava Sinha
 */
class SingleSourceFileUtil {

    public static final String FILE_ARGUMENTS = "single_file_run_arguments"; //NOI18N
    public static final String FILE_VM_OPTIONS = "single_file_vm_options"; //NOI18N

    static FileObject getJavaFileWithoutProjectFromLookup(Lookup lookup) {
        for (DataObject dObj : lookup.lookupAll(DataObject.class)) {
            FileObject fObj = dObj.getPrimaryFile();
            if (isSingleSourceFile(fObj)) {
                return fObj;
            }
        }
        return null;
    }

    static boolean isSingleSourceFile(FileObject fObj) {
        Project p = FileOwnerQuery.getOwner(fObj);
        if (p != null || !fObj.getExt().equalsIgnoreCase("java")) { //NOI18N
            return false;
        }
        // JEP-330 is supported only on JDK-11 and above.
        String javaVersion = System.getProperty("java.specification.version"); //NOI18N 
        if (javaVersion.startsWith("1.")) { //NOI18N
            javaVersion = javaVersion.substring(2);
        }
        int version = Integer.parseInt(javaVersion);

        return version >= 11;
    }

}
