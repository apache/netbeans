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

package org.netbeans.modules.apisupport.project.ui.branding;

import org.netbeans.modules.apisupport.project.spi.BrandingModel;
import javax.swing.JPanel;

/**
 * A tab in branding editor window.
 *
 * @author S. Aubrecht
 */
abstract class AbstractBrandingPanel extends JPanel {

    private final BrandingModel model;
    private BrandingEditorPanel editor;
    private final String displayName;
    private boolean brandingValid = true;
    private String errMessage = null;

    /**
     * C'tor
     * @param displayName Tab's display name.
     * @param model Branding model
     */
    protected AbstractBrandingPanel( String displayName, BrandingModel model ) {
        this.displayName = displayName;
        this.model = model;
    }

    final void init( BrandingEditorPanel editor ) {
        this.editor = editor;
    }

    protected final BrandingModel getBranding() {
        return model;
    }

    public abstract void store();

    protected final void setErrorMessage( String errMessage ) {
        this.errMessage = errMessage;
        notifyEditor();
    }

    final String getErrorMessage() {
        return errMessage;
    }

    protected final void setValid( boolean valid ) {
        this.brandingValid = valid;
        notifyEditor();
    }

    final boolean isBrandingValid() {
        return brandingValid && null == errMessage;
    }

    protected final void setModified() {
        editor.setModified();
    }

    public final String getDisplayName() {
        return displayName;
    }

    private void notifyEditor() {
        if( null == editor )
            return;
        editor.onBrandingValidation();
    }
}
