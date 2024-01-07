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
@TemplateRegistration(folder = "Other", iconBase = JavaTemplates.JAVA_ICON, displayName = "#JAVAtemplate_displayName", content = "File.java.template", requireProject = false, targetName = "newJavaFile", description = "Description.html", scriptEngine = "freemarker", category = {"java-main-class"}, position=300)
        
@Messages(value = "JAVAtemplate_displayName=Java File")
package org.netbeans.modules.java.file.launcher.templates;

import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.openide.util.NbBundle.Messages;
