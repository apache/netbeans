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
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.utils.helper.EnvironmentScope;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.SystemUtils;

/**
 *
 * @author Dmitry Lipin
 */
public abstract class Shell {
    protected String sp = " ";
    protected String sg = "=";
    protected String pr = "\"";
    protected String SETENV = "setenv" + sp;
    protected String EXPORT = "export ";
    protected String SET = "set ";
    
    protected  File getShellScript(EnvironmentScope scope) {
        File file =  null;
        if(scope!=null && EnvironmentScope.PROCESS != scope) {
            
            
            if(EnvironmentScope.ALL_USERS==scope) {
                file = getSystemShellScript();
            }
            
            if(EnvironmentScope.CURRENT_USER==scope || file==null) {
                file = getUserShellScript();
            }
        }
        LogManager.log(ErrorLevel.DEBUG,
                "Used shell file for setting environment variable : " + file);
        return file;
    }
    
    protected abstract String [] getSystemShellFileNames();
    protected abstract String [] getUserShellFileNames();
    public abstract boolean setVar(String name, String value, EnvironmentScope scope)  throws IOException;
    protected abstract String [] getAvailableNames();
    
    public boolean isCurrentShell(String name) {
        String [] names = getAvailableNames();
        boolean result = false;
        if(names!=null && name!=null) {
            for(String shname:names) {
                if(shname.equals(name)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
    
    protected File getUserShellScript() {
        return getSh(getUserShellFileNames(),
                SystemUtils.getUserHomeDirectory().getPath());
    }
    protected File getSystemShellScript() {
        return getSh(getSystemShellFileNames(),
                File.separator + "etc");
        
    }
    protected File getSh(String [] locations, String root) {
        if(locations == null) {
            return null;
        }
        File file = null;
        File firstFile = null;
        for(String loc: locations) {
            if(loc!=null) {
                file = new File(!loc.startsWith(File.separator) ?
                    root + File.separator + loc :
                    loc);
                if(firstFile == null) {
                    firstFile = file;
                }
                
                if(file.exists()) {
                    return file;
                }
            }
        }
        return firstFile;
        
    }
    protected int getSetEnvIndex(List <String> strings) {
        int idx = 0 ;
        int index = strings.size() ;
        for(String str:strings) {
            idx ++ ;
            if(str.startsWith(SET) || str.startsWith(EXPORT) || str.startsWith(SETENV)) {
                index = idx;
            }
        }
        return index;
    }
    protected List<String> getList(File file) throws IOException {
        return (file.canRead()) ?
            FileUtils.readStringList(file) :
            new LinkedList<String> ();
    }
    protected boolean writeList(List <String> strings, File file) throws IOException{
        if(!file.exists()) {
            if(!file.createNewFile()) {
                return false;
            };
        }
        if(file.canWrite()) {
            FileUtils.writeStringList(file,strings);
            return true;
        }
        return false;
    }
}
