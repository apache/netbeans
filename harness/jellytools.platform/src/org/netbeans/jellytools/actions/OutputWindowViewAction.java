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
package org.netbeans.jellytools.actions;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

/** Used to call "Window|Output" main menu item or
 * "org.netbeans.core.output.OutputWindowAction".
 * @see Action
 */
public class OutputWindowViewAction extends Action {
    private static final String menu = "Window|Output";
        
    private static final KeyStroke keystroke = System.getProperty("os.name").toLowerCase().indexOf("mac") > -1 ?
            KeyStroke.getKeyStroke(KeyEvent.VK_4, KeyEvent.META_MASK) :
            KeyStroke.getKeyStroke(KeyEvent.VK_4, KeyEvent.CTRL_MASK);

    /** Creates new instance. */    
    public OutputWindowViewAction() {
        super(menu, null, "org.netbeans.core.io.ui.IOWindowAction", keystroke);
    }
}
