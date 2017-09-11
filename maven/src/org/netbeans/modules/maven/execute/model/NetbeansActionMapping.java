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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class NetbeansActionMapping.
 * 
 * @version $Revision$ $Date$
 */
public class NetbeansActionMapping implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field actionName.
     */
    private String actionName;

    /**
     * Field displayName.
     */
    private String displayName;

    /**
     * Field recursive.
     */
    private boolean recursive = true;

    /**
     * Field packagings.
     */
    private java.util.List<String> packagings;

    /**
     * Field goals.
     */
    private java.util.List<String> goals;

    /**
     * Field properties.
     */
    private Map<String,String> properties;

    /**
     * Field activatedProfiles.
     */
    private java.util.List<String> activatedProfiles;

    private String basedir;

    private String preAction;

    private String reactor;

      //-----------/
     //- Methods -/
    //-----------/


    public String getReactor() {
        return reactor;
    }

    public void setReactor(String reactor) {
        this.reactor = reactor;
    }

    public String getBasedir() {
        return basedir;
    }

    public void setBasedir(String basedir) {
        this.basedir = basedir;
    }

    public String getPreAction() {
        return preAction;
    }

    public void setPreAction(String preAction) {
        this.preAction = preAction;
    }

    /**
     * Method addActivatedProfile.
     * 
     * @param string
     */
    public void addActivatedProfile(String string)
    {
        getActivatedProfiles().add( string );
    } //-- void addActivatedProfile(String) 

    /**
     * Method addGoal.
     * 
     * @param string
     */
    public void addGoal(String string)
    {
        getGoals().add( string );
    } //-- void addGoal(String) 

    /**
     * Method addPackaging.
     * 
     * @param string
     */
    public void addPackaging(String string)
    {
        getPackagings().add( string );
    } //-- void addPackaging(String) 

    /**
     * Method addProperty.
     * 
     * @param key
     * @param value
     */
    public void addProperty(String key, String value)
    {
        getProperties().put( key, value );
    } //-- void addProperty(String, String) 

    /**
     * Get the actionName field.
     * 
     * @return String
     */
    public String getActionName()
    {
        return this.actionName;
    } //-- String getActionName() 

    /**
     * Method getActivatedProfiles.
     * 
     * @return java.util.List
     */
    public java.util.List<String> getActivatedProfiles()
    {
        if ( this.activatedProfiles == null )
        {
            this.activatedProfiles = new java.util.ArrayList<String>();
        }
        
        return this.activatedProfiles;
    } //-- java.util.List getActivatedProfiles() 

    /**
     * Get the displayName field.
     * 
     * @return String
     */
    public String getDisplayName()
    {
        return this.displayName;
    } //-- String getDisplayName() 

    /**
     * Method getGoals.
     * 
     * @return java.util.List
     */
    public java.util.List<String> getGoals()
    {
        if ( this.goals == null )
        {
            this.goals = new java.util.ArrayList<String>();
        }
        
        return this.goals;
    } //-- java.util.List getGoals() 

    /**
     * Method getPackagings.
     * 
     * @return java.util.List
     */
    public java.util.List<String> getPackagings()
    {
        if ( this.packagings == null )
        {
            this.packagings = new java.util.ArrayList<String>();
        }
        
        return this.packagings;
    } //-- java.util.List getPackagings() 

    public Map<String,String> getProperties()
    {
        if ( this.properties == null )
        {
            this.properties = new LinkedHashMap<String,String>();
        }
        
        return this.properties;
    }

    /**
     * Get the recursive field.
     * 
     * @return boolean
     */
    public boolean isRecursive()
    {
        return this.recursive;
    } //-- boolean isRecursive() 

    /**
     * Method removeActivatedProfile.
     * 
     * @param string
     */
    public void removeActivatedProfile(String string)
    {
        getActivatedProfiles().remove( string );
    } //-- void removeActivatedProfile(String) 

    /**
     * Method removeGoal.
     * 
     * @param string
     */
    public void removeGoal(String string)
    {
        getGoals().remove( string );
    } //-- void removeGoal(String) 

    /**
     * Method removePackaging.
     * 
     * @param string
     */
    public void removePackaging(String string)
    {
        getPackagings().remove( string );
    } //-- void removePackaging(String) 

    /**
     * Set the actionName field.
     * 
     * @param actionName
     */
    public void setActionName(String actionName)
    {
        this.actionName = actionName;
    } //-- void setActionName(String) 

    /**
     * Set the activatedProfiles field.
     * 
     * @param activatedProfiles
     */
    public void setActivatedProfiles(java.util.List<String> activatedProfiles)
    {
        this.activatedProfiles = activatedProfiles;
    } //-- void setActivatedProfiles(java.util.List) 

    /**
     * Set the displayName field.
     * 
     * @param displayName
     */
    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    } //-- void setDisplayName(String) 

    /**
     * Set list of goals and phases to execute, order is important.
     * 
     * @param goals
     */
    public void setGoals(java.util.List<String> goals)
    {
        this.goals = goals;
    } //-- void setGoals(java.util.List) 

    /**
     * Set packaging types that this action mapping works for. *
     * for any.
     * 
     * @param packagings
     */
    public void setPackagings(java.util.List<String> packagings)
    {
        this.packagings = packagings;
    } //-- void setPackagings(java.util.List) 

    public void setProperties(Map<String,String> properties)
    {
        this.properties = properties;
    }

    /**
     * Set the recursive field.
     * 
     * @param recursive
     */
    public void setRecursive(boolean recursive)
    {
        this.recursive = recursive;
    } //-- void setRecursive(boolean) 


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
}
