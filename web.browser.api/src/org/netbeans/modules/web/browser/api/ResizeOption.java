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
package org.netbeans.modules.web.browser.api;

import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * Immutable value class describing a single button to resize web browser window.
 *
 * @author S. Aubrecht
 */
public final class ResizeOption {

    private final Type type;
    private final String displayName;
    private final int width;
    private final int height;
    private final boolean showInToolbar;
    private final boolean isDefault;

    public enum Type {
        DESKTOP,
        TABLET_PORTRAIT,
        TABLET_LANDSCAPE,
        SMARTPHONE_PORTRAIT,
        SMARTPHONE_LANDSCAPE,
        WIDESCREEN,
        NETBOOK,
        CUSTOM;
    }

    private ResizeOption( Type type, String displayName, int width, int height, boolean showInToolbar, boolean isDefault ) {
        Parameters.notEmpty( "displayName", displayName ); //NOI18N
        Parameters.notNull( "type", type ); //NOI18N
        this.type = type;
        this.displayName = displayName;
        this.width = width;
        this.height = height;
        this.showInToolbar = showInToolbar;
        this.isDefault = isDefault;
    }

    /**
     * Creates a new instance.
     * @param type
     * @param displayName Display name to show in tooltip, cannot be empty.
     * @param width Screen width
     * @param height Screen height
     * @param showInToolbar True to show in web developer toolbar.
     * @param isDefault True if this is a predefined option that cannot be removed.
     * @return New instance.
     */
    public static ResizeOption create( Type type, String displayName, int width, int height, boolean showInToolbar, boolean isDefault ) {
        if( width <= 0 || height <= 0 )
            throw new IllegalArgumentException( "Invalid screen dimensions: " + width + " x " + height ); //NOI18N
        return new ResizeOption( type, displayName, width, height, showInToolbar, isDefault );
    }

    /**
     * An extra option to size the browser content to fit its window.
     */
    public static final ResizeOption SIZE_TO_FIT = new ResizeOption( Type.CUSTOM, 
            NbBundle.getMessage(ResizeOption.class, "Lbl_AUTO"), -1, -1, true, true );


    public String getDisplayName() {
        return displayName;
    }

    public Type getType() {
        return type;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isShowInToolbar() {
        return showInToolbar;
    }

    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public String getToolTip() {
        if( width < 0 || height < 0 )
            return displayName;
        StringBuilder sb = new StringBuilder();
        sb.append( width );
        sb.append( " x " ); //NOI18N
        sb.append( height );
        sb.append( " ("); //NOI18N
        sb.append( displayName );
        sb.append( ')' ); //NOI18N
        return sb.toString();
    }

    @Override
    public boolean equals( Object obj ) {
        if( obj == null ) {
            return false;
        }
        if( getClass() != obj.getClass() ) {
            return false;
        }
        final ResizeOption other = ( ResizeOption ) obj;
        if( this.type != other.type ) {
            return false;
        }
        if( (this.displayName == null) ? (other.displayName != null) : !this.displayName.equals( other.displayName ) ) {
            return false;
        }
        if( this.width != other.width ) {
            return false;
        }
        if( this.height != other.height ) {
            return false;
        }
        if( this.showInToolbar != other.showInToolbar ) {
            return false;
        }
        if( this.isDefault != other.isDefault ) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 11 * hash + (this.displayName != null ? this.displayName.hashCode() : 0);
        hash = 11 * hash + this.width;
        hash = 11 * hash + this.height;
        hash = 11 * hash + (this.showInToolbar ? 1 : 0);
        hash = 11 * hash + (this.isDefault ? 1 : 0);
        return hash;
    }
}
