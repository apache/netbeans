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
package org.openide.util.io;

import org.openide.util.Lookup;
import org.openide.util.BaseUtilities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import java.lang.reflect.InvocationTargetException;
import org.openide.util.Exceptions;


// note: keep method resolveObject consistent with NbObjectOutputStream.replaceObject

/** Controlled deserialization stream using the system class loader.
* Employs the classloader available from lookup (currently that used for modules).
* Also contains static methods to safely read objects that might have problems
* during deserialization that should not corrupt the stream. The stream also provides
* support for changing name of stored classes.
*
* @see #readClassDescriptor
*/
public class NbObjectInputStream extends ObjectInputStream {
    /** Create a new object input.
    * @param is underlying input stream
    * @throws IOException for the usual reasons
    */
    public NbObjectInputStream(InputStream is) throws IOException {
        super(is);

        try {
            enableResolveObject(true);
        } catch (SecurityException ex) {
            throw new IOException(ex.toString());
        }
    }

    /* Uses NetBeans module classloader to load the class.
     * @param v description of the class to load
     */
    protected Class resolveClass(ObjectStreamClass v) throws IOException, ClassNotFoundException {
        ClassLoader cl = getNBClassLoader();

        try {
            return Class.forName(v.getName(), false, cl);
        } catch (ClassNotFoundException cnfe) {
            String msg = "Offending classloader: " + cl; // NOI18N
            Exceptions.attachMessage(cnfe, msg);
            throw cnfe;
        }
    }

    /** Lazy create default NB classloader for use during deserialization. */
    private static ClassLoader getNBClassLoader() {
        ClassLoader c = Lookup.getDefault().lookup(ClassLoader.class);

        return (c != null) ? c : ClassLoader.getSystemClassLoader();
    }

    /** Provides a special handling for renaming of serialized classes.
     * <P>
     * Often, as the time goes the serialized classes evolve. They can be moved
     * to new packages, renamed or changed (by a mistake) to not reflect the
     * version of class stored in previous sessions.
     * <P>
     * This method deals with some of this incompatibilites and provides the
     * module owners a way how to fix some of them.
     * <P>
     * When a class is read, the <link>Utilities.translate</link> is consulted
     * to find out what whether the name of the class is listed there and
     * what new value is assigned to it. This allows complete rename of the
     * serialized class. For example:
     * <code>org.netbeans.core.NbMainExplorer</code>
     * can be renamed to
     * <code>org.netbeans.core.ui.NbExp</code> - of course supposing that
     * the new class is able to read the serialized fields of the old one.
     * <P>
     * Another useful feature of this method is the ability to supress wrong
     * <code>serialVersionUID</code>. This was causing us a lot of problems,
     * because people were forgetting to specify the <code>serialVersionUID</code>
     * field in their sources and then it was hard to recover from it. Right
     * now we have a solution: Just use <link>Utilities.translate</link> framework
     * to assing your class <code>org.yourpackage.YourClass</code> the same
     * name as it had e.g. <code>org.yourpackage.YourClass</code>. This will
     * be interpreted by this method as a hit to suppress <code>serialVersionUID</code>
     * and the <code>NbObjectInputStream</code> will ignore its value.
     * <P>
     * Please see <link>Utilities.translate</link> to learn how your module
     * can provide list of classes that changed name or want to suppress <code>serialVersionUID</code>.
     *
     */
    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        ObjectStreamClass ose = super.readClassDescriptor();

        String name = ose.getName();
        String newN = BaseUtilities.translate(name);

        if (name == newN) {
            // no translation
            return ose;
        }

        // otherwise reload the ObjectStreamClass to contain the local copy
        ClassLoader cl = getNBClassLoader();
        Class<?> clazz = Class.forName(newN, false, cl);

        ObjectStreamClass newOse = ObjectStreamClass.lookup(clazz);

        // #28021 - it is possible that lookup return null. In that case the conversion
        // table contains class which is not Serializable or Externalizable.
        if (newOse == null) {
            throw new java.io.NotSerializableException(newN);
        }

        return newOse;
    }

    /** Reads an object from the given object input.
    * The object had to be saved by the {@link NbObjectOutputStream#writeSafely} method.
    *
    * @param oi object input
    * @return the read object
    * @exception IOException if IO error occured
    * @exception SafeException if the operation failed but the stream is ok
    *    for further reading
    */
    public static Object readSafely(ObjectInput oi) throws IOException {
        int size = oi.readInt();
        byte[] byteArray = new byte[size];
        oi.readFully(byteArray, 0, size);

        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
            NbObjectInputStream ois = new NbObjectInputStream(bis);
            Object obj = ois.readObject();
            bis.close();

            return obj;
        } catch (Exception exc) {
            // encapsulate all exceptions into safe exception
            throw new SafeException(exc);
        } catch (LinkageError le) {
            throw new SafeException(new InvocationTargetException(le));
        }
    }

    /** Skips an object from the given object input without loading it.
    * The object had to be saved by the {@link NbObjectOutputStream#writeSafely} method.
    *
    * @param oi object input
    * @exception IOException if an I/O error occurred
    */
    public static void skipSafely(ObjectInput oi) throws IOException {
        int size = oi.readInt();
        oi.skip(size);
    }
}
