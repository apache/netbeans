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

package org.openide.loaders;

import java.io.*;
import java.util.*;

import org.openide.filesystems.FileObject;
import org.openide.util.Enumerations;
import org.openide.util.Utilities;

/** Property class that collects a modifiable list of file extensions
* and permits checking of whether a name or a file object has a given extension.
* It comes with a property editor to allow the user to modify the extensions.
*
* @author Jaroslav Tulach
*/
public class ExtensionList extends Object
    implements Cloneable, java.io.Serializable {

    /** if true, ignore case of file extensions (not MIME types tho!) */
    private static final boolean CASE_INSENSITIVE =
            (Utilities.isWindows () || (Utilities.getOperatingSystem () == Utilities.OS_OS2)) || Utilities.getOperatingSystem() == Utilities.OS_VMS;

    /** set of extensions to recognize */
    private Set<String> list;
    /** set of mime types to recognize */
    private Set<String> mimeTypes;

    static final long serialVersionUID =8868581349510386291L;
    /** Default constructor.
    */
    public ExtensionList () {
    }

    /** Clone new object.
    */
    @Override
    public synchronized Object clone () {
        try {
            ExtensionList l = (ExtensionList)super.clone ();
            
            if (list != null) {
                l.list = new HashSet<String>();
                l.list.addAll (list);
            }
            
            if (mimeTypes != null) {
                l.mimeTypes = new HashSet<String>();
                l.mimeTypes.addAll(mimeTypes);
            }
            
            return l;
        } catch (CloneNotSupportedException ex) {
            // has to be supported we implement the right interface
            throw new InternalError ();
        }
    }

    /** Add a new extension.
    * @param ext the extension
    */
    public synchronized void addExtension (String ext) {
        if (list == null) {
            list = new HashSet<String>();
        }
        
        list.add (ext);
    }

    /** Remove an extension.
    * @param ext the extension
    */
    public void removeExtension (String ext) {
        if (list != null) {
            list.remove (ext);
        }
    }
    
    /** Adds new mime type.
    * @param mime the mime type
    */
    public synchronized void addMimeType (String mime) {
        if (mimeTypes == null) {
            mimeTypes = new HashSet<String>();
        }

        mimeTypes.add (mime);
    }
    
    /** Removes a mime type.
     * @param mime the name of the type
     */
    public void removeMimeType (String mime) {
        if (mimeTypes != null) {
            mimeTypes.remove(mime);
        }
    }

    /** Test whether the name in the string is acceptable.
    * It should end with a dot and be one of the registered extenstions.
    * @param s the name
    * @return <CODE>true</CODE> if the name is acceptable
    */
    public boolean isRegistered (String s) {
        if (list == null) {
            return false;
        }
      
        try {
            String ext = s.substring (s.lastIndexOf ('.') + 1);
            return list.contains (ext);
        } catch (StringIndexOutOfBoundsException ex) {
            return false;
        }
    }

    /** Tests whether the file object is acceptable.
    * Its extension should be registered.
    * @param fo the file object to test
    * @return <CODE>true</CODE> if the file object is acceptable
    */
    public boolean isRegistered (FileObject fo) {
        if (list != null && list.contains (fo.getExt ())) {
            return true;
        }

        if (mimeTypes != null) {
            String mime = fo.getMIMEType(mimeTypes.toArray(new String[0]));
            if (mimeTypes.contains(mime)) {
                return true;
            }
        }
        
        return false;
    }

    /** Get all extensions.
    * @return enumeration of extensions
    */
    public Enumeration<String> extensions() {
        return en (list);
    }
    
    /** Get all mime types.
     * @return enumeration of MIME types
     */
    public Enumeration<String> mimeTypes() {
        return en (mimeTypes);
    }
    
    @Override
    public String toString() {
        return "ExtensionList[" + list + mimeTypes + "]"; // NOI18N
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ExtensionList)) return false;
        ExtensionList e = (ExtensionList)o;
        return equalSets(list, e.list, CASE_INSENSITIVE) &&
               equalSets(mimeTypes, e.mimeTypes, false);
    }
    
    @Override
    public int hashCode() {
        int x = 0;
        if (list != null) x = normalizeSet(list, CASE_INSENSITIVE).hashCode();
        if (mimeTypes != null) x += normalizeSet(mimeTypes, false).hashCode();
        return x;
    }
    
    // Helper methods for equals/hashCode.
    // Note that these are unsorted sets; we don't care about order.
    private static boolean equalSets(Set<String> s1, Set<String> s2, boolean flattenCase) {
        if (s1 == null && s2 == null) return true; // quick return
        Set s1a = normalizeSet(s1, flattenCase);
        Set s2a = normalizeSet(s2, flattenCase);
        return s1a.equals(s2a);
    }
    private static Set<String> normalizeSet(Set<String> s, boolean flattenCase) {
        if (s == null || s.isEmpty()) return Collections.emptySet();
        if (flattenCase) {
            Set<String> s2 = new HashSet<String>(s.size() * 4 / 3 + 1);
            for (String item: s) {
                s2.add(item.toLowerCase(Locale.US));
            }
            return s2;
        } else {
            return s;
        }
    }
    
    /** Enumeration from set
     * @param set set or null
     */
    private static Enumeration<String> en(Collection<String> c) {
        if (c == null) {
            return Enumerations.empty();
        } else {
            return Collections.enumeration(createExtensionSet(c));
        }
    }
    
    /** Creates a set for holding the extensions. It is platform 
    * dependent whether case sensitive or insensitive.
    */
    private static SortedSet<String> createExtensionSet (Collection<String> clone) {
        TreeSet<String> t;
        if (CASE_INSENSITIVE) {
            t = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        } else {
            t = new TreeSet<String>();
        }
        t.addAll(clone);
        return t;
    }
    
    /** Backward compatibility settings read.
    */
    @SuppressWarnings("unchecked")
    private void readObject (ObjectInputStream ois) 
    throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField gf = ois.readFields();
        
        Object whichList = gf.get ("list", null); // NOI18N
        if (whichList instanceof Map) {
            // backward compatible serialization
            whichList = ((Map)whichList).keySet ();
        }
        
        if (whichList != null) {
            // have to reinsert everything because we could migrate from
            // different operating system and we might need to change
            // case-sensitivity
            this.list = createExtensionSet ((Set)whichList);
        }
        
        this.mimeTypes = (Set)gf.get ("mimeTypes", null); // NOI18N
    }
}
