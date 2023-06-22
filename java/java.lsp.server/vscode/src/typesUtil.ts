/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
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