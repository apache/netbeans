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
package org.netbeans.modules.tasklist.todo.settings;

/**
 *
 * @author jpeska
 */
public abstract class FileIdentifier implements Comparable<FileIdentifier>{

    private CommentTags commentTags;

    public FileIdentifier(CommentTags commentTags) {
        if (commentTags == null) {
            this.commentTags = new CommentTags();
        } else {
            this.commentTags = commentTags;
        }
    }

    public CommentTags getCommentTags() {
        return commentTags;
    }

    public void setCommentTags(CommentTags commentTags) {
        this.commentTags = commentTags;
    }

    public boolean isValid() {
        boolean lineCommentValid = !commentTags.getLineComment().isEmpty() || !commentTags.isLineCommentEnabled();
        boolean blockCommentValid = (!commentTags.getBlockCommentStart().isEmpty() && !commentTags.getBlockCommentEnd().isEmpty()) || !commentTags.isBlockCommentEnabled();

        return lineCommentValid && blockCommentValid && (commentTags.isLineCommentEnabled() || commentTags.isBlockCommentEnabled());
    }

    public abstract String getDisplayName();

    public abstract String getId();

    abstract Type getType();

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public int compareTo(FileIdentifier other) {
        int comparePriority = getType().getSortPriority().compareTo(other.getType().getSortPriority());
        if (comparePriority != 0) {
            return comparePriority;
        }
        return getDisplayName().compareToIgnoreCase(other.getDisplayName());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FileIdentifier)) {
           return false;
        }
        FileIdentifier other = (FileIdentifier) obj;
        if (this.getType() != other.getType()) {
            return false;
        }
        return this.getId().equalsIgnoreCase(other.getId());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + (this.commentTags != null ? this.commentTags.hashCode() : 0);
        return hash;
    }

    static enum Type {
        MIME(1, "MIME Types"),
        EXTENSION(2, "Extensions");

        private final Integer sortPriority;
        private final String displayName;

        Type(Integer sortPriority, String displayName) {
            this.displayName = displayName;
            this.sortPriority = sortPriority;
        }

        public Integer getSortPriority() {
            return sortPriority;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
