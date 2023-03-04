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

package org.netbeans.installer.utils.helper;

import org.netbeans.installer.utils.ResourceUtils;

public enum Status {
    NOT_INSTALLED("not-installed"),
    TO_BE_INSTALLED("to-be-installed"),
    INSTALLED("installed"),
    INSTALLED_DIFFERENT_BUILD("installed-different-build"),
    TO_BE_UNINSTALLED("to-be-uninstalled");
    
    private String name;
    
    private Status(final String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        switch (this) {
            case NOT_INSTALLED:
                return NOT_INSTALLED_STRING;
            case TO_BE_INSTALLED:
                return TO_BE_INSTALLED_STRING;
            case INSTALLED:
                return INSTALLED_STRING;
            case TO_BE_UNINSTALLED:
                return TO_BE_UNINSTALLED_STRING;
        }
        
        return null;
    }
    
    @Override
    public String toString() {
        return name;
    }
    private static final String NOT_INSTALLED_STRING = 
            ResourceUtils.getString(Status.class,
            "Status.not-installed");
    private static final String TO_BE_INSTALLED_STRING = 
            ResourceUtils.getString(Status.class,
            "Status.to-be-installed");
    private static final String INSTALLED_STRING = 
            ResourceUtils.getString(Status.class,
            "Status.installed");
    private static final String TO_BE_UNINSTALLED_STRING = 
            ResourceUtils.getString(Status.class,
            "Status.to-be-uninstalled");
}

