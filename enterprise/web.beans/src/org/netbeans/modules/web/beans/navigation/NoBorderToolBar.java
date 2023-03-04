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

package org.netbeans.modules.web.beans.navigation;

import java.awt.Graphics;
import javax.swing.JToolBar;

/**
 * ToolBar that doesn't paint any border.
 * 
 * Copy of NoBorderToolBar at java.navigation
 *
 * @author ads
 */
public class NoBorderToolBar extends JToolBar {
    
    private static final long serialVersionUID = 2388606453287832422L;

    /** Creates a new instance of NoBorderToolbar */
    public NoBorderToolBar() {
    }
    
    /** Creates a new instance of NoBorderToolbar 
     * @param layout
     */
    public NoBorderToolBar( int layout ) {
        super( layout );
    }
    
    @Override
    protected void paintComponent(Graphics g) {
    }
}
