/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.git.remote.cli;

/**
 * Represents a git tag.
 * 
 */
public final class GitTag {
    private final String id;
    private final String name;
    private final String message;
    private final String taggedObject;
    private final GitUser tagger;
    private final GitObjectType type;
    private boolean lightWeight;

    GitTag (TagContainer revTag) {
        this.id = revTag.id;
        this.name = revTag.name;
        this.message = revTag.message;
        this.taggedObject = revTag.objectId;
        if (revTag.author != null) {
            int i = revTag.author.indexOf('<');
            this.tagger = new GitUser(revTag.author.substring(0,i).trim(), revTag.author.substring(i+1, revTag.author.length()-1));
        } else {
            this.tagger = new GitUser("", "");
        }
        this.type = revTag.type;
        if (revTag.message == null) {
            this.lightWeight = true;
        } else {
            this.lightWeight = false;
        }
    }

    GitTag (String tagName, String rev, GitObjectType type) {
        this.id = rev;
        this.name = tagName;
        this.message = null;
        this.taggedObject = id;
        this.tagger = null;
        this.type = type;
        this.lightWeight = true;
    }

    GitTag (String tagName, GitRevisionInfo revCommit) {
        this.id = revCommit.getRevision();
        this.name = tagName;
        this.message = revCommit.getFullMessage();
        this.taggedObject = id;
        this.tagger = revCommit.getAuthor() == null ? revCommit.getCommitter() : revCommit.getAuthor();
        this.type = GitObjectType.COMMIT;
        this.lightWeight = true;
    }

    /**
     * @return object id of this tag
     */
    public String getTagId () {
        return id;
    }

    /**
     * @return name of this tag
     */
    public String getTagName () {
        return name;
    }
    
    /**
     * @return object id of a tagged object, type of the object is returned by the <code>getType</code> method.
     */
    public String getTaggedObjectId () {
        return taggedObject;
    }

    /**
     * @return message entered when the tag was created.
     */
    public String getMessage () {
        return message;
    }

    /**
     * @return user who created this tag.
     * Never <code>null</code>, an empty person is returned instead when no user can be identified.
     */
    public GitUser getTagger () {
        return tagger;
    }

    /**
     * @return git object type this tag refers to
     */
    public GitObjectType getTaggedObjectType () {
        return type;
    }

    /**
     * @return <code>true</code> if the tag is lightweight.
     */
    public boolean isLightWeight () {
        return lightWeight;
    }

//    private GitObjectType getType (RevObject object) {
//        GitObjectType objType = GitObjectType.UNKNOWN;
//        if (object != null) {
//            switch (object.getType()) {
//                case Constants.OBJ_COMMIT:
//                    objType = GitObjectType.COMMIT;
//                    break;
//                case Constants.OBJ_BLOB:
//                    objType = GitObjectType.BLOB;
//                    break;
//                case Constants.OBJ_TAG:
//                    objType = GitObjectType.TAG;
//                    break;
//                case Constants.OBJ_TREE:
//                    objType = GitObjectType.TREE;
//                    break;
//            }
//        }
//        return objType;
//    }

    public static final class TagContainer {
        public String id;
        public String ref;
        public String name;
        public String author;
        public String time;
        public String message;
        public GitObjectType type = GitObjectType.UNKNOWN;
        public String objectId;
    }
    
}
