
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
package org.netbeans.modules.cnd.toolchain.compilers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public final class PersistentList<E> extends Vector<E> implements Serializable{
    private static final long serialVersionUID = -8893123456464434693L;
    
    private final transient Object lock;
    
    /** Creates a new instance of PersistentList */
    public PersistentList() {
        lock = new Object();
    }
    
    public PersistentList(List<E> values) {
        super(values);
        lock = new Object();
    }
    
    private static String getRoot() {
        String dir = System.getProperty("netbeans.user") + "/config/cnd-compilers/"; // NOI18N
        return dir;
    }
    
    /*
     * Add a string only if not already in list
     */
    public void addUnique(E string) {
        synchronized(lock) {
            if (!inList(string)) {
                super.add(string);
            }
        }
        
    }
    
    private boolean inList(E path) {
        if (path == null) {
            return false;
        }
        Iterator<E> iterator = iterator();
        while (iterator.hasNext()) {
            if (path.equals(iterator.next())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * For serialization
     */
    private void saveList(ObjectOutputStream out) {
	try {
	    out.writeObject(this);
	}
	catch (IOException ioe) {
	    System.out.println("PersistentList - saveList - ioe " + ioe); // NOI18N
	}
    }

    public void saveList(String name) {
        synchronized (lock) {
            File dirfile = new File(getRoot());
            if (!dirfile.exists()) {
                dirfile.mkdirs();
            }

            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getRoot() + name));
                saveList(oos);
                oos.flush();
                oos.close();
            }
            catch (Exception e) {
                System.out.println("e " + e); // NOI18N
            }
        }
    }

    /**
     * For serialization
     */
    @SuppressWarnings("unchecked")
    private static PersistentList<String> restoreList(ObjectInputStream in) throws Exception {
        PersistentList<String> list = null;
    	try {
            list = (PersistentList<String>)in.readObject();
        } catch (Exception e) {
            System.err.println("PersistentList - restorePicklist - e " + e); // NOI18N
            throw e;
        }
        return new PersistentList<String>(list);
    }

    public static PersistentList<String> restoreList(String name) {
        PersistentList<String> ret = null;
        File file = new File(getRoot() + File.separator + name);
        if (!file.exists()) {
            // nothing
        } else {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getRoot() + name));
                ret = restoreList(ois);
                ois.close();
            }
            catch (Exception e) {
                System.err.println("PersistentList - restoreList - e" + e); // NOI18N
                System.err.println(getRoot() + name + " deleted"); // NOI18N
                file.delete();
            }
        }
        return ret;
    }
    
//    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
//	try {
//	    out.writeObject(new Integer(size()));
//            for (int i = 0; i < size(); i++)
//                out.writeObject(elementAt(i));
//	}
//	catch (IOException ioe) {
//	    System.err.println("DefaultPicklistModel - writeObject - ioe " + ioe); // NOI18N
//	    throw ioe;
//	}
//    }

//    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
//	try {
//            int size = ((Integer)in.readObject()).intValue();
//            for (int i = 0; i < size; i++)
//                add(((String)in.readObject()));
//	}
//	catch (IOException e) {
//	    System.err.println("DefaultPicklistModel - readObject - e " + e); // NOI18N
//	    throw e;
//	}
//	catch (ClassNotFoundException e) {
//	    System.err.println("DefaultPicklistModel - readObject - e " + e); // NOI18N
//	    throw e;
//	}
//    }
}
