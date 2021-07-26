import * as path from 'path';

import { downloadAndUnzipVSCode, resolveCliPathFromVSCodeExecutablePath, runTests } from 'vscode-test';

import * as cp from 'child_process';
import * as fs from 'fs';

async function main() {
    try {
        // The folder containing the Extension Manifest package.json
        // Passed to `--extensionDevelopmentPath`
        const extensionDevelopmentPath = path.resolve(__dirname, '../../');

        const vscodeExecutablePath: string = await downloadAndUnzipVSCode('1.56.2');
        const cliPath: string = resolveCliPathFromVSCodeExecutablePath(vscodeExecutablePath);

        cp.spawnSync(cliPath, ['--install-extension', 'hbenl.vscode-test-explorer'], {
            encoding: 'utf-8',
            stdio: 'inherit',
        });

        // The path to test runner
        // Passed to --extensionTestsPath
        const extensionTestsPath = path.resolve(__dirname, './suite/index');

        const workspaceDir = path.join(extensionDevelopmentPath, 'out', 'test', 'ws');

        if (!fs.statSync(workspaceDir).isDirectory()) {
            throw `Expecting ${workspaceDir} to be a directory!`;
        }

        // Download VS Code, unzip it and run the integration test
        await runTests({
            vscodeExecutablePath,
            extensionDevelopmentPath,
            extensionTestsPath,
            extensionTestsEnv: {
                'ENABLE_CONSOLE_LOG' : 'true'
            },
            launchArgs: [workspaceDir, '--async-stack-traces']
        });
    } catch (err) {
        console.error('Failed to run tests');
        process.exit(1);
    }
}

main();
