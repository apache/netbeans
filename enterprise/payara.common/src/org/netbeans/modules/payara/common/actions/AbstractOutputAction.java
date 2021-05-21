/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.payara.common.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.WeakListeners;
import org.netbeans.modules.payara.spi.PayaraModule;

/**
 *
 * @author Peter Williams
 */
public abstract class AbstractOutputAction extends AbstractAction implements ChangeListener {

    private static final String PROP_ENABLED = "enabled"; // NOI18N

    protected final PayaraModule commonSupport;

    public AbstractOutputAction(final PayaraModule commonSupport, 
            String localizedName, String localizedShortDesc, String iconBase) {
        super(localizedName, ImageUtilities.loadImageIcon(iconBase, false));
        putValue(SHORT_DESCRIPTION, localizedShortDesc);
        this.commonSupport = commonSupport;

        // listen for server state changes
        commonSupport.addChangeListener(WeakListeners.change(this, commonSupport));
    }

    public abstract void actionPerformed(ActionEvent e);

    @Override
    public abstract boolean isEnabled();

    // --------------------------------------------------------------------
    // ChangeListener interface implementation
    // --------------------------------------------------------------------
    public void stateChanged(ChangeEvent evt) {
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                firePropertyChange(PROP_ENABLED, null, isEnabled() ? Boolean.TRUE : Boolean.FALSE);
            }
        });
    }
}
