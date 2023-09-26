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


package org.netbeans.modules.web.core.api;

import java.beans.PropertyChangeSupport;
import java.util.Map;
import java.util.Iterator;

import javax.servlet.jsp.tagext.TagLibraryInfo;

/** Holds data relevant to the JSP coloring for one JSP page. The main purposes
 * of this class are
 * to report which prefixes are tag library prefixes in the page, and allows
 * listening on the change of the prefixes, at which point the page needs to be
 * recolored.
 *
 * @author Petr Jiricka
 */
public final class JspColoringData extends PropertyChangeSupport {
    
    /** An property whose change is fired every time the tag library 
    *  information changes in such a way that recoloring of the document is required. 
    */
    public static final String PROP_COLORING_CHANGE = "coloringChange"; // NOI18N
    public static final String PROP_PARSING_SUCCESSFUL = "parsingSuccessful"; //NOI18N
    public static final String PROP_PARSING_IN_PROGRESS = "parsingInProgress"; //NOI18N
    
    /** Taglib id -> TagLibraryInfo */
    private Map<String, TagLibraryInfo> taglibs;
    
    /** Prefix -> Taglib id */
    private Map<String, String> prefixMapper;
    
    private boolean elIgnored = false;
    
    private boolean xmlSyntax = false;

    private boolean initialized = false;
    
    /** Creates a new instance of JspColoringData. */
    public JspColoringData(Object sourceBean) {
        super(sourceBean);
    }
    
    public Map getPrefixMapper(){
        return prefixMapper;
    }
    
    public String toString() {
        return "JSPColoringData, taglibMap:\n" +
          (prefixMapper == null ?
            "null" :
            mapToString(prefixMapper, "  ")
          );
    }
    
    private static String mapToString(Map m, String indent) {
        StringBuffer sb = new StringBuffer();
        Iterator it = m.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            sb.append(indent).append(key).append(" -> ").append(m.get(key)).append("\n");
        }
        return sb.toString();
    }

    /** Returns true if the given tag library prefix is known in this page.
     */
    public boolean isTagLibRegistered(String prefix) {
        if ((taglibs == null) || (prefixMapper == null)) {
            return false;
        }
        return prefixMapper.containsKey(prefix);
    }

    /** returns true if the JspColoringInfo has already been updated based on parser result. */
    public boolean isInitialized() {
        return initialized;
    }

    /** Returns true if the EL is ignored in this page.
     */
    public boolean isELIgnored() {
        return elIgnored;
    }
    
    /** Returns true if the page is in xml syntax (JSP Documnet). 
     * If the page is in standard syntax, returns false.
     */
    public boolean isXMLSyntax(){
        return xmlSyntax;
    }
    /*public boolean isBodyIntepretedByTag(String prefix, String tagName) {
    }*/
        
    public void parsingStarted() {
        firePropertyChange(PROP_PARSING_IN_PROGRESS, null, true);
    }
    
    /** Incorporates new parse data from the parser, possibly firing a change about coloring.
     * @param newTaglibs the new map of (uri -> TagLibraryInfo)
     * @param newPrefixMapper the new map of (prefix, uri)
     * @param parseSuccessful whether parsing was successful. If false, then the new information is partial only
     */
    public void applyParsedData(Map<String, TagLibraryInfo> newTaglibs, Map<String, String> newPrefixMapper, boolean newELIgnored, boolean newXMLSyntax, boolean parseSuccessful) {

        initialized = true;

        // check whether coloring has not changed
        boolean coloringSame = equalsColoringInformation(taglibs, prefixMapper, newTaglibs, newPrefixMapper);
        
        firePropertyChange(PROP_PARSING_SUCCESSFUL, null, parseSuccessful);
        
        // check and apply EL data
        if (parseSuccessful) {
            coloringSame = coloringSame && (elIgnored == newELIgnored);
            elIgnored = newELIgnored;
        }
        
        //An additional check for the coloring change -> 
        //if the elIgnored and xmlSyntax have default values and the taglibs and prefixes are empty,
        //there is no need to repaint the editor (fire the property change).        
        //Test if this is a first call of this method - after opening of the editor
        if((taglibs == null) && (prefixMapper == null)) {
            coloringSame = ((newELIgnored == elIgnored) && 
                           (newXMLSyntax == xmlSyntax) &&
                           newTaglibs.isEmpty() &&
                           newPrefixMapper.isEmpty());
        }

        if (newXMLSyntax != xmlSyntax){
            xmlSyntax = newXMLSyntax;
            coloringSame = false;
        }        
        
        // appy taglib data
        if (parseSuccessful || (taglibs == null) || (prefixMapper == null)) {
            // overwrite
            taglibs = newTaglibs;
            prefixMapper = newPrefixMapper;
        }
        else {
            // merge
            Iterator<String> it = newPrefixMapper.keySet().iterator();
            while (it.hasNext()) {
                String prefix = it.next();
                String uri = newPrefixMapper.get(prefix);
                String uriOld = prefixMapper.get(prefix);
                if ((uriOld == null) || !uri.equals(uriOld)) {
                    TagLibraryInfo newTaglib = newTaglibs.get(uri);
                    if (newTaglib != null) {
                        // change - merge it
                        prefixMapper.put(prefix, uri);
                        taglibs.put(uri, newTaglib);
                    }
                }
            }
        }
        // possibly fire the change
        if (!coloringSame) {
            firePropertyChange(PROP_COLORING_CHANGE, null, null);
        }
    }

    private static boolean equalsColoringInformation(Map<String, TagLibraryInfo> taglibs1, Map<String, String> prefixMapper1,
            Map<String, TagLibraryInfo> taglibs2, Map<String, String> prefixMapper2) {

        if ((taglibs1 == null) != (taglibs2 == null)) {
            return false;
        }
        if ((prefixMapper1 == null) != (prefixMapper2 == null)) {
            return false;
        }
        if (prefixMapper1.size() != prefixMapper2.size()) {
            return false;
        }
        else {
            Iterator<String> it = prefixMapper1.keySet().iterator();
            while (it.hasNext()) {
                String prefix = it.next();
                String key1 = prefixMapper1.get(prefix);
                String key2 = prefixMapper2.get(prefix);

                if ((key1 == null) || (key2 == null)) {
                    return false;
                }

                TagLibraryInfo tli1 = taglibs1.get(key1);
                TagLibraryInfo tli2 = taglibs2.get(key2);
                if ((tli1 == null) || (tli2 == null)) {
                    return false;
                }
                if (!equalsColoringInformation(tli1, tli2)) {
                    return false;
                }
            }
            return true;
        }
    }

    private static boolean equalsColoringInformation(TagLibraryInfo tli1, TagLibraryInfo tli2) {
        /** PENDING
         * should be going through all tags and checking whether the value 
         * returned by tagInfo.getBodyContent() has not changed.
         */
        return true;
    }
    
}
