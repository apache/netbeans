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
package org.netbeans.modules.web.beans.navigation.actions;

import java.util.Collection;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.beans.api.model.BeansModel;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.navigation.DecoratorsModel;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
public final class DecoratoresActionStrategy implements ModelActionStrategy {

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.actions.ModelActionStrategy#isApplicable(org.netbeans.modules.web.beans.navigation.actions.ModelActionStrategy.InspectActionId)
     */
    @Override
    public boolean isApplicable( InspectActionId id ) {
        return id == InspectActionId.CLASS_CONTEXT;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.actions.ModelActionStrategy#isApplicable(org.netbeans.modules.web.beans.api.model.WebBeansModel, java.lang.Object[])
     */
    @Override
    public boolean isApplicable( WebBeansModel model, Object[] context ) {
        final Object handle = context[0];
        if ( handle == null ){
            return false;
        }
        Element element = ((ElementHandle<?>)handle).resolve( 
                model.getCompilationController());
        if ( element == null ){
            return false;
        }
        List<AnnotationMirror> qualifiers = model.getQualifiers(element,  true);
        // if class has qualifiers then Class context is considered as decorator context
        if ( qualifiers.size() >0 ){
            return true;
        }
        /*
         *  If it doesn't have explicit qualifiers then it could have implicit @Default
         *  qualifier . In the latter case check Interceptor Bindings presence 
         */
        return model.getInterceptorBindings(element).isEmpty();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.actions.ModelActionStrategy#invokeModelAction(org.netbeans.modules.web.beans.api.model.WebBeansModel, org.netbeans.modules.j2ee.metadata.model.api.MetadataModel, java.lang.Object[], javax.swing.text.JTextComponent, org.openide.filesystems.FileObject)
     */
    @Override
    public void invokeModelAction( WebBeansModel model,
            final MetadataModel<WebBeansModel> metaModel, final Object[] subject,
            JTextComponent component, FileObject fileObject )
    {
        final Object handle = subject[0];
        Element element = ((ElementHandle<?>)handle).resolve( 
                model.getCompilationController());
        if ( !( element instanceof TypeElement) ){
            return;
        }
        TypeElement type = (TypeElement)element;
        CompilationController controller = model.getCompilationController();
        Collection<TypeElement> decorators = model.getDecorators(type);
        BeansModel beansModel = model.getModelImplementation().getBeansModel();
        final DecoratorsModel uiModel = new DecoratorsModel(decorators, 
                beansModel, controller, metaModel);
        final String name = type.getSimpleName().toString();
        if (SwingUtilities.isEventDispatchThread()) {
            WebBeansActionHelper.showDecoratorsDialog( metaModel, model, 
                    subject , uiModel , name );
        }
        else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    WebBeansActionHelper.showDecoratorsDialog(metaModel, null , 
                            subject ,uiModel , name );
                }
            });
        }
    }
}
