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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.websvc.design.javamodel;

import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkuchtiak
 */
public class ParamModel {
    
    String name;
    String javaName;
    private FileObject implementationClass;
    private String partName;
    private String targetNamespace;
    private WebParam.Mode mode = WebParam.Mode.IN;
    private String paramType;
    private ElementHandle methodHandle;
    
    /** Creates a new instance of MethodModel */
    ParamModel() {
    }
    
    /** Creates a new instance of MethodModel */
    ParamModel(String name, String javaName) {
        this.name=name;
        this.javaName=javaName;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        if (!this.name.equals(name)) {
            JaxWsUtils.setWebParamAttrValue(implementationClass, methodHandle, javaName, "name", name);
            this.name=(name==null?javaName:name);
        }
    }
    
    public FileObject getImplementationClass(){
        return implementationClass;
    }
    
    void setImplementationClass(FileObject impl){
        implementationClass = impl;
    }

    public ElementHandle getMethodHandle() {
        return methodHandle;
    }
    
    void setMethodHandle(ElementHandle methodHandle) {
        this.methodHandle=methodHandle;
    }
    
    public String getTargetNamespace() {
        return targetNamespace;
    }
    
    void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    public Mode getMode() {
        return mode;
    }

    void setMode(Mode mode) {
        this.mode = mode;
    }
    
    public String getParamType() {
        return paramType;
    }

    void setParamType(String paramType) {
        this.paramType = paramType;
    }
  
    public String getPartName() {
        return partName;
    }

    void setPartName(String partName) {
        this.partName = partName;
    }
        
    public boolean isEqualTo(ParamModel model) {
        if (!name.equals(model.name)) return false;
        if (!paramType.equals(model.paramType)) return false;
        if (!mode.equals(model.mode)) return false;
        if (!Utils.isEqualTo(targetNamespace, model.targetNamespace)) return false;
        if (!Utils.isEqualTo(partName, model.partName)) return false;
        return true;
    }
}
