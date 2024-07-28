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
package org.netbeans.modules.php.blade.editor.indexing;

import java.io.IOException;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.blade.project.ProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author bogdan
 */
public class QueryUtils {

    public static List<BladeIndex.IndexedReference> queryYieldReferences(String prefix, FileObject fo) {
        BladeIndex bladeIndex = getIndex(fo);
        if (bladeIndex == null) {
            return null;
        }
        return bladeIndex.queryYieldIndexedReferences(prefix);
    }
    
    public static List<BladeIndex.IndexedReference> findYieldReferences(String prefix, FileObject fo) {
        BladeIndex bladeIndex = getIndex(fo);
        if (bladeIndex == null) {
            return null;
        }
        return bladeIndex.findYieldIndexedReferences(prefix);
    }

    public static List<BladeIndex.IndexedReference> queryStacksReferences(String prefix, FileObject fo) {
        BladeIndex bladeIndex = getIndex(fo);
        if (bladeIndex == null) {
            return null;
        }
        return bladeIndex.queryStacksIdsReference(prefix);
    }
    
    public static List<BladeIndex.IndexedOffsetReference> getIncludePathReferences(String prefix, FileObject fo) {
        BladeIndex bladeIndex = getIndex(fo);
        if (bladeIndex == null) {
            return null;
        }
        return bladeIndex.getIncludePaths(prefix);
    }
    
    public static BladeIndex getIndex(FileObject fo) {
        Project project = ProjectUtils.getMainOwner(fo);
        
        try {
            return BladeIndex.get(project);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
