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

package org.netbeans.modules.ant.freeform.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.modules.ant.freeform.FreeformProject;
import org.netbeans.modules.ant.freeform.FreeformProjectGenerator;
import org.netbeans.modules.ant.freeform.FreeformProjectType;
import org.netbeans.modules.ant.freeform.TestBase;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 * Test non-GUI functionality of the unbound target alert: binding creation etc.
 * @author Jesse Glick
 */
public class UnboundTargetAlertTest extends TestBase {
    
    public UnboundTargetAlertTest(String name) {
        super(name);
    }

    private FreeformProject prj;
    private UnboundTargetAlert uta;
    
    protected void setUp() throws Exception {
        super.setUp();
        prj = copyProject(simple);
        uta = new UnboundTargetAlert(prj, "debug");
    }
    
    public void testGenerateBindingAndAddContextMenuItem() throws Exception {
        uta.simulateTargetSelection("twiddle-this");
        uta.generateBindingAndAddContextMenuItem();
        List<FreeformProjectGenerator.TargetMapping> mappings = FreeformProjectGenerator.getTargetMappings(prj.helper());
        // Will add it to the end, so just look there.
        FreeformProjectGenerator.TargetMapping lastMapping = mappings.get(mappings.size() - 1);
        assertEquals("debug", lastMapping.name);
        assertEquals(null, lastMapping.script);
        assertEquals(Collections.singletonList("twiddle-this"), lastMapping.targets);
        assertEquals(null, lastMapping.properties);
        assertEquals(null, lastMapping.context);
        // Check also making a binding for multiple targets, which is permitted.
        mappings.remove(lastMapping);
        FreeformProjectGenerator.putTargetMappings(prj.helper(), mappings);
        uta.simulateTargetSelection("  twiddle-this extra-step ");
        uta.generateBindingAndAddContextMenuItem();
        mappings = FreeformProjectGenerator.getTargetMappings(prj.helper());
        lastMapping = mappings.get(mappings.size() - 1);
        assertEquals("debug", lastMapping.name);
        assertEquals(null, lastMapping.script);
        assertEquals(Arrays.asList("twiddle-this", "extra-step"), lastMapping.targets);
        assertEquals(null, lastMapping.properties);
        assertEquals(null, lastMapping.context);
        // Also check the context menu.
        Element data = prj.getPrimaryConfigurationData();
        Element view = XMLUtil.findElement(data, "view", FreeformProjectType.NS_GENERAL);
        assertNotNull(view);
        Element contextMenu = XMLUtil.findElement(view, "context-menu", FreeformProjectType.NS_GENERAL);
        assertNotNull(contextMenu);
        Set<String> actionNames = new TreeSet<String>();
        for (Element action : XMLUtil.findSubElements(contextMenu)) {
            if (action.getLocalName().equals("ide-action")) {
                actionNames.add(action.getAttribute("name"));
            }
        }
        assertEquals("Correct context menu IDE actions",
            new TreeSet<String>(Arrays.asList("build", "clean", "rebuild", "run", "javadoc", /*added*/ "debug")),
            actionNames);
    }
    
}
