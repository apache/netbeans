#!/usr/bin/env node
'use strict';
var fs = require('fs');
var strip = require('./strip-json-comments');
var input = process.argv[2];

if (input === 10) {
    return;
}

if (input === 20) {
    return;
}