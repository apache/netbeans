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

package org.netbeans.modules.debugger.jpda.truffle;

import java.beans.PropertyChangeListener;
import java.net.URL;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.truffle.access.CurrentPCInfo;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.source.Source;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;

/**
 * Recognizes suspended location in a GraalVM guest language script.
 */
@SourcePathProvider.Registration(path = "netbeans-JPDASession")
public class FirstSourceURLProvider extends SourcePathProvider {
    
    private static final String[] NO_SOURCE_ROOTS = new String[]{};
    
    private static final String TRUFFLE_ACCESSOR_CLASS_NAME =
            "org.netbeans.modules.debugger.jpda.backend.truffle.JPDATruffleAccessor"; // NOI18N
    private static final String TRUFFLE_ACCESSOR_PATH =
            "org/netbeans/modules/debugger/jpda/backend/truffle/JPDATruffleAccessor"; // NOI18N
    
    private final JPDADebugger debugger;
    
    public FirstSourceURLProvider(ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, JPDADebugger.class);
    }

    @Override
    public String getURL(String relativePath, boolean global) {
        if (TRUFFLE_ACCESSOR_PATH.equals(relativePath)) {
            return getCurrentURL();
        }
        return null;
    }
    
    public String getURL(JPDAClassType clazz, String stratum) {
        if (TRUFFLE_ACCESSOR_CLASS_NAME.equals(clazz.getName())) {
            return getCurrentURL();
        }
        return null;
    }

    private String getCurrentURL() {
        JPDAThread currentThread = debugger.getCurrentThread();
        CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentGuestPCInfo(currentThread);
        if (currentPCInfo != null) {
            Source source = currentPCInfo.getSourcePosition().getSource();
            if (source != null) {
                URL url = source.getUrl();
                if (url != null) {
                    return url.toExternalForm();
                }
            }
        }
        return null;
    }

    @Override
    public String getRelativePath(String url, char directorySeparator, boolean includeExtension) {
        return null;
    }

    @Override
    public String[] getSourceRoots() {
        return NO_SOURCE_ROOTS;
    }

    @Override
    public void setSourceRoots(String[] sourceRoots) {
    }

    @Override
    public String[] getOriginalSourceRoots() {
        return NO_SOURCE_ROOTS;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }
    
}
