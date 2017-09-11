/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
