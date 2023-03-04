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

package org.netbeans.modules.ant.freeform;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 * Provides attributes that can be used inside scripting templates.
 * <dl><dt><code>project.license</code></dt>
 * <dd>attribute containing license name.
 * The provider reads <code>project-license</code> element value from project.xml
 * and returns it as the template attribute.
 * </dl>
 * 
 * @author Milan Kubec
 */
public class FreeformTemplateAttributesProvider implements CreateFromTemplateAttributesProvider {
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final FreeformFileEncodingQueryImpl encodingQuery;
    
    public FreeformTemplateAttributesProvider(AntProjectHelper helper, PropertyEvaluator eval, FreeformFileEncodingQueryImpl encodingQuery) {
        this.helper = helper;
        this.evaluator = eval;
        this.encodingQuery = encodingQuery;
    }
    
    public Map<String, ?> attributesFor(DataObject template, DataFolder target, String name) {
        Element primData = Util.getPrimaryConfigurationData(helper);
        Element licenseEl = XMLUtil.findElement(primData, "project-license", Util.NAMESPACE); // NOI18N
        Charset charset = encodingQuery.getEncoding(target.getPrimaryFile());
        if (licenseEl == null && charset == null) {
            return null;
        } else {
            Map<String, String> values = new HashMap<String, String>();
            if (licenseEl != null) {
                values.put("license", evaluator.evaluate(XMLUtil.findText(licenseEl))); // NOI18N
            }
            if (charset != null) {
                values.put("encoding", charset.name()); // NOI18N
            }
            return Collections.singletonMap("project", values); // NOI18N
        }
    }
    
}
