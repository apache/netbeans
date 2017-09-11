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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.j2ee.jpa.model;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Basic;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Column;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Id;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Version;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class AttributeWrapper {
    private Object modelElement;
    private Element javaElement;
    private ExecutableElement accesor;
    private VariableElement instanceVariable;
    private ExecutableElement mutator;
    
    public AttributeWrapper(Object modelElement) {
        this.modelElement = modelElement;
    }
    
    public ExecutableElement getMutator() {
        return mutator;
    }

    public void setMutator(ExecutableElement mutator) {
        this.mutator = mutator;
    }
    
    public Object getModelElement(){
        return modelElement;
    }
    
    public String getName(){
        if (modelElement instanceof Basic){
            return ((Basic)modelElement).getName();
        }
        
        if (modelElement instanceof Id){
            return ((Id)modelElement).getName();
        }
        
        if (modelElement instanceof Version){
            return ((Version)modelElement).getName();
        }
        
        return null;
    }
    
    public Column getColumn(){
        if (modelElement instanceof Basic){
            return ((Basic)modelElement).getColumn();
        }
        
        if (modelElement instanceof Id){
            return ((Id)modelElement).getColumn();
        }
        
        if (modelElement instanceof Version){
            return ((Version)modelElement).getColumn();
        }
        
        return null;
    }
    
    public String getTemporal(){
        if (modelElement instanceof Basic){
            return ((Basic)modelElement).getTemporal();
        }
        
        if (modelElement instanceof Id){
            return ((Id)modelElement).getTemporal();
        }
        
        if (modelElement instanceof Version){
            return ((Version)modelElement).getTemporal();
        }
        
        return null;
    }
    
    public Element getJavaElement(){
        return javaElement;
    }
    
    public void setJavaElement(Element javaElement){
        this.javaElement = javaElement;
    }
    
    public ExecutableElement getAccesor(){
        return accesor;
    }
    
    public void setAccesor(ExecutableElement accesor){
        this.accesor = accesor;
    }
    
    public VariableElement getInstanceVariable(){
        return instanceVariable;
    }
    
    public void setInstanceVariable(VariableElement instanceVariable){
        this.instanceVariable = instanceVariable;
    }
    
    public boolean isFullyResolved(){
        return javaElement != null && instanceVariable != null && accesor != null;
    }
    
    public TypeMirror getType(){
        if (instanceVariable != null){
            return instanceVariable.asType();
        } else if (accesor != null){
            return accesor.getReturnType();
        }
        
        assert false : "getType() must not be called before AttrWrapper is resolved";
        
        return null;
    }
}
