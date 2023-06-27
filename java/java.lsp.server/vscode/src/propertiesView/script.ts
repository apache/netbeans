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

import { provideVSCodeDesignSystem, vsCodeButton, vsCodeTextField, vsCodeDivider, vsCodeCheckbox, Button, TextField, Checkbox } from "@vscode/webview-ui-toolkit";
import { isError, asClass, isClass } from "../typesUtil";
import { CommandKey, Message, PropertyMessage } from "./controlTypes";

provideVSCodeDesignSystem().register(vsCodeButton(), vsCodeTextField(), vsCodeDivider(), vsCodeCheckbox());

const vscode = acquireVsCodeApi();
document.addEventListener("DOMContentLoaded", () => {
    try {
        asClass(Button, document.getElementById('save'))
            .addEventListener('click', () => {
                try {
                    if (validate())
                        sendMessage({ _type: CommandKey.Save, properties: getProperties() });
                } catch (e: unknown) {
                    handleError(e);
                }
            });
        asClass(Button, document.getElementById('cancel'))
            .addEventListener('click', () => {
                sendMessage({ _type: CommandKey.Cancel });
            });
    } catch (e: unknown) {
        handleError(e);
    }
});

function handleError(error: unknown) {
    if (isError(error))
        sendMessage({ _type: CommandKey.Error, error: error.message, stack: error.stack });
    else
        sendMessage({ _type: CommandKey.Error, error: JSON.stringify(error) });
}

function sendMessage(message: Message) {
    vscode.postMessage(message);
}

function getProperties(): PropertyMessage[] {
    const out: PropertyMessage[] = [];
    const elements = document.getElementsByName("input");
    for (let i = 0; i < elements.length; ++i) {
        const element = elements.item(i);
        if (element)
            out.push(getProperty(element));
    }
    return out;
}

function getProperty(element: HTMLElement): PropertyMessage {
    if (isClass(TextField, element)) {
        return makeProperty(element.value, element?.id);
    } else if (isClass(Checkbox, element)) {
        return makeProperty(element.checked, element?.id);
    } else if (isClass(HTMLTableElement, element)) {
        return makeProperty(parseProperties(element), element?.id);
    }
    throw new Error("Unknown HTML Element type.");
}

function makeProperty(value: string | boolean | Record<string, string>, name?: string): PropertyMessage {
    if (name)
        return { name: name, value: value };
    throw new Error("HTML Element have no ID.");
}

function parseProperties(table: HTMLTableElement): Record<string, string> {
    const out: Record<string, string> = {};
    for (let i = 0; i < table.rows.length; ++i) {
        readProperty(out, table.rows.item(i)?.cells);
    }
    return out;
}

function readProperty(out: Record<string, string>, cells?: HTMLCollectionOf<HTMLTableCellElement> | null) {
    out[asClass(TextField, cells?.item(0)?.getElementsByClassName("name").item(0)).value]
        = asClass(TextField, cells?.item(1)?.getElementsByClassName("value").item(0)).value;
}

function validate(): boolean {
    return true; // no validation needed ATM
}