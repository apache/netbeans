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
            return ImageUtilities.image2Icon (img);
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
	return img == null ? null : ImageUtilities.image2Icon (img);
        
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
