/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
 */
import * as vscode from 'vscode';
import { PropertyTypes, Properties, Property } from "./controlTypes";

export function makeHtmlForProperties(name: string, nonce: string, scriptUri: vscode.Uri, properties: Properties): string {
    return `<!DOCTYPE html>
    <html lang="en">
    
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta http-equiv="Content-Security-Policy" content="default-src 'none'; script-src 'nonce-${nonce}';">
        <title>${name}</title>
    </head>
    
    <body>
        <h1>${name} Properties</h1>
        <vscode-divider></vscode-divider>
        <table>
            ${makePropertiesTable(properties)}
            <tr>
                <td colspan="2">
                </td>
            </tr>
            <tr>
                <td colspan="2" align="right" style="text-align: right;">
                    <vscode-button id="save" appearance="primary">Save</vscode-button>
                    <vscode-button id="cancel" appearance="secondary">Cancel</vscode-button>
                </td>
            </tr>
        </table>
        <script type="module" nonce="${nonce}" src="${scriptUri}"></script>
    </body>
    
    </html>`
};

function wrapToTable(name: string, content: string, separator: string = ":"): string {
    return `<tr><td align="right"><b>${name}${separator}</b></td><td align="left">${content}</td></tr>`;
}

function makePropertiesTable(properties: Properties): string {
    let html = "";
    for (const prop of properties.properties) {
        html += makePropAccess(prop);
    }
    return html;
}

function makePropAccess(prop: Property): string {
    let out: string;
    switch (prop.type) {
        case PropertyTypes.String:
            out = makeStringAccess(prop);
            break;
        case PropertyTypes.Boolean:
            out = makeBoolAccess(prop);
            break;
        case PropertyTypes.Properties:
            out = makePropertiesAccess(prop);
            break;
        default:
            out = prop.value + "";
            break;
    }
    return wrapToTable(prop.displayName, out) + '\n';
}

function makeStringAccess(prop: Property<typeof PropertyTypes.String>) {
    return `<vscode-text-field name="input" id="${prop.name}" value="${encode(prop.value)}" ${prop.write ? "" : "disabled"}></vscode-text-field>`;
}

function makeBoolAccess(prop: Property<typeof PropertyTypes.Boolean>) {
    return `<vscode-checkbox name="input" id="${prop.name}" ${prop.write ? "" : "disabled"} ${prop.value ? "checked" : ""}></vscode-checkbox>`;
}

function makePropertiesAccess(prop: Property<typeof PropertyTypes.Properties>) {
    return `<details><summary><b>${prop.displayName}</b></summary><table name="input" id="${prop.name}">
    ${makePropTable(prop)}
    </table></details>`;
}

function makePropTable(prop: Property<typeof PropertyTypes.Properties>) {
    let out = "";
    for (const key in prop.value) {
        out += makePropRow(prop, key) + '\n';
    }
    return out;
}

function makePropRow(prop: Property<typeof PropertyTypes.Properties>, key: string) {
    return wrapToTable(asTextField(key, prop.write, "name"), asTextField(prop.value[key], prop.write, "value"), " = ");
}

function asTextField(value: string, enabled: boolean, name: string) {
    return `<vscode-text-field class="${name}" value="${encode(value)}" ${enabled ? "" : "disabled"}></vscode-text-field>`
}

function encode(value: string): string {
    return value.replace(/\"/g, "&quot;");
}