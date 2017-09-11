/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
/*
 * Contributor(s): theanuradha@netbeans.org
 */
package org.netbeans.modules.maven.hints.errors;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.modules.maven.hints.ui.customizers.SearchDependencyCustomizer;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

/**
 *
 * @author Anuradha G
 */
public class SearchClassDependencyHint extends AbstractHint {

    public static final String OPTION_DIALOG = "maven_search_dialog";//NOI18N
    public static SearchClassDependencyHint hint;

    public SearchClassDependencyHint() {
        super(true, false, null);
        synchronized(SearchClassDependencyHint.class){
           hint=this;
        }
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(SearchClassDependencyHint.class, "LBL_Missing_Class_Dec");
    }

    @Override
    public Set<Kind> getTreeKinds() {
        return EnumSet.noneOf(Kind.class);
    }

    @Override
    public List<ErrorDescription> run(CompilationInfo compilationInfo, TreePath treePath) {
        return null;//should not be called

    }

    @Override
    public String getId() {
        return "MAVEN_SEARCH_HINT";//NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(SearchClassDependencyHint.class, "LBL_Missing_Class");
    }

    @Override
    public void cancel() {
    }

    @Override
    public JComponent getCustomizer(Preferences p) {
        return new SearchDependencyCustomizer(p);
    }

    public static  boolean isSearchDialog() {
        synchronized(SearchClassDependencyHint.class){
            if (hint == null) {
                 hint=new SearchClassDependencyHint();
            }
        }
        Preferences prefs = hint.getPreferences(null);
        assert prefs != null; //#240220
        if (prefs == null) {
            return true;
        }
        return prefs.getBoolean(OPTION_DIALOG, true);
    }

    public static boolean isHintEnabled() {
        synchronized(SearchClassDependencyHint.class){
            if (hint == null) {
                 hint=new SearchClassDependencyHint();
            }
        }
        return hint.isEnabled();
    }
}
