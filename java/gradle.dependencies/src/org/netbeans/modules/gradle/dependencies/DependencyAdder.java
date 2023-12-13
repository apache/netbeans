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
package org.netbeans.modules.gradle.dependencies;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lsp.TextEdit;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.java.queries.GradleScope;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.Dependency;
import org.netbeans.modules.project.dependency.DependencyChangeException;
import org.netbeans.modules.project.dependency.DependencyResult;
import org.netbeans.modules.project.dependency.SourceLocation;
import org.openide.util.*;

/**
 *
 * Handles adding dependencies to the project.
 * 
 * @author sdedic
 */
public class DependencyAdder extends AbstractWorker {
    /*
    private Map<Dependency, Dependency> upgrades = new HashMap<>();
    */

    public DependencyAdder(Project project, RewriteContext context) {
        super(project, context);
    }
    
    /*
    boolean newerVersion(String existing, String added) {
        String[] parts1 = existing.split(".");
        String[] parts2 = added.split(".");
        
        for (int i = 0; i < Math.min(parts1.length, parts2.length); i++) {
            if (parts1.length < i) {
                return true;
            } else if (parts2.length < i) {
                return false;
            }
            String p1 = parts1[i];
            String p2 = parts1[i];
            
            int suffix1 = p1.indexOf('-');
            int suffix2 = p2.indexOf('-');
            if ((suffix1 == -1) != (suffix2 == -1)) {
                // different qualifiers
                return false;
            }
            if (suffix1 != -1) {
                String s1 = p1.substring(suffix1 + 1);
                String s2 = p1.substring(suffix2 + 1);
                if (s1.compareTo(s2) >= 0) {
                    return false;
                }
            }
            int r;
            try {
                int v1, v2;
                v1 = Integer.parseInt(p1);
                v2 = Integer.parseInt(p2);
                r = v1 - v2;
            } catch (NumberFormatException ex) {
                // expected
                r = p1.compareTo(p2);
            }
            if (r < 0) {
                return true;
            } else if (r > 0) {
                return false;
            }
        }
        return false;
    }
    */

    @Override
    protected void throwDependencyConflicts() throws DependencyChangeException {
        if (!offending.isEmpty()) {
            throw new DependencyChangeException(request, DependencyChangeException.Reason.CONFLICT, offending);
        }
    }
    
    /**
     * 
     * ignoreVersions - exact versions are not relevant. For remove, it means remove a dependency regardless of the version.
     * skipConflicts - conflicting dependencies are just skipped instead of creating an error.
     * 
     */
    
    @Override
    protected boolean checkDependencyConflicts(Dependency existing, Dependency d) throws DependencyChangeException {
        ArtifactSpec existingA = existing.getArtifact();
        ArtifactSpec toAdd = d.getArtifact();
        
        GradleScope s = getScope(d);
        if (!(existingA.getGroupId().equals(toAdd.getGroupId()) &&
            existingA.getArtifactId().equals(toAdd.getArtifactId()))) {
            // different artifacts -> no conflicts
            return true;
        }
        if (!(existing.getScope().equals(s) || existing.getScope().name().equals(s.getTargetConfigurationName()))) {
            // second chance -- the specified scope could be a meta-scope that maps to Gradle configuration
            return true;
        }
        String existingC = existingA.getClassifier();
        if (existingA != null) {
            if (!Objects.equals(existingC, toAdd.getClassifier())) {
                return true;
            }
        }
        recordConflict(d, existing);
        return false;
        /*
        if (existingA.getVersionSpec() != null) {
            if (!existingA.getVersionSpec().equals(toAdd.getVersionSpec())) {
                recordConflict(d, existing);
                return false;
            }
            /*
            RETAIN for reference, upgrade was too complex, will be added later.
            if (request.getOptions().contains(Options.upgrade)) {
                if (newerVersion(existingA.getVersionSpec(), toAdd.getVersionSpec())) {
                    upgrades.put(existing, d);
                    return true;
                }
            }
        } else if (request.getOptions().contains(Options.upgrade)) {
            if (toAdd.getVersionSpec() != null) {
                upgrades.put(existing, d);
                return true;
            }
            * /
        } else {
            // skipConflicts - any potential conflict that would result in an exception, will be skipped.
            recordConflict(d, existing);
            return false;
        }
        
        // if versions should be ignored, then any version will result in a conflict, possibly skipped.
        if (request.getOptions().contains(Options.ignoreVersions)) {
            recordConflict(d, existing);
            return false;
        }
        
        return true;
        */
    }
    
