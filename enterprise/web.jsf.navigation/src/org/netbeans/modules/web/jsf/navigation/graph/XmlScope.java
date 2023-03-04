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
package org.netbeans.modules.web.jsf.navigation.graph;

import java.util.logging.Logger;
import org.netbeans.modules.web.jsf.navigation.PageFlowToolbarUtilities;
import org.netbeans.modules.web.jsf.navigation.PageFlowToolbarUtilities.Scope;

/**
 * This class was created because of Bug #122734. Previously I used 
 * serialize page locations with a "localized string scope" which was 
 * not the right thing to do.  Although creating a whole XmlScope 
 * dataobject seems like overkill, I wanted to make sure there was 
 * some type saftey in createScopeElement from SceneSerializer.
 * @author joelle lam
 */
public final class XmlScope {
    
    private static final Logger LOGGER = Logger.getLogger(XmlScope.class.getName());

    private static final String SCENE_FACES_SCOPE_XML = "Faces Configuration Only";// NOI18NC
    private static final String SCENE_PROJECT_SCOPE_XML = "Project";// NOI18NC
    private static final String SCENE_ALL_FACESCONFIG_XML ="All Faces Configurations";// NOI18NC
    
    private static final String SCENE_FACES_SCOPE = PageFlowToolbarUtilities.getScopeLabel(PageFlowToolbarUtilities.Scope.SCOPE_FACESCONFIG); //NOI18N
    private static final String SCENE_PROJECT_SCOPE = PageFlowToolbarUtilities.getScopeLabel(PageFlowToolbarUtilities.Scope.SCOPE_PROJECT);
    private static final String SCENE_ALL_FACESCONFIG = PageFlowToolbarUtilities.getScopeLabel(PageFlowToolbarUtilities.Scope.SCOPE_ALL_FACESCONFIG);

    private String xmlScope;
    private Scope scope;

    private XmlScope(Scope scope) {
        xmlScope = getScopeXmlFromScope(scope);
        this.scope = scope;
    }
    
    public Scope getScope() {
        return scope;
    }

    @Override
    public String toString() {
        return xmlScope;
    }
    
    /**
     * This method figures out the XmlScope object associated with
     * either an XML defined String or a Localized String.
     * @param strScope String that is either the Xml String or the localized String.  If this string is not recognized assertion thrown.
     * @return XmlScope 
     **/
    public static final XmlScope getInstance(String strScope){
        XmlScope myXmlScope = getXmlScopeFromXmlStr(strScope);
        if (myXmlScope == null ){
            myXmlScope = getXmlScopeFromLocStr(strScope);
        }
        assert myXmlScope != null;
        if ( myXmlScope == null ){
            LOGGER.severe(XmlScope.class.getName() + ": myXmlScope is null and it should not be.  For string:" + strScope);
            LOGGER.severe(XmlScope.class.getName() + ": Setting to Project Scope regardless as to not cause unnessary errors.");
            myXmlScope = SCOPE_PROJECT;
        }
        
        
        
        return myXmlScope;
    }
    
    public static final XmlScope SCOPE_FACES = new XmlScope(PageFlowToolbarUtilities.Scope.SCOPE_FACESCONFIG);
    public static final XmlScope SCOPE_PROJECT = new XmlScope(PageFlowToolbarUtilities.Scope.SCOPE_PROJECT);
    public static final XmlScope SCOPE_ALL = new XmlScope(PageFlowToolbarUtilities.Scope.SCOPE_ALL_FACESCONFIG);
    

    private final String getScopeXmlFromScope(Scope scope){
        String retVal;
        switch( scope ) {
            case SCOPE_FACESCONFIG:
                retVal =  SCENE_FACES_SCOPE_XML;
                break;
            case SCOPE_PROJECT:
                retVal = SCENE_PROJECT_SCOPE_XML;
                break;
            case SCOPE_ALL_FACESCONFIG:
                retVal = SCENE_ALL_FACESCONFIG_XML;
                break;
            default:
                retVal = null;
        }
        return retVal;
    }
            
     private static final XmlScope getXmlScopeFromXmlStr(String xmlStr) {
        XmlScope retVal = null;
        if (xmlStr.equals(SCENE_FACES_SCOPE_XML)) {
            retVal = SCOPE_FACES;
        } else if (xmlStr.equals(SCENE_PROJECT_SCOPE_XML)) {
            retVal = SCOPE_PROJECT;
        } else if ( xmlStr.equals(SCENE_ALL_FACESCONFIG_XML)){
            retVal = SCOPE_ALL;
        } 
        return retVal;
    }
     
    private static final XmlScope getXmlScopeFromLocStr(String localizedStr) {
        XmlScope retVal = null;
        if (localizedStr.equals(SCENE_FACES_SCOPE)) {
            retVal = SCOPE_FACES;
        } else if (localizedStr.equals(SCENE_PROJECT_SCOPE)) {
            retVal = SCOPE_PROJECT;
        } else if ( localizedStr.equals(SCENE_ALL_FACESCONFIG)){
            retVal = SCOPE_ALL;
        } 
        return retVal;
    }
    
//    private final String getXmlScopeFromXmlStr(String xmlStr) {
//        String retVal = SCENE_PROJECT_SCOPE;
//        if (xmlStr.equals(SCENE_FACES_SCOPE_XML)) {
//            retVal = SCENE_FACES_SCOPE;
//        } else if (xmlStr.equals(SCENE_PROJECT_SCOPE_XML)) {
//            retVal = SCENE_PROJECT_SCOPE;
//        }
//        return retVal;
//    }
//
//    private final String getScopeXmlFromString(String localizedScopeStr) {
//        String retVal = SCENE_PROJECT_SCOPE_XML;
//        if (localizedScopeStr.equals(SCENE_FACES_SCOPE)) {
//            retVal = SCENE_FACES_SCOPE_XML;
//        } else if (localizedScopeStr.equals(SCENE_PROJECT_SCOPE)) {
//            retVal = SCENE_PROJECT_SCOPE_XML;
//        }
//        return retVal;
//    }
}
