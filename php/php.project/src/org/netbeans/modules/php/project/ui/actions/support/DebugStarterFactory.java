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
package org.netbeans.modules.php.project.ui.actions.support;

import org.netbeans.modules.php.spi.executable.DebugStarter;
import org.openide.util.Lookup;
import org.openide.util.Union2;


/**
 * @author Radek Matous
 *
 */
public final class DebugStarterFactory {
    private static Union2<DebugStarter, Boolean> INSTANCE;

    private DebugStarterFactory() {
    }

    public static DebugStarter getInstance() {
        boolean init;
        synchronized (DebugStarterFactory.class) {
            init = (INSTANCE == null);
        }
        if (init) {
            //TODO add lookup listener
            DebugStarter debugStarter = Lookup.getDefault().lookup(DebugStarter.class);
            if (debugStarter != null) {
                INSTANCE = Union2.createFirst(debugStarter);
            } else {
                INSTANCE = Union2.createSecond(Boolean.FALSE);
            }
        }
        return INSTANCE.hasFirst() ? INSTANCE.first() : null;
    }
}
