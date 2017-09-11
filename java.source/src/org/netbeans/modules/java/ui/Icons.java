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

package org.netbeans.modules.java.ui;

import java.awt.Image;
import java.util.Collection;
import java.util.Collections;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Hrebejk
 */
public final class Icons {

    private static final String ICON_BASE = "org/netbeans/modules/java/source/resources/icons/";
    private static final String GIF_EXTENSION = ".gif";
    private static final String PNG_EXTENSION = ".png";
    private static final String WAIT = ICON_BASE + "wait" + PNG_EXTENSION;
        
    /** Creates a new instance of Icons */
    private Icons() {
    }
    
    public static Icon getBusyIcon () {
        Image img = ImageUtilities.loadImage (WAIT);
        if (img == null) {
            return null;
        }
        else {
            return new ImageIcon (img);
        }
    }
            
    
    public static Icon getElementIcon( ElementKind elementKind, Collection<Modifier> modifiers ) {
        
        if ( modifiers == null ) {
            modifiers = Collections.<Modifier>emptyList();
        }
        
        Image img = null;
	
	switch( elementKind ) {
            case MODULE:
                img = ImageUtilities.loadImage( ICON_BASE + "module" + PNG_EXTENSION );
		break;
	    case PACKAGE:
		img = ImageUtilities.loadImage( ICON_BASE + "package" + GIF_EXTENSION );
		break;
	    case ENUM:	
		img = ImageUtilities.loadImage( ICON_BASE + "enum" + PNG_EXTENSION );
		break;
	    case ANNOTATION_TYPE:
		img = ImageUtilities.loadImage( ICON_BASE + "annotation" + PNG_EXTENSION );
		break;
	    case CLASS:	
		img = ImageUtilities.loadImage( ICON_BASE + "class" + PNG_EXTENSION );
		break;
	    case INTERFACE:
		img = ImageUtilities.loadImage( ICON_BASE + "interface"  + PNG_EXTENSION );
		break;
	    case FIELD:
		img = ImageUtilities.loadImage( getIconName(elementKind, ICON_BASE + "field", PNG_EXTENSION, modifiers ) );
		break;
	    case ENUM_CONSTANT: 
		img = ImageUtilities.loadImage( ICON_BASE + "constant" + PNG_EXTENSION );
		break;
	    case CONSTRUCTOR:
		img = ImageUtilities.loadImage( getIconName(elementKind, ICON_BASE + "constructor", PNG_EXTENSION, modifiers ) );
		break;
	    case INSTANCE_INIT: 	
	    case STATIC_INIT: 	
		img = ImageUtilities.loadImage( getIconName(elementKind, ICON_BASE + "initializer", PNG_EXTENSION, modifiers ) );
		break;
	    case METHOD: 	
		img = ImageUtilities.loadImage( getIconName(elementKind, ICON_BASE + "method", PNG_EXTENSION, modifiers ) );
		break;
	    default:	
	        img = null;
        }
	return img == null ? null : new ImageIcon (img);
        
    }
    
    // Private Methods ---------------------------------------------------------
           
    private static String getIconName( ElementKind kind, String typeName, String extension, Collection<Modifier> modifiers ) {
        
        StringBuffer fileName = new StringBuffer( typeName );
        
        if ( modifiers.contains( Modifier.STATIC ) ) {
            fileName.append( "Static" );                        //NOI18N
        }
        if ( modifiers.contains( Modifier.ABSTRACT ) ) {
            fileName.append( "Abstract" );                        //NOI18N
        }
        if ( modifiers.contains( Modifier.DEFAULT ) ) {
            fileName.append( "Default" );                        //NOI18N
        }
        if (kind == ElementKind.STATIC_INIT || kind == ElementKind.INSTANCE_INIT) {
            return fileName.append(extension).toString();
        }
        if ( modifiers.contains( Modifier.PUBLIC ) ) {
            return fileName.append( "Public" ).append( extension ).toString();      //NOI18N
        }
        if ( modifiers.contains( Modifier.PROTECTED ) ) {
            return fileName.append( "Protected" ).append( extension ).toString();   //NOI18N
        }
        if ( modifiers.contains( Modifier.PRIVATE ) ) {
            return fileName.append( "Private" ).append( extension ).toString();     //NOI18N
        }
        return fileName.append( "Package" ).append( extension ).toString();         //NOI18N
                        
    }
    
}
