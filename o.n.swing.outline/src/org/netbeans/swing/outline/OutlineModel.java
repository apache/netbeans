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
package org.netbeans.swing.outline;

import javax.swing.table.TableModel;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.TreeModel;

/** A model for an Outline (&quot;tree-table&quot;).  Implements both
 * TreeModel and TableModel (the default implementation, DefaultOutlineModel,
 * wraps a supplied TreeModel and TableModel).  It is vastly easier to
 * use <code>DefaultOutlineModel</code> than to implement this interface
 * directly.
 *
 * @author  Tim Boudreau  */
public interface OutlineModel extends TableModel, TreeModel {
    /** Get the <code>TreePathSupport</code> object this model uses to manage
     * information about expanded nodes.  <code>TreePathSupport</code> implements
     * logic for tracking expanded nodes, manages <code>TreeWillExpandListener</code>s,
     * and is a repository for preserving expanded state information about nodes whose parents
     * are currently collapsed.  JTree implements very similar logic internally
     * to itself.
     * <p>
     * <i>(PENDING) It is not yet determined if TreePathSupport will remain a
     * public class.</i>
     */
    public TreePathSupport getTreePathSupport ();
    /** Get the layout cache which is used to track the visual state of nodes.
     * This is typically one of the standard JDK layout cache classes, such
     * as <code>VariableHeightLayoutCache</code> or <code>
     * FixedHeightLayoutCache</code>.  */
    public AbstractLayoutCache getLayout ();
    /** Determine if the model is a large-model.  Large model trees keep less
     * internal state information, relying on the TreeModel more.  Essentially
     * they trade performance for scalability. An OutlineModel may be large
     * model or small model; primarily this affects the type of layout cache
     * used, just as it does with JTree.  */
    public boolean isLargeModel();
}
