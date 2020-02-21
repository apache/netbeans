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
package org.netbeans.modules.cnd.spi.toolchain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerSetManagerImpl;
import org.netbeans.modules.cnd.toolchain.compilerset.ToolchainValidator;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class CSMNotifier {

    private static final CSMNotifier DEFAULT = new CSMNotifierDefault();

    
    public static CSMNotifier getInstance() {
        if (CndUtils.isStandalone() || CndUtils.isUnitTestMode()) {
            return DEFAULT;
        }
        Collection<? extends CSMNotifier> notifiers = Lookup.getDefault().lookupAll(CSMNotifier.class);
        if (notifiers.isEmpty()) {
            return DEFAULT;
        }
        return notifiers.iterator().next();
    }

    abstract public void notifyNoCompilerSet(String message);
    
    abstract public void showNotification(final Map<Tool, List<List<String>>> needReset, final CompilerSetManager csm);
    
    public static void applyChanges(final Map<Tool, List<List<String>>> needReset, final CompilerSetManager csm) {
        ToolchainValidator.INSTANCE.applyChanges(needReset, (CompilerSetManagerImpl)csm);
    }

    private static class CSMNotifierDefault extends CSMNotifier {

        public CSMNotifierDefault() {
        }

        @Override
        public void notifyNoCompilerSet(String message) {
            System.err.println(message);
        }

        @Override
        public void showNotification(Map<Tool, List<List<String>>> needReset, CompilerSetManager csm) {
        }

    }

}
