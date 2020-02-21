/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.cnd.refactoring.hints;

import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 *
 */
public class AssignmentVariableFix extends IntroduceVariableBaseFix {
    final FileObject fo;
    private String type;

    public AssignmentVariableFix(CsmExpression expression, Document doc, FileObject fo) {
        super(expression, doc);
        this.fo = fo;
    }

    @Override
    public String getText() {
        return NbBundle.getMessage(SuggestionFactoryTask.class, "FIX_AssignResultToVariable"); //NOI18N
    }

    @Override
    protected boolean isC() {
        return MIMENames.C_MIME_TYPE.equals(fo.getMIMEType());
    }

    @Override
    protected boolean isInstanceRename() {
        return true;
    }

    @Override
    protected List<Pair<Integer, Integer>> replaceOccurrences() {
        return Collections.emptyList();
    }

    @Override
    protected String getType() {
        return type;
    }

    @Override
    public ChangeInfo implement() throws Exception {
        type = suggestType();
        if (type == null) {
            return null;
        }

        final String aName = suggestName();
        if (aName == null) {
            return null;
        }
        final String text = getType() + " " + aName + " = "; //NOI18N
        doc.insertString(expression.getStartOffset(), text, null);
        Position startPosition = new Position() {
            @Override
            public int getOffset() {
                return expression.getStartOffset() + getType().length() + 1;
            }
        };
        Position endPosition = new Position() {
            @Override
            public int getOffset() {
                return expression.getStartOffset() + text.length() - 3;
            }
        };
        ChangeInfo changeInfo = new ChangeInfo(fo, startPosition, endPosition);
        return changeInfo;
    }

}
