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

package org.netbeans.modules.cnd.modelimpl.fsm;

import java.io.IOException;
import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.modelimpl.csm.ParameterListImpl;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 */
public final class DummyParametersListImpl extends ParameterListImpl<DummyParametersListImpl, CsmParameter> {

    private DummyParametersListImpl(CsmFile file, int start, int end, Collection<CsmParameter> parameters) {
        super(file, start, end, parameters);
    }

    public static DummyParametersListImpl create(CsmFile file, int start, int end, Collection<CsmParameter> parameters) {
        DummyParametersListImpl dummyParametersListImpl = new DummyParametersListImpl(file, start, end, parameters);
        return dummyParametersListImpl;
    }

    @Override
    public String toString() {
        return "Dummy " + super.toString(); // NOI18N
    }

    ////////////////////////////////////////////////////////////////////////////
    // persistent
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
    }

    @SuppressWarnings("unchecked")
    public DummyParametersListImpl(RepositoryDataInput input, CsmScope scope) throws IOException {
        super(input, scope);
    }

}
