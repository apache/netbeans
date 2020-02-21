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

package org.netbeans.modules.cnd.apt.support;

import org.netbeans.modules.cnd.apt.support.spi.APTFileSearchImplementation;
import org.netbeans.modules.cnd.apt.support.spi.APTProjectFileSearchProvider;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.util.Lookup;

/**
 *
 */
public final class APTFileSearch {

    private final APTFileSearchImplementation impl;

    private APTFileSearch(APTFileSearchImplementation impl) {
        this.impl = impl;
    }

    public static APTFileSearch get(Key prjKey) {
        APTProjectFileSearchProvider provider = Lookup.getDefault().lookup(APTProjectFileSearchProvider.class);
        if (provider != null) {
            APTFileSearchImplementation impl = provider.getSearchImplementation(prjKey);
            if (impl != null) {
                return new APTFileSearch(impl);
            }
        }
        return null;
    }

    public FSPath searchInclude(String include, CharSequence basePath) {
        return impl.searchInclude(include, basePath);
    }

    @Override
    public String toString() {
        return "APTFileSearch{" + "impl=" + impl + '}'; // NOI18N
    }
    
    
}
