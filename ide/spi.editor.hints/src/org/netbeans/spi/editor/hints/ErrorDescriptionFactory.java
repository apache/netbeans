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
package org.netbeans.spi.editor.hints;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
     * Should be called inside document read lock to assure consistency
     *
     * @param id an optional ID of the {@link ErrorDescription}. Should represent a "type" of an error/warning.
     *           It is recommended that providers prefix the ID with their unique prefix.
     * @param severity the desired {@link Severity}
     * @param customType
     * @param description the text of the error/warning
     * @param details optional "more details" describing the error/warning
     * @param fixes a collection of {@link Fix}es that should be shown for the error/warning
     * @param file for which the {@link ErrorDescription} should be created
     * @param starts the array of start offsets for error/warning
     * @param ends the array of end offsets for error/warning
     * @return a newly created {@link ErrorDescription} based on the given parameters
     * @since 1.42
     */
    public static @NonNull ErrorDescription createErrorDescription(@NullAllowed String id, @NonNull Severity severity, 
            @NullAllowed String customType, @NonNull String description, @NullAllowed CharSequence details, @NonNull LazyFixList fixes, 
            @NonNull FileObject file, int[] starts, int[] ends) {
        Parameters.notNull("severity", severity);
        Parameters.notNull("description", description);
        Parameters.notNull("fixes", fixes);
        Parameters.notNull("file", file);
        Parameters.notNull("starts", starts);
        Parameters.notNull("ends", ends);        
        if (starts.length == 0 || starts[0] < 0) throw new IndexOutOfBoundsException("start < 0 (" + starts + " < 0)");//NOI18N
        if ( ends.length == 0 || ends[0] < 0) throw new IndexOutOfBoundsException("end < 0 (" + ends + " < 0)");//NOI18N
        if (ends.length != starts.length) throw new IndexOutOfBoundsException("starts lentgh:" + starts.length + " != " + ends.length + " ends length");//NOI18N
        PositionBounds span = HintsControllerImpl.linePart(file, starts[0], ends[0]);
        ArrayList<PositionBounds> spanTail = new ArrayList<>();
        if (starts.length > 1) {
            for (int i = 1; i < starts.length; i++) {
                //just skip if starts greater or equals to end, no need to throw exception
                if (starts[i] >= ends[i]) {
                    //log and continue
                    Logger.getLogger(ErrorDescriptionFactory.class.getName()).log(Level.INFO, "Incorrect span,  start=" + starts[i] + ", end=" + ends[i], new Exception());//NOI18N
                    continue;
                }
                spanTail.add(HintsControllerImpl.linePart(file, starts[i], ends[i]));
            }
        }
        return new ErrorDescription(file, id, description, details, severity, customType, fixes, span, spanTail);
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
     * @param doc for which the {@link ErrorDescription} should be created
     * @param start start position of the error/warning
     * @param end end position of the error/warning
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
    
    /**Create a new {@link ErrorDescription} with the given parameters.
     *
     * @param id an optional ID of the {@link ErrorDescription}. Should represent a "type" of an error/warning.
     *           It is recommended that providers prefix the ID with their unique prefix.
     * @param severity the desired {@link Severity}
     * @param customType custom annotation type
     * @param description the text of the error/warning
     * @param details optional "more details" describing the error/warning
     * @param fixes a collection of {@link Fix}es that should be shown for the error/warning
     * @param doc document for which the {@link ErrorDescription} should be created
     * @param starts the array of start offsets for error/warning
     * @param ends the array of end offsets for error/warning
     * @return a newly created {@link ErrorDescription} based on the given parameters
     * @since 1.42
     */
    public static @NonNull ErrorDescription createErrorDescription(@NullAllowed String id, @NonNull Severity severity, 
            @NullAllowed String customType, @NonNull String description, @NullAllowed CharSequence details,  @NonNull List<Fix> fixes, 
            @NonNull Document doc, int[] starts, int[] ends) {
        Parameters.notNull("severity", severity);
        Parameters.notNull("description", description);
        Parameters.notNull("fixes", fixes);
        Parameters.notNull("doc", doc);
        Parameters.notNull("starts", starts);
        Parameters.notNull("ends", ends);                
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        FileObject file = od != null ? od.getPrimaryFile() : null;
        if (starts.length == 0 || starts[0] < 0) throw new IndexOutOfBoundsException("start < 0 (" + starts + " < 0)");//NOI18N
        if ( ends.length == 0 || ends[0] < 0) throw new IndexOutOfBoundsException("end < 0 (" + ends + " < 0)");//NOI18N
        if (ends.length != starts.length) throw new IndexOutOfBoundsException("starts lentgh:" + starts.length + " != " + ends.length + " ends length");//NOI18N
        PositionBounds span = HintsControllerImpl.linePart(file, starts[0], ends[0]);
        ArrayList<PositionBounds> spanTail = new ArrayList<>();
        if (starts.length > 1) {
            for (int i = 1; i < starts.length; i++) {
                //just skip if starts greater or equals to end
                if (starts[i] >= ends[i]) {
                    //log and continue
                    Logger.getLogger(ErrorDescriptionFactory.class.getName()).log(Level.INFO, "Incorrect span,  start=" + starts[i] + ", end=" + ends[i], new Exception());//NOI18N
                    continue;
                }
                spanTail.add(HintsControllerImpl.linePart(file, starts[i], ends[i]));
            }
        }
        return new ErrorDescription(file, id, description, details, severity, customType, new StaticFixList(fixes), 
                span, spanTail);
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
