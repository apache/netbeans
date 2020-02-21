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

package org.netbeans.modules.cnd.modelimpl.repository;

import java.io.IOException;
import org.netbeans.modules.cnd.api.model.CsmParameterList;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmObjectFactory;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.repository.spi.KeyDataPresentation;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.openide.util.CharSequences;

/**
 * A key for CsmParameterList objects (file and offset -based)
 */
/*package*/ final class ParamListKey extends OffsetableKey {
    ParamListKey(CsmParameterList obj) {
        super(obj, CharSequences.empty()); // NOI18N
    }

    /*package*/ ParamListKey(RepositoryDataInput aStream) throws IOException {
        super(aStream);
    }

    ParamListKey(KeyDataPresentation presentation) {
        super(presentation);
    }

    @Override
    public PersistentFactory getPersistentFactory() {
        return CsmObjectFactory.instance();
    }

    @Override
    char getKind() {
        return Utils.getCsmParamListKindKey();
    }

    @Override
    public short getHandler() {
        return KeyObjectFactory.KEY_PARAM_LIST_KEY;
    }

    @Override
    public String toString() {
        String retValue;

        retValue = "ParamListKey: " + super.toString(); // NOI18N
        return retValue;
    }

    @Override
    public int getSecondaryDepth() {
        return super.getSecondaryDepth() + 1;
    }

    @Override
    public int getSecondaryAt(int level) {
        if (level == 0) {
            return getHandler();
        } else {
            return super.getSecondaryAt(level - 1);
        }
    }
}
