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
package org.netbeans.modules.cnd.makeproject.spi;

import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandlerFactory;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class DebuggerChooserProvider {
    
    private static final DebuggerChooserProvider EMPTY = new Empty();

    protected DebuggerChooserProvider() {
    }

    public abstract String[] getNames();
    public abstract String getName(int i);
    public abstract int getDefault();
    public abstract int getNodesSize();
    public abstract ProjectActionHandlerFactory getNode(int i);
    
    
    /** Static method to obtain the resolver.
     * @return the resolver
     */
    public static DebuggerChooserProvider getInstance() {
        DebuggerChooserProvider storage = Lookup.getDefault().lookup(DebuggerChooserProvider.class);
        return storage == null ? EMPTY : storage;
    }
    
    private static final class Empty extends DebuggerChooserProvider {
        private Empty() {
        }

        @Override
        public String[] getNames() {
            return new String[0];
        }

        @Override
        public String getName(int i) {
            return "???"; // NOI18N
        }

        @Override
        public int getDefault() {
            return 0;
        }

        @Override
        public int getNodesSize() {
            return 0;
        }

        @Override
        public ProjectActionHandlerFactory getNode(int i) {
            return null;
        }
    };
}
