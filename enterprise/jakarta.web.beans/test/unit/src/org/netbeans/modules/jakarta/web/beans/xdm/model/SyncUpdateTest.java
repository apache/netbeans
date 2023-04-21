/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.jakarta.web.beans.xdm.model;

import java.util.List;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.jakarta.web.beans.xml.AlternativeElement;
import org.netbeans.modules.jakarta.web.beans.xml.Alternatives;
import org.netbeans.modules.jakarta.web.beans.xml.BeanClass;
import org.netbeans.modules.jakarta.web.beans.xml.BeansElement;
import org.netbeans.modules.jakarta.web.beans.xml.Decorators;
import org.netbeans.modules.jakarta.web.beans.xml.Interceptors;
import org.netbeans.modules.jakarta.web.beans.xml.Stereotype;
import org.netbeans.modules.jakarta.web.beans.xml.WebBeansModel;


/**
 * @author ads
 *
 */
public class SyncUpdateTest extends NbTestCase {

    public SyncUpdateTest( String name ) {
        super(name);
    }
    
    public void testSyncBeansElements() throws Exception{
        WebBeansModel model = Util.loadRegistryModel("beans-orig.xml");
        
        Util.setDocumentContentTo(model, "beans.xml");

        List<BeansElement> elements = model.getBeans().getElements();
        assertEquals( 6 ,  elements.size());

        BeansElement beansElement = elements.get(1);
        assertTrue( beansElement instanceof Interceptors );
        
        beansElement = elements.get(4);
        assertTrue( beansElement instanceof Decorators );
        
        beansElement = elements.get(5);
        assertTrue( beansElement instanceof Alternatives );
        
        List<AlternativeElement> alternativeElements = ((Alternatives)beansElement).
            getElements();
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
    
    public void testAlternatives() throws Exception{
        WebBeansModel model = Util.loadRegistryModel("alternatives-beans-orig.xml");
        
        Util.setDocumentContentTo(model, "alternatives-beans.xml");

        List<BeansElement> elements = model.getBeans().getElements();
        assertEquals( 1 ,  elements.size());
        
        Alternatives alternatives = (Alternatives)elements.get(0);
        List<AlternativeElement> alternativeElements = alternatives.getElements();
        assertEquals(5, alternativeElements.size());
        
        AlternativeElement alternativeElement = alternativeElements.get(2);
        assertTrue( alternativeElement instanceof BeanClass );
        String beanClass = ((BeanClass)alternativeElement).getBeanClass();
        assertEquals( "Class2", beanClass);
        
        alternativeElement = alternativeElements.get(3);
        assertTrue( alternativeElement instanceof BeanClass );
        beanClass = ((BeanClass)alternativeElement).getBeanClass();
        assertEquals( "Class3", beanClass);
        
        alternativeElement = alternativeElements.get(4);
        assertTrue( alternativeElement instanceof Stereotype );
        String stereotype = ((Stereotype)alternativeElement).getStereotype();
        assertEquals( "Stereotype2", stereotype);
    }
}
