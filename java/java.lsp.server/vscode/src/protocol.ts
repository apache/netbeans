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
    ShowMessageParams,
    NotificationType
} from 'vscode-languageclient';

import {
    Position,
    Range
} from 'vscode-languageserver-protocol';

export interface HtmlPageParams {
    id: string;
    text: string;
    pause: boolean;
    resources?: {
        [name: string]: string;
    };
}

export namespace HtmlPageRequest {
    export const type = new ProtocolRequestType<HtmlPageParams, void, never, void, void>('window/showHtmlPage');
};

export namespace ExecInHtmlPageRequest {
    export const type = new ProtocolRequestType<HtmlPageParams, boolean, never, void, void>('window/execInHtmlPage');
};

export interface ShowStatusMessageParams extends ShowMessageParams {
    /**
     * The timeout
     */
    timeout?: number;
}

export interface UpdateConfigParams {
    /**
    * Information specifying configuration update.
    */
    section: string;
    key: string;
    value: string;
}

export namespace UpdateConfigurationRequest {
    export const type = new ProtocolRequestType<UpdateConfigParams, void, never, void, void>('config/update');
}

export namespace StatusMessageRequest {
    export const type = new ProtocolNotificationType<ShowStatusMessageParams, void>('window/showStatusBarMessage');
};

export interface ShowQuickPickParams {
    /**
     * An optional title of the quick pick.
     */
    title?: string;
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
     * An optional title of the input box.
     */
    title?: string;
     /**
     * The text to display underneath the input box.
     */
    prompt: string;
    /**
     * The value to prefill in the input box.
     */
    value: string;
    /**
     * Controls if a password input is shown. Password input hides the typed text.
     */
    password?: boolean;
}

export namespace InputBoxRequest {
    export const type = new ProtocolRequestType<ShowInputBoxParams, string | undefined, never, void, void>('window/showInputBox');
}

export interface ShowMutliStepInputParams {
    /**
     * ID of the input.
     */
    id: string;
    /**
     * An optional title.
     */
    title?: string;
}

export interface InputCallbackParams {
    inputId : string;
    step: number;
    data: { [name: string]: readonly vscode.QuickPickItem[] | string };
}

export interface StepInfo {
	totalSteps: number;
    stepId: string;
}

export type QuickPickStep = StepInfo & ShowQuickPickParams;

export type InputBoxStep = StepInfo & ShowInputBoxParams;

export namespace MutliStepInputRequest {
    export const type = new ProtocolRequestType<ShowMutliStepInputParams, { [name: string]: readonly vscode.QuickPickItem[] | string }, never, void, void>('window/showMultiStepInput');
    export const step = new ProtocolRequestType<InputCallbackParams, QuickPickStep | InputBoxStep | undefined, never, void, void>('input/step');
    export const validate = new ProtocolRequestType<InputCallbackParams, string | undefined, never, void, void>('input/validate');
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
}

export interface SaveDocumentRequestParams {
    documents: string[];
}

export namespace SaveDocumentsRequest {
    export const type = new ProtocolRequestType<SaveDocumentRequestParams, boolean, never, void, void>('window/documentSave');
}

export interface NodeChangedParams {
    rootId : number;
    nodeId : number | null;
    types? : NodeChangeType[];
    properties? : String[];
}

export interface CreateExplorerParams {
    explorerId : string;
}

export interface NodeOperationParams {
    nodeId : number;
}

export interface ProjectActionParams {
    action : string;
    configuration? : string;
    fallback? : boolean;
}

export interface GetResourceParams {
    uri : vscode.Uri;
    acceptEncoding? : string[];
    acceptContent? : string[];
}

export interface ResourceData {
    contentType : string;
    encoding : string;
    content : string;
    contentSize : number;
}

export interface FindPathParams {
    rootNodeId : number;
    uri? : vscode.Uri;
    selectData? : any;
}

export enum NodeChangeType {
    SELF = 0,
    PROPERTY,
    CHILDEN,
    DESTROY
};

export interface NodeChangesParams {
    rootId: number;
    nodeId?: number;
    deactivateListeners?: number[];
    types?: NodeChangeType[];
}

export namespace NodeInfoNotification {
    export const type = new ProtocolNotificationType<NodeChangedParams, void>('nodes/nodeChanged');
}

export namespace NodeInfoRequest {
    export const explorermanager = new ProtocolRequestType<CreateExplorerParams, never, Data, void, void>('nodes/explorermanager');
    export const info = new ProtocolRequestType<NodeOperationParams, Data, never,void, void>('nodes/info');
    export const children = new ProtocolRequestType<NodeOperationParams, number[], never, void, void>('nodes/children');
    export const destroy = new ProtocolRequestType<NodeOperationParams, boolean, never, void, void>('nodes/delete');
    export const collapsed = new ProtocolNotificationType<NodeOperationParams, void>('nodes/collapsed');
    export const getresource = new ProtocolRequestType<GetResourceParams, ResourceData, never, void, void>('nodes/getresource');
    export const findparams = new ProtocolRequestType<FindPathParams, number[], never, void, void>('nodes/findpath');
    export const changes = new ProtocolRequestType<NodeChangesParams, number, never, void, void>('nodes/changes');
    
    export interface IconDescriptor {
        baseUri : vscode.Uri;
    }
    export interface Data {
        id : number; /* numeric ID of the node */
        name : string; /* Node.getName() */
        label : string; /* Node.getDisplayName() */
        tooltip? : string; 
        description : string; /* Node.getShortDescription() */
        resourceUri? : string; /* external URL to file: resource */
        collapsibleState : vscode.TreeItemCollapsibleState;
        canDestroy : boolean; /* Node.canDestroy() */
        contextValue : string; /* Node.getCookies() */
        iconDescriptor? : IconDescriptor;
        iconUri : string | null;
        iconIndex : number;
        command? : string;
    }
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
