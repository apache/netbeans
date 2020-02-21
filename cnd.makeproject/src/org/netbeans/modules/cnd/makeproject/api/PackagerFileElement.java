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

package org.netbeans.modules.cnd.makeproject.api;

public class PackagerFileElement {
    public enum FileType {
        FILE {
            @Override
            public String toString() {
                return "File"; // NOI18N
            }
        },
        DIRECTORY {
            @Override
            public String toString() {
                return "Dir"; // NOI18N
            }
        },
        SOFTLINK {
            @Override
            public String toString() {
                return "Link"; // NOI18N
            }
        },
        UNKNOWN {
            @Override
            public String toString() {
                return ""; // NOI18N
            }
        }};
    
    private FileType type;
    private String from;
    private String to;
    private String permission;
    private String owner;
    private String group;
    private boolean defaultValue;
    
    public PackagerFileElement(FileType type, String from, String to) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.permission = ""; // NOI18N
        this.owner = ""; // NOI18N
        this.group = ""; // NOI18N
        this.defaultValue = false;
    }
    
    public PackagerFileElement(FileType type, String from, String to, String permission, String owner, String group) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.permission = permission;
        this.owner = owner;
        this.group = group;
        this.defaultValue = false;
    }
    
    public FileType getType() {
        return type;
    }
    
    public static FileType toFileType(String type) {
        if (FileType.DIRECTORY.toString().equals(type)) {
            return FileType.DIRECTORY;
        }
        else if (FileType.FILE.toString().equals(type)) {
            return FileType.FILE;
        }
        else if (FileType.SOFTLINK.toString().equals(type)) {
            return FileType.SOFTLINK;
        }
        else {
            return FileType.UNKNOWN;
        }
    }

    public void setType(FileType type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(boolean defaultValue) {
        this.defaultValue = defaultValue;
    }
}
