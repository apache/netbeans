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
package org.netbeans.modules.visual.layout;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.SceneLayout;

/**
 * @author David Kaspar
 */
public final class DevolveWidgetLayout extends SceneLayout {

    private Widget widget;
    private Layout devolveLayout;
    private boolean animate;

    public DevolveWidgetLayout (Widget widget, Layout devolveLayout, boolean animate) {
        super (widget.getScene ());
        assert devolveLayout != null;
        this.widget = widget;
        this.devolveLayout = devolveLayout;
        this.animate = animate;
    }

    protected void performLayout () {
        devolveLayout.layout (widget);
        for (Widget child : widget.getChildren ()) {
            if (animate)
                widget.getScene ().getSceneAnimator ().animatePreferredLocation (child, child.getLocation ());
            else
                child.setPreferredLocation (child.getLocation ());
            child.revalidate ();
        }
    }

}
