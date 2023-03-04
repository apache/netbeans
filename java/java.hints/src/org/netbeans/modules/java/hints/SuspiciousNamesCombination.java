/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.swing.JComponent;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Lahoda
 */
public class SuspiciousNamesCombination extends AbstractHint {
    static final String GROUP_SEPARATOR = "|";
    static final String SEPARATORS_REGEX = "[, \t;]";
    static final String GROUP_KEY = "groups";
    static final String DEFAULT_GROUPS = "x, width|y, height";
    
    /** Creates a new instance of SuspiciousNamesCombination */
    public SuspiciousNamesCombination() {
        super( false, true, AbstractHint.HintSeverity.WARNING, "SuspiciousNameCombination");
    }
    
    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.METHOD_INVOCATION, Kind.ASSIGNMENT, Kind.VARIABLE);
    }

    public List<ErrorDescription> run(CompilationInfo info, TreePath treePath) {
        switch (treePath.getLeaf().getKind()) {
            case METHOD_INVOCATION:
                return handleMethodInvocation(info, treePath);
            case ASSIGNMENT:
                return handleAssignment(info, treePath);
            case VARIABLE:
                return handleVariable(info, treePath);
            default:
                return null;
        }
    }

    public void cancel() {
        // XXX implement me
    }
    
    private static final String KEY = SuspiciousNamesCombination.class.getName();
    
    private Map<String, Integer> ensureNameMapLoaded(CompilationInfo info) {
        Map<String, Integer> mapNameToGroup = (Map<String, Integer>)info.getCachedValue(KEY);
        if (mapNameToGroup != null) {
            return mapNameToGroup;
        }
        mapNameToGroup = new HashMap<String, Integer>();
        Preferences prefs = getPreferences(null);
        String value = prefs.get(GROUP_KEY, DEFAULT_GROUPS);
        if (value == null) {
            info.putCachedValue(KEY, mapNameToGroup, CompilationInfo.CacheClearPolicy.ON_TASK_END);
            return mapNameToGroup;
        }
        String[] groups = value.split(Pattern.quote(GROUP_SEPARATOR));
        int idx = 0;
        for (String g : groups) {
            String[] names = g.split(SEPARATORS_REGEX);
            for (String n : names) {
                if (n.isEmpty()) {
                    continue;
                }
                mapNameToGroup.put(n.toLowerCase(), idx);
            }
            idx++;
        }
        info.putCachedValue(KEY, mapNameToGroup, CompilationInfo.CacheClearPolicy.ON_TASK_END);
        return mapNameToGroup;
    }

    @Override
    public JComponent getCustomizer(Preferences node) {
        return new SuspiciousNamesCustomizer(node);
    }
    
    
    
    private List<ErrorDescription> handleMethodInvocation(CompilationInfo info, TreePath treePath) {
        Element el = info.getTrees().getElement(treePath);
        
        if (el == null || (el.getKind() != ElementKind.CONSTRUCTOR && el.getKind() != ElementKind.METHOD)) {
            return null;
        }
        
        MethodInvocationTree mit = (MethodInvocationTree) treePath.getLeaf();
        ExecutableElement    ee  = (ExecutableElement) el;
        
        if (ee.getParameters().size() != mit.getArguments().size()) {
            //should not happen?
            return null;
        }
        
        List<ErrorDescription> result = new ArrayList<ErrorDescription>();
        
        for (int cntr = 0; cntr < ee.getParameters().size(); cntr++) {
            String         declarationName = ee.getParameters().get(cntr).getSimpleName().toString();
            ExpressionTree arg             = mit.getArguments().get(cntr);
            String         actualName      = getName(arg);
            
            if (isConflicting(info, declarationName, actualName)) {
                long start = info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), arg);
                long end   = info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), arg);
                
                if (start != (-1) && end != (-1)) {
                    result.add(ErrorDescriptionFactory.createErrorDescription(getSeverity().toEditorSeverity(), "Suspicious names combination", info.getFileObject(), (int) start, (int) end));
                }
            }
        }
        
        return result;
    }
    
    private List<ErrorDescription> handleAssignment(CompilationInfo info, TreePath treePath) {
        AssignmentTree at = (AssignmentTree) treePath.getLeaf();
        
        String declarationName = getName(at.getVariable());
        String actualName      = getName(at.getExpression());
        
        if (isConflicting(info, declarationName, actualName)) {
            long start = info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), at.getVariable());
            long end   = info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), at.getVariable());
            
            if (start != (-1) && end != (-1)) {
                return Collections.singletonList(ErrorDescriptionFactory.createErrorDescription(getSeverity().toEditorSeverity(), "Suspicious names combination", info.getFileObject(), (int) start, (int) end));
            }
        }
        
        return null;
    }
    
    private List<ErrorDescription> handleVariable(CompilationInfo info, TreePath treePath) {
        VariableTree vt = (VariableTree) treePath.getLeaf();
        
        if (vt.getName() == null)
            return null;
        
        String declarationName = vt.getName().toString();
        String actualName      = getName(vt.getInitializer());
        
        if (isConflicting(info, declarationName, actualName)) {
            int[] span = info.getTreeUtilities().findNameSpan(vt);

            if (span != null) {
                String description = NbBundle.getMessage(SuspiciousNamesCombination.class, "HINT_SuspiciousNamesCombination");

                return Collections.singletonList(ErrorDescriptionFactory.createErrorDescription(getSeverity().toEditorSeverity(), description, info.getFileObject(), span[0], span[1]));
            }
        }
        
        return null;
    }
    
    static String getName(ExpressionTree et) {
        if (et == null)
            return null;
        
        switch (et.getKind()) {
            case IDENTIFIER:
                return ((IdentifierTree) et).getName().toString();
            case METHOD_INVOCATION:
                return getName(((MethodInvocationTree) et).getMethodSelect());
            case MEMBER_SELECT:
                return ((MemberSelectTree) et).getIdentifier().toString();
            default:
                return null;
        }
    }
    
    private boolean isConflicting(CompilationInfo info, String declarationName, String actualName) {
        if (declarationName == null || actualName == null)
            return false;
        
        int declarationCat = findCategory(info, declarationName);
        int actualCat      = findCategory(info, actualName);
        
        return declarationCat != actualCat && declarationCat != (-1) && actualCat != (-1);
    }
    
    private int findCategory(CompilationInfo info, String name) {
        Set<String> broken = breakName(name);
        Map<String, Integer> mapNameToGroup = ensureNameMapLoaded(info);
        for (String s : broken) {
            Integer i = mapNameToGroup.get(s);
            if (i != null) {
                return i;
            }
        }
        return -1;
    }
    
    static Set<String> breakName(String name) {
        Set<String> result = new HashSet<String>();
        int wordStartOffset = 0;
        int index = 0;
        
        while (index < name.length()) {
            if (Character.isUpperCase(name.charAt(index))) {
                //starting new word:
                if (wordStartOffset < index) {
                    result.add(name.substring(wordStartOffset, index).toLowerCase());
                }
                wordStartOffset = index;
            }
            
            if (name.charAt(index) == '-') {
                //starting new word:
                if (wordStartOffset < index) {
                    result.add(name.substring(wordStartOffset, index).toLowerCase());
                }
                wordStartOffset = index + 1;
            }
            
            index++;
        }
        
        if (wordStartOffset < index) {
            result.add(name.substring(wordStartOffset, index).toLowerCase());
        }
        
        return result;
    }
    
    public String getId() {
        return SuspiciousNamesCombination.class.getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(SuspiciousNamesCombination.class, "DN_SuspiciousNamesCombination");
    }

    public String getDescription() {
        return NbBundle.getMessage(SuspiciousNamesCombination.class, "DESC_SuspiciousNamesCombination");
    }
    
}
