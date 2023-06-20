/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
 */

import * as vscode from 'vscode';
import { CommandKey, ID, Message, MessageProp, Properties, Property, PropTypes } from './controlTypes';
import { asClass, assertNever } from '../typesUtil';
import { makeHtmlForProperties } from './propertiesHtmlBuilder';

export class PropertiesView {
	private static readonly COMMAND_GET_NODE_PROPERTIES = "java.node.properties.get";      // NOI18N
	private static readonly COMMAND_SET_NODE_PROPERTIES = "java.node.properties.set";      // NOI18N

	private static extensionUri: vscode.Uri;
	private static scriptPath: vscode.Uri;
	private static panels: Record<ID, PropertiesView> = {};

	private readonly _panel: vscode.WebviewPanel;
	private readonly id: ID;
	private readonly name: string;
	private readonly _disposables: vscode.Disposable[] = [];

	private properties?: Properties;

	public static async createOrShow(context: vscode.ExtensionContext, node: any) {
		if (!node)
			return;
		const id = node.id;
		// If we already have a panel, show it.
		const current = PropertiesView.panels[id];
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
			PropertiesView.panels[id] = new PropertiesView(id, node.tooltip + " " + node.label);
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
		console.log("Props "+this.properties);
	}

	private async get() : Promise<Map<String,Properties>> {
		let resp = await vscode.commands.executeCommand(PropertiesView.COMMAND_GET_NODE_PROPERTIES, this.id);
		if (!resp) {
			// TODO - possibly report protocol error ?
			return new Map<String, Properties>();
		}
		return new Map<String, Properties>(Object.entries(resp));
		
	}

	private save(properties: MessageProp[]) {
		if (!this.properties) {
			return;
		}
		for (const prop of properties)
			this.mergeProps(prop, this.properties?.props);
		let msg = new Map<String,Properties>();
		msg.set(this.properties.propName, this.properties);

		vscode.commands.executeCommand(PropertiesView.COMMAND_SET_NODE_PROPERTIES, this.id, msg);
	}

	private mergeProps(prop: MessageProp, props?: Property[]): void {
		const p = props?.find(p => p.propName === prop.name);
		if (p && PropTypes.includes(p.propType))
			p.propValue = prop.value;
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
