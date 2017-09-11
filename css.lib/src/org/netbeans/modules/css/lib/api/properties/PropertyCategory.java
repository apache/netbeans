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
package org.netbeans.modules.css.lib.api.properties;

import org.netbeans.modules.css.lib.api.CssModule;

/**
 * Represents a category of css properties.
 *
 * For example: font, background, color, ...
 *
 * In theory the {@link CssModule} could serve as the category identificator.
 * Quite often however the css specification (module) contains groups of
 * logically separated properties - for example the specification "CSS Borders
 * and Backgrounds" provides two groups of properties serving to quite different
 * purpose: border and background;
 *
 * @author marekfukala
 */
//TODO I18N
//TODO defined the displaynames and descriptions
public enum PropertyCategory {

    ANIMATIONS,
    BOX,
    BACKGROUND,
    COLORS,
    CONTENT, //generated & repl. content
    FLEXIBLE_BOX_LAYOUT,
    FONTS,
    GRID,
    HYPERLINKS,
    IMAGES,
    LINE,
    LISTS_AND_COUNTERS,
    MARQUEE,
    MULTI_COLUMN_LAYOUT,
    PAGED_MEDIA,
    RUBY,
    SPEECH,
    TEXT,
    TRANSFORMATIONS_2D,
    TRANSFORMATIONS_3D,
    TRANSITIONS,
    USER_INTERFACE,
    WRITING_MODES,
    
    //browsers
    CHROME,
    FIREFOX,
    INTERNET_EXPLORER,
    OPERA,
    SAFARI,
    
    //default, the rest
    DEFAULT, 
    
    //unknown
    UNKNOWN;
    
    private String displayName;
    private String shortDescription;
    private String longDescription;

    private PropertyCategory() {
        displayName = new StringBuilder()
                .append(name().charAt(0))
                .append(name().substring(1).toLowerCase().replace('_', ' '))
                .toString();
        
        shortDescription = new StringBuilder()
                .append("Provides styling support for ")
                .append(getDisplayName())
                .append('.')
                .toString();
        
        longDescription = shortDescription;
    }

    private PropertyCategory(String displayName, String shortDescription, String longDescription) {
        this.displayName = displayName;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }
    
}
