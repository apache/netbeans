/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
/*
 * ValidatorSchemaFactoryRegistryTest.java
 * JUnit based test
 *
 * Created on February 6, 2007, 11:13 PM
 */

package org.netbeans.modules.xml.wsdl.validator;

import java.util.ArrayList;
import junit.framework.*;
import java.util.Collection;
import java.util.Hashtable;
import org.netbeans.modules.xml.wsdl.validator.spi.ValidatorSchemaFactory;
import org.openide.util.Lookup;

/**
 *
 * @author radval
 */
public class ValidatorSchemaFactoryRegistryTest extends TestCase {

    public ValidatorSchemaFactoryRegistryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of getDefault method, of class org.netbeans.modules.xml.wsdl.validator.ValidatorSchemaFactoryRegistry.
     */
    public void testGetDefault() {
        System.out.println("getDefault");

        ValidatorSchemaFactoryRegistry result = ValidatorSchemaFactoryRegistry.getDefault();
        assertNotNull(result);
        
        
        
    }

    /**
     * Test of getValidatorSchemaFactory method, of class org.netbeans.modules.xml.wsdl.validator.ValidatorSchemaFactoryRegistry.
     */
    public void testGetValidatorSchemaFactory() {
        System.out.println("getValidatorSchemaFactory");
        
        String namespace = "";
        ValidatorSchemaFactoryRegistry instance = ValidatorSchemaFactoryRegistry.getDefault();
        
        ValidatorSchemaFactory expResult = null;
        ValidatorSchemaFactory result = instance.getValidatorSchemaFactory(namespace);
        assertEquals(expResult, result);
        
        
        
    }

    /**
     * Test of getAllValidatorSchemaFactories method, of class org.netbeans.modules.xml.wsdl.validator.ValidatorSchemaFactoryRegistry.
     */
    public void testGetAllValidatorSchemaFactories() {
        System.out.println("getAllValidatorSchemaFactories");
        
        ValidatorSchemaFactoryRegistry instance = ValidatorSchemaFactoryRegistry.getDefault();
        
        Collection<ValidatorSchemaFactory> expResult = new ArrayList<ValidatorSchemaFactory>();
        Collection<ValidatorSchemaFactory> result = instance.getAllValidatorSchemaFactories();
        assertEquals(expResult.size(), result.size());
        
       
        
    }
    
}
