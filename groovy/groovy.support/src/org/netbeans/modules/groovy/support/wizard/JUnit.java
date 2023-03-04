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
package org.netbeans.modules.groovy.support.wizard;

/**
 *
 * @author martin
 */
public enum JUnit {

    JUNIT3("3.8.2"),        // NOI18N
    JUNIT4("4.10"),         // NOI18N
    NOT_DECLARED("");        // NOI18N

    
    private String version;

    private JUnit(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public String getMajorVersion() {
        int indexOfFirstDot = version.indexOf("."); //NOI18N
        if (indexOfFirstDot == -1) {
            indexOfFirstDot = version.length();
        }
        return version.substring(0, indexOfFirstDot);
    }
}