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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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
