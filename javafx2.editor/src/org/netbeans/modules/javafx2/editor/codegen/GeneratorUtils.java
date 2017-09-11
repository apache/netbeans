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
package org.netbeans.modules.javafx2.editor.codegen;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.GuardedException;
import org.netbeans.editor.Utilities;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Copy of org.netbeans.modules.java.editor.codegen.GeneratorUtils
 * @author Jan Lahoda, Dusan Balek
 */
@NbBundle.Messages({"ERR_CannotApplyGuarded=Cannot apply changes due to guarded block"})
public final class GeneratorUtils {

    private GeneratorUtils() {
    }
    
    public static StringBuilder getCapitalizedName(CharSequence cs) {
        StringBuilder sb = new StringBuilder(cs);
        while (sb.length() > 1 && sb.charAt(0) == '_') { //NOI18N
            sb.deleteCharAt(0);
        }

        //Beans naming convention, #165241
        if (sb.length() > 1 && Character.isUpperCase(sb.charAt(1))) {
            return sb;
        }

        if (sb.length() > 0) {
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        }
        return sb;
    }
    
    public static void guardedCommit(JTextComponent component, ModificationResult mr) throws IOException {
        try {
            mr.commit();
        } catch (IOException e) {
            if (e.getCause() instanceof GuardedException) {
                String message = NbBundle.getMessage(GeneratorUtils.class, "ERR_CannotApplyGuarded");

                Utilities.setStatusBoldText(component, message);
                Logger.getLogger(GeneratorUtils.class.getName()).log(Level.FINE, null, e);
            }
        }
    }
    
    public static ClassTree insertClassMembers(WorkingCopy wc, ClassTree clazz, List<? extends Tree> members, int offset) throws IllegalStateException {
        if (offset < 0 || getCodeStyle(wc).getClassMemberInsertionPoint() != CodeStyle.InsertionPoint.CARET_LOCATION) {
            return GeneratorUtilities.get(wc).insertClassMembers(clazz, members);
        }
        int index = 0;
        SourcePositions sp = wc.getTrees().getSourcePositions();
        GuardedDocument gdoc = null;
        try {
            Document doc = wc.getDocument();
            if (doc != null && doc instanceof GuardedDocument) {
                gdoc = (GuardedDocument)doc;
            }
        } catch (IOException ioe) {}
        Tree lastMember = null;
        for (Tree tree : clazz.getMembers()) {
            if (offset <= sp.getStartPosition(wc.getCompilationUnit(), tree)) {
                if (gdoc == null) {
                    break;
                }
                int pos = (int)(lastMember != null ? sp.getEndPosition(wc.getCompilationUnit(), lastMember) : sp.getStartPosition(wc.getCompilationUnit(), clazz));
                pos = gdoc.getGuardedBlockChain().adjustToBlockEnd(pos);
                if (pos <= sp.getStartPosition(wc.getCompilationUnit(), tree)) {
                    break;
                }
            }
            index++;
            lastMember = tree;
        }
        TreeMaker tm = wc.getTreeMaker();
        for (int i = members.size() - 1; i >= 0; i--) {
            clazz = tm.insertClassMember(clazz, index, members.get(i));
        }
        return clazz;
    }
    
    private static CodeStyle getCodeStyle(CompilationInfo info) {
        if (info != null) {
            try {
                Document doc = info.getDocument();
                if (doc != null) {
                    CodeStyle cs = (CodeStyle)doc.getProperty(CodeStyle.class);
                    return cs != null ? cs : CodeStyle.getDefault(doc);
                }
            } catch (IOException ioe) {
                // ignore
            }
            
            FileObject file = info.getFileObject();
            if (file != null) {
                return CodeStyle.getDefault(file);
            }
        }
        
        return CodeStyle.getDefault((Document)null);
    }
}
