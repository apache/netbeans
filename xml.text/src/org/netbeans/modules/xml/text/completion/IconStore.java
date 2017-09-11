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
package org.netbeans.modules.xml.text.completion;

import java.util.HashMap;

import javax.swing.ImageIcon;

/**
 * @author  Sandeep Singh Randhawa
 * @version 0.1
 */
final class IconStore extends Object {

    public static final String EMPTY_TAG = "/org/netbeans/modules/xml/text/completion/resources/emptyTag";
    public static final String END_TAG = "/org/netbeans/modules/xml/text/completion/resources/endTag";
    public static final String CHILDREN = "/org/netbeans/modules/xml/text/completion/resources/typeChildren";
    public static final String MIXED = "/org/netbeans/modules/xml/text/completion/resources/typeMixed";
    public static final String PCDATA = "/org/netbeans/modules/xml/text/completion/resources/typePCDATA";
    
    public static final String TYPE_ENTITY = "/org/netbeans/modules/xml/text/completion/resources/attTypeENTITY";
    public static final String TYPE_ENTITIES = "/org/netbeans/modules/xml/text/completion/resources/attTypeENTITIES";    
    public static final String TYPE_ENUMERATION = "/org/netbeans/modules/xml/text/completion/resources/attTypeEn";
    public static final String TYPE_ID = "/org/netbeans/modules/xml/text/completion/resources/attTypeID";
    public static final String TYPE_IDREF = "/org/netbeans/modules/xml/text/completion/resources/attTypeIDREF";
    public static final String TYPE_IDREFS = "/org/netbeans/modules/xml/text/completion/resources/attTypeIDREFS";
    public static final String TYPE_NMTOKEN = "/org/netbeans/modules/xml/text/completion/resources/attTypeNMTOKEN";
    public static final String TYPE_NMTOKENS = "/org/netbeans/modules/xml/text/completion/resources/attTypeNMTOKENS";
    public static final String TYPE_NOTATION = "/org/netbeans/modules/xml/text/completion/resources/attTypeNOTATION";
    public static final String TYPE_CDATA = "/org/netbeans/modules/xml/text/completion/resources/typeCDATA";
        
    public static final String SPACER_16 = "/org/netbeans/modules/xml/text/completion/resources/spacer_16";
    public static final String SPACER_8 = "/org/netbeans/modules/xml/text/completion/resources/spacer_8";
    public static final String ICON_SUFFIX = ".gif";

    /** HashMap{@link java.util.HashMap } that acts as a store for the icons.
     */    
    private static HashMap iconsMap = new HashMap();
    
    /** Main method to retrieve the ImageIcon{@link javax.swing.ImageIcon}
     * @param name Name of the icon to retrieve. In most instances would be one of the variables of
     * this class.
     * @return ImageIcon{@link javax.swing.ImageIcon}
     */    
    
    public static ImageIcon getImageIcon(String name){
      if(name == null)
        name = SPACER_16;
      
        if(iconsMap.containsKey(name))
            return (ImageIcon)iconsMap.get(name);
        else{
            iconsMap.put(name, new ImageIcon(IconStore.class.getResource(name + ICON_SUFFIX)));
            return (ImageIcon)iconsMap.get(name);
        }
    }
}
