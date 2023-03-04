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

package org.netbeans.modules.websvc.wsitmodelext.security.proprietary;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;

/**
 *
 * @author Martin Grebac
 */
public interface ValidatorConfiguration extends ExtensibilityElement{

    public static final String TIMESTAMPFRESHNESS = "TimestampFreshnessLimit";  //NOI18N
//    public static final String MAXNONCEAGE = "MaxNonceAge";         //NOI18N
    public static final String MAXCLOCKSKEW = "MaxClockSkew";       //NOI18N
    public static final String REVOCATION = "Revocation";  //NOI18N

    void setVisibility(String vis);
    String getVisibility();
    
    void setMaxClockSkew(String maxClockSkew);
    String getMaxClockSkew();

    void setTimestampFreshnessLimit(String limit);
    String getTimestampFreshnessLimit();

    void setRevocationEnabled(boolean revocation);
    boolean isRevocationEnabled();
    
//    void setMaxNonceAge(String maxNonceAge);
//    String getMaxNonceAge();
}
