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
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.spring.beans.completion.completors;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import org.netbeans.modules.spring.api.beans.SpringConstants;
import org.netbeans.modules.spring.beans.completion.CompletionContext;
import org.netbeans.modules.spring.beans.completion.Completor;
import org.netbeans.modules.spring.beans.completion.CompletorUtils;
import org.netbeans.modules.spring.beans.completion.SpringXMLConfigCompletionItem;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class ResourceCompletor extends Completor {

    public ResourceCompletor(int invocationOffset) {
        super(invocationOffset);
    }

    @Override
    protected int initAnchorOffset(CompletionContext context) {
        int idx = context.getCurrentTokenOffset() + 1;
        String typedChars = context.getTypedPrefix();
        int lastSlashIndex = typedChars.lastIndexOf("/"); // NOI18N
        return idx + lastSlashIndex + 1;
    }

    @Override
    protected void compute(CompletionContext context) throws IOException {
        FileObject fileObject = context.getFileObject().getParent();
        String typedChars = context.getTypedPrefix();

        int lastSlashIndex = typedChars.lastIndexOf("/"); // NOI18N
        String prefix = typedChars;

        if (lastSlashIndex != -1) {
            String pathStr = typedChars.substring(0, lastSlashIndex); // NOI18N
            fileObject = fileObject.getFileObject(pathStr);
            if (lastSlashIndex != typedChars.length() - 1) {
                prefix = typedChars.substring(Math.min(typedChars.lastIndexOf("/") + 1, // NOI18N
                        typedChars.length() - 1));
            } else {
                prefix = "";
            }
        }

        if (fileObject == null) {
            return;
        }

        if (prefix == null) {
            prefix = "";
        }

        Enumeration<? extends FileObject> folders = fileObject.getFolders(false);
        while (folders.hasMoreElements()) {
            FileObject fo = folders.nextElement();
            if (fo.getNameExt().startsWith(prefix)) {
                addCacheItem(SpringXMLConfigCompletionItem.createFolderItem(context.getCaretOffset() - prefix.length(),
                        fo));
            }
        }

        Enumeration<? extends FileObject> files = fileObject.getData(false);
        while (files.hasMoreElements()) {
            FileObject fo = files.nextElement();
            if (fo.getNameExt().startsWith(prefix) && SpringConstants.CONFIG_MIME_TYPE.equals(fo.getMIMEType())) {
                addCacheItem(SpringXMLConfigCompletionItem.createSpringXMLFileItem(context.getCaretOffset() - prefix.length(), fo));
            }
        }
    }

    @Override
    public boolean canFilter(CompletionContext context) {
        return CompletorUtils.canFilter(context.getDocument(), getInvocationOffset(), context.getCaretOffset(), getAnchorOffset(), CompletorUtils.RESOURCE_PATH_ELEMENT_ACCEPTOR);
    }

    @Override
    protected List<SpringXMLConfigCompletionItem> doFilter(CompletionContext context) {
        return CompletorUtils.filter(getCacheItems(), context.getDocument(), getInvocationOffset(), context.getCaretOffset(), getAnchorOffset());
    }
}
