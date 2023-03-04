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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Martin Adamek
 */
public class GoToSourceAction extends AbstractAction implements Presenter.Popup {

    private final DataObject classDO;
    private final String actionName;
    
    public GoToSourceAction(DataObject classDO, String actionName) {
        this.actionName = actionName;
        this.classDO = classDO;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        openSourceDO(classDO);
    }

    public Object getValue(String key) {
        if (NAME.equals(key)) {
            return actionName;
        }
        else {
            return super.getValue(key);
        }
    }

    public JMenuItem getPopupPresenter() {
        return new JMenuItem (this);
    }

    // private helpers =========================================================
    
    /*
     * from NbJavaFastOpen
     */
    private void openSourceDO(DataObject dataObject){
        if (dataObject != null) {
            EditCookie editCookie = (EditCookie)dataObject.getCookie(EditCookie.class);
            if (editCookie != null) {
                editCookie.edit();
            } else {
                OpenCookie openCookie = (OpenCookie)dataObject.getCookie(OpenCookie.class);
                if (openCookie != null) {
                    openCookie.open();
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        }
    }

}
