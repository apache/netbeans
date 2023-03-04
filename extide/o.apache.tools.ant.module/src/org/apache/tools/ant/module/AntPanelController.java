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

package org.apache.tools.ant.module;

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.modules.options.java.api.JavaOptions;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
@OptionsPanelController.SubRegistration(
    location=JavaOptions.JAVA,
    id=AntPanelController.OPTIONS_SUBPATH,
    displayName="#Ant",
    keywords="#KW_AntOptions",
    keywordsCategory=JavaOptions.JAVA + "/Ant"
//    toolTip="#Ant_Tooltip"
)
public final class AntPanelController extends OptionsPanelController {

    public static final String OPTIONS_SUBPATH = "Ant"; // NOI18N

    @Override
    public void update () {
        getAntCustomizer ().update ();
    }

    @Override
    public void applyChanges () {
        getAntCustomizer ().applyChanges ();
    }
    
    @Override
    public void cancel () {
        getAntCustomizer ().cancel ();
    }
    
    @Override
    public boolean isValid () {
        return getAntCustomizer ().dataValid ();
    }
    
    @Override
    public boolean isChanged () {
        return getAntCustomizer ().isChanged ();
    }
    
    @Override
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("netbeans.optionsDialog.advanced.ant");
    }
    
    @Override
    public JComponent getComponent (Lookup lookup) {
        return getAntCustomizer ();
    }

    @Override
    public void addPropertyChangeListener (PropertyChangeListener l) {
        getAntCustomizer ().addPropertyChangeListener (l);
    }

    @Override
    public void removePropertyChangeListener (PropertyChangeListener l) {
        getAntCustomizer ().removePropertyChangeListener (l);
    }

    
    private AntCustomizer antCustomizer;
    
    private AntCustomizer getAntCustomizer () {
        if (antCustomizer == null)
            antCustomizer = new AntCustomizer ();
        return antCustomizer;
    }
}
