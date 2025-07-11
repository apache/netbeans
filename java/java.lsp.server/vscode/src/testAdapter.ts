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

import { commands, debug, tests, workspace, CancellationToken, TestController, TestItem, TestRunProfileKind, TestRunRequest, Uri, TestRun, TestMessage, Location, Position, MarkdownString, TestRunProfile, CancellationTokenSource } from "vscode";
import * as path from 'path';
import { asRange, TestCase, TestSuite } from "./protocol";
import { COMMAND_PREFIX, listeners, TEST_PROGRESS_EVENT } from "./extension";

type SuiteState = 'enqueued' | 'started' | 'passed' | 'failed' | 'skipped' | 'errored';

export class NbTestAdapter {

    private readonly testController: TestController;
	private disposables: { dispose(): void }[] = [];
    private currentRun: TestRun | undefined;
    private itemsToRun: Set<TestItem> | undefined;
    private started: boolean = false;
    private suiteStates: Map<TestItem, SuiteState>;
    private parallelRunProfile: TestRunProfile | undefined;

    constructor() {
        this.testController = tests.createTestController('apacheNetBeansController', 'Apache NetBeans');
        const runHandler = (request: TestRunRequest, cancellation: CancellationToken) => this.run(request, cancellation);
        this.testController.createRunProfile('Run Tests', TestRunProfileKind.Run, runHandler);
        this.testController.createRunProfile('Debug Tests', TestRunProfileKind.Debug, runHandler);
        this.disposables.push(this.testController);
        this.load();
        this.suiteStates = new Map();
    }

    public registerRunInParallelProfile(projects: string[]) {
        if (!this.parallelRunProfile) {
            const runHandler = (request: TestRunRequest, cancellation: CancellationToken) => this.run(request, cancellation, true, projects);
            this.parallelRunProfile = this.testController.createRunProfile("Run Tests In Parallel", TestRunProfileKind.Run, runHandler, true);
        } 
        this.testController.items.replace([]);
        this.load();
    }

    public testInParallelProfileExist(): boolean {
        return this.parallelRunProfile ? true : false;
    }

    public runTestsWithParallelProfile(projects?: string[]) {
        if (this.parallelRunProfile) {
            this.run(new TestRunRequest(undefined, undefined, this.parallelRunProfile), new CancellationTokenSource().token, true, projects);
        }
    }

    async load(): Promise<void> {
        for (let workspaceFolder of workspace.workspaceFolders || []) {
            const loadedTests: any = await commands.executeCommand(COMMAND_PREFIX + '.load.workspace.tests', workspaceFolder.uri.toString());
            if (loadedTests) {
                loadedTests.forEach((suite: TestSuite) => {
                    this.updateTests(suite);
                });
            }
        }
    }

