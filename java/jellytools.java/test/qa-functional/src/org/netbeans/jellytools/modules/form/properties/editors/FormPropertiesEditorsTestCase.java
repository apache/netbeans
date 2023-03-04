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
package org.netbeans.jellytools.modules.form.properties.editors;

import java.io.IOException;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.nodes.FormNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.junit.NbTestSuite;

/**
 * Common ancestor for all tests in package
 * org.netbeans.jellytools.modules.form.properties.editors.
 *
 * @author Jiri Skrivanek
 */
public class FormPropertiesEditorsTestCase extends JellyTestCase {

    /**
     * Method used for explicit testsuite definition
     *
     * @return created suite
     */
    public static NbTestSuite suite() {
        return null;
    }

    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public FormPropertiesEditorsTestCase(String testName) {
        super(testName);
    }

    /** Opens sample form, property sheet for Form node and custom editor for title property. */
    @Override
    protected void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
        if (fceo == null) {
            Node sample1 = new Node(new SourcePackagesNode("SampleProject"), "sample1");  // NOI18N
            FormNode node = new FormNode(sample1, SAMPLE_FRAME_NAME);
            node.open();
            // wait for form opened
            FormDesignerOperator fdo = new FormDesignerOperator(SAMPLE_FRAME_NAME);
            // open and close general properties
            new PropertiesAction().perform();
            new PropertySheetOperator().close();
            ComponentInspectorOperator.invokeNavigator();
            ComponentInspectorOperator inspector = new ComponentInspectorOperator();
            PropertySheetOperator pso = inspector.properties("[JFrame]"); // NOI18N
            Property p = new Property(pso, PROPERTY_NAME);
            p.openEditor();
            fceo = new FormCustomEditorOperator(PROPERTY_NAME);
        }
    }
    protected static final String SAMPLE_FRAME_NAME = "JFrameSample"; // NOI18N
    protected static final String PROPERTY_NAME = "title"; // NOI18N
    protected static FormCustomEditorOperator fceo;
}
