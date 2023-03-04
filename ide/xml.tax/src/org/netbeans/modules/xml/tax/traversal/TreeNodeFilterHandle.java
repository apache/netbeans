/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.xml.tax.traversal;

import java.io.*;
import java.util.ArrayList;

import org.netbeans.tax.traversal.TreeNodeFilter;

/**
 * @author Libor Kramolis
 */
public final class TreeNodeFilterHandle implements Serializable {
    private static final long serialVersionUID = -571598256778542088L;

    /** */
    private String[] nodeTypeNames;
    /** */
    private short acceptPolicy;

    /** */
    private transient TreeNodeFilter nodeFilter;


    //
    // init
    //

    /** */
    public TreeNodeFilterHandle (TreeNodeFilter nodeFilter) {
        this.nodeFilter = nodeFilter;
    }


    //
    // itself
    //

    /**
     */
    public TreeNodeFilter getNodeFilter () {
        if ( nodeFilter == null ) { // lazy init

            ArrayList knownTypes = new ArrayList();
            for (int i = 0; i < nodeTypeNames.length; i++) {
                try {
                    knownTypes.add (Class.forName ( nodeTypeNames[i] ));
                } catch (ClassNotFoundException ex) {
                    //let it be
                }
            }
            Class[] nodeTypes = (Class[])knownTypes.toArray (new Class[0]);

            nodeFilter = new TreeNodeFilter (nodeTypes, acceptPolicy);
        }

        return nodeFilter;
    }


    /**
     */
    private void initFields () {
        acceptPolicy = getNodeFilter().getAcceptPolicy();

        Class[] nodeTypes = getNodeFilter().getNodeTypes();
        nodeTypeNames = new String [nodeTypes.length];
        for (int i = 0; i < nodeTypes.length; i++) {
            nodeTypeNames[i] = nodeTypes[i].getName();
        }
    }


    /**
     */
    private void writeObject (ObjectOutputStream oos) throws IOException {
        initFields();

        oos.defaultWriteObject();
    }

}
