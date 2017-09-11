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

package org.netbeans.installer.infra.build.ant;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.build.ant.utils.Utils;

/**
 * This class is an ant task that is capable or releasing an NBI engine to the 
 * registries server.
 * 
 * @author Kirill Sorokin
 */
public class ReleaseEngine extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * URL of the registries server to which the engine should be released.
     */
    private String url;
    
    /**
     * The engine distributive file.
     */
    private File archive;
    
    // setters //////////////////////////////////////////////////////////////////////
    /**
     * Setter for the 'url' property.
     * 
     * @param url The new value of the 'url' property.
     */
    public void setUrl(String url) {
        this.url = url;
    }
    
    /**
     * Setter for the 'archive' property.
     * 
     * @param path The new value of the 'archive' property.
     */
    public void setArchive(String path) {
        archive = new File(path);
        if (!archive.equals(archive.getAbsoluteFile())) {
            archive = new File(getProject().getBaseDir(), path);
        }
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    /**
     * Executes the task. This method sends an HTTP POST request tyo the server, 
     * uploading the engine's archive.
     * 
     * @throws org.apache.tools.ant.BuildException if an I/O error occurs.
     */
    public void execute() throws BuildException {
        try {
            final Map<String, Object> args = new HashMap<String, Object>();
            
            args.put("archive", archive); // NOI18N
            
            
            String response = Utils.post(url + "/update-engine", args); // NOI18N
            
            log(response);
            if (!response.startsWith("200")) { // NOI18N
                throw new BuildException("Failed to release the engine."); // NOI18N
            }
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
}
