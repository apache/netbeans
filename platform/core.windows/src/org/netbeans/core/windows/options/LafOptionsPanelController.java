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

package org.netbeans.core.windows.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.core.WindowSystem;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@OptionsPanelController.SubRegistration(
    displayName="#Laf_DisplayName",
    keywords="#KW_LafOptions",
    keywordsCategory="Appearance/LaF",
    location = "Appearance"
)
public class LafOptionsPanelController extends OptionsPanelController {

    private LafPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;

    @Override
    public void update() {
        getPanel().load();
        changed = false;
    }

    @Override
    public void applyChanges() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                boolean refreshWinsys = getPanel().store();
                changed = false;
                if (refreshWinsys) {
                    WindowSystem ws = Lookup.getDefault().lookup(WindowSystem.class);
                    ws.hide();
                    ws.show();
                }
            }
        });
    }

    @Override
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    @Override
    public boolean isValid() {
        return getPanel().valid();
    }

    @Override
    public boolean isChanged() {
        return changed;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx( "org.netbeans.core.windows.options.LafOptionsPanelController" ); //NOI18N
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    protected LafPanel getPanel() {
        if (panel == null) {
            panel = new LafPanel(this);
        }
        return panel;
    }

    @Override
    protected void setCurrentSubcategory( String subpath ) {
        if( "LaF".equals( subpath ) ) { //NOI18N
            SwingUtilities.invokeLater( new Runnable() {
                @Override
                public void run() {
                    getPanel().selectDarkLookAndFeel();
                }
            });
        }
    }

    protected void changed(boolean isChanged) {
        if (!changed) {
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        changed = isChanged;
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }
}
