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
package org.netbeans.modules.cnd.mixeddev.java.model;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.mixeddev.java.QualifiedNamePart;

/**
 *
 */
public final class JavaClassInfo implements JavaEntityInfo {
    
    private final CharSequence name;
    
    private final List<QualifiedNamePart> qualifiedName;
    private final String stringRepresentation;

    public JavaClassInfo(CharSequence name, List<QualifiedNamePart> qualifiedName) {
        this.name = name;
        this.qualifiedName = Collections.unmodifiableList(qualifiedName);
        StringBuilder res = new StringBuilder();
        for (QualifiedNamePart qualifiedNamePart : qualifiedName) {
            res.append(qualifiedNamePart.toString()).append('.');
        }
        if (qualifiedName.size() > 1) {
            res.deleteCharAt(res.length()-1);
        }
        stringRepresentation = res.toString();
    }

    public CharSequence getName() {
        return name;
    }

    public List<QualifiedNamePart> getQualifiedName() {
        return qualifiedName;
    }
    
    public String getQualified() {
        return stringRepresentation;
    }
}
