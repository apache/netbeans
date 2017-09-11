/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
/*
 * LogFile.java
 *
 * Created on October 8, 2002, 12:40 PM
 */

package org.netbeans.performance.spi;
import org.netbeans.performance.spi.html.HTML;
/** Defines an interface representing an entry in a log file of
 * some kind, and a way to get context information about
 * the log file in question and context for the element.  This is
 * the base interface that all items parsed from logs should
 * expose.
 * @author Tim Boudreau
 */
public interface LogElement {
    /** Returns the path to the object, including the name of the object itself.
     * The structure is similar to a Filesystem path.  The portion of the path
     * preceding the name of the object is obtained from its <i>parent</i>.  <P>The
     * path is used as a means of segregating similar runs.  For example a path
     * for a log element representing a lineswitch used for one run of the software
     * might be:<br><code>/02282002/Windows XP/128Mb/performanceTests/javaconfig/NewSize
     * </code><br>
     * @return The path of the object, including its name - the fully qualified name of the
     * object.
     */    
    public String getPath ();
    /** The parent of the object.  A DataAggregation is an extension to LogElement that
     * makes it a container for other LogElements, including other DataAggregation.
     * This is the means by which multiple sets of identical data are segregated.
     * @return The parent object or null if no parent.
     */    
    public DataAggregation getParent();
    /** Convenience search method for locating an ancestor (a DataAggregation that is
     * named in the path of the element) of the element.
     * @param name The unqualified name of the object in question.
     * @return The element requested or null.
     */    
    public DataAggregation findAncestor (String name);
     /** Create an HTML representation of the object.  HTML representations of objects
      * should be self contained (no dangling tags) and be easily added to an
      * HTMLDocument object.
      * @return An object implementing the HTML interface which produces an HTML representation
      * of the object.
      * @see org.netbeans.spi.html.HTMLDocument
      * @see org.netbeans.spi.html.HTML
      */     
    public HTML toHTML ();
}
 
