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

package org.netbeans.modules.beans.addproperty;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CodeStyleUtils;
import org.netbeans.api.scripting.Scripting;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author  Sandip V. Chitale (Sandip.Chitale@Sun.Com)
 */
public class AddPropertyGenerator {

    public String generate(AddPropertyConfig addPropertyConfig, CodeStyle cs) {
        ScriptEngine scriptEngine = getScriptEngine();
        if (scriptEngine != null) {
            FileObject template = getTemplateFileObject(addPropertyConfig.getTEMPLATE_PATH());
            if (template != null && template.isValid()) {
                final String type = addPropertyConfig.getType().trim();
                final String name = addPropertyConfig.getName().trim();
                final String fieldName = CodeStyleUtils.addPrefixSuffix(name,
                        addPropertyConfig.isStatic() ? cs.getStaticFieldNamePrefix() : cs.getFieldNamePrefix(),
                        addPropertyConfig.isStatic() ? cs.getStaticFieldNameSuffix() : cs.getFieldNameSuffix());
                final String paramName = CodeStyleUtils.addPrefixSuffix(name,
                        cs.getParameterNamePrefix(),
                        cs.getParameterNameSuffix());
                final String paramIndex = CodeStyleUtils.addPrefixSuffix("index", //NOI18N
                        cs.getParameterNamePrefix(),
                        cs.getParameterNameSuffix());
                final String propName = CodeStyleUtils.addPrefixSuffix(
                        addPropertyConfig.getPopName().trim(),
                        cs.getStaticFieldNamePrefix(),
                        cs.getStaticFieldNameSuffix());
                final String initializer = addPropertyConfig.getInitializer().trim();
                String access;
                switch (addPropertyConfig.getAccess()) {
                    case PRIVATE:
                        access = "private "; // NOI18N
                        break;
                    case PROTECTED:
                        access = "protected "; // NOI18N
                        break;
                    case PUBLIC:
                        access = "public "; // NOI18N
                        break;
                    default:
                        access = "";
                        break;
                }

                ScriptContext scriptContext = scriptEngine.getContext();
                StringWriter writer = new StringWriter();
                scriptContext.setWriter(writer);
                scriptContext.setAttribute(FileObject.class.getName(), template, ScriptContext.ENGINE_SCOPE);
                scriptContext.setAttribute(ScriptEngine.FILENAME, template.getNameExt(), ScriptContext.ENGINE_SCOPE);
                scriptContext.setAttribute("access", access, ScriptContext.ENGINE_SCOPE); // NOI18N
                scriptContext.setAttribute("type", type, ScriptContext.ENGINE_SCOPE); // NOI18N
                scriptContext.setAttribute("className", addPropertyConfig.getClassName(), ScriptContext.ENGINE_SCOPE); // NOI18N
                scriptContext.setAttribute("name", name, ScriptContext.ENGINE_SCOPE); // NOI18N
                scriptContext.setAttribute("fieldName", fieldName, ScriptContext.ENGINE_SCOPE); // NOI18N
                scriptContext.setAttribute("paramName", paramName, ScriptContext.ENGINE_SCOPE); // NOI18N
                scriptContext.setAttribute("paramIndex", paramIndex, ScriptContext.ENGINE_SCOPE); // NOI18N
                scriptContext.setAttribute("initializer", initializer, ScriptContext.ENGINE_SCOPE); // NOI18N
                scriptContext.setAttribute("capitalizedName", CodeStyleUtils.getCapitalizedName(name), ScriptContext.ENGINE_SCOPE); // NOI18N
                scriptContext.setAttribute("getterName", CodeStyleUtils.computeGetterName(fieldName, type.equalsIgnoreCase("boolean"), addPropertyConfig.isStatic(), cs), ScriptContext.ENGINE_SCOPE); // NOI18N
                scriptContext.setAttribute("setterName", CodeStyleUtils.computeSetterName(fieldName, addPropertyConfig.isStatic(), cs), ScriptContext.ENGINE_SCOPE); // NOI18N
                scriptContext.setAttribute("static", Boolean.valueOf(addPropertyConfig.isStatic()), ScriptContext.ENGINE_SCOPE); // NOI18N
                scriptContext.setAttribute("final", Boolean.valueOf(addPropertyConfig.isFinale()), ScriptContext.ENGINE_SCOPE); // NOI18N
                AddPropertyConfig.GENERATE generateGetterSetter = addPropertyConfig.getGenerateGetterSetter();
                scriptContext.setAttribute("generateGetter", // NOI18N
                        Boolean.valueOf(generateGetterSetter == AddPropertyConfig.GENERATE.GETTER_AND_SETTER || generateGetterSetter == AddPropertyConfig.GENERATE.GETTER),
                        ScriptContext.ENGINE_SCOPE);
                scriptContext.setAttribute("generateSetter", // NOI18N
                        Boolean.valueOf(generateGetterSetter == AddPropertyConfig.GENERATE.GETTER_AND_SETTER || generateGetterSetter == AddPropertyConfig.GENERATE.SETTER),
                        ScriptContext.ENGINE_SCOPE);
                scriptContext.setAttribute("generateJavadoc", Boolean.valueOf(addPropertyConfig.isGenerateJavadoc()), ScriptContext.ENGINE_SCOPE); // NOI18N
                scriptContext.setAttribute("bound", Boolean.valueOf(addPropertyConfig.isBound()), ScriptContext.ENGINE_SCOPE); // NOI18N
                scriptContext.setAttribute("PROP_NAME", propName, ScriptContext.ENGINE_SCOPE); // NOI18N
                scriptContext.setAttribute("vetoable", Boolean.valueOf(addPropertyConfig.isVetoable()), ScriptContext.ENGINE_SCOPE); // NOI18N
                scriptContext.setAttribute("indexed", Boolean.valueOf(addPropertyConfig.isIndexed()), ScriptContext.ENGINE_SCOPE); // NOI18N
                scriptContext.setAttribute("propertyChangeSupport", addPropertyConfig.getPropertyChangeSupportName(), ScriptContext.ENGINE_SCOPE); // NOI18N
                scriptContext.setAttribute("vetoableChangeSupport", addPropertyConfig.getVetoableChangeSupportName(), ScriptContext.ENGINE_SCOPE); // NOI18N
                scriptContext.setAttribute("generatePropertyChangeSupport", Boolean.valueOf(addPropertyConfig.isGeneratePropertyChangeSupport()), ScriptContext.ENGINE_SCOPE); // NOI18N
                scriptContext.setAttribute("generateVetoablePropertyChangeSupport", Boolean.valueOf(addPropertyConfig.isGenerateVetoableChangeSupport()), ScriptContext.ENGINE_SCOPE); // NOI18N

                Reader templateReader = null;
                try {
                    templateReader = new InputStreamReader(template.getInputStream());
                    scriptEngine.eval(templateReader);
                    return writer.toString();
                } catch (ScriptException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    if (templateReader != null) {
                        try {
                            templateReader.close();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }

                    }
                }
            }
        }
        
        return "/*Error*/"; // NOI18N
    }
    
    private static FileObject getTemplateFileObject(String templatePath) {        
        return FileUtil.getConfigFile(templatePath);
    }

    private static ScriptEngine getScriptEngine() {
        return Scripting.createManager().getEngineByName("freemarker"); // NOI18N
    }
}
