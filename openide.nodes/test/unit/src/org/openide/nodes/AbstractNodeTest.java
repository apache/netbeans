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

package org.openide.nodes;

import java.beans.BeanInfo;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import junit.framework.TestCase;

/**
 * A test for icon loading only so far.
 *
 * @author Nenik
 */
public class AbstractNodeTest extends TestCase {

    public AbstractNodeTest(String testName) {
        super(testName);
    }

    /**
     * Test of setIconBaseWithExtension method, of class org.openide.nodes.AbstractNode.
     */
    public void testSetIconBaseWithExtension() {
        AbstractNode an = new AbstractNode(Children.LEAF);
        Listener lst = new Listener();
        an.addNodeListener(lst);
        
        // default icons
        Image def = getImage("org/openide/nodes/defaultNode.png");
        Image def32 = getImage("org/openide/nodes/defaultNode32.png");
        
        // PNGs, Open32 is missing
        Image aPng = getImage("org/openide/nodes/data/a.png");
        Image aPngOpen = getImage("org/openide/nodes/data/aOpen.png");
        Image a32Png = getImage("org/openide/nodes/data/a32.png");
        
        // GIFs, 32 is missing
        Image aGif = getImage("org/openide/nodes/data/a.gif");
        Image a32Gif = getImage("org/openide/nodes/data/a32.gif");
        Image a32GifOpen = getImage("org/openide/nodes/data/aOpen32.gif");
        
        // extension-less icons
        Image b = getImage("org/openide/nodes/data/b");
        Image b32 = getImage("org/openide/nodes/data/b32");

        // ugly one, no extension, dot in path
        Image b2 = getImage("org/openide/nodes/data/res.t2/b");

        // check the default icon first
        checkIcons(an, def, def32, def, def32);
        
        // verify the original method behaviour
        an.setIconBase("org/openide/nodes/data/a");
        lst.assertEvents(2); // icon and opened icon
        checkIcons(an, aGif, a32Gif, aGif, a32GifOpen);
        
        // Check the preferred method
        an.setIconBaseWithExtension("org/openide/nodes/data/a.png");
        lst.assertEvents(2); // icon and opened icon
        checkIcons(an, aPng, a32Png, aPngOpen, aPngOpen);
        
        // also for gifs
        an.setIconBaseWithExtension("org/openide/nodes/data/a.gif");
        lst.assertEvents(2); // icon and opened icon
        checkIcons(an, aGif, a32Gif, aGif, a32GifOpen);
        
        // What if there is no extension?
        an.setIconBaseWithExtension("org/openide/nodes/data/b");
        lst.assertEvents(2); // icon and opened icon
        checkIcons(an, b, b32, b, b32);

        // Do we support such insane resources too?
        an.setIconBaseWithExtension("org/openide/nodes/data/res.t2/b");
        lst.assertEvents(2); // icon and opened icon
        checkIcons(an, b2, b2, b2, b2);

    }


    private static Image getImage(String path) {
        Image ret = ImageUtilities.loadImage(path, true);
        assertNotNull("Icon loaded", ret);
        return ret;
    }
    
    private static void checkIcons(Node n, Image base, Image base32, Image open, Image open32) {
        assertSame("Base icon", base, n.getIcon(BeanInfo.ICON_COLOR_16x16));
        assertSame("Base icon (mono)", base, n.getIcon(BeanInfo.ICON_MONO_16x16));
        assertSame("Base icon32", base32, n.getIcon(BeanInfo.ICON_COLOR_32x32));
        assertSame("Base icon32 (mono)", base32, n.getIcon(BeanInfo.ICON_MONO_32x32));
        assertSame("Open icon", open, n.getOpenedIcon(BeanInfo.ICON_COLOR_16x16));
        assertSame("Open icon (mono)", open, n.getOpenedIcon(BeanInfo.ICON_MONO_16x16));
        assertSame("Open icon32", open32, n.getOpenedIcon(BeanInfo.ICON_COLOR_32x32));
        assertSame("Open icon32 (mono)", open32, n.getOpenedIcon(BeanInfo.ICON_MONO_32x32));
        
    }
    
    static class Listener extends NodeAdapter {
        int cnt;
        
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            cnt++;
        }       
        
        public void assertEvents(int count) {
            assertEquals("Number of events", count, cnt);
            cnt = 0;
        }
    }
}
