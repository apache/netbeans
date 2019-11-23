#!/usr/bin/env node

import {basename} from "path"
import {readFileSync as readFile} from "fs"
import * as acorn from "../dist/acorn.js"

let infile, forceFile, silent = false, compact = false, tokenize = false
const options = {}

function help(status) {
  const print = (status == 0) ? console.log : console.error
  print("usage: " + basename(process.argv[1]) + " [--ecma3|--ecma5|--ecma6]")
  print("        [--tokenize] [--locations] [---allow-hash-bang] [--compact] [--silent] [--module] [--help] [--] [infile]")
  process.exit(status)
}