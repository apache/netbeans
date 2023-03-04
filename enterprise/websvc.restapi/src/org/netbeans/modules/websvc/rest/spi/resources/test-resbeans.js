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

/*
* Supporting js for testing resource beans
*/

function TestSupport() {
    this.wadlDoc = null;
    this.wadlURL = '';
    this.wadlErr = 'MSG_TEST_RESBEANS_wadlErr';
    this.currentValidUrl = '';
    this.breadCrumbs = [];
    this.currentMethod = '';
    this.currentMimeType = '';
    this.topUrls = [];
    this.paramNumber = 1;
    this.bcCount = 0;
    this.currMonitorText = null;
    this.childrenContent = '';
    this.currentXmlHttpReq = '';
    this.tcStr = '';
    this.tcCount = 0;
    this.prettyContent = '';
    this.colSize = "86";
    this.rowSize = "20";
    this.iframeWidth = "530";
    this.iframeHeight = "400";
    this.allcat = [];
    this.projectName = '';
    
    this.expand = new Image();
    this.expand.src = "expand.gif";
    this.collapse = new Image();
    this.collapse.src = "collapse.gif";
    this.og = new Image();
    this.og.src = "og.gif";
    this.cg = new Image();
    this.cg.src = "cg.gif";

    this.viewIds = [
        { "id" : "table" , "name":"MSG_TEST_RESBEANS_TabularView", "type":"tableContent"}, 
        { "id" : "raw" , "name":"MSG_TEST_RESBEANS_RawView", "type":"rawContent"}, 
        { "id" : "structure" , "name":"MSG_TEST_RESBEANS_SubResources", "type":"structureInfo"},
        { "id" : "header" , "name":"MSG_TEST_RESBEANS_Headers", "type":"headerInfo"},
        { "id" : "monitor" , "name":"MSG_TEST_RESBEANS_Monitor", "type":"monitorContent"}];
    
    this.xhr = new XHR();
    this.wdr = new WADLParser();
}

