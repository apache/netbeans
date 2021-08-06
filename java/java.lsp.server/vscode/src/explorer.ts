import * as vscode from 'vscode';
import { LanguageClient } from 'vscode-languageclient';
import { NodeInfoRequest, NodeQueryRequest } from './protocol';

class VisualizerProvider implements vscode.TreeDataProvider<Visualizer> {
  private root: Promise<Visualizer>;
  private known: Visualizer[] = [];

  constructor(
    private client: LanguageClient,
    id : string
  ) {
    this.root = new Promise((resolve, reject) => {
      client.sendRequest(NodeInfoRequest.explorermanager, id).then((node) => {
        resolve(new Visualizer(this.known, node));
        client.onNotification(NodeInfoRequest.notifyChange, (params) => {
          this.refresh(params);
        })
      }).catch((ex) => {
        vscode.window.showErrorMessage(`Cannot initialize ${ex} tree data provider`);
        reject(ex);
      });
    });
  }

  private _onDidChangeTreeData: vscode.EventEmitter<Visualizer | undefined | null | void> = new vscode.EventEmitter<Visualizer | undefined | null | void>();
  readonly onDidChangeTreeData: vscode.Event<Visualizer | undefined | null | void> = this._onDidChangeTreeData.event;

  refresh(id : number): void {
      for (const v of this.known) {
          if (v.data.id === id) {
              this._onDidChangeTreeData.fire(v);
          }
      }
  }

  getTreeItem(element: Visualizer): vscode.TreeItem {
    return element;
  }

  getChildren(e?: Visualizer): Thenable<Visualizer[]> {
    const self = this;
    async function collectResults(arr: any, element: Visualizer): Promise<Array<Visualizer>> {
      let res = Array<Visualizer>();
      for (let i = 0; i < arr.length; i++) {
        let d = await self.client.sendRequest(NodeInfoRequest.info, arr[i]);
        let v = new Visualizer(self.known, d);
        v.parent = element;
        res.push(v);
      }
      return res;
    }

    if (e) {
      return this.client.sendRequest(NodeInfoRequest.children, e.data.id).then(async (arr) => {
        return collectResults(arr, e);
      });
    } else {
      return this.root.then(async (element: Visualizer) => {
        const arr = await this.client.sendRequest(NodeInfoRequest.children, element.data.id);
        const res = await collectResults(arr, element);
        return res;
      });
    }
  }
}

class Visualizer extends vscode.TreeItem {
  constructor(
    known : Visualizer[],
    public data : NodeInfoRequest.Data
  ) {
    super(data.label, data.collapsibleState);
    known.push(this);
    this.label = data.label;
    this.description = data.description;
    this.collapsibleState = data.collapsibleState;
    if (data.resourceUri) {
        this.resourceUri = vscode.Uri.parse(data.resourceUri);
    }
    this.contextValue = "node";
  }
  parent: Visualizer | null = null;
}

export function foundProjects(c : LanguageClient): vscode.TreeDataProvider<any> {
    return new VisualizerProvider(c, "foundProjects");
}

export function register(c : LanguageClient) {
    let vtp = foundProjects(c);
    let view = vscode.window.createTreeView(
      'foundProjects', {
        treeDataProvider: vtp,
        canSelectMany: true,
        showCollapseAll: true,
      }
    );
    view.message = "Projects view!";
    view.onDidChangeSelection((ev) => {
      if (ev.selection.length > 0) {
          view.message = `Selected ${ev.selection[0].label}`;
      }
    });
    view.title = "Found projects!";

    vscode.commands.registerCommand("foundProjects.deleteEntry", async function (this: any, args: any) {
        let v = args as Visualizer;
        let ok = await c.sendRequest(NodeInfoRequest.destroy, v.data.id);
        if (!ok) {
            vscode.window.showErrorMessage('Cannot delete node ' + v.label);
        }
    }, vtp);
}

