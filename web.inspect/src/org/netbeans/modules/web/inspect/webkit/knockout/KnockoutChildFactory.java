/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.inspect.webkit.knockout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.debugger.PropertyDescriptor;
import org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 * Factory for children of {@code KnockoutNode}.
 *
 * @author Jan Stola
 */
public class KnockoutChildFactory extends ChildFactory<PropertyDescriptor> {
    /** Remote object representing the parent of the children. */
    private RemoteObject remoteObject;
    /** WebKit debugging. */
    private WebKitDebugging webKit;
    /** WebKit node whose Knockout context is the parent of the children. */
    private org.netbeans.modules.web.webkit.debugging.api.dom.Node webKitNode;

    /**
     * Creates a new {@code KnockoutChildFactory} for a WebKit node.
     * 
     * @param webKit WebKit debugging.
     * @param webKitNode WebKit node.
     */
    KnockoutChildFactory(WebKitDebugging webKit, org.netbeans.modules.web.webkit.debugging.api.dom.Node webKitNode) {
        this.webKit = webKit;
        this.webKitNode = webKitNode;
    }

    /**
     * Creates a new {@code KnockoutChildFactory} for a {@code RemoteObject}.
     * 
     * @param remoteObject remote object representing the parent of the children.
     */
    KnockoutChildFactory(RemoteObject remoteObject) {
        this.remoteObject = remoteObject;
    }

    @Override
    protected boolean createKeys(List<PropertyDescriptor> toPopulate) {
        if (webKitNode != null) {
            RemoteObject jsNode = webKit.getDOM().resolveNode(webKitNode, null);
            String function = "function() { return window.NetBeans && NetBeans.getKnockoutVersion() ? NetBeans.getKnockout().contextFor(this) : null; }"; // NOI18N
            remoteObject = webKit.getRuntime().callFunctionOn(jsNode, function);
        }
        if (remoteObject.getType() == RemoteObject.Type.OBJECT) {
            List<PropertyDescriptor> properties = remoteObject.getProperties();
            toPopulate.addAll(sort(properties));
        }
        return true;
    }

    /**
     * Returns a sorted copy of the given list.
     * 
     * @param list list to sort.
     * @return sorted copy of the given list.
     */
    static List<PropertyDescriptor> sort(List<PropertyDescriptor> list) {
        List<PropertyDescriptor> copy = new ArrayList<PropertyDescriptor>();
        copy.addAll(list);
        Collections.sort(copy, PropertyDescriptorComparator.getInstance());
        return copy;
    }

    @Override
    protected Node createNodeForKey(PropertyDescriptor key) {
        return new KnockoutNode(key.getName(), key.getValue());
    }

    /**
     * Refreshes the children.
     */
    void refresh() {
        remoteObject.resetProperties();
        refresh(false);
    }

    /**
     * Comparator for {@PropertyDescriptor}s.
     */
    private static class PropertyDescriptorComparator implements Comparator<PropertyDescriptor> {
        /** The only instance of this class. */
        private static final PropertyDescriptorComparator INSTANCE = new PropertyDescriptorComparator();
        /** Name of the prototype property. */
        private static final String PROTOTYPE = "__proto__"; // NOI18N

        /**
         * Returns the (only) instance of this class.
         * 
         * @return instance of this class.
         */
        static final PropertyDescriptorComparator getInstance() {
            return INSTANCE;
        }

        @Override
        public int compare(PropertyDescriptor descriptor1, PropertyDescriptor descriptor2) {
            String name1 = descriptor1.getName();
            String name2 = descriptor2.getName();
            Data data1 = data(name1);
            Data data2 = data(name2);
            int diff = data1.group - data2.group;
            if (diff == 0) {
                diff = data1.item.compareTo(data2.item);
            }
            return diff;
        }

        /**
         * Returns comparison data for the given name of {@code PropertyDescriptor}.
         * 
         * @param name name of a property descriptor.
         * @return comparison data for the given name.
         */
        private static Data data(String name) {
            Data data = new Data();
            if (PROTOTYPE.equals(name)) {
                // Prototype should be at the end
                data.item = name;
                data.group = 3;
            } else {
                try {
                    // Numeric items (i.e. array items) should be at the beginning
                    data.item = Long.parseLong(name);
                    data.group = 1;
                } catch (NumberFormatException nfex) {
                    data.item = name;
                    data.group = 2;
                }
            }
            return data;
        }

        /**
         * Comparison data for a (name of) {@code PropertyDescriptor}.
         */
        static class Data {
            /** Group where the property should be placed. */
            int group;
            /** Item to use for comparisons to determine the position within the group. */
            Comparable item;
        }
        
    }
    
}
