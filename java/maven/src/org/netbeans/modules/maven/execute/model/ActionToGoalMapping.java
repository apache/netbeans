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

package org.netbeans.modules.maven.execute.model;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

/**
 * Class ActionToGoalMapping.
 * 
 * @version $Revision$ $Date$
 */
public class ActionToGoalMapping implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Supported packaging type that this this project maps to. 
     *                     Useful for custom non-standard
     * packingings to get the same behaviour as the supported ones.
     *                     Supported types: jar, war, ejb, ear
     *                     @deprecated is ethe
     * netbeans.hint.packaging POM property instead.
     */
    @Deprecated
    private String packaging;

    /**
     * Field actions.
     */
    private java.util.List<NetbeansActionMapping> actions;
    private java.util.List<NetbeansActionProfile> profiles;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addAction.
     * 
     * @param netbeansActionMapping
     */
    public void addAction(NetbeansActionMapping netbeansActionMapping)
    {
        getActions().add( netbeansActionMapping );
    } //-- void addAction(NetbeansActionMapping) 

    /**
     * Method getActions.
     * 
     * @return java.util.List
     */
    public java.util.List<NetbeansActionMapping> getActions()
    {
        if ( this.actions == null )
        {
            this.actions = new java.util.ArrayList<NetbeansActionMapping>();
        }
        
        return this.actions;
    } //-- java.util.List getActions() 

    /**
     * Get supported packaging type that this this project maps to.
     * 
     *                     Useful for custom non-standard
     * packingings to get the same behaviour as the supported ones.
     *                     Supported types: jar, war, ejb, ear
     *                     @deprecated is ethe
     * netbeans.hint.packaging POM property instead.
     * 
     * @return String
     */
    @Deprecated
    public String getPackaging()
    {
        return this.packaging;
    } //-- String getPackaging() 

    /**
     * Method removeAction.
     * 
     * @param netbeansActionMapping
     */
    public void removeAction(NetbeansActionMapping netbeansActionMapping)
    {
        getActions().remove( netbeansActionMapping );
    } //-- void removeAction(NetbeansActionMapping) 

    /**
     * Set the actions field.
     * 
     * @param actions
     */
    public void setActions(java.util.List<NetbeansActionMapping> actions)
    {
        this.actions = actions;
    } //-- void setActions(java.util.List) 

    /**
     * Set supported packaging type that this this project maps to.
     * 
     *                     Useful for custom non-standard
     * packingings to get the same behaviour as the supported ones.
     *                     Supported types: jar, war, ejb, ear
     *                     @deprecated is ethe
     * netbeans.hint.packaging POM property instead.
     * 
     * @param packaging
     */
    @Deprecated
    public void setPackaging(String packaging)
    {
        this.packaging = packaging;
    } //-- void setPackaging(String) 


    private String modelEncoding = "UTF-8";

    /**
     * Set an encoding used for reading/writing the model.
     *
     * @param modelEncoding the encoding used when reading/writing the model.
     */
    public void setModelEncoding( String modelEncoding )
    {
        this.modelEncoding = modelEncoding;
    }

    /**
     * @return the current encoding used when reading/writing this model.
     */
    public String getModelEncoding()
    {
        return modelEncoding;
    }
    
    public void addProfile(NetbeansActionProfile p) {
        getProfiles().add(p);
    }

    public java.util.List<NetbeansActionProfile> getProfiles() {
        if (this.profiles == null) {
            this.profiles = new java.util.ArrayList<NetbeansActionProfile>();
        }
        return this.profiles;
    } 

    public void removeProfile(NetbeansActionProfile p) {
        getActions().remove( p );
    }

    public void setProfiles(java.util.List<NetbeansActionProfile> p) {
        this.profiles  = p;
    }
    
}
