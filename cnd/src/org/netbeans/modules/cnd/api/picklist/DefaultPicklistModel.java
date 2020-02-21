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

package org.netbeans.modules.cnd.api.picklist;

import javax.swing.event.EventListenerList;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;

/**
 * This class implements the methods for a picklist list (Picklist).
 */
public class DefaultPicklistModel implements PicklistModel, Serializable {

    private static final long serialVersionUID = -8893287476464434693L;

    /** Default max size of Picklist */
    private int maxSize = 10;
    /** Increase size of Picklist, if variable sized*/
    private static int increaseSize = 10;

    /** The list itself and it's current size */
    private PicklistElement[] picklist = null;
    private int picklistSize = 0;

    /** Holds list of event listeners */
    protected EventListenerList listenerList = null;

    /**
     * Creates a new variable sized Picklist
     */
    public DefaultPicklistModel() {
        initialize(0);
    }

    /**
     * Creates a new Picklist with maximux 'max' elements. 'max' <= 0 means variable sized list.
     */
    public DefaultPicklistModel(int max) {
        initialize(max);
    }

    private EventListenerList getListenerList() {
        if (listenerList == null) {
            listenerList = new EventListenerList();
        }
        return listenerList;
    }


    /**
     * Initializing data
     */
    private void initialize(int max) {
        if (max <= 0) {
            maxSize = 0; // Open-ended ...
            picklist = new PicklistElement[increaseSize];
        } else {
            maxSize = max; // Fixed sized
            picklist = new PicklistElement[increaseSize];
        }
    }

    /**
     * Adds the specified component to the beginning of this list,
     * increasing its size by one.
     * @param   elem   the component to be added.
     */
    @Override
    public void addElement(PicklistElement elem) {
        addElement(elem, true);
    }

    /**
     * Adds the specified component to the beginning of this list,
     * increasing its size by one.
     * @param   elem   the component to be added.
     */
    public void addElement(String elem) {
        addElement(new DefaultPicklistElement(elem), true);
    }


    /**
     * Adds the specified component to the beginning of this list,
     * increasing its size by one.
     * @param   elem   the component to be added.
     */
    @Override
    public synchronized void addElement(PicklistElement elem, boolean check) {
        // Is this the only method that needs to synchronized??? FIXUP
        // first check if element already in the list
        int foundAt = contains(elem);
        int shiftDownFrom = 0;

        if (check && foundAt >= 0) {
            shiftDownFrom = foundAt;
            picklistSize--;
        } else {
            // Open-ended list
            if (picklistSize == picklist.length) {
                // Need to increate the size of picklist
                PicklistElement[] picklist2 = new PicklistElement[picklist.length + increaseSize];
                for (int i = 0; i < picklist.length; i++) {
                    picklist2[i] = picklist[i];
                }
                picklist = picklist2;
            }
            shiftDownFrom = picklistSize;
        }

        // Shift down elements
        for (int i = shiftDownFrom; i > 0; i--) {
            picklist[i] = picklist[i - 1];
        }

        // insert at top and increase size
        picklist[0] = elem;
        picklistSize++;

        // if fixed-sizes list, check size and possile delete last element
        if (maxSize > 0 && picklistSize > maxSize) {
            picklistSize--;
        }

        // notify listener of a change
        fireContentsChanged(this);

    }

    /**
     * Tests if the specified element is a component in this list.
     *
     * @param   elem   an object.
     * @return  index of the specified element in the list if it exists
     * as determined by the <tt>equals</tt> method; <code>-1</code> otherwise.
     */
    @Override
    public int contains(PicklistElement elem) {
        int foundAt = -1;

        if (elem != null) {
            for (int i = 0; i < picklistSize; i++) {
                if (picklist[i].equals(elem)) {
                    foundAt = i;
                    break;
                }
            }
        }
        return foundAt;
    }

