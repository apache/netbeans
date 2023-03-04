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

package org.netbeans.modules.projectimport.eclipse.core;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Martin Krauskopf
 */
public class PreferredVMParserTest extends ProjectImporterTestCase {

    public PreferredVMParserTest(String testName) {
        super(testName);
    }

    /** Also test 57661. */
    public void testParse() throws ProjectImporterException {
        String org_eclipse_jdt_launching_PREF_VM_XML =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<vmSettings defaultVM=\"57,org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType13,1135246830946\" defaultVMConnector=\"\">\n" +
                "<vmType id=\"org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType\">\n" +
                "<vm id=\"0\" name=\"jdk-6-beta-bin-b59c\" path=\"/space/java/jdk-6-beta-bin-b59c\"/>\n" +
                "<vm id=\"1135246830946\" name=\"jdk-6-rc-bin-b64\" path=\"/space/java/jdk-6-rc-bin-b64\">\n" +
                "<libraryLocations>\n" +
                "<libraryLocation jreJar=\"/space/java/0_lib/commons-collections-2.1.jar\" jreSrc=\"\" pkgRoot=\"\"/>\n" +
                "<libraryLocation jreJar=\"/space/java/jdk-6-rc-bin-b64/jre/lib/resources.jar\" jreSrc=\"/space/java/jdk-6-rc-bin-b64/src.zip\" pkgRoot=\"\"/>\n" +
                "<libraryLocation jreJar=\"/space/java/jdk-6-rc-bin-b64/jre/lib/rt.jar\" jreSrc=\"/space/java/jdk-6-rc-bin-b64/src.zip\" pkgRoot=\"\"/>\n" +
                "<libraryLocation jreJar=\"/space/java/jdk-6-rc-bin-b64/jre/lib/jsse.jar\" jreSrc=\"/space/java/jdk-6-rc-bin-b64/src.zip\" pkgRoot=\"\"/>\n" +
                "<libraryLocation jreJar=\"/space/java/jdk-6-rc-bin-b64/jre/lib/jce.jar\" jreSrc=\"/space/java/jdk-6-rc-bin-b64/src.zip\" pkgRoot=\"\"/>\n" +
                "<libraryLocation jreJar=\"/space/java/jdk-6-rc-bin-b64/jre/lib/charsets.jar\" jreSrc=\"/space/java/jdk-6-rc-bin-b64/src.zip\" pkgRoot=\"\"/>\n" +
                "<libraryLocation jreJar=\"/space/java/jdk-6-rc-bin-b64/jre/lib/ext/sunjce_provider.jar\" jreSrc=\"\" pkgRoot=\"\"/>\n" +
                "<libraryLocation jreJar=\"/space/java/jdk-6-rc-bin-b64/jre/lib/ext/sunpkcs11.jar\" jreSrc=\"\" pkgRoot=\"\"/>\n" +
                "<libraryLocation jreJar=\"/space/java/jdk-6-rc-bin-b64/jre/lib/ext/dnsns.jar\" jreSrc=\"\" pkgRoot=\"\"/>\n" +
                "<libraryLocation jreJar=\"/space/java/jdk-6-rc-bin-b64/jre/lib/ext/localedata.jar\" jreSrc=\"\" pkgRoot=\"\"/>\n" +
                "</libraryLocations>\n" +
                "</vm>\n" +
                "</vmType>\n" +
                "</vmSettings>\n";
        
        Map<String,String> jdks = PreferredVMParser.parse(org_eclipse_jdt_launching_PREF_VM_XML);
        
        Map<String,String> expectedJDKs = new HashMap<String,String>();
        expectedJDKs.put("jdk-6-rc-bin-b64", "/space/java/jdk-6-rc-bin-b64");
        expectedJDKs.put("org.eclipse.jdt.launching.JRE_CONTAINER", "/space/java/jdk-6-rc-bin-b64");
        expectedJDKs.put("jdk-6-beta-bin-b59c", "/space/java/jdk-6-beta-bin-b59c");
        
        assertEquals("JDKs were successfully parsed", expectedJDKs, jdks);
    }
    
}
