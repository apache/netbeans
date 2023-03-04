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
package org.netbeans.modules.ide.ergonomics.fod;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.util.concurrent.Callable;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class OptionCntrl extends OptionsPanelController 
implements Callable<JComponent>, Runnable {
    private final FileObject fo;
    private ConfigurationPanel panel;
    private Lookup master;

    public OptionCntrl(FileObject fo) {
        this.fo = fo;
    }
    
    static OptionsPanelController advanced(FileObject fo) {
        return new OptionCntrl(fo);
    }
    
    static OptionsPanelController basic(FileObject fo) {
        return new OptionCntrl(fo);
    }

    @Override
    public void update() {
    }

    @Override
    public void applyChanges() {
    }

    @Override
    public void cancel() {
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isChanged() {
        return false;
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        if (panel == null) {
            FeatureInfo info = FoDLayersProvider.getInstance().whichProvides(fo);
            assert info != null;
            master = masterLookup;
            panel = new ConfigurationPanel(info.clusterName, this, info);
        }
        return panel;
    }

    @Override
    public JComponent call() throws Exception {
        assert EventQueue.isDispatchThread();
        // it would be better not to close the dialog...
        EventQueue.invokeLater(this);
        return new JButton();
    }
    
    @Override
    public void run() {
        OptionsDisplayer.getDefault().open();
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }

}
