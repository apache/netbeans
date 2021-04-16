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
'use strict';

import { WorkspaceFolder, Event, EventEmitter, Uri, commands, debug } from "vscode";
import * as path from 'path';
import { TestAdapter, TestSuiteEvent, TestEvent, TestLoadFinishedEvent, TestLoadStartedEvent, TestRunFinishedEvent, TestRunStartedEvent, TestSuiteInfo, TestInfo, TestDecoration } from "vscode-test-adapter-api";
import { TestSuite } from "./protocol";
import { LanguageClient } from "vscode-languageclient";

export class NbTestAdapter implements TestAdapter {

	private disposables: { dispose(): void }[] = [];
    private children: TestSuiteInfo[] = [];
    private readonly testSuite: TestSuiteInfo;

	private readonly testsEmitter = new EventEmitter<TestLoadStartedEvent | TestLoadFinishedEvent>();
	private readonly statesEmitter = new EventEmitter<TestRunStartedEvent | TestRunFinishedEvent | TestSuiteEvent | TestEvent>();

    constructor(
        public readonly workspaceFolder: WorkspaceFolder,
        private readonly client: Promise<LanguageClient>
    ) {
        this.disposables.push(this.testsEmitter);
        this.disposables.push(this.statesEmitter);
        this.testSuite = { type: 'suite', id: '*', label: 'Tests', children: this.children };
    }

	get tests(): Event<TestLoadStartedEvent | TestLoadFinishedEvent> {
        return this.testsEmitter.event;
    }

    get testStates(): Event<TestRunStartedEvent | TestRunFinishedEvent | TestSuiteEvent | TestEvent> {
        return this.statesEmitter.event;
    }

    async load(): Promise<void> {
        this.testsEmitter.fire(<TestLoadStartedEvent>{ type: 'started' });
        let clnt = await this.client;
        console.log(clnt);
        this.children.length = 0;
        const loadedTests: any = await commands.executeCommand('java.load.workspace.tests', this.workspaceFolder.uri.toString());
        if (loadedTests) {
            loadedTests.forEach((suite: TestSuite) => {
                this.updateTests(suite);
            });
        }
        if (this.children.length > 0) {
            this.testsEmitter.fire(<TestLoadFinishedEvent>{ type: 'finished', suite: this.testSuite });
        } else {
            this.testsEmitter.fire(<TestLoadFinishedEvent>{ type: 'finished' });
        }
    }

    async run(tests: string[]): Promise<void> {
		this.statesEmitter.fire(<TestRunStartedEvent>{ type: 'started', tests });
		if (tests.length === 1) {
            if (tests[0] === '*') {
                await commands.executeCommand('java.run.test', this.workspaceFolder.uri.toString());
                this.statesEmitter.fire(<TestRunFinishedEvent>{ type: 'finished' });
            } else {
                const idx = tests[0].indexOf(':');
                const suiteName = idx < 0 ? tests[0] : tests[0].slice(0, idx);
                const current = this.children.find(s => s.id === suiteName);
                if (current && current.file) {
                    const methodName = idx < 0 ? undefined : tests[0].slice(idx + 1);
                    if (methodName) {
                        await commands.executeCommand('java.run.single', Uri.file(current.file).toString(), methodName);
                    } else {
                        await commands.executeCommand('java.run.single', Uri.file(current.file).toString());
                    }
                    this.statesEmitter.fire(<TestRunFinishedEvent>{ type: 'finished' });
                } else {
                    this.statesEmitter.fire(<TestLoadFinishedEvent>{ type: 'finished', errorMessage: `Cannot find suite to run: ${tests[0]}` });
                }
            }
		} else {
			this.statesEmitter.fire(<TestLoadFinishedEvent>{ type: 'finished', errorMessage: 'Failed to run mutliple tests'});
        }
    }

    async debug(tests: string[]): Promise<void> {
		this.statesEmitter.fire(<TestRunStartedEvent>{ type: 'started', tests });
		if (tests.length === 1) {
            const idx = tests[0].indexOf(':');
            const suiteName = idx < 0 ? tests[0] : tests[0].slice(0, idx);
            const current = this.children.find(s => s.id === suiteName);
            if (current && current.file) {
                const methodName = idx < 0 ? undefined : tests[0].slice(idx + 1);
                if (methodName) {
                    await commands.executeCommand('java.debug.single', Uri.file(current.file).toString(), methodName);
                } else {
                    await commands.executeCommand('java.debug.single', Uri.file(current.file).toString());
                }
                this.statesEmitter.fire(<TestRunFinishedEvent>{ type: 'finished' });
            } else {
                this.statesEmitter.fire(<TestLoadFinishedEvent>{ type: 'finished', errorMessage: `Cannot find suite to debug: ${tests[0]}` });
            }
		} else {
			this.statesEmitter.fire(<TestLoadFinishedEvent>{ type: 'finished', errorMessage: 'Failed to debug mutliple tests'});
        }
    }

