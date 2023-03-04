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

package org.netbeans.modules.j2ee.ejbcore.api.methodcontroller;

import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;

/**
 * Provide simple instance of the visitor pattern to use for code generation.
 * @author Chris Webster
 */
public abstract class MethodType {

    public enum Kind {BUSINESS, SELECT, CREATE, FINDER, HOME}
    
    private final MethodModel methodHandle;
    
    public MethodType(MethodModel methodHandle) {
        this.methodHandle = methodHandle;
    }
    
    public abstract void accept(MethodTypeVisitor visitor);
    
    public abstract Kind getKind();
    
    public final MethodModel getMethodElement() {
        return methodHandle;
    }
    
    public interface MethodTypeVisitor {
        void visit(BusinessMethodType bmt);
        void visit(CreateMethodType cmt);
        void visit(HomeMethodType hmt);
        void visit(FinderMethodType fmt);
    }
    
    public static class BusinessMethodType extends MethodType {
        public BusinessMethodType(MethodModel methodHandle) {
            super(methodHandle);
        }
        
        public void accept(MethodTypeVisitor visitor) {
            visitor.visit(this);
        }
        
        public Kind getKind() {
            return Kind.BUSINESS;
        }
    }
    
    public static class SelectMethodType extends MethodType {
        public SelectMethodType(MethodModel methodHandle) {
            super(methodHandle);
        }
        
        public void accept(MethodTypeVisitor visitor) {
            assert false:"select methods are not intended to be visited";
        }
        
        public Kind getKind() {
            return Kind.SELECT;
        }
    }
    
    public static class CreateMethodType extends MethodType {
        public CreateMethodType(MethodModel methodHandle) {
            super(methodHandle);
        }
        
        public void accept(MethodTypeVisitor visitor) {
            visitor.visit(this);
        }

        public Kind getKind() {
            return Kind.CREATE;
        }
    }
    
    public static class HomeMethodType extends MethodType {
        public HomeMethodType(MethodModel methodHandle) {
            super(methodHandle);
        }
        
        public void accept(MethodTypeVisitor visitor) {
            visitor.visit(this);
        }

        public Kind getKind() {
            return Kind.HOME;
        }
    }
    
    public static class FinderMethodType extends MethodType {
        public FinderMethodType(MethodModel methodHandle) {
            super(methodHandle);
        }
        
        public void accept(MethodTypeVisitor visitor) {
            visitor.visit(this);
        }
        
        public Kind getKind() {
            return Kind.FINDER;
        }
    }
}
