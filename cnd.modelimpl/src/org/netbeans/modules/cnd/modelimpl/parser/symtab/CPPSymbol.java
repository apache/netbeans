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
package org.netbeans.modules.cnd.modelimpl.parser.symtab;

/**
 * based on CPPSymbol removed from cppparser.g
 *
 */
class CPPSymbol extends DictionaryEntry {

    enum CPPObjectType implements DictionaryEntry.ObjectType {

        otInvalid, otFunction, otVariable, otTypedef,
        otStruct, otUnion, otEnum, otClass,
        otTypename, otNonTypename;
    }
    private CPPObjectType type;

    CPPSymbol(CharSequence k, int bucketIndex, int scopeIndex) {
        super(k, bucketIndex, scopeIndex);
    }

    @Override
    ObjectType getType() {
        return this.type;
    }

    @Override
    boolean isTypeOf(ObjectType type) {
        if (type instanceof CPPObjectType) {
            switch ((CPPObjectType) type) {
                case otTypename: {
                    switch (this.type) {
                        case otTypedef:
                        case otClass:
                        case otEnum:
                        case otStruct:
                        case otUnion:
                            return true;
                    }
                    return false;
                }
                case otNonTypename: {
                    switch (this.type) {
                        case otVariable:
                        case otFunction:
                            return true;
                    }
                    return false;
                }
                default:
                    return this.type.equals(type);
            }
        } else {
            return false;
        }
    }
}
