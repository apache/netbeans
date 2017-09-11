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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
    public static abstract class Factory {
        
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

