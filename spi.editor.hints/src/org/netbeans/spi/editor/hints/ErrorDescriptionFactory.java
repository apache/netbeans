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
package org.netbeans.spi.editor.hints;

import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.editor.hints.HintsControllerImpl;
import org.netbeans.modules.editor.hints.StaticFixList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.PositionBounds;
import org.openide.util.Parameters;

/**
 * Factory class with static methods that allow creation of ErrorDescription.
 * @author Jan Lahoda
 */
public class ErrorDescriptionFactory {

    /** No instances of this class are needed - all the API methods are static. */
    private ErrorDescriptionFactory() {
    }

    /**Create a new {@link ErrorDescription} with the given parameters.
     * 
     * Call from inside a document read lock to ensure the meaning of lineNumber
     * does not change while this method runs.
     * 
     * If the lineNumber is greater than the number of lines in the given document,
     * the very last line will be used.
     * 
     */
    public static @NonNull ErrorDescription createErrorDescription(@NonNull Severity severity, @NonNull String description, @NonNull Document doc, int lineNumber) {
        Parameters.notNull("severity", severity);
        Parameters.notNull("description", description);
        Parameters.notNull("doc", doc);
        return createErrorDescription(severity, description, new StaticFixList(), doc, lineNumber);
    }
    
    /**Create a new {@link ErrorDescription} with the given parameters.
     * 
     * Call from inside a document read lock to ensure the meaning of lineNumber
     * does not change while this method runs.
     * 
     * If the lineNumber is greater than the number of lines in the given document,
     * the very last line will be used.
     * 
     */
    public static @NonNull ErrorDescription createErrorDescription(@NonNull Severity severity, @NonNull String description, @NonNull List<Fix> fixes, @NonNull Document doc, int lineNumber) {
        Parameters.notNull("severity", severity);
        Parameters.notNull("description", description);
        Parameters.notNull("fixes", fixes);
        Parameters.notNull("doc", doc);
        return createErrorDescription(severity, description, new StaticFixList(fixes), doc, lineNumber);
    }
    
    /**Create a new {@link ErrorDescription} with the given parameters.
     * 
     * Call from inside a document read lock to ensure the meaning of lineNumber
     * does not change while this method runs.
     * 
     * If the lineNumber is greater than the number of lines in the given document,
     * the very last line will be used.
     * 
     */
    public static @NonNull ErrorDescription createErrorDescription(@NonNull Severity severity, @NonNull String description, @NonNull LazyFixList fixes, @NonNull Document doc, int lineNumber) {
        return createErrorDescription(null, severity, description, null, fixes, doc, lineNumber);
    }

    /**Create a new {@link ErrorDescription} with the given parameters.
     *
     * Call from inside a document read lock to ensure the meaning of lineNumber
     * does not change while this method runs.
     * 
     * If the lineNumber is greater than the number of lines in the given document,
     * the very last line will be used.
     * 
     * @param id an optional ID of the {@link ErrorDescription}. Should represent a "type" of an error/warning.
     *           It is recommended that providers prefix the ID with their unique prefix.
     * @param severity the desired {@link Severity}
     * @param description the text of the error/warning
     * @param details optional "more details" describing the error/warning
     * @param fixes a collection of {@link Fix}es that should be shown for the error/warning
     * @param doc document for which the {@link ErrorDescription} should be created
     * @param lineNumber line on which the error/warning should be shown
     * @return a newly created {@link ErrorDescription} based on the given parameters
     * @since 1.22
     */
    public static @NonNull ErrorDescription createErrorDescription(@NullAllowed String id, @NonNull Severity severity, @NonNull String description, @NullAllowed CharSequence details, @NonNull LazyFixList fixes, @NonNull Document doc, int lineNumber) {
        Parameters.notNull("severity", severity);
        Parameters.notNull("description", description);
        Parameters.notNull("fixes", fixes);
        Parameters.notNull("doc", doc);
        
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        FileObject file = od != null ? od.getPrimaryFile() : null;
        
        return new ErrorDescription(file, id, description, details, severity, fixes, HintsControllerImpl.fullLine(doc, lineNumber));
    }
    
    /**
     * Acquires read lock on the provided document to assure consistency
     */
    public static @NonNull ErrorDescription createErrorDescription(@NonNull Severity severity, @NonNull String description, @NonNull Document doc, @NonNull Position start, @NonNull Position end) {
        Parameters.notNull("severity", severity);
        Parameters.notNull("description", description);
        Parameters.notNull("doc", doc);
        Parameters.notNull("start", start);
        Parameters.notNull("end", end);
        
        return createErrorDescription(severity, description, new StaticFixList(), doc, start, end);
    }

