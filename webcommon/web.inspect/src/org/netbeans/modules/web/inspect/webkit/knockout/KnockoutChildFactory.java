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
package org.netbeans.modules.web.inspect.webkit.knockout;

import java.util.ArrayList;
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
        copy.sort(PropertyDescriptorComparator.getInstance());
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
