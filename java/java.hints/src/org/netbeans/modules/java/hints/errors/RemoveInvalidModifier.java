/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

        EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
        modifiers.addAll(modss);

        Fix removeModifiersFix = FixFactory.removeModifiersFix(compilationInfo, modifierTreePath, modifiers, NbBundle.getMessage(RemoveInvalidModifier.class, "FIX_RemoveInvalidModifier", invalidMod, modss.size()));
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
