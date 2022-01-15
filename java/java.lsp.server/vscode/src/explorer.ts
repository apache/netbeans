import { disconnect } from 'process';
import * as vscode from 'vscode';
import { ThemeIcon } from 'vscode';
import {  LanguageClient } from 'vscode-languageclient/node';
import { NbLanguageClient } from './extension';
import { NodeChangedParams, NodeInfoNotification, NodeInfoRequest } from './protocol';

const doLog : boolean = false;

/**
 * Cached image information.
 */
class CachedImage {
  constructor(
    /**
     * Base URI of the image, if available.
     */
    public baseUri? : string,

    /**
     * Icon URI as sent by the LSP server. Images translated to ThemeIcons have this field undefined.
     */
    public iconUri? : vscode.Uri,

    /**
     * Local resource or theme icon.
     */
    public icon? : string | ThemeIcon,

    /**
     * Additional matched values
     */
    public values? : string[],
  ) {}
}

export class TreeViewService extends vscode.Disposable {  
  
  private handler : vscode.Disposable | undefined;
  private client : NbLanguageClient;
  private trees : Map<string, vscode.TreeView<Visualizer>> = new Map();
  private images : Map<number, CachedImage> = new Map();
  private providers : Map<number, VisualizerProvider> = new Map();
  log : vscode.OutputChannel;
  private entries : ImageEntry[] = [];

  constructor (log : vscode.OutputChannel, c : NbLanguageClient, dd : vscode.Disposable[]) {
    super(() => { 
      this.disposeAllViews(); 
      for (const d of dd) {
        d?.dispose();
      }
    });
    this.log = log;
    this.client = c;

    this.refreshImages();
    dd.push(vscode.extensions.onDidChange(() => this.refreshImages()));
  }

  getClient() : NbLanguageClient {
    return this.client;
  }

  private disposeAllViews() : void {
    for (let tree of this.trees.values()) {
      tree.dispose();
    }
    for (let provider of this.providers.values()) {
      provider.dispose();
    }
    this.trees.clear();
    this.providers.clear();
    this.handler?.dispose();
  }

  public async createView(id : string, title? : string, options? : 
      Partial<vscode.TreeViewOptions<any> & { 
          providerInitializer : (provider : CustomizableTreeDataProvider<Visualizer>) => void }
      >) : Promise<vscode.TreeView<Visualizer>> {
    let tv : vscode.TreeView<Visualizer> | undefined  = this.trees.get(id);
    if (tv) {
      return tv;
    }
    const res = await createViewProvider(this.client, id);
    this.providers.set(res.getRoot().data.id, res);
    options?.providerInitializer?.(res)
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
    this.trees.set(id, view);
    // this will replace the handler over and over, but never mind
    this.handler = this.client.onNotification(NodeInfoNotification.type, params => this.nodeChanged(params));
    return view;
  }

  private nodeChanged(params : NodeChangedParams) : void {
    let p : VisualizerProvider | undefined = this.providers.get(params.rootId);
    if (p) {
      p.refresh(params);
    }
  }

