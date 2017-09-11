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

package org.netbeans.modules.xml.axi.datatype;

import java.util.ArrayList;
import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.axi.*;
import org.netbeans.modules.xml.axi.visitor.DefaultVisitor;


/**
 *
 * @author Ayub Khan
 */
public class DatatypePerfTest extends AbstractTestCase {
    
    public static final String TEST_XSD         = "resources/OTA_TravelItinerary.xsd";
    public static final String GLOBAL_ELEMENT   = "OTA_TravelItineraryRS";
    
    private List<AbstractAttribute> attList;
    
    public DatatypePerfTest(String testName) {
        super(testName, TEST_XSD, GLOBAL_ELEMENT);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        attList = new ArrayList<AbstractAttribute>();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(DatatypePerfTest.class);
        
        return suite;
    }
    
    /**
     * Test of createElement method, of class org.netbeans.modules.xml.axi.XAMFactory.
     */
    public void testCreateDatatype() {
        validateSchema(axiModel.getSchemaModel());
        Element element = globalElement;
        assertNotNull(element);
        
        attList.clear();
        TestVisitor visitor = new TestVisitor();
        visitor.visit(element);
        
        String contentType = "";
        if(element.getType() instanceof Datatype)
            contentType = element.getType().getName();
        print("\n\n=================\nGE: "+element.getName()+"["+contentType+"]");
    }
    
    private void printAttributes(List<Attribute> attList) {
        for(Attribute attr:attList) {
            String attName = "";
            if(attr.getName() != null)
                attName = attr.getName();
            Datatype type = (Datatype) attr.getType();
            if(type != null) {
                print("\n=================\n"+attName+"["+type.getName()+"]");
                printFacets(type);
            }
        }
    }
    
    private void printFacets(final Datatype type) {
        print("\nlength: "+type.getLengths());
        print("\nminLength: "+type.getMinLengths());
        print("\nmaxLength: "+type.getMaxLengths());
        print("\npattern: "+type.getPatterns());
        print("\nenum: "+type.getEnumerations());
        print("\nwhitespace: "+type.getWhiteSpaces());
    }
    
    private class TestVisitor extends DefaultVisitor {
        
        private int depth = 0;
        
        /**
         * Creates a new instance of TestVisitor
         */
        public TestVisitor() {
            try {
            } catch(Exception ex) {
                //ex.printStackTrace();
                assertTrue("Should not be here", false);
            }
        }
        
        public void visit(Element element) {
            for(AbstractAttribute attr : element.getAttributes()) {
                visit(attr);
            }
            visitChildren(element);
        }
        
        public void visit(AbstractAttribute attribute) {
            attList.add(attribute);
        }
        
        protected void visitChildren(AXIComponent component) {
            for(AXIComponent child: component.getChildren()) {
                child.accept(this);
            }
        }
    }
    
}
