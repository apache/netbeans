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

package org.netbeans.modules.java.source;

import java.io.IOException;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
@ServiceProvider(service = FileObjectFromTemplateCreator.class)
public final class DefaultFileObjectFromTemplateCreator implements FileObjectFromTemplateCreator {

    @Override
    public FileObject create(FileObject template, FileObject folder, String name) throws IOException {
        DataObject templateDobj = DataObject.find(template);
        if (templateDobj == null || !templateDobj.isTemplate()) {
            return FileUtil.createData(folder, name);
        }
        DataFolder target = DataFolder.findFolder(folder);
        String simpleName = FileObjects.stripExtension(name);
        DataObject newDobj = templateDobj.createFromTemplate(target, simpleName);
        return newDobj.getPrimaryFile();
    }    
}
