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

package org.netbeans.modules.project.ui;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import junit.framework.TestCase;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author mkleint
 */
public class ExtIconTest extends TestCase {
    
    public ExtIconTest(String testName) {
        super(testName);
    }
    
    public void testByteConversions() {
        ExtIcon ext = new ExtIcon();
        URL res = getClass().getClassLoader().getResource("org/netbeans/modules/project/ui/module.gif");
        assertNotNull(res);
        //#138000
        Image img = ImageUtilities.loadImage("org/netbeans/modules/project/ui/module.gif");
        img = ImageUtilities.addToolTipToImage(img, "XXX");
        Icon icon = ImageUtilities.image2Icon(img);
        ext.setIcon(icon);
        try {
            byte[] bytes1 = ext.getBytes();
            ExtIcon ext2 = new ExtIcon(bytes1);
            byte[] bytes2 = ext2.getBytes();
            ExtIcon ext3 = new ExtIcon(bytes2);
            byte[] bytes3 = ext3.getBytes();
            
            assertEquals(bytes1.length, bytes2.length);
            assertEquals(bytes3.length, bytes3.length);
            for (int i = 0; i < bytes1.length; i++) {
                assertEquals("Non equals at position " + i,bytes1[i], bytes2[i]);
                assertEquals("Non equals at position " + i,bytes1[i], bytes3[i]);
            }
        }
        catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            fail();
        }
        
    }
    
}
