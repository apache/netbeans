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
package org.netbeans.modules.languages.hcl.ast;

import org.antlr.v4.runtime.Token;

/**
 *
 * @author Laszlo Kishalmi
 */
public abstract class HCLElement {

    public abstract void accept(Visitor v);
    
    public interface Visitor {
        /**
         * Visit the given element. Shall return {@code true} if the visit
         * shall be finished at this level in the element tree.
         * 
         * @param e the element to visit.
         * @return {@code false} if the visit shall continue on the subtree of
         *         the given element.
         */
        boolean visit(HCLElement e);
    }
    
    /**
     * Convenience Visitor implementation, where the HCLElements are split to
     * Block, Attribute, and Expression types. 
     */
    public abstract static class BAEVisitor implements Visitor {
        @Override
        public boolean visit(HCLElement e) {
            if (e instanceof HCLBlock) {
                return visitBlock((HCLBlock)e);
            } else if (e instanceof HCLAttribute) {
                return visitAttribute((HCLAttribute) e);
            } else if (e instanceof HCLExpression) {
                return visitExpression((HCLExpression) e);
            }
            return false;
        }
        
        protected abstract boolean visitBlock(HCLBlock block);
        
        protected abstract boolean visitAttribute(HCLAttribute attr);
        
        protected abstract boolean visitExpression(HCLExpression expr);
    }

    public static class BAEVisitorAdapter extends BAEVisitor {

        @Override
        protected boolean visitBlock(HCLBlock block) {
            return false;
        }

        @Override
        protected boolean visitAttribute(HCLAttribute attr) {
            return false;
        }

        @Override
        protected boolean visitExpression(HCLExpression expr) {
            return false;
        }
        
    }

    public static final class CreateContext {
        public final HCLElement element;
        public final Token start;
        public final Token stop;

        public CreateContext(HCLElement element, Token start, Token stop) {
            this.element = element;
            this.start = start;
            this.stop = stop;
        }
        
    }
}
