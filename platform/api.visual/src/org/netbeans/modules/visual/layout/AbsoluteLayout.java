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

import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.widget.Widget;

import java.util.Collection;
import java.awt.*;

/**
 * @author David Kaspar
 */
public final class AbsoluteLayout implements Layout {

    public void layout (Widget widget) {
        Collection<Widget> children = widget.getChildren ();
        for (Widget child : children) {
            if (child.isVisible ())
                child.resolveBounds (child.getPreferredLocation (), null);
            else
                child.resolveBounds (null, new Rectangle ());
        }
    }

    public boolean requiresJustification (Widget widget) {
        return false;
    }

    public void justify (Widget widget) {
    }

}
