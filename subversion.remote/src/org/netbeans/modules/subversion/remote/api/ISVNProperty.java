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
package org.netbeans.modules.subversion.remote.api;

import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * 
 */
public interface ISVNProperty {
    
    public static final String MIME_TYPE = "svn:mime-type"; //NOI18N
    public static final String IGNORE = "svn:ignore"; //NOI18N
    public static final String EOL_STYLE = "svn:eol-style"; //NOI18N
    public static final String KEYWORDS = "svn:keywords"; //NOI18N
    public static final String EXECUTABLE = "svn:executable"; //NOI18N
    public static final String EXECUTABLE_VALUE = "*"; //NOI18N
    public static final String EXTERNALS = "svn:externals"; //NOI18N
    public static final String REV_AUTHOR = "svn:author"; //NOI18N
    public static final String REV_LOG = "svn:log"; //NOI18N
    public static final String REV_DATE = "svn:date"; //NOI18N
    public static final String REV_ORIGINAL_DATE = "svn:original-date"; //NOI18N

    String getName();

    String getValue();

    VCSFileProxy getFile();

    SVNUrl getUrl();

    byte[] getData();

}
