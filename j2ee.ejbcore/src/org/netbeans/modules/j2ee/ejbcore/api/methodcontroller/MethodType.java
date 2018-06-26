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
