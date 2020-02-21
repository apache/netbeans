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
package org.netbeans.modules.cnd.makeproject.uiapi;

import org.openide.util.Lookup;

/**
 *
 */
public abstract class RunActionSupport {
    public abstract boolean canRun(Lookup context);
    public abstract void run(Lookup context);
    
    private static final Default DEFAULT = new Default();

    public static RunActionSupport geRunActionSupport() {
        RunActionSupport defaultFactory = Lookup.getDefault().lookup(RunActionSupport.class);
        return defaultFactory == null ? DEFAULT : defaultFactory;
    }
    
    private static final class Default extends RunActionSupport {

        @Override
        public boolean canRun(Lookup context) {
            return false;
        }

        @Override
        public void run(Lookup context) {
        }
    }
}
