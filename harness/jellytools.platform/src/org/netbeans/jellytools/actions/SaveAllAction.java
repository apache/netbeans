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
import org.netbeans.jellytools.Bundle;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.openide.LifecycleManager;
import org.openide.loaders.DataObject;

/**
 * Used to call "File|Save All" main menu item or to do an API call.
 * <br>
 * After action is performed it waits until it is finished.
 *
 * @see Action
 * @author Jiri Skrivanek
 */
public class SaveAllAction extends Action {

    /**
     * "File|Save All"
     */
    private static final String saveAllMenu =
            Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Menu/File")
            + "|"
            + Bundle.getStringTrimmed("org.openide.loaders.Bundle", "SaveAll");
    private static final KeyStroke keystroke = System.getProperty("os.name").toLowerCase().indexOf("mac") > -1
            ? KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK)
            : KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);

    /**
     * Creates new SaveAllAction instance.
     */
    public SaveAllAction() {
        super(saveAllMenu, null, null, keystroke);
    }

    /**
     * Performs action through main menu and wait until action is not finished.
     */
    @Override
    public void performMenu() {
        super.performMenu();
        waitFinished();
    }

    /**
     * Performs action through API call and wait until action is not finished.
     */
    @Override
    public void performAPI() {
        LifecycleManager.getDefault().saveAll();
        waitFinished();
    }

    /**
     * Waits until SaveAllAction is finished.
     */
    private void waitFinished() {
        try {
            new Waiter(new Waitable() {
                @Override
                public Object actionProduced(Object systemAction) {
                    return DataObject.getRegistry().getModifiedSet().isEmpty() ? Boolean.TRUE : null;
                }

                @Override
                public String getDescription() {
                    return "SaveAllAction is finished";
                }
            }).waitAction(null);
        } catch (InterruptedException e) {
            throw new JemmyException("Waiting interrupted.", e);
        }
    }
}
