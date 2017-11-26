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
package org.netbeans.modules.hibernate.wizards;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.hibernate.wizards.support.SelectedTables;
import org.netbeans.modules.hibernate.wizards.support.TableClosure;
import org.openide.filesystems.FileObject;

/**
 * Helper class to access all the information from the panels
 * used by the reverse engineering wizard.
 * @author gowri
 */
public class HibernateCodeGenWizardHelper {

    private Project project;
    private TableClosure tableClosure;
    private SelectedTables selectedTables;
    private SourceGroup location;
    private String packageName;    
    private boolean domainGen;
    private boolean hbmGen;
    private boolean javaSyntax;
    private boolean ejbAnnotation;
    private FileObject confFile;
    private FileObject revengFile;
    private String schemaName;
    private String catalogName;

    public HibernateCodeGenWizardHelper(Project project) {
        this.project = project;
    }
    
    public Project getProject() {
        return project;
    }
    
    public SourceGroup getLocation() {
        return location;
    }

    public void setLocation(SourceGroup location) {
        this.location = location;
    }
    
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public boolean getDomainGen() {
        return domainGen;
    }
    
    public void setDomainGen(boolean value) {
        this.domainGen = value;
    }
    
    public boolean getHbmGen() {
        return hbmGen;
    }
    
    public void setHbmGen(boolean value) {
        this.hbmGen = value;
    }   
    
    public void setJavaSyntax(boolean value) {
        this.javaSyntax = value;
    }
    
    public boolean getJavaSyntax() {
        return javaSyntax;
    }
    
    public void setEjbAnnotation(boolean value) {
        this.ejbAnnotation = value;
    }
    
    public boolean getEjbAnnotation() {
        return ejbAnnotation;
    }
    
    public void setConfigurationFile(FileObject confFile) {
        this.confFile = confFile;
    }
    
    public FileObject getConfigurationFile() {
        return confFile;
    }

    public void setRevengFile(FileObject revengFile) {
        this.revengFile = revengFile;
    }

    public FileObject getRevengFile() {
        return revengFile;
    }

    
    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
    
    public String getSchemaName() {
        return schemaName;
    }
    
    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }
    
    public String getCatalogName() {
        return catalogName;
    }
    
}
