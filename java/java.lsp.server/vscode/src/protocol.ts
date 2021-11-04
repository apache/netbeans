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
'use strict';

import * as vscode from 'vscode';
import {
    ProtocolNotificationType,
    ProtocolRequestType,
    ShowMessageParams
} from 'vscode-languageclient';
import {
    Position,
    Range
} from 'vscode-languageserver-protocol';

export interface ShowStatusMessageParams extends ShowMessageParams {
    /**
     * The timeout
     */
    timeout?: number;
}

export namespace StatusMessageRequest {
    export const type = new ProtocolNotificationType<ShowStatusMessageParams, void>('window/showStatusBarMessage');
};

export interface ShowQuickPickParams {
    /**
     * A string to show as placeholder in the input box to guide the user what to pick on.
     */
    placeHolder: string;
    /**
     * An optional flag to make the picker accept multiple selections.
     */
    canPickMany?: boolean;
    /**
     * A list of items.
     */
    items: vscode.QuickPickItem[];
}

export namespace QuickPickRequest {
    export const type = new ProtocolRequestType<ShowQuickPickParams, vscode.QuickPickItem[], never, void, void>('window/showQuickPick');
}

export interface ShowInputBoxParams {
    /**
     * The text to display underneath the input box.
     */
    prompt: string;
    /**
     * The value to prefill in the input box.
     */
    value: string;
}

export namespace InputBoxRequest {
    export const type = new ProtocolRequestType<ShowInputBoxParams, string | undefined, never, void, void>('window/showInputBox');
}

export interface TestProgressParams {
    uri: string;
    suite: TestSuite;
}

export interface TestSuite {
    name: string;
    file?: string;
    range?: Range;
    state: 'loaded' | 'started' | 'passed' | 'failed' | 'skipped' | 'errored';
    tests?: TestCase[];
}

export interface TestCase {
    id: string;
    name: string;
    file?: string;
    range?: Range;
    state: 'loaded' | 'started' | 'passed' | 'failed' | 'skipped' | 'errored';
    stackTrace?: string[];
}

export namespace TestProgressNotification {
    export const type = new ProtocolNotificationType<TestProgressParams, void>('window/notifyTestProgress');
};

export interface DebugConnector {
    id: string;
    name: string;
    type: string;
    arguments: string[];
    defaultValues: string[];
    descriptions: string[];
}

export interface SetTextEditorDecorationParams {
    key: string;
    uri: string;
    ranges: Range[];
};

export namespace TextEditorDecorationCreateRequest {
    export const type = new ProtocolRequestType<vscode.DecorationRenderOptions, never, string, void, void>('window/createTextEditorDecoration');
};

export namespace TextEditorDecorationSetNotification {
    export const type = new ProtocolNotificationType<SetTextEditorDecorationParams, void>('window/setTextEditorDecoration');
};

export namespace TextEditorDecorationDisposeNotification {
    export const type = new ProtocolNotificationType<string, void>('window/disposeTextEditorDecoration');
};

export function asPosition(value: undefined | null): undefined;
export function asPosition(value: Position): vscode.Position;
export function asPosition(value: Position | undefined | null): vscode.Position | undefined;
export function asPosition(value: Position | undefined | null): vscode.Position | undefined {
    if (!value) {
        return undefined;
    }
    return new vscode.Position(value.line, value.character);
}

export function asRange(value: undefined | null): undefined;
export function asRange(value: Range): vscode.Range;
export function asRange(value: Range | undefined | null): vscode.Range | undefined;
export function asRange(value: Range | undefined | null): vscode.Range | undefined {
    if (!value) {
        return undefined;
    }
    return new vscode.Range(asPosition(value.start), asPosition(value.end));
}

export function asRanges(value: Range[]): vscode.Range[] {
    return value.map(value => asRange(value));
}
