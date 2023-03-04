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


package org.netbeans.modules.i18n.jsp;


import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import org.netbeans.modules.i18n.HardCodedString;
import org.netbeans.modules.i18n.I18nSupport;
import org.netbeans.modules.i18n.java.JavaI18nFinder;
import org.netbeans.modules.i18n.java.JavaI18nSupport;

import org.openide.loaders.DataObject;
import org.openide.util.Lookup;


/**
 * Support for internationalizing strings in jsp sources.
 * It support i18n-izing strings occured only in jsp scriptlets, declaractions and expressions.
 *
 * @author Peter Zavadsky
 * @see org.netbeans.modules.i18n.JavaI18nSupport
 */
public class JspI18nSupport extends JavaI18nSupport {


    /** Constructor. */
    public JspI18nSupport(DataObject sourceDataObject) {
        super(sourceDataObject);
    }
    
    
    /** Creates <code>I18nFinder</code>. Implements superclass abstract method. */
    @Override
    protected I18nFinder createFinder() {
        return new JspI18nFinder(document);
    }
 
    /** Overrides superclass method. 
     * @return false */
    @Override
    public boolean hasAdditionalCustomizer() {
        return false;
    }
    
    /** Overrides superclass method. 
     * @return null */
    @Override
    public JPanel getAdditionalCustomizer() {
        return null;
    }
    
    /** Overrides superclass method. Does nothing. */
    @Override
    public void performAdditionalChanges() {
    }
    
    
    /** Finder which search hard coded strings in java sources. */
    public static class JspI18nFinder extends JavaI18nFinder {

        /** State when finder is in jsp code excluding parts where java code can occure. */
        protected static final int STATE_JSP = 8;
        /** State when finder is at the start of scripting tag where java code could occure. */
        protected static final int STATE_JSP_START_SCRIPTING = 9;
        /** State when finder is at beginnig of tag where java code occure. */
        protected static final int STATE_JSP_SCRIPTING = 10;
        /** State when finder is at the end of scripting tag where java code occures. */
        protected static final int STATE_JSP_END_SCRIPTING = 11;

        /** Helper array holding jsp scripting element tags for jsp using xml tags. */
        private static final String[] jspStrings = new String[] {
            "jsp:declaration", // NOI18N
            "jsp:expression", // NOI18N
            "jsp:scriptlet" // NOI18N
        }; // PENDING<< Don't know if to use it.

        
        /** Helper variable. Stores old state of java code when possible end of srcipting element occured
         * and state chaned to STATE_JSP_END_SCRIPTING. */
        private int oldJavaState;
        
        /** Constructor. */
        public JspI18nFinder(StyledDocument document) {
            super(document);
            
            state = STATE_JSP;
        }

        
        /** Resets finder. Overrides superclass method. */
        @Override
        protected void reset() {
            super.reset();
            
            state = STATE_JSP;
        }
        
        /** Handles state changes according next character. Overrides superclass method. */
        @Override
        protected HardCodedString handleCharacter(char character) {
            if(state == STATE_JSP)
                return handleStateJsp(character);
            else if(state == STATE_JSP_START_SCRIPTING)
                return handleStateJspStartScripting(character);
            else if(state == STATE_JSP_SCRIPTING)
                return handleStateJspScripting(character);
            else if(state == STATE_JSP_END_SCRIPTING)
                return handleStateJspEndScripting(character);
            else {
                // Java code states.
                if(character == '%') {
                    // Could be end of scripting element.
                    state = STATE_JSP_END_SCRIPTING;
                    oldJavaState = state;
                    
                    return null;
                } else if(character == '<') { // PENDING see above.
                    // Could be end jsp:expression, jsp:scriptlet or jsp:declaration tag.
                    for(int i=0; i<jspStrings.length; i++) {
                        if(isNextString("</"+jspStrings[i]+">")) { // NOI18N

                            position += jspStrings[i].length() + 2;
                            state = STATE_JSP;
                            
                            return null;
                        }
                    }
                }
                
                return super.handleCharacter(character);
            }
        }

        /** Handles state <code>STATE_JSP</code>.
         * @param character char to proceede 
         * @return null */
        protected HardCodedString handleStateJsp(char character) {
            if(character == '<')
                state = STATE_JSP_START_SCRIPTING;
                
            return null;
        }

        /** Handles state <code>STATE_JSP_START_SCRIPTING</code>.
         * @param character char to proceede 
         * @return null */
        protected HardCodedString handleStateJspStartScripting(char character) {
            if(character == '%')
                state = STATE_JSP_SCRIPTING;
            else if(character == 'j') { // PENDING see above.
                // Could be jsp:expression, jsp:scriptlet or jsp:declaration tag.
                for(int i=0; i<jspStrings.length; i++) {
                    if(isNextString(jspStrings[i]+">")) { // NOI18N
                        
                        position += jspStrings[i].length();
                        state = STATE_JAVA;
                    }
                }
            } else
                state = STATE_JSP;
                
            return null;
        }

        /** Utility method.
         * @return true if follows string in searched docuement */
        private boolean isNextString(String nextString) {
            // PENDING better would be operate on buffer tah document.
            
            if(buffer.length < position + nextString.length())
                return false;
            
            try {
                if(nextString.equals(document.getText(position, nextString.length())))
                    return true;
            } catch(BadLocationException ble) {
                // It's OK just to catch it.
            }
            
            return false;
        }
        
        /** Handles state <code>STATE_JSP_SCRIPTING</code>.
         * @param character char to proceede 
         * @return null */
        protected HardCodedString handleStateJspScripting(char character) {
            if(character == '@' || character == '-') 
                state = STATE_JSP; // JSP directive or comment
            else 
                state = STATE_JAVA;  // java code
                
            return null;
        }
        
        /** Handles state <code>STATE_JSP_END_SCRIPTING</code>.
         * @param character char to proceede 
         * @return null */
        protected HardCodedString handleStateJspEndScripting(char character) {
            if(character == '>')
                state = STATE_JSP;
            else
                state = oldJavaState;
                
            return null;
        }
       
    } // End of JavaI18nFinder nested class.
    
    
    /** Factory for <code>JspI18nSupport</code>. */
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.i18n.I18nSupport.Factory.class)
    public static class Factory extends I18nSupport.Factory {
        
        /** Implements superclass abstract method. */
        public I18nSupport createI18nSupport(DataObject dataObject) {
            return new JspI18nSupport(dataObject);
        }
        
        /** Gets class of supported <code>DataObject</code>.
         * @return <code>JspDataObject</code> class or <code>null</code> 
         * if jsp module is not available */
        public Class getDataObjectClass() {
            // XXX Cleaner should be this code dependend on java module
            // -> I18n API needed.
            try {
                return Class.forName(
                    "org.netbeans.modules.web.core.jsploader.JspDataObject", // NOI18N
                    false,
                    Lookup.getDefault().lookup(ClassLoader.class)
                );
            } catch(ClassNotFoundException cnfe) {
                return null;
            }
        }

    } // End of class Factory.
}