  imageUri(nodeData : NodeInfoRequest.Data) : vscode.Uri | string | ThemeIcon | undefined {
    let ci : CachedImage | undefined;
    if (nodeData.iconIndex) {
      ci = this.images.get(nodeData.iconIndex);
      if (ci && ci.baseUri) {
        // hack because of bad protocol: will be fixed when client actively asks for icon data and server will not cache icons.
        const r = this.findProductIcon(ci.baseUri, nodeData.name, nodeData.contextValue);
        if (r) {
          return r;
        }
      }
    }

    if (!ci) {
      if (nodeData.iconDescriptor?.baseUri) {
        const r = this.findProductIcon(nodeData.iconDescriptor.baseUri, nodeData.name, nodeData.contextValue);
        // override the icon with local.
        if (r) {
          ci = new CachedImage(nodeData.iconDescriptor.baseUri, undefined, r, [ nodeData.name, nodeData.contextValue ]);
          this.images.set(nodeData.iconIndex, ci);
        }
      }
      if (!ci) {
        // hardcode visual vscode's File icons for regular files:
        if (nodeData.resourceUri && nodeData.contextValue.includes('is:file')) {
          const uri : vscode.Uri | undefined  = nodeData.iconUri ? vscode.Uri.parse(nodeData.iconUri) : undefined;
          // do not cache
          return ThemeIcon.File;
        }
      }
      if (!ci && nodeData.iconUri) {
          const uri : vscode.Uri = vscode.Uri.parse(nodeData.iconUri);
          ci = new CachedImage(nodeData?.iconDescriptor?.baseUri, uri, undefined);
          this.images.set(nodeData.iconIndex, ci);
      }
    }
    return ci?.icon ? ci.icon : ci?.iconUri;
  }

  public setTranslations(entries : ImageEntry[]) {
    this.entries = entries;
  }

  public findProductIcon(res : string, ...values: string[]) : string | ThemeIcon | undefined {
  outer: for (let e of this.entries) {
      if (e.uriRegexp.exec(res)) {
        if (e.valueRegexps) {
          let s : string = " " + values.join(" ") + " ";
          for (let vr of e.valueRegexps) {
            if (!vr.exec(s)) {
              continue outer;
            }
          }
        }
        if (e.codeicon === '*file') {
          return ThemeIcon.File;
        } else if (e.codeicon == '*folder') {
          return ThemeIcon.Folder;
        } else if (e.iconPath) {
          return e.iconPath;
        }
        return new ThemeIcon(e.codeicon);
      }
    }
    return undefined;
  }

  public refreshImages() {
    let newEntries : ImageEntry[] = [];
    for (const ext of vscode.extensions.all) {
      const iconMapping = ext.packageJSON?.contributes && ext.packageJSON?.contributes['netbeans.iconMapping'];
      if (Array.isArray(iconMapping)) {
        for (const m of iconMapping) {
          const reString = m?.uriExpression;
          if (reString) {
            try {
              let re : RegExp = new RegExp(reString);
              let vals = [];
              if (m?.valueMatch) {
                for (const vm of m.valueMatch) {
                  const re = new RegExp(vm);
                  vals.push(re);
                }
              }
              newEntries.push(new ImageEntry(re, m?.codeicon, m?.iconPath, vals));
            } catch (e) {
              console.log("Invalid icon mapping in extension %s: %s -> %s", ext.id, reString, m?.codicon);
            }
          }
        }
      }
    }
    this.setTranslations(newEntries);
  }
}

export interface TreeItemDecorator<T> extends vscode.Disposable {
  decorateTreeItem(element: T, item : vscode.TreeItem): vscode.TreeItem | Thenable<vscode.TreeItem>;
}

export interface CustomizableTreeDataProvider<T> extends vscode.TreeDataProvider<T> {
  fireItemChange(item? : T) : void;
  addItemDecorator(deco : TreeItemDecorator<T>) : vscode.Disposable;
}

class VisualizerProvider extends vscode.Disposable implements CustomizableTreeDataProvider<Visualizer> {
  private root: Visualizer;
  private treeData : Map<number, Visualizer> = new Map();
  private decorators : TreeItemDecorator<Visualizer>[] = [];
  
  constructor(
    private client: LanguageClient,
    private ts : TreeViewService,
    private log : vscode.OutputChannel,
    id : string,
    rootData : NodeInfoRequest.Data
  ) {
    super(() => this.disconnect());
    this.root = new Visualizer(rootData, ts.imageUri(rootData));
    this.treeData.set(rootData.id, this.root);
  }

  private _onDidChangeTreeData: vscode.EventEmitter<Visualizer | undefined | null | void> = new vscode.EventEmitter<Visualizer | undefined | null | void>();
  readonly onDidChangeTreeData: vscode.Event<Visualizer | undefined | null | void> = this._onDidChangeTreeData.event;

