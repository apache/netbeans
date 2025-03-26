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
package org.netbeans.modules.javascript2.editor.classpath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.Model;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;
import org.netbeans.modules.javascript2.model.spi.ModelInterceptor;
import org.netbeans.modules.javascript2.model.spi.ModelInterceptor.Registration;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

@Registration
public class MetaPropertiesModelContributor implements ModelInterceptor {

    private static final Logger LOG = Logger.getLogger(MetaPropertiesModelContributor.class.getName());

    private static Model GLOBALS = null;

    @Override
    public Collection<JsObject> interceptGlobal(ModelElementFactory factory, FileObject fo) {
        if(GLOBALS == null) {
            try {
                FileObject globalsJS = URLMapper.findFileObject(MetaPropertiesModelContributor.class.getResource("metaproperties.js")); //NOI18N
                Source source = Source.create(globalsJS);
                ParserManager.parse(Set.of(source), (resultIterator) -> {
                    Model model = Model.getModel((JsParserResult) resultIterator.getParserResult(), true);
                    JsObject globalsVariables = model.getGlobalObject().getProperty("metaproperties"); //NOI18N
                    for(String propertyName: new ArrayList<>(globalsVariables.getProperties().keySet())) {
                        globalsVariables.moveProperty(propertyName, model.getGlobalObject());
                    }
                    model.getGlobalObject().getProperties().remove("metaproperties"); //NOI18N
                    GLOBALS = model;
                });
            } catch (ParseException ex) {
                LOG.log(Level.WARNING, "Failed to initialize metaproperties.js to supply i.e. new.target and import.meta");
            }
        }
        JsObject globals = GLOBALS.getGlobalObject();
        return List.of(globals);
    }

}
