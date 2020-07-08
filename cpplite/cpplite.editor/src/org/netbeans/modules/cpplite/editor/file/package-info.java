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
@TemplateRegistrations({
    @TemplateRegistration(folder = "cpplite",
                          content = "CTemplate.c",
                          scriptEngine = "freemarker",
                          category = "cpplite",
                          displayName = "#CTemplate",
                          iconBase = "org/netbeans/modules/cpplite/editor/file/resources/CSrcIcon.gif"),
    @TemplateRegistration(folder = "cpplite",
                          content = "CPPTemplate.cpp",
                          scriptEngine="freemarker",
                          category = "cpplite",
                          displayName = "#CPPTemplate",
                          iconBase = "org/netbeans/modules/cpplite/editor/file/resources/CCSrcIcon.gif"),
    @TemplateRegistration(folder = "cpplite",
                          content = "HTemplate.h",
                          scriptEngine="freemarker",
                          category = "cpplite",
                          displayName = "#HTemplate",
                          iconBase = "org/netbeans/modules/cpplite/editor/file/resources/HDataIcon.gif"),
    @TemplateRegistration(folder = "cpplite",
                          content = "HPPTemplate.hpp",
                          scriptEngine="freemarker",
                          category = "cpplite",
                          displayName = "#HPPTemplate",
                          iconBase = "org/netbeans/modules/cpplite/editor/file/resources/HDataIcon.gif")
})
@Messages({
    "CTemplate=C file",
    "CPPTemplate=C++ file",
    "HTemplate=Header file",
    "HPPTemplate=Header for C++ file",
})
package org.netbeans.modules.cpplite.editor.file;

import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.api.templates.TemplateRegistrations;
import org.openide.util.NbBundle.Messages;
