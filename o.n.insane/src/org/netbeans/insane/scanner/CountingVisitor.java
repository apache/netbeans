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

package org.netbeans.insane.scanner;

import java.util.*;

/**
 * A visitor implementation that counts occurence and total size of found
 * objects, classified by their class.
 *
 * Usage: use it as a {@link Visitor} for an engine. After the engine finishes,
 * you can query found classes and per class statistics using
 * {@link #getClasses()}, {@link #getCountForClass(java.lang.Class)},
 * {@link #getSizeForClass(java.lang.Class)}, and gobal statistics
 * using {@link #getTotalCount()} and {@link #getTotalSize}.
 *
 * @author Nenik
 */
public class CountingVisitor implements Visitor {
    
    private Map<Class<?>, Info> infoMap = new HashMap<Class<?>, Info>();
    private int count;
    private int size;
    
    /** Creates a new instance of CountingVisitor */
    public CountingVisitor() {
    }
    
    
    public void visitClass(Class<?> cls) {
        infoMap.put(cls, new Info());
    }
    
    public void visitObject(ObjectMap map, Object obj) {
        Info info = infoMap.get(obj.getClass());
        assert info != null : "Engine shall announce the class before instance";
        
        info.count++;
        count++;
        int objSize = ScannerUtils.sizeOf(obj); 
        info.size += objSize;
        size += objSize;
    }
    
    public void visitStaticReference(ObjectMap map, Object to, java.lang.reflect.Field ref) {}
    public void visitObjectReference(ObjectMap map, Object from, Object to, java.lang.reflect.Field ref) {}
    public void visitArrayReference(ObjectMap map, Object from, Object to, int index) {}  

    public Set<Class<?>> getClasses() {
        return Collections.unmodifiableSet(infoMap.keySet());
    }
    
    public int getCountForClass(Class cls) {
        Info info = infoMap.get(cls);
        if (info == null) throw new IllegalArgumentException("Unknown class");
        
        return info.count;
    }
    
    public int getSizeForClass(Class cls) {
        Info info = infoMap.get(cls);
        if (info == null) throw new IllegalArgumentException("Unknown class");
        
        return info.size;
    }
    
    public int getTotalCount() {
        return count;
    }
    
    public int getTotalSize() {
        return size;
    }
    
    
    // A structure holding statistics for one class.
    private static class Info {
        int count;
        int size;
    }
}