    /**
     * Acquires read lock on the provided document to assure consistency
     */
    public static @NonNull ErrorDescription createErrorDescription(@NonNull Severity severity, @NonNull String description, @NonNull List<Fix> fixes, @NonNull Document doc, @NonNull Position start, @NonNull Position end) {
        Parameters.notNull("severity", severity);
        Parameters.notNull("description", description);
        Parameters.notNull("fixes", fixes);
        Parameters.notNull("doc", doc);
        Parameters.notNull("start", start);
        Parameters.notNull("end", end);
        
        return createErrorDescription(severity, description, new StaticFixList(fixes), doc, start, end);
    }
    
    /**
     * Acquires read lock on the provided document to assure consistency
     */
    public static @NonNull ErrorDescription createErrorDescription(@NonNull Severity severity, @NonNull String description, @NonNull LazyFixList fixes, @NonNull Document doc, @NonNull Position start, @NonNull Position end) {
        return createErrorDescription(null, severity, description, null, fixes, doc, start, end);
    }

    /**Create a new {@link ErrorDescription} with the given parameters.
     *
     * Acquires read lock on the provided document to assure consistency
     *
     * @param id an optional ID of the {@link ErrorDescription}. Should represent a "type" of an error/warning.
     *           It is recommended that providers prefix the ID with their unique prefix.
     * @param severity the desired {@link Severity}
     * @param description the text of the error/warning
     * @param details optional "more details" describing the error/warning
     * @param fixes a collection of {@link Fix}es that should be shown for the error/warning
     * @param doc document for which the {@link ErrorDescription} should be created
     * @param start starting offset of the error/warning
     * @param end ending offset of the error/warning
     * @return a newly created {@link ErrorDescription} based on the given parameters
     * @since 1.22
     */
    public static @NonNull ErrorDescription createErrorDescription(@NullAllowed String id, @NonNull Severity severity, @NonNull String description, @NullAllowed CharSequence details, @NonNull LazyFixList fixes, @NonNull Document doc, @NonNull Position start, @NonNull Position end) {
        Parameters.notNull("severity", severity);
        Parameters.notNull("description", description);
        Parameters.notNull("fixes", fixes);
        Parameters.notNull("doc", doc);
        Parameters.notNull("start", start);
        Parameters.notNull("end", end);
        
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        FileObject file = od != null ? od.getPrimaryFile() : null;
        
        return new ErrorDescription(file, id, description, details, severity, fixes, HintsControllerImpl.linePart(doc, start, end));
    }

    /**
     * Should be called inside document read lock to assure consistency
     */
    public static @NonNull ErrorDescription createErrorDescription(@NonNull Severity severity, @NonNull String description, @NonNull FileObject file, int start, int end) {
        Parameters.notNull("severity", severity);
        Parameters.notNull("description", description);
        Parameters.notNull("file", file);
        if (start < 0) throw new IndexOutOfBoundsException("start < 0 (" + start + " < 0)");
        if (end < start) throw new IndexOutOfBoundsException("end < start (" + end + " < " + start + ")");
        
        return createErrorDescription(severity, description, new StaticFixList(), file, start, end);
    }

    /**
     * Should be called inside document read lock to assure consistency
     */
    public static @NonNull ErrorDescription createErrorDescription(@NonNull Severity severity, @NonNull String description, @NonNull List<Fix> fixes, @NonNull FileObject file, int start, int end) {
        Parameters.notNull("severity", severity);
        Parameters.notNull("description", description);
        Parameters.notNull("fixes", fixes);
        Parameters.notNull("file", file);
        if (start < 0) throw new IndexOutOfBoundsException("start < 0 (" + start + " < 0)");
        if (end < start) throw new IndexOutOfBoundsException("end < start (" + end + " < " + start + ")");
        
        return createErrorDescription(severity, description, new StaticFixList(fixes), file, start, end);
    }
    
    /**
     * Should be called inside document read lock to assure consistency
     */
    public static @NonNull ErrorDescription createErrorDescription(@NonNull Severity severity, @NonNull String description, @NonNull LazyFixList fixes, @NonNull FileObject file, int start, int end) {
        return createErrorDescription(null, severity, description, null, fixes, file, start, end);
    }

    /**Create a new {@link ErrorDescription} with the given parameters.
     *
     * Should be called inside document read lock to assure consistency
     *
     * @param id an optional ID of the {@link ErrorDescription}. Should represent a "type" of an error/warning.
     *           It is recommended that providers prefix the ID with their unique prefix.
     * @param severity the desired {@link Severity}
     * @param description the text of the error/warning
     * @param details optional "more details" describing the error/warning
     * @param fixes a collection of {@link Fix}es that should be shown for the error/warning
     * @param file for which the {@link ErrorDescription} should be created
     * @param start starting offset of the error/warning
     * @param end ending offset of the error/warning
     * @return a newly created {@link ErrorDescription} based on the given parameters
     * @since 1.22
     */
    public static @NonNull ErrorDescription createErrorDescription(@NullAllowed String id, @NonNull Severity severity, @NonNull String description, @NullAllowed CharSequence details, @NonNull LazyFixList fixes, @NonNull FileObject file, int start, int end) {
        Parameters.notNull("severity", severity);
        Parameters.notNull("description", description);
        Parameters.notNull("fixes", fixes);
        Parameters.notNull("file", file);
        if (start < 0) throw new IndexOutOfBoundsException("start < 0 (" + start + " < 0)");
        if (end < start) throw new IndexOutOfBoundsException("end < start (" + end + " < " + start + ")");
        
        return new ErrorDescription(file, id, description, details, severity, fixes, HintsControllerImpl.linePart(file, start, end));
    }
    
