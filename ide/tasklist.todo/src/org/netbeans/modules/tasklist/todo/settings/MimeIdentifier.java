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
 * To store MIME type and its loader display name. It is used in list.
 */
public class MimeIdentifier extends FileIdentifier {

    private String mimeType;
    private String mimeName;

    public MimeIdentifier(String mimeType, String mimeName) {
        this(mimeType, mimeName, null);
    }

    public MimeIdentifier(String mimeType, String mimeName, CommentTags commentTags) {
        super(commentTags);
        this.mimeType = mimeType;
        this.mimeName = mimeName;
    }

    @Override
    Type getType() {
        return FileIdentifier.Type.MIME;
    }

    @Override
    public String getDisplayName() {
        return mimeName == null || mimeName.isEmpty() ? mimeType : mimeName + " (" + mimeType + ")";
    }

    @Override
    public String getId() {
        return mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getMimeName() {
        return mimeName;
    }
}
