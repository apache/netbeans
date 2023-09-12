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

import java.awt.Image;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import org.openide.util.WeakSet;

// note: keep method NbObjectInputStream.resolveObject
// consistent with replaceObject method

/** Object output stream that could in the future be smart about saving certain objects.
* Also static methods to safely write an object that could cause problems during later deserialization.
*/
public class NbObjectOutputStream extends ObjectOutputStream {
    private static final String SVUID = "serialVersionUID"; // NOI18N
    private static final Set<String> alreadyReported = new WeakSet<String>();

    static {
        // See below.
        alreadyReported.add("java.lang.Exception"); // NOI18N
        alreadyReported.add("java.io.IOException"); // NOI18N
        alreadyReported.add("java.util.TreeSet"); // NOI18N
        alreadyReported.add("java.awt.geom.AffineTransform"); // NOI18N
    }

    private static Map<String,Boolean> examinedClasses = new WeakHashMap<String,Boolean>(250);
    private final List<Class> serializing = new ArrayList<>(50);

    /** Create a new object output.
    * @param os the underlying output stream
    * @throws IOException for the usual reasons
    */
    public NbObjectOutputStream(OutputStream os) throws IOException {
        super(os);

        try {
            enableReplaceObject(true);
        } catch (SecurityException ex) {
            throw (IOException) new IOException(ex.toString()).initCause(ex);
        }
    }

    /*
    * @param obj is an Object to be checked for replace
    */
    @Override
    public Object replaceObject(Object obj) throws IOException {
        if (obj instanceof Image) {
            return null;

            // [LIGHT]
            // additional code needed for full version
        }

        return super.replaceObject(obj);
    }

    /** Writes an object safely to the object output.
     * Can be read by {@link NbObjectInputStream#readSafely}.
    * @param oo object output to write to
    * @param obj the object to write
    * @exception SafeException if the object simply fails to be serialized
    * @exception IOException if something more serious fails
    */
    public static void writeSafely(ObjectOutput oo, Object obj)
    throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(200);

        try {
            NbObjectOutputStream oos = new NbObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bos.close();
        } catch (Exception exc) {
            // exception during safe of the object
            // encapsulate all exceptions into safe exception
            oo.writeInt(0);
            throw new SafeException(exc);
        }

        oo.writeInt(bos.size());
        oo.write(bos.toByteArray());
    }

    @Override
    protected void annotateClass(Class cl) throws IOException {
        super.annotateClass(cl);

        if (cl.isArray()) {
            return;
        }

        if (cl.isInterface()) {
            // TheInterface.class is being serialized, not an instance;
            // no need for svuid.
            return;
        }

        serializing.add(cl);

        if (isSerialVersionUIDDeclared(cl)) {
            return;
        }

        if (IOException.class.isAssignableFrom(cl)) {
            // ObjectOutputStream for some reason stores IOException's that are
            // thrown during serialization (they are rethrown later maybe?).
            // It's no problem, just ignore them here.
            return;
        }

        String classname = cl.getName();

        if (alreadyReported.add(classname)) {
            Set<Class> serializingUniq = new HashSet<Class>();
            StringBuffer b = new StringBuffer("Serializable class "); // NOI18N
            b.append(classname);
            b.append(" does not declare serialVersionUID field. Encountered while storing: ["); // NOI18N

            Iterator<Class> it = serializing.iterator();
            boolean first = true;

            while (it.hasNext()) {
                Class c = it.next();

                if ((c != cl) && serializingUniq.add(c)) {
                    if (first) {
                        first = false;
                    } else {
                        b.append(", "); // NOI18N
                    }

                    b.append(c.getName());
                }
            }

            b.append("] See also http://www.netbeans.org/issues/show_bug.cgi?id=19915"); // NOI18N

            String file = System.getProperty("InstanceDataObject.current.file"); // NOI18N

            if ((file != null) && (file.length() > 0)) {
                b.append(" [may have been writing "); // NOI18N
                b.append(file);
                b.append("]"); // NOI18N
            }

            Logger.getLogger(NbObjectOutputStream.class.getName()).warning(b.toString());
        }
    }
    
    /* Package private for testing */
    static boolean isSerialVersionUIDDeclared(Class clazz) {
        String classname = clazz.getName();
        Boolean okay = examinedClasses.get(classname);
        
        if (okay == null) {
            if (classname.equals("java.util.HashSet") || classname.equals("java.util.ArrayList") || // NOI18N
                clazz.isEnum() || classname.equals("java.lang.Enum")) { // NOI18N
                okay = Boolean.TRUE;
            } else {
                okay = Boolean.FALSE;

                java.lang.reflect.Field[] flds = clazz.getDeclaredFields();

                for (int i = 0; i < flds.length; i++) {
                    if (flds[i].getName().equals(SVUID)) {
                        okay = Boolean.TRUE;

                        break;
                    }
                }
            }

            examinedClasses.put(clazz.getName(), okay);
        }
        
        return okay.booleanValue();
    }
}
