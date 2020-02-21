/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.netbeans.modules.cnd.asm.model.lang.syntax;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.cnd.asm.model.lang.AsmOffsetable;

public class AsmModelStream implements Iterable<AsmOffsetable> {
     
    private final List<AsmOffsetable> elements;
     
    public AsmModelStream(List<AsmOffsetable> in) {
        elements = Collections.unmodifiableList(in);
    }
        
    public Iterator<AsmOffsetable> iterator() {
        return elements.iterator();
    }
    
    public AsmOffsetable get(int index) {
        return elements.get(index);
    }
    
    public int size() {
        return elements.size();
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof AsmModelStream) {
            return elements.equals(((AsmModelStream) obj).elements);
        }
        return false;
    }
    
    public int hashCode() {
        return elements.hashCode();
    }
            
    public void forEach(ElementVisitor visitor, AsmFilter filter) {
        for (AsmOffsetable el : this) {
            if (filter.accept(el)) {
                if (!visitor.visit(el)) 
                    break;                  
            }
        }
    }
    
    public void forEach(ElementVisitor visitor) {
        forEach(visitor, TrueFilter.instance);                        
    }        
    
    
    // Inner classes 
       
    interface ElementVisitor {
        boolean visit(AsmOffsetable el);
    }
        
    public interface AsmFilter {
        boolean accept(AsmOffsetable el);          
    }        
            
    public static class ClazzFilter implements AsmFilter {        
        private final Class<?> cl;
        
        public boolean accept(AsmOffsetable el) {
            return el.getClass() == cl;
        }       
        
        public ClazzFilter(Class<?> cl) {
            this.cl = cl;
        }
    }
                 
    public static final class TrueFilter implements AsmFilter {
        public static final AsmFilter instance = new TrueFilter();        
        
        public boolean accept(AsmOffsetable el) {
            return true;
        }
        
        private TrueFilter() { }        
    }
}
