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

package org.netbeans.modules.xml.axi.sync;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.ComplexContent;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.ComplexTypeDefinition;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.GroupReference;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalAttributeContainer;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentFactory;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.schema.model.Sequence;


/**
 * The unit test covers various use cases of sync on Element
 * and ElementRef.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class SyncElementTest extends AbstractSyncTestCase {
    
    public static final String TEST_XSD         = "resources/address.xsd";
    public static final String GLOBAL_ELEMENT   = "address";
    
    
    /**
     * SyncElementTest
     */
    public SyncElementTest(String testName) {
	super(testName, TEST_XSD, GLOBAL_ELEMENT);
    }
    
    public static Test suite() {
	TestSuite suite = new TestSuite();
//        Disabled as referenced XSD file were partly not donated by oracle to apache
//        suite.addTest(new SyncElementTest("testRemoveElementFromType"));
//        suite.addTest(new SyncElementTest("testRemoveAttributeFromAttrGroup"));
//        suite.addTest(new SyncElementTest("testChangeType"));
//        suite.addTest(new SyncElementTest("testChangeAttributeRef"));
//        suite.addTest(new SyncElementTest("testChangeTypeContent"));
//        suite.addTest(new SyncElementTest("testChangeNameOfElement"));
//	suite.addTest(new SyncElementTest("testRemoveGlobalElement"));
//	suite.addTest(new SyncElementTest("testAddGlobalElement"));
//	suite.addTest(new SyncElementTest("testChangeElementRef"));        
//	suite.addTest(new SyncElementTest("testChangeBase"));
	return suite;
    }
        
    /**
     * Removes an element from type "USAddress".
     * Element count should be one less.
     */
    public void testRemoveElementFromType() throws Exception {
	Element address = findAXIGlobalElement("address");
	int childCount = address.getChildElements().size();
	assert(childCount == 3);
	GlobalComplexType gct = findGlobalComplexType("USAddress");
	getSchemaModel().startTransaction();
	Sequence s = (Sequence)gct.getChildren().get(0);
	s.removeContent(s.getContent().get(0));
	getSchemaModel().endTransaction();
	getAXIModel().sync();
	childCount = address.getChildElements().size();
	assert(childCount == 2);
    }
    
    /**
     * Removes an attribute from attribute group.
     * child count should be one less.
     */
    public void testRemoveAttributeFromAttrGroup() throws Exception {
	Element address = findAXIGlobalElement("address");
	int childCount = address.getChildren().size();
	assert(childCount == 4);
	GlobalAttributeGroup gag = findGlobalAttributeGroup("attr-group");
	getSchemaModel().startTransaction();
	LocalAttribute attr = (LocalAttribute)gag.getChildren().get(1);
	gag.removeLocalAttribute(attr);
	getSchemaModel().endTransaction();
	getAXIModel().sync();
	childCount = address.getChildren().size();
	assert(childCount == 3);
    }
    
    /**
     * Change the type of element "address" from
     * "USAddress" to "USAddress1".
     */
    public void testChangeType() throws Exception {
	PropertyListener l = new PropertyListener();
	getAXIModel().addPropertyChangeListener(l);
	Element address = findAXIGlobalElement("address");
	int childCount = address.getChildElements().size();
	assert(childCount == 2);
	GlobalElement ge = (GlobalElement)globalElement.getPeer();
	getSchemaModel().startTransaction();
	setType(ge, "USAddress1");
	getSchemaModel().endTransaction();
	getAXIModel().sync();
	getAXIModel().removePropertyChangeListener(l);
	childCount = address.getChildElements().size();
	assert(childCount == 5);
    }
    
    /**
     * Change the content of "USAddress1".
     */
    public void testChangeTypeContent() throws Exception {
	PropertyListener l = new PropertyListener();
	getAXIModel().addPropertyChangeListener(l);
	Element address = findAXIGlobalElement("address");
	int childCount = address.getChildElements().size();
	assert(childCount == 5);
	getSchemaModel().startTransaction();
	GlobalGroup gg = findGlobalGroup("group2");
	GroupReference gr = (GroupReference)(findGlobalComplexType("USAddress1").getChildren().get(0));
	NamedComponentReference ref = getSchemaModel().getFactory().
	    createGlobalReference(gg, GlobalGroup.class, gr);
	gr.setRef(ref);
	getSchemaModel().endTransaction();
	getAXIModel().sync();
	getAXIModel().removePropertyChangeListener(l);
	childCount = address.getChildElements().size();
	assert(childCount == 3);
    }
    
    public void testChangeNameOfElement() throws Exception {
	Element address = findAXIGlobalElement("address");
	int childCount = address.getChildElements().size();
	GlobalElement e = findGlobalElement("address");
	getSchemaModel().startTransaction();
	e.setName("NewAddress");
	getSchemaModel().endTransaction();
	getAXIModel().sync();
	assert(childCount == address.getChildElements().size());
        assert(address.getName().equals("NewAddress"));
    }
    
    public void testChangeNameOfType() throws Exception {
	Element address = findAXIGlobalElement("address");
	int childCount = address.getChildElements().size();
	GlobalComplexType type = findGlobalComplexType("USAddress1");
	getSchemaModel().startTransaction();
	type.setName("USAddress2");
	getSchemaModel().endTransaction();
	getAXIModel().sync();
	assert(childCount == address.getChildElements().size());
    }
    
    public void testChangeElementRef() throws Exception {
	getSchemaModel().startTransaction();
	GlobalGroup group = findGlobalGroup("group1");
	ElementReference ref = (ElementReference)group.getChildren().get(0).getChildren().get(0);
	GlobalElement ge = findGlobalElement("fullName");
	NamedComponentReference ncr = getSchemaModel().getFactory().
	    createGlobalReference(ge, GlobalElement.class, ref);
	ref.setRef(ncr);
	getSchemaModel().endTransaction();
	getAXIModel().sync();
    }
    
    /**
     * Remove a global element.
     */
    public void testRemoveGlobalElement() throws Exception {
	int elementCount = getAXIModel().getRoot().getElements().size();
	Element address = findAXIGlobalElement("NewAddress");        
	GlobalElement ge = (GlobalElement)address.getPeer();
	getSchemaModel().startTransaction();
	getSchemaModel().getSchema().removeElement(ge);
	getSchemaModel().endTransaction();
	getAXIModel().sync();
	int newCount = getAXIModel().getRoot().getElements().size();
	assert( (elementCount-1) == newCount);
    }
    
    public void testAddGlobalElement() throws Exception {
	int elementCount = getAXIModel().getRoot().getElements().size();
	getSchemaModel().startTransaction();
	GlobalElement ge = getSchemaModel().getFactory().createGlobalElement();
	ge.setName("address");
	setType(ge, "USAddress1");
	getSchemaModel().getSchema().addElement(ge);
	getSchemaModel().endTransaction();
	getAXIModel().sync();
	int newCount = getAXIModel().getRoot().getElements().size();
	assert( (elementCount+1) == newCount);
    }
    
    private void setType(GlobalElement ge, String globalComplexType) {
	for(GlobalComplexType type : getSchemaModel().getSchema().getComplexTypes()) {
	    if(type.getName().equals(globalComplexType)) {
		NamedComponentReference ref = getSchemaModel().getFactory().
		    createGlobalReference(type, GlobalType.class, ge);
		ge.setType(ref);
	    }
	}
    }
    
    public void testRenameGlobalElement() throws Exception {
	getSchemaModel().startTransaction();
	GlobalElement ge = (GlobalElement)globalElement.getPeer();
	ge.setName("address1");
	getSchemaModel().endTransaction();
	getAXIModel().sync();
	assert(globalElement.getName().equals("address1"));
    }
    
    public void testChangeAttributeRef() throws Exception {
	getSchemaModel().startTransaction();
	GlobalAttributeGroup group = findGlobalAttributeGroup("attr-group");
	AttributeReference ref = (AttributeReference)group.getChildren().get(0);
	GlobalAttribute ga = findGlobalAttribute("countryString");
	NamedComponentReference ncr = getSchemaModel().getFactory().
	    createGlobalReference(ga, GlobalAttribute.class, ref);
	ref.setRef(ncr);
	getSchemaModel().endTransaction();
	getAXIModel().sync();
    }

    /**
     * Remove a GCT "USAddress1", even tho it is being used by "address".
     */
    public void testDeleteGlobalType() throws Exception {
	Element address = findAXIGlobalElement("NewAddress");
	int childCount = address.getChildElements().size();
	assert(childCount == 3);
	GlobalComplexType gct = findGlobalComplexType("USAddress1");
	getSchemaModel().startTransaction();
        getSchemaModel().getSchema().removeComplexType(gct);
	getSchemaModel().endTransaction();
	getAXIModel().sync();
	childCount = address.getChildElements().size();
	assert(childCount == 0);
    }
    
    /**
     * Changes the base for a complex type.
     */
    public void testChangeBase() throws Exception {
	PropertyListener l = new PropertyListener();
	getAXIModel().addPropertyChangeListener(l);
        ContentModel cm1 = findContentModel("CT1");
        assert(cm1.getChildElements().size() == 1);
	GlobalComplexType ct1 = findGlobalComplexType("CT1");
	GlobalComplexType ct2 = findGlobalComplexType("CT2");
        ComplexTypeDefinition def = ct1.getDefinition();
        LocalAttributeContainer lac = ct1;
        SchemaComponentFactory factory = def.getModel().getFactory();
	getSchemaModel().startTransaction();        
        ComplexContent cc = factory.createComplexContent();
        ComplexExtension ce = factory.createComplexExtension();
        moveComplexContents(lac,ce);
        cc.setLocalDefinition(ce);
        ct1.setDefinition(cc);
        ce.setBase(ce.createReferenceTo(ct2, GlobalType.class));
	getSchemaModel().endTransaction();
	getAXIModel().sync();
        assert(cm1.getChildElements().size() == 2);        
    }
    
    private void moveComplexContents(final LocalAttributeContainer oldParent,
            final LocalAttributeContainer newParent) {
        if(oldParent==null || newParent==null) return;
        SchemaModel model = getSchemaModel();
        ArrayList<Class<? extends SchemaComponent>> childTypes =
                new ArrayList<Class<? extends SchemaComponent>>(4);
        childTypes.add(LocalAttribute.class);
        childTypes.add(AttributeReference.class);
        childTypes.add(AttributeGroupReference.class);
        childTypes.add(AnyAttribute.class);
        childTypes.add(ComplexTypeDefinition.class);
        for(SchemaComponent child :oldParent.getChildren(childTypes)) {
            if(newParent.canPaste(child))
                model.addChildComponent(newParent,child.copy(newParent),-1);
            model.removeChildComponent(child);
        }
    }
    
    static class PropertyListener implements PropertyChangeListener {
	List<PropertyChangeEvent> events  = new ArrayList<PropertyChangeEvent>();
	
	public void propertyChange(PropertyChangeEvent evt) {
	    events.add(evt);
	}
	public List<PropertyChangeEvent> getEvents() {
	    return events;
	}
	public void clearEvents() { events.clear();}
    }
}
