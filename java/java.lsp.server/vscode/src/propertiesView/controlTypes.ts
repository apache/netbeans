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

import { EnumType, Typed } from "../typesUtil";

export type ID = number;

export const PropertyTypes = {
    String: "java.lang.String",
    Boolean: "java.lang.Boolean",
    Properties: "java.util.Properties",
    Unknown: "unknown",
} as const;// unfortunate but necessary duplication
export type PropertyTypeMap = {
    "java.lang.String": string;
    "java.lang.Boolean": boolean;
    "java.util.Properties": Record<string, string>;
    "unknown": unknown
};
export type Property<T extends keyof PropertyTypeMap = keyof PropertyTypeMap> = T extends T ? {
    preferred: boolean;
    displayName: string;
    shortName: string;
    htmlName?: string;
    write: boolean;
    hidden: boolean;
    expert: boolean;
    type: T;
    value: PropertyTypeMap[T];
    name: string;
} : never; // Distributive type
export type Properties = {
    preferred: boolean;
    displayName: string;
    shortName: string;
    htmlName?: string;
    hidden: boolean;
    expert: boolean;
    name: string;
    properties: Property[];
};

export type PropertyMessage = {
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
    properties: PropertyMessage[];
};
export type CancelMessage = MessageCommon<typeof CommandKey.Cancel>;
export type Message = InfoMessage | ErrMessage | SaveMessage | CancelMessage;