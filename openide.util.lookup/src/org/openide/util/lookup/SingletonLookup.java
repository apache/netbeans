/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.openide.util.lookup;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;

/**
 * Unmodifiable lookup that contains just one fixed object.
 *
 * @author Marian Petras
 */
class SingletonLookup extends Lookup {

    private final Object objectToLookup;
    private final String id;

    SingletonLookup(Object objectToLookup) {
        this(objectToLookup, null);
    }

    SingletonLookup(Object objectToLookup, String id) {
        if (objectToLookup == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }

        this.objectToLookup = objectToLookup;
        this.id = id;
    }

    @Override
    public <T> T lookup(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }

        return (clazz.isInstance(objectToLookup))
               ? clazz.cast(objectToLookup)
               : null;
    }

    @Override
    public <T> Result<T> lookup(Template<T> template) {
        if (template == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }

        Lookup.Item<T> item = lookupItem(template);
        if (item != null) {
            return new SingletonResult<T>(item);
        } else {
            return Lookup.EMPTY.lookup(template);
        }
    }

    @Override
    public <T> Collection<? extends T> lookupAll(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }

        return (clazz.isInstance(objectToLookup))
               ? Collections.singletonList(clazz.cast(objectToLookup))
               : Collections.<T>emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Item<T> lookupItem(Template<T> template) {
        if (template == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }

        String templateId = template.getId();
        if (templateId != null) {
            if (id == null) {
                if (!templateId.equals(objectToLookup.toString())) {
                    return null;
                }
            } else {
                if (!templateId.equals(id)) {
                    return null;
                }
            }
        }

        Object templateInst = template.getInstance();
        if ((templateInst != null) && (objectToLookup != templateInst)) {
            return null;
        }

        Class<T> clazz = template.getType();
        if ((clazz != null) && !clazz.isInstance(objectToLookup)) {
            return null;
        }

        Lookup.Item<T> item;
        if (clazz != null) {
            item = Lookups.lookupItem(clazz.cast(objectToLookup), id);
        } else {
            item = Lookups.lookupItem((T) objectToLookup, id);
        }
        return item;
    }

    @Override public String toString() {
        return "SingletonLookup[" + objectToLookup + "]";
    }

    static class SingletonResult<T> extends Lookup.Result<T> {

        private final Lookup.Item<T> item;

        SingletonResult(Lookup.Item<T> item) {
            this.item = item;
        }

        @Override
        public void addLookupListener(LookupListener l) {
            // this result never changes - no need to register a listener
        }

        @Override
        public void removeLookupListener(LookupListener l) {
            // this result never changes - no need to register a listener
        }

        @Override
        public Set<Class<? extends T>> allClasses() {
            return Collections.<Class<? extends T>>singleton(item.getType());
        }

        @Override
        public Collection<? extends Item<T>> allItems() {
            return Collections.singletonList(item);
        }

        @Override
        public Collection<? extends T> allInstances() {
            return Collections.singletonList(item.getInstance());
        }

    }

}
