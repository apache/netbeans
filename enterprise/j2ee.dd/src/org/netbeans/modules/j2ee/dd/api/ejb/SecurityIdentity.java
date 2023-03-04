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

package org.netbeans.modules.j2ee.dd.api.ejb;

//
// This interface has all of the bean info accessor methods.
//
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface;
import org.netbeans.modules.j2ee.dd.api.common.RunAs;

public interface SecurityIdentity extends CommonDDBean, DescriptionInterface {

    public static final String USE_CALLER_IDENTITY = "UseCallerIdentity";	// NOI18N
    public static final String RUN_AS = "RunAs";	// NOI18N

    public void setUseCallerIdentity(boolean value);
    
    public boolean isUseCallerIdentity();
    
    public void setRunAs(RunAs value);
    
    public RunAs getRunAs();
        
    public RunAs newRunAs();

}

