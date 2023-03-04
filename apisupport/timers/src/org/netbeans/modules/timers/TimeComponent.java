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
package org.netbeans.modules.timers;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import org.openide.ErrorManager;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jan Lahoda
 */
public class TimeComponent extends TopComponent {

    private static final String PREFERRED_ID = "timers"; //NOI18N
            static final String ICON_PATH = "org/netbeans/modules/timers/resources/timer.png"; //NOI18N
    private static TimeComponent INSTANCE;
    
    /**
     * Creates a new instance of TimeComponent
     */
    public TimeComponent() {
        setName ("timers"); //NOI18N
        setDisplayName (NbBundle.getMessage ( TimeComponent.class, "LBL_TimeComponent" )); //NOI18N
        setIcon(ImageUtilities.loadImage(ICON_PATH));
        
        setLayout(new GridBagLayout());
        
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;

        add(new TimeComponentPanel(), gridBagConstraints);
    }

    public @Override String preferredID () {
        return PREFERRED_ID;
    }
    

    public @Override int getPersistenceType () {
        return PERSISTENCE_ALWAYS;
    }
    
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized TimeComponent getDefault() {
        if (INSTANCE == null) {
            INSTANCE = new TimeComponent();
        }
        return INSTANCE;
    }
    
    public static synchronized TimeComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "Cannot find TimeComponent component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof TimeComponent) {
            return (TimeComponent)win;
        }
        ErrorManager.getDefault().log(ErrorManager.WARNING,
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }
    
}
