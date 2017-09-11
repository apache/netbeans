/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.execute.cmd;

import java.io.File;
import java.io.IOException;
import org.apache.maven.execution.ExecutionEvent;
import org.json.simple.JSONObject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
public class ExecProject extends ExecutionEventObject {

    public final GAV gav;
    public final @NullAllowed File currentProjectLocation;

    public ExecProject(GAV currentProject, File currentProjectLocation, ExecutionEvent.Type type) {
        super(type);
        this.gav = currentProject;
        this.currentProjectLocation = currentProjectLocation;
    }
    
    public @CheckForNull Project findProject() {
        if (currentProjectLocation != null) {
            FileObject fo = FileUtil.toFileObject(currentProjectLocation);
            if (fo != null) {
                try {
                    return ProjectManager.getDefault().findProject(fo);
                } catch (IOException ex) {
                    //Exceptions.printStackTrace(ex);
                } catch (IllegalArgumentException ex) {
                    //Exceptions.printStackTrace(ex);
                }
            }
        }
        return null;
    }

    public static ExecutionEventObject create(JSONObject obj, ExecutionEvent.Type t) {
        JSONObject prj = (JSONObject) obj.get("prj");
        String id = (String) prj.get("id");
        String[] ids = id.split(":");
        GAV prjGav = new GAV(ids[0], ids[1], ids[2]);
        File prjFile = null;
        String file = (String) prj.get("file");
        if (file != null) {
            prjFile = FileUtil.normalizeFile(new File(file));
        }
        return new ExecProject(prjGav, prjFile, t);
    }
    
}
