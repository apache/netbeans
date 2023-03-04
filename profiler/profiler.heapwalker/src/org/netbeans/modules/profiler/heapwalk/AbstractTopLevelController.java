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

import java.awt.Insets;
import javax.swing.AbstractButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 *
 * @author Jiri Sedlacek
 */
public abstract class AbstractTopLevelController extends AbstractController {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private AbstractButton[] clientPresenters;

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    // --- Public interface ------------------------------------------------------
    public AbstractButton[] getClientPresenters() {
        if (clientPresenters == null) {
            clientPresenters = createClientPresenters();

            for (int i = 0; i < clientPresenters.length; i++)
                registerClientPresenterListener(clientPresenters[i]);

            updateClientPresentersEnabling(clientPresenters);
        }

        return clientPresenters;
    }

    // --- Protected implementation ----------------------------------------------
    protected abstract AbstractButton[] createClientPresenters();

    protected void updateClientPresentersEnabling(AbstractButton[] clientPresenters) {
        int disabledPresenterIndex = -1;

        int selectedPresenterIndex = -1;
        int unselectedPresentersCount = 0;

        for (int i = 0; i < clientPresenters.length; i++) {
            if (clientPresenters[i].isSelected()) {
                selectedPresenterIndex = i;
            } else {
                unselectedPresentersCount++;
            }

            if (!clientPresenters[i].isEnabled()) {
                disabledPresenterIndex = i;
            }
        }

        if (unselectedPresentersCount == (clientPresenters.length - 1)) {
            if (disabledPresenterIndex == -1) {
                clientPresenters[selectedPresenterIndex].setEnabled(false);
            }
        } else {
            if (disabledPresenterIndex != -1) {
                clientPresenters[disabledPresenterIndex].setEnabled(true);
            }
        }
    }

    // --- Private implementation ------------------------------------------------
    private void registerClientPresenterListener(AbstractButton presenter) {
        presenter.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    updateClientPresentersEnabling(getClientPresenters());
                }
            });
    }
}
