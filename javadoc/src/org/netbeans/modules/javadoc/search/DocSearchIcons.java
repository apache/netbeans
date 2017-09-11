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