    cancel(): void {
        debug.stopDebugging();
    }

    dispose(): void {
		this.cancel();
		for (const disposable of this.disposables) {
			disposable.dispose();
		}
		this.disposables = [];
	}

    testProgress(suite: TestSuite): void {
        switch (suite.state) {
            case 'loaded':
                if (this.updateTests(suite)) {
                    this.testsEmitter.fire(<TestLoadFinishedEvent>{ type: 'finished', suite: this.testSuite });
                }
                break;
            case 'running':
                this.statesEmitter.fire(<TestSuiteEvent>{ type: 'suite', suite: suite.suiteName, state: suite.state });
                break;
            case 'completed':
            case 'errored':
                let errMessage: string | undefined;
                if (suite.tests) {
                    const currentSuite = this.children.find(s => s.id === suite.suiteName);
                    if (currentSuite) {
                        suite.tests.forEach(test => {
                            let message: string | undefined;
                            let decorations: TestDecoration[] | undefined;
                            if (test.stackTrace) {
                                message = test.stackTrace.join('\n');
                                const testFile = test.file ? Uri.parse(test.file)?.path : undefined;
                                if (testFile) {
                                    const fileName = path.basename(testFile);
                                    const line = test.stackTrace.map(frame => {
                                        const info = frame.match(/^\s*at\s*\S*\((\S*):(\d*)\)$/);
                                        if (info && info.length >= 3 && info[1] === fileName) {
                                            return parseInt(info[2]);
                                        }
                                        return null;
                                    }).find(l => l);
                                    if (line) {
                                        decorations = [{ line: line - 1, message: test.stackTrace[0] }];
                                    }
                                }
                            }
                            let currentTest = (currentSuite as TestSuiteInfo).children.find(ti => ti.id === test.id);
                            if (currentTest) {
                                this.statesEmitter.fire(<TestEvent>{ type: 'test', test: test.id, state: test.state, message, decorations });
                            } else if (test.state !== 'passed' && message && !errMessage) {
                                suite.state = 'errored';
                                errMessage = message;
                            }
                        });
                    }
                }
                this.statesEmitter.fire(<TestSuiteEvent>{ type: 'suite', suite: suite.suiteName, state: suite.state, message: errMessage });
                break;
        }
    }

    updateTests(suite: TestSuite): boolean {
        let changed = false;
        const currentSuite = this.children.find(s => s.id === suite.suiteName);
        if (currentSuite) {
            const file = suite.file ? Uri.parse(suite.file)?.path : undefined;
            if (file && currentSuite.file !== file) {
                currentSuite.file = file;
                changed = true;
            }
            if (suite.line && currentSuite.line !== suite.line) {
                currentSuite.line = suite.line;
                changed = true
            }
            if (suite.tests) {
                const ids = new Set();
                suite.tests.forEach(test => {
                    ids.add(test.id);
                    let currentTest = (currentSuite as TestSuiteInfo).children.find(ti => ti.id === test.id);
                    if (currentTest) {
                        const file = test.file ? Uri.parse(test.file)?.path : undefined;
                        if (file && currentTest.file !== file) {
                            currentTest.file = file;
                            changed = true;
                        }
                        if (test.line && currentTest.line !== test.line) {
                            currentTest.line = test.line;
                            changed = true;
                        }
                    } else {
                        (currentSuite as TestSuiteInfo).children.push({ type: 'test', id: test.id, label: test.shortName, tooltip: test.fullName, file: test.file ? Uri.parse(test.file)?.path : undefined, line: test.line });
                        changed = true;
                    }
                });
                if ((currentSuite as TestSuiteInfo).children.length !== ids.size) {
                    (currentSuite as TestSuiteInfo).children = (currentSuite as TestSuiteInfo).children.filter(ti => ids.has(ti.id));
                    changed = true;
                }
            }
        } else {
            const children: TestInfo[] = suite.tests ? suite.tests.map(test => {
                return { type: 'test', id: test.id, label: test.shortName, tooltip: test.fullName, file: test.file ? Uri.parse(test.file)?.path : undefined, line: test.line };
            }) : [];
            this.children.push({ type: 'suite', id: suite.suiteName, label: suite.suiteName, file: suite.file ? Uri.parse(suite.file)?.path : undefined, line: suite.line, children });
            changed = true;
        }
        return changed;
    }
}
