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
package org.netbeans.modules.maven.execute.model;

/**
 *
 * @author Jaroslav Tulach &lt;jtulach@netbeans.org&gt;
 */
public class NetbeansActionProfile {
    /**
     * Field actions.
     */
    private java.util.List<NetbeansActionMapping> actions;
    private String id;
    
    /**
     * Display name, mainly for profiles shipped with the code. No reason to
     * have in user custom profiles named by the users. 
     */
    private String displayName;

      //-----------/
     //- Methods -/
    //-----------/

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
     * Returns human-readable display name, if defined.
     * @return display name, or {@code null}
     * @since 2.148
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name.
     * @param displayName new display name, can be {@code null}.
     * @since 2.148
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
