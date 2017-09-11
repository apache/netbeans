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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.java.hints.spi.ErrorRule.Data;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.support.FixFactory;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "DN_RemoveInvalidModifier=Remove invalid modifier",
    "DESC_RemoveInvalidModifier=Remove invalid modifier",
    "# {0} - modifier like private, public, protected",
    "# {1} - number of invalid modifiers",
    "FIX_RemoveInvalidModifier=Remove invalid ''{0}'' {1,choice,0#modifiers|1#modifier|1<modifiers} "
})
public class RemoveInvalidModifier implements ErrorRule<Void> {
    public static final String ERROR_PATTERN = "modifier (.+?) not allowed here";

    private static final Set<String> CODES = new HashSet<>(Arrays.asList(
            "compiler.err.mod.not.allowed.here"
    ));
    
    @Override
    public Set<String> getCodes() {
        return CODES;
    }

    @Override
    public List<Fix> run(CompilationInfo compilationInfo, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        EnumSet<Kind> supportedKinds = EnumSet.of(Kind.ANNOTATION_TYPE, Kind.CLASS, Kind.ENUM, Kind.VARIABLE, Kind.INTERFACE, Kind.METHOD);
        boolean isSupported = (supportedKinds.contains(treePath.getLeaf().getKind()));
	if (!isSupported) {
	    return null;
	}
        String invalidMod = getInvalidModifier(compilationInfo, treePath, CODES);
        if (null==invalidMod)
        {
            return null;
        }

        //support multiple invalid modifiers
        Collection<Modifier> modss=convertToModifiers(invalidMod.split(","));
        TreePath modifierTreePath = TreePath.getPath(treePath, getModifierTree(treePath));
        Fix removeModifiersFix = FixFactory.removeModifiersFix(compilationInfo, modifierTreePath, new HashSet<>(modss), NbBundle.getMessage(RemoveInvalidModifier.class, "FIX_RemoveInvalidModifier", invalidMod, modss.size()));
        return Arrays.asList(removeModifiersFix);
    }
    

    @Override
    public void cancel() {
    }

    @Override
    public String getId() {
        return RemoveInvalidModifier.class.getName();
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(RemoveInvalidModifier.class, "DN_RemoveInvalidModifier");
    }

    public String getDescription() {
        return NbBundle.getMessage(RemoveInvalidModifier.class, "DESC_RemoveInvalidModifier");
    }

    /**
     * Returns the diagnostics entry
     * @param compilationInfo
     * @param start
     * @param codes
     * @return 
     */
    private Diagnostic getDiagnostic(CompilationInfo compilationInfo, long start, Set<String> errorCodes) {
        Diagnostic result = null;
        for (Diagnostic d : compilationInfo.getDiagnostics()) {
            if (start != d.getStartPosition()) {
                continue;
            }
            if (!errorCodes.contains(d.getCode())) {
                continue;
            }
            result=d;
        }
        return result;
    }

    private String getInvalidModifier(CompilationInfo compilationInfo, TreePath treePath, Set<String> codes) {
        long start = compilationInfo.getTrees().getSourcePositions().getStartPosition(compilationInfo.getCompilationUnit(), treePath.getLeaf());
        Diagnostic diagnostic = getDiagnostic(compilationInfo, start, codes);
        if (null==diagnostic){
            return null;
        }
        //parse the error message
        //HACK: hope this will be stable in the long term
        String message = diagnostic.getMessage(Locale.ENGLISH);
        Matcher matcher = Pattern.compile(ERROR_PATTERN).matcher(message);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }

    private Collection<Modifier> convertToModifiers(String... mods) {
        final Map<String, Modifier> map = new HashMap<>();
        for (Modifier modifier : Modifier.values()) {
            map.put(modifier.toString(), modifier);
        }

        //convert to modifier
        List<Modifier> result = new ArrayList<>();
        for (String string : mods) {
            result.add(map.get(string));
        }
        return result;
    }

    private ModifiersTree getModifierTree(TreePath treePath) {
        Kind kind = treePath.getLeaf().getKind();
        switch (kind) {
            case ANNOTATION_TYPE:
            case CLASS:
            case ENUM:
            case INTERFACE:
                return ((ClassTree) treePath.getLeaf()).getModifiers();
            case VARIABLE:
                return ((VariableTree) treePath.getLeaf()).getModifiers();
            case METHOD:
                return ((MethodTree) treePath.getLeaf()).getModifiers();
            default:
                throw new UnsupportedOperationException("kind " + kind + " not yet suppported");
        }
    }
}
