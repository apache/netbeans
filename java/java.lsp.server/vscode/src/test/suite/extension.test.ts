import * as assert from 'assert';
import * as fs from 'fs';
import * as path from 'path';
import * as ps from 'ps-node';
import { spawn, ChildProcessByStdio, spawnSync, SpawnSyncReturns } from 'child_process';
import { Readable } from 'stream';

// You can import and use all API from the 'vscode' module
// as well as import your extension to test it
import * as vscode from 'vscode';
import * as myExtension from '../../extension';
import { TextDocument, TextEditor, Uri } from 'vscode';

suite('Extension Test Suite', () => {
    vscode.window.showInformationMessage('Cleaning up workspace.');
    let folder: string = assertWorkspace();
    fs.rmdirSync(folder, { recursive: true });
    fs.mkdirSync(folder, { recursive: true });
    vscode.window.showInformationMessage('Start all tests.');
    myExtension.enableConsoleLog();

    test('VSNetBeans is present', async () => {
        let nbcode = vscode.extensions.getExtension('asf.apache-netbeans-java');
        assert.ok(nbcode, "Apache NetBeans Extension is present");
        let api = await nbcode.activate();
        assert.ok(api.version, "Some version is specified");

        let cannotReassignVersion = false;
        try {
            api.version = "different";
        } catch (e) {
            cannotReassignVersion = true;
        }
        assert.ok(cannotReassignVersion, "Cannot reassign value of version");
    });

    test('Find clusters', async () => {
        const nbcode = vscode.extensions.getExtension('asf.apache-netbeans-java');
        assert.ok(nbcode);

        const extraCluster = path.join(nbcode.extensionPath, "nbcode", "extra");
        let clusters = myExtension.findClusters('non-existent').
            // ignore 'extra' cluster in the extension path, since nbjavac is there during development:
            filter(s => !s.startsWith(extraCluster));


        let found : string[] = [];
        function assertCluster(name : string) {
            for (let c of clusters) {
                if (c.endsWith('/' + name)) {
                    found.push(c);
                    return;
                }
            }
            assert.fail(`Cannot find ${name} among ${clusters}`);
        }

        assertCluster('extide');
        assertCluster('ide');
        assertCluster('java');
        assertCluster('nbcode');
        assertCluster('platform');
        assertCluster('webcommon');
        assertCluster('harness');

        for (let c of found) {
            assert.ok(c.startsWith(nbcode.extensionPath), `All extensions are below ${nbcode.extensionPath}, but: ${c}`);
        }
    });

    async function demo(where: number) {
        let folder: string = assertWorkspace();

        await prepareProject(folder);

        vscode.workspace.saveAll();

        if (where === 6) return;

        try {
            console.log("Test: invoking compile");
            let res = await vscode.commands.executeCommand("java.workspace.compile");
            console.log(`Test: compile finished with ${res}`);
        } catch (error) {
            dumpJava();
            throw error;
        }

        if (where === 7) return;

        let mainClass = path.join(folder, 'target', 'classes', 'pkg', 'Main.class');

        if (where === 8) return;

        assert.ok(fs.statSync(mainClass).isFile(), "Class created by compilation: " + mainClass);
    }

    test("Compile workspace6", async() => demo(6));
    test("Compile workspace7", async() => demo(7));
    test("Compile workspace8", async() => demo(8));

    /**
     * Checks that maven-managed process can be started, and forcefully terminated by vscode
     * although it does not run in debugging mode.
     */
    async function mavenTerminateWithoutDebugger() {
        let folder: string = assertWorkspace();

        await prepareProject(folder);

        vscode.workspace.saveAll();
        let u : Uri = vscode.Uri.file(path.join(folder, 'src', 'main', 'java', 'pkg', 'Main.java'));
        let doc : TextDocument = await vscode.workspace.openTextDocument(u);
        let e : TextEditor = await vscode.window.showTextDocument(doc);

        try {
            let r = new Promise((resolve, reject) => {
                function waitUserApplication(cnt : number, running: boolean, cb : () => void) {
                    ps.lookup({
                        command: "^.*[/\\\\]java",
                        arguments: "pkg.Main"
                    }, (err, list ) => {
                        let success : boolean = (list && list.length > 0) == running;
                        if (success) {
                            cb();
                        } else {
                            if (cnt == 0) {
                                reject(new Error("Timeout waiting for user application"));
                                return;
                            }
                            setTimeout(() => waitUserApplication(cnt - 1, running, cb), 1000);
                            return;
                        }
                    });
                }
                
                function onProcessStarted() {
                    console.log("Test: invoking debug.stop");
                    // attempt to terminate:
                    vscode.commands.executeCommand("workbench.action.debug.stop").
                        then(() => waitUserApplication(5, false, () => resolve(true)));
                }
                console.log("Test: invoking debug debug.run");
                const workspaceFolder = (vscode.workspace.workspaceFolders!)[0];
                vscode.debug.startDebugging(workspaceFolder, {type: "java8+", name: "Launch Java 8+ App", request: "launch"}, {}).
                    then(() => waitUserApplication(5, true, onProcessStarted));
            });
            return r;
        } catch (error) {
            dumpJava();
            throw error;
        }
    }

    test("Maven run termination", async() => mavenTerminateWithoutDebugger());

    async function getProjectInfo() {
        let folder: string = assertWorkspace();

        await prepareProject(folder);

        vscode.workspace.saveAll();

        try {
            console.log("Test: get project java source roots");
            let res: any = await vscode.commands.executeCommand("java.get.project.source.roots", Uri.file(folder).toString());
            console.log(`Test: get project java source roots finished with ${res}`);
            assert.ok(res, "No java source root returned");
            assert.strictEqual(res.length, 2, `Invalid number of java roots returned`);
            assert.strictEqual(res[0], path.join('file:', folder, 'src', 'main', 'java') + path.sep, `Invalid java main source root returned`);
            assert.strictEqual(res[1], path.join('file:', folder, 'src', 'test', 'java') + path.sep, `Invalid java test source root returned`);

            console.log("Test: get project resource roots");
            res = await vscode.commands.executeCommand("java.get.project.source.roots", Uri.file(folder).toString(), 'resources');
            console.log(`Test: get project resource roots finished with ${res}`);
            assert.ok(res, "No resource root returned");
            assert.strictEqual(res.length, 1, `Invalid number of resource roots returned`);
            assert.strictEqual(res[0], path.join('file:', folder, 'src', 'main', 'resources') + path.sep, `Invalid resource root returned`);

            console.log("Test: get project compile classpath");
            res = await vscode.commands.executeCommand("java.get.project.classpath", Uri.file(folder).toString());
            console.log(`Test: get project compile classpath finished with ${res}`);
            assert.ok(res, "No compile classpath returned");
            assert.strictEqual(res.length, 9, `Invalid number of compile classpath roots returned`);
            assert.ok(res.find((item: any) => item === path.join('file:', folder, 'target', 'classes') + path.sep, `Invalid compile classpath root returned`));

            console.log("Test: get project source classpath");
            res = await vscode.commands.executeCommand("java.get.project.classpath", Uri.file(folder).toString(), 'SOURCE');
            console.log(`Test: get project source classpath finished with ${res}`);
            assert.ok(res, "No source classpath returned");
            assert.strictEqual(res.length, 3, `Invalid number of source classpath roots returned`);
            assert.ok(res.find((item: any) => item === path.join('file:', folder, 'src', 'main', 'java') + path.sep, `Invalid source classpath root returned`));
            assert.ok(res.find((item: any) => item === path.join('file:', folder, 'src', 'main', 'resources') + path.sep, `Invalid source classpath root returned`));
            assert.ok(res.find((item: any) => item === path.join('file:', folder, 'src', 'test', 'java') + path.sep, `Invalid source classpath root returned`));

            console.log("Test: get project boot classpath");
            res = await vscode.commands.executeCommand("java.get.project.classpath", Uri.file(folder).toString(), 'BOOT');
            console.log(`Test: get project boot classpath finished with ${res}`);
            assert.ok(res, "No boot classpath returned");
            assert.ok(res.length > 0, `Invalid number of boot classpath roots returned`);

            console.log("Test: get project boot source classpath");
            res = await vscode.commands.executeCommand("java.get.project.classpath", Uri.file(folder).toString(), 'BOOT', true);
            console.log(`Test: get project boot source classpath finished with ${res}`);
            assert.ok(res, "No boot source classpath returned");
            assert.ok(res.length > 0, `Invalid number of boot source classpath roots returned`);

            console.log("Test: get all project packages");
            res = await vscode.commands.executeCommand("java.get.project.packages", Uri.file(folder).toString());
            console.log(`Test: get all project packages finished with ${res}`);
            assert.ok(res, "No packages returned");
            assert.ok(res.length > 0, `Invalid number of packages returned`);

            console.log("Test: get project source packages");
            res = await vscode.commands.executeCommand("java.get.project.packages", Uri.file(folder).toString(), true);
            console.log(`Test: get project source packages finished with ${res}`);
            assert.ok(res, "No packages returned");
            assert.strictEqual(res.length, 1, `Invalid number of packages returned`);
            assert.strictEqual(res[0], 'pkg', `Invalid package returned`);
        } catch (error) {
            dumpJava();
            throw error;
        }
    }

    test("Get project sources, classpath, and packages", async() => getProjectInfo());

    async function testExplorerTests() {
        let folder: string = assertWorkspace();

        await prepareProject(folder);

        vscode.workspace.saveAll();

        try {
            console.log("Test: load workspace tests");
            let tests: any = await vscode.commands.executeCommand("java.load.workspace.tests", Uri.file(folder).toString());
            console.log(`Test: load workspace tests finished with ${tests}`);
            assert.ok(tests, "No tests returned for workspace");
            assert.strictEqual(tests.length, 2, `Invalid number of test suites returned`);
            assert.strictEqual(tests[0].name, 'pkg.MainTest', `Invalid test suite name returned`);
            assert.strictEqual(tests[0].tests.length, 1, `Invalid number of tests in suite returned`);
            assert.strictEqual(tests[0].tests[0].name, 'testGetName', `Invalid test name returned`);
            assert.strictEqual(tests[1].name, 'pkg.MainTest$NestedTest', `Invalid test suite name returned`);
            assert.strictEqual(tests[1].tests.length, 1, `Invalid number of tests in suite returned`);
            assert.strictEqual(tests[1].tests[0].name, 'testTrue', `Invalid test name returned`);

            console.log("Test: run all workspace tests");
            const workspaceFolder = (vscode.workspace.workspaceFolders!)[0];
            await vscode.commands.executeCommand('java.run.test', workspaceFolder.uri.toString());
            console.log(`Test: run all workspace tests finished`);
        } catch (error) {
            dumpJava();
            throw error;
        }
    }

    test("Test Explorer tests", async() => testExplorerTests());
});

