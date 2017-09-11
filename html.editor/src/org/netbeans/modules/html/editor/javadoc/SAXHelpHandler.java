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

package org.netbeans.modules.html.editor.javadoc;

import java.util.Hashtable;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 *
 * @author Petr Pisl
 */

class SAXHelpHandler extends DefaultHandler {
        private TagHelpItem tag;
        private TagHelpItem attribute;

        private static final int HELP_CODE = "help".hashCode(); //NOI18N
        private static final int TAG_CODE = "tag".hashCode();// NOI18N
        private static final int ATTRIBUTE_CODE = "attribute".hashCode();// NOI18N
        private static final int LOCATION_CODE = "location".hashCode();// NOI18N
        private static final int START_TEXT_CODE = "start-text".hashCode();// NOI18N
        private static final int END_TEXT_CODE = "end-text".hashCode();// NOI18N
        private static final int ADD_TEXT_CODE = "add-text".hashCode();// NOI18N
        private static final int BEFORE_CODE = "before".hashCode();// NOI18N
        private static final int AFTER_CODE = "after".hashCode();// NOI18N
        
        private static final String NAME_STRING = "name";// NOI18N
        private static final String LOCATION_STRING = "location"; //NOI18N
        private static final String FILE_STRING = "file"; //NOI18N
        private static final String IDENTICAL_STRING = "identical"; //NOI18N
        private static final String TEXT_STRING = "text"; //NOI18N
        private static final String OFFSET_STRING = "offset"; //NOI18N
        private static final String BEFORE_STRING = "before"; //NOI18N
        private static final String AFTER_STRING = "after"; //NOI18N
        
                
        private static final int TAG_STATE = 1;
        private static final int ATTRIBUTE_STATE = 2;
        
        private static final int BEFORE_STATE = 10;
        private static final int AFTER_STATE = 11;
       
        private int state;
        private int textState;
        
        private Hashtable<String, TagHelpItem> map = new Hashtable<>();
        private String file;
        
        public SAXHelpHandler(){
            super();
        }
        
        @Override
        public void startElement(String uri, String localname, String qname, Attributes attrs) throws SAXException {
            int controlCode = qname.hashCode();
            String value;
            if (controlCode == TAG_CODE){    
                value = attrs.getValue(NAME_STRING);
                tag = new TagHelpItem(value);
                map.put (tag.getName().toUpperCase(), tag);
                state = TAG_STATE;
                //System.out.println("tag");
                value = attrs.getValue(IDENTICAL_STRING);
                if (value != null){
                    //System.out.println("value: " + value);
                    tag.setIdentical(value);
                }
            }
            else if (controlCode == ATTRIBUTE_CODE){
                value = attrs.getValue(NAME_STRING);
                attribute = new TagHelpItem(value);
                state = ATTRIBUTE_STATE;
                map.put((tag.getName() + "#" + attribute.getName()).toUpperCase(), attribute); // NOI18N
                value = attrs.getValue(IDENTICAL_STRING);
                if (value != null){
                    //System.out.println("value: " + value);
                    attribute.setIdentical(value);
                }
            }
            else if (controlCode == LOCATION_CODE){
                value = attrs.getValue(FILE_STRING);
                switch (state){
                    case TAG_STATE: tag.setFile(value); break;
                    case ATTRIBUTE_STATE: attribute.setFile(value); break;
                }
            }
            else if (controlCode == START_TEXT_CODE){  
                value = attrs.getValue(OFFSET_STRING);
                int offset = 0;
                if (value != null){
                    try{
                        offset = (new Integer(value)).intValue();
                    }
                    catch (NumberFormatException e){
                    }
                }
                value = attrs.getValue(TEXT_STRING);
                switch (state){
                    case TAG_STATE: 
                        tag.setStartText(value); 
                        tag.setStartTextOffset(offset); 
                        break;
                    case ATTRIBUTE_STATE: 
                        attribute.setStartText(value); 
                        attribute.setStartTextOffset(offset);
                        break;
                }
            }
            else if (controlCode == END_TEXT_CODE){  
                value = attrs.getValue(OFFSET_STRING);
                int offset = 0;
                if (value != null){
                    try{
                        offset = (new Integer(value)).intValue();
                    }
                    catch (NumberFormatException e){
                    }
                }
                value = attrs.getValue(TEXT_STRING);
                switch (state){
                    case TAG_STATE: 
                        tag.setEndText(value); 
                        tag.setEndTextOffset(offset); 
                        break;
                    case ATTRIBUTE_STATE: 
                        attribute.setEndText(value); 
                        attribute.setEndTextOffset(offset);
                        break;
                }
            }
            else if (controlCode == ADD_TEXT_CODE){  
                String before = attrs.getValue(BEFORE_STRING);
                String after = attrs.getValue(AFTER_STRING);
                switch (state){
                    case TAG_STATE: 
                        tag.setTextBefore(before); 
                        tag.setTextAfter(after); 
                        break;
                    case ATTRIBUTE_STATE: 
                        attribute.setTextBefore(before); 
                        attribute.setTextAfter(after);
                        break;
                }
            }
            else if(controlCode == START_TEXT_CODE){
                
            }
            else if(controlCode == BEFORE_CODE){
                textState = BEFORE_STATE;
            }
            else if(controlCode == AFTER_CODE){
                textState = AFTER_STATE;
            }
            else if ( controlCode == HELP_CODE){
                file = attrs.getValue(FILE_STRING);
                
            }
            
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException{
            String text = (new String(ch, start, length)).trim();
            if (text != null && text.length() > 0){
                TagHelpItem key = null;
                switch (state){
                    case TAG_STATE:
                        key = tag;
                        break;
                    case ATTRIBUTE_STATE:
                        key = attribute;
                        break;
                }
                if (key != null){
                    switch (textState){
                        case BEFORE_STATE:
                            if (key.getTextBefore() != null)
                                key.setTextBefore(key.getTextBefore() + text);
                            else
                                key.setTextBefore(text);
                            break;
                        case AFTER_STATE:
                            if (key.getTextAfter() != null)
                                key.setTextAfter(key.getTextAfter() + text);
                            else
                                key.setTextAfter(text);
                    }
                }
            }
        }
        
        public String getHelpFile(){
            return file;
        }
        
        public Hashtable getMap(){
            return map;
        }
    }
