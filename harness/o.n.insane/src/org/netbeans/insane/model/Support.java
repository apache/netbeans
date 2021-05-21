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

package org.netbeans.insane.model;

import java.io.*;
import java.util.*;

/**
 * A support class containing HeapModel factories and helper methods
 * for analysing HeapModels.
 *
 * @author Nenik
 */
public final class Support {

    private  Support() {}

    /**
     * Parses a XML dump of the heap created by the SimpleXmlVisitor.
     *
     * @param file the file with the heap dump.
     * @return The HeapModel representing the heap dump.
     * @throws Exception if there is any error during processing the dump.
     */
    public static HeapModel parseSimpleXMLDump(File file) throws Exception {
        return XmlHeapModel.parse(file);
    }
    
    /**
     * Opens a binary dump of the heap created by conversion from the XML
     * dump produced by SimpleXmlVisitor.
     *
     * @param file the file with the heap dump.
     * @return The HeapModel representing the heap dump.
     * @throws Exception if there is any error during processing the dump.
     */
    public static HeapModel openSimpleBinaryDump(File file) throws Exception {
        return BinaryHeapModel.open(file);
    }

    /*
     * Converts the XML dump format to throws ebinary file format.
     */
    public static void convertSimpleDump(File from, File to) throws Exception {
        InsaneConverter.convert(from, to);
    }
    
    /*
     */
    public static void findRoots(HeapModel model, Item itm, boolean weak) {        
        LinkedList<PathElement> queue = new LinkedList<PathElement>();
        queue.add(new PathElement(itm, null));
        Set<Object> visited = new HashSet<Object>(queue);
        while (!queue.isEmpty()) {
            PathElement act = queue.remove(0);
            Enumeration<Object> en = act.getItem().incomming();
            while(en.hasMoreElements()) {
                Object o = en.nextElement();
                if (o instanceof String) {
                    System.out.println(o + "->\n" + act);
                    return;
                } else { // add real support for weak references to the model.
                    Item ref = (Item)o;
                    if (!weak && ("java.lang.ref.WeakReference".equals(ref.getType()) ||
                        "javax.swing.AbstractActionPropertyChangeListener$OwnedWeakReference".equals(ref.getType()) ||
                        "org.openide.util.WeakListener$ListenerReference".equals(ref.getType()) ||
                        "org.openide.util.WeakListenerImpl$ListenerReference".equals(ref.getType()) ||
                        "org.openide.util.WeakSet$Entry".equals(ref.getType()) ||
                        "java.lang.ref.SoftReference".equals(ref.getType()) ||
//                        "sun.misc.Cleaner".equals(ref.getType()) ||
                        "org.netbeans.modules.javacore.classpath.MergedClassPathImplementation$ClassPathMap$WeakPair".equals(ref.getType()) ||
                        "java.util.WeakHashMap$Entry".equals(ref.getType()) ||
                        "org.netbeans.modules.project.ant.FileChangeSupport$Holder".equals(ref.getType()) |
                        "org.netbeans.api.nodes2looks.LookNode$FirerImpl".equals(ref.getType()) ||
                        "org.openide.loaders.DataObjectPool$ItemReference".equals(ref.getType()) ||
                        "org.netbeans.mdr.NBMDRepositoryImpl$FacilityCache$HandlerReference".equals(ref.getType()) ||
                        "org.netbeans.mdr.storagemodel.MdrStorage$InstanceMap$InstanceReference".equals(ref.getType()) ||
                        "org.openide.util.IconManager$ActiveRef".equals(ref.getType()))) {
                        // skip
                    } else {
                        // add to the queue if not new
                        if (visited.add(ref)) queue.add(new PathElement(ref, act));
                    }
                }
            }
        }
    }

    private static class PathElement {
        private Item item;
        private PathElement next; 
        public PathElement(Item item, PathElement next) {
            this.item = item;
            this.next = next;
        }
        public Item getItem() {
            return item;
        }
        public String toString() {
            if (next == null) {
                return item.toString();
            } else {
                return item.toString() + "->\n" + next.toString();
            }
        }
    }

    
}
