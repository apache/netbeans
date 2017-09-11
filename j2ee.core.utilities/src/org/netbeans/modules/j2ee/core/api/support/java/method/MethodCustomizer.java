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
 *
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

package org.netbeans.modules.j2ee.core.api.support.java.method;

import java.util.Collection;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.core.support.java.method.MethodCustomizerPanel;
import org.netbeans.modules.j2ee.core.support.java.method.ValidatingPropertyChangeListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author Martin Adamek
 */
public final class MethodCustomizer {
    
    private final MethodCustomizerPanel panel;
    private final String title;
    private final String prefix;
    private final Collection<MethodModel> existingMethods;
    
    // factory should be used to create instances
    protected MethodCustomizer(String title, MethodModel methodModel, ClasspathInfo cpInfo, boolean hasLocal, boolean hasRemote,
            boolean selectLocal, boolean selectRemote, boolean hasReturnType, String  ejbql, boolean hasFinderCardinality,
            boolean hasExceptions, boolean hasInterfaces, String prefix, Collection<MethodModel> existingMethods) {
        this(title, methodModel, cpInfo, hasLocal, hasRemote, selectLocal, selectRemote, hasReturnType, ejbql, hasFinderCardinality,
            hasExceptions, hasInterfaces, false, prefix, existingMethods);
    }

    protected MethodCustomizer(String title, MethodModel methodModel, ClasspathInfo cpInfo, boolean hasLocal, boolean hasRemote, 
            boolean selectLocal, boolean selectRemote, boolean hasReturnType, String  ejbql, boolean hasFinderCardinality, 
            boolean hasExceptions, boolean hasInterfaces, boolean allowsNoInterface, String prefix, Collection<MethodModel> existingMethods) {
        this.panel = MethodCustomizerPanel.create(methodModel, cpInfo, hasLocal, hasRemote, selectLocal, selectRemote,
                hasReturnType, ejbql, hasFinderCardinality, hasExceptions, hasInterfaces, allowsNoInterface);
        
        // A11Y 
        this.panel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "ACSD_AddMethod")); // NOI18N
               
        this.title = title;
        this.prefix = prefix;
        this.existingMethods = existingMethods;
    }
    
    /**
     * Opens modal window for method customization.
     * 
     * @return true if OK button on customizer was pressed and changes should be written to source files,
     * false if Cancel button on customizer was pressed or if customizer was cancelled in any other way and
     * nothing should be written to source files.
     */
    public boolean customizeMethod() {
        DialogDescriptor notifyDescriptor = new DialogDescriptor(
                panel, title, true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                null
                );
        panel.addPropertyChangeListener(new ValidatingPropertyChangeListener(panel, notifyDescriptor, existingMethods, prefix));
        return DialogDisplayer.getDefault().notify(notifyDescriptor) == NotifyDescriptor.OK_OPTION;
    }
    
    public MethodModel getMethodModel() {
        return MethodModel.create(
                panel.getMethodName(),
                panel.getReturnType(),
                panel.getMethodBody(),
                panel.getParameters(),
                panel.getExceptions(),
                panel.getModifiers()
                );
    }
    
    public boolean finderReturnIsSingle() {
        return panel.finderReturnIsSingle();
    }
    
    public boolean publishToLocal() {
        return panel.hasLocal();
    }
    
    public boolean publishToRemote() {
        return panel.hasRemote();
    }
    
    public String getEjbQL() {
        return panel.getEjbql();
        
    }
    
}
