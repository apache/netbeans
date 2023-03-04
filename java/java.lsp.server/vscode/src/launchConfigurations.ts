/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
'use strict';

import { commands, CompletionItem, CompletionList, ExtensionContext, languages, ProviderResult, SnippetString } from 'vscode';
import { InsertTextFormat } from 'vscode-languageclient';
import * as jsoncp from 'jsonc-parser';

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
                            completionItems = commands.executeCommand('java.project.configuration.completion', uri);
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
                                completionItems = commands.executeCommand('java.project.configuration.completion', uri, attributesMap, propName);
                            } else {
                                let attributesMap = getAttributes(node);
                                // Get additional possible attributes:
                                completionItems = commands.executeCommand('java.project.configuration.completion', uri, attributesMap);
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

function getAttributesMap(node: jsoncp.Node) {
    let attributes = new Map<string, object>();
    if (node.children) {
        for (let index in node.children) {
            let ch = node.children[index];
            let prop = ch.children;
            if (prop) {
                attributes.set(prop[0].value, prop[1].value);
            }
        }
    }
    return attributes;
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
    }
}

