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
