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

package org.netbeans.modules.j2ee.sun.dd.api;

import java.text.MessageFormat;
import java.util.ResourceBundle;


/**
 * Exception for cases when specific DTD specification doesn't support a particular property or method.
 *
 * @author  Milan Kuchtiak
 */
public class VersionNotSupportedException extends java.lang.Exception {

    private static String exceptionMsg = 
            ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/dd/api/Bundle").getString("MSG_versionNotSupported"); // NOI18N
    
    private String version;

    /**
     * Constructor for VersionNotSupportedException
     *
     * @param version specific version of Servlet Spec. e.g."2.4"
     * @param message exception message
     */
    public VersionNotSupportedException(String version, String message) {
        super(message);
        this.version=version;
    }
    
    /**
     * Constructor for VersionNotSupportedException
     * 
     * @param version specific version of Servlet Spec. e.g."2.4"
     */
    public VersionNotSupportedException(String version) {
        super(MessageFormat.format(exceptionMsg, version));
        this.version=version;
    }
    
    /**
     * Returns the version of deployment descriptor that caused this exception.
     * 
     * @return string specifying the DD version e.g. "2.4"
     */    
    public String getVersion() {
        return version;
    }
}
