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

package org.netbeans.modules.openfile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.filesystems.MIMEResolver;

/**
 * {@code OpenFileDialogFilter} is an abstract class used by {@link FileChooser}
 * for filtering the set of files shown to the user in the <i>Open</i> dialog -
 * <br />
 * <b>Main Menu ► File ► Open File...</b>
 * <p>
 * This class is designed to be used as an abstraction of the service provider
 * in the NetBeans modules that provide opening of the standalone files via the
 * Open dialog.<br />
 * To add a filter to the list "<b>Files of type:</b>"  of the <i>Open</i>
 * dialog the module should declare a non-abstract successor of this class, and
 * annotate it as a service provider like this:
 * <pre>
 * &#x40;ServiceProvider(service=org.netbeans.modules.openfile.OpenFileDialogFilter.class)
 * public class AFileTypeFilter extends OpenFileDialogFilter {
 *     ...
 *     // implementation of the class
 *     ...
 * }
 * </pre>
 * Note, if you plan to implement the service provider as a nested class then it
 * should be <i>static nested class</i>, not an <i>inner class</i>.
 * </p>
 * <p>
 * Unlike the {@link javax.swing.filechooser.FileNameExtensionFilter}, the
 * default implementation of the {@code OpenFileDialogFilter} does not use file
 * <em>extensions</em>, but it just tests whether the file name ends with one of
 * specified <em>suffix</em>es. It lets to check not only a file name suffix,
 * but whole file name.
 * <p><b>Example</b>:
 * <pre>
 * &#x40;ServiceProvider(service=OpenFileDialogFilter.class)
 * public class ReadMeFilesFilter extends OpenFileDialogFilter {
 *
 *     &#x40;Override
 *     public String getDescriptionString() {
 *         return NbBundle.getMessage(getClass(), "OFDFD_ReadMe"); // NOI18N
 *     }
 *
 *     &#x40;Override
 *     public String[] getSuffixes() {
 *         return new String[] {"read.me", ".readme", "readme.txt"}; // NOI18N
 *     }
 *
 * }
 * </pre>
 * </p>
 * <p><b>Note:</b><br />
 * If behavior of the file
 * filter should be exactly the same as implemented by the
 * {@link FileNameExtensionFilter } class then, please use the class
 * {@link OpenFileDialogFilter.ExtensionFilter } instead.</p>
 *
 * @see javax.swing.filechooser.FileFilter
 * @see javax.swing.filechooser.FileNameExtensionFilter
 * @see FileChooser
 * @see OpenFileDialogFilter.ExtensionFilter
 *
 * @author Victor G. Vasilyev <vvg@netbeans.org>
 * @deprecated Use {@link MIMEResolver.Registration} and its parameter
 * {@code showInFileChooser}.
 */
@Deprecated
public abstract class OpenFileDialogFilter extends FileFilter {