  private disconnect() : void {
    // nothing at the moment.
    for (let deco of this.decorators) {
      deco.dispose();
    }
  }

  fireItemChange(item : Visualizer | undefined) : void {
    if (doLog) {
      this.log.appendLine(`Firing change on ${item?.idstring()}`);
    }
    if (!item || item == this.root) {
      this._onDidChangeTreeData.fire();
    } else {
      this._onDidChangeTreeData.fire(item);
    }
  }

  addItemDecorator(decoInstance : TreeItemDecorator<Visualizer>) : vscode.Disposable {
    this.decorators.push(decoInstance);
    const self = this;
    return new vscode.Disposable(() => {
      const idx = this.decorators.indexOf(decoInstance);
      if (idx > 0) {
        this.decorators.splice(idx, 1);
        decoInstance.dispose();
      }
    });
  }

  refresh(params : NodeChangedParams): void {
      if (this.root.data.id === params.rootId) {
        let v : Visualizer | undefined;
        if (this. root.data.id == params.nodeId || !params.nodeId) {
          v = this.root;
        } else {
          v = this.treeData.get(params.nodeId);
        }
        if (v) {
          if (this.delayedFire.has(v)) {
            if (doLog) {
              this.log.appendLine(`Delaying change on ${v.idstring()}`);
            }
            v.pendingChange = true;
          } else {
            this.fireItemChange(v);
          }
        }
      }
  }

  getRoot() : Visualizer {
    return this.root.copy();
  }

  getTreeItem(element: Visualizer): vscode.TreeItem | Thenable<vscode.TreeItem> {
    const n : number = Number(element.id);
    const self = this;
    if (doLog) {
      this.log.appendLine(`Doing getTreeItem on ${element.idstring()}`);
    }

    return this.wrap(async (arr) => {
      let fetched = await this.queryVisualizer(element, arr, () => this.fetchItem(n));
      element.update(fetched);
      return self.getTreeItem2(fetched);
    });
  }

  /**
   * Wraps code that queries individual Visualizers so that blocked changes are fired after
   * the code terminated.
   * 
   * Usage:
   * wrap(() => { ... code ... ; queryVisualizer(vis, () => { ... })});
   * @param fn the code to execute
   * @returns value of the code function
   */
  async wrap<X>(fn : (pending : Visualizer[]) => Thenable<X>) : Promise<X> {
    let arr : Visualizer[] = [];
    try {
      return await fn(arr);
    } finally {
      this.releaseVisualizersAndFire(arr);
    }
  }

  /**
   * Just creates a string list from visualizer IDs. Diagnostics only.
   */
  private visualizerList(arr : Visualizer[]) : string {
    let s = "";
    for (let v of arr) {
      s += v.idstring() + " ";
    }
    return s;
  }

  /**
   * Do not use directly, use wrap(). Fires delayed events for visualizers that have no pending queries.
   */
  private releaseVisualizersAndFire(list : Visualizer[] | undefined) {
    if (!list) {
      list = Array.from(this.delayedFire);
    }
    if (doLog) {
      this.log.appendLine(`Done with ${this.visualizerList(list)}`);
    }
    // v can be in list several times, each push increased its counter, so we need to decrease it.
    for (let v of list) {
      if (this.treeData?.get(Number(v.id || -1)) === v) {
        if (--v.pendingQueries) {
          if (doLog) {
            this.log.appendLine(`${v.idstring()} has pending ${v.pendingQueries} queries`);
          }
          continue;
        }
        if (v.pendingChange) {
          if (doLog) {
            this.log.appendLine(`Fire delayed change on ${v.idstring()}`);
          }
          this.fireItemChange(v);
          v.pendingChange = false;
        }
      }
      this.delayedFire.delete(v);
    }
    if (doLog) {
      this.log.appendLine("Pending queue: " + this.visualizerList(Array.from(this.delayedFire)));
      this.log.appendLine("---------------");
    }
  }

