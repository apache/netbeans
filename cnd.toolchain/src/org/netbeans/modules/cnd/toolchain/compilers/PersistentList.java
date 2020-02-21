
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
