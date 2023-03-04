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
package org.netbeans.libs.git;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;

/**
 * Represents a git tag.
 * 
 * @author Ondra Vrabec
 */
public final class GitTag {
    private final String id;
    private final String name;
    private final String message;
    private final String taggedObject;
    private final GitUser tagger;
    private final GitObjectType type;
    private boolean lightWeight;

    GitTag (RevTag revTag) {
        this.id = ObjectId.toString(revTag.getId());
        this.name = revTag.getTagName();
        this.message = revTag.getFullMessage();
        this.taggedObject = ObjectId.toString(revTag.getObject().getId());
        PersonIdent personIdent = revTag.getTaggerIdent();
        if (personIdent == null) {
            personIdent = new PersonIdent("", ""); //NOI18N
        }
        this.tagger = new GitUser(personIdent.getName(), personIdent.getEmailAddress());
        this.type = getType(revTag.getObject());
        this.lightWeight = false;
    }

    GitTag (String tagName, RevObject revObject) {
        this.id = ObjectId.toString(revObject.getId());
        this.name = tagName;
        this.message = null;
        this.taggedObject = id;
        this.tagger = null;
        this.type = getType(revObject);
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

    private GitObjectType getType (RevObject object) {
        GitObjectType objType = GitObjectType.UNKNOWN;
        if (object != null) {
            switch (object.getType()) {
                case Constants.OBJ_COMMIT:
                    objType = GitObjectType.COMMIT;
                    break;
                case Constants.OBJ_BLOB:
                    objType = GitObjectType.BLOB;
                    break;
                case Constants.OBJ_TAG:
                    objType = GitObjectType.TAG;
                    break;
                case Constants.OBJ_TREE:
                    objType = GitObjectType.TREE;
                    break;
            }
        }
        return objType;
    }
    
}
