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
package org.netbeans.modules.web.beans.xdm.model;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.beans.xml.AlternativeElement;
import org.netbeans.modules.web.beans.xml.Alternatives;
import org.netbeans.modules.web.beans.xml.BeanClass;
import org.netbeans.modules.web.beans.xml.Beans;
import org.netbeans.modules.web.beans.xml.BeansElement;
import org.netbeans.modules.web.beans.xml.Decorators;
import org.netbeans.modules.web.beans.xml.Interceptors;
import org.netbeans.modules.web.beans.xml.Stereotype;
import org.netbeans.modules.web.beans.xml.WebBeansModel;


/**
 * @author ads
 *
 */
public class BeansComponentTest extends NbTestCase {

    public BeansComponentTest( String name ) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }
    
    protected void setUp() throws Exception {
        Logger.getLogger(WebBeansModel.class.getName()).setLevel(Level.FINEST);
    }
    
    public void testEmpty() throws Exception {
        WebBeansModel model = Util.loadRegistryModel("empty-beans.xml");
        model.sync();

        Beans beans = model.getBeans();
        List<BeansElement> elements = beans.getElements();
        assertEquals(0, elements.size());
        assertEquals(0, beans.getChildren().size());
    }

    public void testBeans() throws Exception{
        WebBeansModel model = Util.loadRegistryModel("beans.xml");
        model.sync();
        
        Beans beans = model.getBeans();
        List<BeansElement> elements = beans.getElements();
        
        assertEquals(6, elements.size());
        
        BeansElement beansElement = elements.get(0);
        assertTrue( beansElement instanceof Interceptors );
        beansElement = elements.get(1);
        assertTrue( beansElement instanceof Interceptors );
        
        beansElement = elements.get(2);
        assertTrue( beansElement instanceof Decorators );
        beansElement = elements.get(4);
        assertTrue( beansElement instanceof Decorators );
        
        beansElement = elements.get(3);
        assertTrue( beansElement instanceof Alternatives );
        beansElement = elements.get(5);
        assertTrue( beansElement instanceof Alternatives );
    }
    
    public void testInterceptors() throws Exception{
        WebBeansModel model = Util.loadRegistryModel("beans.xml");
        model.sync();
        
        Beans beans = model.getBeans();
        List<BeansElement> elements = beans.getElements();
        
        BeansElement beansElement = elements.get(0);
        assertTrue( beansElement instanceof Interceptors );
        
        Interceptors interceptors = (Interceptors)beansElement;
        assertEquals( 0, interceptors.getChildren().size() );
        
        beansElement = elements.get(1);
        assertTrue( beansElement instanceof Interceptors );
        
        interceptors = (Interceptors)beansElement;
        assertEquals( 1, interceptors.getChildren().size() );
        
        List<BeanClass> beansClasses = interceptors.getBeansClasses();
        assertEquals(1, beansClasses.size());
        String beanClass = beansClasses.get(0 ).getBeanClass();
        assertEquals("Class1", beanClass);
    }

    
    public void testDecorators() throws Exception{
        WebBeansModel model = Util.loadRegistryModel("beans.xml");
        model.sync();
        
        Beans beans = model.getBeans();
        List<BeansElement> elements = beans.getElements();
        
        BeansElement beansElement = elements.get(4);
        assertTrue( beansElement instanceof Decorators );
        
        Decorators decorators = (Decorators)beansElement;
        assertEquals( 0, decorators.getChildren().size() );
        
        beansElement = elements.get(2);
        assertTrue( beansElement instanceof Decorators );
        
        decorators = (Decorators)beansElement;
        assertEquals( 1, decorators.getChildren().size() );
        
        List<BeanClass> beansClasses = decorators.getBeansClasses();
        assertEquals(1, beansClasses.size());
        String beanClass = beansClasses.get(0 ).getBeanClass();
        assertEquals("Class2", beanClass);
    }
    
    public void testAlternatives() throws Exception{
        WebBeansModel model = Util.loadRegistryModel("beans.xml");
        model.sync();
        
        Beans beans = model.getBeans();
        List<BeansElement> elements = beans.getElements();
        
        BeansElement beansElement = elements.get(3);
        assertTrue( beansElement instanceof Alternatives );
        
        Alternatives alternatives = (Alternatives)beansElement;
        assertEquals( 0, alternatives.getChildren().size() );
        
        beansElement = elements.get(5);
        assertTrue( beansElement instanceof Alternatives );
        
        alternatives = (Alternatives)beansElement;
        
        List<AlternativeElement> alternativeElements = alternatives.getElements();
        assertEquals(2, alternativeElements.size());
        AlternativeElement element = alternativeElements.get(0 );
        
        assertTrue( element instanceof BeanClass );
        String beanClass = ((BeanClass)element).getBeanClass();
        assertEquals("Class3", beanClass );
        
        element = alternativeElements.get(1 );
        assertTrue( element instanceof Stereotype );
        
        String stereotype = ((Stereotype)element).getStereotype();
        assertEquals("Stereotype1", stereotype);
    }
    
    /*
     *  TODO : modification OM tests. 
     *  ads : At this moment OM is used only for reading . So there is no 
     *  need in modification . As consequence modification tests are not written. 
     */
}
