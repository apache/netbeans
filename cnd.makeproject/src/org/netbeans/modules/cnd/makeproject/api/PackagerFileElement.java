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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
