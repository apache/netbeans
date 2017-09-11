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

package org.netbeans.modules.dbschema.nodes;

/** Interface for filtering and ordering the items in the visual
* presentation of a source element.
* Used to control the children of a source element node.
* <p>Note that this does <em>not</em> fire events for changes
* in its properties; it is expected that a new filter will instead
* be created and applied to the source children.
*
* @see org.openide.src.SourceElement
* @see SourceChildren
* @author Dafe Simonek, Jan Jancura
*/
public class SchemaElementFilter {

  /** Specifies a child representing a package or class import. */
  public static final int       TABLE = 1;
  /** Specifies a child representing a (top-level) class. */
  public static final int       VIEW = 2;
  /** Does not specify any top-level element. */
  public static final int       ALL = TABLE + VIEW;

  /** Default order of the top-level element types in the hierarchy.
  * A list, each of whose elements is a bitwise disjunction of element types.
  * By default, only classes and interfaces are listed, and these together.
  */
  public static final int[]     DEFAULT_ORDER = {TABLE + VIEW};

  /** stores property value */
  private boolean               allTables = false;
  /** stores property value */
  private int[]                 order = null;
  

  /** Test whether all classes in the source should be recursively shown.
  * @return <code>true</code> to include inner classes/interfaces, <code>false</code> to only
  * include top-level classes/interfaces
  */
  public boolean isAllTables () {
    return allTables;
  }

  /** Set whether all classes should be shown.
  * @param type <code>true</code> if so
  * @see #isAllClasses
  */
  public void setAllTables (boolean allTables) {
    this.allTables = allTables;
  }

  /** Get the current order for elements.
  * @return the current order, as a list of bitwise disjunctions among element
  * types (e.g. {@link #CLASS}). If <code>null</code>, the {@link #DEFAULT_ORDER},
  * or no particular order at all, may be used.
  */
  public int[] getOrder () {
    return order;
  }

  /** Set a new order for elements.
  * Should update the children list of the source element node.
  * @param order the new order, or <code>null</code> for the default
  * @see #getOrder
  */
  public void setOrder (int[] order) {
    this.order = order;
  }
}