TestSupport.prototype = {    

    init : function () {
        this.debug('Initializing scripts...');
        var patterns = baseURL.split('||');
        baseURL = patterns[0];
        // handle the Netbeans 6.5 way of retrieving applicationPath
        if (patterns.length == 3) {
            var servletNames = patterns[1].split(',');
            var servletUrl = patterns[2].split(',');
            var resourceUri = 'resources/';
            for(var i in servletNames) {
                var name = servletNames[i];
                if ('ServletAdaptor' == name) {
                    resourceUri = servletUrl[i].replace('*', '');
                    break;
                }
            }
            baseURL = this.concatPath(baseURL, resourceUri);
        } else if (patterns.length == 2) {
            var applicationPath = patterns[1]+'/';
            baseURL = this.concatPath(baseURL, applicationPath);
        }
        this.wadlURL = this.concatPath(baseURL, "application.wadl");
        this.initFromWadl();
    },

    initFromWadl : function () {
        var wDoc = this.wdr.getWadlDocument(this.wadlURL);
        if(wDoc != null) {
            ts.wadlDoc = wDoc;
            this.wdr.updateMenu(this.wadlURL, ts.wadlDoc);
        } else {
            this.setvisibility('main', 'inherit');
            this.updatepage('content', 'MSG_TEST_RESBEANS_Help');
        }            
    },

    concatPath : function(url, pathElem) {
        var ndx = url.length;
        if(url.substring(ndx-1) == '/') {
            for(;ndx>=0;ndx--) {
                if(url.substring(ndx-2,ndx-1) != '/')
                    break;
            }
            url = url.substring(0, ndx-1);
        }
        ndx = 0;
        if(pathElem.substring(ndx,ndx+1) == '/') {
            for(;ndx<pathElem.length;ndx++) {
                if(pathElem.substring(ndx,ndx+1) != '/')
                    break;
            }
            pathElem = pathElem.substring(ndx);
        }
        return url + '/' + pathElem;
    },
    
    trim : function (str) {
        if(!str || typeof str != 'string')
           return null;
        return str.replace(/^[\s]+/,'').replace(/[\s]+$/,'').replace(/[\s]{2,}/,' ');
    },

    setvisibility : function (id, state) {
        try {
            document.getElementById(id).style.visibility = state;
        } catch(e) {}
    },
    
    getElementsByTagName : function (parent, name) {
        var results = [];
        if(parent != null && name != null) {
          var childs = parent.childNodes;
          for(var i=0;i<childs.length;i++) {
            var child = childs[i];
            var nodeName = child.nodeName;
            if(name == nodeName || nodeName.indexOf(":"+name, 
                nodeName.length-name.length-1)!==-1) 
            {
                results.push(child);
            }
          }
        }
        return results;
    },
    
    changeMethod : function ()
    {    
        var methodNode = document.getElementById("methodSel");
        var method = methodNode.options[methodNode.selectedIndex].value;  
        var mIndex = -1;
        if(method.indexOf('[') != -1) {
            mIndex = method.substring(method.indexOf('[')+1, method.lastIndexOf(']'));
            method = method.substring(0, method.indexOf('['));
        }
        var mimeNode = document.getElementById("mimeSel");
        if(mimeNode == null || mimeNode == undefined) {
            this.currentMimeType = this.wdr.getMimeType(method);
        }
        this.currentMethod = this.wdr.getMethod(method);
        document.getElementById("methodName").value = this.currentMethod;
        var request = null;
        var resource = this.currentResource;
        if(resource != null && mIndex != -1) {
            var m = this.getElementsByTagName(resource, "method")[mIndex];
            request = this.getElementsByTagName(m, "request");
        }
        var paramRep = this.getParamRep(request, this.currentMethod);
        this.updatepage('paramHook', paramRep);
        document.getElementById("mimeType").value = this.currentMimeType;
        ts.clearOutput();
    },
    
    addTemplateParams : function (resource, request) {
        if(resource != null && request != null) {
            var rPath = resource.attributes.getNamedItem('path');
            if(rPath == null)
                return;
            var paths = rPath.nodeValue.split('/');
            for(var i in paths) {
                var path = paths[i];
                if(path.indexOf("{") != -1) {
                    var param = document.createElement("param");
                    request.addChild(param);
                    var ndx = path.indexOf('{');
                    var name = path.substring(ndx+1, path.indexOf('}'));
                    param.name = name;
                    param.type = "xs:string";
                    param.style = "template";
                }
            }
        }
    },
    
    changeMimeType : function ()
    {
        var mimeNode = document.getElementById("mimeSel");
        var mime = mimeNode.options[mimeNode.selectedIndex].value;
        document.getElementById("mimeType").value = mime;
    },
    
    getMethodMimeTypeCombo : function (resource) {
        var methods = this.getElementsByTagName(resource, "method");
        var str = '<table border=0><tbody><tr><td valign="top"><span id="j_id14"><label for="methodSel" class="LblLev2Txt_sun4">'+
                            '<span>MSG_TEST_RESBEANS_ChooseMethod: </span></label></span></td>';
        str += "<td><span id=j_id14><select id='methodSel' class=MnuJmp_sun4 name='methodSel' onchange='javascript:ts.changeMethod();'>";
        for(var j=0;j<methods.length;j++) {
            var m = methods[j];                            
            var mName = m.attributes.getNamedItem("name").nodeValue;
            if ('OPTIONS' === mName) {
                continue;
            }
            var mediaType = this.wdr.getMediaType(m);
            if(mediaType == null)
                mediaType = this.wdr.getDefaultMime();
            var mimeTypes = mediaType.split(',');
            for(var k=0;k<mimeTypes.length;k++) {
                var mimeType = mimeTypes[k];
                var dispName = this.wdr.getMethodNameForDisplay(mName, mimeType);
                if(mName == 'GET')
                    str += "  <option class=MnuJmpOpt_sun4 value='"+dispName+"["+j+"]' selected>"+dispName+"</option>";
                else
                    str += "  <option class=MnuJmpOpt_sun4 value='"+dispName+"["+j+"]'>"+dispName+"</option>";
            }
        }   
        str += "</select></span></td><td width=46/><td><a class='Btn1_sun4 Btn1Hov_sun4' onclick='ts.testResource()'>MSG_TEST_RESBEANS_TestButton</a></td></tr></tbody></table>";
        return str;
    },
    
    doShowContent : function (uri) {
        this.clearInput();
        document.getElementById("req_headers").style.visibility="visible";
        var r = this.wdr.findResource(uri);
        this.currentResource = r;
        if(r != null) {
            var app1 = this.wadlDoc.documentElement;     
            this.doShowStaticResource(uri, r);
        } else {
            this.doShowDynamicResource(uri, this.wdr.getDefaultMethod(), this.wdr.getDefaultMime());
        }
    },

    doShowContentForId : function (ndx) {
        this.clearInput();
        var elem = document.getElementById("req_headers");
        if (elem) {
            elem.style.visibility="visible";
        }
        var cat = ts.allcat[ndx];
        var r = cat.r;
        var uri = cat.uri;
        this.currentResource = r;
        if(r != null) {
            var app1 = this.wadlDoc.documentElement;     
            this.doShowStaticResource(uri, r);
        } else {
            this.doShowDynamicResource(uri, this.wdr.getDefaultMethod(), this.wdr.getDefaultMime());
        }
    },
    
    getResourcePath : function (n) {
        var path = this.getPath(n, '')
        return path.replace(/\/\//g,"\/");
    },
    
    getPath : function (n, pathVal) {
        if(n.parentNode == null || n.attributes.getNamedItem('path') == null) {
            if(pathVal == null || pathVal == '')
                return '';
            else
                return pathVal;
        } else {
            var path = n.attributes.getNamedItem('path');
            var pathElem = path.nodeValue;
            if(pathVal == null || pathVal == '') {
                return this.getPath(n.parentNode, pathElem);
            } else {
                return this.getPath(n.parentNode, pathElem+'/'+pathVal);
            }
        }
    },
    
    doShowDynamicResource : function (uri, mName, mediaType) {
        ts.clearOutput();
        paramNumber = 1;
        var qmName = '';
        if(mediaType != null)
            qmName = qmName + "("+mediaType+")";
        else
            mediaType = this.getDefaultMime();
        this.showBreadCrumbs(uri);
        var req = this.getDisplayUri(uri);
        var str = '<br/><table border=0><tbody><tr><td valign="top"><span id="j_id14"><label for="methodSel" class="LblLev2Txt_sun4">'+
                            '<span>MSG_TEST_RESBEANS_ChooseMethod: </span></label></span></td>';
        str += "<td><span id=j_id14><select id='methodSel' class=MnuJmp_sun4 name='methodSel' onchange='javascript:ts.changeMethod();'>";
        str += "  <option class=MnuJmpOpt_sun4 selected value='GET'>GET</option>";
        str += "  <option class=MnuJmpOpt_sun4 value='PUT'>PUT</option>";
        str += "  <option class=MnuJmpOpt_sun4 value='DELETE'>DELETE</option>";
        str += "</select></span></td>";
        str += '<td valign="top"><span id="j_id14"><label for="methodSel" style="padding-left: 6px;" class="LblLev2Txt_sun4">'+
            '<span>MSG_TEST_RESBEANS_ChooseMime: </span></label></span></td>';
        str += "<td><span id=j_id14><select id='mimeSel' class=MnuJmp_sun4 name='mimeSel' onchange='javascript:ts.changeMimeType();'>";
        str += "  <option class=MnuJmpOpt_sun4 value='application/xml'>application/xml</option>";
        str += "  <option class=MnuJmpOpt_sun4 value='application/json'>application/json</option>";
        str += "  <option class=MnuJmpOpt_sun4 value='text/xml'>text/xml</option>";
        str += "  <option class=MnuJmpOpt_sun4 value='text/plain'>text/plain</option>";
        str += "  <option class=MnuJmpOpt_sun4 value='text/html'>text/html</option>";
        str += "  <option class=MnuJmpOpt_sun4 value='image/*'>image/*</option>"; 
        str += "</select></span></td>";
        str += "<td width=30/>"
        str += "<td><span id=j_id14><a class='Btn2_sun4 Btn1Hov_sun4' onclick='ts.addParam()'>MSG_TEST_RESBEANS_AddParamButton</a>";
        str += "</span></td><td><a class='Btn1_sun4 Btn1Hov_sun4' onclick='ts.testResource()'>MSG_TEST_RESBEANS_TestButton</a></td></tr></tbody></table><br/>";
        ts.updatepage('testaction', str);
        ts.updatepage('testinput', this.getFormRep(req, uri, mName, mediaType));
        var paramRep = this.getTemplateParamsRep(req);
        if(paramRep != "") {
            ts.updatepage('pathParamHook', paramRep);
        }
        var disp = this.getDisplayUri(req);
        var uriLink = "<a id='"+req+"' class=Hyp_sun4 href=javascript:ts.doShowContent('"+req+"') >"+this.getDisplayURL(disp, 80)+"</a>";
        this.updatepage('request', '<span class=bld>MSG_TEST_RESBEANS_Resource:</span> '+uriLink+' <br/>(<a href="'+req+'" class=Hyp_sun4 target="_blank"><span>'+this.getDisplayURL(req, 90)+'</span></a>)');
    },
    
    doShowStaticResource : function (uri, r) {
        ts.clearOutput();
        this.paramNumber = 1;
        this.showBreadCrumbs(uri);
        var mName = this.wdr.getDefaultMethod();
        var mediaType = this.wdr.getDefaultMime();   
        var req = uri;
        var disp = this.getDisplayUri(req);        
        this.updatepage('testaction', '<br/>'+this.getMethodMimeTypeCombo(r)+'<br/>');
        this.updatepage('testinput', this.getFormRep(disp, uri, mName, mediaType));
        var paramRep = this.getTemplateParamsRep(disp);
        if(paramRep != "") {
            ts.updatepage('pathParamHook', paramRep);
        }
        var methodNode = document.getElementById("methodSel");
        var options = methodNode.options;
        for(var i=0;i<options.length;i++) {
            if(options[i].value.substring(0, 3) == 'GET') {
                methodNode.selectedIndex = i;
            }
        }
        this.changeMethod();    
        var uriLink = "<a id='"+req+"' class=Hyp_sun4 href=javascript:ts.doShowContent('"+req+"') >"+this.getDisplayURL(disp, 80)+"</a>";
        this.updatepage('request', '<span class=bld>MSG_TEST_RESBEANS_Resource:</span> '+uriLink+' <br/>(<a href="'+req+'" class=Hyp_sun4 target="_blank"><span>'+this.getDisplayURL(req, 90)+'</span></a>)');
    },
    
    getFormRep : function (req, uri, mName, mediaType) {
        if(mName == null || mName == 'undefined')
            mName = this.getDefaultMethod();
        if(mediaType == null || mediaType == 'undefined')
            mediaType = this.getDefaultMime();
        var str = "<div id='formSubmittal'>";
        str += "<form action='javascript:ts.dummyMethod()' method="+mName+" id='form1' name='form1'>";
        str += "<div id='pathParamHook'></div>";
        str += "<div id='paramHook'></div>";
        str += "<input name='path' value='"+uri+"' type='hidden'>";
        str += "<input id='methodName' name='methodName' value='"+mName+"' type='hidden'>";
        str += "<input id='mimeType' name='mimeType' value='"+mediaType+"' type='hidden'>";
        str += "<input id='test' name='test' style='display:none' value='' type='submit' onchange='ts.testResource();'>";
        str += "</form>";
        str += "</div>";
        return str;
    },
    
    getTemplateParamsRep : function (req) {
        var paramRep = "";
        var paths = ts.trim(req).split('/');
        for(var i in paths) {
          var path = ts.trim(paths[i]);
          if(path == null)
            continue;
          var compositeIds = path.split(',');
          for(var j in compositeIds) {
            var compositeId = ts.trim(compositeIds[j]);
            if(compositeId != null && compositeId.indexOf('{') > -1) {
                var pname = compositeId.substring(1, compositeId.length-1);
                paramRep += '<tr><td valign="top"><span id="j_id14"><label for="tparams" class="LblLev2Txt_sun4">';
                paramRep += '<span>'+pname+': </span></label></span></td>';
                paramRep += '<td><span id="j_id14"><input id=tparams name="'+pname+'" type=text value="" size=40 title="'+pname+'" class="TxtFld_sun4 TxtFldVld_sun4"/></span></td></tr>';
            }
          }
        }
        if(paramRep != "") {
            paramRep = "<table border=0><tbody><tr>"+paramRep+"</tr></tbody></table>";
        }
        return paramRep;
    },
    
    dummyMethod : function() {
    },
    
    addParam : function () {
        var str = '<tr><td valign="top"><span id="j_id14"><label for="newParamNames" class="LblLev2Txt_sun4">';
        str += '<span>param'+paramNumber+': </span></label></span></td>';
        str += '<td><span id="j_id14"><input id=newParamNames name="param'+paramNumber+'" type=text value="param'+paramNumber+'" size=40 title="param'+paramNumber+'" class="TxtFld_sun4 TxtFldVld_sun4"/></span></td></tr>';
        
        str += '<tr><td valign="top"><span id="j_id14"><label for="newParamValues" class="LblLev2Txt_sun4">';
        str += '<span>value'+paramNumber+': </span></label></span></td>';
        str += '<td><span id="j_id14"><input id=newParamValues name="param'+paramNumber+'" type=text value="value'+paramNumber+'" size=40 title="value'+paramNumber+'" class="TxtFld_sun4 TxtFldVld_sun4"/></span></td></tr>';
        var prevParam = document.getElementById("paramHook").innerHTML;
        if(prevParam.indexOf('Additional parameters') == -1) {
            str = '<tr><td valign="top"><span id="j_id14"><label for="dummy" class="LblLev2Txt_sun4">'+
                            '<span>Additional parameters MSG_TEST_RESBEANS_AdditionalParams:</span></label></span></td>'+
                            '<td><span id="j_id14"></span></td></tr>'+str;
        }
        document.getElementById("paramHook").innerHTML = prevParam + str;
        this.saveFormInput('form1', 'resttest-');
        paramNumber++;
    },
    
    setCookie : function (name, value, expires, path, domain, secure) {
        var today = new Date();
        today.setTime( today.getTime() );
        if(expires) {
            expires = expires * 1000 * 60 * 60 * 24;
        }
        var expires_date = new Date( today.getTime() + (expires) );
        document.cookie = name+"="+escape( value ) +
            ( ( expires ) ? ";expires="+expires_date.toGMTString() : "" ) +
            ( ( path ) ? ";path=" + path : "" ) +
            ( ( domain ) ? ";domain=" + domain : "" ) +
            ( ( secure ) ? ";secure" : "" );
    },
    
    getCookie : function ( name ) {
        var start = document.cookie.indexOf( name + "=" );
        var len = start + name.length + 1;
        if((!start) && (name != document.cookie.substring(0, name.length))) {
            return null;
        }
        if(start == -1) 
            return null;
        var end = document.cookie.indexOf( ";", len );
        if(end == -1) 
            end = document.cookie.length;
        return unescape(document.cookie.substring(len, end));
    },

    saveFormInput : function (form_id, pfx) {
        var form = document.getElementById(form_id);
        var els = document.getElementsByTagName('input');
        for (var i = 0; i < els.length; i++) {
            var el = els.item(i);
            if (el.type == 'text') {
                el.onblur = function() {
                    var name = this.name;
                    var value = this.value;
                    ts.setCookie( pfx + name, value);
                };
                var old_value = this.getCookie(pfx + el.name);
                if (old_value && old_value != '') {
                    el.value = old_value;
                }
            }
        }
    },

    clearAll : function() {
        this.clearOutput();
        document.getElementById('req_headers').style.visibility="hidden";
        this.updatepage('request', 'MSG_TEST_RESBEANS_INFO');
        this.updatepage('testaction', '');
        this.updatepage('testinput', '');
        this.updatepage('navigation', '');
    },
    
    clearInput : function() {
        this.updatepage('paramHook', '');
        var testInput = document.getElementById('testinput');
        testInput.className = 'ConMgn_sun4';
    },
    
    clearOutput : function() {
        this.updatepage('result', '');
        this.updatepage('resultheaders', '');
    },
    
    trimEndingPathDelim : function(path) {
        var req = path;
        if(req.substring(req.length-1) == '/')
            req = req.substring(0, req.length-1);
        return req;
    },
    
    showBreadCrumbs : function (uri) {
        var disp = this.getDisplayUri(uri);
        this.breadCrumbs[1] = disp;
        var str = "<a class=Hyp_sun4 href=javascript:ts.clearAll() >"+ts.projectName+"</a>";
        var req = this.getDisplayUri(uri);
        var currPath = baseURL;
        if(currPath.substring(currPath.length-1) != '/')
            currPath = currPath + '/';
        req = this.trimEndingPathDelim(req);
        var paths = req.split('/');
        for(var i=0;i<paths.length-1;i++) {
            var pname = paths[i];
            if(pname == '')
                continue;
            currPath += pname+'/';
            var ndx = 0;
            var jsmethod = "ts.doShowContent('"+currPath+"')";
            for(var j=0;j<ts.allcat.length;j++) {
                if(ts.allcat[j].uri == currPath) {
                    ndx = j;
                    jsmethod = "ts.doShowContentForId('"+ndx+"')";
                }
            }
            str += "&nbsp;&gt; <a id='"+currPath+"' class=Hyp_sun4 href=javascript:"+jsmethod+" >"+pname+"</a>";
        }
        str += "<span>&nbsp;&gt; "+paths[paths.length-1]+"</span>";
        this.updatepage('navigation', str);
    },

    getParamRep : function (req, mName) {
        var str = '<table border="0"><tbody>';        
        if(req != null && req.length > 0) {      
            for(var i=0;i<req.length;i++) {
                var params = req[i].childNodes;
                if(params != null) {
                    for(var j=0;j<params.length;j++) {
                        var param = params[j];
                        if(param.nodeName == null || param.nodeName != 'param')
                            continue;
                        var pname = param.attributes.getNamedItem('name').nodeValue;
                        var defaultVal = '';
                        if(param.attributes.getNamedItem('default') != null)
                            defaultVal = param.attributes.getNamedItem('default').nodeValue;
                        var type = 'query';
                        if(param.attributes.getNamedItem('style') != null)
                            type = param.attributes.getNamedItem('style').nodeValue;
                        var paramsId = 'qparams';
                        if(type == 'template')
                            paramsId = 'tparams';
                        else if(type == 'matrix')
                            paramsId = 'mparams';
                        str += '<tr><td valign="top"><span id="j_id14"><label for="'+paramsId+'" class="LblLev2Txt_sun4">'+
                            '<span>'+pname+'</span></label></span></td>'+
                            '<td><span id="j_id14"><input type="text" id="'+paramsId+'" name="'+pname+'" value="'+defaultVal+'" size=40 title="'+pname+'" class="TxtFld_sun4 TxtFldVld_sun4"/></span></td></tr>';
                    }
                }
            }
        }
        if(mName == 'PUT' || mName == 'POST') {   
            str += '<tr><td valign="top"><span id="j_id14"><label for="blobParam" class="LblLev2Txt_sun4">'+
                '<span>MSG_TEST_RESBEANS_Content: </span></label></span></td>'+
                '<td><span id="j_id14"><textarea class="TxtAra_sun4 TxtAraVld_sun4" id=blobParam name=params rows=6 cols=65>MSG_TEST_RESBEANS_Insert</textarea></span></td></tr>';
        }
        str += '</tbody></table>';
        return str;
    },
    
    testResource : function () {
        this.updatepage('result', 'MSG_TEST_RESBEANS_Loading');
        
        if (document.getElementById('req_hdr_1')) {
            ts.storeRequestHeaders();
        }
        
        var testInput = document.getElementById('testinput');
        testInput.className = 'ConMgn_sun4 fxdHeight';
        var mimetype = this.getFormMimeType();
        var method = this.getFormMethod();
        var p = '';
        var path = document.forms[0].path.value;
        
        //filter template parameters that show up on the path
        var tps = document.forms[0].tparams;
        var tparams = [];
        var found = path.indexOf( "{" );
        if (found != -1){
            if(tps != null) {
                if(tps.length == undefined) {
                    if(path.indexOf("{"+tps.name+"}"))
                        path = path.replace("{"+tps.name+"}", tps.value);
                    else
                        tparams.push(tps);
                } else {
                    var len = tps.length;
                    for(var j=0;j<len;j++) {
                        var param = tps[j];
                        if(path.indexOf("{"+param.name+"}"))
                            path = path.replace("{"+param.name+"}", param.value);
                        else
                            tparams.push(param);
                    }
                }
            }
        }
        
        var qparams = document.forms[0].qparams;
        if(qparams != null) {
            if(qparams.length == undefined) {
                p += qparams.name+"="+qparams.value;
            } else {
                var len = qparams.length;
                for(var j=0;j<len;j++) {
                    var param = qparams[j]
                    if(len == 1 || len-j == 1)
                            p += escape(param.name)+"="+escape(param.value);
                    else
                            p += escape(param.name)+"="+escape(param.value)+"&";
                }
            }
        }

        //process user added parameters
        var newParamNames = document.forms[0].newParamNames;
        var newParamValues = document.forms[0].newParamValues;
        if(newParamNames != null) {
            if(newParamNames.length == undefined) {
                p += newParamNames.value+"="+newParamValues.value;
            } else {
                var len = newParamNames.length;
                for(var j=0;j<len;j++) {
                    var paramName = newParamNames[j].value;
                    var paramValue = newParamValues[j].value;
                    if(len == 1 || len-j == 1)
                        p += escape(paramName)+"="+escape(paramValue);
                    else
                        p += escape(paramName)+"="+escape(paramValue)+"&";
                }
            }
        }
        
        var params = null;
        var paramLength = 0;
        if(method == 'POST' || method == 'PUT'){
            var blobParam = document.getElementById('blobParam').value;
            if(blobParam != null && blobParam != undefined){
                params = blobParam;
            }
            if(p != null && p != undefined && p.length>0 ){
	    		if ( params!=null && params.length >0 ){
					params = p+"&"+params;
				}
				else {
					params = p;
				}
	    	}
            if(params != null)
                paramLength = params.length;
        } else if(method == 'GET' || method == 'DELETE') {
            paramLength = 0;
        }
        var req;
        if( ts.isURL(path) )
            req = path;
        else
            req = baseURL+escape(path);
        
        //change url if there are template params
        if(tparams != null) {
            if(tparams.length == undefined) {
                req += "/" + escape(tparams.value);
            } else {
                var len = tparams.length;
                for(var j=0;j<len;j++) {
                    req += "/" + escape(tparams[j].value);
                }
            }
        }

        if(method == 'GET' && p.length > 0)
            req+= "?"+p;
        
        //process matrix parameters
        var mparams = document.forms[0].mparams;
        if(mparams != null) {
            if(mparams.length == undefined) {
                req += ";"+escape(mparams.name)+"="+escape(mparams.value);
            } else {
                var len = mparams.length;
                for(var j=0;j<len;j++) {
                    var param = mparams[j]
                    req += ";"+escape(param.name)+"="+escape(param.value);
                }
            }
        }
        
        var disp = this.getDisplayUri(req);
        this.currentMethod = method;
        this.currentMimeType = mimetype;
        
        //add timestamp to make url unique in case of IE7
        var timestamp = new Date().getTime();
        if(req.indexOf("?") != -1)
            req = req+"&timestamp="+timestamp;
        else
            req = req+"?timestamp="+timestamp;
        
        var c = '';
        if(method == 'POST') {
            c = this.xhr.post(req, mimetype, params);
        } else if(method == 'PUT') {
            c = this.xhr.put(req, mimetype, params);
        } else if(method == 'GET') {
            c = this.xhr.get(req, mimetype);
        } else if(method == 'DELETE') {
            c = this.xhr.delete_(req);
        }
        ts.updateContent(c);
    },

    isURL : function (url) {
        return url.length > 7 && url.substring(0, 4) == "http";
    },
    
    createIFrameForUrl : function (url) {
        var c = 
            '<iframe id="iFrame_" src="'+url+'" class="frame" width="'+ts.iframeWidth+'" align="left">'+
                '<p>MSG_TEST_RESBEANS_See <a class=Hyp_sun4 href="'+url+'">"'+url+'"</a>.</p>'+
            '</iframe>';
        return c;
    },
    
    createIFrameForContent : function (content) {
        var iframe;
        if (document.createElement && (iframe =
            document.createElement('iframe'))) {
            iframe.name = iframe.id = 'iFrame_';
            iframe.width = ts.iframeWidth;
            iframe.height = ts.iframeHeight;
            iframe.src = 'about:blank';
            document.getElementById('rawContent').appendChild(iframe);
        }
        if (iframe) {
            var iframeDoc;
            if (iframe.contentDocument) {
                iframeDoc = iframe.contentDocument;
            }
            else if (iframe.contentWindow) {
                iframeDoc = iframe.contentWindow.document;
            }
            else if (window.frames[iframe.name]) {
                iframeDoc = window.frames[iframe.name].document;
            }
            if (iframeDoc) {
                iframeDoc.open();
                iframeDoc.write(content);
                iframeDoc.close();
            }
        }
    },
    
    showViews : function (name) {
        var c = 
          '<table cellspacing="0" cellpadding="0" border="0" title="" class="Tab1TblNew_sun4">'+
                '<tbody>'+
                    '<tr id="tabRow">';
        for(var i in this.viewIds) {
            var vid = this.viewIds[i]['id'];
            var tabMain = document.getElementById(this.viewIds[i]['type']);
            if(name == vid) {
                c += this.getTab(vid, true);
                tabMain.style.display="block";
            } else {
                c += this.getTab(vid, false);
                tabMain.style.display="none";
            }
        }
        c += '</tr></tbody></table>';
        this.updatepage('tabTable', c);
    },

    monitor : function (method, xmlHttpReq, param) {
        var rawContent = 'MSG_TEST_RESBEANS_Received:\n<br/>'+this.printPretty(xmlHttpReq.responseText)+'\n<br/>';
        if(param != null && param != undefined)
            rawContent = 'MSG_TEST_RESBEANS_Sent:\n<br/>'+this.printPretty(param) + '\n\n<br/><br/>' + rawContent;
        var cURL = this.currentValidUrl;
        var params = '';
        if(cURL.indexOf('?') > 0) {
            params = cURL.substring(cURL.indexOf('?')+1);
            cURL = cURL.substring(0, cURL.indexOf('?')+1);
        }
        var s = 'MSG_TEST_RESBEANS_Request: ' + this.currentMethod + ' ' + cURL + '\n<br/>' + params +
                    '\n\n<br/><br/>MSG_TEST_RESBEANS_Status: ' + xmlHttpReq.status + ' (' + xmlHttpReq.statusText + ')'+
                    '\n\n<br/><br/>MSG_TEST_RESBEANS_TimeStamp: ' + ' ' + xmlHttpReq.getResponseHeader('Date') + '';
        var prevs = '';
        if(this.currMonitorText != null && this.currMonitorText != undefined) {
            prevs = this.currMonitorText;        
            this.currMonitorText = 
                s + '\n\n<br/><br/>' + rawContent+
                '\n<br/>-----------------------------------------------------------------------\n\n<br/><br/>'+
                prevs;  
        } else {
            this.currMonitorText = s + '\n\n<br/><br/>' + rawContent;
        }
    },

    updateContent : function (content) {
        var showRaw = true;
        var cErr = 'MSG_TEST_RESBEANS_No_Container';
        if(content != null && content != undefined) {
            try {
                if(content.indexOf("RequestFailed -->") != -1)
                    throw "Error: "+content;
                if(content == '')
                content = 'MSG_TEST_RESBEANS_NoContents'
                else
                    content = content.replace(/'/g,"\'");
                var tableContent = cErr;
                if(content.indexOf("<?xml ") != -1 || 
                        content.indexOf('{"') != -1) {
                    var tc = this.getContainerTable(content);
                    if(tc != null) {
                        tableContent = tc;
                        showRaw = false;
                    }
                }
                var rawContent = content;
                this.updatepage('result', this.createTabs(tableContent, rawContent, showRaw));
                if(showRaw) {
                    if( ts.isURL(content) )
                        this.currentValidUrl = content;
                    if(this.currentMethod == 'GET' && this.currentMimeType == 'text/html') {
                        this.updatepage('rawContent', '');
                        this.createIFrameForContent(content);
                    } else if(content.indexOf("<?xml ") != -1) {
                        this.updatepage('rawContent', '');
                        this.updatepage('rawContent', this.printPretty(content));
                    } else {
                        if(this.currentMethod == 'GET' && this.currentMimeType != 'application/json' && this.currentMimeType != 'application/csv' && this.currentMimeType != 'text/plain')
                            this.updatepage('rawContent', this.createIFrameForUrl(this.currentValidUrl));
                        else
                            this.updatepage('rawContent', content);
                    }
                    this.showViews('raw');
                } else {
                    this.showViews('table');
                }
            } catch( e ) {
                ts.debug('updateContent() err name: [' + e.name + '] message: [' + e.message+"]");
                this.updatepage('result', this.createTabs(cErr, content));
                this.showViews('raw');
            }  
        }
    },
    
    createTabs : function(tableContent, rawContent, showRaw) {
        var nodisp = ' class="nodisp" ';
        var tableViewStyle = ' ';
        var rawViewStyle = nodisp;
        if(showRaw) {
            tableViewStyle = nodisp;
            rawViewStyle = ' ';
        }
        var structure = this.xhr.options(this.currentValidUrl, 'application/vnd.sun.wadl+xml');
        var subResources = this.getContainerTable(ts.wdr.evaluateWADLUpdate(this.currentValidUrl, structure));
        if(subResources == null)
            subResources = 'MSG_TEST_RESBEANS_No_SubResources';
        var tabs = '<br/><span class=bld>MSG_TEST_RESBEANS_Status:</span> '+ this.currentXmlHttpReq.status+' ('+this.currentXmlHttpReq.statusText+')<br/><br/>'+
                    '<span class=bld>MSG_TEST_RESBEANS_Response:</span> '+
                    '<div class="Tab1Div_sun4" id="tabTable"></div>'+
                    '<div class="tabMain">'+
                    '<div id="menu_bottom" class="stab tabsbottom"></div>'+
                    '<div id="headerInfo"'+nodisp+'>'+this.getHeaderAsTable(this.currentXmlHttpReq)+'</div>'+
                    '<div id="tableContent"'+tableViewStyle+'>'+tableContent+'</div>'+
                    '<div id="structureInfo"'+nodisp+'>'+subResources+'</div>'+
                    '<div id="rawContent"'+rawViewStyle+'>'+this.printPretty(rawContent)+'</div>'+ 
                    '<div id="monitorContent"'+nodisp+'>'+this.currMonitorText+'</div>'+
                    '</div>';
        return tabs;
    },
    
    printPretty : function(content) {
        if(content.indexOf("<?xml ") != -1) {
            var doc2 = this.xhr.loadXml(content);
            if(doc2 != null && doc2.documentElement.nodeName == 'parsererror')
                return content;
            prettyContent = "";
            this.prettyPrint(doc2);
            return prettyContent;
        }
        return content;
    },
    
    prettyPrint : function (/*Node*/ node) {
       printIndented(node, 0);

       function printIndented(/*Node*/ node, /*int*/ indent) {
         if(node.nodeValue != null) {
             prettyContent += node.nodeValue;
         } else {
             var nd = getIndent(indent);
             prettyContent += nd + getContent(node, true);
             if(node.childNodes != null && node.childNodes.length > 0) {
                 for (var i = 0; i < node.childNodes.length; ++i) {
                   printIndented(node.childNodes[i], indent+2);
                 }
                 if(node.childNodes[0].nodeValue == null ||
                     trim(node.childNodes[0].nodeValue) == "")
                    prettyContent += nd + getContent(node, false);
                 else
                    prettyContent += getContent(node, false);
             }
         }
       }
       
       function getContent(/*Node*/ n, start) {
         var c = '';
         if(n.nodeValue == null) {//DOM Elements only
            if(n.nodeName == '#document') {
                if(start)
                    c += '&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;    ';
            } else {
                if(start) {
                    c += '&lt;'+n.nodeName;
                    if(n.attributes != null && n.attributes.length > 0) {
                        for (var i = 0; i < n.attributes.length; ++i) {
                           var attr = n.attributes[i];
                           c += ' ' + attr.nodeName + '="' + attr.nodeValue+'"';
                        }
                    }
                    if(n.childNodes != null && n.childNodes.length > 0)
                        c += '&gt;';
                    else
                        c += '/&gt;';
                } else {
                    c += '&lt;/'+n.nodeName+'&gt;';
                }
            }
         } else {
            if(start)
                c += n.nodeValue;
         }
         return c;
       }

       function getIndent(/*int*/ indent) {
         var s = "";
         if(indent < 0)
             return s;
         while (indent) {
           s+=' &nbsp;';
           --indent; 
         }
         return "\n<br/>"+s;
       }
       
       function breakLine(line, len, indent) {
         var c = breakLine2(line, len, indent);
         return c;
       }
       
       function breakLine2(line, len, indent) {
         var c = line;
         if(indent.length + c.length > 100) {
             var len2 = 55;
             c = c.substring(0, len2) + indent + '&nbsp;&nbsp;&nbsp;' + breakLine2(c.substring(len2), len, indent);
         }
         return c;
       }
       
       function trim(str) {
         if(!str || typeof str != 'string')
             return null;
         return str.replace(/^[\s]+/,'').replace(/[\s]+$/,'').replace(/[\s]{2,}/,' ');
       }
    },

    getTab : function (id, actived) {
        var name = '';
        for(var i in this.viewIds) {
            var vid = this.viewIds[i]['id'];
            if(id == vid) {
                name = this.viewIds[i]['name'];
                break;
            }
        }
        if(actived)
            return '<td class="Tab1TblSelTd_sun4"><div title="MSG_TEST_RESBEANS_CurrentSelection" class="Tab1SelTxtNew_sun4"><a name="selectedTabAnchor" id="tab'+id+'"/>'+name+'</div></td>';
        else {
            if(id == '')
                return '<td style="visibility: hidden;"><a href="javascript:ts.showViews(\''+id+'\')" class="Tab1Lnk_sun4" id="tab'+id+'">'+name+'</a></td>';
            else
                return '<td><a href="javascript:ts.showViews(\''+id+'\')" class="Tab1Lnk_sun4" id="tab'+id+'">'+name+'</a></td>';
        }
    },
    
    getHeaderAsTable : function (xmlHttpReq) { 
        var header = xmlHttpReq.getAllResponseHeaders();
        var colNames = new Array()
        colNames[0] = "MSG_TEST_RESBEANS_HeaderName"
        colNames[1] = "MSG_TEST_RESBEANS_HeaderValue"
        var colSizes = new Array()
        colSizes[0] = ""
        colSizes[1] = ""
        var rows = header.split('\r\n');
        if(rows.length == 1)
            rows = header.split('\n');
        var count = 0;
        var str2 = '';
        for(var i=0;i<rows.length;i++) {
            var index = rows[i].indexOf(':');
            var name = rows[i].substring(0, index);
            if(name == '')
                continue;
            count++;
            var val = rows[i].substring(index+1);
            str2 += '<tr><th align="left" scope="row" class="TblTdLyt_sun4" ><span id="j_id9"><span class="">'+name+'</span></span></th>';    
            str2 += '<td align="left" class="TblTdLyt_sun4" ><span id="j_id10"><span class="">'+val+'</span></span></td></tr>';
        }    
        var req = this.currentValidUrl;
        if(req.indexOf('?') > 0)
            req = req.substring(0, req.indexOf('?'));
        var str = '<table width="100%" cellspacing="0" cellpadding="0" border="0" class="Tbl_sun4">'+
                    '<caption class="TblTtlTxt_sun4">'+this.getDisplayUri(req)+' ('+count+')</caption>';
        str += '<tbody><tr>';
        for (i=0;i<colNames.length;i++) {
            str += '<th align="left" scope="col" class="TblColHdr_sun4"><span class="TblHdrTxt_sun4">'+colNames[i]+'</span></th>';
        }
        str += "</tr>";
        str += str2;
        str += "</tbody></table>";
        return str;
    },

    getContainerTable : function (content) {
        if(content == null)
            return null;
        var ret = null;    
        var container = null;
        try {
            if(content.indexOf("<?xml ") != -1) {
                var doc2 = this.xhr.loadXml(content);
                if(doc2 != null && doc2.documentElement.nodeName == 'parsererror')
                    return null;
                container=doc2.documentElement;
                if(container == null || container.nodeName == 'html')
                    return null;
            }
            var colNames = new Array()
            colNames[0] = "ID"
            colNames[1] = "URI"
            var colSizes = new Array()
            colSizes[0] = ""
            colSizes[1] = ""
            var str2 = null;
            if(container != null)
                str2 = this.findUriFromXml(container);
            else
                str2 = this.findUriFromContent(content);
            if(str2 == null || str2 == '')
                return null;
            var req = this.currentValidUrl;
            if(req.indexOf('?') > 0)
                req = req.substring(0, req.indexOf('?'));
            var str = '<table width="100%" cellspacing="0" cellpadding="0" border="0" class="Tbl_sun4">'+
                    '<caption class="TblTtlTxt_sun4">'+this.getDisplayUri(req)+' ('+this.tcCount+')</caption>';
            str += '<tbody><tr>';
            for (i=0;i<colNames.length;i++) {
                str += '<th align="left" scope="col" class="TblColHdr_sun4"><span class="TblHdrTxt_sun4">'+colNames[i]+'</span></th>';
            }
            str += "</tr>";
            str += str2;
            str += "</tbody></table>";
            ret = str;
        } catch(e) {
            ts.debug('getContainerTable() err name: [' + e.name + '] message: [' + e.message+"]");
            return null;
        }

        return ret;
    },
    
    findUriFromXml : function (container) {
        this.tcStr = '';
        this.tcCount = 0;
        this.getChildUriFromXml(container);
        return this.tcStr;
    },

    getChildUriFromXml : function (refChild) {
        if(refChild == null)
            return;
        var subChilds = refChild.childNodes;
        if(subChilds == null || subChilds.length == 0)
            return;
        var j = 0;
        for(j=0;j<subChilds.length;j++) {
            var subChild = subChilds[j];            
            if(subChild.nodeValue == null) {//DOM Elements only
                if(subChild.attributes != null && subChild.attributes.length > 0 && 
                    subChild.attributes.getNamedItem('uri') != null) {
                    this.tcStr += this.createRowForUriFromXml(subChild);
                }
                this.getChildUriFromXml(subChild);
            }
        }
    },
    
    createRowForUriFromXml : function (refChild) {
        var str = '';    
        var id = '-';
        if(refChild.childNodes != null && refChild.childNodes.length > 0 && 
                refChild.childNodes[0].childNodes != null && 
                    refChild.childNodes[0].childNodes.length > 0) {
            id = refChild.childNodes[0].childNodes[0].nodeValue;
        }
        if(id == null)
            id = '-';
        str += '<tr><th align="left" scope="row" class="TblTdLyt_sun4" ><span id="j_id9"><span class="">'+(++this.tcCount)+'</span></span></th>';
        var uri = refChild.attributes.getNamedItem('uri').nodeValue;
        str += '<td align="left" class="TblTdLyt_sun4" ><span id="j_id10"><span class="">';
        var disp = this.getDisplayUri(uri);
        var subsUri = this.getSubstitutedUri(uri);
        str += "<a id='"+uri+"' href=javascript:ts.doShowContent('"+uri+"') >"+this.getDisplayURL(disp, 70)+"</a>";
        str += "<br/>(<a href='"+subsUri+"' target='_blank'><span>"+this.getDisplayURL(uri, 70)+"</span></a>)";
        str += '</span></span></td></tr>';
        return str;
    },
    
    getSubstitutedUri : function (uri) {
        var subsUri = uri;
        var ndx = subsUri.indexOf('{');
        if(ndx > 0)
            subsUri = subsUri.substring(0, ndx) + '1' + subsUri.substring(subsUri.indexOf('}', ndx)+1);
        return subsUri;
    },
    
    findUriFromContent : function (content) {
        if(content == null || content == '')
            return '';
        var c = content.replace(/\\\//g,"/");   
        var uris = c.split('\"');
        var str = '';
        var cvl = this.currentValidUrl.indexOf("?");
        if(cvl == -1)
            cvl = this.currentValidUrl.length;
        this.tcCount = 0;
        for(var i=0;i<uris.length;i++) {
            var uri = uris[i];
            if(uri.indexOf(baseURL) == 0 && uri.length > cvl) {
                str += '<tr><th align="left" scope="row" class="TblTdLyt_sun4" ><span id="j_id9"><span class="">'+(++this.tcCount)+'</span></span></th>';    
                str += '<td align="left" class="TblTdLyt_sun4" ><span id="j_id10"><span class="">';
                var disp = this.getDisplayUri(uri);
                str += "<a id='"+uri+"' href=javascript:ts.doShowContent('"+uri+"') >"+this.getDisplayURL(disp, 70)+"</a>";
                str += "<br/>(<a href='"+uri+"' target='_blank'><span>"+this.getDisplayURL(uri, 70)+"</span></a>)";
                str += '</span></span></td></tr>';           
            }            
        }
        return str;
    },
    
    getDisplayUri : function (uri) {
        var disp = uri;
        if(disp.length > baseURL.length)
            disp = disp.substring(baseURL.length);
        return disp;
    },
    
    getDisplayURL : function (url, len) {
        return url.substring(0, len);
    },
    
    updatepage : function (id, str){
        var n = document.getElementById(id);
        if(n != null)
            n.innerHTML = str;
    },
    
    getFormMimeType : function () {
        var resource = document.getElementById('mimeType');
        if(resource != null)
            return this.wdr.getMimeType(resource.value);
        else
            return this.wdr.getDefaultMime();
    },
    
    getFormMethod : function () {
        var resource = document.getElementById('methodName');
        if(resource != null)
            return this.wdr.getMethod(resource.value);
        else
            return this.wdr.getDefaultMethod();
    },

    getItemString : function (name, uri){
        var itemString = '<img src="cc.gif" border="0">';
        itemString += '<img src="item.gif" border="0">';
        itemString += '<a href="javascript:ts.doShowContent(\''+uri+'\')">';
        itemString += name;
        itemString += '</a><br>';
        return itemString;
    },

    toggleCategory : function (img){
        var ImageNode = document.getElementById('I' + img);
        var ImageNode1 = document.getElementById('I1' + img);
        if(ImageNode1.src.indexOf('cg.gif')>-1) {
            ImageNode.src = ts.expand.src;
            ImageNode1.src = ts.og.src;
        } else {
            ImageNode.src = ts.collapse.src;
            ImageNode1.src = ts.cg.src;
        }
    },
    
    debug : function(message) {
        var dbgComp = document.getElementById("dbgComp");
        if(dbgComp == null) {
            dbgComp = document.createElement("div");
            dbgComp.setAttribute("id", "dbgComp");
            dbgComp.style.border = "#2574B7 1px solid";
            dbgComp.style.font = "12pt/14pt sans-serif";
            var br = document.createElement("div");
            document.getElementsByTagName("body")[0].appendChild(br);
            br.innerHTML = '<br/><br/><br/>';
            document.getElementsByTagName("body")[0].appendChild(dbgComp);
            if((typeof rjsConfig!="undefined") && rjsConfig.isDebug) {
                dbgComp.style.display = "";
            } else {
                dbgComp.style.display = "none";
            }
            var tab = 'width: 20px; border-right: #2574B7 1px solid; border-top: #2574B7 1px solid; border-left: #2574B7 1px solid; border-bottom: #2574B7 1px solid; color: #000000; text-align: center;';
            var addActionStr = '<div style="'+tab+'"><a style="text-decoration: none" href="javascript:ts.closeDebug()"><span style="color: red">X</span></a></div>';        
            dbgComp.innerHTML = '<table><tr><td><span style="color: blue">MSG_TEST_RESBEANS_DebugWindow</span></td><td>'+addActionStr + '</td></tr></table><br/>';
        }
        var s = dbgComp.innerHTML;
        var now = new Date();
        var dateStr = now.getHours()+':'+now.getMinutes()+':'+now.getSeconds();
        dbgComp.innerHTML = s + '<span style="color: red">rest debug('+dateStr+'): </span>' + message + "<br/>";
    },
    
    closeDebug : function() {
        var dbgComp = document.getElementById("dbgComp");
        if(dbgComp != null) {
            dbgComp.style.display = "none";
            dbgComp.innerHTML = '';
        }
    },
    
    storeRequestHeaders: function() {
        if (localStorage) {
            ts._storeHeader('req_hdr_1');
            ts._storeHeader('req_hdr_2');
            ts._storeHeader('req_hdr_3');
            ts._storeHeader('req_hdr_4');
            ts._storeHeader('req_hdr_5');
            ts._storeHeader('req_hdr_val_1');
            ts._storeHeader('req_hdr_val_2');
            ts._storeHeader('req_hdr_val_3');
            ts._storeHeader('req_hdr_val_4');
            ts._storeHeader('req_hdr_val_5');
        }
    },
    
    _storeHeader: function(header) {
        var hdr = document.getElementById(header).value.trim();
        if (hdr.length === 0) {
            localStorage.removeItem(header);
        } else {
            localStorage.setItem(header, hdr);
        }
    },

    restoreRequestHeaders: function() {
        if (localStorage) {
            ts._restoreHeader('req_hdr_1');
            ts._restoreHeader('req_hdr_2');
            ts._restoreHeader('req_hdr_3');
            ts._restoreHeader('req_hdr_4');
            ts._restoreHeader('req_hdr_5');
            ts._restoreHeader('req_hdr_val_1');
            ts._restoreHeader('req_hdr_val_2');
            ts._restoreHeader('req_hdr_val_3');
            ts._restoreHeader('req_hdr_val_4');
            ts._restoreHeader('req_hdr_val_5');
        }
    },

    _restoreHeader: function(header) {
        var value = localStorage.getItem(header);
        if (value) {
            document.getElementById(header).value = value;
        }
    },

    getRequestHeader: function(header) {
        var hdrEl = document.getElementById(header);
        if (hdrEl) {
            return hdrEl.value.trim();
        } else {
            if (localStorage) {
                var value = localStorage.getItem(header);
                return value ? value : '';
            } else {
                return '';
            }
        }
    }
}

function WADLParser() {
  this.wadlResources = [];
}

WADLParser.prototype = {
    getWadlDocument : function (wadlURL) {
        var wadlData = ts.xhr.get(wadlURL);
        if(wadlData == undefined || wadlData == "" || wadlData.indexOf("RequestFailed -->") != -1) {   
            var newUrl = prompt(ts.wadlErr, baseURL);
            if(newUrl != null && baseURL != newUrl) {
                baseURL = newUrl;
                ts.wadlURL = ts.concatPath(baseURL, "application.wadl");
                ts.initFromWadl();
            }
            return null;
        }
        var wDoc = ts.xhr.loadXml(wadlData);
        if(wDoc != null) { 
            var app=wDoc.documentElement;
            if(app != null) {
              var resources = ts.getElementsByTagName(app, 'resources')[0];
              var rChilds = ts.getElementsByTagName(resources, 'resource');
              if(rChilds != null && rChilds.length > 0) {
                var wDocArr = [];
                for(var i=0;i<rChilds.length;i++) {
                  var r = rChilds[i];
                  var base = resources.attributes.getNamedItem('base').nodeValue;
                  var path = r.attributes.getNamedItem('path').nodeValue;
                  wadlData = ts.xhr.options(ts.concatPath(base, path), 'application/vnd.sun.wadl+xml');
                  wDocArr.push(ts.xhr.loadXml(wadlData));
                }
                if(wDocArr.length > 0) {
                  wDoc = wDocArr[0];
                  resources = ts.getElementsByTagName(wDoc.documentElement, 'resources')[0];
                  for(var i=1;i<wDocArr.length;i++) {
                    var rs = ts.getElementsByTagName(wDocArr[i].documentElement, 'resources')[0];
                    rChilds = ts.getElementsByTagName(rs, 'resource');
                    if(rChilds != null && rChilds.length > 0) {
                        try{
                            var n = wDoc.importNode(rChilds[0], true);
                            resources.appendChild(n);
                        }
                        catch (e) {
                            resources.appendChild(rChilds[0]);
                        }
                    }
                  }
                }
              }
            }
        }
        return wDoc;
    },
    
    updateMenu : function (wUrl, wDoc) {
        ts.setvisibility('main', 'inherit');
        ts.updatepage('subheader', '<br/><span class=MstLbl_sun4>WADL : </span><a class=MstLnk_sun4 href=\"'+wUrl+'\">'+wUrl+'</a>');
        if(wDoc != null) {                
            this.initTree(wDoc);
        }
    },
    
    initTree : function (wadlDoc) {
        var myTree = this.createTree(wadlDoc);
        var treeString = myTree.toString();
        ts.updatepage('leftSidebar', treeString);
        this.showTopCategory();
    },
    
    refreshTree : function (wadlDoc) {
        var myTree = this.createTree(wadlDoc);
        var treeString = myTree.toString();                            
        ts.updatepage('leftSidebar', treeString);
    },
    
    createTree : function (wadlDoc) {
        var app=wadlDoc.documentElement;
        var myTree = new tree();
        var rs;
        if(app != null) {
            rs = ts.getElementsByTagName(app, 'resources')[0];
            ts.projectName = this.projectNameFromUrl(rs.attributes.getNamedItem('base').nodeValue);
            var resources = new category(rs, this.getTopCategoryId(), baseURL, ts.projectName);
            myTree.add(resources);
            this.createChildNodes(rs, resources);
        }
        return myTree;
    },
    
    projectNameFromUrl : function (url_) {
        var url = url_
        var begin = url.indexOf('/', 7);
        if(begin != -1)
            url = url.substring(begin, url.length);
        var index = url.indexOf('/', 1);
        if(url.length > 1 && index != -1)
            url = url.substring(1, index);
        return url;
    },

    createChildNodes : function (/*Node*/ node, parentCat) {
       if(node.childNodes != null && node.childNodes.length > 0) {
          for (var i = 0; i < node.childNodes.length; ++i) {
            var n = node.childNodes[i];
            if(ts.wdr.isResource(n)) {
                var pathVal = unescape(ts.wdr.getNormailizedPath(n));
                n.attributes.getNamedItem('path').nodeValue = pathVal;
                ts.topUrls.push(pathVal);
            }
          } 
       }
       createChildNodes2(node, parentCat);

       function createChildNodes2(/*Node*/ node, parentCat) {
         if(node.nodeValue == null){
             if(node.childNodes != null && node.childNodes.length > 0) {
                 for (var i = 0; i < node.childNodes.length; ++i) {
                    var ch = node.childNodes[i];
                    if(ts.wdr.isResource(ch)) {
                        var n = createNode(ch, parentCat);
                        if(n != null) {
                          parentCat.add(n);
                          createChildNodes2(ch, n);
                        }
                    }
                 } 
             }
         }
       }
       
       function createNode(/*Node*/ n, parentCat) {  
         var pathVal = ts.wdr.getNormailizedPath(n);
         var pathElem = '';
         if (pathVal.substring(0,1) == '/' && pathVal.length > 1)
             pathElem = pathVal.substring(1);
         else
             pathElem = pathVal;
         var parentUri = parentCat.uri;
         var uri ='';
         if(parentUri.substring(parentUri.length-1) == '/')
             uri = parentCat.uri+pathElem;
         else
             uri = parentCat.uri+'/'+pathElem;
         if (uri.substring(uri.length-2) == '//')
             uri = uri.substring(0, uri.length-2)
         var cName = (pathVal == '/' ? '/' : ts.wdr.trimSeperator(pathVal));
         if(ts.wdr.hasResource(n)) {
            return new category(n, ts.wdr.getUniqueCategoryId(pathVal), uri, cName);
         } else {
            var methods = ts.getElementsByTagName(n, 'method');
            if(methods != null && methods.length > 0) {
                return new item(n, pathVal, uri, cName);
            } else {
                var n2 = ts.wdr.findResource(baseURL+pathVal);
                if(n2 == null) {
                    return null;
                } else {
                    if(ts.wdr.hasResource(n2)) {
                        //Stop recursion when inner resources have reference to static resource.
                        return null;
//                        var cat = new category(n, ts.wdr.getUniqueCategoryId(pathVal+'_1'), uri, cName);
//                        createChildNodes2(n2, cat);
//                        return cat;
                    } else {
                        return new item(n2, pathVal, uri, cName);
                    }
                }
            }
         }
       }
    },
    
    getNormailizedPath : function (n) {
        var path = n.attributes.getNamedItem('path');
        var pathVal = path.nodeValue;
        return this.prependSeperator(pathVal);
    },
    
   isResource : function (/*Node*/ n) {  
        if(n.nodeValue == null && n.nodeName == 'resource') {
            return true;
        }
        return false;
   },

   hasResource : function (/*Node*/ node) {
     if(node.nodeValue == null){
         if(node.childNodes != null && node.childNodes.length > 0) {
             for (var i = 0; i < node.childNodes.length; ++i) {
                var ch = node.childNodes[i];
                if(this.isResource(ch) && this.hasMethod(ch)) {
                    return true;
                }
             } 
         }
     }
     return false;
   },
   
   hasMethod : function (/*Node*/ n) {
       var methods = ts.getElementsByTagName(n, 'method');
       if(methods != null && methods.length > 0) {
           return true;
       }
       return false;
   },
    
    prependSeperator : function (cName) {
        if(cName != null) {
            if(cName.substring(0, 1) != '/')
                cName = '/' + cName;
        }
        return cName;
    },
    
    trimSeperator : function (cName) {
        if(cName != null) {
            if(cName.substring(0, 1) == '/')
                cName = cName.substring(1);
            if(cName.substring(cName.length-1, cName.length) == '/')
                cName = cName.substring(0, cName.length-1);
        }
        return cName;
    },
    
    evaluateWADLUpdate : function (uri, content) {
        var str = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><root>';
        try {
          var resources = this.getAllResourcesFromWadl(content);
          if(resources != null) {
              for (i=0;i<resources.length;i++) {
                  var path = ts.getResourcePath(resources[i]);
                  var url = '';
                  if(baseURL.substring(baseURL.length-1, baseURL.length) != '/' && path.substring(0, 1) != '/')
                      url = baseURL + '/' + path;
                  else
                      url = baseURL + path;
                  str += '<node uri="'+url+'"/>';
              }
          }
        }
        catch (e) {
          ts.debug('evaluateWADLUpdate() err name: [' + e.name + '] message: [' + e.message+']');
        }
        str += '</root>';
        return str;
    },
    
        /*
        <?xml version="1.0" encoding="UTF-8"?>
           <application xmlns="http://research.sun.com/wadl/2006/10">
               <resources base="http://localhost:8080/NewCustomerDB/resources/">
                   <resource path="/discountCodes/"> 
        */
    getAllResourcesFromWadl : function (content) {
        try {
            if(content.indexOf("<?xml ") != -1) {
                var doc2 = ts.xhr.loadXml(content);
                if(doc2 != null && doc2.documentElement.nodeName == 'parsererror')
                    return null;
                container=doc2.documentElement;
                if(container == null || container.nodeName == 'html')
                    return null;
            }
            if(container != null)
                return this.findResourcesFromWadl(container);
        } catch(e) {
            ts.debug('getAllResourcesFromWadl() err name: [' + e.name + '] message: [' + e.message+"]");
        }

        return null;
    },
    
    findResourcesFromWadl : function (container) {
        this.wadlResources = [];
        this.findResourcesFromWadlRecursively(container);
        return this.wadlResources;
    },

    findResourcesFromWadlRecursively : function (refChild) {
        if(refChild == null)
            return;
        var subChilds = refChild.childNodes;
        if(subChilds == null || subChilds.length == 0)
            return;
        var j = 0;
        for(j=0;j<subChilds.length;j++) {
            var subChild = subChilds[j];            
            if(subChild.nodeValue == null) {//DOM Elements only
                if(subChild.nodeName == 'resource' &&
                    subChild.attributes != null && subChild.attributes.length > 0 && 
                      subChild.attributes.getNamedItem('path') != null) {
                    this.wadlResources.push(subChild);
                }
                if(subChild.nodeName == 'resource' ||
                    subChild.nodeName == 'resources') {
                  this.findResourcesFromWadlRecursively(subChild);
                }
            }
        }
    },
    
    showTopCategory : function (){
        this.showCategory(this.getTopCategoryId());
    },
    
    showCategory : function (category){
        var categoryChildNodes = document.getElementById(category).style;
        if(categoryChildNodes.display=="block")
            categoryChildNodes.display="none";
        else
            categoryChildNodes.display="block";
        ts.toggleCategory(category);
    },
    
    getTopCategoryId : function (){
        return 'resources' + '_0';
    },

    getUniqueCategoryId : function (category){
        return category + '_' + ts.allcat.length;
    },
    
    updateTree : function (catId){
        if(catId == this.getTopCategoryId()) {//return if top level
            this.showTopCategory();
            return;
        }
        var myTree = this.createTree(ts.wadlDoc);
        ts.updatepage('leftSidebar', myTree.toString());
        childrenContent = '';
        this.getChildren(catId);
        currentCategory = catId;
        setTimeout("ts.wdr.refreshCategory()",1000);
    },

    refreshCategory : function(){
        var catId = currentCategory;
        ts.updatepage(catId, childrenContent);
        this.showTopCategory();
        this.showCategory(catId);
    },

    //get mediatype from method
    getMediaType : function (m) {
        var mName = m.attributes.getNamedItem("name").nodeValue;
        var request = ts.getElementsByTagName(m, 'request');
        var response = ts.getElementsByTagName(m, 'response');
        var mediaType = '';
        var io = request;
        if(mName == 'GET')
            io = response;
        if(io != null && io.length > 0) {
            var rep = ts.getElementsByTagName(io[0], 'representation');
            if(rep != null) {    
                for(var i=0;i<rep.length;i++) {
                    if(rep[i].attributes.length > 0) {
                        var att = rep[i].attributes.getNamedItem('mediaType');
                        if(att != null)
                            mediaType += att.nodeValue + ',';
                    }
                }
            }
        }
        if(mediaType.length > 1)
            mediaType = mediaType.substring(0, mediaType.length-1);
        return mediaType;
    },
    
    findResource : function (uri) {
        var r = null;
        var len = baseURL.length;
        if(uri.length > len) {
            var u = uri.substring(len, uri.length);
            var ri = this.lookupIndex(u);
            if(ri == -1) {//look for reference resource
                var li = u.lastIndexOf('/');
                if(li != -1) {
                    var u2 = u.substring(0, li);
                    var li2 = u2.lastIndexOf('/');
                    u = u.substring(li2, u.length);
                    ri = this.lookupIndex(u);
                }
            }
            if(ri > -1) {
                var app1 = ts.wadlDoc.documentElement;
                var rs = ts.getElementsByTagName(app1, 'resources')[0];
                var rlist = rs.childNodes;
                if(rlist != null && rlist.length > 0) {
                    for(var i=0;i<rlist.length;i++) {
                        var rr = rlist[i];
                        if(rr.nodeValue == null && rr.nodeName == 'resource') {
                            var path = rr.attributes.getNamedItem('path');
                            if(ts.wdr.trimSeperator(path.nodeValue) == ts.wdr.trimSeperator(u)) {
                                return rr;
                            }
                        }
                    }
                }
            }
        }
        return r;
    },
    
    lookupIndex : function (u)
    {
        var ri = -1;
        for(var i=0;i<ts.topUrls.length;i++) {
            if(ts.topUrls[i] == u) {
                ri = i;
                break;
            }
        }
        return ri;
    },
    
    getMethodNameForDisplay : function (mName, mediaType) {
        if ('DELETE' === mName) {
            return mName;
        }
        var m = mName;
        if(!mediaType && (mName === 'PUT' || mName === 'POST')) {
            mediaType = getDefaultMime();
        }
        if(mediaType) {
            m += '(' + mediaType + ')';
        }
        return m;
    },
    
    getDefaultMime : function () {
        return "application/xml";
    },
    
    getDefaultMethod : function () {
        return "GET";
    },
    
    getMimeType : function (mime) {
        if(mime != null) {
            var i = mime.indexOf('(');
            if(i == -1) {
                if(mime == 'GET' || mime == 'POST' || mime == 'PUT' || mime == 'DELETE')
                    return getDefaultMime();
                else
                    return mime;
            } else
                return mime.substring(i+1, mime.length-1);
        } else
            return getDefaultMime();
    },
    
    getMethod : function (method) {
        if(method != null) {
            var i = method.indexOf('(');
            if(i == -1)
                return method;
            else
                return method.substring(0, i);
        } else
            return this.getDefaultMethod();
    },

    getChildren : function (uri) {
        var content = ts.xhr.get(baseURL+uri, this.getDefaultMime());
        this.getChildrenContent(content);
    },

    getChildrenContent : function (content) {
        if(content != -1) {
            var ret = this.getChildrenAsItems(content);
            childrenContent = ret;
        } else {
            childrenContent = '';
        }
    },

    getChildrenAsItems : function (xmlStr) {
        var ret = xmlStr;  
        if(ret == null)
            return '';
        var doc2 = null;
        try {
            doc2 = ts.xhr.loadXml(ret);
        } catch(e) { 
            ts.debug('getChildrenAsItems()->loadXml() err name: [' + e.name + '] message: [' + e.message+"]");
            return null;
        }
        if(doc2 != null && doc2.documentElement.nodeName != 'parsererror') {
            try {
                var container=doc2.documentElement;
                if(container == null || container.nodeName == 'html')
                    return ret;
                var str = "";
                var refs = container.childNodes;
                var count = 0;
                for(var i=0;i<refs.length;i++) {
                    var refsChild = refs[i];
                    if(refsChild.nodeValue == null) {//DOM Elements only
                        var ref = refsChild;             
                        var refChilds = ref.childNodes;
                        for(var j=0;j<refChilds.length;j++) {
                            var refChild = refChilds[j];
                            if(refChild.nodeValue == null) {//DOM Elements only
                                var id = refChild;
                                if(ref.attributes != null && ref.attributes.length > 0 && 
                                        ref.attributes.getNamedItem('uri') != null) {
                                    var uri = ref.attributes.getNamedItem('uri').nodeValue;
                                    var idval = id.childNodes[0].nodeValue;
                                    var disp = this.getDisplayUri(uri);
                                    str += this.getItemString(idval, uri);
                                }
                            }
                        }
                        count++;
                    }
                }
                ret = str;
            } catch(e) {
                ts.debug('getChildrenAsItems() err name: [' + e.name + '] message: [' + e.message+"]");
                return null;
            }
        }
        return ret;
    }
}

function tree(){
    this.categories = [];
}

tree.prototype = {
    add : function (category){
        this.categories[this.categories.length] = category;
    },

    list : function (){
        return this.categories;
    },

    toString : function (){
        var treeString = '';
        for(var i=0;i<this.categories.length;i++) {
            treeString += this.categories[i].write();
        }
        return treeString;
    }
}

function category(resource, id, uri, text){
    this.r = resource;
    this.id = id;
    this.text = text;

    this.items = [];
    this.ndx = ts.allcat.length;
    ts.allcat.push(this);
    this.uri = uri;
}

category.prototype = {
    write : function (){
        var categoryString = '<span id="nodeSel' + this.id + '" class="category TreeContent_sun4"';
        if(this.uri != baseURL) {
            categoryString += '><img src="cg.gif" id="I1' + this.id + '" onClick="ts.wdr.showCategory(\'' + this.id + '\')">';
            categoryString += '<img src="collapse.gif" id="I' + this.id + '">';
            categoryString += "<div class='item2'><a class=Hyp_sun4 href=javascript:ts.doShowContentForId('"+this.ndx+"') >"+ this.text + "</a></div>";
        } else {
            categoryString += '><img src="cg.gif" id="I1' + this.id + '" onClick="ts.wdr.updateTree(\'' + this.id + '\')">';
            categoryString += '<img src="app.gif" id="I' + this.id + '">';
            categoryString += "<div class='item2'><a class=Hyp_sun4 href=javascript:ts.clearAll() >"+ this.text + "</a></div>";
        }
        categoryString += '</span>';
        categoryString += '<span class="item" id="';
        categoryString += this.id + '">';
        var numitems = this.items.length;
        for(var j=0;j<numitems;j++)
            categoryString += this.items[j].write();
        categoryString += '</span>';
        return categoryString;
    },

    add : function (item){
        this.items[this.items.length] = item;
    },

    list : function (){
        return this.items;
    }
}

function item(resource, id, uri, text, ndx){
    this.r = resource;
    this.id = id;
    this.text = text;
    this.ndx = ts.allcat.length;
    ts.allcat.push(this);
    this.uri = uri;//baseURL + this.id;
}

item.prototype = {
    write : function (){
        var itemString = '<span class="category TreeContent_sun4"><img src="cc.gif" border="0">';

        itemString += '<img src="item.gif" border="0">';
        if(this.uri != null)
            itemString += "<div class=item1><a class=Hyp_sun4 href=javascript:ts.doShowContentForId('"+this.ndx+"') >"+ this.text + "</a></div>";
        else
            itemString += this.text;
        itemString += '</span>';
        return itemString;
    }
}

function XHR() {
}
 
XHR.prototype = {
    getHttpRequest : function() {
        var xmlHttpReq;
        try
        {    // Firefox, Opera 8.0+, Safari, IE7.0+
            xmlHttpReq=new XMLHttpRequest();
        }
        catch (e)
        {    // Internet Explorer 6.0+, 5.0+
            try
            {
                xmlHttpReq=new ActiveXObject("Msxml2.XMLHTTP");
            }
            catch (e)
            {
                try
                {
                    xmlHttpReq=new ActiveXObject("Microsoft.XMLHTTP");
                }
                catch (e)
                {
                    ts.debug("MSG_TEST_RESBEANS_No_AJAX");
                }
            }
        }
        return xmlHttpReq;
     },

    connect : function (method, url, mimeType, paramLen, async) {
        var xmlHttpReq = this.getHttpRequest();
        if(xmlHttpReq == null) {
            ts.debug('Error: Cannot create XMLHttpRequest');
            return null;
        }
        try {
            netscape.security.PrivilegeManager.enablePrivilege ("UniversalBrowserRead");
        } catch (e) {
            //ts.debug('connect(): Permission UniversalBrowserRead denied. err name: [' + e.name + '] message: [' + e.message+']');
        }
        try {
            xmlHttpReq.open(method, url, async);
        } catch( e ) {
            ts.debug('connect(): Error: XMLHttpRequest.open failed for: '+url+' Error name: '+e.name+' Error message: '+e.message);
            return null;
        }
        if (mimeType != null) {
            if(method == 'GET' || method == 'OPTIONS') {
                //ts.debug("setting GET accept: "+mimeType);
                xmlHttpReq.setRequestHeader('Accept', mimeType);
            } else if(method == 'POST' || method == 'PUT'){
                //ts.debug("setting content-type: "+mimeType);
                //Send the proper header information along with the request
                xmlHttpReq.setRequestHeader("Content-Type", mimeType);
                xmlHttpReq.setRequestHeader("Content-Length", paramLen);
                xmlHttpReq.setRequestHeader("Connection", "close");
            }
        }
        
        if (toggleHeadersBlock && toggleHeadersBlock.cache) {
            var req_hdr = ts.getRequestHeader('req_hdr_1');
            if (req_hdr.length > 0) {
                xmlHttpReq.setRequestHeader(req_hdr, ts.getRequestHeader('req_hdr_val_1'));
            }
            req_hdr = ts.getRequestHeader('req_hdr_2');
            if (req_hdr.length > 0) {
                xmlHttpReq.setRequestHeader(req_hdr, ts.getRequestHeader('req_hdr_val_2'));
            }
            req_hdr = ts.getRequestHeader('req_hdr_3');
            if (req_hdr.length > 0) {
                xmlHttpReq.setRequestHeader(req_hdr, ts.getRequestHeader('req_hdr_val_3'));
            }
            req_hdr = ts.getRequestHeader('req_hdr_4');
            if (req_hdr.length > 0) {
                xmlHttpReq.setRequestHeader(req_hdr, ts.getRequestHeader('req_hdr_val_4'));
            }
            req_hdr = ts.getRequestHeader('req_hdr_5');
            if (req_hdr.length > 0) {
                xmlHttpReq.setRequestHeader(req_hdr, ts.getRequestHeader('req_hdr_val_5'));
            }
        }
        
        //For cache control on IE7
        if (navigator.userAgent.toLowerCase().indexOf('msie') >= 0) {
            xmlHttpReq.setRequestHeader("Cache-Control", "no-cache");
            xmlHttpReq.setRequestHeader("Pragma", "no-cache");
            xmlHttpReq.setRequestHeader("Expires", "-1");
        }
        
        ts.currentValidUrl = url;
        return xmlHttpReq;
    },

    httpRequest : function(method, url, mime, content, monitor) {
        var len = 0;
        if(content != undefined && content != null)
            len = content.length;
        var xmlHttpReq = this.connect(method, url, mime, len, false);
        try {
            xmlHttpReq.send(content);
            if (this.isResponseReady(method, xmlHttpReq, content, monitor)) {
              var rtext = xmlHttpReq.responseText;
              if ( (rtext== undefined || rtext == '' ) && (method=='POST' || method=='PUT' || method=='DELETE')){
                  return 'MSG_TEST_RESBEANS_NoContent';
              }
              if(rtext == undefined || rtext == '' || rtext.indexOf('HTTP Status') != -1) {
                  var err = method+' MSG_TEST_RESBEANS_RequestFailed RequestFailed --> MSG_TEST_RESBEANS_Status: (' 
                  	   + xmlHttpReq.status+')\n<br/>'+
                      'MSG_TEST_RESBEANS_Response: {<br/>' + xmlHttpReq.responseText + "<br/>}";
                  ts.debug('Failed XHR('+method+', '+url+'): '+err);
                  return err;
              }
              return rtext;           
            }
        } catch( e ) {
           ts.debug('httpRetrieve(): Caught Exception; name: [' + e.name + '] message: [' + e.message+']');
        }
        return '-1';
    },
    
    get : function(url, mime) {
        return this.httpRequest('GET', url, mime, null, true);
    },
    
    options : function(url, mime) {
        return this.httpRequest('OPTIONS', url, mime, null, false);
    },

    post : function(url, mime, content) {
        return this.httpRequest('POST', url, mime, content, true);
    },

    put : function(url, mime, content) {
        return this.httpRequest('PUT', url, mime, content, true);
    },

    delete_ : function(url) {
        return this.httpRequest('DELETE', url, 'application/xml', '', true);
    },
    
    loadXml : function(xmlStr) {
        var doc2;
        // code for IE
        if (window.ActiveXObject)
        {
            doc2=new ActiveXObject("Microsoft.XMLDOM");
            doc2.async="false";
            doc2.loadXML(xmlStr);
        }
        // code for Mozilla, Firefox, Opera, etc.
        else
        {
            var parser=new DOMParser();
            doc2=parser.parseFromString(xmlStr,ts.wdr.getDefaultMime());
        }
        return doc2;
    },
    
    isResponseReady : function (method, xmlHttpReq, param, monitor) {
        if (xmlHttpReq.readyState == 4) {
            if(monitor) {
                ts.currentXmlHttpReq = xmlHttpReq;
                ts.monitor(method, xmlHttpReq, param);
            }
            return true;
        } else
            return false;
    }
}
