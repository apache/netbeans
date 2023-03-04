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

package org.netbeans.modules.xml.schema.model;
import java.beans.PropertyChangeEvent;
import junit.framework.*;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.text.Document;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
/*
 * SchemaModelTest.java
 * JUnit based test
 *
 * Created on October 3, 2005, 3:51 PM
 */

/**
 *
 * @author nn136682
 */
public class SchemaModelTest extends TestCase {
    
    private static String TEST_XSD = "resources/testInclude.xsd";
    private static String TEST_BAD_XSD = "resources/testBad.xsd";
    private static String TEST_BAD_INCLUDE_XSD = "resources/testBadInclude.xsd";
    private static String TEST_TYPES_XSD = "resources/testTypes.xsd";
    private static String TEST_FAKE_XSD = "resources/fakeSchema.xsd";
    
    public SchemaModelTest(String testName) {
        super(testName);
    }
    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new SchemaModelTest("testGetSchema"));
        suite.addTest(new SchemaModelTest("testGetState"));
        suite.addTest(new SchemaModelTest("testTypes"));
        suite.addTest(new SchemaModelTest("testVersionChangedListener"));
        suite.addTest(new SchemaModelTest("testFakeSchema"));
        suite.addTest(new SchemaModelTest("testBadInclude"));
        suite.addTest(new SchemaModelTest("testCircularInclude"));        
        suite.addTest(new SchemaModelTest("testFlushDumpThenReload"));                
        return suite;
    }

    /**
     * Test of flush method, of class org.netbeans.modules.xml.schema.model.api.SchemaModel.
     */
    public void testFlushDumpThenReload() throws Exception {
        SchemaModel sm = Util.loadSchemaModel(TEST_XSD);
        assertNotNull(sm);
        Document doc = AbstractDocumentModel.class.cast(sm).getBaseDocument();
        assertNotNull(doc);
        Schema schema = sm.getSchema();
        assertNotNull(schema);
        String current = schema.getVersion();
        sm.startTransaction();
        schema.setVersion("1.3");
        sm.endTransaction();
	assertEquals("1.3", sm.getSchema().getVersion());
	Document d = (Document) sm.getModelSource().getLookup().lookup(Document.class);
	assertSame(doc,d);
	
	//System.out.println(d.getText(0, d.getLength()));
        SchemaModel sm2 = Util.dumpAndReloadModel(sm);
	d = (Document) sm2.getModelSource().getLookup().lookup(Document.class);
        //System.out.println(d.getText(0, d.getLength()));
       
        assertFalse("testSync", sm2.getSchema().getVersion().equals(current));
        assertEquals("1.3", sm2.getSchema().getVersion());
    }

    /**
     * Test of getSchema method, of class org.netbeans.modules.xmlschema.api.SchemaModel.
     */
    public void testGetSchema() throws Exception {
        SchemaModel sm = Util.loadSchemaModel(TEST_XSD);
        assertNotNull(sm);
        assert(!sm.isEmbedded());
    	String uri = sm.getSchema().getTargetNamespace();
        String expectUri = "http://www.example.com/testInclude";
        assertEquals(expectUri, uri);
    }

    /**
     * Test of getState method, of class org.netbeans.modules.xmlschema.api.SchemaModel.
     */
    public void testGetState() throws Exception {
        SchemaModel sm = Util.loadSchemaModel(TEST_XSD);
        State s1 = sm.getState();
        State expResult1 = State.VALID;
        assertEquals(expResult1, s1);

        SchemaModel sm2 = Util.loadSchemaModel(TEST_BAD_XSD);
        assertEquals("Expect not well-formed source", State.NOT_WELL_FORMED, sm2.getState());
    }
	
    /**
     * Test of getState method, of class org.netbeans.modules.xmlschema.api.SchemaModel.
     */
    public void testBadInclude() throws Exception {
        SchemaModel sm = Util.loadSchemaModel(TEST_XSD);
        State s1 = sm.getState();
        State expResult1 = State.VALID;
        assertEquals(expResult1, s1);
        SchemaModel sm2 = Util.loadSchemaModel(TEST_BAD_INCLUDE_XSD);
        assertEquals("Expect not well-formed source", State.NOT_WELL_FORMED, sm2.getState());
    }

    private class Listener implements PropertyChangeListener {
        private String expectedEvent;
        private boolean gotIt = false;
        
        public Listener(String expected) {
            expectedEvent = expected;
            
        }
        public void propertyChange(PropertyChangeEvent evt) {
            assertNotNull(evt);
            System.out.println("Got " + evt.getPropertyName());
            assertEquals(expectedEvent, evt.getPropertyName());
            gotIt = true;
        }
        public boolean gotIt() { return gotIt; }
        public void resetGotIt() { gotIt = false; }
    }
    
    /**
     * Test of addPropertyChangeListener method, of class org.netbeans.modules.xmlschema.api.SchemaModel.
     */
    public void testVersionChangedListener() throws Exception {
        SchemaModel sm = Util.loadSchemaModel(TEST_XSD);
        Listener pcl = new Listener(Schema.VERSION_PROPERTY);
        sm.addPropertyChangeListener(pcl);
        sm.startTransaction();
        sm.getSchema().setVersion("1.0");
        sm.endTransaction();
        assertTrue("test VersionChanged event", pcl.gotIt());
        
        pcl.resetGotIt();
        sm.removePropertyChangeListener(pcl);
        sm.startTransaction();
        sm.getSchema().setVersion("1.1");
        sm.endTransaction();
        assertFalse("test remove listener, no more events", pcl.gotIt());
    }
    
    public void testCircularInclude() throws Exception {
            SchemaModel sm = TestCatalogModel.getDefault().getSchemaModel(NamespaceLocation.TEST_INCLUDE);
            if (! NamespaceLocation.SOMEFILE.getResourceFile().exists()) {
                NamespaceLocation.SOMEFILE.refreshResourceFile();
            }
            Collection<Schema> schemas = sm.findSchemas("http://www.example.com/testInclude");
            ArrayList<Schema> list = new ArrayList<Schema>(schemas);
            assertEquals("circular include is ok", 2, schemas.size());
            assertTrue("2 distinct schema", list.get(1) != list.get(0));
            assertEquals("http://www.example.com/testInclude", list.get(0).getTargetNamespace());
    }
    
    public void testTypes() throws Exception {
        SchemaModel sm = Util.loadSchemaModel(TEST_TYPES_XSD);
        GlobalComplexType gct = (GlobalComplexType)sm.getSchema().getChildren().get(0);
        ComplexTypeDefinition ctd = gct.getDefinition();
        ComplexExtension ce = (ComplexExtension)ctd.getChildren().get(0);
        NamedComponentReference<GlobalType> ncr = ce.getBase();
        GlobalType type = ncr.get();
        assert(type instanceof GlobalSimpleType);
        GlobalSimpleType gst = (GlobalSimpleType)type;
        assert(gst.getName() != null && gst.getName().equals("anyType"));
    }

    public void testFakeSchema() throws Exception {
        SchemaModel sm = Util.loadSchemaModel(TEST_FAKE_XSD);
        assert(sm.getState() == State.NOT_WELL_FORMED);
    }
    
}
