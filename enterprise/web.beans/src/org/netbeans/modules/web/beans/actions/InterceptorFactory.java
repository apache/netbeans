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
package org.netbeans.modules.web.beans.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.text.JTextComponent;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.netbeans.spi.editor.codegen.CodeGenerator.Factory;
import org.openide.util.Lookup;

import com.sun.source.util.TreePath;


/**
 * @author ads
 *
 */
// @todo: Support JakartaEE
public class InterceptorFactory implements Factory {
    
    static final String INTERCEPTOR_BINDING = "InterceptorBinding";         // NOI18N
    
    private static final String INTERCEPTOR_BINDING_FQN = 
            "javax.interceptor." +INTERCEPTOR_BINDING;                      // NOI18N

    /* (non-Javadoc)
     * @see org.netbeans.spi.editor.codegen.CodeGenerator.Factory#create(org.openide.util.Lookup)
     */
    @Override
    public List<? extends CodeGenerator> create( Lookup lookup ) {
        CompilationController controller = lookup.lookup(CompilationController.class);
        JTextComponent component = lookup.lookup(JTextComponent.class);
        List<CodeGenerator> result = new ArrayList<CodeGenerator>(1);
        if (component != null && controller != null) {
            try {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                
                TypeElement interceptorBinding = controller.getElements().
                    getTypeElement( INTERCEPTOR_BINDING_FQN );
                if ( interceptorBinding == null ){
                    return result;
                }
                int dot = component.getCaret().getDot();
                TreePath tp = controller.getTreeUtilities().pathFor(dot);
                if ( tp == null ){
                    return result;
                }
                Element contextElement = controller.getTrees().getElement(tp );
                if ( contextElement == null || 
                        contextElement.getKind() != ElementKind.ANNOTATION_TYPE )
                {
                    return result;
                }
                
                List<? extends AnnotationMirror> annotations = controller.
                    getElements().getAllAnnotationMirrors( contextElement );
                boolean isInterceptorBinding = false;
                for (AnnotationMirror annotation : annotations) {
                    Element annotationElement = controller.getTypes().asElement( 
                            annotation.getAnnotationType());
                    if ( interceptorBinding.equals( annotationElement) ){
                        isInterceptorBinding = true;
                        break;
                    }
                }
                if ( isInterceptorBinding ){
                    result.add( new InterceptorGenerator( 
                            contextElement.getSimpleName().toString(), 
                            controller.getFileObject()) );
                }
            } catch (IOException ex) {
                Logger.getLogger( InterceptorFactory.class.getName()).log(
                        Level.INFO, null, ex );
            }
        }
        return result;
    }

}
