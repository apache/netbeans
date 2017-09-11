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


package org.netbeans.modules.i18n;


import java.io.IOException;
import java.util.Arrays;

import org.openide.loaders.DataObject;

/**
 * Abstract class which implementation's are wrappers for holders of properties resources.
 * Typically such object is <code>PropertiesDataObject</code> which represents bundle
 * of .properties files. But it can be also some object representing other type of resources
 * e.g. subclass of <code>ListResourceBundle</code> class or in case of i18n-ing JSP pages
 * object representing tag library etc.
 *
 * @author  Peter Zavadsky
 */
public abstract class ResourceHolder {
    
    /** Instance of resource bundle object. */
    protected DataObject resource;
    
    /** Classes which instances as resources can be hold by this <code>ResourceHolder</code>. */
    protected final Class[] resourceClasses;

    
    /** Construct resource holder. */
    public ResourceHolder(Class[] resourceClasses) {
        if(resourceClasses == null || resourceClasses.length == 0)
            throw new IllegalArgumentException();
        
        this.resourceClasses = resourceClasses;
    }
    
    
    /** Setter for </code>resource</code>. */
    public void setResource(DataObject resource) {
        if (resource == null) {
            this.resource = null;
            return;
        }

        Class clazz = resource.getClass();

        // Check if the class of parameter is valid for this ResourceHolder.
        if(!Arrays.asList(resourceClasses).contains(clazz))
            throw new IllegalArgumentException();

        if(!resource.equals(this.resource))
            this.resource = resource;
    }
    
    /** Getter for </code>resource</code>. */
    public DataObject getResource() {
        return resource;
    }

    /** Getter for supported <code>resourceClasses</code>. */
    public Class[] getResourceClasses() {
        return resourceClasses;
    }
    
    /** Gets all keys which are stored in underlying resource object. */
    public abstract String[] getAllKeys();
    
    /** Gets value for specified key. 
     * @return value for key or null if such key os not stored in resource */
    public abstract String getValueForKey(String key);
    
    /** Gets comment for specified key. 
     * @return value for key or null if such key os not stored in resource */
    public abstract String getCommentForKey(String key);
    
    /** 
     * Adds new property (key-value pair) to resource object.
     * Behave according to settings.
     */
    public void addProperty(Object key, Object value, String comment) {
        boolean overwriteValues = I18nUtil.getOptions().isReplaceResourceValue();
        addProperty(key, value, comment, overwriteValues);
    }
    
    /** Adds new property (key-value pair) to resource object, with forcing
     * of reset the value for existing key in all locales. */
    public abstract void addProperty(Object key, Object value, String comment, boolean forceNewValue);
    
    /** Gets template for reosurce data object. Used by instatianing. 
     * @param clazz <code>Class</code> of object to instantaniate. Have to be one of supported classes. */
    public final DataObject getTemplate(Class clazz) throws IOException {
        if(!Arrays.asList(resourceClasses).contains(clazz))
            throw new IllegalArgumentException();
        
        return createTemplate(clazz);
    }
    
    /** Creates template of type clazz. */
    protected abstract DataObject createTemplate(Class clazz) throws IOException;

    @Override
    public String toString() {
        return super.toString() +
               "[resource=" +
               String.valueOf(resource) +
               ", resourceClasses=" +
               Arrays.toString(resourceClasses) + ']'; // NOI18N
    }

}

