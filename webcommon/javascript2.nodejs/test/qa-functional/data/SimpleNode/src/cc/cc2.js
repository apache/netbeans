/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var mod = require("../core/mymod");
var fs = require("fs");
var http = require("http");

//cc;1;mod.;0;myFnc,myOL,myArr,myInst,myRes,hasOwnProperty;myFnc;mod.myFnc();a,pokus,ale

//cc;1;fs.;0;readFile,FileReadStream,readFileSync;readFileSync;fs.readFileSync();CANCELLED,Client,ale,myArr

//cc;1;http.;0;Agent,request,get,globalAgent;globalAgent;http.globalAgent;main,extensions,myArr

var buf = require('buffer');
//cc;1;buf.;0;INSPECT_MAX_BYTES,Buffer,SlowBuffer,hasOwnProperty;INSPECT_MAX_BYTES;buf.INSPECT_MAX_BYTES;main,extensions,myArr

var child = require('child_process');
//cc;1;child.;0;_forkChild,fork,exec,execFile,hasOwnProperty,spawn;exec;child.exec();myArr

var cluster = require('cluster');
//cc;1;cluster.;0;isMaster,isWorker,setupMaster,disconnect,hasOwnProperty,worker,workers;setupMaster;cluster.setupMaster();myArr

var cons = require('console');
//cc;1;cons.;0;log,assert,dir,info,hasOwnProperty,error,time,timeEnd,trace;timeEnd;cons.timeEnd();myArr

var cr = require('crypto');
//cc;1;cr.;0;getCiphers,getHashes,createCredentials,createHash,hasOwnProperty,DEFAULT_ENCODING,randomBytes;getHashes;cr.getHashes();myArr

var dg = require('dgram');
//cc;1;dg.;0;Socket,createSocket,_createSocketHandle,hasOwnProperty;Socket;dg.Socket;myArr

var dn = require('dns');
//cc;1;dn.;0;lookup,ADNAME,resolve,resolve4,hasOwnProperty,resolve6,resolveMx,resolveTxt,resolveSrv,reverse;resolve;dn.resolve();myArr

var dom = require('domain');
//cc;1;dom.;0;create,Domain,active,hasOwnProperty;Domain;dom.Domain();myArr

var evs = require('events');
//cc;1;evs.;0;EventEmitter,usingDomains,hasOwnProperty;defineProperties;evs.defineProperties();myArr

var fre = require('freelist');
//cc;1;fre.;0;FreeList,hasOwnProperty;FreeList;fre.FreeList();myArr

var https = require('https');
//cc;1;https.;0;createServer,Server,hasOwnProperty,request,get,Agent,globalAgent;get;https.get();myArr

var modu = require('module');
//cc;1;modu.;0;Module,_debug,hasOwnProperty,_cache,wrap,_load;_compile;modu._compile();myArr

var net = require('net');
//cc;1;net.;0;createServer,connect,hasOwnProperty,createConnection,createConnection;createServer;net.createServer();myArr

var os = require('os');
//cc;1;os.;0;tmpdir,getNetworkInterfaces,hasOwnProperty,EOL,release;platform;os.platform();myArr

var pth = require('path');
//cc;1;pth.;0;normalize,join,hasOwnProperty,resolve,relative,dirname,basename,extname,sep,delimiter;dirname;pth.dirname();myArr

var qs = require('querystring');
//cc;1;qs.;0;stringify,parse,hasOwnProperty,escape,unescape;parse;qs.parse();myArr

var rl = require('readline');
//cc;1;rl.;0;createInterface,Interface,hasOwnProperty,clearLine,cursorTo;cursorTo;rl.cursorTo();myArr

var rep = require('repl');
//cc;1;rep.;0;start,REPLServer,hasOwnProperty;start;rep.start();myArr

var str = require('stream');
//cc;1;rep.;0;start,REPLServer,hasOwnProperty;start;rep.start();myArr


var sdec = require('string_decoder');
//cc;1;sdec.;0;StringDecoder,hasOwnProperty;StringDecoder;sdec.StringDecoder();myArr

var tim = require('timers');
//cc;1;tim.;0;setTimeout,clearTimeout,setInterval,clearInterval,setImmediate,clearImmediate,hasOwnProperty;setInterval;tim.setInterval();myArr

var tls = require('tls');
//cc;1;tls.;0;getCiphers,createServer,SLAB_BUFFER_SIZE,connect,createSecurePair,Server,hasOwnProperty;connect;tls.connect();myArr

var tty = require('tty');
//cc;1;tty.;0;isatty,setRawMode,ReadStream,WriteStream,hasOwnProperty;isatty;tty.isatty();myArr

var url = require('url');
//cc;1;url.;0;parse,format,resolve,resolveObject,hasOwnProperty;resolve;url.resolve();myArr

var util = require('util');
//cc;1;util.;0;debug,format,error,puts,print,log,inspect,isArray,hasOwnProperty;isArray;util.isArray();myArr

var vm = require('vm');
//cc;1;vm.;0;runInThisContext,runInNewContext,runInContext,createContext,createScript,hasOwnProperty;createContext;vm.createContext;myArr

var zlib = require('zlib');
//cc;1;zlib.;0;Deflate,createDeflate,hasOwnProperty;createDeflate;zlib.createDeflate();myArr






