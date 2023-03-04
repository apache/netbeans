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

package org.netbeans.modules.options.editor.spi;

import javax.swing.JComponent;

/**
 * This interface should be implemented by all option customizers that wish
 * to provide a preview of changes in the options they maintain.
 *
 * <p>An example is the Editor -> Formatting panel in Tools-Options, which presents
 * customizers for many mime types and these customizers have to share the same
 * preview area. Each of those customizers should provide implementation of this
 * interface in order to supply the contents of the preview area.
 *
 * <p>Typically option customizers are supplied in form of <code>OptionsPanelController</code>,
 * which provides the customizer's UI component and allows to control it. If such
 * a customizer wishes to provide a preview component it should do so by
 * letting its <code>OptionsPanelController</code> implementation to also implement
 * the <code>PreviewProvider</code> interface.
 *
 * @author Vita Stejskal
 */
public interface PreviewProvider {

    /**
     * Gets the component that will be used for previewing changes in the
     * associated options panel.
     *
     * @return The preview component.
     */
    public JComponent getPreviewComponent();

    /**
     * Refreshes the preview component. The implementation should use the current
     * option values to refrfesh the preview component.
     *
     * <p>Normally option panels are responsible for refreshing their preview
     * components whenever the options that they maintain are changed. This method
     * take out this obligation. It is here for the options UI infrastructure
     * to be able to enforece the preview refresh.
     */
    public void refreshPreview();

}
