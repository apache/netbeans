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

package org.netbeans.modules.cnd.discovery.project.cases;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.netbeans.modules.cnd.discovery.project.MakeProjectTestBase;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.util.Utilities;

/**
 *
 */
public class QT_QLifeTestCase extends MakeProjectTestBase {

    public QT_QLifeTestCase() {
        super("QLife");
    }

    @Override
    protected List<String> requiredTools() {
        List<String> res = new ArrayList<>(super.requiredTools());
        res.add("qmake");
        res.add("sed");
        return res;
    }

    @Test
    public void testQLife() throws Exception {
        List<String> list = new ArrayList<>();
        //list.add("qmake qlife.pro");
        if (Utilities.isWindows()) {
            // There are troubles with generated Makefile on Windows - attempt to fix it.
            // Note: MSYS make is required to run patched Makefile.
            list.add("sed -e 's:\\\\\\(.\\):/\\1:g' -i Makefile");
            list.add("sed -e 's:\\\\\\(.\\):/\\1:g' -i Makefile.Debug");
            list.add("sed -e 's:\\\\\\(.\\):/\\1:g' -i Makefile.Release");
            // does not work on windows
            return;
        }
        HostInfo hostInfo = HostInfoUtils.getHostInfo(getEE());
        if (hostInfo.getOSFamily() == HostInfo.OSFamily.SUNOS) {
            //Solaris is not desctop system. Impossible to port Qt on Solaris.
            return;
        }
        boolean useStudio = false;
        if (hostInfo.getOSFamily() == HostInfo.OSFamily.SUNOS && hostInfo.getCpuFamily() == HostInfo.CpuFamily.SPARC) {
            useStudio = true;
        }
        performTestProject("http://personal.inet.fi/koti/rkauppila/projects/life/qlife-qt4-0.9.tar.gz", list, useStudio, "");
    }
}
