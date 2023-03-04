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
 *
 * @author Jiri.Prox@oracle.com, Marian.Mirilovic@oracle.com
 */
public class FindUsagesAction extends ActionNoBlock {

    private static final String findUsagesPopup = Bundle.getStringTrimmed("org.netbeans.modules.refactoring.java.ui.Bundle", "LBL_WhereUsedAction"); // Find Usages
    private static final String findUsagesMenu = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Menu/Edit") // Edit
            + "|"
            + findUsagesPopup;
    private static final KeyStroke keystroke = System.getProperty("os.name").toLowerCase().indexOf("mac") > -1 ?
            KeyStroke.getKeyStroke(KeyEvent.VK_F7, KeyEvent.META_MASK) :
            KeyStroke.getKeyStroke(KeyEvent.VK_F7, KeyEvent.ALT_MASK);

    /**
     * creates new FindUsagesAction instance
     */
    public FindUsagesAction() {
        super(findUsagesMenu, findUsagesPopup, null, keystroke);
    }
}
