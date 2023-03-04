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

package org.netbeans.modules.terminal;

import org.netbeans.modules.terminal.api.ui.IOVisibility;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.SwingUtilities;
import org.netbeans.modules.terminal.api.*;
import org.netbeans.modules.terminal.test.IOTest;
import org.openide.util.Exceptions;
import org.openide.windows.InputOutput;

/**
 * Test the three kinds of closing as described in terminal/README.close_semantics
 * @author ivan
 */
public class T1_Close_Test extends TestSupport {


    public T1_Close_Test(String testName) {
	super(testName);
    }


    @Override
    protected void setUp() throws Exception {
	super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
	super.tearDown();
    }

    public void testHello() {
	System.out.printf("testHello()\n");

	io.getOut().println("Hello to Out\r");
	io.getErr().println("Hello to Err\r");
	sleep(4);
    }


    private void testTitleHelp(InputOutput tio) {
	// This test doesn't work very well visually because when running
	// under the testsuite tabs' names always appear in bold irregardless.

	// title should not be in bold
	tio.getOut().println("testTitle\r");

	// previous getOut() should cause title to become bold
	// tio.getOut().println("title should be in bold\r");
	sleep(4);

	if (!IOVisibility.isSupported(tio))
	    return;

	// will remove tab and attempt to adjust title
	// should not have problems
	IOVisibility.setVisible(tio, false);
	sleep(2);

	// Next time we become visible title should not be bold
	tio.getOut().close();
	sleep(2);
	assertTrue("getOut() still connected after close()", ! IOConnect.isConnected(tio));

	IOVisibility.setVisible(tio, true);
	sleep(4);

	// Currently unfortunately title is still in bold, but can't
	// figure why.
	// When I try in TerminalExamples by hand it works.
    }

    public void testTitle() {

	testTitleHelp(io);
	InputOutput io1 = ioProvider.getIO("io1", null, ioContainer);
	io1.select();
	testTitleHelp(io1);
	io1.closeInputOutput();
    }

    private boolean visible = true;

