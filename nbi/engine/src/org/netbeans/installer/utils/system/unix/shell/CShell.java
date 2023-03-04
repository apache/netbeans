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

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.installer.utils.helper.EnvironmentScope;

/**
 *
 * @author dlm198383
 */
public class CShell extends Shell{
    private String [] SYSTEM_PROFILE_FILES = {
        "csh.cshrc",
        "csh.login",
        "profile",
        ".cshrc",
    };
    
    private String [] USER_PROFILE_HOMEDIRFILES = {
        ".cshrc.user",
        ".cshrc",
        ".profile",
        ".login"
    };
    /** Creates a new instance of Bash */
    public CShell() {
    }
    
    public boolean setVar(String name, String value, EnvironmentScope scope) throws IOException {
        File file = getShellScript(scope);
        if(file==null)  {
            return false;
        }
        List <String> strings = getList(file);
        
        boolean exist = false;
        String str;
        String substr;
        for(int i=0;i<strings.size();i++) {
            
            str = strings.get(i);
            if(str!=null) {
                str = str.trim();
               
                if(str.startsWith(SETENV)) {
                    substr = str.substring(SETENV.length());
                    substr = substr.trim();
                                        
                    if(substr.startsWith(name + sg) || substr.startsWith(name + sp) || substr.equals(name)) {
                        if(value==null) {                            
                            strings.remove(i);
                            i--;
                        } else {                            
                            strings.set(i, SETENV + name + sp + pr + value + pr);
                        }
                        exist = true;
                        break;
                    }
                }
            }
        }
        if(!exist) {            
            strings.add(getSetEnvIndex(strings),SETENV + name + sp + pr + value + pr);
        }
        
        return writeList(strings,file);
    }
    
    public String [] getSystemShellFileNames() {
        return SYSTEM_PROFILE_FILES;
    }
    
    public String[] getUserShellFileNames() {
        return USER_PROFILE_HOMEDIRFILES;
    }
    public String [] getAvailableNames() {
        return new String [] { "csh", "pfcsh"};
    }
}
