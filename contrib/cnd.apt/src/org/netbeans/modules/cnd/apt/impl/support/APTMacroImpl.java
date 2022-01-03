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

package org.netbeans.modules.cnd.apt.impl.support;

import org.netbeans.modules.cnd.antlr.TokenStream;
import java.io.IOException;
import java.util.Collection;
import org.netbeans.modules.cnd.apt.impl.structure.APTBuilderImpl;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.apt.utils.ListBasedTokenStream;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * implementation of APTMacro
 */
public final class APTMacroImpl implements APTMacro {
    private final CharSequence file;
    private final APTDefine defineNode;
    private final Kind macroType;
    private volatile int hashCode = 0;

    public APTMacroImpl(CharSequence file, APTDefine defineNode, Kind macroType) {
        assert (defineNode.getName() != null);
        this.file = file;
        assert file != null;
        assert file.length() == 0 || macroType == Kind.DEFINED : "file info has only #defined macro " + file;
        this.defineNode = (APTDefine) APTBuilderImpl.createLightCopy(defineNode);
        this.macroType = macroType;
    }

    @Override
    public CharSequence getFile() {
        return file;
    }

    @Override
    public Kind getKind() {
        return macroType;
    }

    @Override
    public boolean isFunctionLike() {
        return defineNode.isFunctionLike();
    }

    @Override
    public APTToken getName() {
        return defineNode.getName();
    }

    @Override
    public Collection<APTToken> getParams() {
        return defineNode.getParams();
    }

    @Override
    public TokenStream getBody() {
        return new ListBasedTokenStream(defineNode.getBody());
    }

    @Override
    public APTDefine getDefineNode() {
        return defineNode;
    }

    @Override
    public boolean equals(Object obj) {
        boolean retValue;
        if (obj == null || !(obj instanceof APTMacroImpl)) {
            retValue = false;
        } else {
            APTMacroImpl other = (APTMacroImpl)obj;
            retValue = APTMacroImpl.equals(this, other);
        }
        return retValue;
    }

    private static boolean equals(APTMacroImpl one, APTMacroImpl other) {
        if (one.macroType != other.macroType) {
            return false;
        }
        // check files
        if ((one.file == other.file) && (one.file != null) && !one.file.equals(other.file)) {
            return false;
        }
        return one.defineNode.equals(other.defineNode);
    }

    @Override
    public int hashCode() {
        int retValue = hashCode;
        if (retValue == 0) {
            // init hash
            retValue = 31*retValue + macroType.ordinal();
            retValue = 31*retValue + (file == null ? 0 : file.hashCode());
            retValue = 31*retValue + defineNode.hashCode();
            hashCode = APTUtils.hash(retValue);
            retValue = hashCode;
        }
        return retValue;
    }

    @Override
    public String toString() {
        StringBuilder retValue = new StringBuilder();
        // preserve macro signature for existing model tests
        switch(getKind()){
            case DEFINED:
                retValue.append("<U>"); // NOI18N
                break;
            case COMPILER_PREDEFINED:
            case POSITION_PREDEFINED:
            case USER_SPECIFIED:
            default:
                retValue.append("<S>"); // NOI18N
                break;
        }
        retValue.append("#define '"); // NOI18N
        retValue.append(getName());
        if (getParams() != null) {
            retValue.append("["); // NOI18N
            boolean first = true;
            for (APTToken elem : getParams()) {
                if (!first) {
                    retValue.append(", "); // NOI18N
                }
                first = false;
                retValue.append(elem);
            }
            retValue.append("]"); // NOI18N
        }
        TokenStream bodyStream = getBody();
        retValue.append("'='"); // NOI18N
        retValue.append(APTUtils.toString(bodyStream));
        return retValue.toString();
    }

    public void write(RepositoryDataOutput output) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented"); // NOI18N
    }

    public APTMacroImpl(RepositoryDataInput input) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented"); // NOI18N
    }

}
