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
