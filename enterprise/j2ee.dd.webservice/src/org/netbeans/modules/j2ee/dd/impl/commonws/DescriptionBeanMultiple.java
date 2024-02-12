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

/**
 * Superclass that implements DescriptionInterface for Servlet2.4 beans.
 *
 * @author  Milan Kuchtiak
 */

package org.netbeans.modules.j2ee.dd.impl.commonws;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

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
            Iterator<String> keys = descriptions.keySet().iterator();
            int i=0;
            while (keys.hasNext()) {
                String key = keys.next();
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

    @Override
    public Map<String, String> getAllDescriptions() {
        Map<String, String> map =new HashMap<>();
        for (int i=0;i<sizeDescription();i++) {
            String desc=getDescription(i);
            String loc=getDescriptionXmlLang(i);
            map.put(loc, desc);
        }
        return map;
    }
    
    public void removeDescriptionForLocale(String locale) throws VersionNotSupportedException {
        Map<String, String> map = new HashMap<>();
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
