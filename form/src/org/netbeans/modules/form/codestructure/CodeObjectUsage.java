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

package org.netbeans.modules.form.codestructure;

import java.util.*;

/**
 * @author Tomas Pavek
 */

final class CodeObjectUsage {

    private UsedCodeObject usedObject;
    private java.util.List usageList;

    public CodeObjectUsage(UsedCodeObject usedObject) {
        this.usedObject = usedObject;
    }

    public CodeStructureChange addUsingObject(UsingCodeObject usingObject,
                                              int useType,
                                              Object useCategory,
                                              boolean provideUndoableChange)
    {
        if (useCategory == null)
            throw new IllegalArgumentException();

        if (usageList == null)
            usageList = new LinkedList();

        ObjectUse use = new ObjectUse(usingObject, useType, useCategory);
        usageList.add(use); // [check if the object is not already registered??]

        usingObject.usageRegistered(usedObject);

        return provideUndoableChange ?
               new UsageChange(usedObject, use, true) : null;
    }

    public CodeStructureChange removeUsingObject(UsingCodeObject usingObject,
                                                 boolean provideUndoableChange)
    {
        if (usageList == null)
            return null;

        ObjectUse removed = null;
        Iterator it = usageList.iterator();
        while (it.hasNext()) {
            ObjectUse use = (ObjectUse) it.next();
            if (usingObject == use.usingObject) {
                removed = use;
                it.remove();
            }
        }

        if (removed != null)
            usingObject.usedObjectRemoved(usedObject);

        return provideUndoableChange && removed != null ?
               new UsageChange(usedObject, removed, false) : null;
    }

    public Iterator getUsingObjectsIterator(int useType, Object useCategory) {
        Iterator it = usageList != null ? usageList.iterator() : null;
        return new UsageIterator(it, useType, useCategory);
    }

    public boolean isEmpty() {
        return usageList == null || usageList.isEmpty();
    }

    // -------

    private static class ObjectUse {
        private UsingCodeObject usingObject;
        private int type;
        private Object category;

        ObjectUse(UsingCodeObject usingObject, int useType, Object useCategory) {
            this.usingObject = usingObject;
            this.type = useType;
            this.category = useCategory;
        }

        boolean matches(int type, Object category) {
            if (type != 0 && type != this.type)
                return false;
            if (category == null)
                return true;
            return category.equals(this.category);
        }
    }

    // --------

    private static class UsageChange implements CodeStructureChange {
        private UsedCodeObject usedObject;
        private ObjectUse use;
        private boolean added; // true: added, false: removed

        UsageChange(UsedCodeObject usedObject, ObjectUse use, boolean added) {
            this.usedObject = usedObject;
            this.use = use;
            this.added = added;
        }

        @Override
        public void undo() {
            if (added)
                usedObject.removeUsingObject(use.usingObject);
            else
                usedObject.addUsingObject(use.usingObject, use.type, use.category);
        }

        @Override
        public void redo() {
            if (added)
                usedObject.addUsingObject(use.usingObject, use.type, use.category);
            else
                usedObject.removeUsingObject(use.usingObject);
        }
    }

    // --------

    private static class UsageIterator implements Iterator {
        private int useType;
        private Object useCategory;

        private Iterator iterator;
        private Object next;

        public UsageIterator(Iterator iterator,
                             int useType, Object useCategory)
        {
            this.iterator = iterator;
            this.useType = useType;
            this.useCategory = useCategory;
        }

        @Override
        public boolean hasNext() {
            if (iterator == null)
                return false;
            if (next != null)
                return true;

            while (iterator.hasNext()) {
                ObjectUse use = (ObjectUse) iterator.next();
                if (use.matches(useType, useCategory)) {
                    next = use.usingObject;
                    return true;
                }
            }
            return false;
        }

        @Override
        public Object next() {
            if (!hasNext())
                throw new NoSuchElementException();

            Object nextObject = next;
            next = null;
            return nextObject;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
