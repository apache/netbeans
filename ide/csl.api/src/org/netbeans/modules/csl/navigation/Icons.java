/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.csl.navigation;

import java.util.Collection;
import java.util.Collections;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.openide.util.ImageUtilities;


/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 * @todo Perhaps include icons from http://xdesign-tools.czech.sun.com/visualdesign/prehled/index.html
 * @author phrebejk
 */
public final class Icons {
    private static final String ICON_BASE = "org/netbeans/modules/csl/source/resources/icons/";
    private static final String GIF_EXTENSION = ".gif";
    private static final String PNG_EXTENSION = ".png";
    private static final String WAIT = ICON_BASE + "wait" + PNG_EXTENSION;
    //private static final Map<String, Icon> icons = new HashMap<String, Icon>();

    /** Creates a new instance of Icons */
    private Icons() {
    }

    public static ImageIcon getElementIcon( ElementKind elementKind, Collection<Modifier> modifiers ) {
    
        if ( modifiers == null ) {
            modifiers = Collections.<Modifier>emptyList();
        }
    
        Icon icon = null;
    
        switch( elementKind ) {
        case FILE:
            icon = ImageUtilities.loadIcon( ICON_BASE + "emptyfile-icon" + PNG_EXTENSION );
            break;
        case ERROR:
            icon = ImageUtilities.loadIcon( ICON_BASE + "error-glyph" + GIF_EXTENSION );
            break;
        case PACKAGE:
        case MODULE:
            icon = ImageUtilities.loadIcon( ICON_BASE + "package" + GIF_EXTENSION );
            break;
        case TEST:
            icon = ImageUtilities.loadIcon( ICON_BASE + "test" + PNG_EXTENSION );
            break;
        case CLASS:
        case INTERFACE:
            icon = ImageUtilities.loadIcon( ICON_BASE + "class" + PNG_EXTENSION );
            break;
        case TAG:
            icon = ImageUtilities.loadIcon( ICON_BASE + "html_element" + PNG_EXTENSION );
            break;
        case RULE:
            icon = ImageUtilities.loadIcon( ICON_BASE + "rule" + PNG_EXTENSION );
            break;
        case VARIABLE:
        case PROPERTY:
        case GLOBAL:
        case ATTRIBUTE:
        case FIELD:
            icon = ImageUtilities.loadIcon( getIconName( ICON_BASE + "field", PNG_EXTENSION, modifiers ) );
            break;
        case PARAMETER:
        case CONSTANT:
            icon = ImageUtilities.loadIcon(getIconName(ICON_BASE + "constant", PNG_EXTENSION, modifiers)); // NOI18N
            if (icon == null) {
                icon = ImageUtilities.loadIcon(ICON_BASE + "constantPublic" + PNG_EXTENSION); // NOI18N
            }
            break;
        case CONSTRUCTOR:
            icon = ImageUtilities.loadIcon( getIconName( ICON_BASE + "constructor", PNG_EXTENSION, modifiers ) );
            break;
        case METHOD:
            icon = ImageUtilities.loadIcon( getIconName( ICON_BASE + "method", PNG_EXTENSION, modifiers ) );
            break;
        case DB:
            icon = ImageUtilities.loadIcon(ICON_BASE + "database" + GIF_EXTENSION);
            break;
        default:   
                icon = null;
        }
    
        return icon == null ? null : ImageUtilities.icon2ImageIcon (icon);
    }
        
    // Private Methods ---------------------------------------------------------
    private static String getIconName(String typeName, String extension, Collection<Modifier> modifiers) {

        StringBuffer fileName = new StringBuffer( typeName );

        if (modifiers.contains(Modifier.STATIC)) {
            fileName.append( "Static" );
        }
        if (modifiers.contains(Modifier.PROTECTED)) {
            return fileName.append( "Protected" ).append( extension ).toString();
        }
        if (modifiers.contains(Modifier.PRIVATE)) {
            return fileName.append( "Private" ).append( extension ).toString();
        }
        // Assume it's public
        return fileName.append( "Public" ).append( extension ).toString();
        //return fileName.append( "Package" ).append( extension ).toString();
        //return fileName.append(extension).toString();
    }
}
