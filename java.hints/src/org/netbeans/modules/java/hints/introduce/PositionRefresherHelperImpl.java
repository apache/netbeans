/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.introduce;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.support.SelectionAwareJavaSourceTaskFactory;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.java.hints.introduce.PositionRefresherHelperImpl.DocumentVersionImpl;
import org.netbeans.modules.java.hints.providers.spi.PositionRefresherHelper;
import org.netbeans.modules.java.hints.providers.spi.PositionRefresherHelper.DocumentVersion;
import org.netbeans.spi.editor.hints.Context;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lahvac
 */
@MimeRegistration(mimeType="text/x-java", service=PositionRefresherHelper.class)
public class PositionRefresherHelperImpl extends PositionRefresherHelper<DocumentVersionImpl> {
    public PositionRefresherHelperImpl() {
        super(IntroduceHint.class.getName());
    }
    
    @Override
    protected boolean isUpToDate(Context context, Document doc, DocumentVersionImpl oldVersion) {
        FileObject file = NbEditorUtilities.getFileObject(doc);

        if (file == null) return false;

        int[] selection = SelectionAwareJavaSourceTaskFactory.getLastSelection(file);

        if (selection == null) return false;
        
        return oldVersion.introduceSelStart == selection[0] && oldVersion.introduceSelEnd == selection[1];
    }

    @Override
    public List<ErrorDescription> getErrorDescriptionsAt(CompilationInfo info, Context context, Document doc) throws Exception {
        int[] selection = SelectionAwareJavaSourceTaskFactory.getLastSelection(info.getFileObject());

        if (selection == null) {
            return Collections.emptyList();
        }
        
        return IntroduceHint.computeError(info, selection[0], selection[1], new EnumMap<IntroduceKind, Fix>(IntroduceKind.class), new EnumMap<IntroduceKind, String>(IntroduceKind.class), context.getCancel());
    }

    static void setVersion(Document doc, int selStart, int selEnd) {
        for (PositionRefresherHelper h : MimeLookup.getLookup("text/x-java").lookupAll(PositionRefresherHelper.class)) {
            if (h instanceof PositionRefresherHelperImpl) {
                ((PositionRefresherHelperImpl) h).setVersion(doc, new DocumentVersionImpl(doc, selStart, selEnd));
            }
        }
    }

    static final class DocumentVersionImpl extends DocumentVersion {

        private final long introduceSelStart;
        private final long introduceSelEnd;

        public DocumentVersionImpl(Document doc, long introduceSelStart, long introduceSelEnd) {
            super(doc);
            this.introduceSelStart = introduceSelStart;
            this.introduceSelEnd = introduceSelEnd;
        }

    }
}
