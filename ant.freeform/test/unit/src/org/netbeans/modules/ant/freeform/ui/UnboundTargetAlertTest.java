/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
