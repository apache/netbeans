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
package org.netbeans.modules.css.refactoring.api;

import org.openide.filesystems.FileObject;


/**
 *
 * @author mfukala@netbeans.org
 */
public class CssRefactoringInfo {
    public enum Type {
        ELEMENT,
        CLASS,
        ID,
        HEX_COLOR,
        RESOURCE_IDENTIFIER,
        URI
    }

    private final FileObject fileObject;
    private final String name;
    private final Type type;

    public CssRefactoringInfo(FileObject fileObject, String name, Type type) {
        this.fileObject = fileObject;
        this.name = name;
        this.type = type;
    }

    public FileObject getFileObject() {
        return fileObject;
    }

    public String getElementName() {
        return name;
    }

    public Type getType() {
        return type;
    }
}
