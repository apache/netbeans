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

package org.netbeans.modules.profiler.snaptracer.impl;

import javax.swing.JComponent;
import org.netbeans.modules.profiler.snaptracer.impl.details.DetailsPanel;
import org.netbeans.modules.profiler.snaptracer.impl.swing.VisibilityHandler;
import org.netbeans.modules.profiler.snaptracer.impl.timeline.TimelineSupport;

/**
 *
 * @author Jiri Sedlacek
 */
final class DetailsView {

    private final TimelineSupport timelineSupport;
    private DetailsPanel panel;

    private boolean hasData;

    private VisibilityHandler viewHandler;

    // --- Constructor ---------------------------------------------------------

    DetailsView(TracerModel model) {
        timelineSupport = model.getTimelineSupport();
    }


    // --- Internal interface --------------------------------------------------

    void registerViewListener(VisibilityHandler viewHandler) {
        if (panel != null) {
            viewHandler.handle(panel);
        } else {
            this.viewHandler = viewHandler;
        }

    }

    boolean isShowing() {
        return panel != null && panel.isShowing();
    }

    boolean hasData() {
        return hasData;
    }


    // --- UI implementation ---------------------------------------------------

    JComponent getView() {
        panel = new DetailsPanel(timelineSupport);

        timelineSupport.addSelectionListener(new TimelineSupport.SelectionListener() {
            public void intervalsSelectionChanged() {}
            public void indexSelectionChanged() {}
            public void timeSelectionChanged(boolean timestampsSelected, boolean justHovering) {}
        });
        
        if (viewHandler != null) {
            viewHandler.handle(panel);
            viewHandler = null;
        }

        return panel;
    }

}
