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

package org.netbeans.modules.j2ee.dd.api.common;

import org.netbeans.modules.schema2beans.BaseBean;
/**
 * Interface representing the root of interfaces bean tree structure.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 *
 * @author Milan Kuchtiak
 */
public interface RootInterface extends ComponentInterface {
    /**
     * Used in {@link #merge} method.<br>
     * The result of merge operation is the intersection of graphs.
     */
    public static final int MERGE_INTERSECT = BaseBean.MERGE_INTERSECT;
    /**
     * Used in {@link #merge} method.<br>
     * The result of merge operation is the union of graphs.
     */
    public static final int MERGE_UNION	= BaseBean.MERGE_UNION;
    /**
     * Used in {@link #merge} method.<br>
     * The result of merge operation is the first graph updated with the changes in another graph.
     */
    public static final int MERGE_UPDATE = BaseBean.MERGE_UPDATE;
    
    /**
     * Writes the deployment descriptor data from deployment descriptor bean graph to file object.<br>
     * This is more convenient method than {@link org.netbeans.modules.j2ee.dd.api.common.CommonDDBean#write} method.<br>
     * The locking problems are solved for the user in this method.
     *
     * @param fo File Object where to write the content of depl.descriptor holding in bean tree structure
     * @throws java.io.IOException
     */
    public void write(org.openide.filesystems.FileObject fo) throws java.io.IOException ;
    
    /**
     * Merging two bean tree structures together.<pre>
     *
     *There are several ways odf merging :
     *
     *	Let's define:
     *		G1 the current graph and G2 the new graph we want to merge
     *		E1 the set of element of G1 that don't exist anymore in G2.
     *		E2 the set of new elements of G2 that don't exist in G1.
     *
     *	Then,
     *		MERGE_UPDATE is 	G1 - E1 + E2	(G1 becomes G2)
     *		MERGE_UNION is 	G1 U G2 <=> G1 + E2
     *		MERGE_INTERSECT is	G1 n G2 <=> (G1 U G2) - E1 - E2
     *</pre>
     * @param root of the bean graph that is merged with actual bean graph
     * @param mode type of merging (INTERSECT, UNION, UPDATE)
     */
    public void merge(RootInterface root, int mode);
}
