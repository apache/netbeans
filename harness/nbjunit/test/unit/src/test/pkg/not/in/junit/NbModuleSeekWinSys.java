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

package test.pkg.not.in.junit;

import java.lang.reflect.Method;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;

public class NbModuleSeekWinSys extends NbTestCase {

    public NbModuleSeekWinSys(String t) {
        super(t);
    }

    public void testIsWinSysEnabled() throws Exception {
        Class<?> modInfo = Class.forName("org.openide.modules.ModuleInfo");
        Method isEnabled = modInfo.getMethod("isEnabled");
        Method codeNameBase = modInfo.getMethod("getCodeNameBase");
        for (Object o : Lookup.getDefault().lookupAll(modInfo)) {
            String cnb = (String)codeNameBase.invoke(o);
            if (cnb.equals("org.netbeans.core.windows")) {
                System.setProperty("winsys.on", isEnabled.invoke(o).toString());
                return;
            }
        }
        System.setProperty("winsys.on", "hidden");
    }
}
