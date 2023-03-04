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
import java.io.IOException;
import junit.framework.*;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;

/**
 * The unit test covers various use cases of sync on Element
 * and ElementRef.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class SyncDeadlockTest extends AbstractSyncTestCase {
    
    public static final String TEST_XSD         = "resources/po.xsd";
    public static final String GLOBAL_ELEMENT   = "purchaseOrder";
    private boolean done = false;
    
    /**
     * SyncElementTest
     */
    public SyncDeadlockTest(String testName) {
	super(testName, TEST_XSD, GLOBAL_ELEMENT);
    }
    
    public static Test suite() {
	TestSuite suite = new TestSuite();
//        Disabled as referenced XSD file were partly not donated by oracle to apache
//        suite.addTestSuite(SyncDeadlockTest.class);
	return suite;
    }
    
    public void testDeadlock() throws Exception {
        final AXIDocument doc = getAXIModel().getRoot();
        doc.addPropertyChangeListener(new AXIDocumentListener(doc));
	SchemaModel sm = getAXIModel().getSchemaModel();
	// This thread will run a sync on axiom
	// this will be run when the schema model is known to be locked
	// from the listener. 
	// The listener will wait until this thread is blocked
	// then continue. will force the locking
	Thread t = new Thread(new Runnable() {
	   public void run() {
	       try {
		    doc.getModel().sync();
	       } catch (IOException ioe) {
		   fail();
	       }
	   } 
	});
        sm.addPropertyChangeListener(new SchemaModelListener(t));
        sm.startTransaction();
        Schema schema = sm.getSchema();
        schema.setTargetNamespace("http://xml.netbeans.org/schema/po");
        sm.endTransaction();
	t.join();
        assertTrue("axi model event not fired", done);
    }
        
    private class SchemaModelListener implements PropertyChangeListener {
	private Thread t;
	
	public SchemaModelListener(Thread t) {
	    this.t = t;
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getSource() instanceof Schema  &&
	       evt.getPropertyName().equals(Schema.TARGET_NAMESPACE_PROPERTY)) {
                // we are now holding a lock on the schema model
		t.start();
		while (!t.getState().equals(Thread.State.BLOCKED)) {
		    try {
			Thread.currentThread().sleep(50);
		    } catch (InterruptedException ex) {
			
		    }
		}
		getAXIModel().isIntransaction();
            }
	}        
    }
    
    private class AXIDocumentListener implements PropertyChangeListener {
        private AXIComponent source;
        
        public AXIDocumentListener(AXIComponent source) {
            this.source = source;
        }
        
	public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getSource() == source && evt.getPropertyName().equals(AXIDocument.PROP_TARGET_NAMESPACE)) {
                String name = (String)evt.getNewValue();
                assertEquals("http://xml.netbeans.org/schema/po",name);
                done = true;
            }
	}        
    }
}
