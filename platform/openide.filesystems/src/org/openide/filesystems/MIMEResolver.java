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
package org.openide.filesystems;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;
import org.netbeans.modules.openide.filesystems.declmime.MIMEResolverImpl;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.lookup.ServiceProvider;

/**
 * Use the {@link FileUtil#getMIMEType(org.openide.filesystems.FileObject, java.lang.String[])} 
 * to invoke the mime resolving infrastructure.
 * All registered mime resolvers are looked up and asked one by one
 * to {@link #findMIMEType(org.openide.filesystems.FileObject)  resolve MIME type} 
 * of passed in {@link FileObject}. Resolving is finished right after
 * a resolver is able to resolve the FileObject or if all registered
 * resolvers returned <code>null</code> (not recognized).
 * <p>
 * Use {@link ExtensionRegistration}, 
 * {@link MIMEResolver.NamespaceRegistration} or {@link MIMEResolver.Registration}
 * to register declarative resolvers. 
 * <p>
 * In the rarely case, when declarative resolvers are not sufficient, you can
 * register subclass of {@link MIMEResolver} directly by using {@link ServiceProvider}
 * annotation.
 *
 * @author  rmatous
 */
public abstract class MIMEResolver {

    private String[] resolvableMIMETypes = null;

    /** Creates a new MIMEResolver.
     * @param mimeTypes an array of MIME types which can be resolved by this resolver.
     * It should contain all MIME types which {@link #findMIMEType} can return.
     * If something is missing, this resolver can be ignored, when searching for that
     * missing MIME type (see {@link FileUtil#getMIMEType(FileObject, String...)}).
     * @since 7.13
     */
    public MIMEResolver(String... mimeTypes) {
        Parameters.notNull("mimeTypes", mimeTypes);  //NOI18N
        if(mimeTypes.length == 0) {
            throw new IllegalArgumentException("The mimeTypes parameter cannot be empty array.");  //NOI18N
        }
        for (String mimeType : mimeTypes) {
            if(mimeType == null || mimeType.length() == 0) {
                throw new IllegalArgumentException("The item in mimeTypes parameter cannot be null nor empty String.");  //NOI18N
            }
        }
        resolvableMIMETypes = mimeTypes;
    }

    /** Creates a new MIMEResolver. 
     * @deprecated Use {@link #MIMEResolver(String...)} instead. Declaring MIME
     * types which can only be resolved by this resolver helps to speed up IDE.
     */
    @Deprecated
    public MIMEResolver() {
    }

    /**
     * Resolves FileObject and returns recognized MIME type
     * @param fo is FileObject which should be resolved (This FileObject is not
     * thread safe. Also this FileObject should not be cached for later use)
     * @return  recognized MIME type or null if not recognized
     */
    public abstract String findMIMEType(FileObject fo);
    
    /** Returns an array of MIME types which can be resolved by this resolver.
     * @return a non-empty array of MIME types
     */
    String[] getMIMETypes() {
        return resolvableMIMETypes;
    }

    /** factory method for {@link MIMEResolver.Registration} */
    static MIMEResolver create(FileObject fo) throws IOException {
        return MIMEResolverImpl.create(fo);
    }

    /** Internal support for implementors of MIME resolver UIs. 
     * 
     * @since 7.34
     */
    public abstract class UIHelpers {

        /** Throws an exception. Allows instantiation only by known subclasses.
         * @throws IllegalStateException
         */
        protected UIHelpers() {
            if (getClass().getName().equals("org.netbeans.core.ui.options.filetypes.FileAssociationsModel")) { // NOI18N
                // only core.ui is allowed to use methods of this class.
                return;
            }
            throw new IllegalStateException();
        }

        /**
         * Stores declarative resolver corresponding to specified mapping of MIME type
         * and set of extensions. This resolver has the highest priority. Usually
         * it resides in userdir/config/Servicer/MIMEResolver.
         * <p><strong>Not intended for use by modules outside the NetBeans Platform.</strong>
         * @param mimeToExtensions mapping of MIME type to set of extensions like
         * {@code {image/jpeg=[jpg, jpeg], image/gif=[]}}
         * @since org.openide.filesystems 7.34
         */
        protected final void storeUserDefinedResolver(final Map<String, Set<String>> mimeToExtensions) {
            MIMEResolverImpl.storeUserDefinedResolver(mimeToExtensions);
        }