  /**
   * Should wrap calls to NBLS for individual visualizers (info, children). Puts visualizer on the delayed fire list.
   * Must be itself wrapped in wrap() -- wrap(... queryVisualizer()).
   * @param element visualizer to be queried, possibly undefined (new item is expected)
   * @param fn code to execute
   * @returns code's result
   */
  async queryVisualizer<X>(element : Visualizer | undefined, pending : Visualizer[], fn : () => Promise<X>) : Promise<X> {
    if (!element) {
      return fn();
    }
    this.delayedFire.add(element);
    pending.push(element);
    element.pendingQueries++;
    if (doLog) {
      this.log.appendLine(`Delaying visualizer ${element.idstring()}, queries = ${element.pendingQueries}`)
    }
    return fn();
  }

  async getTreeItem2(element: Visualizer): Promise<vscode.TreeItem> {
    const n = Number(element.id);
    if (this.decorators.length == 0) {
     return element;
    }
    let list : TreeItemDecorator<Visualizer>[] = [...this.decorators];
    
    async function f(item : vscode.TreeItem) : Promise<vscode.TreeItem> {
      const deco = list.shift();
      if (!deco) {
       return item;
      }
      const decorated = deco.decorateTreeItem(element, item);
      if (decorated instanceof vscode.TreeItem) {
          return f(decorated);
      } else {
         return (decorated as Thenable<vscode.TreeItem>).then(f);
      }
    }
    return f(element.copy());
  }

  delayedFire : Set<Visualizer> = new Set<Visualizer>();

  async fetchItem(n : number) : Promise<Visualizer> {
    let d = await this.client.sendRequest(NodeInfoRequest.info, { nodeId : n });
    let v = new Visualizer(d, this.ts.imageUri(d));
    if (d.command) {
      // PENDING: provide an API to register command (+ parameters) -> command translators.
      if (d.command === 'vscode.open') {
        v.command = { command : d.command, title: '', arguments: [v.resourceUri]};
      } else {
        v.command = { command : d.command, title: '', arguments: [v]};
      }
    }
    return v;
  }

  getChildren(e?: Visualizer): Thenable<Visualizer[]> {
    const self = this;

    if (doLog) {
      this.log.appendLine(`Doing getChildren on ${e?.idstring()}`);
    }

    async function collectResults(list : Visualizer[], arr: any, element: Visualizer): Promise<Visualizer[]> {
      let res : Visualizer[] = [];
      let now : Visualizer[] | undefined;
      for (let i = 0; i < arr.length; i++) {
        const old : Visualizer | undefined = self.treeData.get(arr[i]);
        res.push(
          await self.queryVisualizer(old, list, () => self.fetchItem(arr[i]))
        );
      }
      now = element.updateChildren(res, self);
      for (let i = 0; i < arr.length; i++) {
        const v = now[i];
        const n : number = Number(v.id || -1);
        self.treeData.set(n, v);
        v.parent = element;
      }
      return now || [];
    }

    return self.wrap((list) => self.queryVisualizer(e, list, () => {
        if (e) {
          return this.client.sendRequest(NodeInfoRequest.children, { nodeId : e.data.id}).then(async (arr) => {
            return collectResults(list, arr, e);
          });
        } else {
          return this.client.sendRequest(NodeInfoRequest.children, { nodeId: this.root.data.id}).then(async (arr) => {
            return collectResults(list, arr, this.root);
          });
        }
      }
    ));
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
    if (ch.length > 0) {
      this.removeVisualizers(ch);
    }
  }
}

let visualizerSerial = 1;

export class Visualizer extends vscode.TreeItem {