function assertWorkspace(): string {
    assert.ok(vscode.workspace, "workspace is defined");
    const dirs = vscode.workspace.workspaceFolders;
    assert.ok(dirs?.length, "There are some workspace folders: " + dirs);
    assert.strictEqual(dirs.length, 1, "One folder provided");
    let folder: string = dirs[0].uri.fsPath;
    return folder;
}

async function prepareProject(folder: string) {
    await fs.promises.writeFile(path.join(folder, 'pom.xml'), `
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
<modelVersion>4.0.0</modelVersion>
<groupId>org.netbeans.demo.vscode.t1</groupId>
<artifactId>basicapp</artifactId>
<version>1.0</version>
<properties>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
</properties>
<build>
<plugins>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>2.22.0</version>
    </plugin>
</plugins>
</build>
<dependencies>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>5.3.1</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-params</artifactId>
    <version>5.3.1</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-engine</artifactId>
    <version>5.3.1</version>
    <scope>test</scope>
</dependency>
</dependencies>
</project>
            `);

            let pkg = path.join(folder, 'src', 'main', 'java', 'pkg');
            let testPkg = path.join(folder, 'src', 'test', 'java', 'pkg');
            let resources = path.join(folder, 'src', 'main', 'resources');
            let mainJava = path.join(pkg, 'Main.java');
            let mainTestJava = path.join(testPkg, 'MainTest.java');

            await fs.promises.mkdir(pkg, { recursive: true });
            await fs.promises.mkdir(resources, { recursive: true });
            await fs.promises.mkdir(testPkg, { recursive: true });

            await fs.promises.writeFile(mainJava, `
package pkg;
public class Main {
    public static void main(String... args) throws Exception {
        System.out.println("Endless wait...");
        while (true) {
            Thread.sleep(1000);
        }
    }
    public String getName() {
        return "John";
    }
}
            `);

            await fs.promises.writeFile(mainTestJava, `
package pkg;
import static org.junit.jupiter.api.Assertions.*;
class MainTest {
    @org.junit.jupiter.api.Test
    public void testGetName() {
        assertEquals("John", new Main().getName());
    }
    @org.junit.jupiter.api.Nested
    class NestedTest {
        @org.junit.jupiter.api.Test
        public void testTrue() {
            assertTrue(true);
        }
    }
}
            `);
}

async function dumpJava() {
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
