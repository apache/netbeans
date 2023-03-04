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

package org.netbeans.nbbuild.testdist;

import java.io.File;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

/**
 * Fails if any path element is misssing. Attributes:<br>
 *  path - input paths for validation<br>
 * The task is used for validation runtime class in binary tests distribution.
 */
public class ValidatePath extends Task {

    private String failedProperty;

    private Path path;

    public void setPath(Path p) {
        if (path == null) {
            path = p;
        } else {
            path.append(p);
        }
    }

    public Path createPath() {
        if (path == null) {
            path = new Path(getProject());
        }
        return path.createPath();
    }

    public void setPathRef(Reference r) {
        createPath().setRefid(r);
    }

    public void setFailedProperty(String fp) {
        failedProperty = fp;
    }

    public @Override void execute() throws BuildException {
        for (String p : path.list()) {
            if (!new File(p).exists()) {
                String msg = "File " + p + " does not exist.";
                if (failedProperty == null) {
                    throw new BuildException(msg);
                } else {
                    log(msg, Project.MSG_WARN);
                }
                getProject().setNewProperty(failedProperty, "true");
                break;
            }
        }
    }

}
