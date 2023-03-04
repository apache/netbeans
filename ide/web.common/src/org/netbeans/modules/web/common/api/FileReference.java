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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.openide.filesystems.FileObject;

/**
 * File reference descriptor. Represents a link from one file to another
 *
 * @author marekfukala
 */
public final class FileReference {

    static final String DESCENDING_PATH_ITEM = ".."; //NOI18N
    
    private FileObject source, target, baseFolder;
    private String linkPath;
    private FileReferenceType type;

    FileReference(FileObject source, FileObject target, FileObject baseFolder, String linkPath, FileReferenceType type) {
        assert baseFolder == null || baseFolder.isFolder();

        this.source = source;
        this.target = target;
        this.baseFolder = baseFolder;
        this.linkPath = linkPath;
        this.type = type;
    }
    
    public FileObject source() {
        return source;
    }

    public FileObject target() {
        return target;
    }

    public FileObject baseFolder() {
        return baseFolder;
    }

    public List<FileObject> sourcePathMembersToBase() {
        return type() == FileReferenceType.RELATIVE ? 
            getPathMembersToBase(source()) :
            Collections.<FileObject>emptyList(); //absolute path must not list the source file path members
    }
    
    public List<FileObject> targetPathMembersToBase() {
        return getPathMembersToBase(target());
    }

    private List<FileObject> getPathMembersToBase(FileObject file) {
        if(baseFolder() == null) {
            return Collections.emptyList();
        }
        List<FileObject> members = new LinkedList<FileObject>();
        FileObject member = file;
        while((member = member.getParent()) != null && !member.equals(baseFolder())) {
            members.add(0, member);
        }
        return members;
    }

    public String linkPath() {
        return linkPath;
    }

    /**
     * @return normalized relative path.
     */
    public String optimizedLinkPath() {
        return type() == FileReferenceType.RELATIVE ?
            WebUtils.getRelativePath(source, target):
            linkPath();
    }

    public FileReferenceType type() {
        return type;
    }

    public FileReferenceModification createModification() {
        Map<FileObject, String> items = new LinkedHashMap<FileObject, String>();
        //add source path members first
        List<FileObject> members = sourcePathMembersToBase();
        //we need to add them in reveresed order - they are ordered from base to the leaf as the target member
        Collections.reverse(members);
        for(FileObject file : members) {
            items.put(file, DESCENDING_PATH_ITEM); //NOI18N
        }
        //add target path members
        for(FileObject file : targetPathMembersToBase()) {
            items.put(file, file.getNameExt());
        }

        //add the target file itself
        items.put(target, target.getNameExt());

        return new FileReferenceModification(items, type() == FileReferenceType.ABSOLUTE);
    }

}
