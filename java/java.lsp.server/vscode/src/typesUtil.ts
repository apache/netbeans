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

export function isError(obj: unknown): obj is Error {
    return obj instanceof Error;
}

export function assertNever(_obj: never, errorMessage?: string): never {
    throw new Error(errorMessage || "Shouldn't reach here.");
}

export type KeyOfArray<T> = TupleUnion<keyof T>;
export type TupleUnion<U extends PropertyKey, R extends PropertyKey[] = []> = {
    [S in U]: Exclude<U, S> extends never ? [...R, S] : TupleUnion<Exclude<U, S>, [...R, S]>;
}[U];

export type EnumType<T extends PropertyKey> = { readonly [P in T]: P };
export type Typed<T extends PropertyKey> = { _type: T };