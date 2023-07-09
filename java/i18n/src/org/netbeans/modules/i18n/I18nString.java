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


package org.netbeans.modules.i18n;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;

import org.openide.loaders.DataObject;
import org.openide.util.MapFormat;


/**
 * This object represent i18n values which will be used by actual
 * i18n-izing of found hard coded string. I.e. resource where will be stored
 * new key-value pair, actual key-value pair and replace code wich will
 * replace found hard coded string.
 * <p>
 * It also prescribes that each subclass MUST have <b>copy constuctor</b>
 * calling its superclass copy constructor. The copy constructor MUST be  then 
 * called during <b>cloning</b>. All subclasses must also support oposite
 * process <b>becoming</b>
 *
 * @author  Peter Zavadsky
 * @author  Petr Kuzel
 */
public class I18nString {

    /** 
     * Support for this i18n string istance. 
     * It contains implementation.
     */
    protected I18nSupport support;
    
    /** The key value according the hard coded string will be i18n-ized. */
    protected String key;
    
    /** The "value" value which will be stored to resource. */
    protected String value;
    
    /** Comment for key-value pair stored in resource. */
    protected String comment;
    
    /** Replace format. */
    protected String replaceFormat;

    
    /** 
     * Creates new I18nString. 
     * @param support <code>I18nSupport</code> linked to this instance,
     * has to be non-null 
     */
    protected I18nString(I18nSupport support) {
        if (support == null) {
            throw new NullPointerException();
        }

        this.support = support;
        
        //??? what is this
        replaceFormat = I18nUtil.getOptions().getReplaceJavaCode();
    }

    /**
     * Copy contructor.
     */
    protected  I18nString(I18nString copy) {
        this.key = copy.key;
        this.value = copy.value;
        this.comment = copy.comment;
        this.replaceFormat = copy.replaceFormat;
        this.support = copy.support;
    }
    
    /**
     * Let this instance take its state from passed one.
     * All subclasses must extend it.
     */
    public void become(I18nString copy) {
        this.key = copy.key;
        this.value = copy.value;
        this.comment = copy.comment;
        this.replaceFormat = copy.replaceFormat;
        this.support = copy.support;
    }
    
    /**
     * Cloning must use copy contructors.
     */
    @Override
    public Object clone() {
        return new I18nString(this);
    }
    
    /** Getter for <code>support</code>. */
    public I18nSupport getSupport() {
        return support;
    }
    
    /** Getter for <code>key</code>. */
    public String getKey() {
        return key;
    }

    /** Setter for <code>key</code>. */
    public void setKey(String key) {
        if (Objects.equals(this.key, key)) {
            return;
        }

        this.key = key;
    }
    
    /** Getter for <code>value</code>. */
    public String getValue() {
        return value;
    }

    /** Setter for <code>value</code>. */
    public void setValue(String value) {
        if (Objects.equals(this.value, value)) {
            return;
        }

        this.value = value;
    }

    /** Getter for <code>comment</code>. */
    public String getComment() {
        return comment;
    }

    /** Setter for <code>comment</code>. */
    public void setComment(String comment) {
        if (Objects.equals(this.comment, comment)) {
            return;
        }

        this.comment = comment;
    }

    /** Getter for replace format property. */
    public String getReplaceFormat() {
        return replaceFormat;
    }
    
    /** Setter for replace format property. */
    public void setReplaceFormat(String replaceFormat) {
        this.replaceFormat = replaceFormat;
    }

    /** 
     * Derive replacing string. The method substitutes parameters into
     * a format string using <code>MapFormat.format</code>. If you
     * override this method, you must not call <code>.format</code>
     * on the return value because values substituted in the previous 
     * round can contain control codes. All replacements
     * must take place simultaneously in a single, the first, call. Thus, if you
     * need to substitute some additional parameters not substituted by
     * default, use 
     * the provided hook {@link #fillFormatMap}.
     * 
     * @return replacing string or null if this instance is invalid 
     */
    public String getReplaceString() {
        if (getKey() == null || getSupport() == null) {
            return null;
        }
        
        if (replaceFormat == null) {
            replaceFormat = I18nUtil.getOptions().getReplaceJavaCode();
        }

        // Create map.
        
        DataObject sourceDataObject = getSupport().getSourceDataObject();

        Map<String,String> map = new HashMap<String,String>(4);

        if (getSupport().getResourceHolder().getResource() != null) { // Issue 150287
            FileObject fo = getSupport().getResourceHolder().getResource().getPrimaryFile();
            ClassPath cp = Util.getExecClassPath(sourceDataObject.getPrimaryFile(), fo);
            if (cp == null) {
                return null; // #148081 properties file not found on classpath, likely invalid
            }
            map.put("bundleNameSlashes", Util.getResourceName(sourceDataObject.getPrimaryFile(), fo, '/', false)); // NOI18N
            map.put("bundleNameDots", Util.getResourceName(sourceDataObject.getPrimaryFile(), fo, '.', false)); // NOI18N
        }

        map.put("key", getKey()); // NOI18N
        map.put("sourceFileName", sourceDataObject == null ? "" : sourceDataObject.getPrimaryFile().getName()); // NOI18N

        fillFormatMap(map);
        String res = null;
        try {
            res = MapFormat.format(replaceFormat, map);
        } catch (IllegalArgumentException ilae) {
            return null;
        }
        return res;
    }

    /**
     * Hook for filling in additional format key/value pair in
     * subclasses. Within the method, the provided substituion map can
     * be arbitrarilly modified.
     * @param subst Map to be filled in with key/value pairs
     */ 
    protected void fillFormatMap(Map<String,String> subst) {
    }
}