    /**
     * Returns the value at the specified index.
     * @param index the requested index
     * @return the value at <code>index</code>
     */
    @Override
    public PicklistElement getElementAt(int index) {
        if (index >= 0 && index < picklistSize) {
            return picklist[index];
        } else {
            return null;
        }
    }

    /**
     * Returns all the elements in the list (possibly size 0 array)
     * @return all the elements in the list (possibly size 0 array).
     */
    @Override
    public PicklistElement[] getElements() {
        PicklistElement[] ret = new PicklistElement[picklistSize];
        for (int i = 0; i < picklistSize; i++) {
            ret[i] = picklist[i];
        }
        return ret;
    }

    @Override
    public String[] getElementsDisplayName() {
        PicklistElement[] elements = getElements();
        String[] elementsDisplayName = new String[getSize()];
        for (int i = 0; i < getSize(); i++) {
            elementsDisplayName[i] = elements[i].displayName();
        }
        return elementsDisplayName;
    }

    /**
     * Returns the max length of the list, 0 being variable sized.
     * @return the max length of the list, 0 being variable sized
     */
    @Override
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Returns the top-most element in the list if not empty, otherwise null.
     * @return the top-most element in the list
     */
    @Override
    public PicklistElement getMostRecentUsedElement() {
        if (picklistSize > 0) {
            return picklist[0];
        } else {
            return null;
        }
    }

    /**
     * Returns the length of the list.
     * @return the length of the list
     */
    @Override
    public int getSize() {
        return picklistSize;
    }

    /**
     * Removes all elements from this list and sets its size to zero.
     */
    @Override
    public void removeAllElements() {
        picklistSize = 0;

        // notify listener of a change
        fireContentsChanged(this);
    }

    /**
     * Removes the first (lowest-indexed) occurrence of the argument
     * from this list.
     *
     * @param   elem   the element to be removed.
     * @return  the element removed if found, null otherwise.
     */
    @Override
    public PicklistElement removeElement(PicklistElement elem) {
        return removeElementAt(contains(elem));
    }

    /**
     * Removes the element at the specified index from the list. Each element in
     * this list with an index greater or equal to the specified
     * <code>index</code> is shifted downward to have an index one
     * smaller than the value it had previously. The size of this list
     * is decreased by <tt>1</tt>.<p>
     *
     * The index must be a value greater than or equal to <code>0</code>
     * and less than the current size of the list. <p>
     *
     * @param      index   the index of the object to remove.
     * @return  the element removed if index valid, null otherwise.
     */
    @Override
    public PicklistElement removeElementAt(int index) {
        PicklistElement toBeRemoved = null;
        if (index < 0 || index >= picklistSize) {
            return toBeRemoved;
        }
        toBeRemoved = picklist[index];
        for (int i = index; i < picklistSize - 1; i++) {
            picklist[i] = picklist[i + 1];
        }
        picklistSize--;

        // notify listener of a change
        fireContentsChanged(this);

        return toBeRemoved;
    }


    /**
     * Replaces the element at the specified index with the one
     * specified.
     *
     * @param      index   the index of the object to remove.
     * @return  the element replaced if index valid, null otherwise.
     */
    @Override
    public PicklistElement replaceElementAt(PicklistElement elem, int index) {
        PicklistElement toBeReplaced = null;
        if (index < 0 || index >= picklistSize) {
            return toBeReplaced;
        }
        toBeReplaced = picklist[index];
        picklist[index] = elem;

        // notify listener of a change
        fireContentsChanged(this);

        return toBeReplaced;
    }

    /**
     * Adds a listener to the list that's notified each time a change
     * to the data model occurs.
     * @param l the <code>PicklistDataListener</code> to be added
     */
    @Override
    public void addPicklistDataListener(PicklistDataListener l) {
        getListenerList().add(PicklistDataListener.class, l);
    }

    /**
     * Removes a listener from the list that's notified each time a
     * change to the data model occurs.
     * @param l the <code>PicklistDataListener</code> to be removed
     */
    @Override
    public void removePicklistDataListener(PicklistDataListener l) {
        getListenerList().remove(PicklistDataListener.class, l);
    }

