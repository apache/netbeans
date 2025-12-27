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

package org.netbeans.spi.java.hints;

import com.sun.source.util.TreePath;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSourcePath;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.java.hints.spiimpl.JavaFixImpl;
import org.netbeans.modules.java.hints.spiimpl.JavaFixImpl.EnhancedJavaFixImpl;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchUtilities;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**A base class for fixes that modify Java source code. Using this class
 * as a base class makes creating the fix somewhat simpler, but also supports
 * running the hint in the Inspect&amp;Transform dialog. The fix can be converted
 * to {@link Fix} by means of the {@link #toEditorFix() } method.
 *
 * @see JavaFixUtilities for various predefined fixes.
 * @author Jan Lahoda
 */
public abstract class JavaFix {

    private final JavaSourcePath path;
    private final TreePathHandle handle;
    private final Map<String, String> options;
    private final String sortText;
    private volatile Function<ModificationResult, ChangeInfo> modResult2ChangeInfo;

    /**Create JavaFix with the given base {@link TreePath}. The base {@link TreePath}
     * will be passed back to the real implementation of the fix.
     *
     * @param info a {@link CompilationInfo} from which the given {@link TreePath} originates
     * @param tp a {@link TreePath} that will be passed back to the
     *           {@link #performRewrite(org.netbeans.spi.java.hints.JavaFix.TransformationContext) } method
     */
    protected JavaFix(@NonNull CompilationInfo info, @NonNull TreePath tp) {
        this(info, tp, Collections.<String, String>emptyMap());
    }
    
    /**Create JavaFix with the given base {@link TreePath}. The base {@link TreePath}
     * will be passed back to the real implementation of the fix.
     *
     * @param info a {@link CompilationInfo} from which the given {@link TreePath} originates
     * @param tp a {@link TreePath} that will be passed back to the
     *           {@link #performRewrite(org.netbeans.spi.java.hints.JavaFix.TransformationContext) } method
     * @param sortText if non-null, fix returned from {@link #toEditorFix() } will be an {@link EnhancedFix},
     *                 and the given {@code sortText} will be returned from its {@link EnhancedFix#getSortText() }.
     * @since 1.18
     */
    protected JavaFix(@NonNull CompilationInfo info, @NonNull TreePath tp, @NullAllowed String sortText) {
        this(info.getJavaSourcePath(), TreePathHandle.create(tp, info), Collections.<String, String>emptyMap(), sortText);
    }

    JavaFix(CompilationInfo info, TreePath tp, Map<String, String> options) {
        this(info.getJavaSourcePath(), TreePathHandle.create(tp, info), options, null);
    }
    
    /**Create JavaFix with the given base {@link TreePathHandle}. The base {@link TreePathHandle}
     * will be resolved and passed back to the real implementation of the fix.
     *
     * @param handle a {@link TreePathHandle} that will be resolved and passed back to the
     *              {@link #performRewrite(org.netbeans.spi.java.hints.JavaFix.TransformationContext) } method
     */
    //TODO: deprecate? won't reasonably work for embedding
    protected JavaFix(@NonNull TreePathHandle handle) {
        this(null, handle, Collections.<String, String>emptyMap());
    }

    /**Create JavaFix with the given base {@link TreePathHandle}. The base {@link TreePathHandle}
     * will be resolved and passed back to the real implementation of the fix.
     *
     * @param handle a {@link TreePathHandle} that will be resolved and passed back to the
     *              {@link #performRewrite(org.netbeans.spi.java.hints.JavaFix.TransformationContext) } method
     * @param sortText if non-null, fix returned from {@link #toEditorFix() } will be an {@link EnhancedFix},
     *                 and the given {@code sortText} will be returned from its {@link EnhancedFix#getSortText() }.
     * @since 1.18
     */
    //TODO: deprecate? won't reasonably work for embedding
    protected JavaFix(@NonNull TreePathHandle handle, @NullAllowed String sortText) {
        this(null, handle, Collections.<String, String>emptyMap());
    }

    JavaFix(JavaSourcePath path, TreePathHandle handle, Map<String, String> options) {
        this(path, handle, options, null);
    }
    
    JavaFix(JavaSourcePath path, TreePathHandle handle, Map<String, String> options, String sortText) {
        this.path = path != null ? path : JavaSourcePath.forFile(handle.getFileObject());
        this.handle = handle;
        this.options = Collections.unmodifiableMap(new HashMap<>(options));
        this.sortText = sortText;
    }

    /**The display text of the fix.
     *
     * @return the display text of the fix.
     */
    protected abstract @NonNull String getText();

    /**Do the transformations needed to implement the hint's function.
     *
     * @param ctx a context over which the fix should operate
     * @throws Exception if something goes wrong while performing the transformation
     *                   - will be logged by the infrastructure
     */
    protected abstract void performRewrite(@NonNull TransformationContext ctx) throws Exception;

    /**Convert this {@link JavaFix} into the Editor Hints {@link Fix}.
     *
     * @return a {@link Fix}, that when invoked, will invoke {@link #performRewrite(org.netbeans.spi.java.hints.JavaFix.TransformationContext) }
     * method on this {@link JavaFix}.
     */
    public final Fix toEditorFix() {
        return sortText != null ? new EnhancedJavaFixImpl(this) : new JavaFixImpl(this);
    }

    static {
        JavaFixImpl.Accessor.INSTANCE = new JavaFixImpl.Accessor() {
            @Override
            public String getText(JavaFix jf) {
                return jf.getText();
            }
            @Override
            public ChangeInfo process(JavaFix jf, WorkingCopy wc, boolean canShowUI, Map<FileObject, byte[]> resourceContent, Collection<? super RefactoringElementImplementation> fileChanges) throws Exception {
                TreePath tp = jf.handle.resolve(wc);
                if (tp == null) {
                    Logger.getLogger(JavaFix.class.getName()).log(Level.SEVERE, "Cannot resolve handle={0}", jf.handle);
                    return null;
                }

                GeneratorUtilities.get(wc).importComments(tp.getLeaf(), wc.getCompilationUnit());
                jf.performRewrite(new TransformationContext(wc, tp, canShowUI, resourceContent, fileChanges));

                return null;
            }
            @Override
            public JavaSourcePath getJavaSourcePath(JavaFix jf) {
                return jf.path;
            }
            @Override
            public Map<String, String> getOptions(JavaFix jf) {
                return jf.options;
            }

            @Override
            public Fix rewriteFix(CompilationInfo info, String displayName, TreePath what, String to, Map<String, TreePath> parameters, Map<String, Collection<? extends TreePath>> parametersMulti, Map<String, String> parameterNames, Map<String, TypeMirror> constraints, Map<String, String> options, String... imports) {
                return JavaFixUtilities.rewriteFix(info, displayName, what, to, parameters, parametersMulti, parameterNames, constraints, options, imports);
            }

            @Override
            public Fix createSuppressWarningsFix(CompilationInfo compilationInfo, TreePath treePath, String... keys) {
                return ErrorDescriptionFactory.createSuppressWarningsFix(compilationInfo, treePath, keys);
            }

            @Override
            public List<Fix> createSuppressWarnings(CompilationInfo compilationInfo, TreePath treePath, String... keys) {
                return ErrorDescriptionFactory.createSuppressWarnings(compilationInfo, treePath, keys);
            }

            @Override
            public List<Fix> resolveDefaultFixes(HintContext ctx, Fix... provided) {
                return ErrorDescriptionFactory.resolveDefaultFixes(ctx, provided);
            }

            @Override
            public String getSortText(JavaFix jf) {
                return jf.sortText;
            }

            @Override
            public void setChangeInfoConvertor(JavaFix jf, Function<ModificationResult, ChangeInfo> modResult2ChangeInfo) {
                jf.modResult2ChangeInfo = modResult2ChangeInfo;
            }

            @Override
            public Function<ModificationResult, ChangeInfo> getChangeInfoConvertor(JavaFix jf) {
                return jf.modResult2ChangeInfo;
            }
        };
    }

    /**A context that contains a reference to a {@link WorkingCopy} through which
     * modifications of Java source code can be made.
     *
     */
    public static final class TransformationContext {
        private final WorkingCopy workingCopy;
        private final TreePath path;
        private final boolean canShowUI;
        private final Map<FileObject, byte[]> resourceContentChanges;
        private final Collection<? super RefactoringElementImplementation> fileChanges;
        TransformationContext(WorkingCopy workingCopy, TreePath path, boolean canShowUI, Map<FileObject, byte[]> resourceContentChanges, Collection<? super RefactoringElementImplementation> fileChanges) {
            this.workingCopy = workingCopy;
            this.path = path;
            this.canShowUI = canShowUI;
            this.resourceContentChanges = resourceContentChanges;
            this.fileChanges = fileChanges;
        }

        boolean isCanShowUI() {
            return canShowUI;
        }

        /**Returns the {@link TreePath} that was passed to a {@link JavaFix} constructor.
         *
         * @return the {@link TreePath} that was passed to a {@link JavaFix} constructor.
         */
        public @NonNull TreePath getPath() {
            return path;
        }

        /**A {@link WorkingCopy} over which the transformation should operate.
         * @return {@link WorkingCopy} over which the transformation should operate.
         */
        public @NonNull WorkingCopy getWorkingCopy() {
            return workingCopy;
        }

        /**Allows access to non-Java resources. The content of this InputStream will
         * include all changes done through {@link #getResourceOutput(org.openide.filesystems.FileObject) }
         * before calling this method.
         *
         * @param file whose content should be returned
         * @return the file's content
         * @throws IOException if something goes wrong while opening the file
         * @throws IllegalArgumentException if {@code file} parameter is null, or
         *                                  if it represents a Java file
         */
        public @NonNull InputStream getResourceContent(@NonNull FileObject file) throws IOException, IllegalArgumentException {
            Parameters.notNull("file", file);
            if ("text/x-java".equals(file.getMIMEType("text/x-java")))
                throw new IllegalArgumentException("Cannot access Java files");

            byte[] newContent = resourceContentChanges != null ? resourceContentChanges.get(file) : null;

            if (newContent == null) {
                final Document doc = BatchUtilities.getDocument(file);

                if (doc != null) {
                    final String[] result = new String[1];

                    doc.render(() -> {
                        try {
                            result[0] = doc.getText(0, doc.getLength());
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    });

                    if (result[0] != null) {
                        ByteBuffer encoded = FileEncodingQuery.getEncoding(file).encode(result[0]);
                        byte[] encodedBytes = new byte[encoded.remaining()];

                        encoded.get(encodedBytes);

                        return new ByteArrayInputStream(encodedBytes);
                    }
                }
                
                return file.getInputStream();
            } else {
                return new ByteArrayInputStream(newContent);
            }
        }

        /**Record a changed version of a file. The changes will be applied altogether with
         * changes to the Java file. In Inspect&amp;Transform, changes done through this
         * method will be part of the preview.
         *
         * @param file whose content should be changed
         * @return an {@link java.io.OutputStream} into which the new content of the file should be written
         * @throws IOException if something goes wrong while opening the file
         * @throws IllegalArgumentException if {@code file} parameter is null, or
         *                                  if it represents a Java file
         */
        public @NonNull OutputStream getResourceOutput(@NonNull final FileObject file) throws IOException {
            Parameters.notNull("file", file);
            if ("text/x-java".equals(file.getMIMEType("text/x-java")))
                throw new IllegalArgumentException("Cannot access Java files");
            if (resourceContentChanges == null) return file.getOutputStream();

            return new ByteArrayOutputStream() {
                @Override public void close() throws IOException {
                    super.close();
                    resourceContentChanges.put(file, toByteArray());
                }
            };
        }

        Collection<? super RefactoringElementImplementation> getFileChanges() {
            return fileChanges;
        }

    }

}
