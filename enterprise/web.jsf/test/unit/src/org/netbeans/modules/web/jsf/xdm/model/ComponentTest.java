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
package org.netbeans.modules.web.jsf.xdm.model;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.jsf.api.facesmodel.ConfigAttribute;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesComponent;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.Facet;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.Property;
import org.netbeans.modules.web.jsf.api.metamodel.Component;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigModelImpl;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.openide.util.Exceptions;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class ComponentTest extends NbTestCase {

    public ComponentTest( String name ) {
        super(name);
    }
    
    @Override
    protected Level logLevel() {
        return Level.INFO;
    }
    
    protected void setUp() throws Exception {
        Logger.getLogger(JSFConfigModelImpl.class.getName()).setLevel(Level.FINEST);
    }
    
    protected void tearDown() throws Exception {
    }
    
    
    public void testEmptyComponent() throws Exception{
        JSFConfigModel model = Util.loadRegistryModel("faces-config-empty-component.xml");
        FacesConfig facesConfig = model.getRootComponent();
        
        model.startTransaction();
        
        List<Component> list = facesConfig.getComponents();
        assertEquals( "list of component should contain exactly one" +
                " element. But found :" +list.size(), list.size() , 1);
        
        FacesComponent component = (FacesComponent)list.get(0);
        
        assertEquals( "list of child elements should should be empty" 
                ,0, component.getChildren().size() );
        
        component.addProperty( model.getFactory().createProperty());
        component.addAttribute( model.getFactory().createAttribute());
        component.addFacet( model.getFactory().createFacet());
        component.setComponentClass("compClass");
        component.setComponentType("compType");
        
        assertEquals("compClass",  component.getComponentClass());
        assertEquals("compType",  component.getComponentType());
        assertEquals( 1 ,  component.getFacets().size());
        assertEquals( 1,  component.getAttributes().size());
        assertEquals( 1,  component.getProperties().size());
        
        Element element = Util.getElement( component.getPeer(), 0);
        assertEquals("component-type",  element.getNodeName());
        
        element = Util.getElement( component.getPeer(), 1);
        assertEquals("component-class",  element.getNodeName());
        
        element = Util.getElement( component.getPeer(), 2);
        assertEquals("facet",  element.getNodeName());
        
        element = Util.getElement( component.getPeer(), 3);
        assertEquals("attribute",  element.getNodeName());
        
        element = Util.getElement( component.getPeer(), 4);
        assertEquals("property",  element.getNodeName());
        
        try {
            model.endTransaction();
        } catch (IllegalStateException ise) {
            Exceptions.printStackTrace(ise);
        }
        model.sync();
        
        //Util.dumpToStream(((AbstractDocumentModel)model).getBaseDocument(), System.out);
    }
    
    public void testComponent() throws Exception{
        JSFConfigModel model = Util.loadRegistryModel("faces-config-component.xml");
        FacesConfig facesConfig = model.getRootComponent();
        
        model.startTransaction();
        
        List<Component> list = facesConfig.getComponents();
        assertEquals( "list of component should contain exactly one" +
                " element. But found :" +list.size(), 1, list.size() );
        Component component = list.get( 0 );
        assertTrue( "expect to find XML element , but found :" +
                component.getClass(), component instanceof FacesComponent);
        
        assertEquals("expected component class is 'class', but found :"+
                component.getComponentClass(),"class",
                component.getComponentClass());
        
        assertEquals("expected component type is 'type', but found :"+
                component.getComponentType(),"type",component.getComponentType());
        
        FacesComponent facesComponent = (FacesComponent)component;
        List<Facet> fasets = facesComponent.getFacets();
        assertEquals(2 , fasets.size());
        
        List<ConfigAttribute> attributes = facesComponent.getAttributes();
        assertEquals(2 , attributes.size());
        
        List<Property> properties = facesComponent.getProperties();
        assertEquals(1 , properties.size());
        
        facesComponent.addFacet( model.getFactory().createFacet());
        facesComponent.addDescription( model.getFactory().createDescription());
        
        assertEquals( "There should be exactly one description " +
        		"element ",1, facesComponent.getDescriptions().size());
        
        assertEquals( "There should be exactly 3 facet " +
                "elements ",3, facesComponent.getFacets().size());
        
        Element element = Util.getElement( facesComponent.getPeer(), 0);
        assertEquals( "description", element.getNodeName());
        
        element = Util.getElement( facesComponent.getPeer(), 5); // new facet should be fifth element
        assertEquals( "facet", element.getNodeName());
        
        assertEquals( 0 , element.getChildNodes().getLength());
        
        try {
            model.endTransaction();
        } catch (IllegalStateException ise) {
            Exceptions.printStackTrace(ise);
        }
        model.sync();
        
        //Util.dumpToStream(((AbstractDocumentModel)model).getBaseDocument(), System.out);
    }

}
