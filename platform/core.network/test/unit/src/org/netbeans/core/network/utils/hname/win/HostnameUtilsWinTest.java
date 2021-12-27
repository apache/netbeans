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
package org.netbeans.core.network.utils.hname.win;

import com.sun.jna.Platform;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Hector Espert
 */
public class HostnameUtilsWinTest extends NbTestCase {

    public HostnameUtilsWinTest(String name) {
        super(name);
    }

    @Override
    public boolean canRun() {
        return super.canRun() && Platform.isWindows();
    }

    public void testGetComputerName() throws Exception {
        assertNotNull(HostnameUtilsWin.getComputerName());
    }

    public void testGetHostName() throws Exception {
        assertNotNull(HostnameUtilsWin.getHostName(true));
        assertNotNull(HostnameUtilsWin.getHostName(false));
    }

}
