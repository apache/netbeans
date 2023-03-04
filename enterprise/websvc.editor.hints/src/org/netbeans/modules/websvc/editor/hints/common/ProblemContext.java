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

package org.netbeans.modules.websvc.editor.hints.common;

import com.sun.source.tree.Tree;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Encapsulate often reused and sometimes expensive to calculate
 * properties of the class being examined
 * This class is not thread safe and 
 * one instance should not be passed among multiple threads.
 * @author Ajit.Bhate@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 */
public class ProblemContext implements Lookup.Provider {
    private FileObject fileObject;
    private CompilationInfo info;
    private boolean cancelled = false;
    private Tree elementToAnnotate;
    private TypeElement javaClass;
    private AbstractLookup lookup;
    private InstanceContent ic;
    
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
    
    public TypeElement getJavaClass(){
        return javaClass;
    }
    
    public void setJavaClass(TypeElement element){
        this.javaClass = element;
    }

    public synchronized Lookup getLookup() {
        if(lookup == null) {
            if (ic == null) ic = new InstanceContent();
            lookup = new AbstractLookup(ic);
        }
        return lookup;
    }
    
    public synchronized void addUserObject(Object info) {
        if (ic == null) ic = new InstanceContent();
        ic.add(info);
    }

    public synchronized void removeUserObject(Object info) {
        if (ic == null) return;
        ic.remove(info);
    }

}
