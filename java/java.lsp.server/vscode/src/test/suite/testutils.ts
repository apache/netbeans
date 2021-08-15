import * as assert from 'assert';
import { spawn, ChildProcessByStdio } from 'child_process';
import { Readable } from 'stream';

// You can import and use all API from the 'vscode' module
// as well as import your extension to test it
import * as vscode from 'vscode';

export function assertWorkspace(): string {
    assert.ok(vscode.workspace, "workspace is defined");
    const dirs = vscode.workspace.workspaceFolders;
    assert.ok(dirs?.length, "There are some workspace folders: " + dirs);
    assert.strictEqual(dirs.length, 1, "One folder provided");
    let folder: string = dirs[0].uri.fsPath;
    return folder;
}

export async function dumpJava() {
    const cmd = 'jps';
    const args = [ '-v' ];
    console.log(`Running: ${cmd} ${args.join(' ')}`);
    let p : ChildProcessByStdio<null, Readable, Readable> = spawn(cmd, args, {
        stdio : ["ignore", "pipe", "pipe"],
    });
    let n = await new Promise<number>((r, e) => {
        p.stdout.on('data', function(d: any) {
            console.log(d.toString());
        });
        p.stderr.on('data', function(d: any) {
            console.log(d.toString());
        });
        p.on('close', function(code: number) {
            r(code);
        });
    });
    console.log(`${cmd} ${args.join(' ')} finished with code ${n}`);
}
