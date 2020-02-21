/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.parser.clank;

import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.apt.support.ClankDriver.ClankMacroDirective;
import org.netbeans.modules.cnd.modelimpl.csm.MacroImpl;
import org.netbeans.modules.cnd.modelimpl.csm.SystemMacroImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.filesystems.FileSystem;
import org.openide.util.CharSequences;

/**
 *
 */
public final class MacroReference extends OffsetableBase implements CsmReference {
    private final CsmMacro referencedMacro;

    static MacroReference createMacroReference(FileImpl curFile, int startOffset, int endOffset, FileImpl startFile, final ClankMacroDirective directive) {
        CsmMacro referencedMacro;
        CharSequence macroName = directive.getMacroName();
        if (CharSequenceUtilities.equals(ClankMacroDirective.BUILD_IN_FILE, directive.getFile())) {
            CharSequence body = findBody(startFile, macroName);
            if (body == null) {
                if (CharSequences.comparator().compare("__FILE__", macroName) == 0) { // NOI18N
                    //NOI18N
                    body = curFile.getAbsolutePath();
                } else if (CharSequences.comparator().compare("__LINE__", macroName) == 0) { // NOI18N
                    //NOI18N
                    body = "" + curFile.getLineColumn(startOffset)[0];
                } else if (CharSequences.comparator().compare("__DATE__", macroName) == 0) { // NOI18N
                    //NOI18N
                    body = "";
                } else if (CharSequences.comparator().compare("__TIME__", macroName) == 0) { // NOI18N
                    //NOI18N
                    body = "";
                } else if (CharSequences.comparator().compare("__FUNCTION__", macroName) == 0) { // NOI18N
                    body = "";
                } else {
                    // file was parsed without context?
                    //assert false : "Directive " + directive + " with unknown system or user predefined macros"; //NOI18N
                    body = "";
                }
            }
            referencedMacro = SystemMacroImpl.create(macroName, body, directive.getParameters(), startFile, findType(startFile, macroName));
        } else {
            CsmFile targetFile = getTargetFile(startFile, directive.getFile());
            if (targetFile == null) {
                if (curFile.isValid() && CsmModelAccessor.isModelAlive()) {
                    if (TraceFlags.REPORT_PARSING_ERRORS || CndUtils.isUnitTestMode()) {
                        CndUtils.assertTrueInConsole(false, "Can not resolve file by path in macro directive: ["+directive + //NOI18N
                                "] used at [" + startOffset + "-" + endOffset + "] in file [" + curFile + " valid=" + curFile.isValid() + //NOI18N
                                "] included from [" + startFile + " valid=" + startFile.isValid()+"]"); //NOI18N
                    }
                }
                return null;
                //targetFile = ((ProjectBase)curFile.getProject()).getUnresolvedFile();
            }
            referencedMacro = MacroImpl.create(macroName, directive.getParameters(), "", targetFile, directive.getDirectiveStartOffset(), directive.getDirectiveEndOffset(), CsmMacro.Kind.DEFINED); //NOI18N
        }
        return new MacroReference(curFile, startOffset, endOffset, referencedMacro);
    }

    public static CsmMacro.Kind findType(FileImpl startFile, CharSequence macroName) {
        CsmMacro.Kind res = CsmMacro.Kind.COMPILER_PREDEFINED;
        NativeFileItem item = startFile.getNativeFileItem();
        if (item != null) {
            for (String m : item.getUserMacroDefinitions()) {
                if (CharSequenceUtilities.startsWith(m, macroName)) {
                    res = CsmMacro.Kind.USER_SPECIFIED;
                    break;
                }
            }
        }
        return res;
    }

    public static CharSequence findBody(FileImpl startFile, CharSequence macroName) {
        CharSequence res = null;
        NativeFileItem item = startFile.getNativeFileItem();
        if (item != null) {
            for (String m : item.getSystemMacroDefinitions()) {
                res = extractBody(macroName, m, res);
            }
            for (String m : item.getUserMacroDefinitions()) {
                res = extractBody(macroName, m, res);
            }
        }
        return res;
    }

    private static CharSequence extractBody(CharSequence macroName, String candidate, CharSequence res) {
        if (CharSequenceUtilities.startsWith(candidate, macroName)) {
            boolean parmList = false;
            for(int i = macroName.length(); i < candidate.length(); i++) {
                char c = candidate.charAt(i);
                if (c == '(') {
                    parmList = true;
                } else if (c == ')') {
                    parmList = false;
                } else if (parmList) {
                } else if (c == '=') {
                    res = candidate.substring(i+1);
                    break;
                } else if (c == ' ') {
                } else {
                    break;
                }
            }
        }
        return res;
    }

    private MacroReference(FileImpl curFile, int startOffset, int endOffset, CsmMacro referencedMacro) {
        super(curFile, startOffset, endOffset);
        this.referencedMacro = referencedMacro;
    }

    @Override
    public CsmObject getReferencedObject() {
        return referencedMacro;
    }

    @Override
    public CsmObject getOwner() {
        return null;
    }

    @Override
    public CsmReferenceKind getKind() {
        return CsmReferenceKind.DIRECT_USAGE;
    }

    @Override
    public CharSequence getText() {
        return referencedMacro.getName();
    }

    @Override
    public CsmObject getClosestTopLevelObject() {
        return getContainingFile();
    }

    static CsmFile getTargetFile(FileImpl current, CharSequence macroContainerFile) {
        CsmFile target = null;
        if (current != null && macroContainerFile.length() > 0) {
            FileSystem fs;
            ProjectBase currentPrj = (ProjectBase)current.getProject();
            ProjectBase targetPrj = currentPrj.findFileProject(macroContainerFile, true);
            if (targetPrj != null) {
                target = targetPrj.findFile(macroContainerFile, true, false);
                fs = targetPrj.getFileSystem();
            } else {
                fs = currentPrj.getFileSystem();
            }
            // try full model?
            if (target == null) {
                target = CsmModelAccessor.getModel().findFile(new FSPath(fs, macroContainerFile.toString()), false, false);
            }
        }
        return target;
    }

}
