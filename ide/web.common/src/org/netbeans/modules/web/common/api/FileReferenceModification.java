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

package org.netbeans.modules.web.common.api;

import java.util.Map;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public class FileReferenceModification {

    private Map<FileObject, String> items; //keys order preserving map
    private boolean absolutePathLink;

    FileReferenceModification(Map<FileObject, String> items, boolean absolutePathLink) {
        this.items = items;
        this.absolutePathLink = absolutePathLink;
    }

    public boolean rename(FileObject file, String newName) {
        if(items.get(file) == null) {
            return false;
        }
        String item = items.get(file);
        if(FileReference.DESCENDING_PATH_ITEM.equals(item)) {
            return false; // ../ path items is not affected by folder rename
        }

        items.put(file, newName); //LinkedHashMap preserves the key orders on put of existing key
        return true;
    }

    public String getModifiedReferencePath() {
        StringBuilder b = new StringBuilder();
        if(absolutePathLink) {
            b.append('/'); //initial slash
        }
        for(String item : items.values()) {
            b.append(item);
            b.append('/'); //NOI18N
        }
        //remove last slash after filename
        return b.deleteCharAt(b.length() - 1).toString();
    }

}