    async run(request: TestRunRequest, cancellation: CancellationToken, testInParallel: boolean = false, projects?: string[]): Promise<void> {
        if (!this.currentRun) {
            if (!testInParallel) {
                commands.executeCommand('workbench.debug.action.focusRepl');
            }
            cancellation.onCancellationRequested(() => this.cancel());
            this.currentRun = this.testController.createTestRun(request);
            this.currentRun.token.onCancellationRequested(() => this.cancel());
            this.itemsToRun = new Set();
            this.started = false;
            if (request.include) {
                const include = [...new Map(request.include.map(item => !item.uri && item.parent?.uri ? [item.parent.id, item.parent] : [item.id, item])).values()];
                for (let item of include) {
                    if (item.uri) {
                        this.set(item, 'enqueued');
                        item.parent?.children.forEach(child => {
                            if (child.id?.includes(item.id)) {
                                this.set(child, 'enqueued');
                            }
                        })
                        const idx = item.id.indexOf(':');
                        const isNestedClass = item.id.includes('$');
                        const topLevelClassName = item.id.lastIndexOf('.');
                        let nestedClass: string | undefined;
                        if (isNestedClass && topLevelClassName > 0) {
                            nestedClass = idx < 0 
                                ? item.id.slice(topLevelClassName + 1)
                                : item.id.substring(topLevelClassName + 1, idx);
                            nestedClass = nestedClass.replace('$', '.');
                        }
                        if (!cancellation.isCancellationRequested) {
                            try {
                                //TODO: testRun == true, file(!)
                                await commands.executeCommand(request.profile?.kind === TestRunProfileKind.Debug ? COMMAND_PREFIX + '.debug.test' : COMMAND_PREFIX + '.run.test', item.uri.toString(), idx < 0 ? undefined : item.id.slice(idx + 1), 
                                    undefined /* configuration */, nestedClass);
                            } catch(err) {
                                // test state will be handled in the code below
                                console.log(err);
                            }
                        }
                    }
                }
            } else {
                this.testController.items.forEach(item => this.set(item, 'enqueued'));
                for (let workspaceFolder of workspace.workspaceFolders || []) {
                    if (!cancellation.isCancellationRequested) {
                        try {
                            if (testInParallel) {
                                await commands.executeCommand(COMMAND_PREFIX + '.run.test', workspaceFolder.uri.toString(), undefined, undefined, undefined, true, projects);
                            } else {
                                await commands.executeCommand(request.profile?.kind === TestRunProfileKind.Debug ? COMMAND_PREFIX + '.debug.test': COMMAND_PREFIX + '.run.test', workspaceFolder.uri.toString());
                            }
                        } catch(err) {
                            // test state will be handled in the code below
                            console.log(err);
                        }
                    }
                }
            }
            if (this.started) {
                this.itemsToRun.forEach(item => {
                    var isContainer = true;
                    if (item.children.size == 0) {
                        isContainer = false;
                    } else {
                        item.children.forEach(c => {
                            isContainer &&= c.children.size != 0;
                        });
                    }
                    if (!isContainer) {
                        this.set(item, 'skipped');
                    }
                });
            } 
            // TBD - message
            else {
                this.itemsToRun.forEach(item => this.set(item, 'failed', new TestMessage('Build failure'), false, true));    
            }
            this.itemsToRun = undefined;
            this.currentRun.end();
            this.currentRun = undefined;
        }
    }

    set(item: TestItem, state: SuiteState, message?: TestMessage | readonly TestMessage[], noPassDown? : boolean, dispatchBuildFailEvent?: boolean): void {
        if (this.currentRun) {
            switch (state) {
                case 'enqueued':
                    this.dispatchTestEvent(state, item);
                    this.itemsToRun?.add(item);
                    this.currentRun.enqueued(item);
                    break;
                case 'skipped':
                    this.dispatchTestEvent(state, item);
                case 'started':
                case 'passed':
                    this.itemsToRun?.delete(item);
                    this.currentRun[state](item);
                    break;
                case 'failed':
                case 'errored':
                    if (dispatchBuildFailEvent) {
                        this.dispatchTestEvent(state, item);
                    }
                    this.itemsToRun?.delete(item);
                    this.currentRun[state](item, message || new TestMessage(""));
                    break;
            }
            this.suiteStates.set(item, state);
            if (!noPassDown) {
                item.children.forEach(child => this.set(child, state, message, noPassDown, dispatchBuildFailEvent));
            }
        }
    }
    
    dispatchTestEvent(state: SuiteState, testItem: TestItem): void {
        if (testItem.parent && testItem.children.size > 0) {
            if (testItem.id.includes(":") && testItem.parent.parent) {
                // special case when parameterized test
                const testEvent = this.getParametrizedTestEvent(state, testItem);
                if (!testEvent) return;

                this.dispatchEvent(testEvent);
            } else {
                this.dispatchEvent({
                    name: testItem.id,
                    moduleName: testItem.parent.id,
                    modulePath: testItem.parent.uri?.path,
                    state,
                });
            }
        } else if (testItem.children.size === 0) {
            const testSuite = testItem.parent;
            const parentState = testSuite && this.suiteStates.get(testSuite) ? this.suiteStates.get(testSuite) : state;
            if (testSuite) {
                let moduleName = testSuite.parent?.id;
                let modulePath = testSuite.parent?.uri?.path;
                if (testSuite.id.includes(":") && testSuite.parent?.parent) {
                    // special case when parameterized test
                    moduleName = testSuite.parent.parent.id;
                    modulePath = testSuite.parent.parent.uri?.path;
                }
                const testSuiteEvent: any = {
                    name: testSuite.id,
                    moduleName,
                    modulePath,
                    state: parentState,
                    tests: []
                }
                testSuite?.children.forEach(suite => {
                    if (suite.id === testItem.id) {
                        const idx = suite.id.indexOf(':');
                        if (idx >= 0) {
                            const name = suite.id.slice(idx + 1);
                            testSuiteEvent.tests?.push({
                                id: suite.id,
                                name,
                                state
                            })
                        }
                    }
                })
                this.dispatchEvent(testSuiteEvent);
            }
        }
    }

