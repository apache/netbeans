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

package org.netbeans.lib.profiler.ui.swing;

import java.awt.Color;
import javax.swing.Icon;
import javax.swing.JLabel;
import org.netbeans.lib.profiler.ui.UIUtils;

/**
 *
 * @author Jiri Sedlacek
 */
public class GrayLabel extends JLabel {
    
    { setFocusable(false); }
    
    
    public GrayLabel() { super(); }
    
    public GrayLabel(Icon icon) { super(icon); }
    
    public GrayLabel(String text) { super(text); }
    
    public GrayLabel(Icon icon, int alignment) { super(icon, alignment); }
    
    public GrayLabel(String text, int alignment) { super(text, alignment); }
    
    public GrayLabel(String text, Icon icon, int alignment) { super(text, icon, alignment); }
    
    
    public Color getForeground() {
        return UIUtils.getDisabledLineColor();
    }
    
    
    public void setEnabled(boolean enabled) {
        super.setEnabled(true); // To workaround the 3D look on some LaFs
    }
    
}
