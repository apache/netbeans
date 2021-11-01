import * as vscode from 'vscode';
import {  LanguageClient } from 'vscode-languageclient/node';
import { NodeChangedParams, NodeInfoNotification, NodeInfoRequest } from './protocol';

class TreeViewService {  
  private client : LanguageClient;
  private trees : Map<string, vscode.TreeView<Visualizer>> = new Map();
  private images : Map<number, vscode.Uri> = new Map();
  private providers : Map<number, VisualizerProvider> = new Map();
  constructor (c : LanguageClient) {
    this.client = c;
  }

  getClient() : LanguageClient {
    return this.client;
  }

  async createView(id : string, options? : Partial<vscode.TreeViewOptions<any>>) : Promise<vscode.TreeView<Visualizer>> {
    let tv : vscode.TreeView<Visualizer> | undefined  = this.trees.get(id);
    if (tv) {
      return tv;
    }
    const res = await createViewProvider(id);
    this.providers.set(res.getRoot().data.id, res);
    let opts : vscode.TreeViewOptions<Visualizer> = {
      treeDataProvider : res,
      canSelectMany: true,
      showCollapseAll: true,
    }
    
    if (options?.canSelectMany !== undefined) {
      opts.canSelectMany = options.canSelectMany;
    }
    if (options?.showCollapseAll !== undefined) {
      opts.showCollapseAll = options.showCollapseAll;
    }
    let view = vscode.window.createTreeView(id, opts);
    // this will replace the handler over and over, but never mind
    this.client.onNotification(NodeInfoNotification.type, params => this.nodeChanged(params));
    return view;
  }

  private nodeChanged(params : NodeChangedParams) : void {
    let p : VisualizerProvider | undefined = this.providers.get(params.rootId);
    if (p) {
      p.refresh(params);
    }
  }

  imageUri(nodeData : NodeInfoRequest.Data) : vscode.Uri | undefined {
    if (nodeData.iconUri) {
      const uri : vscode.Uri = vscode.Uri.parse(nodeData.iconUri)
      this.images.set(nodeData.iconIndex, uri);
      return uri;
    } else {
      return this.images.get(nodeData.iconIndex);
    }
  }
}

let treeService : Promise<TreeViewService>;
let promisedTreeService : (ts : TreeViewService) => void;

export function findTreeViewService() : Promise<TreeViewService> {
  if (treeService) {
    return treeService;
  } else {
    return treeService = new Promise((resolve, reject) => {
      promisedTreeService = resolve;
    })
  }
}


class VisualizerProvider implements vscode.TreeDataProvider<Visualizer> {
  private root: Visualizer;
  private treeData : Map<number, Visualizer> = new Map();
  
  constructor(
    private client: LanguageClient,
    private ts : TreeViewService,
    id : string,
    rootData : NodeInfoRequest.Data
  ) {
    this.root = new Visualizer(rootData, ts.imageUri(rootData));
    this.treeData.set(rootData.id, this.root);
  }

  private _onDidChangeTreeData: vscode.EventEmitter<Visualizer | undefined | null | void> = new vscode.EventEmitter<Visualizer | undefined | null | void>();
  readonly onDidChangeTreeData: vscode.Event<Visualizer | undefined | null | void> = this._onDidChangeTreeData.event;
  
  refresh(params : NodeChangedParams): void {
      if (this.root.data.id === params.rootId) {
        if (this.root.data.id == params.nodeId || !params.nodeId) {
          this._onDidChangeTreeData.fire();
        } else {
          let v : Visualizer | undefined = this.treeData.get(params.nodeId);
          if (v) {
              this._onDidChangeTreeData.fire(v);
          }
        }
      }
  }

  getRoot() : Visualizer {
    return this.root;
  }

  getTreeItem(element: Visualizer): vscode.TreeItem {
    return element;
  }

