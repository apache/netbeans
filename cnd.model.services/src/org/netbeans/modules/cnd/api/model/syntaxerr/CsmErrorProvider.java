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

package org.netbeans.modules.cnd.api.model.syntaxerr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.xref.CsmIncludeHierarchyResolver;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.NamedOption;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * An abstract error provider.
 */
public abstract class CsmErrorProvider extends NamedOption {
    private static final boolean TRACE_TASKS = false;
    private final Collection<? extends RequestValidator> requestAndProviderValidators;

    public CsmErrorProvider() {
        requestAndProviderValidators = Lookup.getDefault().lookupAll(CsmErrorProvider.RequestValidator.class);
    }

    private boolean checkValidators(Request request) {
        for (CsmErrorProvider.RequestValidator p : requestAndProviderValidators) {
            if (!p.isValid(this, request)) {
                return false;
            }
        }
        return true;
    }

    //
    // Interface part
    //

    /** Represents a request for getting errors for a particular file   */
    public interface Request {

        /** A file to process */
        CsmFile getFile();

        /** Determines whether the caller wants to cancel the processing of the request */
        boolean isCancelled();

        Document getDocument();
        
        EditorEvent getEvent();
    }

    /** Response for adding errors for a particular file */
    public interface Response {

        /** Is called for each error */
        void addError(CsmErrorInfo errorInfo);

        /** Is called once the processing is done */
        void done();
    }
    
    public enum EditorEvent {
        DocumentBased,
        FileBased
    }
    
    public interface RequestValidator {

        /**
         * Checks should we terminate highlighting for this request or not.
         *
         * @param request - request for highlighting
         * @return disable or not
         */
        public boolean isValid(CsmErrorProvider provider, CsmErrorProvider.Request request);

    }
    

    public final void getErrors(Request request, Response response) {
        if (validate(request) && checkValidators(request)) {
            doGetErrors(request, response);
        }
        response.done();
    }

    protected boolean validate(CsmErrorProvider.Request request) {
        return NamedOption.getAccessor().getBoolean(getName()) && !request.isCancelled();
    }
    
    @Override
    public OptionKind getKind() {
        return OptionKind.Boolean;
    }

    @Override
    public Object getDefaultValue() {
        return true;
    }

    public boolean hasHintControlPanel() {
        return false;
    }
    
    protected abstract void doGetErrors(Request request, Response response);
    
    public abstract boolean isSupportedEvent(EditorEvent kind);

    public static boolean disableAsLibraryHeaderFile(CsmFile file) {
        // partially included files are excluded
        if (CsmErrorProvider.isPartial(file, new HashSet<CsmFile>())) {
            return true;
        }
        // in release mode we skip library files, because it's very irritating
        // for user to see errors in system libraries
        return CndUtils.isReleaseMode() && (file != null) && file.isHeaderFile() && 
                (file.getProject() != null) && file.getProject().isArtificial();
    }

    @Override
    public String toString() {
        return getName();
    }
    
    //
    // Implementation part
    //

    private static final boolean ENABLE = CndUtils.getBoolean("cnd.csm.errors", true); //NOI18N
    private static final boolean ASYNC = CndUtils.getBoolean("cnd.csm.errors.async", true); //NOI18N
    private static final RequestProcessor RP = new RequestProcessor("ErrorsProvider", CndUtils.getConcurrencyLevel()*2); // NOI18N
    private static abstract class BaseMerger extends CsmErrorProvider {

        protected final Lookup.Result<CsmErrorProvider> res;

        public BaseMerger() {
            res = Lookup.getDefault().lookupResult(CsmErrorProvider.class);
        }

        protected abstract void getErrorsImpl(Request request, Response response);

        @Override
        protected boolean validate(CsmErrorProvider.Request request) {
            // all real providers should call super
            return ENABLE;
        }

        @Override
        public void doGetErrors(Request request, Response response) {
            Thread currentThread = Thread.currentThread();
            currentThread.setName("Provider "+getName()+" prosess "+request.getFile().getAbsolutePath()); // NOI18N
            getErrorsImpl(request, response);
        }

        @Override
        public String getDisplayName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getDescription() {
            throw new UnsupportedOperationException();
        }
   }

    private static class SynchronousMerger extends BaseMerger {
        
        @Override
        public void getErrorsImpl(Request request, Response response) {
            for( CsmErrorProvider provider : res.allInstances() ) {
                if (request.isCancelled()) {
                    break;
                }
                provider.getErrors(request, response);
            }
        }

        @Override
        public String getName() {
            return "synchronous-merger"; // NOI18N
        }

        @Override
        public boolean isSupportedEvent(EditorEvent kind) {
            return kind == EditorEvent.DocumentBased || kind == EditorEvent.FileBased;
        }
    }

    private static class AsynchronousMerger extends BaseMerger {

        @Override
        public void getErrorsImpl(final Request request, final Response response) {
            final Collection<RequestProcessor.Task> tasks = new ArrayList<RequestProcessor.Task>();
            for( final CsmErrorProvider provider : res.allInstances() ) {
                if (request.isCancelled()) {
                    break;
                }
                RequestProcessor.Task task = RP.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!request.isCancelled()){
                            try {
                                provider.getErrors(request, response);
                                if (TRACE_TASKS) {System.err.println("finish "+provider);} //NOI18N
                            } catch (AssertionError ex) {
                                ex.printStackTrace(System.err);
                            } catch (Exception ex) {
                                ex.printStackTrace(System.err);
                            }
                        }
                    }
                });
                tasks.add(task);
            }
            for (RequestProcessor.Task task : tasks) {
                task.waitFinished();
            }
            if (TRACE_TASKS) {System.err.println("finish all  providers");} //NOI18N
        }

        @Override
        public String getName() {
            return "asynchronous-merger"; // NOI18N
        }

        @Override
        public boolean isSupportedEvent(EditorEvent kind) {
            return kind == EditorEvent.DocumentBased || kind == EditorEvent.FileBased;
        }
    }
    
    /** default instance */
    private static final CsmErrorProvider DEFAULT = ASYNC ? new AsynchronousMerger() : new SynchronousMerger();

    // for testing only
    public static final void getAllErrors(Request request, Response response) {
        DEFAULT.getErrors(request, response);
    }

    // for testing only
    public static CsmErrorProvider getDefault() {
        return DEFAULT;
    }    
    
    /**
     * Determines whether this file contains part of some declaration,
     * i.e. whether it was included in the middle of some other declaration
     */
    public static boolean isPartial(CsmFile isIncluded, Set<CsmFile> antiLoop) {
        if (antiLoop.contains(isIncluded)) {
            return false;
        }
        antiLoop.add(isIncluded);
        //Collection<CsmFile> files = CsmIncludeHierarchyResolver.getDefault().getFiles(isIncluded);
        Collection<CsmReference> directives = CsmIncludeHierarchyResolver.getDefault().getIncludes(isIncluded);
        for (CsmReference directive : directives) {
            if (directive != null  ) {
                int offset = directive.getStartOffset();
                CsmFile containingFile = directive.getContainingFile();
                if (containingFile != null) {
                    if (CsmSelect.hasDeclarations(containingFile)) {
                        CsmFilter filter = CsmSelect.getFilterBuilder().createOffsetFilter(offset);
                        Iterator<CsmOffsetableDeclaration> declarations = CsmSelect.getDeclarations(containingFile, filter);
                        if (declarations.hasNext()) {
                            return true;
                        }
                    } else {
                        if (isPartial(containingFile, antiLoop)) {
                            return true;
                        }
                    }
                }
            }
        }
	return false;
    }

}
