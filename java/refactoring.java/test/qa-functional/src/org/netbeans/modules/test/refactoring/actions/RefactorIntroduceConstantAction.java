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

package org.netbeans.modules.test.refactoring.actions;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.ActionNoBlock;

/**
 <p>
 @author Standa
 */
public class RefactorIntroduceConstantAction extends ActionNoBlock {
    private static final String introduceMethodPopup = Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "Actions/Refactoring")
            + "|Introduce|Constant...";
          
    private static final String introduceMethodMenu = Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "Menu/Refactoring") // Refactoring
            + "|Introduce|Constant...";
    private static final KeyStroke keystroke = System.getProperty("os.name").toLowerCase().indexOf("mac") > -1 ?
            KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_MASK) : //Mac
            KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK);  //Win, Unix

    /**
     * creates new RefactorRenameAction instance
     */
    public RefactorIntroduceConstantAction() {
        super(introduceMethodMenu, introduceMethodPopup, null, keystroke);
    }
}