    public void testVisibilityNotification() {
	if (!IONotifier.isSupported(io))
	    return;

	PropertyChangeListener pcl = new PropertyChangeListener() {
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(IOVisibility.PROP_VISIBILITY)) {
		    assertTrue("property change not on EDT", SwingUtilities.isEventDispatchThread());
		    assertTrue("Got event '" + evt.getPropertyName() + "' instead of PROP_VISIBILITY",
			evt.getPropertyName().equals(IOVisibility.PROP_VISIBILITY));
		    visible = (Boolean) evt.getNewValue();
		} else if (evt.getPropertyName().equals(IOResizable.PROP_SIZE)) {
		} else {
		    System.out.printf("Unexpected event '%s'\n", evt.getPropertyName());
		}
	    }
	};
	IONotifier.addPropertyChangeListener(io, pcl);

	// setUp() calls select() so the terminal should be initially visible

	try {
	    // make it invisible
	    IOVisibility.setVisible(io, false);
	    sleep(1);
	    assertTrue("no visibility property change", visible == false);

	    // make it visible again
	    IOVisibility.setVisible(io, true);
	    sleep(1);
	    assertTrue("no visibility property change", visible == true);

	    // make it invisible again
	    IOVisibility.setVisible(io, false);
	    sleep(1);
	    assertTrue("no visibility property change", visible == false);

	    // make it visible again
	    IOVisibility.setVisible(io, true);
	    sleep(1);
	    assertTrue("no visibility property change", visible == true);
	} finally {
	    IONotifier.removePropertyChangeListener(io, pcl);
	}
    }

    public void testStreamClose() {
	// getIO(String, boolean newIO=false) reuses an IO
	// that is not stream-open (i.e. streams never started
	// or all closed.
	System.out.printf("testStreamClose()\n");

	InputOutput io1 = ioProvider.getIO("io1", null, ioContainer);

	InputOutput io2;
	InputOutput io3;

	// until we open any streams reusing getIO should find it
	io2 = ioProvider.getIO("io1", false, null , ioContainer);
	assertTrue("reusing getIO() didn't find unopened IO", io2 == io1);

	// after opening an io stream reusing getIO should create a new one.
	io1.select();		// so we can check the output
	io1.getOut().println("Hello to io1\r");
	sleep(4);
	io2 = ioProvider.getIO("io1", false, null, ioContainer);
	if (defaultProvider) {
	    // doesn't work as advertised
	    // the following will appear in "io1".
	    io2.getOut().println("Hello to io2\r");
	} else {
	    assertFalse("reusing getIO() found opened IO", io2 == io1);
	    // This used to appear in a separate window, IOContainer.default(),
	    // per BZ #182538, but it's fixed now.
	    io2.select();
	    io2.getOut().println("Hello to io2\r");
	}
	sleep(2);

	// after closing io stream reusing getIO should find it
	io1.getOut().close();
	io3 = ioProvider.getIO("io1", false);
	assertTrue("reusing getIO() didn't find stream closed IO", io3 == io1);

	// at this point io1 and io3 point to the same io.

	// but we can't write to it because we've closed it
	io1.select();		// so we can check the output
	io1.getOut().println("Should not appear\r");
	sleep(3);

	// until we reset it
	try {
	    io1.getOut().reset();
	} catch (IOException ex) {
	    Exceptions.printStackTrace(ex);
	    fail("reset() failed");
	}
	io1.select();		// so we can check the output
	io1.getOut().println("Hello to io1 after reset\r");
	sleep(4);
    }

    public void testMultiStreamClose() {
	// One of getOut() or getErr() or IOTerm.connect()
	// will mark the stream as open.
	//
	// Both getOut() and getErr() must be closed and
	// IOTerm must be disconnecetd for stream to be
	// considered closed.

	if (defaultProvider) {
	    System.out.printf("testMultiStreamClose() Skipped\n");
	    return;
	}

	// just with out
	assertTrue("IO not initially stream-closed",
		   ! IOConnect.isConnected(io));
	io.getOut().println("Hello to io1\r");
	assertTrue("IO still stream-closed after getOut()",
		   IOConnect.isConnected(io));
	io.getOut().close();
	sleep(1);
	assertTrue("IO not stream-closed after out close",
		   ! IOConnect.isConnected(io));

	// just with err

	// LATER
	// VV's fix for missing getErr() uses IOColorLines to
	// implement println() and IOColorLines uses getOut() !
	// io.getErr().println("Hello to io1\r");
	io.getErr();
	assertTrue("IO still stream-closed after getErr()",
		   IOConnect.isConnected(io));
	io.getErr().close();
	sleep(1);
	assertTrue("IO not stream-closed after err close",
		   ! IOConnect.isConnected(io));

	// just using connect
	if (IOTerm.isSupported(io)) {
	    // just with IOTerm.connect()
	    IOTerm.connect(io, null, null, null);
	    sleep(1);
	    assertTrue("IO still stream-closed after connect()",
		       IOConnect.isConnected(io));
	    IOTerm.disconnect(io, continuationInterlock());
	    sleep(1);
	    assertTrue("IO not stream-closed after disconnect",
		       ! IOConnect.isConnected(io));
	}


	// using all three close in one order
	assertTrue("IO should be stream-closed before \"all three\" test",
		   ! IOConnect.isConnected(io));
	io.getOut().println("Hello to io1\r");
	io.getErr();		// see above for why no print
	if (IOTerm.isSupported(io))
	    IOTerm.connect(io, null, null, null);
	assertTrue("IO should be stream-open after all 3 streams are open",
		   IOConnect.isConnected(io));

	if (IOTerm.isSupported(io))
	    IOTerm.disconnect(io, continuationInterlock());
	sleep(1);
	assertTrue("IO should still be stream-open after disconnect",
		   IOConnect.isConnected(io));
	io.getErr().close();
	sleep(1);
	assertTrue("IO should still be stream-open after closing err",
		   IOConnect.isConnected(io));
	io.getOut().close();
	sleep(1);
	assertTrue("IO should be stream-closed after closing out",
		   ! IOConnect.isConnected(io));
    }

    public void testWeakClose() {
	// weak closing removes IO from container
	// select() reinstalls it.

	System.out.printf("testWeakClose()\n");
	InputOutput ios[] = new InputOutput[4];
	ios[0] = io;
	sleep(1);
	// SHOULD not become visible unless we call select
	// BZ 181064
	ios[1] = ioProvider.getIO("test1", null, ioContainer);
	sleep(1);
	ios[2] = ioProvider.getIO("test2", null, ioContainer);
	sleep(1);
	ios[3] = ioProvider.getIO("test3", null, ioContainer);

	sleep(4);

	IOVisibility.setVisible(ios[3], false);
	sleep(1);
	IOVisibility.setVisible(ios[2], false);
	sleep(1);
	IOVisibility.setVisible(ios[1], false);
	// LATER ... who knows what wil happen:
	// IOTest.setVisible(ios[0], false);

	sleep(4);

	ios[3].select();
	sleep(1);

	ios[2].select();
	sleep(1);

	ios[1].select();
	sleep(1);

	sleep(3);
    }

    public void testUnconditionalClose() {
	IOVisibility.setClosable(io, false);

	// conditional close ... should not close
	IOTest.performCloseAction(io);
	sleep(1);
	assertTrue("CloseAction wasn't ignored", io.isClosed() == false);

	// unconditional close ... should close
	IOVisibility.setVisible(io, false);
	sleep(1);
	assertTrue("setVisibility(false) was ignored", io.isClosed() == true);
    }
}