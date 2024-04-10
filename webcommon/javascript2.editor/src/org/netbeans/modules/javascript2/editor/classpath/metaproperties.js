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

var metaproperties = {};

metaproperties.new = {};
/**
 * The <strong><code>new.target</code></strong> meta-property lets you detect
 * whether a function or constructor was called using the <code>new</code>
 * operator. In constructors and functions invoked using the <code>new</code>
 * operator, <strong><code>new.target</code></strong> returns a reference to the
 * constructor or function that new was called upon. In normal function calls,
 * <strong><code>new.target</code></strong> is undefined.
 */
metaproperties.new.target = function(){};

metaproperties.import = {};
metaproperties.import.meta = {};
/**
 * The full URL to the module, includes query parameters and/or hash (following
 * the ? or #).
 *
 * In browsers, this is either the URL from which the script was obtained (for
 * external scripts), or the URL of the containing document (for inline
 * scripts).
 *
 * In Node.js, this is the file path (including the file:// protocol).
 *
 * @type String|null
 */
metaproperties.import.meta.url = "";
/**
 * import.meta.resolve() is a built-in function defined on the import.meta
 * object of a JavaScript module that resolves a module specifier to a URL using
 * the current module's URL as base.
 *
 * @param {String} moduleName
 * @returns {String}
 */
metaproperties.import.meta.resolve = function(moduleName) {};