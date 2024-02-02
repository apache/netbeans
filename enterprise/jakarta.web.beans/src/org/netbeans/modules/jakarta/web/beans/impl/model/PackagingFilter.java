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
package org.netbeans.modules.jakarta.web.beans.impl.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.SourceUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;



/**
 * @author ads
 *
 */
class PackagingFilter {
    
    PackagingFilter(WebBeansModelImplementation model){
        myModel = model;
    }
    
    void filter(Collection<? extends Element> collection, AtomicBoolean cancel ){
        for (Iterator<? extends Element> iterator = collection.iterator(); 
            iterator.hasNext(); ) 
        {
            if(cancel.get()) {
                break;
            }
            Element element = iterator.next();
            if ( remove(element, cancel)){
                iterator.remove();
            }
        }
    }
    
    void filterTypes(Collection<? extends DeclaredType> collection, AtomicBoolean cancel ){
        for (Iterator<? extends DeclaredType> iterator = collection.iterator(); 
            iterator.hasNext(); ) 
        {
            DeclaredType type = iterator.next();
            Element element = getModel().getHelper().getCompilationController().
                getTypes().asElement( type );
            if ( element != null && remove(element, cancel)){
                iterator.remove();
            }
        }
    }
    
    private boolean remove( Element element, AtomicBoolean cancel  ){
        TypeElement typeElement;
        if ( element instanceof TypeElement ){
            typeElement = (TypeElement) element;
        }
        else {
            typeElement = getModel().getHelper().getCompilationController().
                getElementUtilities().enclosingTypeElement(element);
        }
        if ( typeElement == null || cancel.get()){
            return false;
        }
        
        FileObject file = SourceUtils.getFile(ElementHandle.create(typeElement), 
                ClasspathInfo.create(getModel().getModelUnit().getBootPath() , 
                        ClassPath.EMPTY, getModel().getModelUnit().getSourcePath()));
        
        if ( file != null || cancel.get()){
            return false;
        }
        
        PackageElement pack = getModel().getHelper().getCompilationController().
            getElements().getPackageOf( typeElement );
        if ( pack == null || cancel.get()){
            return false;
        }
        String packageName = pack.getQualifiedName().toString();
        String fqn = ElementUtilities.getBinaryName(typeElement);
        String className = fqn.substring(packageName.length());
        if ( className.length() >0 && className.charAt(0)=='.' ){
            className = className.substring(1);
        }
        else {
            return false;
        }
        int dotIndex = className.indexOf('.');
        if ( dotIndex != -1 ){
            className = className.substring( 0, dotIndex );
        }
        if ( className == null || cancel.get()){
            return false;
        }
        
        String path = packageName.replace('.', '/')+'/'+className+".class"; // NOI18N
        ClassPath classPath = getModel().getModelUnit().getCompilePath();
        FileObject resource = classPath.findResource( path );
        if ( resource != null && !cancel.get()){
            FileObject root = classPath.findOwnerRoot( resource );
            if ( root == null || cancel.get()){
                return false;
            }
            if ( FileUtil.isArchiveFile( root ) && !cancel.get()){
                FileObject archiveFile = FileUtil.getArchiveFile(root);
                String ext = archiveFile.getExt();
                if ( "war".equalsIgnoreCase( ext)){                        // NOI18N
                    return root.getFileObject("WEB-INF/beans.xml") == null; // NOI18N
                }
            }
            return !hasMetaBeans(root);
        }
        return false;
    }
    
    private boolean hasMetaBeans(FileObject root ){
        return root.getFileObject("META-INF/beans.xml") != null;            // NOI18N
    }
    
    private WebBeansModelImplementation getModel(){
        return myModel;
    }

    private WebBeansModelImplementation myModel;
}
