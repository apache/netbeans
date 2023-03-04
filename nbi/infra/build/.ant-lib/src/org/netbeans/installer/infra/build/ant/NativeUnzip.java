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

package org.netbeans.installer.infra.build.ant;

import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Expand;
import org.netbeans.installer.infra.build.ant.utils.Utils;

public class NativeUnzip extends Expand {
    private File dest; //req
    private File source; // req
    
    @Override
    public void setDest(File d) {
        this.dest = d;
        
        super.setDest(d);
    }
    
    @Override
    public void setSrc(File s) {
        this.source = s;
        
        super.setSrc(s);
    }
    
    @Override
    public void execute() throws BuildException {
        try {
            Utils.setProject(getProject());
            log("trying native unzip");
            
            Utils.nativeUnzip(source, dest);
        } catch (IOException e) {
            log("native unzip failed, falling back to java implementation");
            
            Utils.delete(dest);
            super.execute();
        }
    }
}
