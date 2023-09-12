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

import java.util.HashMap;

/**
 * A name space hierarchy, where each level holds a name directory with names that may be unique for
 * each level.
 */

public class Namespace {
    /** Parent namespace. */
    private final Namespace parent;

    /** Name directory - version count for each name */
    private final HashMap<String, Integer> directory;

    /**
     * Constructor
     */
    public Namespace() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param parent parent name space
     */
    public Namespace(final Namespace parent) {
        this.parent = parent;
        this.directory = new HashMap<>();
    }

    /**
     * Return the parent Namespace of this space.
     *
     * @return parent name space
     */
    public Namespace getParent() {
        return parent;
    }

    /**
     * Create a uniqueName name in the namespace in the form base$n where n varies .
     *
     * @param base Base of name. Base will be returned if uniqueName.
     *
     * @return Generated uniqueName name.
     */
    public String uniqueName(final String base) {
        for (Namespace namespace = this; namespace != null; namespace = namespace.getParent()) {
            final HashMap<String, Integer> namespaceDirectory = namespace.directory;
            final Integer counter = namespaceDirectory.get(base);

            if (counter != null) {
                final int count = counter + 1;
                namespaceDirectory.put(base, count);
                return base + '-' + count;
            }
        }

        directory.put(base, 0);

        return base;
    }

    @Override
    public String toString() {
        return directory.toString();
    }
}