  visId : number;
  pendingQueries : number = 0;
  pendingChange : boolean = false;
  constructor(
    public data : NodeInfoRequest.Data,
    public image : vscode.Uri | string | ThemeIcon | undefined
  ) {
    super(data.label, data.collapsibleState);
    this.visId = visualizerSerial++;
    this.id = "" + data.id;
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

  copy() : Visualizer {
    let v : Visualizer = new Visualizer(this.data, this.image);
    v.id = this.id;
    v.label = this.label;
    v.description = this.description;
    v.tooltip = this.tooltip;
    v.iconPath = this.iconPath;
    v.resourceUri = this.resourceUri;
    v.contextValue = this.contextValue;
    return v;
  }

  parent: Visualizer | null = null;
  children: Map<number, Visualizer> | null = null;

  idstring() : string {
    return `[${this.id} : ${this.visId} - "${this.label}"]`;
  }

  update(other : Visualizer) : Visualizer {
    this.label = other.label;
    this.description = other.description;
    this.tooltip = other.tooltip;
    this.collapsibleState = other.collapsibleState;
    this.iconPath = other.iconPath;
    this.resourceUri = other.resourceUri;
    this.contextValue = other.contextValue;
    this.data = other.data;
    this.image = other.image;
    this.collapsibleState = other.collapsibleState;
    this.command = other.command;
    return this;
  }

  updateChildren(newChildren : Visualizer[], provider : VisualizerProvider) : Visualizer[] {
    let toRemove : number[] = [];
    let ch : Map<number, Visualizer> = new Map();

    for (let i = 0; i < newChildren.length; i++) {
      let c = newChildren[i];
      const n : number = Number(c.id || -1);
      const v : Visualizer | undefined = this.children?.get(n);
      if (v) {
        v.update(c);
        newChildren[i] = c = v;
      }
      ch.set(n, c);
    }

    if (this.children) {
      for (let k of this.children.keys()) {
        if (!ch.get(k)) {
          toRemove.push(k);
        }
      }
    }
    this.children = ch;
    if (toRemove.length) {
      provider.removeVisualizers(toRemove);
    }
    return newChildren;
  }
}

class ImageEntry {
  constructor(
    readonly uriRegexp : RegExp,
    readonly codeicon : string,
    readonly iconPath? : string,
    readonly valueRegexps? : RegExp[]
    ) {}
}
class ImageTranslator {
  private entries : ImageEntry[] = [];

  public setTranslations(entries : ImageEntry[]) {
    this.entries = entries;
  }

  public findProductIcon(res : string) : string | undefined {
    for (let e of this.entries) {
      if (e.uriRegexp.exec(res)) {
        return e.codeicon;
      }
    }
    return undefined;
  }
}

export async function createViewProvider(c : NbLanguageClient, id : string) : Promise<VisualizerProvider> {
  const ts = c.findTreeViewService();
  const client = ts.getClient();
  const res = client.sendRequest(NodeInfoRequest.explorermanager, { explorerId: id }).then(node => {
    if (!node) {
      throw "Unsupported view: " + id;
    }
    return new VisualizerProvider(client, ts, ts.log, id, node);
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
export async function createTreeView<T>(c: NbLanguageClient, viewId: string, viewTitle? : string, options? : Partial<vscode.TreeViewOptions<any>>) : Promise<vscode.TreeView<Visualizer>> {
  let ts = c.findTreeViewService();
  return ts.createView(viewId, viewTitle, options);
}

/**
 * Registers the treeview service with the language server.
 */
export function createTreeViewService(log : vscode.OutputChannel, c : NbLanguageClient): TreeViewService {
    const d = vscode.commands.registerCommand("foundProjects.deleteEntry", async function (this: any, args: any) {
        let v = args as Visualizer;
        let ok = await c.sendRequest(NodeInfoRequest.destroy, { nodeId : v.data.id });
        if (!ok) {
            vscode.window.showErrorMessage('Cannot delete node ' + v.label);
        }
    });
    const ts : TreeViewService = new TreeViewService(log, c, [ d ]);
    return ts;
}

