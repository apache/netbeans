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

import { commands, CompletionItem, CompletionList, ExtensionContext, languages, ProviderResult, SnippetString, window, workspace } from 'vscode';
import { InsertTextFormat } from 'vscode-languageclient';
import * as jsoncp from 'jsonc-parser';
import * as fs from 'fs';
import { COMMAND_PREFIX } from "./extension";

export function updateLaunchConfig() {
    workspace.findFiles('.vscode/launch.json').then(async files => {
        const updateOption = 'Update the existing launch.json file(s)';
        let selection: any = undefined;
        for (const file of files) {
            let edits: jsoncp.Edit[] = [];
            const content = fs.readFileSync(file.fsPath, 'utf8');
            const root = jsoncp.parseTree(content);
            root?.children?.forEach(rch => {
                if (rch.type === 'property' && rch.children?.length === 2) {
                    const name = rch.children[0].type === 'string' ? rch.children[0].value : undefined;
                    if (name === 'configurations' && rch.children[1].type === 'array') {
                        rch.children[1].children?.forEach(config => {
                            if (config.type === 'object') {
                                config.children?.forEach(cch => {
                                    if (cch.type === 'property' && cch.children?.length === 2) {
                                        const cname = cch.children[0].type === 'string' ? cch.children[0].value : undefined;
                                        if (cname === 'type' && cch.children[1].type === 'string' && cch.children[1].value === 'java8+') {
                                            const path = jsoncp.getNodePath(cch.children[1]);
                                            if (path) {
                                                edits = edits.concat(jsoncp.modify(content, path, 'java+', {}));
                                            }
                                        }
                                    }
                                });
                            }
                        });
                    }
                }
            });
            const newContent = jsoncp.applyEdits(content, edits);
            if (newContent !== content) {
                if (!selection) {
                    selection = await window.showWarningMessage('Java 8+ debug configuration has been renamed to Java+', updateOption);
                }
                if (selection === updateOption) {
                    fs.writeFileSync(file.fsPath, newContent);
                } else {
                    return;
                }
            }
        };
    });
}

export function registerCompletion(context: ExtensionContext) {
   context.subscriptions.push(languages.registerCompletionItemProvider({ language: 'jsonc', pattern: '**/launch.json' }, {
        provideCompletionItems(document, position, cancelToken) {
            const sourceText = document.getText();
            const root = jsoncp.parseTree(sourceText);
            if (root) {
                const offset = document.offsetAt(position);
                const currentNode = jsoncp.findNodeAtOffset(root, offset);
                if (currentNode) {
                    const path = jsoncp.getNodePath(currentNode);
                    if (path.length >= 1 && 'configurations' == path[0]) {
                        const uri = document.uri.toString();
                        let completionItems: ProviderResult<CompletionList<CompletionItem>> | CompletionItem[];
                        if (path.length == 1) {
                            // Get all configurations:
                            completionItems = commands.executeCommand(COMMAND_PREFIX + '.project.configuration.completion', uri);
                        } else {
                            let node: jsoncp.Node = currentNode;
                            if (currentNode.type == 'property' && currentNode.parent) {
                                let propName = currentNode.children?.[0]?.value;
                                if (!propName) { // Invalid node?
                                    return new CompletionList();
                                }
                                node = currentNode.parent;
                                let attributesMap = getAttributes(node);
                                // Get possible values of property 'propName':
                                completionItems = commands.executeCommand(COMMAND_PREFIX + '.project.configuration.completion', uri, attributesMap, propName);
                            } else {
                                let attributesMap = getAttributes(node);
                                // Get additional possible attributes:
                                completionItems = commands.executeCommand(COMMAND_PREFIX + '.project.configuration.completion', uri, attributesMap);
                            }
                        }
                        return (completionItems as Thenable<CompletionList<CompletionItem>>).then(itemsList => {
                            let items = itemsList.items;
                            if (!items) {
                                items = ((itemsList as unknown) as CompletionItem[]);
                            }
                            addCommas(sourceText, offset, items);
                            return new CompletionList(items);
                        });
                    }
                }
            }
        }
    }));
}

function getAttributes(node: jsoncp.Node) {
    let attributes: any = {};
    if (node.children) {
        for (let index in node.children) {
            let ch = node.children[index];
            let prop = ch.children;
            if (prop) {
                attributes[prop[0].value] = prop[1].value;
            }
        }
    }
    return attributes;
}

function addCommas(sourceText: string, offset: number, completionItems: CompletionItem[]) {
    if (!completionItems) {
        return ;
    }
    let prepend = false;
    let o = offset - 1;
    while (o >= 0) {
        let c = sourceText.charAt(o);
        if (!/\s/.test(c)) {
            prepend = c != '[' && c != '{' && c != ',' && c != ':';
            break;
        }
        o--;
    }
    let append = false;
    o = offset + 1;
    while (o < sourceText.length) {
        let c = sourceText.charAt(o);
        if (!/\s/.test(c)) {
            append = c != ']' && c != '}' && c != ',';
            break;
        }
        o++;
    }
    for (let index in completionItems) {
        let ci = completionItems[index];
        if (ci.insertText) {
            if ((<any> ci).insertTextFormat === InsertTextFormat.Snippet) {
                let snippet = new SnippetString(<string> ci.insertText);
                ci.insertText = snippet;
                if (prepend) {
                    snippet.value = ',' + snippet.value;
                }
                if (append) {
                    snippet.value = snippet.value + ',';
                }
            } else {
                if (prepend) {
                    ci.insertText = ',' + ci.insertText;
                }
                if (append) {
                    ci.insertText = ci.insertText + ',';
                }
            }
        }
        if (ci.kind) {
            ci.kind--; // Note difference between vscode's CompletionItemKind and lsp's CompletionItemKind
        }
    }
}

