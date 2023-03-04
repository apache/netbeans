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
