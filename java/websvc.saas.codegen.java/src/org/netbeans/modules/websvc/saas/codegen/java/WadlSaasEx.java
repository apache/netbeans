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

package org.netbeans.modules.websvc.saas.codegen.java;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.websvc.saas.codegen.java.support.Xsd2Java;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.WadlSaas;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.websvc.saas.util.SaasUtil;

/**
 *
 * @author ayubkhan
 */
public class WadlSaasEx {
    private WadlSaas wadlSaas;
    private ArrayList<FileObject> jaxbJars;
    private ArrayList<FileObject> jaxbSourceJars;
   
    public WadlSaasEx(WadlSaas wadlSaas) {
        this.wadlSaas = wadlSaas;
    }
    
    private boolean compileSchemas() throws IOException {
        assert this.wadlSaas != null;
        assert this.wadlSaas.getWadlModel() != null;
        jaxbJars = new ArrayList<FileObject>();
        jaxbSourceJars = new ArrayList<FileObject>();
        for (FileObject xsdFile : this.wadlSaas.getLocalSchemaFiles()) {
            Xsd2Java xjCompiler = new Xsd2Java(xsdFile, this.wadlSaas.getPackageName() + "." + 
                    SaasUtil.toValidJavaName(xsdFile.getName()).toLowerCase());
            if (! xjCompiler.compile()) {
                return false;
            }
            jaxbJars.add(xjCompiler.getJaxbJarFile());
            jaxbSourceJars.add(xjCompiler.getJaxbSourceJarFile());
        }
        
        return true;
    }

    public List<FileObject> getLibraryJars() {
        List<FileObject> result = new ArrayList<FileObject>();
        if (jaxbJars == null) {
            try {
                compileSchemas();
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        }
        result.addAll(jaxbJars);
        return result;
    }
    
    public List<FileObject> getJaxbSourceJars() {
        if (jaxbSourceJars == null) {
            try {
                compileSchemas();
                return Collections.unmodifiableList(jaxbSourceJars);
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        }
        return Collections.emptyList();
    }
}
