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

package org.netbeans.modules.profiler.heapwalk;

import javax.swing.AbstractButton;
import javax.swing.JPanel;


/**
 *
 * @author Jiri Sedlacek
 */
public abstract class AbstractController {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private AbstractButton presenter;
    private JPanel controllerUI;

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    // --- Public interface ------------------------------------------------------
    public JPanel getPanel() {
        if (controllerUI == null) {
            controllerUI = createControllerUI();
            controllerUI.setOpaque(false);
        }

        return controllerUI;
    }

    public AbstractButton getPresenter() {
        if (presenter == null) {
            presenter = createControllerPresenter();
        }

        return presenter;
    }

    protected abstract AbstractButton createControllerPresenter();

    // --- Protected implementation ----------------------------------------------
    protected abstract JPanel createControllerUI();
}
