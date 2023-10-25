
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

import * as assert from 'assert';
import * as fs from 'fs';
import * as path from 'path';
import { spawn, ChildProcessByStdio } from 'child_process';
import { Readable } from 'stream';

// You can import and use all API from the 'vscode' module
// as well as import your extension to test it
import * as vscode from 'vscode';

import * as myExtension from '../../extension';

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
    <exec.mainClass>pkg.Main</exec.mainClass>
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
    vscode.workspace.saveAll();
    await waitProjectRecognized(mainJava);
}

export function waitCommandsReady() : Promise<void> {
    return new Promise((resolve, reject) => {
        function checkCommands(attempts : number, cb : () => void) {
            try {
                // this command is parameterless
                vscode.commands.executeCommand(myExtension.COMMAND_PREFIX + ".java.attachDebugger.configurations")
                console.log("NBLS commands ready.");
                resolve();
            } catch (e) {
                if (attempts > 0) {
                    console.log("Waiting for NBLS commands to be registered, " + attempts + " attempts to go...");
                    setTimeout(() => checkCommands(attempts - 1, cb), 100);
                } else {
                    reject(new Error("Timeout waiting for NBLS commands registration: " + e));
                }
            }
        }
        myExtension.awaitClient().then(() => checkCommands(5, () => {}));
    });
}

export function assertWorkspace(): string {
    assert.ok(vscode.workspace, "workspace is defined");
    const dirs = vscode.workspace.workspaceFolders;
    assert.ok(dirs?.length, "There are some workspace folders: " + dirs);
    assert.strictEqual(dirs.length, 1, "One folder provided");
    let folder: string = dirs[0].uri.fsPath;
    return folder;
}

/**
 * Ensures that the project that holds the parameter file was opened in NBJLS.
 * @param folder 
 * @returns promise that will be fullfilled after the project opens in NBJLS.
 */
async function waitProjectRecognized(someJavaFile : string) {
    return waitCommandsReady().then(() => {
        const u : vscode.Uri = vscode.Uri.file(someJavaFile);
        // clear out possible bad or negative caches.
        return vscode.commands.executeCommand(myExtension.COMMAND_PREFIX + ".clear.project.caches").then(
            // this should assure opening the root with the created project.
            () => vscode.commands.executeCommand(myExtension.COMMAND_PREFIX + ".java.get.project.packages", u.toString())
        );
    });
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
