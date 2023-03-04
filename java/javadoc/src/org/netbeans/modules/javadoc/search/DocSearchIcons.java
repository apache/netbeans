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

package org.netbeans.modules.javadoc.search;

import java.util.EnumSet;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.swing.Icon;

import org.netbeans.api.java.source.ui.ElementIcons;
import org.openide.util.ImageUtilities;

/** <DESCRIPTION>

 @author Petr Hrebejk
*/
final class DocSearchIcons extends Object {

    public static final int ICON_NOTRESOLVED = 0;
    public static final int ICON_PACKAGE = ICON_NOTRESOLVED + 1 ;
    public static final int ICON_CLASS = ICON_PACKAGE + 1 ;
    public static final int ICON_INTERFACE = ICON_CLASS + 1;
    public static final int ICON_ENUM = ICON_INTERFACE + 1;
    public static final int ICON_ANNTYPE = ICON_ENUM + 1;
    public static final int ICON_EXCEPTION = ICON_ANNTYPE + 1;
    public static final int ICON_ERROR = ICON_EXCEPTION + 1;
    public static final int ICON_CONSTRUCTOR = ICON_ERROR + 1;
    public static final int ICON_METHOD = ICON_CONSTRUCTOR + 1;
    public static final int ICON_METHOD_ST = ICON_METHOD + 1;
    public static final int ICON_VARIABLE = ICON_METHOD_ST + 1;
    public static final int ICON_VARIABLE_ST = ICON_VARIABLE + 1;
    public static final int ICON_NOT_FOUND = ICON_VARIABLE_ST + 1;
    public static final int ICON_WAIT = ICON_NOT_FOUND + 1;

    private static final Icon[] icons = new Icon[ ICON_WAIT + 1 ];

    static {
        try {
            final EnumSet<Modifier> mods = EnumSet.of(Modifier.PUBLIC);
            final EnumSet<Modifier> modsSt = EnumSet.of(Modifier.PUBLIC, Modifier.STATIC);
            icons[ ICON_NOTRESOLVED ] = ImageUtilities.loadImageIcon("org/netbeans/modules/javadoc/resources/pending.gif", false); // NOI18N
            icons[ ICON_PACKAGE ] = ImageUtilities.loadImageIcon("org/netbeans/modules/javadoc/comments/resources/package.gif", false); // NOI18N                                    
            icons[ ICON_CLASS ] = ElementIcons.getElementIcon(ElementKind.CLASS, mods);
            icons[ ICON_INTERFACE ] = ElementIcons.getElementIcon(ElementKind.INTERFACE, mods);
            icons[ ICON_ENUM ] = ElementIcons.getElementIcon(ElementKind.ENUM, mods);
            icons[ ICON_ANNTYPE ] = ElementIcons.getElementIcon(ElementKind.ANNOTATION_TYPE, mods);
            icons[ ICON_EXCEPTION ] = ImageUtilities.loadImageIcon("org/netbeans/modules/javadoc/resources/exception.gif", false); // NOI18N
            icons[ ICON_ERROR ] = ImageUtilities.loadImageIcon("org/netbeans/modules/javadoc/resources/error.gif", false); // NOI18N
            icons[ ICON_CONSTRUCTOR ] = ElementIcons.getElementIcon(ElementKind.CONSTRUCTOR, mods);
            icons[ ICON_METHOD ] = ElementIcons.getElementIcon(ElementKind.METHOD, mods);
            icons[ ICON_METHOD_ST ] = ElementIcons.getElementIcon(ElementKind.METHOD, modsSt);
            icons[ ICON_VARIABLE ] = ElementIcons.getElementIcon(ElementKind.FIELD, mods);
            icons[ ICON_VARIABLE_ST ] = ElementIcons.getElementIcon(ElementKind.FIELD, modsSt);
            icons[ ICON_NOT_FOUND ] = ImageUtilities.loadImageIcon("org/netbeans/modules/javadoc/resources/notFound.gif", false); // NOI18N
            icons[ ICON_WAIT ] = ImageUtilities.loadImageIcon("org/netbeans/modules/javadoc/resources/wait.png", false); // NOI18N
        }
        catch (Throwable w) {
            w.printStackTrace ();
        }
    }

    static Icon getIcon( int index ) {
        return icons[ index ];
    }

}
