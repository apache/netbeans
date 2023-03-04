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


package org.netbeans.modules.i18n;

import java.io.IOException;
import javax.swing.JPanel;
import javax.swing.text.StyledDocument;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;

/**
 * Abstract class. Supports 'one i18n session' -> i18n-zing of one source file.
 * Used as a base class for concrete support implementations.
 *
 * @author  Peter Zavadsky
 */
public abstract class I18nSupport {

    /** <code>I18nFinder</code>. */
    private I18nFinder finder;

    /** <code>I18nReplacer</code>. */
    private I18nReplacer replacer;

    /** <code>DataObject</code> which document the i18n session will run thru. */
    protected DataObject sourceDataObject;

    /** Document on which the i18n-session will be performed. */
    protected StyledDocument document;
    
    /** Resource holder for sepcific subclass instance. */
    protected final ResourceHolder resourceHolder;
    

    /** Constructor. Note: It gets document from data object's editor cookie. If the document
     * is not available it tries to load it. Therefore this construction could take some time.
     * @exception IOException when the document is could not be loaded
     */
    public I18nSupport(DataObject sourceDataObject) {
        this.sourceDataObject = sourceDataObject;
        
        EditorCookie editorCookie = sourceDataObject.getCookie(EditorCookie.class);
        
        if (editorCookie == null) {
            throw new IllegalArgumentException("I18N: Illegal data object type"+ sourceDataObject); // NOI18N
        }
        
        this.document = editorCookie.getDocument();
        
        this.resourceHolder = createResourceHolder();
    }
    

    /** Loads document if was not get in constructor should be called after creation if the
     * work on document is necessary. */
    private void loadDocument() throws IOException {
        if (document == null) {
            EditorCookie editorCookie = sourceDataObject.getCookie(EditorCookie.class);

            if (editorCookie == null) {
                throw new IllegalArgumentException("I18N: Illegal data object type"+ sourceDataObject); // NOI18N
            }
            
            document = editorCookie.openDocument();
        }
    }
    
    /** Creates <code>I18nFinder</code> instance used by this instance. */
    protected abstract I18nFinder createFinder();
    
    /** Cretates <code>I18nReplacer</code> instance used by this instance. */
    protected abstract I18nReplacer createReplacer();
    
    /** Creates <code>ResourceHolder<code> for this instance. */
    protected abstract ResourceHolder createResourceHolder();

    /** Gets <code>I18nFinder</code>. */
    public final I18nFinder getFinder() {
        if (finder == null) {
            finder = createFinder();
        }
        return finder;
    }
    
    /** Gets <code>I18nReplacer</code> for this support. */
    public final I18nReplacer getReplacer() {
        if (replacer == null) {
            replacer = createReplacer();
        }
        return replacer;
    }

    /** Getter for <code>sourceDataObject</code> property. */
    public final DataObject getSourceDataObject() {
        return sourceDataObject;
    }
    
    /** Getter for <code>document</code> property. */
    public final StyledDocument getDocument() {
        return document;
    }
    
    /** Gets default <code>I18nString</code> instance for this i18n session,
     * has a non-null resource holder field, but resource of that holder may be not initialized yet. */
    public I18nString getDefaultI18nString() {
        return getDefaultI18nString(null);
    }
    
    /** Gets default <code>I18nString</code> for this instance. */
    public abstract I18nString getDefaultI18nString(HardCodedString hcString);
    
    /** Gets JPanel showing info about found hard coded value. */
    public abstract JPanel getInfo(HardCodedString hcString);

    /** Getter for <code>resourceHolder</code>. */
    public ResourceHolder getResourceHolder() {
        return resourceHolder;
    }

    /** Gets <code>PropertyPanel</code> which is used for customizing <code>i18nStrings</code>
     * linked to this support instance. Is possible to override in subclasses if any additional customization is needed. */
    public PropertyPanel getPropertyPanel() {
        return new PropertyPanel();
    }
    
    /** Indicates if supports customizer for additional source specific values. Override in subclasses if nedded.
     * @return false 
     * @see #getAdditionalCustommizer */
    public boolean hasAdditionalCustomizer() {
        return false;
    }
    
    /** Gets additional customizer. Override in subclasses if needed.
     * @return null 
     * @see #hasAdditionalCustomizer */
    public JPanel getAdditionalCustomizer() {
        return null;
    }
    
    /** Provides addtional changes. Usually connected with values
     * set via <code>getAdditionalCustomizer</code> method. Override in subclasses. 
     * Default implementation does nothing. */
    public void performAdditionalChanges() {
    }

    
    /**
     * Interface for finder which will search for hard coded (non-i18n-ized)
     * string in i18n-ized source document.
     */
    public interface I18nFinder {

        /** Gets next hard coded string. Starts from the beginning of the source.
         * @return next hard coded string or null if the search reached end. */
        public HardCodedString findNextHardCodedString();

        /** Gets all hard coded strings from document. 
         * @return all hard coded strings from source document or null if such don't exist */
        public HardCodedString[] findAllHardCodedStrings();
        
        /** Gets next hard coded but i18n-ized string. Starts from the beginning of the source. Used in test tool.
         * @return next hard coded string or null if the search reached end. */
        public HardCodedString findNextI18nString();

        /** Gets all i18n-zed hard coded strings from document.  Used in test tool.
         * @return all hard coded strings from source document or null if such don't exist */
        public HardCodedString[] findAllI18nStrings();
    }

    /**
     * Interface implemented by objects which replaces (i18n-zes) hard coded strings
     * source specific way. (The way of i18n-zing source is different from java file to
     * jsp file etc.)
     */
    public interface I18nReplacer {

        /** Replaces hard coded string using settigns encapsulated by <code>I18nString</code> typically customized by user. */
        public void replace(HardCodedString hardString, I18nString i18nString);
    }

    /** Factory inteface for creating {@code I18nSupport} instances. */
    public abstract static class Factory {
        
        /** Gets <code>I18nSupport</code> instance for specified data object and document.
         * @exception IOException when the document could not be loaded */
        public I18nSupport create(DataObject dataObject) throws IOException {
            I18nSupport support = createI18nSupport(dataObject);
            support.loadDocument();
          
            return support;
        };
        
        /** Actually creates i18n support instance. */
        protected abstract I18nSupport createI18nSupport(DataObject dataObject);
        
        /** Gets class type of <code>DataObject</code> which can to internationalize. */
        public abstract Class getDataObjectClass();
        
    } // End of nested Factory class.
    
}

