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
package org.netbeans.modules.web.beans.navigation;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.JEditorPane;
import javax.swing.JLabel;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class DecoratorsPanel extends BindingsPanel {

    private static final long serialVersionUID = -5097678699872215262L;
    private static final String DELEGATE = "javax.decorator.Delegate";  // NOI18N

    public DecoratorsPanel( Object[] subject, MetadataModel<WebBeansModel> metaModel,
            WebBeansModel model, JavaHierarchyModel treeModel )
    {
        super(subject, metaModel, model, treeModel);
        // use Scope components for showing type of Delegate injectoin point
        setVisibleScope( true );
        initUI();
    }
    
    @Override
    protected void setScope( WebBeansModel model, Element element ){
        TypeElement clazz = (TypeElement)element;
        List<VariableElement> fields = ElementFilter.fieldsIn(
                model.getCompilationController().getElements().getAllMembers( clazz));
        VariableElement delegate = null;
        for (VariableElement field : fields) {
            if( hasDelegate(field,  model.getCompilationController())){
                delegate = field;
                break;
            }
        }
        TypeMirror delegateType = delegate.asType();
        StringBuilder shortName = new StringBuilder();
        StringBuilder fqnName = new StringBuilder();
        fillElementType(delegateType, shortName, fqnName, model.getCompilationController());
        JEditorPane scopeComponent = getScopeComponent();
        if ( showFqns() ){
            scopeComponent.setText( fqnName.toString() );
        }
        else {
            scopeComponent.setText( shortName.toString() );
        }
    }
    
    private boolean hasDelegate( Element element , CompilationController controller){
        List<? extends AnnotationMirror> allAnnotationMirrors = controller.
            getElements().getAllAnnotationMirrors( element);
        TypeElement delegate = controller.getElements().getTypeElement( DELEGATE);
        if( delegate == null ){
            return false;
        }
        for (AnnotationMirror annotationMirror : allAnnotationMirrors) {
            Element annotation = controller.getTypes().asElement( 
                    annotationMirror.getAnnotationType());
            if ( annotation!= null && annotation.equals( delegate )){
                return true;
            }
        }
        return false;
    }

    private void initUI() {
        JLabel elementLabel = getSubjectElementLabel();
        Mnemonics.setLocalizedText(elementLabel,NbBundle.getMessage( 
                ObserversPanel.class, "LBL_DecoratedElement") );       // NOI18N
        elementLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSN_DecoratedElement"));       // NOI18N
        elementLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSD_DecoratedElement"));       // NOI18N
        
        JLabel qualifiers= getSubjectBindingsLabel();
        Mnemonics.setLocalizedText(qualifiers,NbBundle.getMessage( 
                ObserversPanel.class, "LBL_DecoratorQualifiers") );  // NOI18N
        qualifiers.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSN_DecoratorQualifiers"));  // NOI18N
        qualifiers.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSD_DecoratorQualifiers"));  // NOI18N
        
        JLabel selectedQualifiers = getSelectedBindingsLabel();
        Mnemonics.setLocalizedText(selectedQualifiers,NbBundle.getMessage( 
                ObserversPanel.class, "LBL_SelectedDecoratorQualifiers") );  // NOI18N
        selectedQualifiers.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSN_SelectedDecoratorQualifiers"));  // NOI18N
        selectedQualifiers.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSD_SelectedDecoratorQualifiers"));  // NOI18N
        
        JLabel scopeLabel = getScopeLabel();
        Mnemonics.setLocalizedText(scopeLabel,NbBundle.getMessage( 
                ObserversPanel.class, "LBL_SelectedDelegateType") );  // NOI18N
        scopeLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSN_SelectedDelegateType"));  // NOI18N
        scopeLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSD_SelectedDelegateType"));  // NOI18N
        
    }

}
