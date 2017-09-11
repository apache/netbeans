/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.beans.addproperty;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CodeStyleUtils;
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
        return new ScriptEngineManager().getEngineByName("freemarker"); // NOI18N
    }
}
