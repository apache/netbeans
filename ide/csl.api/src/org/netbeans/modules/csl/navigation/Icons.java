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
package org.netbeans.modules.csl.navigation;

import java.awt.Image;
import java.util.Collection;
import java.util.Collections;
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

//    public static Icon getBusyIcon() {
//        Image img = Utilities.loadImage(WAIT);
//
//        if (img == null) {
//            return null;
//        } else {
//            return new ImageIcon(img);
//        }
//    }
//
//    public static Icon getMethodIcon() {
//        // TODO - consider modifiers
//        Image img =
//            Utilities.loadImage(ICON_BASE + "method" + "Public" + PNG_EXTENSION);
//
//        if (img == null) {
//            return null;
//        } else {
//            return new ImageIcon(img);
//        }
//    }
//
//    public static Icon getFieldIcon() {
//        // TODO - consider modifiers
//        Image img =
//            Utilities.loadImage(ICON_BASE + "field" + "Public" + PNG_EXTENSION);
//
//        if (img == null) {
//            return null;
//        } else {
//            return new ImageIcon(img);
//        }
//    }
//
//    public static Icon getClassIcon() {
//        Image img = Utilities.loadImage(ICON_BASE + "class" + PNG_EXTENSION);
//
//        if (img == null) {
//            return null;
//        } else {
//            return new ImageIcon(img);
//        }
//    }
//
//    public static Icon getModuleIcon() {
//        Image img =
//            Utilities.loadImage(ICON_BASE + "package"  + GIF_EXTENSION);
//
//        if (img == null) {
//            return null;
//        } else {
//            return new ImageIcon(img);
//        }
//    }

    public static ImageIcon getElementIcon( ElementKind elementKind, Collection<Modifier> modifiers ) {
    
        if ( modifiers == null ) {
            modifiers = Collections.<Modifier>emptyList();
        }
    
        Image img = null;
    
        switch( elementKind ) {
        case FILE:
            img = ImageUtilities.loadImage( ICON_BASE + "emptyfile-icon" + PNG_EXTENSION );
            break;
        case ERROR:
            img = ImageUtilities.loadImage( ICON_BASE + "error-glyph" + GIF_EXTENSION );
            break;
        case PACKAGE:
        case MODULE:
            img = ImageUtilities.loadImage( ICON_BASE + "package" + GIF_EXTENSION );
            break;
        case TEST:
            img = ImageUtilities.loadImage( ICON_BASE + "test" + PNG_EXTENSION );
            break;
        case CLASS:
        case INTERFACE:
            img = ImageUtilities.loadImage( ICON_BASE + "class" + PNG_EXTENSION );
            break;
        case TAG:
            img = ImageUtilities.loadImage( ICON_BASE + "html_element" + PNG_EXTENSION );
            break;
        case RULE:
            img = ImageUtilities.loadImage( ICON_BASE + "rule" + PNG_EXTENSION );
            break;
        case VARIABLE:
        case PROPERTY:
        case GLOBAL:
        case ATTRIBUTE:
        case FIELD:
            img = ImageUtilities.loadImage( getIconName( ICON_BASE + "field", PNG_EXTENSION, modifiers ) );
            break;
        case PARAMETER:
        case CONSTANT:
            img = ImageUtilities.loadImage(getIconName(ICON_BASE + "constant", PNG_EXTENSION, modifiers)); // NOI18N
            if (img == null) {
                img = ImageUtilities.loadImage(ICON_BASE + "constantPublic" + PNG_EXTENSION); // NOI18N
            }
            break;
        case CONSTRUCTOR:
            img = ImageUtilities.loadImage( getIconName( ICON_BASE + "constructor", PNG_EXTENSION, modifiers ) );
            break;
        case METHOD:
            img = ImageUtilities.loadImage( getIconName( ICON_BASE + "method", PNG_EXTENSION, modifiers ) );
            break;
        case DB:
            img = ImageUtilities.loadImage(ICON_BASE + "database" + GIF_EXTENSION);
            break;
        default:   
                img = null;
        }
    
        return img == null ? null : new ImageIcon (img);
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
