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


var net = require("http");
var rfnc = require("../app/ref/rrfunc");
var litr = require('../folder/literalRef'); var instRef = require("../folder/instanceRef");

//cc;1;litr.;0;pokus2,obj2,neco2,defineProperty,hasOwnProperty;pokus2;litr.pokus2;jejda,pokus,neco2,getDate,E

//cc;1;litr.ox.;0;f1,f3,defineProperty,hasOwnProperty;f1;litr.ox.f1;pokus2,obj2,neco2,getDate,E,pokus

//cc;1;litr.pokus2.;0;getDate,getDay,defineProperty,hasOwnProperty,getHours;getDay;litr.pokus2|.getDay();pokus2,obj2,neco2,E,pokus,push

//cc;1;litr.obj2.;0;nick2,dob2,defineProperty,hasOwnProperty,hello2,conf2;hello2;litr.obj2|.hello2();nick,pokus,hello

//cc;1;litr.obj2.dob2.;0;getDate,getDay,defineProperty,hasOwnProperty,getHours;getDay;litr.obj2.dob2|.getDay();pokus2,obj2,neco2,E,pokus,pop

//cc;1;litr.obj2.conf2.;0;a,b,defineProperty,hasOwnProperty;b;litr.obj2|.conf2.b;pokus,obj,aa,neco2,E,pokus


//cc;1;instRef.;0;rfirstName,rdob,rorigin,rprops,rwalk,rlastName,rgetName,hasOwnProperty;rwalk;instRef.rwalk();firstName,dob,origin,props,walk,identify,dateOfBirth,today,age,rnumberOfLegs

//cc;1;instRef.rage().;0;getDate,getDay,defineProperty,hasOwnProperty,getHours;getDay;instRef.rage().|getDay();rfakeOrigin,obj2,neco2,E,obj

//cc;1;instRef.rdob.;0;getDate,getDay,defineProperty,hasOwnProperty,getHours;getDay;instRef.rdob.|getDay();rfakeOrigin,obj2,neco2,E,obj

//cc;1;instRef.rorigin.;0;rnumberOfLegs,rinfo,defineProperty,hasOwnProperty;rinfo;instRef.rorigin.|rinfo();rfakeOrigin,obj2,neco2,getDate,E,obj

//cc;1;instRef.rorigin.rinfo().;0;a1,a2,defineProperty,hasOwnProperty;a1;instRef.rorigin.|rinfo().a1;rfakeOrigin,obj2,neco2,getDate,E,obj

//cc;1;instRef.rprops.;0;a,b,a3,defineProperty,hasOwnProperty;a;instRef.rprops.|a;fakeOrigin,obj2,a2,neco2,getDate,E,obj,b1

//cc;1;instRef.rprops.b.;0;b1,b2,defineProperty,hasOwnProperty;b1;instRef.rprops.|b.b1;fakeOrigin,obj2,neco2,getDate,E,obj,a2

//cc;1;instRef.rdateOfBirth().;0;getDate,getDay,defineProperty,hasOwnProperty,getHours;getDay;instRef.rdateOfBirth().|getDay();fakeOrigin,obj2,neco2,E,obj

//cc;1;instRef.rfakeOrigin.;0;rnumberOfLegs,rinfo,defineProperty,hasOwnProperty;rinfo;instRef.rfakeOrigin.r|info();fakeOrigin,obj2,props,getDate,E,obj


var rnewe = new rfnc();

//cc;1;rnewe.;0;rale,rale2,rbar,rattempt,rgetAttempt,hasOwnProperty;rale;rnewe.rale;ver,da,pokus,a1,ale,attempt

//cc;1;rnewe.rale2.;0;f,f1,hasOwnProperty;f;rnewe.rale2.f;rver,da,pokus,a1

//cc;1;rnewe.rbar().;0;getDate,getDay,defineProperty,hasOwnProperty,getHours;getDay;rnewe.rbar().|getDay();rfakeOrigin,robj2,neco2,f1,robj

//cc;1;rfnc.;0;rinn,rstNumber,hasOwnProperty,rstDate,rstObj;rstDate;rfnc.rstDate;rver,rda,pokus,a1

//cc;1;rfnc.rinn.;0;rver,rda,hasOwnProperty,rlog,rinit;rda;rfnc.rinn.rda;rale,rbar,pokus,a1

//cc;1;rfnc.rinn.rda.;0;getDate,getDay,hasOwnProperty;getDay;rfnc.rinn.rda.getDay();rale,bar,pokus,a1

//cc;1;rfnc.rinn.rlog.;0;messages,owner,hasOwnProperty;message;rfnc.rinn.rlog.messages;rale,rbar,pokus,a1

//cc;1;rnewe.rgetAttempt().;0;aa,ab,hasOwnProperty;aa;rnewe.rgetAttempt().aa;ver,da,pokus,a1

var ar = require("../folder/arrRef");
//cc;1;ar.;0;push,pop,hasOwnProperty;push;ar.push();ver,da,pokus,a1