    public static String[] gavSplit(String gav) {
        // the general GAV format is - <group>:<artifact>:<version/snapshot>[:<classifier>][@extension]
        int firstColon = gav.indexOf(':'); // NOI18N
        int versionColon = gav.indexOf(':', firstColon + 1); // NOI18N
        int versionEnd = versionColon > firstColon ? gav.indexOf(':', versionColon + 1) : -1; // NO18N

        if (firstColon == -1 || versionColon == -1 || firstColon == versionColon) {
            throw new IllegalArgumentException("Invalid GAV format: '" + gav + "'"); //NOI18N
        }
        int end = versionEnd == -1 ? gav.length() : versionEnd;

        return new String[]{
            gav.substring(0, firstColon),
            gav.substring(firstColon + 1, versionColon),
            gav.substring(versionColon + 1, end)
        };
    }

    private StringBuilder appendDependencyDecl(StringBuilder sb, Dependency d) throws DependencyChangeException {
        GradleScope gs = getScope(d);
        sb.append(gs.getTargetConfigurationName()).append("('");
        ArtifactSpec a = d.getArtifact();
        sb.append(a.getGroupId()).append(':').append(a.getArtifactId());
        if (a.getVersionSpec() != null) {
            sb.append(':').append(a.getVersionSpec());
        }
        sb.append("')");
        return sb;
    }
    
    /**
     * Generates a complete dependency block, assuming it is missing at all.
     * @throws DependencyChangeException 
     */
    void generateDependencyBlockAtEnd() throws DependencyChangeException {
        int start = lineDoc.getLength();
        StringBuilder sb = new StringBuilder();
        sb.append("dependencies {\n");
        for (Dependency d : accepted) {
            sb.append("    "); // indent
            appendDependencyDecl(sb, d);
            sb.append("\n");
        }
        sb.append("}\n");
        textEdits.add(new TextEdit(start, start, sb.toString()));
    }
    
    @Override
    protected void generateDependencies() throws DependencyChangeException {
        if (accepted.isEmpty()) {
            return;
        }
        DependencyResult current = context.getCurrentDependencies();
        SourceLocation loc;
        List<Dependency> roots = current.getRoot().getChildren();
        try {
            // do it a stupid way: choose indent according to the 1st dependency in the container,
            // place the declaration at the following line after the last dependency in the container.
            loc = current.getDeclarationRange(null, DependencyResult.PART_CONTAINER);
            
            if (loc == null) {
                // whoa ! No dependencies at all.
                generateDependencyBlockAtEnd();
                return;
            }
            
            // get the indent initially from the container's declaration itself, plus fixed indent
            int dependencyIndent = Math.max(0, 
                    LineDocumentUtils.getLineFirstNonWhitespace(lineDoc, loc.getStartOffset()) - 
                    LineDocumentUtils.getLineStart(lineDoc, loc.getStartOffset()));
            int declarationIndent = dependencyIndent + 4; // fixed-size indent, may need to change
            
            // but better to try one of the declarations within the container:
            for (int i = roots.size() - 1; i >= 0; i++) {
                Dependency d = roots.get(i);
                SourceLocation l2 = current.getDeclarationRange(d, null);
                if (l2 == null) {
                    continue;
                }
                SourceLocation container = current.getDeclarationRange(d, DependencyResult.PART_CONTAINER);
                if (container != null) {
                    // instead of nested declaration, get the start of the declaration group, as the new declaration will be indented
                    // on the same level.
                    l2 = container;
                }
                declarationIndent = Math.max(0, 
                    LineDocumentUtils.getLineFirstNonWhitespace(lineDoc, l2.getStartOffset()) - 
                    LineDocumentUtils.getLineStart(lineDoc, l2.getStartOffset()));
                // one item should be sufficient
                break;
            }
            
            // insert at the line where the `dependencies' ending } is; if it is alone, start from the beginning of the line with the indent, terminate
            // with a newline.
            int start = LineDocumentUtils.getLineStart(lineDoc, loc.getEndOffset());
            int nonWhite = LineDocumentUtils.getLineFirstNonWhitespace(lineDoc, loc.getEndOffset());
            StringBuilder toInsert = new StringBuilder();
            StringBuilder suffix = new StringBuilder();
            
            if (!"}".equals(lineDoc.getText(nonWhite, 1))) {
                // and if not alone on the line, cut the line at the } position, but add the whitespaces up to the former } column at the end (suffix).
                toInsert.append("\n");
                indent(suffix, loc.getEndOffset() - 1 - start);
                start = loc.getEndOffset() - 1;
            }

            for (Dependency d : accepted) {
                appendDependencyDecl(indent(toInsert, declarationIndent), d).append('\n');
            }
            toInsert.append(suffix);
            textEdits.add(new TextEdit(start, start, toInsert.toString()));
        } catch (BadLocationException | IOException ex) {
            // FIXME - handle
            Exceptions.printStackTrace(ex);
        }
    }
    
}
