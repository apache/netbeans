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

package org.netbeans.modules.j2ee.jpa.verification.common;

import com.sun.source.tree.Tree;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.openide.filesystems.FileObject;

/**
 * Encapsulate often reused and sometimes expensive to calculate
 * properties of the class being examined
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class ProblemContext {
    private FileObject fileObject;
    private CompilationInfo info;
    private boolean cancelled = false;
    private Tree elementToAnnotate;
    private Object modelElement;
    private TypeElement javaClass;
    
    public FileObject getFileObject(){
        return fileObject;
    }
    
    public void setFileObject(FileObject fileObject){
        this.fileObject = fileObject;
    }
    
    public CompilationInfo getCompilationInfo(){
        return info;
    }
    
    public void setCompilationInfo(CompilationInfo info){
        this.info = info;
    }
    
    public void setCancelled(boolean cancelled){
        this.cancelled = cancelled;
    }
    
    /**
     * @return true if the problem finding task was cancelled
     */
    public boolean isCancelled(){
        return cancelled;
    }
    
    public Tree getElementToAnnotate(){
        return elementToAnnotate;
    }
    
    public void setElementToAnnotate(Tree elementToAnnotate){
        this.elementToAnnotate = elementToAnnotate;
    }
    
    public void setElementToAnnotateOrNullIfExists(Tree elementToAnnotate){
        if (getElementToAnnotate() == null){
            setElementToAnnotate(elementToAnnotate);
        }
        else{
            setElementToAnnotate(null);
        }
    }
    
    public Object getModelElement(){
        return modelElement;
    }
    
    public void setModelElement(Object modelElement){
        this.modelElement = modelElement;
    }
    
    public TypeElement getJavaClass(){
        return javaClass;
    }
    
    public void setJavaClass(TypeElement element){
        this.javaClass = element;
    }
}
