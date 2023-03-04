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
package org.netbeans.modules.editor.actions;

import java.awt.event.ActionEvent;
import java.util.Map;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionNames;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.EditorActionRegistrations;
import org.netbeans.spi.editor.AbstractEditorAction;

/**
 * Zoom editor pane's text in/out.
 *
 * @author Miloslav Metelka
 * @since 1.11
 */
@EditorActionRegistrations({
    @EditorActionRegistration(
        name = EditorActionNames.zoomTextIn
    ),
    @EditorActionRegistration(
        name = EditorActionNames.zoomTextOut
    )
})
public class ZoomTextAction extends AbstractEditorAction {
    
    private static final long serialVersionUID = 1L;
    private static final String TEXT_ZOOM_PROPERTY = "text-zoom"; // Defined in DocumentView in editor.lib2
    
    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        String actionName = actionName();
        int delta = (EditorActionNames.zoomTextIn.equals(actionName)) ? +1 : -1;
        if (target != null) {
            int newZoom = 0;
            Integer currentZoom = (Integer) target.getClientProperty(TEXT_ZOOM_PROPERTY);
            if (currentZoom != null) {
                newZoom += currentZoom;
            }
            newZoom += delta;
            target.putClientProperty(TEXT_ZOOM_PROPERTY, newZoom);
        }
    }

}
