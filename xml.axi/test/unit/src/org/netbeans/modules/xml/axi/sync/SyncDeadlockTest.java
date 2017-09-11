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
	TestSuite suite = new TestSuite(SyncDeadlockTest.class);
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
