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

package org.netbeans.modules.properties;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.openide.filesystems.FileObject;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.NbBundle;
import org.openide.util.io.SafeException;

/**
 * Data loader which recognizes properties files.
 * This class is final only for performance reasons,
 * can be unfinaled if desired.
 *
 * @author Ian Formanek, Petr Jiricka
 * @author Marian Petras
 */
public final class PropertiesDataLoader extends MultiFileLoader {

    /** Extension for properties files. */
    static final String PROPERTIES_EXTENSION = "properties"; // NOI18N

    /** Properties MIME type*/
    static final String PROPERTIES_MIME_TYPE = "text/x-properties"; //NOI18N

    /** Character used to separate parts of bundle properties file name */
    public static final char PRB_SEPARATOR_CHAR = '_';

    /** Generated serial version UID. */
    static final long serialVersionUID =4384899552891479449L;
    
    /** name of property with extensions */
    public static final String PROP_EXTENSIONS = "extensions"; // NOI18N
    
    /** */
    private Reference<PropertiesEncoding> encodingRef;
    
    /** */
    private static Set<String> knownLanguages;
    /** */
    private static Set<String> knownCountries;

    /** */
    private static boolean nestedView = false;

    /** Creates new PropertiesDataLoader. */
    public PropertiesDataLoader() {
        super("org.netbeans.modules.properties.PropertiesDataObject"); // NOI18N
        
        // Set extentions. Due performance reasons do it here instead in initialize method.
        // During startup it's in findPrimaryFile method called getExtensions method. If the 
        // extentions list was not set in constructor the initialize method would be called
        // during startup, but we want to avoid the initialize call since we don't need
        // actions and display name initialized during startup time.
        ExtensionList extList = new ExtensionList();
        extList.addExtension(PROPERTIES_EXTENSION);
        // Add .impl for CORBA module.
        extList.addExtension("impl"); // NOI18N
        setExtensions(extList);
    }

    /**
     */
    PropertiesEncoding getEncoding() {
        PropertiesEncoding encoding;
        if ((encodingRef == null) || ((encoding = encodingRef.get()) == null)) {
            encoding = new PropertiesEncoding();
            encodingRef = new SoftReference<PropertiesEncoding>(encoding);
        }
        return encoding;
    }
    
    /** */
    @Override
    protected String defaultDisplayName() {
        return NbBundle.getMessage(PropertiesDataLoader.class,
                                   "PROP_PropertiesLoader_Name");       //NOI18N
    }
    
    /**
     * This methods uses the layer action context so it returns
     * a non-<code>null</code> value.
     *
     * @return  name of the context on layer files to read/write actions to
     */
    @Override
    protected String actionsContext () {
        return "Loaders/text/x-properties/Actions/";                    //NOI18N
    }
    
    /**
     * @return  <code>PropertiesDataObject</code> for the specified
     *          <code>FileObject</code>
     */
    protected MultiDataObject createMultiObject(final FileObject fo)
            throws IOException {
        return new PropertiesDataObject(fo, this);
    }

    /** */
    protected FileObject findPrimaryFile (FileObject fo) {
        if (fo.isFolder()) {
            return null;
        }
        if (fo.getExt().equalsIgnoreCase(PROPERTIES_EXTENSION)) {
            
            /*
             * returns a file whose name is the shortest valid prefix
             * corresponding to an existing file
             */
            String fName = fo.getName();
            if (nestedView) {
                int index = fName.indexOf(PRB_SEPARATOR_CHAR);
                while (index != -1) {
                    FileObject candidate = fo.getParent().getFileObject(
                            fName.substring(0, index), fo.getExt());
                    if (candidate != null && isValidLocaleSuffix(fName.substring(index))) {
                        return candidate;
                    }
                    index = fName.indexOf(PRB_SEPARATOR_CHAR, index + 1);
                }
            }
            return fo;
        } else if (getExtensions().isRegistered(fo)) {
            return fo;
        } else if (PROPERTIES_MIME_TYPE.equalsIgnoreCase(fo.getMIMEType(PROPERTIES_MIME_TYPE))) {
            return fo;
        } else {
            return null;
        }
    }

    private static boolean isValidLocaleSuffix(String s) {
        // first char is _
        int n = s.length();
        String s1;
        // check first suffix - language (two chars)
        if (n == 3 || (n > 3 && s.charAt(3) == PRB_SEPARATOR_CHAR)) {
            s1 = s.substring(1, 3).toLowerCase();
            // language must be followed by a valid country suffix or no suffix
        } else {
            return false;
        }
        // check second suffix - country (two chars)
        String s2;
        if (n == 3) {
            s2 = null;
        } else if (n == 6 || (n > 6 && s.charAt(6) == PRB_SEPARATOR_CHAR)) {
            s2 = s.substring(4, 6).toUpperCase();
            // country may be followed by whatever additional suffix
        } else {
            return false;
        }
        
        if (knownLanguages == null) {
            knownLanguages = new HashSet<String>(Arrays.asList(Locale.getISOLanguages()));
        }
        if (!knownLanguages.contains(s1)) {
            return false;
        }

        if (s2 != null) {
            if (knownCountries == null) {
                knownCountries = new HashSet<String>(Arrays.asList(Locale.getISOCountries()));
            }
            if (!knownCountries.contains(s2)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return  <code>PropertiesFileEntry</code> for the given file
     */
    protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj,
                                                       FileObject primaryFile) {
        return new PropertiesFileEntry(obj, primaryFile);
    }

    /**
     * @return  <code>PropertiesFileEntry</code> for the given file
     */
    protected MultiDataObject.Entry createSecondaryEntry(
            MultiDataObject obj,
            FileObject secondaryFile) {
        return new PropertiesFileEntry(obj, secondaryFile);
    }
    

    /**
     * Sets the extension list for this data loader.
     * This data loader will then recognize all files having any extension
     * of the given list.
     *
     * @param  ext  list of extensions
     */
    public void setExtensions(ExtensionList ext) {
        putProperty(PROP_EXTENSIONS, ext, true);
    }

    /**
     * Get the extension list for this data loader.
     *
     * @return  list of extensions
     * @see  #setExtensions
     */
    public ExtensionList getExtensions() {
        ExtensionList l = (ExtensionList) getProperty(PROP_EXTENSIONS);
        if (l == null) {
            l = new ExtensionList();
            putProperty(PROP_EXTENSIONS, l, false);
        }
        return l;
    }

    /** Writes extensions to the stream.
    * @param oo ignored
    */
    @Override
    public void writeExternal (ObjectOutput oo) throws IOException {
        super.writeExternal (oo);

        oo.writeObject (getProperty (PROP_EXTENSIONS));
    }

    /** Reads nothing from the stream.
    * @param oi ignored
    */
    @Override
    public void readExternal (ObjectInput oi)
    throws IOException, ClassNotFoundException {
        SafeException se;
        try {
            super.readExternal (oi);
            se = null;
        } catch (SafeException se2) {
            se = se2;
        }

        setExtensions ((ExtensionList)oi.readObject ());
        if (se != null) {
            throw se;
        }
    }
    
}
