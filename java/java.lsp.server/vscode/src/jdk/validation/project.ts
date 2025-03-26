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

import * as xml2js from 'xml2js';
import * as fs from 'fs';
import * as path from 'path';
import * as vscode from 'vscode';
import * as os from 'os';
import { normalizeJavaVersion } from './javaUtil';

const GET_PROJECT_INFO = 'nbls.project.info';
const GRADLE_TARGET_COMPATIBILITY_REGEX = /targetCompatibility\s*=\s*(?:JavaVersion\s*\.\s*toVersion\s*\(\s*['"](\d+(\.\d+)?)['"]\s*\)|['"](\d+(\.\d+)?)['"])/
const GRADLE_SOURCE_COMPATIBILITY_REGEX = /sourceCompatibility\s*=\s*(?:JavaVersion\s*\.\s*toVersion\s*\(\s*['"](\d+(\.\d+)?)['"]\s*\)|['"](\d+(\.\d+)?)['"])/

export enum BuildSystemType {
    MAVEN = 'Maven',
    GRADLE = 'Gradle',
    UNKNOWN = 'Unknown'
}

export async function getProjectFrom(projectUri: vscode.Uri): Promise<Project | undefined> {
    const projectInfos: any[] = await vscode.commands.executeCommand(GET_PROJECT_INFO, projectUri.toString(), { recursive: true, projectStructure: true });
    if (projectInfos?.length && projectInfos[0]) {
        const projectDirectory = projectInfos[0].projectDirectory.toString();
        const buildSystem: BuildSystemType = resolveBuildSystemType(projectUri, projectInfos[0].projectType);

        switch (buildSystem) {
            case BuildSystemType.MAVEN:
                const mavenSubprojects: Project[] = projectInfos[0].subprojects
                    .map((subproject: string) => new MavenProject(subproject, []))
                return new MavenProject(projectDirectory, mavenSubprojects);
            case BuildSystemType.GRADLE:
                const gradleSubprojects: Project[] = projectInfos[0].subprojects
                    .map((subproject: string) => new GradleProject(subproject, []))
                return new GradleProject(projectDirectory, gradleSubprojects);
            default:
                break;
        }
    }
    return Promise.resolve(undefined);
}

function resolveBuildSystemType(uri: vscode.Uri, projectType?: string): BuildSystemType {
    if (projectType?.includes('gradle')) {
        return BuildSystemType.GRADLE;
    }
    if (projectType?.includes('maven')) {
        return BuildSystemType.MAVEN;
    }
    if (fs.existsSync(path.join(uri.fsPath, 'build.gradle'))) {
        return BuildSystemType.GRADLE;
    }
    if (fs.existsSync(path.join(uri.fsPath, 'pom.xml'))) {
        return BuildSystemType.MAVEN;
    }
    return BuildSystemType.UNKNOWN;
}

export abstract class Project {

    readonly directory: string;
    readonly subprojects: Project[];

    constructor(directory: string, subprojects: any[]) {
        this.directory = vscode.Uri.parse(directory).fsPath;
        this.subprojects = subprojects;
    }

    // Whether the project contains subprojects
    containsSubprojects(): boolean {
        return this.subprojects.length > 0;
    }

    async getJavaVersion(): Promise<number | undefined> {
        if (!this.containsSubprojects()) {
            return this.extractJavaVersion();
        }

        let maxJavaVersion: number | undefined;

        for (const subproject of this.subprojects) {
            const projectDirectory: string = vscode.Uri.file(subproject.directory).toString();
            const subInfos: any[] = await vscode.commands.executeCommand(GET_PROJECT_INFO, projectDirectory);
            if (subInfos?.length && subInfos[0]) {
                const javaVersion = subproject.extractJavaVersion();

                if (!maxJavaVersion || (javaVersion && javaVersion > maxJavaVersion)) {
                    maxJavaVersion = javaVersion;
                }
            }
        }
        return maxJavaVersion;
    }

    // Extracts project java version
    // Note: update when this feature becomes available: https://github.com/apache/netbeans/issues/7557
    protected abstract extractJavaVersion(): number | undefined
}

export class MavenProject extends Project {

    constructor(directory: string, subprojects: any[]) {
        super(directory, subprojects);
    }

    extractJavaVersion(): number | undefined {
        const buildscript = path.resolve(this.directory, 'pom.xml');
        let version: string | undefined;
        if (fs.existsSync(buildscript)) {
            const parser: xml2js.Parser = new xml2js.Parser({ async: false });
            parser.parseString(fs.readFileSync(buildscript)?.toString() || '', (err, result) => {
                if (!err && result) {
                    const properties = result['project']?.['properties'];
                    if (properties?.[0]) {
                        const mavenCompilerTarget = properties[0]['maven.compiler.target'];
                        if (mavenCompilerTarget?.[0]) {
                            version = mavenCompilerTarget[0];
                            return;
                        }

                        const mavenCompilerSource = properties[0]['maven.compiler.source'];
                        if (mavenCompilerSource?.[0]) {
                            version = mavenCompilerSource[0];
                            return;
                        }

                        const jdkVersion = properties[0]['jdk.version'];
                        if (jdkVersion?.[0]) {
                            version = jdkVersion[0];
                            return;
                        }
                    }
                }
            })
        }
        return version ? Number(normalizeJavaVersion(version)) : undefined;
    }
}

export class GradleProject extends Project {

    constructor(directory: string, subprojects: any[]) {
        super(directory, subprojects);
    }

    getJavaCompatibilityFrom(buildscript: string, from: 'target' | 'source'): string | undefined {
        const res = from === 'target' ? GRADLE_TARGET_COMPATIBILITY_REGEX.exec(buildscript) : GRADLE_SOURCE_COMPATIBILITY_REGEX.exec(buildscript)
        if (res?.[3]) {
            return res[3]; // Get the version number directly
        } else if (res?.[1]) {
            return res[1]; // Get the version number from JavaVersion.toVersion
        }
        return undefined;
    }

    extractJavaVersion(): number | undefined {
        let version: number | undefined;
        const buildscript = path.resolve(this.directory, 'build.gradle');
        if (fs.existsSync(buildscript)) {
            fs.readFileSync(buildscript)?.toString().split(os.EOL).find(l => {
                let tempVersion: string | undefined = this.getJavaCompatibilityFrom(l, 'target');

                if (!tempVersion) {
                    tempVersion = this.getJavaCompatibilityFrom(l, 'source');
                }

                if (tempVersion) {
                    version = Number(normalizeJavaVersion(tempVersion));
                    return true;
                }

                return false;
            });
        }
        return version;
    }
}