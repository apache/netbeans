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
package org.netbeans.modules.cnd.remote.mapper;

import java.util.Collection;
import java.util.List;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Lookup;

/**
 *
 */
abstract public  class FixRemotePathMapper {
    private static final FixRemotePathMapper DEFAULT = new FixRemotePathMapperDefault();

    
    public static FixRemotePathMapper getInstance() {
        if (CndUtils.isStandalone() || CndUtils.isUnitTestMode()) {
            return DEFAULT;
        }
        Collection<? extends FixRemotePathMapper> notifiers = Lookup.getDefault().lookupAll(FixRemotePathMapper.class);
        if (notifiers.isEmpty()) {
            return DEFAULT;
        }
        return notifiers.iterator().next();
    }
    
    abstract public boolean fixRemotePath(ExecutionEnvironment execEnv, List<String> invalidLocalPaths);

    private static class FixRemotePathMapperDefault extends FixRemotePathMapper {

        public FixRemotePathMapperDefault() {
        }

        @Override
        public boolean fixRemotePath(ExecutionEnvironment execEnv, List<String> invalidLocalPaths) {
            return false;
        }
        
    }

    
}
