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

package org.netbeans.api.languages;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


/**
 * Represents path in AST tree.
 *
 * @author Jan Jancura
 */
public abstract class ASTPath {

    ASTPath () {};
    
    /**
     * Returns last ASTItem in this path.
     * 
     * @return last ASTItem in this path
     */
    public abstract ASTItem                 getLeaf ();
    
    /**
     * Returns size of this path.
     * 
     * @return size of this path
     */
    public abstract int                     size ();
    
    /**
     * Returns first ASTItem in this path.
     * 
     * @return first ASTItem in this path
     */
    public abstract ASTItem                 getRoot ();
    
    /**
     * Returns iterator for this path.
     * 
     * @return iterator for this path
     */
    public abstract ListIterator<ASTItem>   listIterator ();
    
    /**
     * Returns iterator for this path.
     * 
     * @return iterator for this path
     */
    public abstract ListIterator<ASTItem>   listIterator (int index);
    
    /**
     * Returns ASTItem on given index.
     * 
     * @return ASTItem on given index
     */
    public abstract ASTItem                 get (int index);
    
    /**
     * Returns subpath of this path from given index.
     * 
     * @return subpath of this path from given index
     */
    public abstract ASTPath                 subPath (int index);

    /**
     * Returns new path from {@link javax.util.List}, or null if the path is empty.
     * 
     * @param path list of ASTItems or null, if the path is empty
     * @return new ASTPath
     */
    public static ASTPath create (List<ASTItem> path) {
        if (path.isEmpty ()) return null;
        return new Token2Path (path);
    }


    /**
     * Creates new singleton path.
     * 
     * @param item 
     * @return new ASTPath
     */
    public static ASTPath create (ASTItem item) {
        if (item == null) throw new NullPointerException ();
        return new TokenPath (item);
    }
    
    
    // innerclasses ............................................................

    private static final class TokenPath extends ASTPath {

        private ASTItem o;
        
        TokenPath (ASTItem o) {
            this.o = o;
        }
        
        public ASTItem getLeaf () {
            return o;
        }
        
        public int size () {
            return 1;
        }
        
        public ASTItem getRoot () {
            return o;
        }
        
        public ListIterator<ASTItem> listIterator () {
            return Collections.singletonList (o).listIterator ();
        }
        
        public ListIterator<ASTItem> listIterator (int index) {
            return Collections.singletonList (o).listIterator (index);
        }
        
        public ASTItem get (int index) {
            if (index == 0) return o;
            throw new ArrayIndexOutOfBoundsException ();
        }
        
        public ASTPath subPath (int index) {
            if (index == 0) return this;
            throw new ArrayIndexOutOfBoundsException ();
        }
        
        public String toString () {
            return "ASTPath " + o;
        }
    }

    private static final class Token2Path extends ASTPath {

        private List<ASTItem> path;
        
        Token2Path (List<ASTItem> path) {
            this.path = path;
            if (path.size () < 1)
                throw new IllegalArgumentException ();
        }
        
        public ASTItem getLeaf () {
            return path.get (path.size () - 1);
        }
        
        public int size () {
            return path.size ();
        }
        
        public ASTItem getRoot () {
            return path.get (0);
        }
        
        public ListIterator<ASTItem> listIterator () {
            return path.listIterator ();
        }
        
        public ListIterator<ASTItem> listIterator (int index) {
            return path.listIterator (index);
        }
        
        public ASTItem get (int index) {
            return path.get (index);
        }
        
        public ASTPath subPath (int index) {
            return new Token2Path (path.subList (0, index + 1));
        }
        
        public String toString () {
            StringBuilder sb = new StringBuilder ("ASTPath ");
            Iterator<ASTItem> it = path.iterator ();
            if (it.hasNext ()) {
                ASTItem item = it.next ();
                if (item instanceof ASTNode)
                    sb.append (((ASTNode) item).getNT ());
                else
                    sb.append (item);
            }
            while (it.hasNext ()) {
                ASTItem item = it.next ();
                if (item instanceof ASTNode)
                    sb.append (", ").append (((ASTNode) item).getNT ());
                else
                    sb.append (", ").append (item);
            }
            return sb.toString ();
        }
    }
}
