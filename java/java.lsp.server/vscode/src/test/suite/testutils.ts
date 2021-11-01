import * as assert from 'assert';
import * as fs from 'fs';
import * as path from 'path';
import { spawn, ChildProcessByStdio } from 'child_process';
import { Readable } from 'stream';

// You can import and use all API from the 'vscode' module
// as well as import your extension to test it
import * as vscode from 'vscode';

export async function prepareProject(folder: string) {
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
