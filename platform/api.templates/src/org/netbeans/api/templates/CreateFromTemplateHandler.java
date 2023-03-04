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

package org.netbeans.api.templates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/** This is an interface for <i>smart templating</i> that allows
 * any module to intercept calls to 
 * {@link FileBuilder#build()}
 * and handle them themselves. The NetBeans IDE provides default
 * implementation that allows use of Freemarker templating engine.
 * Read more in the <a href="@TOP@/architecture-summary.html#script">howto document</a>.
 * <p>
 * An implementation of CreateHandler should honor {@link CreateDescriptor#hasFreeExtension()} and
 * {@link CreateDescriptor#isPreformatted()}.
 * 
 * @author Jaroslav Tulach
 * @author Svatopluk Dedic
 */
public abstract class CreateFromTemplateHandler {
    /** Method that allows a handler to reject a file. If all handlers
     * reject a file, regular processing defined in {@link FileBuilder#createFromTemplate(org.openide.filesystems.FileObject, org.openide.filesystems.FileObject, java.lang.String, java.util.Map, org.netbeans.api.templates.FileBuilder.Mode)}
     * is going to take place.
     * 
     * @param desc describes the request that is about to be performed
     * @return true if this handler wants to handle the createFromTemplate operation
     */
    protected abstract boolean accept(CreateDescriptor desc);
    
    /** Handles the creation of new files. The Handler may create one or more files. The 
     * files should be ordered so that the "important" file (i.e. the one which is then presented
     * to the user etc) is ordered first in the list.
     * 
     * @param desc command objects that describes the file creation request
     * @return the newly create file
     * @throws IOException if something goes wrong with I/O
     */
    protected abstract @NonNull List<FileObject> createFromTemplate(
            CreateDescriptor    desc
    ) throws IOException;

    /**
     * Replaces ${param} tokens in a String. The parameter reference has syntax "${" paramName [":" defaultValue ] "}".
     * Parameters are replaced recursively. "${" can be escaped by \, so "\${" will not act as parameter reference start.
     * @param expression string template
     * @param parameters parameter values
     * @return evaluated string.
     */
    static String mapParameters(String expression, Map<String, ?> parameters) {
        int start = 0;
        int pos = 0;
        StringBuilder sb = null;
        
        while ((pos = expression.indexOf("${", pos)) != -1) { // NOI18N
            if (pos > 0 && expression.charAt(pos - 1) == '\\') { // NOI18N
                pos += 2;
                continue;
            }
            int endPos = pos + 2;
            int state = 0;
            int nested = 0;
            A: while (endPos < expression.length()) {
                char c = expression.charAt(endPos);
                if (state == 1) {
                    state = 0;
                    endPos++;
                    continue;
                }
                switch (c) {
                    case '\\': // NOI18N
                        state = 1;
                        break;
                    case '$': // NOI18N
                        state = 2;
                        break;
                    case '{': // NOI18N
                        if (state == 2) {
                            state = 0;
                            nested++;
                        }
                        break;
                    case '}': // NOI18N
                        if (state == 0) {
                            if (nested-- == 0) {
                                break A;
                            }
                        }
                        break;
                }
                endPos++;
            }
            if (endPos >= expression.length()) {
                pos += 2;
                continue;
            }
            String token = expression.substring(pos + 2, endPos);
            String defValue = null;
            
            int colon = token.indexOf(':'); // NOI18N
            if (colon != -1) {
                defValue = token.substring(colon + 1);
                token = token.substring(0, colon);
            }
            
            Object v = parameters.get(token);
            if (v == null) {
                v = defValue;
            }
            if (v == null) {
                pos += 2;
                continue;
            }
            if (sb == null) {
                sb = new StringBuilder();
            }
            sb.append(expression.substring(start, pos)).append(v.toString());
            start = endPos + 1;
            pos = start;
        }
        if (sb == null) {
            return expression;
        }
        if (start < expression.length()) {
            sb.append(expression.substring(start));
        }
        
        return mapParameters(sb.toString(), parameters);
    }
    
    private static final String PROP_TEMPLATE = "template"; // NOI18N

    /**
     * Prefix for template-controlling attributes. Such attributes will not be copied
     * during template instantiation.
     */
    private static final String ATTR_TEMPLATE_PREFIX = "template.";
    
    /**
     * Copies template attributes over to the created file. Copies over from the source (template) to the target (usually
     * the produced file), except attributes that parametrize template creation. By default, attributes that are prefixed
     * with {@code "template."} are not copied. As the operation is not atomic, the method should be called in a 
     * {@link org.openide.filesystems.FileSystem#runAtomicAction(org.openide.filesystems.FileSystem.AtomicAction)}.
     * The {@code "template"} attribute is never copied.
     * 
     * @param h the Handler responsible for file creation, usually the calling handler.
     * @param from the original file
     * @param to the target file
     * @throws IOException if an I/O error occurs.
     */
    public static void copyAttributesFromTemplate(CreateFromTemplateHandler h, FileObject from, FileObject to) throws IOException {
        // copy attributes; some attributes are filtered by FileSystems API already
        FileUtil.copyAttributes (from, to, (n, v) -> {
            if ("javax.script.ScriptEngine".equals(n) // NOI18N 
                    || n.startsWith(ATTR_TEMPLATE_PREFIX)
                    || PROP_TEMPLATE.equals(n)) {
                return null;
            } else {
                return FileUtil.defaultAttributesTransformer().apply(n, v);
            }
        });
    }
    
    
    /**
     * Copies the files or folders contained in the specified folder to the target. The original descriptor can be passed in,
     * since it has Lookup, parameters etc set up. They will be passed on to individual created files. The `contentsParent` may
     * be {@code null}, in which case the {@link CreateDescriptor#getTemplate()} folder will be used.
     * @param origDescriptor original descriptor to get parameters / lookup from
     * @param contentsParent template folder whose contents should be copied
     * @param target target folder
     * @return created files
     * @throws IOException 
     * @since 1.23
     */
    protected static List<FileObject> defaultCopyContents(CreateDescriptor origDescriptor, FileObject contentsParent, FileObject target) throws IOException {
        if (contentsParent == null) {
            contentsParent = origDescriptor.getTemplate();
        }
        if (contentsParent == null || !contentsParent.isFolder()) {
            throw new IllegalArgumentException("contentsParent is not a folder: " + contentsParent.getPath());
        }
        List<FileObject> r = new ArrayList<>();
        if (!origDescriptor.getParameters().containsKey("_noFolders")) { // NOI18N
            r.add(target);
        }
        for (FileObject child : origDescriptor.getTemplate().getChildren()) {
            Map<String, Object> parameters = new HashMap<>(origDescriptor.getParameters());
            parameters.remove("name"); // NOI18N
            parameters.remove("nameAndExt"); // NOI18N

            // recursively instruct not to return folders unnecessarily
            parameters.put("_noFolders", Boolean.TRUE); // NOI18N
            
            FileBuilder b = new FileBuilder(child, target);
            b.defaultMode(FileBuilder.Mode.COPY)
                    .useLookup(origDescriptor.getLookup())
                    .withParameters(parameters).createDescriptor(true);
            
            List<FileObject> files = b.build();
            if (child.isFolder() || Boolean.TRUE.equals(child.getAttribute(FileBuilder.ATTR_TEMPLATE_OPEN_FILE))) {
                r.addAll(files);
            }
        }
        return r;
    }
    
}
