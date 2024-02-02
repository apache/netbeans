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

import * as vscode from 'vscode';
import { CommandKey, ID, Message, PropertyMessage, Properties, Property, PropertyTypes } from './controlTypes';
import { assertNever, isObject, isRecord, isString, IsType } from '../typesUtil';
import { makeHtmlForProperties } from './propertiesHtmlBuilder';
import { TreeViewService, TreeNodeListener, Visualizer } from '../explorer';
import { NodeChangeType } from '../protocol';
import { COMMAND_PREFIX } from '../extension';

function isVisualizer(node : any) : node is Visualizer {
	return node?.id && node?.rootId;
}
export class PropertiesView {
	private readonly COMMAND_GET_NODE_PROPERTIES = COMMAND_PREFIX + ".node.properties.get";      // NOI18N
	private readonly COMMAND_SET_NODE_PROPERTIES = COMMAND_PREFIX + ".node.properties.set";      // NOI18N

	private static extensionUri: vscode.Uri;
	private static scriptPath: vscode.Uri;
	private static panels: Record<ID, PropertiesView> = {};

	private readonly _panel: vscode.WebviewPanel;
	private readonly id: ID;
	private readonly name: string;
	private readonly _disposables: vscode.Disposable[] = [];

	private properties?: Properties;

	public static async createOrShow(context: vscode.ExtensionContext, node: any, treeService? : TreeViewService) {
		if (!node)
			return;
		if (!isVisualizer(node)) {
			return;
		}
		const id = node.id ? Number(node.id) : 0;
		// If we already have a panel, show it.
		const current = PropertiesView.panels[id];

		let view : PropertiesView;

		// the listener will remove/close the properties view, if the associated node gets destroyed.
		class L implements TreeNodeListener {
			nodeDestroyed(n : Visualizer) : void {
				if (view) {
					/*
					vscode.window.showInformationMessage(`${node.label} has been removed.`);
					*/
					view.dispose();
				}
			}
		}

		try {
			if (current) {
				await current.load();
				current._panel.reveal();
				return;
			}
			if (!PropertiesView.extensionUri) {
				PropertiesView.extensionUri = context.extensionUri;
				PropertiesView.scriptPath = vscode.Uri.joinPath(context.extensionUri, 'out', 'script.js');
			} else if (PropertiesView.extensionUri !== context.extensionUri)
				throw new Error("Extension paths differ.");
			// Otherwise, create a new panel.
			PropertiesView.panels[id] = view = new PropertiesView(id, node.tooltip + " " + node.label);

			if (treeService) {
				treeService.addNodeChangeListener(node, new L(), NodeChangeType.DESTROY);
			}
		} catch (e: unknown) {
			console.log(e);
		}
	}

	private constructor(id: ID, name: string) {
		this.id = id;
		this.name = name;
		this._panel = vscode.window.createWebviewPanel('Properties', 'Properties', vscode.ViewColumn.One, {
			// Enable javascript in the webview
			enableScripts: true,
		});

		// Set the webview's html content
		this.load().then(() =>
			this.setHtml()).catch((e) => {
				console.error(e);
				this.dispose();
			});

		// Listen for when the panel is disposed
		// This happens when the user closes the panel or when the panel is closed programatically
		this._panel.onDidDispose(() => this.dispose(), null, this._disposables);

		// Update the content based on view changes
		this._panel.onDidChangeViewState(
			() => {
				if (this._panel.visible) {
					try {
						this.setHtml();
					} catch (e: unknown) {
						console.error(e);
						this.dispose();
					}
				}
			},
			null,
			this._disposables
		);

		// Handle messages from the webview
		this._panel.webview.onDidReceiveMessage(
			message => {
				try {
					this.processMessage(message);
				} catch (e: unknown) {
					console.error(e);
					this.dispose();
				}
			},
			undefined,
			this._disposables
		);
	}

	private async load() {
		const props = await this.get();
		if (props.size === 0) {
			throw new Error("No properties.");
		}
		this.properties = props.values().next().value;
	}

	private async get(): Promise<Map<String, Properties>> {
		const resp = await vscode.commands.executeCommand(this.COMMAND_GET_NODE_PROPERTIES, this.id);
		if (!isObject(resp)) {
			// TODO - possibly report protocol error ?
			return new Map<String, Properties>();
		}
		return new Map<String, Properties>(Object.entries(resp)); // TODO - validate cast
	}

	private save(properties: PropertyMessage[]) {
		if (!this.properties) return;

		for (const prop of properties)
			this.mergeProps(prop, this.properties?.properties);

		const msg: Record<string, Properties> = {};
		msg[this.properties.name] = this.properties;

		vscode.commands.executeCommand(this.COMMAND_SET_NODE_PROPERTIES, this.id, msg)
			.then(done => {
				if (isRecord(isRecord.bind(null, isString) as IsType<Record<string, string>>, done)) {
					this.processSaveError(done);
				}
			}, err => vscode.window.showErrorMessage(err.message, { modal: true, detail: err.stack }));
	}

	private processSaveError(errObj: Record<string, Record<string, string>>) {
		if (Object.keys(errObj).length === 0)
			return;
		let out = "";
		for (const propertiesName of Object.keys(errObj)) {
			for (const property of Object.entries(errObj[propertiesName]))
				out += `${propertiesName}.${property[0]}: ${property[1]}\n`;
		}
		vscode.window.showErrorMessage("Saving of properties failed.", { modal: true, detail: out });
	}

	private mergeProps(prop: PropertyMessage, props?: Property[]): void {
		const p = props?.find(p => p.name === prop.name);
		if (p && Object.values(PropertyTypes).includes(p.type))
			p.value = prop.value;
	}

	private processMessage(message: Message) {
		switch (message._type) {
			case CommandKey.Save:
				this.save(message.properties);
			case CommandKey.Cancel:
				this.dispose();
				break;
			case CommandKey.Error:
				console.error(message.error);
				if (message.stack)
					console.error(message.stack);
				this.dispose();
				break;
			case CommandKey.Info:
				console.log(message.info);
				break;
			default:
				assertNever(message, "Got unknown message: " + JSON.stringify(message));
		}
	}

	private static getNonce() {
		let text = "";
		const possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		for (let i = 0; i < 32; i++) {
			text += possible.charAt(Math.floor(Math.random() * possible.length));
		}
		return text;
	}

	private setHtml() {
		if (!this.properties)
			throw new Error("No properties to show.");
		const script = this._panel.webview.asWebviewUri(PropertiesView.scriptPath);
		const html = makeHtmlForProperties(this.name, PropertiesView.getNonce(), script, this.properties);
		this._panel.webview.html = html;
	}

	public dispose() {
		delete PropertiesView.panels[this.id];
		// Clean up our resources
		this._panel.dispose();
		while (this._disposables.length) {
			const x = this._disposables.pop();
			if (x) {
				x.dispose();
			}
		}
	}
}
