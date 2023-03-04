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

package org.netbeans.modules.xml.wsdl.model.readwrite;

import java.util.Collection;
import java.util.Iterator;
import junit.framework.TestCase;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.TestCatalogModel;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.Util;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.impl.WSDLSchemaImpl;
import org.netbeans.modules.xml.wsdl.model.impl.WSDLComponentFactoryImpl;
import org.netbeans.modules.xml.wsdl.model.impl.WSDLModelImpl;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author rico
 */
public class SchemaReadWriteTest extends TestCase implements TestReadWrite{

    /** Creates a new instance of SchemaTest */
    public SchemaReadWriteTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }

    /**
     * Reconstruct the schema pointed by #getSchemaResourcePath from empty schema.
     */
    public void testWrite() throws Exception {
  
        WSDLModel model = Util.loadWSDLModel(getTestResourcePath2());
        WSDLComponentFactory fact = model.getFactory();
        this.assertNotNull(model);
        Definitions d = model.getDefinitions();
        this.assertNotNull(d);
        
        model.startTransaction();
        d.setName("StockQuote");
        d.setTargetNamespace("http://example.com/stockquote.wsdl");
        Types types = fact.createTypes();
        WSDLComponentFactoryImpl factory = new WSDLComponentFactoryImpl((WSDLModelImpl)model);
        WSDLSchema wsdlSchema = factory.createWSDLSchema();
        types.addExtensibilityElement(wsdlSchema);
        model.endTransaction();
        Collection<ExtensibilityElement> ees = types.getExtensibilityElements();
        assertEquals("number of EE", 1, ees.size());
        SchemaModel schemaModel = wsdlSchema.getSchemaModel();
        assertNotNull("schemaModel not null", schemaModel);
        Schema schema = schemaModel.getSchema();
        assertNotNull("schema not null", schema);
        GlobalElement ge = schemaModel.getFactory().createGlobalElement();
        model.startTransaction();
        schema.addElement(ge);
        model.endTransaction();
        //File dumpFile = Util.dumpToTempFile(((WSDLModelImpl)model).getBaseDocument());
        //System.out.println("dumpFile; " + dumpFile.getCanonicalPath());
        Collection<Schema> schemas = types.getSchemas();
        assertEquals("number of schemas " , 1 , schemas.size());
    }
    

    /**
     * Test reading in the schema specified by #getSchemaResourcePath.
     * Verifying the resulted model using a visitor or 
     * FindSchemaComponentFromDOM#findComponent method.
     */
    public void testRead() throws Exception {
        WSDLModel model = Util.loadWSDLModel(getTestResourcePath());
        this.assertNotNull(model);
        Definitions d = model.getDefinitions();
        this.assertNotNull(d);
        Types types = d.getTypes();
        this.assertNotNull(types);
        Collection<ExtensibilityElement> ee = types.getExtensibilityElements();
        System.out.println("number of EE: " + ee.size());
        assertTrue("ExtensibilityElement instanceof WSDLSchemaImpl", ee.iterator().next() instanceof WSDLSchemaImpl);
        WSDLSchemaImpl wsdlSchema = (WSDLSchemaImpl)ee.iterator().next();
        SchemaModel schemaModel = wsdlSchema.getSchemaModel();
        assertNotNull("schema model is not null", schemaModel);
        Schema schema = schemaModel.getSchema();
        assertNotNull("schema not null", schema);
        Collection<GlobalElement> gElements = schema.getElements();
        assertEquals("number of global elements", 2, gElements.size());
        
        Binding binding = d.getBindings().iterator().next();
        BindingOperation bo = binding.getBindingOperations().iterator().next();
        BindingInput bi = bo.getBindingInput();
        SOAPHeader sh = (SOAPHeader) bi.getExtensibilityElements(SOAPHeader.class).iterator().next();
        assertNotNull(sh.getMessage().get());
    }

    public void testReadSchemaReference() throws Exception {
        WSDLModel model = Util.loadWSDLModel(getTestResourcePath());
        this.assertNotNull(model);
        Definitions d = model.getDefinitions();
        this.assertNotNull(d);
        Types types = d.getTypes();
        this.assertNotNull(types);
        Collection<Message> messages = d.getMessages();
        assertEquals("number of messages", 2, messages.size());
        //get the first message
        Message message = messages.iterator().next();
        Collection<Part> parts = message.getParts();
        assertEquals("number of parts", 1, parts.size());
        //get the part
        Part part = parts.iterator().next();
        //retrieve the referenced element
        NamedComponentReference<GlobalElement> ref = part.getElement();
        assertEquals("namespace of reference", "http://example.com/stockquote.xsd", ref.getEffectiveNamespace());
        GlobalElement ge = ref.get();
        assertEquals("name of global element", "TradePriceRequest", ge.getName());
    }
    
    public void testWriteSchemaReference() throws Exception {
        WSDLModel model = Util.loadWSDLModel(getTestResourcePath3());
        this.assertNotNull(model);
        Definitions d = model.getDefinitions();
        this.assertNotNull(d);
        Types types = d.getTypes();
        Collection<Schema> schemas = types.getSchemas();
        Schema schema = schemas.iterator().next();
        Collection<GlobalElement> elements = schema.getElements();
        GlobalElement ge = elements.iterator().next();
        
        this.assertNotNull(types);
        WSDLComponentFactory factory = model.getFactory();
        Message message = factory.createMessage();
        model.startTransaction();
        message.setName("BogusMessage");
        Part part = factory.createPart();      
        part.setName("BogusPart");
        NamedComponentReference<GlobalElement> ref = part.createSchemaReference(ge, GlobalElement.class);
        part.setElement(ref);
        message.addPart(part);
        d.addMessage(message);
        model.endTransaction();
        
        
        //read back the message
        Collection<Message> messages = d.getMessages();
        assertEquals("number of messages", 3 , messages.size());
        Iterator<Message> mIterator = messages.iterator();
        mIterator.next();
        mIterator.next();
        //get the third message
        Message m = mIterator.next();
        Collection<Part> parts = m.getParts();
        part = parts.iterator().next();
        NamedComponentReference<GlobalElement> gRef = part.getElement();
        assertNotNull("global reference to part element is not null", gRef);
        GlobalElement rsc = gRef.get();
        assertNotNull("ReferenceableSchemaComponent should not be null", rsc);
        //File dumpFile = Util.dumpToTempFile(((WSDLModelImpl)model).getBaseDocument());
        //System.out.println("dumpFile; " + dumpFile.getCanonicalPath());
    }
    
    public String getTestResourcePath() {
        return "resources/stockquote.xml";
    }
    
    public String getTestResourcePath2(){
       return "resources/emptyStockquote.xml";
    }

    public String getTestResourcePath3(){
       return "resources/stockquoteWrite.xml";
    }
}
