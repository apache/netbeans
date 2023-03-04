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

import java.util.*;

/**
 * Default implementation class of CodeExpression interface. No other class
 * should implement CodeExpression or extend this class. All extensibility
 * is done through the CodeExpressionOrigin implementations. CodeExpression
 * is kept as an interface for compatibility and usability reasons.
 *
 * @author Tomas Pavek
 */

final class DefaultCodeExpression implements CodeExpression {

    private CodeStructure codeStructure;

    private CodeExpressionOrigin expressionOrigin;

    private CodeObjectUsage expressionUsage;


    public DefaultCodeExpression(CodeStructure codeStructure,
                                 CodeExpressionOrigin origin)
    {
        this.codeStructure = codeStructure;
        setOrigin(origin);
    }

    DefaultCodeExpression(CodeStructure codeStructure) {
        this.codeStructure = codeStructure;
    }

    // -------

    @Override
    public CodeStructure getCodeStructure() {
        return codeStructure;
    }

    @Override
    public CodeVariable getVariable() {
        return codeStructure.getVariable(this);
    }

    @Override
    public CodeExpressionOrigin getOrigin() {
        return expressionOrigin;
    }

    @Override
    public void setOrigin(CodeExpressionOrigin newOrigin) {
        CodeExpressionOrigin oldOrigin = expressionOrigin;
        if (oldOrigin == newOrigin)
            return;

        CodeExpression registerParent = null;
        List<CodeExpression> registerParams = null;

        if (oldOrigin != null) {
            if (newOrigin != null) { // changing one origin to another
                CodeExpression oldParent = oldOrigin.getParentExpression();
                CodeExpression newParent = newOrigin.getParentExpression();
                if (oldParent != null && oldParent != newParent)
                    CodeStructure.unregisterObjectUsage(this, oldParent);
                if (newParent != null && newParent != oldParent)
                    registerParent = newParent;

                CodeExpression[] oldParams = oldOrigin.getCreationParameters();
                CodeExpression[] newParams = newOrigin.getCreationParameters();

                for (int i=0; i < oldParams.length; i++) {
                    CodeExpression oldPar = oldParams[i];
                    if (i < newParams.length && oldPar == newParams[i])
                        continue;
                    int j = 0;
                    while (j < newParams.length) {
                        if (oldPar == newParams[j])
                            break;
                        j++;
                    }
                    if (j == newParams.length)
                        CodeStructure.unregisterObjectUsage(this, oldPar);
                }

                for (int i=0; i < newParams.length; i++) {
                    CodeExpression newPar = newParams[i];
                    if (i < oldParams.length && newPar == oldParams[i])
                        continue;
                    int j = 0;
                    while (j < oldParams.length) {
                        if (newPar == oldParams[j])
                            break;
                        j++;
                    }
                    if (j == oldParams.length) {
                        if (registerParams == null)
                            registerParams = new ArrayList<CodeExpression>();
                        registerParams.add(newPar);
                    }
                }
            }
            else CodeStructure.unregisterUsingCodeObject(this);
        }

        expressionOrigin = newOrigin;

        if (codeStructure.isUndoRedoRecording())
            codeStructure.logUndoableChange(
                new OriginChange(oldOrigin, newOrigin));

        if (newOrigin != null) {
            if (oldOrigin != null) {
                if (registerParent != null)
                    registerParent.addUsingObject(
                        this, UsedCodeObject.DEFINED, CodeExpression.class);

                if (registerParams != null)
                    for (int i=0, n=registerParams.size(); i < n; i++) {
                        CodeExpression param = registerParams.get(i);
                        param.addUsingObject(this,
                                             UsedCodeObject.USING,
                                             CodeExpression.class);
                    }
            }
            else CodeStructure.registerUsingCodeObject(this);
        }
    }

    // --------
    // UsedCodeObject implementation - registering objects that use
    // this expression

    @Override
    public void addUsingObject(UsingCodeObject usingObject,
                               int useType,
                               Object useCategory)
    {
        CodeStructureChange undoableChange =
                getExpressionUsage().addUsingObject(
                                       usingObject,
                                       useType,
                                       useCategory,
                                       codeStructure.isUndoRedoRecording());
        if (undoableChange != null)
            codeStructure.logUndoableChange(undoableChange);
    }

    @Override
    public boolean removeUsingObject(UsingCodeObject usingObject) {
        CodeStructureChange undoableChange =
                getExpressionUsage().removeUsingObject(
                                       usingObject,
                                       codeStructure.isUndoRedoRecording());
        if (undoableChange != null)
            codeStructure.logUndoableChange(undoableChange);

        boolean stillUsed = !getExpressionUsage().isEmpty();
        if (!stillUsed) // the elment is no longer used in the structure
            codeStructure.removeExpressionFromVariable(this);
        return stillUsed;
    }

    @Override
    public Iterator getUsingObjectsIterator(int useType, Object useCategory) {
        return getExpressionUsage().getUsingObjectsIterator(useType, useCategory);
    }

    private CodeObjectUsage getExpressionUsage() {
        if (expressionUsage == null)
            expressionUsage = new CodeObjectUsage(this);
        return expressionUsage;
    }

    // ---------
    // UsingCodeObject implementation - handling objects used by this expression

    // notifying about registering this object in used object
    @Override
    public void usageRegistered(UsedCodeObject usedObject) {
    }

    // notifying about removing the used object from structure
    @Override
    public boolean usedObjectRemoved(UsedCodeObject usedObject) {
//        if (!(usedObject instanceof CodeExpression))
//            return true;
        // an used expression was removed - this expression will be too ...
        codeStructure.removeExpressionFromVariable(this);
        return false;
    }

    @Override
    public UsedCodeObject getDefiningObject() {
        return getOrigin().getParentExpression();
    }

    @Override
    public Iterator getUsedObjectsIterator() {
        return new UsedObjectsIterator();
    }

    // --------

    private class OriginChange implements CodeStructureChange {
        private CodeExpressionOrigin oldOrigin;
        private CodeExpressionOrigin newOrigin;

        OriginChange(CodeExpressionOrigin oldOrigin,
                     CodeExpressionOrigin newOrigin)
        {
            this.oldOrigin = oldOrigin;
            this.newOrigin = newOrigin;
        }

        @Override
        public void undo() {
            expressionOrigin = oldOrigin;
        }

        @Override
        public void redo() {
            expressionOrigin = newOrigin;
        }
    }

    // --------

    private class UsedObjectsIterator implements Iterator {
        int index;
        CodeExpression[] parameters;

        UsedObjectsIterator() {
            index = getOrigin().getParentExpression() != null ? -1 : 0;
            parameters = getOrigin().getCreationParameters();
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

            Object obj = index > -1 ? parameters[index] :
                                      getOrigin().getParentExpression();
            index++;
            return obj;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
