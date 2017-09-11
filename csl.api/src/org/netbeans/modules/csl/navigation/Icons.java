/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
