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

import java.util.Collection;
import java.util.Collections;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;

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
    
    public static ImageIcon getBusyIcon () {
        ImageIcon icon = ImageUtilities.loadImageIcon(WAIT, false);
        return icon;
    }
            
    
    public static ImageIcon getElementIcon( ElementKind elementKind, Collection<Modifier> modifiers ) {
        
        if ( modifiers == null ) {
            modifiers = Collections.<Modifier>emptyList();
        }
        
        ImageIcon icon = null;
	
	switch( elementKind ) {
            case MODULE:
                icon = ImageUtilities.loadImageIcon(ICON_BASE + "module" + PNG_EXTENSION, false );
		break;
	    case PACKAGE:
		icon = ImageUtilities.loadImageIcon(ICON_BASE + "package" + GIF_EXTENSION, false );
		break;
	    case ENUM:	
		icon = ImageUtilities.loadImageIcon( ICON_BASE + "enum" + PNG_EXTENSION, false );
		break;
	    case ANNOTATION_TYPE:
		icon = ImageUtilities.loadImageIcon( ICON_BASE + "annotation" + PNG_EXTENSION, false );
		break;
	    case CLASS:	
		icon = ImageUtilities.loadImageIcon( ICON_BASE + "class" + PNG_EXTENSION, false );
		break;
	    case INTERFACE:
		icon = ImageUtilities.loadImageIcon( ICON_BASE + "interface"  + PNG_EXTENSION, false );
		break;
	    case FIELD:
		icon = ImageUtilities.loadImageIcon(getIconName(elementKind, ICON_BASE + "field", PNG_EXTENSION, modifiers ), false );
		break;
	    case ENUM_CONSTANT: 
		icon = ImageUtilities.loadImageIcon(ICON_BASE + "constant" + PNG_EXTENSION, false );
		break;
	    case CONSTRUCTOR:
		icon = ImageUtilities.loadImageIcon(getIconName(elementKind, ICON_BASE + "constructor", PNG_EXTENSION, modifiers ), false );
		break;
	    case INSTANCE_INIT: 	
	    case STATIC_INIT: 	
		icon = ImageUtilities.loadImageIcon(getIconName(elementKind, ICON_BASE + "initializer", PNG_EXTENSION, modifiers ), false );
		break;
	    case METHOD: 	
		icon = ImageUtilities.loadImageIcon(getIconName(elementKind, ICON_BASE + "method", PNG_EXTENSION, modifiers ), false );
		break;
	    default:	
	        icon = null;
        }
	return icon;
        
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