    /**
     * Clones a picklist
     */
    @Override
    public PicklistModel clonePicklist() {
        DefaultPicklistModel newpicklist = new DefaultPicklistModel(maxSize);
        for (int i = getSize() - 1; i >= 0; i--) {
            newpicklist.addElement(getElementAt(i), false);
        }
        return newpicklist;
    }

    /**
     * First removes all elements, then copies all elements from 'picklist'.
     */
    @Override
    public void copyPicklist(PicklistModel picklist) {
        removeAllElements();
        for (int i = picklist.getSize() - 1; i >= 0; i--) {
            addElement(picklist.getElementAt(i), false);
        }
    }

    /**
     * Dumps all list elements to std out (for debugging...)
     */
    @Override
    public void dumpElements() {
        for (int i = 0; i < picklistSize; i++) {
            System.out.println("[" + i + "] " + picklist[i].displayName()); // NOI18N
        }
        System.out.println();
    }


    //-----------------------

    protected void fireContentsChanged(Object source) {
        Object[] listeners = getListenerList().getListenerList();
        PicklistDataEvent e = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == PicklistDataListener.class) {
                if (e == null) {
                    e = new PicklistDataEvent(source, PicklistDataEvent.CONTENTS_CHANGED);
                }
                ((PicklistDataListener) listeners[i + 1]).contentsChanged(e);
            }
        }
    }

    //---------------------------

    /**
     * For serialization
     */
    public void savePicklist(ObjectOutputStream out) {
        try {
            out.writeObject(this);
        } catch (IOException ioe) {
            //System.out.println("DefaultPicklistModel - savePicklist - ioe " + ioe);
        }
    }

    public void savePicklist(String dir, String name) {
        File dirfile = new File(dir);
        if (!dirfile.exists()) {
            dirfile.mkdirs();
        }

        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dir + File.separator + name));
            savePicklist(oos);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            //System.out.println("e " + e);
        }
    }

    /**
     * For serialization
     */
    public static PicklistModel restorePicklist(ObjectInputStream in) throws Exception {
        PicklistModel picklist = null;
        try {
            picklist = (PicklistModel) in.readObject();
        } catch (Exception e) {
            System.err.println("DefaultPicklistModel - restorePicklist - e " + e); // NOI18N
            throw e;
        }

        return picklist;
    }

    public static PicklistModel restorePicklist(String dir, String name) {
        PicklistModel ret = null;
        File file = new File(dir + File.separator + name);
        if (file.exists()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dir + File.separator + name));
                ret = restorePicklist(ois);
                ois.close();
            } catch (Exception e) {
                System.err.println("DefaultPicklistModel - restorePicklist - e " + e); // NOI18N
                System.err.println("DefaultPicklistModel - restorePicklist - deleting serialized data"); // NOI18N
                System.err.println("" + dir + File.separator + name + " deleted"); // NOI18N
                file.delete();
            }
        }
        return ret;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        try {
            out.writeObject(Integer.valueOf(maxSize));
            out.writeObject(Integer.valueOf(picklistSize));
            for (int i = 0; i < picklistSize; i++) {
                out.writeObject(picklist[i]);
            }
        } catch (IOException ioe) {
            System.err.println("DefaultPicklistModel - writeObject - ioe " + ioe); // NOI18N
            throw ioe;
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        try {
            maxSize = ((Integer) in.readObject()).intValue();
            picklistSize = ((Integer) in.readObject()).intValue();
            picklist = new PicklistElement[picklistSize];
            for (int i = 0; i < picklistSize; i++) {
                PicklistElement e = (PicklistElement) in.readObject();
                picklist[i] = e;
            }
        } catch (IOException e) {
            System.err.println("DefaultPicklistModel - readObject - e " + e); // NOI18N
            throw e;
        } catch (ClassNotFoundException e) {
            System.err.println("DefaultPicklistModel - readObject - e " + e); // NOI18N
            throw e;
        }
    }
}
