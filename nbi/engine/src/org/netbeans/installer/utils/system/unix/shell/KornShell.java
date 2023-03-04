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

package org.netbeans.installer.utils.system.unix.shell;

/**
 *
 * @author Lipin Dmitry
 */
public class KornShell extends BourneShell {
      private String [] SYSTEM_PROFILE_FILES = {       
        "profile",
        "kshrc"
                
    };
    
    private String [] USER_PROFILE_HOMEDIRFILES = {
        ".profile",
        ".kshrc"
    };
  
    public String [] getAvailableNames() {
        return new String [] { "ksh", "rksh", "pfksh", "pdksh"};
    }
}
