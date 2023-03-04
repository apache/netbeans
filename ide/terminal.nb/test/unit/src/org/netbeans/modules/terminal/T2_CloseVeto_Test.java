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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import javax.swing.SwingUtilities;
import org.netbeans.modules.terminal.api.IOConnect;
import org.netbeans.modules.terminal.api.IONotifier;
import org.netbeans.modules.terminal.api.IOResizable;
import org.netbeans.modules.terminal.api.ui.IOVisibility;
import org.netbeans.modules.terminal.test.IOTest;
import org.openide.windows.InputOutput;

/**
 *
 * @author ivan
 */
public class T2_CloseVeto_Test extends TestSupport {

    public T2_CloseVeto_Test(String testName) {
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

    private static final class CloseVetoConfig {
	public final boolean isClosable;
	public final boolean registerVetoer;
	public final boolean closeStreamFirst;
	public final boolean closeIfDisconnected;
	public final boolean sayYes;

	public final boolean shouldSeeVetoable;
	public final boolean shouldBeClosed;

	public CloseVetoConfig(boolean isClosable,
			       boolean registerVetoer,
		               boolean closeStreamFirst,
			       boolean closeIfDisconnected,
			       boolean sayYes,
			       boolean shouldSeeVetoable,
	                       boolean shouldBeClosed) {
	    this.isClosable = isClosable;
	    this.registerVetoer = registerVetoer;
	    this.closeStreamFirst = closeStreamFirst;
	    this.closeIfDisconnected = closeIfDisconnected;
	    this.sayYes = sayYes;
	    this.shouldSeeVetoable = shouldSeeVetoable;
	    this.shouldBeClosed = shouldBeClosed;
	}

	@Override
	public String toString() {
	    return String.format("isClosable %s\nregisterVetoer %b\ncloseStreamFirst %b\ncloseIfDisconnected %b\nsayYes %b\nshouldSeeVEtoable %b\nshouldBeClosed %b\n",
		    isClosable, registerVetoer, closeStreamFirst, closeIfDisconnected, sayYes, shouldSeeVetoable, shouldBeClosed);
	}
    }

    private static final CloseVetoConfig[] configs = new CloseVetoConfig[] {
	// Columns:
	//		isClosable	registerVetoer		sayYes		shouldSeeVetoable
	//					closeStreamFirst			shouldBeClosed
	//						closeIfDisconnected
	// AllowClose.NEVER
	// never see confirmer never close
	new CloseVetoConfig(false,	true,	false,	false,	false,		false,	false),
	new CloseVetoConfig(false,	true,	true,	false,	false,		false,	false),
	// no vetoer
	new CloseVetoConfig(false,	false,	false,	false,	false,		false,	false),
	new CloseVetoConfig(false,	false,	true,	false,	false,		false,	false),

	// AllowClose.ALWAYS
	new CloseVetoConfig(true,	true,	false, 	false,	false, 		true,	false),
	new CloseVetoConfig(true,	true,	false, 	false,	true, 		true,	true),
	new CloseVetoConfig(true,	true,	true, 	false,	false, 		true,	false),
	new CloseVetoConfig(true,	true,	true, 	false,	true, 		true,	true),

	// AllowClose.DISCONNECTED
	// still connected need confirmer
	new CloseVetoConfig(true,	true,	false,	true,	false, 		true,	false),
	new CloseVetoConfig(true,	true,	false,	true,	true, 		true,	true),

	// no longer connected see vetoable but no confirmer
	new CloseVetoConfig(true,	true,	true,	true,	false, 		true,	true),

	// no vetoer
	new CloseVetoConfig(true,	false,	true,	false,	false, 		false,	true),
	new CloseVetoConfig(true,	false,	false,	false,	false, 		false,	true),
    };

    private CloseVetoConfig currentCvc = null;
    private boolean sawVetoable = false;
    private boolean sawClose = false;
    private boolean visible = true;


    private void testCloseVeto(CloseVetoConfig cvc) {

	VetoableChangeListener vcl = null;
	if (cvc.registerVetoer) {
	    vcl = new VetoableChangeListener() {
		@Override
		public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
		    if (evt.getPropertyName().equals(IOVisibility.PROP_VISIBILITY) &&
			evt.getNewValue().equals(Boolean.FALSE)) {

			sawVetoable = true;
			InputOutput src = (InputOutput) evt.getSource();
			if (currentCvc.closeIfDisconnected) {
			    if (IOConnect.isConnected(src)) {
				if (! currentCvc.sayYes)
				    throw new PropertyVetoException("don't close", evt);
			    } else {
				// close w/o confirming
			    }
			} else {
			    if (! currentCvc.sayYes)
				throw new PropertyVetoException("don't close", evt);
			}
		    }
		}
	    };
	}

	IONotifier.addVetoableChangeListener(io, vcl);
	currentCvc = cvc;
	sawVetoable = false;
	sawClose = false;
	try {

	    IOVisibility.setClosable(io, cvc.isClosable);
	    io.select();
	    io.getOut().println("Config X\r");
	    if (cvc.closeStreamFirst)
		io.getOut().close();

	    // This should first trigger a veto propery change followed by
	    // an actual property change
	    IOTest.performCloseAction(io);

	    // give it all time to settle down.
	    sleep(3);
	    assertTrue("sawVetoable != cvc.shouldSeeVetoable\n" + cvc, sawVetoable == cvc.shouldSeeVetoable);
	    assertTrue("sawClose != cvc.shouldSeeClose\n" + cvc, sawClose == cvc.shouldBeClosed);
	} finally {
	    IONotifier.removeVetoableChangeListener(io, vcl);
	}
    }

    public void testCloseVeto() {

	PropertyChangeListener pcl = new PropertyChangeListener() {
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(IOVisibility.PROP_VISIBILITY)) {
		    assertTrue("property change not on EDT", SwingUtilities.isEventDispatchThread());
		    assertTrue("Got event '" + evt.getPropertyName() + "' instead of PROP_VISIBILITY",
			evt.getPropertyName().equals(IOVisibility.PROP_VISIBILITY));
		    visible = (Boolean) evt.getNewValue();
		    if (visible == false)
			sawClose = true;
		} else if (evt.getPropertyName().equals(IOResizable.PROP_SIZE)) {
		} else if (evt.getPropertyName().equals(IOConnect.PROP_CONNECTED)) {
		} else {
		    System.out.printf("Unexpected event '%s'\n", evt.getPropertyName());
		}
	    }
	};

	IONotifier.addPropertyChangeListener(io, pcl);

	try {
	    for (CloseVetoConfig cvc : configs) {
		testCloseVeto(cvc);
	    }
	} finally {
	    IONotifier.removePropertyChangeListener(io, pcl);
	}
    }
}