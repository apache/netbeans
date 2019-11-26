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
package org.netbeans.modules.javaee.project.api.ant;

/**
 * Misc project constants
 */
public class AntProjectConstants {

    public static final String DESTINATION_DIRECTORY_LIB = "200";
    public static final String ENDORSED_LIBRARY_NAME_6 = "javaee-endorsed-api-6.0"; // NOI18N
    public static final String ENDORSED_LIBRARY_CLASSPATH_6 = "${libs." + ENDORSED_LIBRARY_NAME_6 + ".classpath}"; // NOI18N
    public static final String ENDORSED_LIBRARY_NAME_7 = "javaee-endorsed-api-7.0"; // NOI18N
    public static final String ENDORSED_LIBRARY_CLASSPATH_7 = "${libs." + ENDORSED_LIBRARY_NAME_7 + ".classpath}"; // NOI18N
    public static final String ENDORSED_LIBRARY_NAME_8 = "javaee-endorsed-api-8.0"; // NOI18N
    public static final String ENDORSED_LIBRARY_CLASSPATH_8 = "${libs." + ENDORSED_LIBRARY_NAME_8 + ".classpath}"; // NOI18N
    public static final String DESTINATION_DIRECTORY_ROOT = "100";
    public static final String DESTINATION_DIRECTORY_DO_NOT_COPY = "300";
    public static final String DESTINATION_DIRECTORY = "destinationDirectory";

}