    getParametrizedTestEvent(state: SuiteState, testItem: TestItem): any {
        if (!testItem.parent || !testItem.parent.parent) {
            return undefined;
        }
        let name = testItem.parent.id;
        const idx = name.indexOf(':');
        return {
            name,
            moduleName: testItem.parent.parent.id,
            modulePath: testItem.parent.parent.uri?.path,
            state,
            tests: [
                {
                    id: name,
                    name: name.slice(idx + 1),
                    state
                }
            ]
        }
    }

    dispatchEvent(event: any): void {
        const testProgressListeners = listeners.get(TEST_PROGRESS_EVENT);
        testProgressListeners?.forEach(listener => {
            commands.executeCommand(listener, event);
        })
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

    testOutput(output: string): void {
        if (this.currentRun && output) {
            this.currentRun.appendOutput(output.replace(/\n/g, '\r\n'));
        }
    }

    testProgress(suite: TestSuite): void {
        const currentModule = this.testController.items.get(this.getModuleItemId(suite.moduleName));

        let currentTarget = currentModule;
        let suiteName = suite.name;
        if (suite.relativePath) {
            const relativePathComponents = suite.relativePath.split('/');
            for (let i = 0; i < relativePathComponents.length - 1; i++) {
                currentTarget = currentTarget?.children.get(relativePathComponents[i]);
            }
            suiteName = relativePathComponents[relativePathComponents.length - 1];
        }

        const currentSuite = currentTarget?.children.get(suiteName);

        switch (suite.state) {
            case 'loaded':
                this.updateTests(suite);
                break;
            case 'started':
                this.started = true;
                if (currentSuite) {
                    this.set(currentSuite, 'started');
                }
                break;
            case 'passed':
            case "failed":
            case 'errored':
            case 'skipped':
                if (suite.tests) {
                    this.updateTests(suite, true);
                    if (currentSuite && currentModule) {
                        const suiteMessages: TestMessage[] = [];
                        suite.tests?.forEach(test => {
                            if (this.currentRun) {
                                let currentTest = currentSuite.children.get(test.id);
                                if (!currentTest) {
                                    currentSuite.children.forEach(item => {
                                        if (!currentTest) {
                                            const subName = this.subTestName(item, test);
                                            if (subName) {
                                                currentTest = subName === '()' ? item : item.children.get(test.id);
                                            }
                                        }
                                    });
                                }
                                let message: TestMessage | undefined;
                                if (test.stackTrace) {
                                    message = new TestMessage(this.stacktrace2Message(currentTest?.uri?.toString(), test.stackTrace));
                                    if (currentTest) {
                                        const testUri = currentTest.uri || currentTest.parent?.uri;
                                        if (testUri) {
                                            const fileName = path.basename(testUri.path);
                                            const line = test.stackTrace.map(frame => {
                                                const info = frame.match(/^\s*at[^\(]*\((\S*):(\d*)\)$/);
                                                if (info && info.length >= 3 && info[1] === fileName) {
                                                    return parseInt(info[2]);
                                                }
                                                return null;
                                            }).find(l => l);
                                            const pos = line ? new Position(line - 1, 0) : currentTest.range?.start;
                                            if (pos) {
                                                message.location = new Location(testUri, pos);
                                            }
                                        }
                                    } else {
                                        message.location = new Location(currentSuite.uri!, currentSuite.range!.start);
                                    }
                                }
                                if (currentTest && test.state !== 'loaded') {
                                    this.set(currentTest, test.state, message, true);
                                } else if (test.state !== 'passed' && message) {
                                    suiteMessages.push(message);
                                }
                            }
                        });
                        if (suiteMessages.length > 0) {
                            this.set(currentSuite, 'errored', suiteMessages, true);
                            currentSuite.children.forEach(item => this.set(item, 'skipped'));
                        } else {
                            this.set(currentSuite, suite.state, undefined, true);
                        }
                        this.set(currentModule, this.calculateStateFor(currentModule), undefined, true);
                    }
                }
                break;
        }
    }

    calculateStateFor(testItem: TestItem): 'passed' | 'failed' | 'skipped' | 'errored' {
        let passed: number = 0;
        testItem.children.forEach(item => {
            const state = this.suiteStates.get(item);
            if (state === 'enqueued' || state === 'failed') return state;
            if (state === 'passed') passed++;
        })
        if (passed > 0) return 'passed';
        return 'skipped';
    }

    updateTests(suite: TestSuite, testExecution?: boolean): void {
        const moduleName = this.getModuleItemId(suite.moduleName);
        let currentModule = this.testController.items.get(moduleName);
        if (!currentModule) {
            const parsedName = this.parseModuleName(moduleName);
            currentModule = this.testController.createTestItem(moduleName, this.getNameWithIcon(parsedName, 'module'), this.getModulePath(suite));
            this.testController.items.add(currentModule);
        }

        let currentTarget = currentModule;
        let suiteName = suite.name;
        if (suite.relativePath) {
            let currentUri = suite.modulePath ? Uri.parse(suite.modulePath) : null;
            const relativePathComponents = suite.relativePath.split('/');
            for (let i = 0; i < relativePathComponents.length - 1; i++) {
                const currentTargetChildren: TestItem[] = []
                let newTarget = currentTarget.children.get(relativePathComponents[i]);
                currentUri = currentUri ? Uri.joinPath(currentUri, relativePathComponents[i]) : null;
                if (!newTarget) {
                    newTarget = this.testController.createTestItem(relativePathComponents[i], this.getNameWithIcon(relativePathComponents[i], 'package'), currentUri ? currentUri : undefined);
                    currentTargetChildren.push(newTarget);
                }
                currentTarget.children.forEach(suite => currentTargetChildren.push(suite));
                currentTarget.children.replace(currentTargetChildren);
                currentTarget = newTarget;
            }
            suiteName = relativePathComponents[relativePathComponents.length - 1];
        }

        const suiteChildren: TestItem[] = []
        let currentSuite = currentTarget.children.get(suiteName);
        const suiteUri = suite.file ? Uri.parse(suite.file) : undefined;
        if (!currentSuite || suiteUri && currentSuite.uri?.toString() !== suiteUri.toString()) {
            currentSuite = this.testController.createTestItem(suiteName, this.getNameWithIcon(suiteName, 'class'), suiteUri);
            suiteChildren.push(currentSuite);
        }
        currentTarget.children.forEach(suite => suiteChildren.push(suite));

        const suiteRange = asRange(suite.range);
        if (!testExecution && suiteRange && suiteRange !== currentSuite.range) {
            currentSuite.range = suiteRange;
        }
        const children: TestItem[] = []
        const parentTests: Map<TestItem, TestItem[]> = new Map();
        suite.tests?.forEach(test => {
            let currentTest = currentSuite?.children.get(test.id);
            const testUri = test.file ? Uri.parse(test.file) : undefined;
            if (currentTest) {
                if (testUri && currentTest.uri?.toString() !== testUri?.toString()) {
                    currentTest = this.testController.createTestItem(test.id, this.getNameWithIcon(test.name, 'method'), testUri);
                    currentSuite?.children.add(currentTest);
                }
                const testRange = asRange(test.range);
                if (!testExecution && testRange && testRange !== currentTest.range) {
                    currentTest.range = testRange;
                }
                children.push(currentTest);
            } else {
                if (testExecution) {
                    const parents: Map<TestItem, string> = new Map();
                    currentSuite?.children.forEach(item => {
                        const subName = this.subTestName(item, test);
                        if (subName && '()' !== subName) {
                            parents.set(item, subName);
                        }
                    });
                    const parent = this.selectParent(parents);
                    if (parent) {
                        let arr = parentTests.get(parent.test);
                        if (!arr) {
                            parentTests.set(parent.test, arr = []);
                            children.push(parent.test);
                        }
                        arr.push(this.testController.createTestItem(test.id, this.getNameWithIcon(parent.label, 'method')));
                    }
                } else {
                    currentTest = this.testController.createTestItem(test.id, this.getNameWithIcon(test.name, 'method'), testUri);
                    currentTest.range = asRange(test.range);
                    children.push(currentTest);
                    currentSuite?.children.add(currentTest);
                }
            }
        });
        if (testExecution) {
            parentTests.forEach((val, key) => {
                const item = this.testController.createTestItem(key.id, key.label, key.uri);
                item.range = key.range;
                item.children.replace(val);
                currentSuite?.children.add(item);
            });
        } else {
            currentSuite.children.replace(children);
            currentTarget.children.replace(suiteChildren);
        }
    }

    getModuleItemId(moduleName?: string): string {
        return moduleName?.replace(":", "-") || "";
    }
    
    parseModuleName(moduleName: string): string {
        if (!this.parallelRunProfile) {
            return moduleName.replace(":", "-");
        }
        const index = moduleName.indexOf(":");
        if (index !== -1) {
            return moduleName.slice(index + 1);
        }
        const parts = moduleName.split("-");
        return parts[parts.length - 1];
    }
    
    getModulePath(suite: TestSuite): Uri {
        return Uri.parse(suite.modulePath || ""); //XXX
    }

    getNameWithIcon(itemName: string, itemType: 'module' | 'class' | 'method' | 'package'): string  {
        switch (itemType) {
            case 'module':
                return `$(project) ${itemName}`;
            case 'class':
                return `$(symbol-class) ${itemName}`;
            case 'method':
                return `$(symbol-method) ${itemName}`;
            default:
                return itemName;
        }
    }

    getNameWithoutIcon(itemName: string): string {
        return itemName.replace(/^\$\([^)]+\)\s*/, "");
    }

    subTestName(item: TestItem, test: TestCase): string | undefined {
        if (test.id.startsWith(item.id)) {
            let label = test.name;
            const nameWithoutIcon = this.getNameWithoutIcon(item.label);
            if (label.startsWith(nameWithoutIcon)) {
                label = label.slice(nameWithoutIcon.length).trim();
            }
            return label;
        } else {
            const regexp = new RegExp(item.id.replace(/[-[\]{}()*+?.,\\^$|\s]/g, '\\$&').replace(/#\w*/g, '\\S*'));
            if (regexp.test(test.id)) {
                return test.name;
            }
        }
        return undefined;
    }

    selectParent(parents: Map<TestItem, string>): {test: TestItem, label: string} | undefined {
        let ret: {test: TestItem, label: string} | undefined = undefined;
        parents.forEach((label, parentTest) => {
            if (ret) {
                if (parentTest.id.replace(/#\w*/g, '').length > ret.test.id.replace(/#\w*/g, '').length) {
                    ret = {test: parentTest, label};
                }
            } else {
                ret = {test: parentTest, label};
            }
        });
        return ret;
    }

    stacktrace2Message(currentTestUri: string | undefined, stacktrace: string[]): MarkdownString {
        const regExp: RegExp = /(\s*at\s+(?:[\w$\\.]+\/)?((?:[\w$]+\.)+[\w\s$<>]+))\(((.*):(\d+))\)/;
        const message = new MarkdownString();
        message.isTrusted = true;
        message.supportHtml = true;
        for (const line of stacktrace) {
            if (message.value.length) {
                message.appendMarkdown('<br/>');
            }
            const result = regExp.exec(line);
            if (result) {
                message.appendText(result[1]).appendText('(').appendMarkdown(`[${result[3]}](command:${COMMAND_PREFIX}.open.stacktrace?${encodeURIComponent(JSON.stringify([currentTestUri, result[2], result[4], +result[5]]))})`).appendText(')');
            } else {
                message.appendText(line);
            }
        }
        return message;
    }
}
