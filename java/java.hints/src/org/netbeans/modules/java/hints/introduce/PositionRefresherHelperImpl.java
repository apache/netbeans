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
