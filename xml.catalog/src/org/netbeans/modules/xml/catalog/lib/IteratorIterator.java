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
package org.netbeans.modules.xml.catalog.lib;

import java.util.*;

/**
 * Let a list of iterators behave as a single iterator.
 *
 * @author  Petr Kuzel
 */
public final class IteratorIterator implements Iterator {

    private Vector iterators = new Vector();

    private Iterator current = null;  //current iterator;
    private Iterator it = null;       //iterators.iterator();

    /*
     * It is set by hasNext() and cleared by next() call.
     */
    private Object next = null;       //current element

    /**
     * New iterators can be added while hasNext() or prior its first call.
     * @param it iterator, never <codE>null</null>.
     */
    public void add(Iterator it) {
        assert it != null;
        iterators.add(it);
    }

    /**
     * Unsupported operation.
     */
    public void remove() {
        throw new UnsupportedOperationException(); 
    }

    public Object next() {
       if (hasNext()) {
           Object tmp = next;
           next = null;
           return tmp;
       } else {
           throw new NoSuchElementException();
       }
    }

    public boolean hasNext() {
        if (next != null) return true;
        
        if (it == null) it = iterators.iterator();
        while (current == null) {
            if (it.hasNext()) {
                current = (Iterator) it.next();
            } else {
                return false;
            }
        }
        
        while (current.hasNext() || it.hasNext()) {
            
            // fetch next iterator if necessary
            if (current.hasNext() == false) {
                current = (Iterator) it.next();
                continue;
            } else {           
                next = current.next();
                return true;
            }
        }
        next = null;
        return false;
    }  
}
