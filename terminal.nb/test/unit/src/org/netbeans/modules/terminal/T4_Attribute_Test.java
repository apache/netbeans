/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
 * when a componet is added or re-added after having been removed (aka closed)
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