    /**Create a new {@link ErrorDescription} with the given parameters.
     *
     * @param id an optional ID of the {@link ErrorDescription}. Should represent a "type" of an error/warning.
     *           It is recommended that providers prefix the ID with their unique prefix.
     * @param severity the desired {@link Severity}
     * @param description the text of the error/warning
     * @param details optional "more details" describing the error/warning
     * @param fixes a collection of {@link Fix}es that should be shown for the error/warning
     * @param file for which the {@link ErrorDescription} should be created
     * @param errorBounds start and end position of the error/warning
     * @return a newly created {@link ErrorDescription} based on the given parameters
     * @since 1.24
     */
    public static @NonNull ErrorDescription createErrorDescription(@NullAllowed String id, @NonNull Severity severity, @NonNull String description, @NullAllowed CharSequence details, @NonNull LazyFixList fixes, @NonNull FileObject file, @NonNull PositionBounds errorBounds) {
        Parameters.notNull("severity", severity);
        Parameters.notNull("description", description);
        Parameters.notNull("fixes", fixes);
        Parameters.notNull("file", file);
        
        return new ErrorDescription(file, id, description, details, severity, fixes, errorBounds);
    }
    
    /**Create a new {@link ErrorDescription} with the given parameters.
     *
     * @param id an optional ID of the {@link ErrorDescription}. Should represent a "type" of an error/warning.
     *           It is recommended that providers prefix the ID with their unique prefix.
     * @param severity the desired {@link Severity}
     * @param customType custom annotation type
     * @param description the text of the error/warning
     * @param details optional "more details" describing the error/warning
     * @param fixes a collection of {@link Fix}es that should be shown for the error/warning
     * @param file for which the {@link ErrorDescription} should be created
     * @param errorBounds start and end position of the error/warning
     * @return a newly created {@link ErrorDescription} based on the given parameters
     * @since 1.39
     */
    public static @NonNull ErrorDescription createErrorDescription(@NullAllowed String id, @NonNull Severity severity, @NullAllowed String customType, @NonNull String description, @NullAllowed CharSequence details,  @NonNull List<Fix> fixes, @NonNull Document doc, @NonNull Position start, @NonNull Position end) {
        Parameters.notNull("severity", severity);
        Parameters.notNull("description", description);
        Parameters.notNull("fixes", fixes);
        Parameters.notNull("doc", doc);
        Parameters.notNull("start", start);
        Parameters.notNull("end", end);
        
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        FileObject file = od != null ? od.getPrimaryFile() : null;
        
        return new ErrorDescription(file, id, description, details, severity, customType, new StaticFixList(fixes), HintsControllerImpl.linePart(doc, start, end));
    }

    /**
     * Converts "normal" list of {@link Fix}es into {@link LazyFixList}
     * @param fixes
     * @return lazy
     */
    public static @NonNull LazyFixList lazyListForFixes(@NonNull List<Fix> fixes) {
        Parameters.notNull("fixes", fixes);
        
        return new StaticFixList(fixes);
    }

    /**
     * Concatenates several {@link LazyFixList}s into one.
     * @param delegates the lists to be delegated to
     * @return one list to contain them all
     */
    public static @NonNull LazyFixList lazyListForDelegates(@NonNull List<LazyFixList> delegates) {
        Parameters.notNull("delegates", delegates);
        
        return new HintsControllerImpl.CompoundLazyFixList(delegates);
    }

    /**Attach given sub-fixes to the given fix. The sub-fixes may be shown as a
     * sub-menu for the given fix. Only one level of sub-fixes is currently supported
     * (attaching sub-fixes to any of the sub-fix will not have any effect). The sub-fixes
     * are held in memory as long as the given fix exists.
     *
     * @param to fix to which should be the sub-fixes attached
     * @param subfixes the sub-fixes to attach
     * @return the given fix
     * @since 1.13
     */
    public static @NonNull Fix attachSubfixes(@NonNull Fix to, @NonNull Iterable<? extends Fix> subfixes) {
        Parameters.notNull("to", to);
        Parameters.notNull("subfixes", subfixes);
        
        HintsControllerImpl.attachSubfixes(to, subfixes);
        return to;
    }
}
