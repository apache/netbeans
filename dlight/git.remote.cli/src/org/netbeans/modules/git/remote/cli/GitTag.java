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
