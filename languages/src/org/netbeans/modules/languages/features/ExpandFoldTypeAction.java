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

package org.netbeans.modules.languages.features;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.languages.ParseException;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;


/**
 *
 * @author Jan Jancura
 */
public class ExpandFoldTypeAction extends BaseAction {

    public ExpandFoldTypeAction (String name) {
        super (name);
        //putValue(SHORT_DESCRIPTION, NbBundle.getBundle(JavaKit.class).getString("expand-all-code-block-folds"));
        //putValue(BaseAction.POPUP_MENU_TEXT, NbBundle.getBundle(JavaKit.class).getString("popup-expand-all-code-block-folds"));
    }

    public void actionPerformed (ActionEvent evt, JTextComponent target) {
        FoldHierarchy hierarchy = FoldHierarchy.get (target);
        // Hierarchy locking done in the utility method
        try {
            String mimeType = (java.lang.String) target.getDocument ().getProperty ("mimeType");
            Language l = LanguagesManager.getDefault ().getLanguage (mimeType);
            if (expand (hierarchy, l)) return;
            Iterator<Language> it = l.getImportedLanguages ().iterator ();
            while (it.hasNext ())
                if (expand (hierarchy, it.next ()))
                    return;
        } catch (ParseException ex) {
        }
    }

    private boolean expand (FoldHierarchy hierarchy, Language language) {
        List<Feature> folds = language.getFeatureList ().getFeatures (LanguagesFoldManager.FOLD);
        Iterator<Feature> it = folds.iterator ();
        while (it.hasNext ()) {
            Feature fold = it.next ();
            String expand = LocalizationSupport.localize (language, (String) fold.getValue ("expand_type_action_name"));
            if (expand == null) continue;
            if (!expand.equals (getValue (NAME)))
                continue;
            String collapse = LocalizationSupport.localize (language, (String) fold.getValue ("collapse_type_action_name"));
            if (collapse == null) continue;
            List<FoldType> types = new ArrayList<FoldType> ();
            types.add (Folds.getFoldType (collapse));
            FoldUtilities.expand (hierarchy, types);
            return true;
        }
        return false;
    }
}

