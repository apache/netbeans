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

/*
 * IncludeTest.java
 * JUnit based test
 *
 * Created on October 4, 2005, 4:26 PM
 */

package org.netbeans.modules.xml.schema.model;
import java.beans.PropertyChangeListener;
import junit.framework.*;
import java.net.URI;
import java.util.Collection;

/**
 *
 * @author rico
 */
public class IncludeTest extends TestCase {
    private static final String TEST_XSD = "resources/testInclude.xsd";
    
    public IncludeTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(IncludeTest.class);
        
        return suite;
    }
    
    /**
     * Test of getSchemaLocation method, of class org.netbeans.modules.xmlschema.api.model.Include.
     */
    public void testGetSchemaLocation() throws Exception {
        Include instance = null;
        SchemaModel model = Util.loadSchemaModel(TEST_XSD);
         Schema schema = model.getSchema();
        assertNotNull("Null schema " , schema);
        
	Collection<Include> refs = schema.getIncludes();
	this.assertNotNull("Null refs ", refs);
	instance = refs.iterator().next();
        this.assertNotNull("Null include ", instance);
        String expResult = "somefile.xsd";
        System.out.println("expResult: " + expResult.toString());
        String result = instance.getSchemaLocation();
        System.out.println("result: " + result.toString());
        assertEquals(expResult.toString(), result.toString());
    }
    
    /**
     * Test of setSchemaLocation method, of class org.netbeans.modules.xmlschema.api.model.Include.
     */
    public void testSetSchemaLocation() throws Exception {
        URI uri = null;
        Include instance = null;
        SchemaModel model = Util.loadSchemaModel(TEST_XSD);
        Schema schema = model.getSchema();
        assertNotNull("Null schema " , schema);
        
        java.util.List<SchemaComponent> children = schema.getChildren();
        for(SchemaComponent child: children){
            if(child instanceof Include){
                instance = (Include)child;
                break;
            }
        }
        this.assertNotNull("Null include ", instance);
	TestListener tl = new TestListener();
	instance.getModel().addPropertyChangeListener(tl);
        String result = instance.getSchemaLocation();
        assertEquals("somefile.xsd", instance.getSchemaLocation().toString());
	model.startTransaction();
	instance.setSchemaLocation("newfile.xsd");
        model.endTransaction();
	assertEquals("newfile.xsd", instance.getSchemaLocation().toString());
	assertTrue("only one event should be fired " + tl.getEventsFired(), tl.getEventsFired()==1);
	assertEquals("event should be modified", Include.SCHEMA_LOCATION_PROPERTY, tl.getLastEventName());
    }
    
    static class TestListener implements PropertyChangeListener {
	    private String eventName;
	    private int count = 0;
	    
	    public void propertyChange(java.beans.PropertyChangeEvent evt) {
		    eventName = evt.getPropertyName();
		    count++;
	    }
	    
	    public int getEventsFired() {
		    return count;
	    }
	    
	    public String getLastEventName() {
		    return eventName;
	    }
	    
	    public void resetFiredEvents() {
		    count = 0;
	    }
    }
}
