/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
 */
import * as vscode from 'vscode';
import { PropTypes, Properties, Property } from "./controlTypes";

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
    for (const prop of properties.props) {
        html += makePropAccess(prop);
    }
    return html;
}

function makePropAccess(prop: Property): string {
    let out: string;
    switch (prop.propType) {
        case PropTypes.String:
            out = makeStringAccess(prop);
            break;
        case PropTypes.Boolean:
            out = makeBoolAccess(prop);
            break;
        case PropTypes.Properties:
            out = makePropertiesAccess(prop);
            break;
        default:
            out = prop.propValue + "";
            break;
    }
    return wrapToTable(prop.propDispName, out) + '\n';
}

function makeStringAccess(prop: Property<typeof PropTypes.String>) {
    return `<vscode-text-field name="input" id="${prop.propName}" value="${encode(prop.propValue)}" ${prop.propWrite ? "" : "disabled"}></vscode-text-field>`;
}

function makeBoolAccess(prop: Property<typeof PropTypes.Boolean>) {
    return `<vscode-checkbox name="input" id="${prop.propName}" ${prop.propWrite ? "" : "disabled"} ${prop.propValue ? "checked" : ""}></vscode-checkbox>`;
}

function makePropertiesAccess(prop: Property<typeof PropTypes.Properties>) {
    return `<details><summary><b>${prop.propDispName}</b></summary><table name="input" id="${prop.propName}">
    ${makePropTable(prop)}
    </table></details>`;
}

function makePropTable(prop: Property<typeof PropTypes.Properties>) {
    let out = "";
    for (const key in prop.propValue) {
        out += makePropRow(prop, key) + '\n';
    }
    return out;
}

function makePropRow(prop: Property<typeof PropTypes.Properties>, key: string) {
    return wrapToTable(asTextField(key, prop.propWrite, "name"), asTextField(prop.propValue[key], prop.propWrite, "value"), " = ");
}

function asTextField(value: string, enabled: boolean, name: string) {
    return `<vscode-text-field class="${name}" value="${encode(value)}" ${enabled ? "" : "disabled"}></vscode-text-field>`
}

function encode(value: string): string {
    return value.replace(/\"/g, "&quot;");
}