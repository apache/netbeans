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
package org.netbeans.modules.web.browser.ui;

import javax.swing.Icon;
import javax.swing.JToggleButton;
import org.netbeans.modules.web.browser.api.ResizeOption;
import org.openide.util.ImageUtilities;

/**
 * Button to resize the browser window.
 * 
 * @author S. Aubrecht
 */
class BrowserResizeButton extends JToggleButton {

    private final ResizeOption resizeOption;
    private static final String ICON_PATH_PREFIX = "org/netbeans/modules/web/browser/ui/resources/"; //NOI18N

    private BrowserResizeButton( ResizeOption resizeOption ) {
        this.resizeOption = resizeOption;
        setIcon( toIcon( resizeOption ) );
        setToolTipText( resizeOption.getToolTip() );
    }

    ResizeOption getResizeOption() {
        return resizeOption;
    }

    static BrowserResizeButton create( ResizeOption resizeOption ) {
        return new BrowserResizeButton( resizeOption );
    }

    static Icon toIcon( ResizeOption ro ) {
        if( ro == ResizeOption.SIZE_TO_FIT )
            return ImageUtilities.loadImageIcon( ICON_PATH_PREFIX+"sizeToFit.png", true ); //NOI18N
        return toIcon( ro.getType() );
    }

    static Icon toIcon( ResizeOption.Type type ) {
        switch( type ) {
            case NETBOOK: return ImageUtilities.loadImageIcon( ICON_PATH_PREFIX+"netbook.png", true ); //NOI18N
            case SMARTPHONE_LANDSCAPE: return ImageUtilities.loadImageIcon( ICON_PATH_PREFIX+"handheldLandscape.png", true ); //NOI18N
            case SMARTPHONE_PORTRAIT: return ImageUtilities.loadImageIcon( ICON_PATH_PREFIX+"handheldPortrait.png", true ); //NOI18N
            case TABLET_LANDSCAPE: return ImageUtilities.loadImageIcon( ICON_PATH_PREFIX+"tabletLandscape.png", true ); //NOI18N
            case TABLET_PORTRAIT: return ImageUtilities.loadImageIcon( ICON_PATH_PREFIX+"tabletPortrait.png", true ); //NOI18N
            case WIDESCREEN: return ImageUtilities.loadImageIcon( ICON_PATH_PREFIX+"widescreen.png", true ); //NOI18N
        }
        return ImageUtilities.loadImageIcon( ICON_PATH_PREFIX+"desktop.png", true ); //NOI18N
    }
}
