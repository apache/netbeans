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
package org.netbeans.api.visual.widget;

import java.awt.*;

/**
 * The layer widget represents a transparent widget which functionality is similar to JGlassPane.
 * The layer widget is used for speed optimalization too since it is not repainted when the widget is re-layout.
 * <p>
 * It can be used widgets organization. A scene usually has layer widgets directly underneath.
 * E.g. each layer widget is used different purpose:
 * background for widgets on background,
 * main layer for node widgets,
 * connection layer for edge widgets,
 * interraction layer for temporary widgets created/used by actions.
 *
 * @author David Kaspar
 */
public class LayerWidget extends Widget {

    /**
     * Creates a layer widget.
     * @param scene the scene
     */
    public LayerWidget (Scene scene) {
        super (scene);
    }

    /**
     * Returns whether a specified local location is part of the layer widget.
     * @param localLocation the local location
     * @return always false
     */
    public boolean isHitAt (Point localLocation) {
        return false;
    }

    /**
     * Returns whether the layer widget requires to repainted after revalidation.
     * @return always false
     */
    protected boolean isRepaintRequiredForRevalidating () {
        return false;
    }

    void layout (boolean fullValidation) {
        super.layout (fullValidation);
        resolveBounds (getPreferredLocation(), null);
        justify ();
    }

}