  getChildren(e?: Visualizer): Thenable<Visualizer[]> {
    const self = this;
    async function collectResults(arr: any, element: Visualizer): Promise<Array<Visualizer>> {
      let res = Array<Visualizer>();
      let ch : Map<number, Visualizer> = new Map();
      for (let i = 0; i < arr.length; i++) {
        let d = await self.client.sendRequest(NodeInfoRequest.info, { nodeId : arr[i] });
        let v = new Visualizer(d, self.ts.imageUri(d));
        if (d.command) {
          // PENDING: provide an API to register command (+ parameters) -> command translators.
          if (d.command === 'vscode.open') {
            v.command = { command : d.command, title: '', arguments: [v.resourceUri]};
          } else {
            v.command = { command : d.command, title: '', arguments: [v]};
          }
        }
        v.parent = element;
        res.push(v);
        ch.set(d.id, v);
      }
      element.updateChildren(ch, self);
      return res;
    }

    if (e) {
      return this.client.sendRequest(NodeInfoRequest.children, { nodeId : e.data.id}).then(async (arr) => {
        return collectResults(arr, e);
      });
    } else {
      return this.client.sendRequest(NodeInfoRequest.children, { nodeId: this.root.data.id}).then(async (arr) => {
        return collectResults(arr, this.root);
      });
    }
  }

  removeVisualizers(vis : number[]) {
    let ch : number[] = [];
    vis.forEach(a => {
      let v : Visualizer | undefined = this.treeData.get(a);
      if (v && v.children) {
        ch.push(...v.children.keys());
        this.treeData.delete(a);
      }
    });
    // cascade
    this.removeVisualizers(ch);
  }
}

class Visualizer extends vscode.TreeItem {
  constructor(
    public data : NodeInfoRequest.Data,
    public image : vscode.Uri | undefined
  ) {
    super(data.label, data.collapsibleState);
    this.label = data.label;
    this.description = data.description;
    this.tooltip = data.tooltip;
    this.collapsibleState = data.collapsibleState;
    this.iconPath = image;
    if (data.resourceUri) {
        this.resourceUri = vscode.Uri.parse(data.resourceUri);
    }
    this.contextValue = data.contextValue;
  }
  parent: Visualizer | null = null;
  children: Map<number, Visualizer> | null = null;

  updateChildren(ch : Map<number, Visualizer>, provider : VisualizerProvider) {
    let c : number[] = [];
    if (this.children) {
      for (let k of this.children.keys()) {
        if (!ch.get(k)) {
          c.push(k);
        }
      }
    }
    this.children = ch;
    if (c.length) {
      provider.removeVisualizers(c);
    }
  }
}

function replaceTreeService(ts : TreeViewService) {
  treeService = new Promise((resolve, reject) => {
    resolve(ts);
  });
  if (promisedTreeService) {
    promisedTreeService(ts);
  }
}


export async function createViewProvider(id : string) : Promise<VisualizerProvider> {
  const ts = await findTreeViewService();
  const client = ts.getClient();
  const res = client.sendRequest(NodeInfoRequest.explorermanager, { explorerId: id }).then(node => {
    if (!node) {
      throw "Unsupported view: " + id;
    }
    return new VisualizerProvider(client, ts, id, node);
  });
  if (!res) {
    throw "Unsupported view: " + id;
  }
  return res;
}
/**
 * Creates a view of the specified type or returns an existing one. The View has to be registered in package.json in
 * some workspace position. Waits until the view service initializes.
 * 
 * @param id view ID, consistent with package.json registration
 * @param viewTitle title for the new view, optional.
 * @returns promise of the tree view instance.
 */
export async function createTreeView<T>(viewId: string, viewTitle? : string, options? : Partial<vscode.TreeViewOptions<any>>) : Promise<vscode.TreeView<Visualizer>> {
  return treeService.then(ts => ts.createView(viewId, options).then(v => { 
    if (viewTitle) {
        v.title = viewTitle; 
    }
    return v; 
  }));
}

/**
 * Registers the treeview service with the language server.
 */
export function register(c : LanguageClient) {
    const ts : TreeViewService = new TreeViewService(c);
    replaceTreeService(ts);

    vscode.commands.registerCommand("foundProjects.deleteEntry", async function (this: any, args: any) {
        let v = args as Visualizer;
        let ok = await c.sendRequest(NodeInfoRequest.destroy, { nodeId : v.data.id });
        if (!ok) {
            vscode.window.showErrorMessage('Cannot delete node ' + v.label);
        }
    });
}

