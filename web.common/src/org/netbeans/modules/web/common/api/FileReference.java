/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
