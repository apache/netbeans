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

package org.netbeans.spi.editor.mimelookup;

import java.util.List;
import org.openide.filesystems.FileObject;

/**
 * Provider of the instance of the given class.
 * <br>
 * The provider gets a list of files which it transfers
 * into one instance of the class for which it's declared.
 *
 * <p>
 * For example there can be an instance provider
 * of actions for the editor popup. The file object names
 * of the actions declared in the layer can be of two forms:<ul>
 *   <li><i>MyAction.instance</i> are actions instances declaration files</li>.
 *   <li><i>reformat-code</i> are editor actions names</li>.
 * </ul>
 * <br/>
 * The instance provider translates all the file objects to actions
 * which it returns as a collection in some sort of collection-like class
 * e.g.<pre>
 * interface PopupActions {
 *
 *     List&lt;Action> getActions();
 *
 * }</pre>
 *
 * @param T type of instance which will be created
 */
public interface InstanceProvider<T> {
    
    /**
     * Create an instance of the class for which this
     * instance provider is declared in {@link Class2LayerFolder}.
     *
     * @param fileObjectList non-null list of the file objects
     *  collected from the particular layer folder and possibly
     *  the inherited folders.
     * @return non-null instance of the class for which
     *  this instance provider is declared. The list of the file objects
     *  should be translated to that instance so typically the instance
     *  contains some kind of the collection.
     */
    public T createInstance(List<FileObject> fileObjectList);

}
