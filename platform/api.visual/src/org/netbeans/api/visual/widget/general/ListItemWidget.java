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
package org.netbeans.api.visual.widget.general;

import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.laf.LookFeel;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.visual.util.GeomUtil;

/**
 * This class represents a general list item widget. Right now it presented as a label.
 *
 * @deprecated
 * @author David Kaspar
 */
@Deprecated
public class ListItemWidget extends LabelWidget {

    /**
     * Creates a list item widget.
     * @param scene the scene
     */
    public ListItemWidget (Scene scene) {
        super (scene);
        GeomUtil.LOG.warning ("org.netbeans.api.visual.widget.general.ListItemWidget class is deprecated. Use org.netbeans.modules.visual.experimental.widget.general.ListItemWidget class instead. Since it is an experimental class outside of public-API packages, you have to set an implementation dependency on the org.netbeans.api.visual module."); // NOI18N

        setState (ObjectState.createNormal ());
    }

    /**
     * Implements the widget-state specific look of the widget.
     * @param previousState the previous state
     * @param state the new state
     */
    public void notifyStateChanged (ObjectState previousState, ObjectState state) {
        LookFeel lookFeel = getScene ().getLookFeel ();
        setBorder (BorderFactory.createCompositeBorder (BorderFactory.createEmptyBorder (8, 2), lookFeel.getMiniBorder (state)));
        setForeground (lookFeel.getForeground (state));
    }

}
