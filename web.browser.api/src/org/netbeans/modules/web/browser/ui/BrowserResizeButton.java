/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
