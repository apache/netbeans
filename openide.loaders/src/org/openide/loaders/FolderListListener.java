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

package org.openide.loaders;

/** Listener that watches progress of recognizing objects
* in a folder. The listener may even influence the data object recognition
* and, in such a way, act as a filter.
*
* <p>Normally the methods of this class are called in the process of a task
* to collect the data objects within a folder, e.g. in
* {@link FolderList#computeChildrenList(FolderListListener)}. In such a task
* implementations of {@link #process(DataObject, java.util.List)} may act as
* filters by not added the data object to the result list. Implementations
* of {@link #finished(java.util.List)} may be used to inform the caller about
* the result of the task and for further processing of the result. E.g.
* {@link FolderList#computeChildrenList(FolderListListener)} has as its return
* value the task to compute the list and not the computed children. An
* implementation of {@link #finished(java.util.List)} may be used by the caller
* of {@link FolderList#computeChildrenList(FolderListListener)} to get informed
* about the result of children computation.</p>
*
* @author Jaroslav Tulach
*/
interface FolderListListener {
    /** Another object has been recognized.
    * @param obj the object recognized
    * @param arr array where the implementation should add the 
    *    object
    */
    public void process (DataObject obj, java.util.List<DataObject> arr);

    /** All objects has been recognized.
    * @param arr list of DataObjects
    */
    public void finished (java.util.List<DataObject> arr);
}
