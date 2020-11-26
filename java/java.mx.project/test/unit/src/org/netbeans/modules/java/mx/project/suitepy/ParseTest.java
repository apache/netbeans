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
package org.netbeans.modules.java.mx.project.suitepy;

import java.net.URL;
import java.util.Map;
import org.junit.Assume;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.util.RequestProcessor;

public class ParseTest extends NbTestCase {

    public ParseTest(String n) {
        super(n);
    }

    public static junit.framework.Test suite() {
        return NbModuleSuite.emptyConfiguration().
                        gui(false).
                        addTest(ParseTest.class).
                        suite();
    }

    public void testParse() throws Exception {
        try {
            MxSuite.parse(null);
        } catch (IllegalStateException | InternalError ex) {
            Assume.assumeNoException("This test has to run on GraalVM", ex);
        }

        URL u = ParseTest.class.getResource("compiler-suite.py");
        MxSuite mxSuite = MxSuite.parse(u);
        assertEquals("version", "5.124.0", mxSuite.mxversion());
        assertEquals("name", "compiler", mxSuite.name());

        StringBuilder libSb = new StringBuilder();
        int libCount = 0;
        for (Map.Entry<String, MxLibrary> entry : mxSuite.libraries().entrySet()) {
            final String libName = entry.getKey();
            final MxLibrary lib = entry.getValue();

            libSb.append("  libs: ").append(libName).append(" sha1: ").append("\n" + lib.sha1());
            for (String url : lib.urls()) {
                libSb.append("    url: ").append(url).append("\n");
            }
            libCount++;
        }
        assertEquals(libSb.toString(), 11, libCount);
        assertEquals(4, mxSuite.libraries().get("LLVM_PLATFORM_SPECIFIC").os_arch().size());

        StringBuilder prjSb = new StringBuilder();
        int prjCount = 0;
        for (Map.Entry<String, MxProject> entry : mxSuite.projects().entrySet()) {
            final String prjName = entry.getKey();
            final MxProject prj = entry.getValue();

            prjSb.append("  prj: ").append(prjName).append(" src: ").append(prj.sourceDirs()).append("\n");
            for (String url : prj.dependencies()) {
                prjSb.append("    dep: ").append(url).append("\n");
            }
            prjCount++;
        }
        assertEquals(prjSb.toString(), 83, prjCount);

        StringBuilder distSb = new StringBuilder();
        int distCount = 0;
        for (Map.Entry<String, MxDistribution> entry : mxSuite.distributions().entrySet()) {
            final String distName = entry.getKey();
            final MxDistribution dist = entry.getValue();
            distSb.append("  dist: ").append(distName).append(" dist: ").append(dist.distDependencies()).append("\n");
            for (String url : dist.dependencies()) {
                distSb.append("    dep: ").append(url).append("\n");
            }
            distCount++;
        }
        assertEquals(distSb.toString(), 19, distCount);

        MxSuite inBackground = RequestProcessor.getDefault().submit(() -> {
            return MxSuite.parse(u);
        }).get();

        assertEquals("version", "5.124.0", inBackground.mxversion());
        assertEquals("name", "compiler", inBackground.name());

    }

}
