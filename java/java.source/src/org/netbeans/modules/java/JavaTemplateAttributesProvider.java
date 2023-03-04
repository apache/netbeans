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

package org.netbeans.modules.java;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.openide.filesystems.FileObject;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.SpecificationVersion;

/**
 * Provides attributes that can be used inside scripting templates.
 * <dl>
 * <dt><code>package</code></dt>
 * <dd>attribute containing <code>target</code> folder as package.</dd>
 * <dt><code>javaSourceLevel</code></dt>
 * <dd>source level to be used for Java code (e.g. "1.5")</dd>
 * <dt><code>java15style</code></dt>
 * <dd>this attribute is defined only if java source level is
 * greater or equal to "1.5" (so generics/annotations may be used)</dd>
 * </dl>
 * 
 * @author Jan Pokorsky
 * @author Petr Slechta
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.loaders.CreateFromTemplateAttributesProvider.class)
public final class JavaTemplateAttributesProvider implements CreateFromTemplateAttributesProvider {
    
    private static final Logger LOG = Logger.getLogger(JavaTemplateAttributesProvider.class.getName());
    private static final SpecificationVersion VER15 = new SpecificationVersion("1.5");
    
    public Map<String,?> attributesFor(DataObject template, DataFolder target, String name) {
        FileObject templateFO = template.getPrimaryFile();
        if (!JavaDataLoader.JAVA_EXTENSION.equals(templateFO.getExt()) || templateFO.isFolder()) {
            return null;
        }
        
        FileObject targetFO = target.getPrimaryFile();
        Map<String,Object> result = new HashMap<String,Object>();
        
        ClassPath cp = ClassPath.getClassPath(targetFO, ClassPath.SOURCE);
        if (cp == null) {
            LOG.warning("No classpath was found for folder: " + target.getPrimaryFile()); // NOI18N
        }
        else {
            result.put("package", cp.getResourceName(targetFO, '.', false)); // NOI18N
        }
        
        String sourceLevel = SourceLevelQuery.getSourceLevel(targetFO);
        if (sourceLevel != null) {
            result.put("javaSourceLevel", sourceLevel); // NOI18N
            if (isJava15orLater(sourceLevel))
                result.put("java15style", Boolean.TRUE); // NOI18N
        }
        
        return result;
    }

    private boolean isJava15orLater(String sourceLevel) {
        SpecificationVersion ver = new SpecificationVersion(sourceLevel);
        return (ver.compareTo(VER15) >= 0);
    }
    
}
