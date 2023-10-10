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

export type IsType<T> = (obj: unknown) => obj is T;

function assertType<T>(obj: unknown, isTypeTest: IsType<T>, errorMessage?: string): asserts obj is T {
    if (!isTypeTest(obj))
        throw new Error(errorMessage || "Object isn't of expected type.");
}

export type Constructor<T> = new (...args: any) => T;

export function isClass<T>(cls: Constructor<T>, obj: unknown): obj is T {
    return obj instanceof cls;
}
export function isClassTest<T>(cls: Constructor<T>): ((obj: unknown) => obj is T) {
    return (obj: unknown): obj is T => isClass(cls, obj);
}
export function asClass<T>(cls: Constructor<T>, obj: unknown, errorMessage?: string): T {
    assertType(obj, isClassTest(cls), errorMessage);
    return obj;
}

export function isString(obj: unknown): obj is string {
    return typeof obj === 'string';
}

export function isObject(obj: unknown): obj is object {
    return typeof obj === 'object' && obj !== null;
}

export function isRecord<K extends PropertyKey, T>(typeTest: IsType<T>, obj: unknown): obj is Record<K, T> {
    return isObject(obj) && Object.values(obj).every(typeTest);
}

export function isError(obj: unknown): obj is Error {
    return obj instanceof Error;
}

export function assertNever(_obj: never, errorMessage?: string): never {
    throw new Error(errorMessage || "Shouldn't reach here.");
}

export type EnumType<T extends PropertyKey> = { readonly [P in T]: P };
export type Typed<T extends PropertyKey> = { _type: T };