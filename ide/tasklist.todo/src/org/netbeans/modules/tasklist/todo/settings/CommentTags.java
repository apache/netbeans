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
package org.netbeans.modules.tasklist.todo.settings;

/**
 *
 * @author jpeska
 */
public class CommentTags {

    private String lineComment = "";
    private String blockCommentStart = "";
    private String blockCommentEnd = "";
    private boolean lineCommentEnabled = false;
    private boolean blockCommentEnabled = false;


    public CommentTags(String lineComment, String blockCommentStart, String blockCommentEnd) {
        this.lineComment = lineComment;
        lineCommentEnabled = !lineComment.isEmpty();
        this.blockCommentStart = blockCommentStart;
        this.blockCommentEnd = blockCommentEnd;
        blockCommentEnabled = !blockCommentStart.isEmpty();
    }

    public CommentTags(String blockCommentStart, String blockCommentEnd) {
        this.blockCommentStart = blockCommentStart;
        this.blockCommentEnd = blockCommentEnd;
        blockCommentEnabled = true;
    }

    public CommentTags(String lineComment, boolean lineCommentEnabled, String blockCommentStart, String blockCommentEnd, boolean blockCommentEnabled) {
        this.lineComment = lineComment;
        this.lineCommentEnabled = lineCommentEnabled;
        this.blockCommentStart = blockCommentStart;
        this.blockCommentEnd = blockCommentEnd;
        this.blockCommentEnabled = blockCommentEnabled;
    }

    public CommentTags(String lineComment) {
        this.lineComment = lineComment;
        lineCommentEnabled = true;
    }

    public CommentTags() {
    }

    public String getLineComment() {
        return lineComment;
    }

    public void setLineComment(String lineComment) {
        this.lineComment = lineComment;
    }

    public String getBlockCommentStart() {
        return blockCommentStart;
    }

    public void setBlockCommentStart(String blockCommentStart) {
        this.blockCommentStart = blockCommentStart;
    }

    public String getBlockCommentEnd() {
        return blockCommentEnd;
    }

    public void setBlockCommentEnd(String blockCommentEnd) {
        this.blockCommentEnd = blockCommentEnd;
    }

    public boolean isLineCommentEnabled() {
        return lineCommentEnabled;
    }

    public void setLineCommentEnabled(boolean lineCommentEnabled) {
        this.lineCommentEnabled = lineCommentEnabled;
    }

    public boolean isBlockCommentEnabled() {
        return blockCommentEnabled;
    }

    public void setBlockCommentEnabled(boolean blockCommentEnabled) {
        this.blockCommentEnabled = blockCommentEnabled;
    }
}
