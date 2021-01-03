/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.python.project;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.python.project.ui.customizer.PythonProjectProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;


public class PythonProjectTemplateAttributesProvider implements CreateFromTemplateAttributesProvider {
    private final PropertyEvaluator eval;
    
    public PythonProjectTemplateAttributesProvider(final PropertyEvaluator eval) {
        this.eval = eval;
    }

    @Override
    public Map<String, ?> attributesFor(DataObject template, DataFolder target, String name) {
        Map<String,Object> result = new HashMap<>();
        String encoding = eval.getProperty(PythonProjectProperties.SOURCE_ENCODING);
        if (encoding != null) {
            result.put("encoding", encoding); //NOI18N
        }
        return result;
    }
}
