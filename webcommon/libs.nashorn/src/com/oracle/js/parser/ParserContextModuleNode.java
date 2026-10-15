/*
 * Copyright (c) 2010, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oracle.js.parser;

import com.oracle.js.parser.ir.ExportNode;
import com.oracle.js.parser.ir.ImportNode;
import java.util.ArrayList;
import java.util.List;

import com.oracle.js.parser.ir.Module;
import com.oracle.js.parser.ir.Module.ExportEntry;
import com.oracle.js.parser.ir.Module.ImportEntry;

/**
 * ParserContextNode that represents a module.
 */
class ParserContextModuleNode extends ParserContextBaseNode {

    /** Module name. */
    private final String name;

    private List<String> requestedModules = new ArrayList<>();
    private List<ImportEntry> importEntries = new ArrayList<>();
    private List<ExportEntry> localExportEntries = new ArrayList<>();
    private List<ExportEntry> indirectExportEntries = new ArrayList<>();
    private List<ExportEntry> starExportEntries = new ArrayList<>();

    private List<ImportNode> imports = new ArrayList<>();
    private List<ExportNode> exports = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param name name of the module
     */
    ParserContextModuleNode(final String name) {
        this.name = name;
    }

    /**
     * Returns the name of the module.
     *
     * @return name of the module
     */
    public String getModuleName() {
        return name;
    }

    public void addImport(ImportNode importNode) {
        imports.add(importNode);
    }

    public void addExport(ExportNode exportNode) {
        exports.add(exportNode);
    }

    public void addModuleRequest(String moduleRequest) {
        requestedModules.add(moduleRequest);
    }

    public void addImportEntry(ImportEntry importEntry) {
        importEntries.add(importEntry);
    }

    public void addLocalExportEntry(ExportEntry exportEntry) {
        localExportEntries.add(exportEntry);
    }

    public void addIndirectExportEntry(ExportEntry exportEntry) {
        indirectExportEntries.add(exportEntry);
    }

    public void addStarExportEntry(ExportEntry exportEntry) {
        starExportEntries.add(exportEntry);
    }

    public Module createModule() {
        return new Module(requestedModules, importEntries, localExportEntries, indirectExportEntries, starExportEntries, imports, exports);
    }
}
