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
package org.netbeans.api.java.source.gen;

import java.io.IOException;
import java.util.List;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileStateInvalidException;

/**
 * Tests more transaction on file with fields. Tests fields generating and
 * also update.
 *
 * @author  Pavel Flaska
 */
public class FieldTest3 extends GeneratorTestBase {

    /** Creates a new instance of FieldTest3 */
    public FieldTest3(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(FieldTest3.class);
        return suite;
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "FieldTest3.java");
    }

    /**
     * Tests inital value for fields in field group.
     */
    public void testGroupInitValues() throws IOException {
        System.err.println("testGroupInitValues");
/*            FieldGroup group = (FieldGroup) clazz.getContents().get(2);
            List fields = group.getFields();
            ((Field) fields.get(0)).setInitialValueText("\"prvni\"");
            ((Field) fields.get(2)).setInitialValueText("\"treti\"");*/
        assertFiles("testGroupInitValues_FieldTest3.pass");
    }
    
    /**
     * Tests group separation
     */
    public void testGroupSeparation() throws IOException {
        System.err.println("testGroupSeparation");
  /*          Field second = (Field) clazz.getFeatures().remove(3);
            Field third = (Field) clazz.getFeatures().remove(3);
            clazz.getFeatures().add(second);
            clazz.getFeatures().add(third);*/
        assertFiles("testGroupSeparation_FieldTest3.pass");
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
    String getSourcePckg() {
        return "org/netbeans/test/codegen/";
    }

    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/FieldTest3/";
    }
}
