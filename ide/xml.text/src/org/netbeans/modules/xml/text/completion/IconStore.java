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
    private static HashMap<String, ImageIcon> iconsMap = new HashMap<>();
    
    /** Main method to retrieve the ImageIcon{@link javax.swing.ImageIcon}
     * @param name Name of the icon to retrieve. In most instances would be one of the variables of
     * this class.
     * @return ImageIcon{@link javax.swing.ImageIcon}
     */    
    
    public static ImageIcon getImageIcon(String name){
      if(name == null)
        name = SPACER_16;
      
        if(iconsMap.containsKey(name))
            return iconsMap.get(name);
        else{
            iconsMap.put(name, new ImageIcon(IconStore.class.getResource(name + ICON_SUFFIX)));
            return iconsMap.get(name);
        }
    }
}
