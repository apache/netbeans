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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.web.beans.xdm.model;

import java.util.List;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.beans.xml.AlternativeElement;
import org.netbeans.modules.web.beans.xml.Alternatives;
import org.netbeans.modules.web.beans.xml.BeanClass;
import org.netbeans.modules.web.beans.xml.BeansElement;
import org.netbeans.modules.web.beans.xml.Decorators;
import org.netbeans.modules.web.beans.xml.Interceptors;
import org.netbeans.modules.web.beans.xml.Stereotype;
import org.netbeans.modules.web.beans.xml.WebBeansModel;


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