        /**
         * Lists registered MIMEResolver instances in reverse order,
         * i.e. first are ones with lower priority (position attribute higher)
         * and last are ones with highest prority (position attribute lower).
         * <p><strong>Not intended for use by modules outside the NetBeans Platform.</strong>
         * @return list of all registered MIME resolver definitions in reverse order
         * @since org.openide.filesystems 7.34
         */
        protected final Collection<? extends FileObject> getOrderedResolvers() {
            return MIMEResolverImpl.getOrderedResolvers();
        }

        /**
         * Checks whether a given resolver is user-defined.
         * <p><strong>Not intended for use by modules outside the NetBeans Platform.</strong>
         * @param mimeResolverFO resolver definition
         * @return true if the specified file is a user-defined MIME resolver, false otherwise
         * @since org.openide.filesystems 7.34
         */
        protected final boolean isUserDefined(FileObject mimeResolverFO) {
            return MIMEResolverImpl.isUserDefined(mimeResolverFO);
        }

        /**
         * Returns mapping of MIME type to set of extensions.
         * <p><strong>Not intended for use by modules outside the NetBeans Platform.</strong>
         * @param fo MIMEResolver definition
         * @return mapping of MIME type to set of extensions like
         * {@code {image/jpeg=[jpg, jpeg], image/gif=[]}} (never null but may be empty)
         * @since org.openide.filesystems 7.34
         */
        protected final Map<String, Set<String>> getMIMEToExtensions(FileObject fo) {
            return MIMEResolverImpl.getMIMEToExtensions(fo);
        }
        
    }

    /** Often a mime type can be deduced just by looking at a file extension.
     * If that is your case, this annotation is for you. It associates
     * extension(s) with provided mime type.
     * <p>
     * If this kind of registration seems to trivial you can try 
     * {@link MIMEResolver.NamespaceRegistration} or {@link MIMEResolver.Registration}.
     * @since 7.58
     */
    @Retention(RetentionPolicy.SOURCE)
    public @interface ExtensionRegistration {
        /** Display name to present this type of objects to the user.
         */
        public String displayName();
        /** Mime type to be assigned to files with {@link #extension}.
         */
        public String mimeType();
        /** One or few extensions that should be recognized as given
         * {@link #mimeType}.
         */
        public String[] extension();
        /** In case ordering of mime resolvers is important, one can 
         * specify it by defining their {@link org.openide.filesystems.annotations.LayerBuilder.File#position(int) position}.
         */        
        public int position() default Integer.MAX_VALUE;
        /**
         * Show file filters for this MIME type. Set localized names of file
         * filters that should contain files resolved by this resolver.
         *
         * @see <a href="@org-openide-filesystems-nb@/org/openide/filesystems/FileChooserBuilder.html#addDefaultFileFilters--" >FileChooserBuilder#addDefaultFileFilters()</a>
         * @since 8.1
         */
        public String[] showInFileChooser() default {};
    }

    /** Recognize mime type by looking into header of XML files. The
     * file will be recognized by following rules:
     * <ul>
     * <li>If the actual extension is 
     *   among {@link #acceptedExtension()} list, then the recognition
     *   is over and {@link #mimeType()} is assigned to the file.
     * </li>
     * <li>If the extension is not among the list of {@link #checkedExtension()
     * extensions to check} (which by default contains <code>"xml"</code>), then
     * the recognition is over without recognizing any mime type.
     * </li>
     * <li>
     * If the document contains one of requested {@link #doctypePublicId public ID},
     * then the mime type is recognized and the recognition ends.
     * </li>
     * <li>
     * If the root element is not equal to specified {@link #elementName()},
     * then the recognition is over without recognizing the mime type.
     * </li>
     * <li>
     * If the list of {@link #elementNS()} namespaces is specified and the
     * namespace of the root element is not among the list, then the recognition
     * is over and no mime type is recognized.
     * </li>
     * <li>
     * If all the above, passed OK, the {@link #mimeType()} is recognized.
     * </li>
     * </ul>
     * If you want to recognize file just by its extension consider
     * using {@link MIMEResolver.ExtensionRegistration}.
     * @since 7.58
     */
    @Retention(RetentionPolicy.SOURCE)
    public @interface NamespaceRegistration {
        /** Display name to present this type of objects to the user.
         */
        public String displayName();
        /** Mime type to be assigned to files that match description of this
         * annotation.
         */
        public String mimeType();
        /** In case ordering of mime resolvers is important, one can 
         * specify it by defining their {@link org.openide.filesystems.annotations.LayerBuilder.File#position(int) position}.
         */        
        public int position() default Integer.MAX_VALUE;
        
