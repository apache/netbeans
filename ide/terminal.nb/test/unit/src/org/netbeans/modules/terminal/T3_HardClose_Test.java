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

package org.netbeans.modules.terminal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.lib.terminalemulator.Term;
import org.netbeans.modules.terminal.api.IONotifier;
import org.netbeans.modules.terminal.api.ui.IOTerm;
import org.netbeans.modules.terminal.api.ui.IOVisibility;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/*
 * Test "hard closing", InputOutput.closeInputOutput().
 * - resource freeing
 * - ordering of various closures.
 * - non-recyclability of hard closes IO's.
 * - handling of io and other features after a hard close.
 * @author ivan
 */
public class T3_HardClose_Test extends TestSupport {


    public T3_HardClose_Test(String testName) {
	super(testName);
//	defaultContainer = true;
//	defaultProvider = true;
    }

    @Override
    protected void setUp() throws Exception {
	super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
	super.tearDown();
    }

    public void testResourcesFreed() {
	System.out.printf("testResourcesFreed()\r");

	//
	// Allocate various sub-resources
	//
	io.getOut().println("Hello to Out\r");
	io.getErr().println("Hello to Err\r");

	VetoableChangeListener vcl = null;
	PropertyChangeListener pcl = null;
	if (IONotifier.isSupported(io)) {
	    vcl = new VetoableChangeListener() {
		@Override
		public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
		}
	    };
	    pcl = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
		}
	    };

	    IONotifier.addVetoableChangeListener(io, vcl);
	    IONotifier.addPropertyChangeListener(io, pcl);
	}

	// SHOULD add tips and icons but only after verification.


	//
	// Do the deed
	//
	io.closeInputOutput();
	sleep(1);


	//
	// Retain weak references to sub-resources
	// null out strong references
	//
	Reference<Term> termRef = null;
	if (IOTerm.isSupported(io)) {
	    termRef = new WeakReference(IOTerm.term(io));
	}

	Reference<InputOutput> ioRef = new WeakReference<>(io);
	io = null;

	Reference<Action> actionRef = new WeakReference<>(dummyAction);
	dummyAction = null;

	Reference<PropertyChangeListener> pclRef = new WeakReference<>(pcl);
	pcl = null;

	Reference<VetoableChangeListener> vclRef = new WeakReference<>(vcl);
	vcl = null;


	//
	// Prepare a preferred rootset
	//
	Set<Object> rootset = new HashSet<Object>();
	rootset.add(ioProvider);
	rootset.add(ioContainer);
	rootset.add(actualContainer);

	//
	// resources should be garbage collected
	//
	assertGC("io still referenced", ioRef, rootset);
	// actions are hard to check. They linger in ActionPropertyChangeListener
	// See the nbdev thread assertGC() and Actions/ReferenceQueues
	// And Petr Nejedly's answer.
	// LATER assertGC("action still referenced", actionRef, rootset);
	if (termRef != null)
	    assertGC("term still referenced", termRef, rootset);
	if (IONotifier.isSupported(io)) {
	    assertGC("pcl still referenced", pclRef, rootset);
	    assertGC("vcl still referenced", vclRef, rootset);
	}
    }

    /**
     * Hard close, weak close, then stream close
     */
    public void testHardWeakStream() {
	System.out.printf("testHardWeakStream()\r");

	OutputWriter ow = io.getOut();
	ow.println("Hello to Out\r");

	io.closeInputOutput();
	if (IOVisibility.isSupported(io))
	    IOVisibility.setVisible(io, false);
	ow.close();

	// Additional operations should be no-ops
	ow.println("Should go to bitbucket\r");
	io.select();
    }

    /**
     * Weak close, hard close,then stream close
     */
    public void testWeakHardStream() {
	System.out.printf("testWeakHardStream()\r");

	OutputWriter ow = io.getOut();
	ow.println("Hello to Out\r");

	if (IOVisibility.isSupported(io))
	    IOVisibility.setVisible(io, false);
	io.closeInputOutput();
	ow.close();

	// Additional operations should be no-ops
	ow.println("Should go to bitbucket\r");
	io.select();
    }

    /**
     * Weak close, stream close, then hard close
     */
    public void testWeakStreamHard() {
	System.out.printf("testWeakStreamHard()\r");

	OutputWriter ow = io.getOut();
	ow.println("Hello to Out\r");

	if (IOVisibility.isSupported(io))
	    IOVisibility.setVisible(io, false);
	ow.close();
	io.closeInputOutput();

	// redundant hard closes should not cause problems
	io.closeInputOutput();

	// Additional operations should be no-ops
	ow.println("Should go to bitbucket\r");
	io.select();
    }

    /*
     * After a hard close the same object should not be
     * available via "reuse" getIO().
     */
    public void testNotReusable() {
	System.out.printf("testNotReusabele()\r");

	InputOutput ioRef = io;
	io.closeInputOutput();

	// because closeInputOutput() gets posted to run on the EDT
	// it effect isn't immediate.
	// W/o this sleep() both terminal and default IO will end up
	// reusing because the close operation hasn't gone through.
	sleep(1);

	boolean reuse = true;
	InputOutput uniqueIO = ioProvider.getIO("test", !reuse);
	assertTrue("hard closed io got reused", ioRef != uniqueIO);

	// cleanup
	uniqueIO.closeInputOutput();
    }
}