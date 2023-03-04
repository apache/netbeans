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
package org.netbeans.modules.visual.apichanges;

import org.netbeans.junit.NbTestCase;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.border.BorderSupport;

import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

/**
 * Test for issue #103456 - BorderSupport.getSwingBorder method introduced
 * @author David Kaspar
 */
public class SwingBorderGetterTest extends NbTestCase {

    public SwingBorderGetterTest (String name) {
        super (name);
    }

    public void testGetter () {
        Scene scene = new Scene ();
        BevelBorder originalBorder = new BevelBorder (BevelBorder.RAISED);
        scene.setBorder (originalBorder);
        Border foundBorder = BorderSupport.getSwingBorder (scene.getBorder ());
        assertEquals (originalBorder, foundBorder);
    }

}
