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
package org.netbeans.modules.web.beans.impl.model;

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
