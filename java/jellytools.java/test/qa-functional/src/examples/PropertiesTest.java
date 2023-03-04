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
package examples;

import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jellytools.properties.editors.StringCustomEditorOperator;

public class PropertiesTest extends JellyTestCase {
    
    /** Constructor required by JUnit */
    public PropertiesTest(String testName) {
        super(testName);
    }

    /** Creates suite from particular test cases. */
    public static Test suite() {
        return createModuleTest(PropertiesTest.class);
    }

    @Override
    public void setUp() throws Exception {
        System.out.println("########  " + getName() + "  #######");
        openDataProjects("SampleProject");
    }

    public void testProperties() {
        Node node = new Node(new SourcePackagesNode("SampleProject"), "sample1|properties.properties|mykey");
        // open property sheet
        new PropertiesAction().perform(node);
        // find property sheet with given title
        PropertySheetOperator pso = new PropertySheetOperator("mykey");
        // find property Value
        Property p = new Property(pso, "Value");
        // get name and value
        System.out.println("PROPERTY: "+p.getName()+"="+p.getValue());
        // set new value
        p.setValue("new value");
        // open custom editor for property (...)
        p.openEditor();
        // find custom editor
        StringCustomEditorOperator customEditor = new StringCustomEditorOperator("Value");
        // get value from custom editor
        System.out.println("Value="+customEditor.getStringValue());
        // set value
        customEditor.setStringValue("new value1");
        // confirm custom editor dialog
        customEditor.ok();
        // close property sheet
        pso.close();
    }
}
