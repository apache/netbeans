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
package org.netbeans.modules.java.source.usages;

import java.util.Objects;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.annotations.common.NonNull;

/**
 *
 * @author Tomas Zezula
 */
final class BinaryName {
    static final char PKG_SEPARATOR = '.';                              //NOI18N
    
    private final String binaryName;
    private final int pkgEnd;
    private final int simpleNameStart;
    
    private BinaryName(
            @NonNull final String binaryName,
            final int pkgEnd,
            final int simpleNameStart) {
        assert pkgEnd < binaryName.length() - 1 : binaryName;
        assert pkgEnd != 0 : binaryName;
        assert simpleNameStart < binaryName.length() - 1 : binaryName;
        assert simpleNameStart > pkgEnd : binaryName;
        this.binaryName = binaryName;
        this.pkgEnd = pkgEnd;
        this.simpleNameStart = simpleNameStart;
    }
    
    @NonNull
    String getBinaryName() {
        return binaryName.substring(0, binaryName.length()-1);
    }
    
    @NonNull
    char getKind() {
        return binaryName.charAt(binaryName.length()-1);
    }
    
    @NonNull
    String getPackage() {
        return pkgEnd > 0 ?
                binaryName.substring(0, pkgEnd) :
                ""; //NOI18N
    }
    
    @NonNull 
    String getClassName() {
        return pkgEnd > 0 ?
                binaryName.substring(pkgEnd+1, binaryName.length()-1) :
                binaryName.substring(0, binaryName.length()-1);
                
    }
    
    @NonNull 
    String getClassNameKind() {
        return pkgEnd > 0 ?
                binaryName.substring(pkgEnd+1) :
                binaryName;
    }
    
    @NonNull
    String getSimpleName() {
        return binaryName.substring(simpleNameStart, binaryName.length()-1);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + Objects.hashCode(this.binaryName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BinaryName other = (BinaryName) obj;
        if (this.pkgEnd != other.pkgEnd) {
            return false;
        }
        if (this.simpleNameStart != other.simpleNameStart) {
            return false;
        }
        return Objects.equals(this.binaryName, other.binaryName);
    }

    @Override
    public String toString() {
        return String.format("BinaryName{binaryName=%s, package=%s, className=%s, simpleName=%s}",  //NOI18N
                binaryName,
                getPackage(),
                getClassName(),
                getSimpleName());
    }
    
    
    
    
    @NonNull
    static BinaryName create(
            @NonNull final String binaryName,
            @NonNull final ElementKind kind) {
        final int pkgEnd = binaryName.lastIndexOf(PKG_SEPARATOR);
        int simpleNameStart = binaryName.lastIndexOf('$');      //NOI18N
        if (simpleNameStart < pkgEnd) {
            simpleNameStart = pkgEnd;
        }
        simpleNameStart += 1;
        return new BinaryName(
                binaryName+DocumentUtil.encodeKind(kind),
                pkgEnd,
                simpleNameStart);
    }
    
    @NonNull
    static BinaryName create(
            @NonNull final String binaryName,
            @NonNull final ElementKind kind,
            final boolean isLocal,
            final int simpleNameStart) {
        final int pkgEnd = binaryName.lastIndexOf(PKG_SEPARATOR, simpleNameStart);
        return new BinaryName(
                binaryName+DocumentUtil.encodeKind(kind, isLocal),
                pkgEnd,
                simpleNameStart);
    }
}
