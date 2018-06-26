/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.inspect.webkit;

import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;
import org.openide.nodes.Node;

/**
 * Property set sorted according to property names.
 * 
 * @author Jan Stola
 */
abstract class SortedPropertySet<T extends Node.Property> extends Node.PropertySet {
    /** Property name to property map sorted according to property names. */
    private final SortedMap<String,T> properties;
    /** Determines whether this property set has been initialized. */
    private boolean initialized;

    /**
     * Creates a new {@code SortedPropertySet}.
     * 
     * @param name name of the property set.
     * @param displayName display name of the property set.
     * @param shortDescription short description of the property set.
     */
    SortedPropertySet(String name, String displayName, String shortDescription) {
        super(name, displayName, shortDescription);
        properties = new TreeMap<String,T>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
    }

    /**
     * Adds a property into this property set.
     * 
     * @param property property to add into this set.
     */
    synchronized void addProperty(T property) {
        properties.put(property.getName(), property);
    }

    /**
     * Removes a property from this property set.
     * 
     * @param property property to remove from this set.
     */
    synchronized void removeProperty(T property) {
        properties.remove(property.getName());
    }

    /**
     * Returns the property of the specified name.
     * 
     * @param name name of the requested property.
     * @return property of the specified name.
     */
    synchronized T getProperty(String name) {
        return properties.get(name);
    }

    /**
     * Updates this property set, i.e., forces synchronization with its model/source.
     */
    abstract void update();

    @Override
    public synchronized Node.Property<?>[] getProperties() {
        if (!initialized) {
            initialized = true;
            update();
        }
        return properties.values().toArray(new Node.Property<?>[properties.size()]);
    }

}
