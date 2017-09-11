/**
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.persistence.unit;

import junit.framework.*;
import java.util.List;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.xml.multiview.Error;

/**
 * Tests for the <code>PersistenceValidator</code>.
 * @author Erno Mononen
 */
public class PersistenceValidatorTest extends PersistenceEditorTestBase {
    
    public PersistenceValidatorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    /**
     * Tests that validator reports duplicate names as errors.
     */
    public void testValidateNameIsUnique() {
        String version=dataObject.getPersistence().getVersion();
        PersistenceUnit unit1 = Persistence.VERSION_1_0.equals(version) ?
            new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit() :
            Persistence.VERSION_2_0.equals(version) ? new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit() :
            new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_1.PersistenceUnit();
        unit1.setName("name1");
        dataObject.addPersistenceUnit(unit1);
        PersistenceUnit unit2 = Persistence.VERSION_1_0.equals(version) ?
            new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit() :
            Persistence.VERSION_2_0.equals(version) ? new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit() :
            new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_1.PersistenceUnit();
        unit2.setName("name1");
        dataObject.addPersistenceUnit(unit2);
        PersistenceValidator validator = new PersistenceValidatorImpl(dataObject, false);
        List<Error> errors = validator.validate();
        assertEquals(2, errors.size());
        assertEquals(Error.DUPLICATE_VALUE_MESSAGE, errors.get(0).getErrorType());
        assertEquals(Error.DUPLICATE_VALUE_MESSAGE, errors.get(1).getErrorType());
    }

    
    /**
     * Tests that validator reports usage of exclude-unlisted-classes in 
     * Java SE environments as errors.
     */
    public void testValidateExcludeUnlistedClasses(){
        // Java SE
        PersistenceValidator javaSEvalidator = new PersistenceValidatorImpl(dataObject, true);
        String version=dataObject.getPersistence().getVersion();
        PersistenceUnit unit1 = Persistence.VERSION_1_0.equals(version) ?
            new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit() :
            Persistence.VERSION_2_0.equals(version) ? new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit() :
            new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_1.PersistenceUnit();
        unit1.setName("unit1");
        unit1.setExcludeUnlistedClasses(true);
        dataObject.addPersistenceUnit(unit1);
        List<Error> errors = javaSEvalidator.validate();
        assertEquals(1, errors.size());
        assertEquals(Error.TYPE_WARNING, errors.get(0).getErrorType());
        // Java EE
        PersistenceValidator javaEEvalidator = new PersistenceValidatorImpl(dataObject, false);
        errors = javaEEvalidator.validate();
        assertTrue(errors.isEmpty());;
    }
    
    /**
     * Tests that validator reports usage of jar-files in 
     * Java SE environments as errors.
     */
    public void testValidateJarFiles(){
        // Java SE
        PersistenceValidator javaSEvalidator = new PersistenceValidatorImpl(dataObject, true);
        String version=dataObject.getPersistence().getVersion();
        PersistenceUnit unit1 = Persistence.VERSION_1_0.equals(version) ?
            new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit() :
            Persistence.VERSION_2_0.equals(version) ? new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit() :
            new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_1.PersistenceUnit();
        unit1.setName("unit1");
        unit1.addJarFile("my-jar.jar");
        dataObject.addPersistenceUnit(unit1);
        List<Error> errors = javaSEvalidator.validate();
        assertEquals(1, errors.size());
        assertEquals(Error.TYPE_WARNING, errors.get(0).getErrorType());
        // Java EE
        PersistenceValidator javaEEvalidator = new PersistenceValidatorImpl(dataObject, false);
        errors = javaEEvalidator.validate();
        assertTrue(errors.isEmpty());;
    }
    
    /**
     * Implementation of PersistenceValidator that allows to be specified 
     * whether we're dealing with Java SE environment. 
     */ 
    private static class PersistenceValidatorImpl extends PersistenceValidator {
        
        private boolean javaSE;
        
        public PersistenceValidatorImpl(PUDataObject puDataObject, boolean javaSE){
            super(puDataObject);
            this.javaSE = javaSE;
        }

        protected boolean isJavaSE() {
            return javaSE;
        }
        
        
    }
}
