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
package org.netbeans.modules.rust.project.templates.privileged;

import java.io.IOException;
import java.util.List;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.api.templates.CreateFromTemplateHandler;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.api.templates.TemplateRegistrations;
import org.netbeans.modules.rust.project.RustProject;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 * PrivilegedTemplates for Rust projects.
 */
@TemplateRegistrations({
    @TemplateRegistration(folder = "rust",
            content = "rust-file.rs",
            scriptEngine = "freemarker",
            category = "rust",
            displayName = "#RustFile",
            iconBase = RustProject.ICON),})
@Messages({
    "RustFile=Rust file",})
public final class RustPrivilegedTemplates extends CreateFromTemplateHandler
        implements PrivilegedTemplates {

    private static final String[] TEMPLATES = new String[]{
        "Templates/rust/file.rs", // NOI18N
    };

    @Override
    public String[] getPrivilegedTemplates() {
        return TEMPLATES;
    }

    @Override
    protected boolean accept(CreateDescriptor desc) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    protected List<FileObject> createFromTemplate(CreateDescriptor desc) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
