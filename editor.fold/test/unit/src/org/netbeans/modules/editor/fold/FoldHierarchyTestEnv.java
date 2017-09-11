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
package org.netbeans.modules.editor.fold;

import javax.swing.JEditorPane;
import javax.swing.text.AbstractDocument;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.fold.FoldManagerFactory;

/*
 * FoldHierarchyExecutionTest.java
 * JUnit based test
 *
 * Created on June 27, 2004, 1:03 AM
 */


/**
 *
 * @author mmetelka
 */
public class FoldHierarchyTestEnv {
    
    private JEditorPane pane;
    
    public FoldHierarchyTestEnv(FoldManagerFactory factory) {
        this(new FoldManagerFactory[] { factory });
    }

    public FoldHierarchyTestEnv(FoldManagerFactory... factories) {
        pane = new JEditorPane();
        assert (getMimeType() != null);
        pane.setDocument(new BaseDocument(false, "text/plain"));

        FoldManagerFactoryProvider.setForceCustomProvider(true);
        FoldManagerFactoryProvider provider = FoldManagerFactoryProvider.getDefault();
        assert (provider instanceof CustomProvider)
            : "setForceCustomProvider(true) did not ensure CustomProvider use"; // NOI18N

        CustomProvider customProvider = (CustomProvider)provider;
        customProvider.removeAllFactories(); // cleanup all registered factories
        customProvider.registerFactories(getMimeType(), factories);
    }

    public JEditorPane getPane() {
        return pane;
    }
    
    public AbstractDocument getDocument() {
        return (AbstractDocument)getPane().getDocument();
    }
    
    public String getMimeType() {
        return pane.getEditorKit().getContentType();
    }
    
    public FoldHierarchy getHierarchy() {
        FoldHierarchy hierarchy = FoldHierarchy.get(getPane());
        FoldHierarchyExecution.waitHierarchyInitialized(getPane());
        assert (hierarchy != null);
        return hierarchy;
    }
    
}
