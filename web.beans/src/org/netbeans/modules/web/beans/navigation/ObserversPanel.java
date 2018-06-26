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

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
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
public class ObserversPanel extends BindingsPanel {

    private static final long serialVersionUID = -5038408349629504998L;
    
    static final String OBSERVES_ANNOTATION = 
        "javax.enterprise.event.Observes";                      // NOI18N


    public ObserversPanel( Object[] subject, 
            MetadataModel<WebBeansModel> metaModel , WebBeansModel model , 
            ObserversModel uiModel )
    {
        super(subject, metaModel, model , uiModel );
        initLabels();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.InjectablesPanel#setInjectableType(javax.lang.model.type.TypeMirror, org.netbeans.api.java.source.CompilationController)
     */
    @Override
    protected void setContextElement( Element context,
            CompilationController controller )
    {
        TypeMirror typeMirror = context.asType();
        TypeMirror parameterType = ((DeclaredType)typeMirror).getTypeArguments().get( 0 );
        super.setContextType(parameterType, controller);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.InjectablesPanel#getQualifiedElement(javax.lang.model.element.Element, org.netbeans.modules.web.beans.api.model.WebBeansModel)
     */
    @Override
    protected Element getSelectedQualifiedElement( Element context , 
            WebBeansModel model) 
    {
        if ( context.getKind() == ElementKind.METHOD){
            return model.getObserverParameter((ExecutableElement)context );
        }
        return context;
    }

    private void initLabels() {
        JLabel typeLabel = getSubjectElementLabel();
        Mnemonics.setLocalizedText(typeLabel,NbBundle.getMessage( 
                ObserversPanel.class, "LBL_EventType") );       // NOI18N
        typeLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSN_EventType"));       // NOI18N
        typeLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSD_EventType"));       // NOI18N
        
        JLabel qualifiersLabel= getSubjectBindingsLabel();
        Mnemonics.setLocalizedText(qualifiersLabel,NbBundle.getMessage( 
                ObserversPanel.class, "LBL_EventQualifiers") );       // NOI18N
        qualifiersLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSN_EventQualifiers"));       // NOI18N
        qualifiersLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSD_EventQualifiers"));       // NOI18N
    }
    
}
