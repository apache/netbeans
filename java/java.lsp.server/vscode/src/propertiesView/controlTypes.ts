/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
 */
import { EnumType, Typed } from "../typesUtil";

export type ID = number;

export const PropTypes = {
    String: "java.lang.String",
    Boolean: "java.lang.Boolean",
    Properties: "java.util.Properties",
    Unknown: "unknown",
} as const;// unfortunate but necessary duplication
export type PropTypeMap = {
    "java.lang.String": string;
    "java.lang.Boolean": boolean;
    "java.util.Properties": Record<string, string>;
    "unknown": unknown
};
export type Property<T extends keyof PropTypeMap = keyof PropTypeMap> = T extends T ? {
    propPref: boolean;
    propDispName: string;
    propShortName: string;
    propWrite: boolean;
    propHidden: boolean;
    propExpert: boolean;
    propType: T;
    propValue: PropTypeMap[T];
    propName: string;
} : never; // Distributive type
export type Properties = {
    propPref: boolean;
    propDispName: string;
    propShortName: string;
    propHidden: boolean;
    propExpert: boolean;
    propName: string;
    props: Property[];
};

export type MessageProp = {
    name: string;
    value: string | boolean | Record<string, string>;
};

export type Command = "Save" | "Cancel" | "Error" | "Info";
export const CommandKey: EnumType<Command> = {
    Save: "Save",
    Error: "Error",
    Info: "Info",
    Cancel: "Cancel"
};

export type MessageCommon<T extends Command> = Typed<T>;
export type InfoMessage = MessageCommon<typeof CommandKey.Info> & {
    info: string;
};
export type ErrMessage = MessageCommon<typeof CommandKey.Error> & {
    error: string;
    stack?: string;
};
export type SaveMessage = MessageCommon<typeof CommandKey.Save> & {
    properties: MessageProp[];
};
export type CancelMessage = MessageCommon<typeof CommandKey.Cancel>;
export type Message = InfoMessage | ErrMessage | SaveMessage | CancelMessage;