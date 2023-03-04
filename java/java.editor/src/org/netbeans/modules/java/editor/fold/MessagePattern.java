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

package org.netbeans.modules.java.editor.fold;

import java.util.regex.Pattern;

/**
 * Describes a call in the code that obtains a message from the bundle, or bundle instance.
 * @author sdedic
 */
public final class MessagePattern {
    /**
     * Usable as 'bundleFile' param. The code uses ResourceBundle instance stored in a variable/field
     */
    public static final int BUNDLE_FROM_INSTANCE = -2;
    
    /**
     * Usable as 'bundleFile' param. Specifies that the bundle file should be derived from the target class name.
     */
    public static final int BUNDLE_FROM_CLASS = -4;
    
    /**
     * Usable as a value of 'key'. No key param is present, the pattern describes the code to obtain a bundle instance.
     */
    public static final int GET_BUNDLE_CALL = -3;
    
    /**
     * Usable as a value of 'keyParam'. The resource bundle key is derived from the called method name.
     */
    public static final int KEY_FROM_METHODNAME = -5;
    
    /**
     * Name of type that defines the method. May contain wildcards.
     */
    private Pattern ownerTypePattern;
    /**
     * Pattern for the method name
     */
    private Pattern methodNamePattern;
    /**
     * Bundle param defines location of the bundle.  {@link #BUNDLE_FROM_INSTANCE} means
     * that the instance assignment has to be found and inspected for bundle name
     */
    private int bundleParam;
    /**
     * Param index of the message key
     */
    private int keyParam;
    private String bundleFile = "Bundle"; // NOI18N

    public MessagePattern(Pattern ownerTypePattern, Pattern methodNamePattern, int bundleParam, int keyParam) {
        assert bundleParam >= 0 || (bundleParam == BUNDLE_FROM_CLASS || bundleParam == BUNDLE_FROM_INSTANCE);
        assert keyParam >= 0 || (keyParam == KEY_FROM_METHODNAME || keyParam == GET_BUNDLE_CALL);
        
        this.ownerTypePattern = ownerTypePattern;
        this.methodNamePattern = methodNamePattern;
        this.bundleParam = bundleParam;
        this.keyParam = keyParam;
    }

    public Pattern getOwnerTypePattern() {
        return ownerTypePattern;
    }

    public Pattern getMethodNamePattern() {
        return methodNamePattern;
    }

    public int getBundleParam() {
        return bundleParam;
    }

    public int getKeyParam() {
        return keyParam;
    }

    public String getBundleFile() {
        return bundleFile;
    }

    public void setBundleFile(String bundleFile) {
        this.bundleFile = bundleFile;
    }
    
}
