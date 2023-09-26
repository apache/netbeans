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
package org.netbeans.modules.payara.tooling.data;

/**
 *
 * @author jGauravGupta
 */
public interface PayaraPlatformVersionAPI {

    /**
     * Version elements separator character.
     */
    char SEPARATOR = '.';

    /**
     * Version elements separator REGEX pattern.
     */
    String SEPARATOR_PATTERN = "\\.";

    /**
     * Get major version number.
     *
     * @return Major version number.
     */
    short getMajor();

    /**
     * Get minor version number.
     * <p/>
     * @return Minor version number.
     */
    short getMinor();

    /**
     * Get update version number.
     * <p/>
     * @return Update version number.
     */
    short getUpdate();

    /**
     * Get build version number.
     * <p/>
     * @return Build version number.
     */
    short getBuild();

    String getUriFragment();

    String getDirectUrl();

    String getIndirectUrl();

    String getLicenseUrl();
    
    String toFullString();

    boolean isMinimumSupportedVersion();

    public boolean isEE7Supported();

    public boolean isEE8Supported();

    public boolean isEE9Supported();

    public boolean isEE10Supported();
    
    /**
     * Compare major and minor parts of version number <code>String</code>s.
     * <p/>
     * @param version Payara Platform version to compare with this object.
     * @return Value of <code>true</code> when major and minor parts of version
     * numbers are the same or <code>false</code> otherwise.
     */
    public boolean equalsMajorMinor(final PayaraPlatformVersionAPI version);

    /**
     * Compare all parts of version number <code>String</code>s.
     * <p/>
     * @param version Payara Platform version to compare with this object.
     * @return Value of <code>true</code> when all parts of version numbers are
     * the same or <code>false</code> otherwise.
     */
    public boolean equals(final PayaraPlatformVersionAPI version);
    
}
