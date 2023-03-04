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

package org.netbeans.modules.form.codestructure;

import java.util.Iterator;

/**
 * Abstract class providing common implementation of UsingCodeObject
 * interface for further CodeStatement implementations. No other
 * UsingCodeObject implementation for statement should be introduced.
 *
 * @author Tomas Pavek
 */

abstract class AbstractCodeStatement implements CodeStatement {

    protected CodeExpression parentExpression;

    protected AbstractCodeStatement(CodeExpression parentExpression) {
        this.parentExpression = parentExpression;
    }

    @Override
    public CodeExpression getParentExpression() {
        return parentExpression;
    }

    // --------
    // UsingCodeObject implementation

    // notifying about registering this object in used object
    @Override
    public void usageRegistered(UsedCodeObject usedObject) {
    }

    // notifying about removing the used object from structure
    @Override
    public boolean usedObjectRemoved(UsedCodeObject usedObject) {
        return false;
    }

    @Override
    public UsedCodeObject getDefiningObject() {
        return getParentExpression();
    }

    @Override
    public Iterator getUsedObjectsIterator() {
        return new UsedObjectsIterator();
    }

    // --------

    private class UsedObjectsIterator implements Iterator {
        int index;
        CodeExpression[] parameters;

        UsedObjectsIterator() {
            index = getParentExpression() != null ? -1 : 0;
            parameters = getStatementParameters();
            if (parameters == null)
                parameters = CodeStructure.EMPTY_PARAMS;
        }

        @Override
        public boolean hasNext() {
            return index < parameters.length;
        }

        @Override
        public Object next() {
            if (!hasNext())
                throw new java.util.NoSuchElementException();

            Object obj = index > -1 ? parameters[index] : getParentExpression();
            index++;
            return obj;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
