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
