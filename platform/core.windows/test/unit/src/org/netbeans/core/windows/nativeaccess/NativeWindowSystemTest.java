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
package org.netbeans.core.windows.nativeaccess;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.geom.Area;
import javax.swing.Icon;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author S. Aubrecht
 */
public class NativeWindowSystemTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(NativeWindowSystemTest.class);
    }

    public NativeWindowSystemTest(String testName) {
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

    public void testNoExceptionIsThrown() {
        NativeWindowSystem nws = NativeWindowSystem.getDefault();
        
        assertNotNull(nws);
        
            nws.isWindowAlphaSupported();
            Frame f = new Frame();
            f.setUndecorated(true);
            nws.setWindowAlpha(f, 0.5f);
        
            f = new Frame();
            f.setUndecorated(true);
            nws.setWindowMask(f, new Icon() {
                public void paintIcon(Component arg0, Graphics arg1, int arg2, int arg3) {
                }

                public int getIconWidth() {
                    return 10;
                }

                public int getIconHeight() {
                    return 10;
                }
            });
            f = new Frame();
            f.setUndecorated(true);
            nws.setWindowMask(f, new Area( new Rectangle(0,0,10,10) ) );
    }
}
