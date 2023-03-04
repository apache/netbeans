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
