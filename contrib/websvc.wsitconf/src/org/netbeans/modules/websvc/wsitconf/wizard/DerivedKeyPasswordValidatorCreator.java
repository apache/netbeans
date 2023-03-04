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

package org.netbeans.modules.websvc.wsitconf.wizard;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

public class DerivedKeyPasswordValidatorCreator {
  
    public DerivedKeyPasswordValidatorCreator() { }
     
    public DataObject generate(FileObject targetFolder, String targetName) {
        try {
            DataFolder folder = (DataFolder) DataObject.find(targetFolder);
            FileObject fo = null;
            fo = FileUtil.getConfigFile("Templates/WebServices/DerivedKeyPasswordValidator.java"); // NOI18N
            if (fo != null) {
                DataObject template = DataObject.find(fo);
                DataObject obj = template.createFromTemplate(folder, targetName);            
                return obj;
            }
        } catch (IOException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        }
        
        return null;
    }
    
}
