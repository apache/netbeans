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

package org.netbeans.installer.utils.system.shortcut;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dmitry Lipin
 */
public class FileShortcut extends Shortcut {
    private File target;
    private boolean modifyPath;
    private List<String> arguments;
    
    public FileShortcut(String name, File target) {
        super(name);
        setTarget(target);    
        setArguments(new ArrayList<String>());
    }
    
    public boolean canModifyPath() {
        return modifyPath;
    }
    
    public void setModifyPath(final boolean modifyPath) {
        this.modifyPath = modifyPath;
    }
    
    public String getTargetPath() {
        return target.getPath();
    }    
    public File getTarget() {
        return target;
    }
    public void setTarget(File target) {
        this.target = target;
    }
    
    
    public List<String> getArguments() {
        return this.arguments;
    }
    
    public String getArgumentsString() {
        if (arguments.size() != 0) {
            StringBuilder builder = new StringBuilder();
            
            for (int i = 0; i < arguments.size(); i++) {
                builder.append(arguments.get(i));
                
                if (i != arguments.size() - 1) {
                    builder.append(" ");
                }
            }
            
            return builder.toString();
        }  else {
            return null;
        }
    }
    
    public void setArguments(final List<String> arguments) {
        this.arguments = arguments;
    }
    
    public void addArgument(final String argument) {
        arguments.add(argument);
    }
    
    public void removeArgument(final String argument) {
        arguments.remove(argument);
    }
    @Deprecated
    public File getExecutable() {
        return getTarget();
    }
    @Deprecated
    public String getExecutablePath() {
        return getTargetPath();
    }
    @Deprecated
    public void setExecutable(final File executable) {
        setTarget(executable);
    }
    @Deprecated
    public boolean canModifyExecutablePath() {
        return canModifyPath();
    }
    @Deprecated
    public void setModifyExecutablePath(final boolean modifyExecutablePath) {
        setModifyPath(modifyExecutablePath);
    }
}
