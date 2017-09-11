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
            Enumeration en = act.getItem().incomming();
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
