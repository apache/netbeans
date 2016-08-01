/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.jetbrains.kotlin.resolve.lang.java.structure;

import java.util.Collection;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationArgument;
import org.jetbrains.kotlin.name.Name;
import org.netbeans.api.project.Project;

public class NetBeansJavaAnnotationArgument<T extends Element> extends NetBeansJavaElement<T> 
        implements JavaAnnotationArgument {
    
    public NetBeansJavaAnnotationArgument(T javaElement){
        super(javaElement);
    }

    @Override
    public Name getName() {
        return Name.identifier(getBinding().getSimpleName().toString());
    }
    
    public static JavaAnnotationArgument create(Object value, Name name, Project project){
        
        if (value instanceof AnnotationMirror){
            return new NetBeansJavaAnnotationAsAnnotationArgument((AnnotationMirror) value, name);
        }
        else if (value instanceof VariableElement){
            return new NetBeansJavaReferenceAnnotationArgument((VariableElement) value);
        }
        else if (value instanceof String){
            return new NetBeansJavaLiteralAnnotationArgument(value, name);
        }
        else if (value instanceof Class<?>){
            return new NetBeansJavaClassObjectAnnotationArgument((Class) value, name, project);
        }
        else if (value instanceof Collection<?>){
            return new NetBeansJavaArrayAnnotationArgument((Collection) value, name, project);
        } else if (value instanceof AnnotationValue){
            return create(((AnnotationValue) value).getValue(), name, project);
        } else return null;
    }
    
}
