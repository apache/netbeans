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
package org.netbeans.modules.web.beans.navigation;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
class MethodTreeNode extends InjectableTreeNode<ExecutableElement> {

    private static final long serialVersionUID = -137177182006814794L;

    MethodTreeNode( FileObject fileObject, ExecutableElement element,
            DeclaredType parentType, boolean disabled , 
            CompilationInfo compilationInfo )
    {
        super(fileObject, element, parentType , disabled, compilationInfo);
    }

    boolean isOverridden( int index,
            List<ExecutableElement> overriddenMethods, 
            CompilationController controller )
    {
        ExecutableElement exec = getElementHandle().resolve(controller);
        if ( exec == null ){
            return true;
        }
        int execIndex = overriddenMethods.indexOf( exec );
        if ( execIndex == -1){
            return true;
        }
        return index < execIndex ;
    }

    boolean overridesMethod( ExecutableElement element,
            CompilationController controller )
    {
        ExecutableElement exec = getElementHandle().resolve(controller);
        if ( exec == null ){
            return false;
        }
        ExecutableElement overriddenMethod = exec;
        while ( true ){
            overriddenMethod = 
                controller.getElementUtilities().getOverriddenMethod(overriddenMethod);
            if ( overriddenMethod == null ){
                break;
            }
            if ( overriddenMethod.equals( element )){
                return true;
            }
        }
        return false;
    }

}
