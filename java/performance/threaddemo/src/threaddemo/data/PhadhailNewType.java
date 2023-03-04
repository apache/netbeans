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

package threaddemo.data;

import java.io.IOException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.datatransfer.NewType;
import threaddemo.model.Phadhail;

/**
 * A new type for (container) phadhails.
 * @author Jesse Glick
 */
public final class PhadhailNewType extends NewType {

    private final Phadhail ph;
    private final boolean dir;

    public PhadhailNewType(Phadhail ph, boolean dir) {
        if (!ph.hasChildren()) throw new IllegalArgumentException();
        this.ph = ph;
        this.dir = dir;
    }
    
    public void create() throws IOException {
        String title = "Create New " + getName();
        NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine("New name:", title);
        if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
            String name = d.getInputText();
            if (dir) {
                ph.createContainerPhadhail(name);
            } else {
                ph.createLeafPhadhail(name);
            }
        }
    }
    
    public String getName() {
        if (dir) {
            return "Directory";
        } else {
            return "File";
        }
    }
    
}

