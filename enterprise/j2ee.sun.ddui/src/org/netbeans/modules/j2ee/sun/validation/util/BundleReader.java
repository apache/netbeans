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

package org.netbeans.modules.j2ee.sun.validation.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.netbeans.modules.j2ee.sun.validation.Constants;


/**
 * BundleReader  is a Class  to read properties from the bundle.
 * <code>getValue()</code> method can be used to read the properties
 * from the bundle file(Bundle.properties). Default bundle file used
 * is <code>{ @link Constants }.BUNDLE_FILE</code>. Bundle file to use
 * can be set by using <code>setBundle()</code> method.
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class BundleReader {

    /**
     * A resource bundle of this reader.
     */
    // !PW FIXME Get rid of this, probably requires eliminating this entire class
    //     and upgrading callers to use some NbBundle variant for bundle management.
    //     See IZ 96422
    private static ResourceBundle resourceBundle;

    
    /** Creates a new instance of BundleReader */
    public BundleReader() {
    }

    /**
     * Gets the value of the the given <code>key</code> from the bundle
     * 
     * @param key the key of which, the value needs to be fetched from
     * the bundle.
     */
    public static String getValue(String key) {
        if(resourceBundle == null)
            return key;
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException missingResourceException) {
            return key;
        }
    }


    /**
     * sets the given bundle file as the file to use by this object.
     */
    public static void setBundle(String bundleFile){
        try {
            resourceBundle = ResourceBundle.getBundle(bundleFile);
        } catch (Exception ex) { }
    }
    

    static {
        try {
            resourceBundle = ResourceBundle.getBundle(Constants.BUNDLE_FILE);
        } catch (Exception ex) { }
    }
}
