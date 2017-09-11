/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