        /** The extension that is automatically accepted. If specified,
         * then a file with such extension automatically leads to recognition
         * of defined {@link #mimeType()}.
         */
        public String[] acceptedExtension() default {};
        /** The extension of the file to check. By default only <em>.xml</em>
         * files are tested for content. One can change the extension of the file
         * by changing the value of this attribute. Assigning an empty array
         * to attribute turns the content check on every file
         */
        public String[] checkedExtension() default { "xml" };
        
        /** Name of root element. If at least one of the provided element names matches
         * the real element name, this condition is satisfied.
         * @return one or more element name. May be left empty, if element
         *      name should not be checked at all.
         */
        public String elementName() default "";
        /** Accept only elements that have specific namespace. If this 
         * attribute is specified, then only such {@link #elementName() elements} 
         * are accepted that have the right namespace.
         */
        public String[] elementNS() default {};
        
        /** If this attribute is specified, the system will check whether
         * at least one of the specified doctype's public ids, is used in
         * the document.
         */
        public String[] doctypePublicId() default {};
    }
    
    /** Registration that allows effective, declarative registration of 
     * complex {@link MIMEResolver mime resolvers}. 
     * For simpler cases rather consider using
     * {@link MIMEResolver.NamespaceRegistration} or 
     * {@link MIMEResolver.ExtensionRegistration}.
     * 
     * <p>
     * The <code>resource</code>
     * attribute of the annotation should be a relative reference to
     * an XML like <a href="doc-files/HOWTO-MIME.html">document</a> describing
     * the rules that will be interpreted by the mime recognizing infrastructure.
     * <pre>
     * {@code @}{@link org.openide.util.NbBundle.Messages}({
     *    "NICE_NAME=Nice name!"
     * })
     * {@code @}MIMEResolver.Registration(
     *   displayName="#NICE_NAME"
     *   resource="<a href="doc-files/HOWTO-MIME.html">your-resolver-definition.xml</a>"
     * )
     * class AnyClassYouHave {
     *   // ...
     * }
     * </pre>
     * The definition is pre-processed during compile time in order to eliminate
     * XML parsing during execution.
     * 
     * @since 7.58
     */
    @Retention(RetentionPolicy.SOURCE)
    public @interface Registration {
        /** {@link LayerBuilder#absolutizeResource(javax.lang.model.element.Element, java.lang.String) Relative path} 
         * to resource XML file describing
         * the <a href="doc-files/HOWTO-MIME.html">mime recognition rules</a>.
         */
        public String resource();

        /** In case ordering of mime resolvers is important, one can 
         * specify it by defining their {@link org.openide.filesystems.annotations.LayerBuilder.File#position(int) position}.
         */        
        public int position() default Integer.MAX_VALUE;
        
        /** Display name to present this type of objects to the user.
         */
        public String displayName();
        /**
         * Show file filters for registered MIME types. Set localized names of
         * file filters that should accept files resolved by this resolver. Only
         * types resolved by file name or extension (not by file content) are
         * supported.
         *
         * @see <a href="@org-openide-filesystems-nb@/org/openide/filesystems/FileChooserBuilder.html#addDefaultFileFilters--" >FileChooserBuilder#addDefaultFileFilters()</a>
         * @since 8.1
         */
        public String[] showInFileChooser() default {};
    }
}
