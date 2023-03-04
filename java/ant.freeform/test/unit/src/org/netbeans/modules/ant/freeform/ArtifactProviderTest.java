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

package org.netbeans.modules.ant.freeform;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test {@link ArtifactProvider}.
 * @author Jesse Glick
 */
public class ArtifactProviderTest extends TestBase {
    
    public ArtifactProviderTest(String name) {
        super(name);
    }
    
    public void testBuildArtifact() throws Exception {
        File mainJar = simple.helper().resolveFile("build/simple-app.jar");
        AntArtifact aa = AntArtifactQuery.findArtifactFromFile(mainJar);
        assertNotNull("have artifact for " + mainJar, aa);
        verifyArtifact(aa);
        // Could be different instances returned each time, so check each one:
        aa = AntArtifactQuery.findArtifactByID(simple, "jar");
        assertNotNull("found artifact by ID", aa);
        verifyArtifact(aa);
        AntArtifact[] aas = AntArtifactQuery.findArtifactsByType(simple, "jar");
        assertEquals("found one 'jar' artifact", 1, aas.length);
        verifyArtifact(aas[0]);
    }
    
    private void verifyArtifact(AntArtifact aa) {
        assertEquals("right project", simple, aa.getProject());
        assertEquals("right location", URI.create("build/simple-app.jar"), aa.getArtifactLocations()[0]);
        assertEquals("right target", "jar", aa.getTargetName());
        assertEquals("right clean target", "clean", aa.getCleanTargetName());
        // ID should be target name if that does not cause a conflict
        assertEquals("right ID", "jar", aa.getID());
        assertEquals("right type", "jar", aa.getType());
        assertEquals("right script", simple.helper().resolveFile("build.xml"), aa.getScriptLocation());
    }
    
    public void testGetBuildArtifacts() throws Exception {
        FreeformProject simple_ = copyProject(simple);
        AntProjectHelper helper = simple_.helper();
        List<Export> exports = new ArrayList<Export>();
        Export e = new Export();
        e.type = "jar";
        e.location = "path/smth.jar";
        e.script = "someScript";
        e.buildTarget = "build_target";
        exports.add(e);
        putExports(helper, exports);
        AntArtifact[] aa = AntArtifactQuery.findArtifactsByType(simple_, "jar");
        assertNotNull("some artifact found", aa);
        assertEquals("one artifact found", 1, aa.length);

        e = new Export();
        e.type = "jar";
        e.location = "path/smth.jar";
        e.script = "someScript";
        e.buildTarget = "build_target2";
        exports.add(e);
        putExports(helper, exports);
        aa = AntArtifactQuery.findArtifactsByType(simple_, "jar");
        assertNotNull("some artifact found", aa);
        assertEquals("two artifacts found", 2, aa.length);

        // one type/target/script produces two outputs -> no AA
        e = new Export();
        e.type = "jar";
        e.location = "path/smth2.jar";
        e.script = "someScript";
        e.buildTarget = "build_target2";
        exports.add(e);
        putExports(helper, exports);
        aa = AntArtifactQuery.findArtifactsByType(simple_, "jar");
        assertNotNull("some artifact found", aa);
        assertEquals("two artifacts found", 2, aa.length);
        
        exports.remove(0);
        putExports(helper, exports);
        aa = AntArtifactQuery.findArtifactsByType(simple_, "jar");
        assertNotNull("some artifact found", aa);
        assertEquals("one artifact found", 1, aa.length);
        assertEquals("the artifact has two locations", 2, aa[0].getArtifactLocations().length);
        // XXX test ordering perhaps? not critical
    }

    private static void putExports(AntProjectHelper helper, List<Export> exports) {
        //assert ProjectManager.mutex().isWriteAccess();
        Element data = Util.getPrimaryConfigurationData(helper);
        Document doc = data.getOwnerDocument();
        for (Element exportEl : XMLUtil.findSubElements(data)) {
            if (!exportEl.getLocalName().equals("export")) { // NOI18N
                continue;
            }
            data.removeChild(exportEl);
        }
        for (Export export : exports) {
            Element exportEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "export"); // NOI18N
            Element el;
            el = doc.createElementNS(FreeformProjectType.NS_GENERAL, "type"); // NOI18N
            el.appendChild(doc.createTextNode(export.type)); // NOI18N
            exportEl.appendChild(el);
            el = doc.createElementNS(FreeformProjectType.NS_GENERAL, "location"); // NOI18N
            el.appendChild(doc.createTextNode(export.location)); // NOI18N
            exportEl.appendChild(el);
            if (export.script != null) {
                el = doc.createElementNS(FreeformProjectType.NS_GENERAL, "script"); // NOI18N
                el.appendChild(doc.createTextNode(export.script)); // NOI18N
                exportEl.appendChild(el);
            }
            el = doc.createElementNS(FreeformProjectType.NS_GENERAL, "build-target"); // NOI18N
            el.appendChild(doc.createTextNode(export.buildTarget)); // NOI18N
            exportEl.appendChild(el);
            if (export.cleanTarget != null) {
                el = doc.createElementNS(FreeformProjectType.NS_GENERAL, "clean-target"); // NOI18N
                el.appendChild(doc.createTextNode(export.cleanTarget)); // NOI18N
                exportEl.appendChild(el);
            }
            Element later = XMLUtil.findElement(data, "view", FreeformProjectType.NS_GENERAL);
            if (later == null) {
                later = XMLUtil.findElement(data, "subprojects", FreeformProjectType.NS_GENERAL);
            }
            data.insertBefore(exportEl, later);
        }
        Util.putPrimaryConfigurationData(helper, data);
    }

    private static final class Export {
        public String type;
        public String location;
        public String script; // optional
        public String buildTarget;
        public String cleanTarget; // optional
    }
    
}
