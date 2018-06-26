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

/**
 * Superclass that implements DescriptionInterface for Servlet2.4 beans.
 *
 * @author  Milan Kuchtiak
 */

package org.netbeans.modules.j2ee.dd.impl.commonws;

import org.netbeans.modules.schema2beans.Version;
import org.netbeans.modules.j2ee.dd.api.common.*;
import org.openide.ErrorManager;

public abstract class DescriptionBeanMultiple extends EnclosingBean implements DescriptionInterface {

    public DescriptionBeanMultiple(java.util.Vector comps, Version version) {
	super(comps, version);
    }
    // methods implemented by specific s2b beans
    public void setDescription(int index, java.lang.String value){}
    public String getDescription(int index){return null;}
    public void setDescription(java.lang.String[] value){}
    //public abstract java.lang.String[] getDescription();
    public int sizeDescription(){return 0;}
    public int addDescription(java.lang.String value){return 0;}
    //public abstract int removeDescription(java.lang.String value);
    public void setDescriptionXmlLang(int index, java.lang.String value){}
    public String getDescriptionXmlLang(int index){return null;}
    
    public void setDescription(String locale, String description) throws VersionNotSupportedException {
        if (description==null) removeDescriptionForLocale(locale);
        else {
            int size = sizeDescription();
            boolean found=false;
            for (int i=0;i<size;i++) {
                String loc=getDescriptionXmlLang(i);
                if ((locale==null && loc==null) || (locale!=null && locale.equalsIgnoreCase(loc))) {
                    found=true;
                    setDescription(i, description);
                    break;
                }
            }
            if (!found) {
                addDescription(description);
                if (locale!=null) setDescriptionXmlLang(size, locale.toLowerCase());
            }
        }
    }
    
    public void setDescription(String description) {
        try {
            setDescription(null,description);
        } catch (VersionNotSupportedException ex){
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    public void setAllDescriptions(java.util.Map descriptions) throws VersionNotSupportedException {
        removeAllDescriptions();
        if (descriptions!=null) {
            java.util.Iterator keys = descriptions.keySet().iterator();
            int i=0;
            while (keys.hasNext()) {
                String key = (String) keys.next();
                addDescription((String)descriptions.get(key));
                setDescriptionXmlLang(i++, key);
            }
        }
    }
    
    public String getDescription(String locale) throws VersionNotSupportedException {
        for (int i=0;i<sizeDescription();i++) {
            String loc=getDescriptionXmlLang(i);
            if ((locale==null && loc==null) || (locale!=null && locale.equalsIgnoreCase(loc))) {
                return getDescription(i);
            }
        }
        return null;
    }
    public String getDefaultDescription() {
        try {
            return getDescription(null);
        } catch (VersionNotSupportedException ex){return null;}
    }
    public java.util.Map getAllDescriptions() {
        java.util.Map map =new java.util.HashMap();
        for (int i=0;i<sizeDescription();i++) {
            String desc=getDescription(i);
            String loc=getDescriptionXmlLang(i);
            map.put(loc, desc);
        }
        return map;
    }
    
    public void removeDescriptionForLocale(String locale) throws VersionNotSupportedException {
        java.util.Map map = new java.util.HashMap();
        for (int i=0;i<sizeDescription();i++) {
            String desc=getDescription(i);
            String loc=getDescriptionXmlLang(i);
            if ((locale==null && loc!=null) || (locale!=null && !locale.equalsIgnoreCase(loc)))
                map.put(loc, desc);
        }
        setAllDescriptions(map);
    }
    
    public void removeDescription() {
        try {
            removeDescriptionForLocale(null);
        } catch (VersionNotSupportedException ex){
            ErrorManager.getDefault().notify(ex);
        }
    }
    public void removeAllDescriptions() {
        setDescription(new String[]{});
    }
}
