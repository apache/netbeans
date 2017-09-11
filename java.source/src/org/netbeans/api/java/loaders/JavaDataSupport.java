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

package org.netbeans.api.java.loaders;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.JavaNode;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * This class contains helper methods necessary to write extensions
 * of the java data support.
 *
 * @author Jan Pokorsky
 */
public final class JavaDataSupport {

    /** singleton */
    private JavaDataSupport() {
    }
    
    /**
     * In case you write own data loader you should use this entry for the
     * <code>.java</code> file object. The entry provides functionality like
     * create from template.
     * @param mdo the data object this entry will belong to
     * @param javafile the file object for the entry
     * @return the java entry
     */
    public static MultiDataObject.Entry createJavaFileEntry(MultiDataObject mdo, FileObject javafile) {
        return new JavaDataLoader.JavaFileEntry(mdo, javafile);
    }

    /**
     * Creates a default node for a particular java file object.
     * @param javafile the java file object to represent
     * @return the node
     */
    public static Node createJavaNode(FileObject javafile) {
        try {
            DataObject jdo = DataObject.find(javafile);
            return new JavaNode(jdo, true);
        } catch (DataObjectNotFoundException ex) {
            Logger.getLogger(JavaDataSupport.class.getName()).log(Level.INFO, null, ex);
            return new AbstractNode(Children.LEAF);
        }
    }

}
