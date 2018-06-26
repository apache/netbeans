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
package org.netbeans.modules.refactoring.php.findusages;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * @author Martin Matula, Jan Becicka
 */
public class WhereUsedQueryUI implements RefactoringUI {
    private final WhereUsedQuery query;
    private final String name;
    private WhereUsedPanel panel;
    private final WhereUsedSupport element;
    private ElementKind kind;
    private AbstractRefactoring delegate;

    public WhereUsedQueryUI(WhereUsedSupport usage) {
        this.query = new WhereUsedQuery(Lookups.singleton(usage));
        this.element = usage;
        name = usage.getName();
        kind = usage.getElementKind();
    }

    @Override
    public boolean isQuery() {
        return true;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new WhereUsedPanel(name, element, parent);
        }
        return panel;
    }

    @Override
    public org.netbeans.modules.refactoring.api.Problem setParameters() {
        query.putValue(WhereUsedQuery.SEARCH_IN_COMMENTS, panel.isSearchInComments());
        ModelElement element1 = panel.getElement();
        this.element.setModelElement(element1);
        if (kind == ElementKind.METHOD) {
            setForMethod();
            return query.checkParameters();
        } else if (kind == ElementKind.MODULE || kind == ElementKind.CLASS || kind == ElementKind.INTERFACE) {
            setForClass();
            return query.checkParameters();
        } else {
            return null;
        }
    }

    private void setForMethod() {
        if (panel.isMethodFromBaseClass()) {
            query.setRefactoringSource(Lookups.singleton(panel.getBaseMethod()));
        } else {
            query.setRefactoringSource(Lookups.singleton(element));
        }
        query.putValue(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS, panel.isMethodOverriders());
        query.putValue(WhereUsedQuery.FIND_REFERENCES, panel.isMethodFindUsages());
    }

    private void setForClass() {
        query.putValue(WhereUsedQueryConstants.FIND_SUBCLASSES, panel.isClassSubTypes());
        query.putValue(WhereUsedQueryConstants.FIND_DIRECT_SUBCLASSES, panel.isClassSubTypesDirectOnly());
        query.putValue(WhereUsedQueryConstants.FIND_SUBCLASSES, panel.isClassSubTypes());
        query.putValue(WhereUsedQuery.FIND_REFERENCES, panel.isClassFindUsages());
    }

    @Override
    public org.netbeans.modules.refactoring.api.Problem checkParameters() {
        if (kind == ElementKind.METHOD) {
            setForMethod();
            return query.fastCheckParameters();
        } else if (kind == ElementKind.CLASS || kind == ElementKind.MODULE) {
            setForClass();
            return query.fastCheckParameters();
        } else {
            return null;
        }
    }

    @Override
    public org.netbeans.modules.refactoring.api.AbstractRefactoring getRefactoring() {
        return query != null ? query : delegate;
    }

    @Override
    public String getDescription() {
        String nameText = name; //NOI18N
        String bundleKey = (panel != null && panel.isClassSubTypesDirectOnly())
                ? "DSC_WhereUsedFindDirectSubTypes" : "DSC_WhereUsed"; //NOI18N
        return getString(bundleKey, nameText);
    }
    private ResourceBundle bundle;

    private String getString(String key) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(WhereUsedQueryUI.class);
        }
        return bundle.getString(key);
    }

    private String getString(String key, String value) {
        return new MessageFormat(getString(key)).format(new Object[]{value});
    }

    @Override
    public String getName() {
        return new MessageFormat(NbBundle.getMessage(WhereUsedPanel.class, "LBL_WhereUsed")).format(
                new Object[]{name});
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.refactoring.php.findusages.WhereUsedQueryUI");
    }
}
