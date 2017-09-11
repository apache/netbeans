/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
