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

import javax.swing.Icon;
import org.netbeans.modules.terminal.api.ui.IOVisibility;
import org.openide.util.ImageUtilities;
import org.openide.windows.IOTab;
import org.openide.windows.InputOutput;

/*
 * Each IO has the following attributes which have to be managed by a container:
 * - callbacks
 * - title			immutable			restore
 * - toolbarActions		immutable	selection
 * - toolTipText						restore
 * - icon							restore
 * - connected (shows up as bold text)
 * - isClosable
 * - findState					selection
 *
 * 'immutable' means that the attribute is set at IO creation time and there
 * are no mutator API's for them.
 * 'selection' means that this attribute has to be multiplexed by the container
 * based on the "selected" component. I.e. when a new tab is selected the action
 * buttons have to be adjusted.
 * 'restore' means the attribute is managed by the tabbed pane. As a result,
 * when a component is added or re-added after having been removed (aka closed)
 * these attributes need to be restored.
 * @author ivan
 */
public class T4_Attribute_Test extends TestSupport {

    private final String iconResource = "org/netbeans/modules/terminal/sunsky.png";
    Icon icon = ImageUtilities.loadImageIcon(iconResource, false);

    public T4_Attribute_Test(String testName) {
	super(testName);
    }


    @Override
    protected void setUp() throws Exception {
	super.setUp(false);
    }

    @Override
    protected void tearDown() throws Exception {
	super.tearDown();
    }

    private void setAttrs() {
	if (IOTab.isSupported(io)) {
	    IOTab.setIcon(io, icon);
	    IOTab.setIcon(io, null);
	    IOTab.setIcon(io, icon);

	    IOTab.setToolTipText(io, "ToolTip");
	    IOTab.setToolTipText(io, null);
	    IOTab.setToolTipText(io, "ToolTip");

	    if (IOVisibility.isSupported(io)) {
		IOVisibility.setClosable(io, false);
		IOVisibility.setClosable(io, true);
		sleep(1);
	    }

	    io.getOut().println("Altering attributes\r");
	    // This indirectly affects the title:
	    io.getOut().close();
	    sleep(1);
	}
    }

    /*
     * Set attributes before the component is first selected/made visible.
     */
    public void testPreSelect() {
	System.out.printf("testPreSelect()\n");

	setAttrs();
	sleep(1);	// give them time to take effect

	io.select();
	sleep(1);	// give select time to take effect
    }

    /*
     * Set attributes when the component is the sole component and then
     * switch to tabbed mode.
     */
    public void testFirstTab() {
	System.out.printf("testFirstTab()\n");

	io.select();
	sleep(1);	// give select time to take effect

	setAttrs();
	sleep(1);	// give them time to take effect

	InputOutput io2 = ioProvider.getIO("test2", null, ioContainer);
	assertNotNull ("Could not get InputOutput", io2);
	io2.select();
	sleep(1);	// give select time to take effect
    }

    /*
     * Set attributes when we're already in tabbed mode
     */
    public void testSecondTab() {
	System.out.printf("testSecondTab()\n");

	io.select();
	sleep(1);	// give select time to take effect

	InputOutput io2 = ioProvider.getIO("test2", null, ioContainer);
	assertNotNull ("Could not get InputOutput", io2);
	io2.select();
	sleep(1);	// give select time to take effect

	setAttrs();
	sleep(1);	// give them time to take effect
    }

    /*
     * Set attributes when the table is invisible (aka closed),
     * Then select the tab and the attributes should take effect.
     */
    public void testClosedTab() {
	System.out.printf("testClosedTab()\n");

	io.select();
	sleep(1);	// give select time to take effect

	InputOutput io2 = ioProvider.getIO("test2", null, ioContainer);
	assertNotNull ("Could not get InputOutput", io2);
	io2.select();
	sleep(1);	// give select time to take effect

	IOVisibility.setVisible(io, false);
	sleep(1);	// wait til it's closed

	setAttrs();
	sleep(1);	// give them time to take effect

	io.select();
	sleep(1);	// give select time to take effect
    }
}