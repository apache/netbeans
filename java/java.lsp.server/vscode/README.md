This is an extension for VS Code. Based on "lsp-sample" from:
https://github.com/microsoft/vscode-extension-samples

# Building

To build the VS Code extension do:
 * cd java/java.lsp.server
 * ant build-lsp-server
 * cd vscode
 * npm install
 * npm run compile
 * (cd ../build/; npm install vsce)
 * ../build/node_modules/vsce/out/vsce package

The resulting extension is then in this directory, with the vsix extension.