    /**
     * Whether the given file is accepted by this filter.
     * <p>
     * To make a decision about acceptability of the file the default
     * implementation of this method relies on a result returned by the method
     * {@code compareSuffixes(String fileName, String suffix)}. <p />
     *
     * @param file the file under test.
     * @return  <code>true</code> if the file's name ends with one of the
     *          strings specified by the constructor or if the file
     *          is a directory, <code>false</code> otherwise.
     *
     * @see #compareSuffixes(String fileName, String suffix)
     */
    @Override
    public boolean accept(File file) {
        if (file != null) {
            if (file.isDirectory()) {
                return true;
            }
            for (String ext : getSuffixes()) {
                if (compareSuffixes(file.getName(), ext)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Compares <code>fileName</code> and <code>suffix</code>.
     * <p>
     * Default implementation of this method compares <code>fileName</code>
     * to <code>suffix</code>, ignoring case considerations.
     * The <code>fileName</code> and the <code>suffix</code> are
     * considered equal ignoring case if <code>fileName</code> ends with
     * <code>suffix</code> and corresponding characters in the suffix
     * part are equal ignoring case. <br />
     * To change this logic a successur class should override this method.
     * </p>
     *
     * @param fileName a file name
     * @param suffix an extension from a collection obtained via the
     * {@link #getExtensions()} method.
     *
     * @return <code>true</code> if <code>fileName</code> matches with
     *         <code>suffix</code>, otherwaise <code>true</code>.
     */
    public boolean compareSuffixes(String fileName, String suffix) {
        return fileName.toUpperCase().endsWith(suffix.toUpperCase());
    }

    /**
     * Returns a formatted string containing both the filter description and
     * the list of applicable file name suffixes associated with this filter.
     *
     * <p>
     * Note, it is final method. To define the filter description 
     * a successor class should override the method
     * {@link #getDescriptionString() }. </p>
     * @return a description string of the file filter that will be visible in
     * the Open dialog.
     */
    @Override
    public final String getDescription() {
        StringBuilder sb = new StringBuilder(getDescriptionString());
        sb.append(" ");
        sb.append(getSuffixesList());
        return sb.toString();
    }


    /**
     * Returns the <u>localized</u> string of the filter descriptor.
     * <p>
     * A successor class should override this method, and it might be
     * implemented as the following: <pre>
     * &#x40;Override
     * public String getDescription() {
     *     return NbBundle.getMessage(XFilesFilter.class, "OFDFD_XFiles");
     * }
     * </pre>
     * where "OFDFD_XFiles" is a name of the Open File Dialog Filter
     * Description (<b>OFDFD</b>) property defined in a
     * resource bundle file (e.g. {@code Bundle.properties}) suitable for
     * internationalizing. </p>
     *
     * <p><b>Important note:</b><br />
     * Value of the OFDFD property <b>MUST NOT</b> list the suffixes, nor
     * extensions of the files, because the implementation will automatically
     * add this to the description in the uniform manner,
     * i.e. the OFDFD should define
     * <pre>Java Files</pre>
     * but not something like
     * <pre>Java Files (*.java)</pre></p>
     *
     * @return a non-null description string of the file filter without a list of
     * suffixes/extensions.
     */
    public abstract String getDescriptionString();

    /**
     * Returns acceptable file name suffixes.
     *   
     * @return an array of the non-null strings that contains file name suffixes.
     */
    public abstract String[] getSuffixes();

    /**
     * Returns a string representation of the {@code OpenFileDialogFilter}.
     * This method is intended to be used for debugging purposes,
     * and the content and format of the returned string may vary
     * between implementations.
     *
     * @return a string representation of this {@code OpenFileDialogFilter}
     */
    @Override
    public String toString() {
        return super.toString() + "[description=" + getDescription() +
            " suffixes=" + getSuffixesList() + "]";
    }

    private String getSuffixesList() {
        return Arrays.asList(getSuffixes()).toString();
    }

    /**
     * An abstraction extending the {@link OpenFileDialogFilter } class, and
     * intended to be used as a service provider. This implementation wraps
     * an instance of the {@link FileNameExtensionFilter } class.
     *
     * <p><b>Example:</b>
     * <pre>
     * &#x40;ServiceProvider(service=OpenFileDialogFilter.class)
     * public class JavaFilesFilter extends OpenFileDialogFilter.ExtensionFilter {
     *
     *     &#x40;Override
     *     public FileNameExtensionFilter getFilter() {
     *         return new FileNameExtensionFilter(
     *                       NbBundle.getMessage(this.getClass(), "OFDFD_Java"),
     *                       "java"); // NOI18N
     *     }
     * }
     * </pre>
     *
     * @see OpenFileDialogFilter
     * @see FileNameExtensionFilter
     * @deprecated Use {@link MIMEResolver.ExtensionRegistration} and its
     * parameter {@code showInFileChooser}.
     */
    @Deprecated
    public abstract static class ExtensionFilter extends OpenFileDialogFilter {

        private static final char EXTENSION_SEPARATOR = '.';

        /**
         * Returns an instance of the {@link FileNameExtensionFilter } class.
         * 
         * @return an initialized non-null instance of the
         * {@link FileNameExtensionFilter } class.
         */
        public abstract FileNameExtensionFilter getFilter();

        @Override
        public boolean accept(File file) {
            return getFilter().accept(file);
        }

        @Override
        public String toString() {
            return getFilter().toString();
        }

        @Override
        public String getDescriptionString() {
            return getFilter().getDescription();
        }

        @Override
        public String[] getSuffixes() {
            ArrayList<String> suffixList = new ArrayList<>();
            for(String extension : getFilter().getExtensions()) {
                suffixList.add(EXTENSION_SEPARATOR + extension);
            }
            return suffixList.toArray(new String[] {});
        }

    } // End of ExtensionFilter

}
