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
package org.netbeans.modules.javafx2.project.fxml;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.javafx2.project.JFXProjectProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 * Provides attributes that can be used inside scripting templates.
 * <dl>
 * <dt><code>package</code></dt>
 * <dd>attribute containing <code>target</code> folder as package.</dd>
 * </dl>
 * 
 * @author Petr Somol
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.loaders.CreateFromTemplateAttributesProvider.class)
public final class FXMLTemplateAttributesProvider implements CreateFromTemplateAttributesProvider{

    private static final Logger LOG = Logger.getLogger(FXMLTemplateAttributesProvider.class.getName());
    
    @Override
    public Map<String, ?> attributesFor(DataObject template, DataFolder target, String name) {
        FileObject templateFO = template.getPrimaryFile();
        if (!JFXProjectProperties.FXML_EXTENSION.equals(templateFO.getExt()) || templateFO.isFolder()) {
            return null;
        }
        
        FileObject targetFO = target.getPrimaryFile();
        Map<String,Object> result = new HashMap<String,Object>();
        
        ClassPath cp = ClassPath.getClassPath(targetFO, ClassPath.SOURCE);
        if (cp == null) {
            LOG.log(
                Level.WARNING,
                "No classpath was found for folder: {0}",   // NOI18N
                FileUtil.getFileDisplayName(targetFO));
        } else if (cp.findOwnerRoot(targetFO) == null) {
            LOG.log(
                Level.WARNING,
                "Folder {0} is not on its classpath: {1}",  // NOI18N
                new Object[] {
                    FileUtil.getFileDisplayName(targetFO),
                    cp.toString(ClassPath.PathConversionMode.PRINT)
                });
        }
        else {
            result.put("package", cp.getResourceName(targetFO, '.', false)); // NOI18N
        }
        
        return result;
    }
    
}
