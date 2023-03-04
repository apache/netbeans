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
package org.netbeans.modules.web.inspect.webkit;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.web.browser.api.Page;
import org.netbeans.modules.web.inspect.PageModel;

/**
 * Page model listener that is responsible for focusing/opening
 * of the inspected files in editor.
 *
 * @author Jan Stola
 */
public class EditorSynchronizer implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        WebKitPageModel pageModel = (WebKitPageModel)evt.getSource();
        if (propName.equals(Page.PROP_BROWSER_SELECTED_NODES)) {
            org.netbeans.modules.web.inspect.ui.Utilities.focusInspectedFile(pageModel);
        } else if (propName.equals(PageModel.PROP_SELECTION_MODE)) {
            if (pageModel.isSelectionMode()) {
                org.netbeans.modules.web.inspect.ui.Utilities.focusInspectedFile(pageModel);
            }
        }
    }

}
