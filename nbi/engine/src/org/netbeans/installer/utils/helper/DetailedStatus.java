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

public enum DetailedStatus {
    INSTALLED_SUCCESSFULLY,
    INSTALLED_WITH_WARNINGS,
    FAILED_TO_INSTALL,
    UNINSTALLED_SUCCESSFULLY,
    UNINSTALLED_WITH_WARNINGS,
    FAILED_TO_UNINSTALL;
    
    public String toString() {
        switch (this) {
            case INSTALLED_SUCCESSFULLY:
                return INSTALLED_SUCCESSFULLY_STRING;
            case INSTALLED_WITH_WARNINGS:
                return INSTALLED_WITH_WARNINGS_STRING;
            case FAILED_TO_INSTALL:
                return FAILED_TO_INSTALL_STRING;
            case UNINSTALLED_SUCCESSFULLY:
                return UNINSTALLED_SUCCESSFULLY_STRING;
            case UNINSTALLED_WITH_WARNINGS:
                return UNINSTALLED_WITH_WARNINGS_STRING;
            case FAILED_TO_UNINSTALL:
                return FAILED_TO_UNINSTALL_STRING;
            default:
                return null;
        }
    }
    private static final String INSTALLED_SUCCESSFULLY_STRING = 
            ResourceUtils.getString(DetailedStatus.class,
            "DetailedStatus.install.succes");//NOI18N
private static final String INSTALLED_WITH_WARNINGS_STRING = 
            ResourceUtils.getString(DetailedStatus.class,
            "DetailedStatus.install.warning");//NOI18N
private static final String FAILED_TO_INSTALL_STRING = 
            ResourceUtils.getString(DetailedStatus.class,
            "DetailedStatus.install.error");//NOI18N

private static final String UNINSTALLED_SUCCESSFULLY_STRING = 
            ResourceUtils.getString(DetailedStatus.class,
            "DetailedStatus.uninstall.success");//NOI18N
private static final String UNINSTALLED_WITH_WARNINGS_STRING = 
            ResourceUtils.getString(DetailedStatus.class,
            "DetailedStatus.uninstall.warning");//NOI18N
private static final String FAILED_TO_UNINSTALL_STRING = 
            ResourceUtils.getString(DetailedStatus.class,
            "DetailedStatus.uninstall.error");//NOI18N
}
