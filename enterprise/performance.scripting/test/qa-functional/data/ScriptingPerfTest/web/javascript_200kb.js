function newInst(parentObject,instName,instConstructor){
    parentObject[instName]=new instConstructor(instName)
};

function gEl(ids){
    return document.getElementById(ids)
};

function cEl(name){
    return document.createElement(name)
};

function cTxt(cont){
    return document.createTextNode(cont)
};

function clear(){
    if(browser.klient=='op'){
        return
    }
    engine.map.clear(1);
    dom.clearEvents();
    if(typeof(engine.controls.ruler)!='undefined'){
        if(browser.klient=='geck'){
            engine.controls.ruler.end()
        }
    }
    for(i in dom){
        dom[i]=null
    }
    for(i in engine){
        engine[i]=null
    }
    for(i in resultMaker){
        resultMaker[i]=null
    }
    for(i in mainMenu){
        mainMenu[i]=null
    }
    for(i in subMenu){
        subMenu[i]=null
    }
    for(i in config){
        config[i]=null
    }
    if(result){
        for(i in result){
            result[i]=null
        }
        result=null
    }
    config=null;
    subMenu=null;
    mainMenu=null;
    resultMaker=null;
    dom=null;
    engine=null
};

function serialize(obj){
    if(arguments.length>1)var serString=arguments[1];
    var startMark="{";
    var endMark="},";
    var endMarkFin="}";
    var varSep=',';
    var sep=':';
    var varDef='';
    if(typeof(serString)!="undefined")var serString=serString+startMark;
    else{
        var serString='';
        var serString=serString+startMark
    }
    for(i in obj){
        if(typeof(obj[i])!='function'){
            var varName=i;
            var varValue=obj[i];
            if(typeof(obj[i])!='object'){
                if(typeof(obj[i])=='string'){
                    var re=new RegExp("\'",'gi');
                    var varValue="'"+varValue.replace(re,"\\'")+"'"
                }
                var varDef="'"+varName+"'"+sep+varValue+varSep;
                var serString=serString+varDef
            }
            else{
                var varDef=varName+sep;
                var serString=serString+varDef;
                var serString=serialize(varValue,serString)
            }
        }
    }
    if(arguments.length>1)var serString=serString+endMark;
    else var serString=serString+endMarkFin;
    return serString.replace(/,}/g,'}')
}

function unserialize(serialized_string){
    eval('var newvar='+serialized_string);
    return newvar
}

function evnt(nam){
    this.eventFolder=new Array();
    this.getEvent=function(){
        e=arguments[0];
        e=e?e:window.event;
        return e
    };
    this.getTarget=function(e){
        e=window[nam].getEvent(e);
        if(!e.currentTarget){
            e.currentTarget=e.srcElement
        }
        return e.currentTarget
    };
    this.addEvent=function(elm,eventType,fn,set){
        if(arguments[4]){
            var num=window[nam].eventFolder.length;
            window[nam].eventFolder[num]=new Object();
            window[nam].eventFolder[num].trg=elm;
            window[nam].eventFolder[num].typ=eventType;
            window[nam].eventFolder[num].action=fn;
            window[nam].eventFolder[num].bool=set
        }
        if(document.addEventListener){
            if(window.opera&&(elm==window)){
                elm=document
            }
            elm.addEventListener(eventType,fn,set)
        }
        else if(document.attachEvent){
            elm.attachEvent('on'+eventType,fn)
        }
    };
    this.clearEvents=function(){
        for(var i=0;i<window[nam].eventFolder.length;i++){
            try{
                dom.removeEvent(window[nam].eventFolder[i].trg,window[nam].eventFolder[i].typ,window[nam].eventFolder[i].action,window[nam].eventFolder[i].bool)
            }
            catch(e){
                
            }
            window[nam].eventFolder[i]=null
        }
        window[nam].eventFolder=null
    };
    this.removeEvent=function(elm,eventType,fn,set){
        if(document.removeEventListener){
            if(window.opera&&(elm==window)){
                elm=document
            }
            elm.removeEventListener(eventType,fn,set)
        }
        else if(document.detachEvent){
            elm.detachEvent('on'+eventType,fn)
        }
    };
    this.stopEvent=function(e){
        var e=window[nam].getEvent(e);
        if((browser.klient=='ie')&&(typeof(clickTracker)!='undefined')&&(e.type=='click')){
            clickTracker.logger()
        }
        if(e.stopPropagation){
            e.stopPropagation()
        }
        else{
            e.cancelBubble=true
        }
    };
    this.cancelDef=function(e){
        e=window[nam].getEvent(e);
        if(e.preventDefault){
            e.preventDefault()
        }
        else{
            e.returnValue=false
        }
    }

};

var browser={
    platform:'',klient:'',version:'',_getPlatform:function(){
        var txt=navigator.userAgent;
        if(txt.indexOf('X11')!=-1){
            return'lin'
        }
        else if(txt.indexOf('Mac')!=-1){
            return'mac'
        }
        else if(txt.indexOf('Win')!=-1){
            return'win'
        }
        else{
            return'oth'
        }
    }
    ,_getKlient:function(){
        var txt=navigator.userAgent;
        if(document.addEventListener&&!window.opera&&(txt.indexOf('KHTML')==-1)){
            return'geck'
        }
        else if(document.addEventListener&&window.opera){
            return'op'
        }
        else if(document.attachEvent&&!window.opera){
            return'ie'
        }
        else if(txt.indexOf('KHTML')!=-1){
            if(txt.indexOf('Safari')!=-1){
                return'saf'
            }
            else{
                return'kon'
            }
        }
        else{
            return'oth'
        }
    }
    ,_getVersion:function(){
        var txt=navigator.userAgent;
        if(browser.klient=='ie'){
            var st=txt.indexOf('MSIE');
            var ln=8;
            var num=parseFloat(txt.substr(st+5,3));
            if(num<5.5){
                return 1
            }
            else if(num<5.9){
                return 2
            }
            else if(num>5.9){
                return 3
            }
        }
        else if(browser.klient=='op'){
            var test=txt.indexOf('Opera');
            var ver=txt.substr(test+6,5);
            num=parseInt(ver,10)>7?1:0;
            return num
        }
        else if(browser.klient=='kon'){
            var st=txt.indexOf('KHTML/');
            var ln=6;
            var num=parseFloat(txt.substr(st+6,4));
            return num
        }
        else{
            return 0
        }
    }
    ,getBrowser:function(){
        browser.platform=browser._getPlatform();
        browser.klient=browser._getKlient();
        browser.version=browser._getVersion()
    }

};

newInst(window,'dom',evnt);
browser.getBrowser();
var decChar=new Array('%','>','<','°','"','ě','š','č','ř','ž','ý','á','í','é',' ','ó','ú','ů','ň','ď','ť','Á','Ó','É','Ú','Í','Ý','Ů','Ě','Š','Č','Ř','Ž','Ď','Ť','Ň');
var encChar=new Array('%25','%3E','%3C','%C2%B0','%22','%C4%9B','%C5%A1','%C4%8D','%C5%99','%C5%BE','%C3%BD','%C3%A1','%C3%AD','%C3%A9','%20','%C3%B3','%C3%BA','%C5%AF','%C5%88','%C4%8F','%C5%A5','%C3%81','%C3%93','%C3%89','%C3%9A','%C3%8D','%C3%9D','%C5%AE','%C4%9A','%C5%A0','%C4%8C','%C5%98','%C5%BD','%C4%8E','%C5%A4');
function myEncode(myTxt){
    for(var i=0;i<decChar.length;i++){
        if(myTxt.indexOf(decChar[i])!=-1){
            var vzor=new RegExp(decChar[i],"g");
            myTxt=myTxt.replace(vzor,encChar[i])
        }
    }
    return myTxt
};

function myDecode(myTxt){
    for(var i=0;i<encChar.length;i++){
        if(myTxt.indexOf(encChar[i])!=-1){
            var vzor=new RegExp(encChar[i],"g");
            myTxt=myTxt.replace(vzor,decChar[i])
        }
    }
    return myTxt
};

try{
    var a=encodeURI('žšč')
}

catch(e){
    encodeURI=myEncode;
    decodeURI=myDecode
};

function Main(objName){
    this.map=new Object();
    this.BPN=28;
    this.BPT=20;
    this.center={
        x:0,y:0
    };
    this.tileSize=0;
    this.firstStep=false;
    function getMain(){
        return window[objName]
    };
    function _getObject(name){
        obj=getMain();
        return obj[name]
    };
    this.init=function(){
        obj=getMain();
        this.dynamicRequest=new DynamicRequestObj();
        this.port=new PortObj(obj,'port');
        this.map=new MapObj(obj,'map');
        this.mpLayer=new LayersObj(obj,'mpLayer');
        this.status=new StatusObj(obj,'status');
        this.status.set('changeCenter',true);
        this.controls=new ControlsObj(obj,'controls');
        this.urlMaker=new UrlMakerObj(obj,'urlMaker');
        this.routeMaker=new RouteMakerObj(obj,'routeMaker');
        this.points=new PointsObj(obj,'points');
        this.userIcon=new UserIconObj(obj,'userIcon');
        this.cookie=new Cookie();
        this.repairCoord=new RepairCoord(obj,'repairCoord');
        if(userPoint){
            obj.userIcon.init()
        }
        obj.urlMaker.init();
        obj.routeMaker.init();
        if(config.mapLayers.active.running){
            this.points=new PointsObj(obj,'points');
            obj.points.init();
            this.vizitka=new VizitkaObj(obj,'vizitka');
            obj.vizitka.init()
        }
        this.firstSetting();
        obj.port.init();
        obj.controls.init();
        obj.map.init();
        obj.map.set();
        obj.mpLayer.init();
        obj.map.setLimit();
        obj.controls.zoom.setBar(engine.map.scale);
        obj.controls.zoomLimit=engine.controls.zoom.setLimit();
        obj.map.check();
        this.mapScale=new MapScaleObj(obj,'mapScale');
        this.mapNorth=new MapNorthObj(obj,'mapNorth');
        obj.mapNorth.init();
        obj.mapScale.init();
        obj.map.makeAuthorsInfo();
        obj.urlMaker.make();
        if(resultMaker&&result){
            resultMaker.init()
        }
        obj.controls.clickAction.init()
    };
    this.firstSetting=function(){
        obj=getMain();
        obj.map.el=gEl('mapa');
        obj.map.scale=config.map.zoom;
        obj.map.typ=config.map.type;
        obj.tileSize=config.tileSize;
        obj.map.mapServer=config.map.mapServer;
        obj.map.firmServer=config.map.firmServer;
        obj.map.apiServer=config.map.apiServer;
        obj.center.x=config.mapCenter.x;
        obj.center.y=config.mapCenter.y;
        obj.map.maxSize=config.map.maxSize;
        obj.map.limit.drift=config.mapLimit.drift;
        obj.controls.zoomAllow=config.mapControls.zoomAllow;
        obj.controls.mouseAllow=config.mapControls.mouse;
        obj.controls.kbdAllow=config.mapControls.keyboard;
        obj.controls.mapMove=config.mapCommands.move;
        obj.controls.mapCut=config.mapCommands.cut;
        obj.encoding=config.encoding
    };
    this.getDocSize=function(){
        var x=document.documentElement.clientWidth&&(browser.klient!='op')?document.documentElement.clientWidth:document.body.clientWidth;
        var y=document.documentElement.clientHeight&&(browser.klient!='op')?document.documentElement.clientHeight:document.body.clientHeight;
        if((browser.klient=='saf')||(browser.klient=='kon')){
            y=window.innerHeight
        }
        return{
            width:x,height:y
        }
    }
    ,this.getPosition=function(obj,dir){
        var pos=0;
        while(obj.offsetParent){
            pos=(dir=='top')?pos+obj.offsetTop:pos+obj.offsetLeft;
            obj=obj.offsetParent
        }
        return pos
    };
    this.getInBoxPosition=function(obj,refBox,dir){
        var pos=0;
        do{
            pos=(dir=='top')?pos+obj.offsetTop:pos+obj.offsetLeft;
            obj=obj.offsetParent
        }
        while(obj.offsetParent!=refBox);
        return pos
    };
    this.decToHex=function(param){
        var obj=getMain();
        var out=param<<obj.BPN-obj.map.scale;
        out=out==0?'0000000':out.toString(16);
        while(out.length<7){
            out='0'+out
        }
        return out
    };
    this.getNumToHex=function(num){
        var c=new Number(num);
        var out=c.toString(16);
        while(out.length<7){
            out='0'+out
        }
        return out
    };
    this.getPointInScale=function(point,dir){
        var obj=getMain();
        var param=parseInt(point,16)>>(obj.BPT-obj.map.scale);
        if(dir=='x'){
            param=param-obj.map.tileDrift.x
        }
        else{
            param=obj.map.height-(param-obj.map.tileDrift.y)
        }
        return param
    };
    this.scrollPos=function(){
        if(document.documentElement.scrollTop){
            var ox=document.documentElement.scrollLeft;
            var oy=document.documentElement.scrollTop
        }
        else if(document.body.scrollTop){
            var ox=document.body.scrollLeft;
            var oy=document.body.scrollTop
        }
        else{
            var ox=0;
            var oy=0
        }
        return{
            x:ox,y:oy
        }
    };
    this.getData=function(dataFold,dataURL){
        var obj=getMain();
        if(browser.klient=='op'){
            window.setTimeout(function(){
                obj.dynamicRequest.send(dataURL,dataFold)
            }
            ,100)
        }
        else{
            obj.dynamicRequest.send(dataURL,dataFold)
        }
    };
    this.reSet=function(resetData){
        var obj=getMain();
        if(!resetData){
            return
        }
        obj.controls.zoom.tempZoom=resetData.scale;
        resetData=null
    };
    this.activeItem=function(item,evnt,fnc){
        dom.addEvent(item,evnt,fnc,false,1);
        item.style.cursor=browser.klient!='ie'?'pointer':'hand'
    };
    this.deactiveItem=function(item,evnt,fnc){
        dom.removeEvent(item,evnt,fnc,false);
        item.style.cursor='default'
    };
    this.showItem=function(items){
        for(i=0;i<items.length;i++){
            items[i].style.display='block'
        }
    };
    this.setElPosition=function(nod,box){
        var obj=getMain();
        if(!box.style.width){
            box.style.width=box.offsetWidth+'px'
        }
        if(!box.style.height){
            box.style.height=box.offsetHeight+'px'
        }
        var port=engine.port.getSize();
        var x=nod.x-obj.getPosition(gEl('port'),'left')+obj.scrollPos()['x'];
        var y=nod.y-obj.getPosition(gEl('port'),'top')+obj.scrollPos()['y'];
        if(((nod.x+15)+parseInt(box.style.width))>=(port.width+2-0*obj.map.drift)){
            box.style.left=null;
            box.style.right=(port.width-nod.x+10)+'px'
        }
        else{
            box.style.right=null;
            box.style.left=(nod.x+10)+'px'
        }
        if((nod.y+10+parseInt(box.style.height))>port.height){
            box.style.top=null;
            box.style.bottom=(port.height-nod.y+5)+'px'
        }
        else{
            box.style.bottom=null;
            box.style.top=(nod.y+10)+'px'
        }
    };
    this.makePoints=function(){
        var obj=getMain();
        var param=arguments[0]?arguments[0]:[];
        obj.points.make(param)
    };
    this.checkStr=function(str){
        var vzor=/[&~#$%]/g;
        return str.replace(vzor,'')
    };
    this.getRandom=function(){
        var rndNum=(browser.klient=='saf')?parseInt(Math.random()*10000):false;
        var rnd=rndNum?'&rnd='+rndNum:'';
        return rnd
    }

};

function PortObj(mainObj,name){
    this.el=null;
    this.type='';
    this.width=0;
    this.height=0;
    this.minWidth='';
    this.minHeight='';
    this.tmp=new Object();
    this.init=function(){
        var obj=mainObj[name];
        obj.el=gEl('port');
        obj.type=config.mapPort.type;
        obj.el=gEl('port');
        var status=1;
        if(obj.type=='static'){
            if(config.mapPort.size.length<2){
                throw new Error("port size definition Error");
                var a
            }
            obj.width=config.mapPort.size[0];
            obj.height=config.mapPort.size[1]
        }
        else if(engine.port.type=='dynamic'){
            obj.width='auto'
        }
        else{
            throw new Error("port definition Error");
            var a
        }
        status=obj.set();
        return status
    };
    this.set=function(){
        var obj=mainObj[name];
        var typ=engine.port.type;
        var box=obj.el;
        if((typ=='dynamic')){
            obj.setMinSize()
        }
        if(browser.klient=='op'){
            document.getElementsByTagName('body')[0].style.overflow='hidden'
        }
        if(typ=='static'){
            box.style.width=obj.width+'px';
            box.style.height=obj.height+'px';
            obj.el.style.display='block';
            obj.el.style.visibility='visible'
        }
        else{
            obj.setSize();
            obj.tmp.mozstop=obj.getSize().height;
            obj.tmp.mozstop=null;
            obj.el.style.visibility='visible';
            dom.addEvent(window,'resize',obj.setSize,false,1)
        }
        return 0
    };
    this.setSize=function(){
        var obj=mainObj[name];
        var box=obj.el;
        var x=mainObj.getDocSize().width;
        var y=mainObj.getDocSize().height;
        var portTop=mainObj.getPosition(box,'top');
        var y=y-portTop-10;
        var portWidth=box.offsetWidth;
        if(browser.klient!='ie'){
            box.style.width=obj.width;
            box.style.height=y+'px'
        }
        else{
            if(!obj.tmp.winWidth&&(portWidth<=200)){
                box.style.width=obj.minWidth+'px';
                obj.tmp.winWidth=x
            }
            else if(obj.tmp.winWidth&&(obj.tmp.winWidth<x)){
                box.style.width='auto';
                obj.tmp.winWidth=null
            }
            y=y<=obj.minHeight?obj.minHeight:y;
            box.style.height=y+'px'
        }
        if(subMenu.inited){
            window.setTimeout('subMenu.restoreBox()',1)
        }
        if(!obj.tmp.postInit){
            obj.tmp.postInit='firstStep'
        }
        else{
            window.clearTimeout(obj.tmp.resizeFlag);
            if((browser.klient=='ie')&&(obj.tmp.postInit=='firstStep')){
                obj.tmp.postInit='next'
            }
            else{
                obj.tmp.resizeFlag=window.setTimeout('engine.map.update();',10)
            }
            box.style.display='block'
        }
    };
    this.getSize=function(){
        var obj=mainObj[name];
        var box=obj.el;
        var x=box.offsetWidth;
        var y=box.offsetHeight;
        return{
            width:x,height:y
        }
    };
    this.setMinSize=function(){
        var obj=mainObj[name];
        obj.minHeight=200;
        obj.minWidth=200;
        obj.el.style.minWidth=obj.minWidth+'px';
        obj.el.style.minHeight=obj.minHeight+'px'
    };
    this.getFirstTile=function(){
        var obj=mainObj[name];
        var tile=new Object();
        var port=obj.getSize();
        var x=parseInt(((mainObj.map.center.x+mainObj.map.drift)-(port.width/2))/mainObj.tileSize,10);
        var y=parseInt((mainObj.map.center.y-(port.height/2))/mainObj.tileSize,10);
        tile.x=x<0?0:x;
        tile.y=y<0?0:y;
        return tile
    };
    this.getLastTile=function(){
        var obj=mainObj[name];
        var tile=new Object();
        var port=obj.getSize();
        var x=parseInt(((mainObj.map.center.x+mainObj.map.drift)+(port.width/2))/mainObj.tileSize,10);
        var y=parseInt((mainObj.map.center.y+(port.height/2))/mainObj.tileSize,10);
        tile.x=x>=mainObj.map.tilesX?mainObj.map.tilesX-1:x;
        tile.y=y>=mainObj.map.tilesX?mainObj.map.tilesX-1:y;
        return tile
    }

};

function MapObj(mainObj,name){
    this.el=null;
    this.tilesX=0;
    this.tilesY=0;
    this.width=0;
    this.height=0;
    this.scale=0;
    this.center={
        x:0,y:0
    };
    this.realCenter={
        x:0,y:0
    };
    this.drift=config.mapControls.menuBg?100:0;
    this.typ='';
    this.mapServer='';
    this.maxSize=0;
    this.tileDrift={
        x:0,y:0
    };
    this.limit=new Object();
    this.preLoad=0;
    this.virt=null;
    this.oldCenter=new Object();
    this.tmp=new Object();
    this.init=function(){
        var obj=mainObj[name];
        if(obj.scale<=obj.maxSize){
            var exp=engine.map.scale;
            obj.tileDrift={
                x:0,y:0
            };
            obj.virt=false
        }
        else{
            var exp=obj.maxSize;
            obj.virt=true
        }
        obj.tilesX=Math.pow(2,exp);
        obj.tilesY=obj.tilesX;
        obj.width=obj.tilesX<<8;
        obj.height=obj.width;
        var realTilesX=Math.pow(2,obj.scale);
        var realTilesY=realTilesX;
        var realWidth=realTilesX<<8;
        var realHeight=realWidth;
        obj.realCenter.x=mainObj.center.x>>(mainObj.BPT-obj.scale);
        obj.realCenter.y=mainObj.center.y>>(mainObj.BPT-obj.scale);
        var realCenterX=mainObj.center.x>>8;
        var realCenterY=mainObj.center.y>>8;
        var left=(obj.realCenter.x>>8)-obj.tilesX/2;
        var top=(obj.realCenter.y>>8)-obj.tilesY/2;
        if(left<0){
            obj.tileDrift.x=0
        }
        else if(left+obj.tilesX>realTilesX){
            obj.tileDrift.x=(left-((left+obj.tilesX)-realTilesX))<<8
        }
        else{
            obj.tileDrift.x=left<<8
        }
        if(top<0){
            obj.tileDrift.y=0
        }
        else if(top+obj.tilesY>realTilesY){
            obj.tileDrift.y=((top-((top+obj.tilesY)-realTilesY)))<<8
        }
        else{
            obj.tileDrift.y=top<<8
        }
        obj.center.x=obj.realCenter.x-obj.tileDrift.x;
        obj.center.y=obj.height-(obj.realCenter.y-obj.tileDrift.y);
        obj.tmp.moveCount={
            x:0,y:0
        }
    };
    this.set=function(){
        var obj=mainObj[name];
        obj.tmp.move=true;
        var mapa=obj.el;
        var port=mainObj.port.getSize();
        mapa.style.width=obj.width+'px';
        mapa.style.height=obj.height+'px';
        mapa.style.top=-(obj.center.y)+Math.round(port.height/2)+'px';
        mapa.style.left=(-(obj.center.x)+((Math.round(port.width/2))-obj.drift/2))+'px'
    };
    this.updateCenter=function(){
        var obj=mainObj[name];
        obj.oldCenter.x=obj.realCenter.x;
        obj.oldCenter.y=obj.realCenter.y;
        obj.realCenter.x=obj.center.x+obj.tileDrift.x;
        obj.realCenter.y=obj.height-obj.center.y+obj.tileDrift.y;
        var x=obj.oldCenter.x-obj.realCenter.x;
        var y=obj.oldCenter.y-obj.realCenter.y;
        obj.tmp.moveCount.x+=x;
        obj.tmp.moveCount.y+=y;
        if((Math.abs(obj.tmp.moveCount.x/256)>2)||(Math.abs(obj.tmp.moveCount.y/256)>2)){
            obj.clear();
            obj.tmp.moveCount={
                x:0,y:0
            }
        }
        var stredX=obj.realCenter.x<<((mainObj.BPT-obj.scale));
        var stredY=obj.realCenter.y<<((mainObj.BPT-obj.scale));
        if(mainObj.status.get('changeCenter')){
            mainObj.center.x=stredX;
            mainObj.center.y=stredY
        }
        mainObj.status.set('changeCenter',true);
        if(obj.virt){
            obj.updateVirtual()
        }
    };
    this.updateVirtual=function(){
        var obj=mainObj[name];
        var port=mainObj.port.getSize();
        var left=obj.realCenter.x-port.width;
        var right=obj.realCenter.x+port.width;
        var bottom=obj.realCenter.y-port.height;
        var top=obj.realCenter.y+port.height;
        var update=0;
        if((left>0)&&((obj.center.x-port.width))<0){
            update+=1
        }
        else if((right<Math.pow(2,obj.scale)*256)&&((obj.center.x+port.width)>obj.width)){
            update+=1
        }
        else if((top<Math.pow(2,obj.scale)*256)&&((obj.center.y-port.height)<0)){
            update+=1
        }
        else if(bottom>0&&((obj.center.y+port.height)>obj.height)){
            update+=1
        }
        if(update){
            obj.update()
        }
    };
    this.update=function(){
        var obj=mainObj[name];
        if(!mainObj.status.get('fromResult')){
            mainObj.status.set('noBubleMove',1)
        }
        else{
            mainObj.status.set('fromResult',0)
        }
        var co=obj.el.childNodes;
        while(co.length>0){
            obj.el.removeChild(obj.el.firstChild)
        }
        obj.clear(1);
        obj.init();
        obj.check();
        obj.setLimit();
        obj.set();
        mainObj.mpLayer.init();
        obj.updateAuthorInfo();
        mainObj.urlMaker.make();
        if(nearSearch){
            nearSearch.updateItems()
        }
        obj.el.style.display='block'
    };
    this.move=function(x,y,posun){
        var obj=mainObj[name];
        if(gEl('buble')){
            engine.vizitka.moveSwitch()
        }
        var newMapX=parseInt(obj.el.style.left)+x*posun;
        var newMapY=parseInt(obj.el.style.top)+y*posun;
        var i=0;
        var k=0;
        function move(){
            i+=2;
            k=k+i;
            if(k>posun){
                window.clearInterval(interval);
                obj.el.style.top=newMapY+'px';
                obj.el.style.left=newMapX+'px';
                obj.center.x-=x*posun;
                obj.center.y-=y*posun;
                var a=mainObj.controls.kbdAllow?mainObj.controls.kbd.init():0;
                obj.endMove();
                obj.tmp.move=true;
                return
            }
            obj.tmp.move=false;
            obj.el.style.top=(parseInt(obj.el.style.top)+i*y)+'px';
            obj.el.style.left=(parseInt(obj.el.style.left)+i*x)+'px'
        };
        var interval=window.setInterval(move,5)
    };
    this.moveX=function(x,y,posunX,posunY,moveType){
        var obj=mainObj[name];
        if(gEl('buble')){
            engine.vizitka.moveSwitch()
        }
        var newMapX=parseInt(obj.el.style.left)+x*posunX;
        var newMapY=parseInt(obj.el.style.top)+y*posunY;
        var i=0;
        var kx=0;
        var ky=0;
        function move(){
            i+=2;
            kx=kx>Math.abs(posunX)?kx:kx+i;
            ky=ky>Math.abs(posunY)?ky:ky+i;
            if((kx>=posunX)&&(ky>=posunY)){
                window.clearInterval(window.interval2);
                obj.el.style.top=newMapY+'px';
                obj.el.style.left=newMapX+'px';
                var port=mainObj.port.getSize();
                obj.center.y=Math.round(port.height/2)-parseInt(obj.el.style.top);
                obj.center.x=(Math.round(port.width/2))-parseInt(obj.el.style.left)-obj.drift;
                var a=mainObj.controls.kbdAllow?mainObj.controls.kbd.init():0;
                window.interval2=0;
                obj.endMove();
                obj.tmp.move=true;
                return
            }
            obj.tmp.move=false;
            if((i%10==0)){
                var port=mainObj.port.getSize();
                obj.center.y=Math.round(port.height/2)-parseInt(obj.el.style.top);
                obj.center.x=(Math.round(port.width/2))-parseInt(obj.el.style.left)-obj.drift;
                engine.mpLayer.mapLayers["basis"].fill()
            }
            if(ky<posunY){
                obj.el.style.top=(parseInt(obj.el.style.top)+i*y)+'px'
            }
            if(kx<posunX){
                obj.el.style.left=(parseInt(obj.el.style.left)+i*x)+'px'
            }
        };
        window.interval2=window.setInterval(move,10)
    };
    this.endMove=function(){
        var obj=mainObj[name];
        if(gEl('buble')){
            engine.vizitka.moveSwitch()
        }
        mainObj.status.set('changeCenter',true);
        obj.updateCenter();
        mainObj.urlMaker.make();
        if(nearSearch){
            nearSearch.updateItems()
        }
        obj.tmp.fillTimer=window.setTimeout('engine.mpLayer.fill()',50);
        obj.check()
    };
    this.changeBasis=function(e){
        var obj=mainObj[name];
        if(!arguments[0]||(arguments[0].type!='fake')){
            var trg=dom.getTarget(e);
            var ids=trg.id?trg.id:trg.parentNode.id;
            var where=gEl('act').getElementsByTagName('span')[0].firstChild;
            if(trg.firstChild.nodeName.toLowerCase()=='span'){
                var val=trg.firstChild.firstChild.nodeValue
            }
            else{
                var val=trg.firstChild.nodeValue
            }
            where.nodeValue=val;
            dom.cancelDef(e)
        }
        else{
            var trg=arguments[0];
            var ids=trg.id
        }
        obj.typ=ids;
        obj.update();
        if(mainObj.controls.basisSwitch){
            mainObj.controls.basisSwitch.reSet()
        }
    };
    this.clear=function(){
        var obj=mainObj[name];
        for(i in mainObj.mpLayer.mapLayers){
            if(mainObj.mpLayer.mapLayers[i].run){
                var first=mainObj.port.getFirstTile();
                var last=mainObj.port.getLastTile();
                var tmpObj=new Object();
                var startX=first.x;
                var endX=last.x;
                var startY=first.y;
                var endY=last.y;
                if(!arguments[0]){
                    for(var x=startX;x<=endX;x++){
                        for(var y=startY;y<=endY;y++){
                            var a=(mainObj.map.tilesX-1)-y;
                            var tileX=mainObj.decToHex(x+(mainObj.map.tileDrift.x>>8));
                            var tileY=mainObj.decToHex(a+(mainObj.map.tileDrift.y>>8));
                            var str=i+'-'+mainObj.map.scale+'-'+tileX+'-'+tileY;
                            if(mainObj.mpLayer.mapLayers[i].folder[str]){
                                tmpObj[str]=gEl(str);
                                mainObj.mpLayer.mapLayers[i].folder[str]=null
                            }
                        }
                    }
                }
                for(k in mainObj.mpLayer.mapLayers[i].folder){
                    if(mainObj.mpLayer.mapLayers[i].folder[k]){
                        if(i!='active'){
                            dom.removeEvent(mainObj.mpLayer.mapLayers[i].folder[k],'mouseover',dom.cancelDef,false);
                            dom.removeEvent(mainObj.mpLayer.mapLayers[i].folder[k],'mousemove',dom.cancelDef,false);
                            dom.removeEvent(mainObj.mpLayer.mapLayers[i].folder[k],'onclick',dom.stopEvent,false)
                        }
                        mainObj.mpLayer.mapLayers[i].folder[k].parentNode.removeChild(mainObj.mpLayer.mapLayers[i].folder[k])
                    }
                }
                mainObj.mpLayer.mapLayers[i].folder=null;
                mainObj.mpLayer.mapLayers[i].folder=tmpObj;
                tmpObj=null
            }
        }
    };
    this.check=function(){
        var obj=mainObj[name];
        var tileX=mainObj.getNumToHex(((mainObj.center.x)));
        var tileY=mainObj.getNumToHex(((mainObj.center.y)));
        var typ=engine.map.typ;
        if((mainObj.mapScale)&&(mainObj.mapScale.pic)){
            mainObj.mapScale.set()
        }
        if((mainObj.mapNorth)&&(mainObj.mapNorth.pic)){
            mainObj.mapNorth.set()
        }
    };
    this.setLimit=function(){
        var obj=mainObj[name];
        if(!arguments[0]){
            obj.limit.bottom=config.mapLimit.bottom?mainObj.getPointInScale(config.mapLimit.bottom,'y'):0;
            obj.limit.right=config.mapLimit.right?mainObj.getPointInScale(config.mapLimit.right,'x'):Math.pow(2,obj.scale)<<8;
            obj.limit.top=config.mapLimit.top?mainObj.getPointInScale(config.mapLimit.top,'y'):Math.pow(2,obj.scale)<<8;
            obj.limit.left=config.mapLimit.left?mainObj.getPointInScale(config.mapLimit.left,'x'):0
        }
        else{
            var lm=arguments[0];
            var max=Math.pow(2,obj.scale)*mainObj.tileSize;
            var data={
                top:[lm[0],max],right:[lm[1],max],bottom:[lm[2],0],left:[lm[3],0]
            };
            for(i in obj.limit){
                if((i!='inits')&&(i!='drift')){
                    var num=((i=='top')||(i=='bottom'))?obj.realCenter.y:obj.realCenter.x;
                    var tile=parseInt(num/256);
                    var num=tile*256;
                    var dir=((i=='bottom')||(i=='left'))?-1:1;
                    var nov=num+(dir*(256*(data[i][0])));
                    if(data[i][0]){
                        if(dir>0){
                            nov=nov+(256);
                            obj.limit[i]=nov<data[i][1]?nov:data[i][1]
                        }
                        else{
                            nov=nov;
                            obj.limit[i]=nov>data[i][1]?nov:data[i][1]
                        }
                    }
                    else{
                        if(dir>0){
                            obj.limit[i]=nov+256
                        }
                        else{
                            obj.limit[i]=nov
                        }
                    }
                }
            }
        }
    };
    this.getAuthors=function(){
        var obj=mainObj[name];
        var str=config.authors[obj.typ][obj.scale-1];
        return str
    };
    this.makeAuthorsInfo=function(){
        var obj=mainObj[name];
        var co=cEl('p');
        co.id='authorInfo';
        co.style.marginRight=mainObj.map.drift?'90px':'3px';
        var str=obj.getAuthors();
        co.innerHTML='Mapové podklady &copy; '+str;
        gEl('port').appendChild(co)
    };
    this.updateAuthorInfo=function(){
        var obj=mainObj[name];
        var co=gEl('authorInfo');
        var str=obj.getAuthors();
        co.innerHTML='Mapové podklady &copy; '+str
    }

};

function LayersObj(mainObj,name){
    this.layersFold=new Array();
    this.mapLayers=new Object();
    this.init=function(){
        var obj=mainObj[name];
        var k=0;
        for(var i in config.mapLayers){
            k++
        };
        var num=1;
        for(var i in config.mapLayers){
            if(config.mapLayers[i].position=='bott'){
                obj.layersFold[0]=i
            }
            else if(config.mapLayers[i].position=='top'){
                obj.layersFold[k-1]=i
            }
            else{
                obj.layersFold[num]=i;
                num++
            }
            var source='';
            switch(config.mapLayers[i].source){
                case'def':source=mainObj.map.mapServer;
                break;
                case'ext':source=config.extend.mapServer;
                break;
                default:source=config.mapLayers[i].source;
                break
            };
            obj.mapLayers[i]=new obj.MapLayer(i,config.mapLayers[i].type,source,config.mapLayers[i].customQuery,config.mapLayers[i].running,mainObj)
        }
        obj.make();
        return 0
    };
    this.make=function(){
        var obj=mainObj[name];
        var cont=mainObj.map.el;
        var fillTyp=cont.childNodes.length>0?'before':'append';
        for(var i=0;i<obj.layersFold.length;i++){
            var lay=obj.mapLayers[obj.layersFold[i]].box;
            if(fillTyp=='append'){
                cont.appendChild(lay)
            }
            else{
                var prev=cont;
                cont.insertBefore(lay,cont.lastChild)
            }
        }
        obj.fill()
    };
    this.fill=function(){
        var obj=mainObj[name];
        for(var i in obj.mapLayers){
            if(obj.mapLayers[i].run&&(i!='active')){
                obj.mapLayers[i].fill()
            }
            else if(mainObj.firstStep&&(obj.mapLayers[i].run)&&(i=='active')){
                mainObj.points.fillActive('result')
            }
        }
    };
    this.MapLayer=function(layerName,typ,data,query,run,obj){
        this.box=cEl('div');
        this.box.style.position='absolute';
        this.box.style.top='0px';
        this.box.style.left='0px';
        this.box.style.width=obj.map.width+'px';
        this.box.style.height=obj.map.width+'px';
        this.box.id=layerName;
        this.server=data;
        this.name=layerName;
        this.typ=typ;
        this.run=run;
        this.folder=new Object();
        this.query=query;
        if(this.typ=='tile'){
            this.fill=fillTile
        }
        else{
            this.fill=fillIcon
        }
    };
    this.changeLayer=function(layerName){
        var obj=mainObj[name];
        gEl(layerName).id='old';
        var type=obj.mapLayers[layerName].typ;
        var source=obj.mapLayers[layerName].server;
        var run=obj.mapLayers[layerName].run;
        var qw=obj.mapLayers[layerName].query;
        var temp=new obj.MapLayer(name,type,source,qw,run,mainObj);
        mainObj.map.el.replaceChild(temp.box,gEl('old'));
        obj.mapLayers[layerName]=temp;
        temp=null;
        obj.mapLayers[layerName].fill()
    };
    function fillTile(){
        var obj=mainObj[name];
        var actLayer=this.name;
        var first=mainObj.port.getFirstTile();
        var last=mainObj.port.getLastTile();
        mainObj.tileQuery='';
        var custom='';
        if(this.query){
            custom='?customQuery='+this.query
        }
        engine.tileQuery='/lookup?'+custom;
        var startX=first.x;
        var endX=last.x;
        var startY=first.y;
        var endY=last.y;
        for(x=startX;x<=endX;x++){
            if((x>=0)&&(x<mainObj.map.width/mainObj.tileSize)){
                for(y=startY;y<=endY;y++){
                    if((y>=0)&&(y<mainObj.map.height/mainObj.tileSize)){
                        var a=(engine.map.tilesX-1)-y;
                        var tileX=mainObj.decToHex(x+(mainObj.map.tileDrift.x>>8));
                        var tileY=mainObj.decToHex(a+(mainObj.map.tileDrift.y>>8));
                        mainObj.tileQuery+='tile='+mainObj.map.scale+'_'+tileX+'_'+tileY+'&';
                        if(this.name=='basis'){
                            var urlStr=this.server+'/'+mainObj.map.typ+'/'+mainObj.map.scale+'_'+tileX+'_'+tileY
                        }
                        else{
                            var urlStr=this.server+'/'+actLayer+'/'+mainObj.map.scale+'_'+tileX+'_'+tileY+custom
                        }
                        if(!gEl(actLayer+'-'+mainObj.map.scale+'-'+tileX+'-'+tileY)){
                            var pic=cEl('img');
                            pic.style.top=((y*mainObj.tileSize))+'px';
                            pic.style.left=((x*mainObj.tileSize))+'px';
                            pic.style.position='absolute';
                            pic.width='256';
                            pic.height='256';
                            pic.id=actLayer+'-'+engine.map.scale+'-'+tileX+'-'+tileY;
                            pic.galleryimg="no";
                            if(actLayer=='trasa'){
                                obj.setOpacity(pic,70)
                            }
                            obj.mapLayers[actLayer].folder[pic.id]=pic;
                            dom.addEvent(gEl('mapa'),'contextmenu',dom.cancelDef,false);
                            dom.addEvent(pic,'mouseover',dom.cancelDef,false);
                            dom.addEvent(pic,'mousemove',dom.cancelDef,false);
                            dom.addEvent(pic,'onclick',dom.stopEvent,false);
                            obj.mapLayers[actLayer].box.appendChild(pic);
                            pic.src=urlStr
                        }
                    }
                }
            }
        }
        if(!engine.firstStep){
            mainObj.points.fillActive('result');
            engine.firstStep=true
        }
    };
    function fillIcon(def){
        var obj=mainObj[name];
        if(!def){
            def=new Array()
        }
        var num=mainObj.points.fillFlag;
        if(num=='result'){
            mainObj.points.fillFlag=0
        }
        else if(!isNaN(mainObj.points.fillFlag)){
            mainObj.points.fillFlag++;
            if(mainObj.points.fillFlag<mainObj.points.pointServers.length){
                
            }
            else{
                mainObj.points.fillFlag='ext'
            }
        }
        else{
            mainObj.points.fillFlag='result'
        }
        var actLayer=this.name;
        for(var i=0;i<def.length;i++){
            var idTileX=def[i].x.toString(16);
            var idTileY=def[i].y.toString(16);
            rTileX=((def[i].x>>(mainObj.BPN-mainObj.map.scale))<<(mainObj.BPN-mainObj.map.scale));
            rTileY=((def[i].y>>(mainObj.BPN-mainObj.map.scale))<<(mainObj.BPN-mainObj.map.scale));
            var hexX=rTileX.toString(16);
            var hexY=rTileY.toString(16);
            var str=actLayer+'-'+mainObj.map.scale+'-'+hexX+'-'+hexY;
            var picStr='basis'+'-'+mainObj.map.scale+'-'+hexX+'-'+hexY;
            if(mainObj.mpLayer.mapLayers['basis'].folder[picStr]){
                if(!this.folder[str]){
                    var tileX=(rTileX>>(mainObj.BPT-mainObj.map.scale))-mainObj.map.tileDrift.x;
                    var tileY=mainObj.map.height-((rTileY>>(mainObj.BPT-mainObj.map.scale))-mainObj.map.tileDrift.y);
                    var pTiles=cEl('div');
                    pTiles.style.position='absolute';
                    pTiles.style.top=(tileY-256)+'px';
                    pTiles.style.left=tileX+'px';
                    pTiles.style.width=mainObj.tileSize+'px';
                    pTiles.style.height=mainObj.tileSize+'px';
                    pTiles.style.backgroundColor=color;
                    pTiles.id=str;
                    this.folder[pTiles.id]=pTiles;
                    obj.mapLayers.active.box.appendChild(pTiles)
                }
                else{
                    var pTiles=this.folder[str]
                }
                if(!document.getElementById('acp-'+def[i].id+'-'+num)){
                    var pointX=(def[i].x>>(mainObj.BPT-mainObj.map.scale))&255;
                    var pointY=(def[i].y>>(mainObj.BPT-mainObj.map.scale))&255;
                    pointX=pointX<0?0:pointX;
                    pointY=pointY<0?0:pointY;
                    if(def[i].logo){
                        
                    }
                    else if(!isNaN(num)){
                        var point=mainObj.points.makeIcon(pointX,pointY,def[i].z,def[i].title,'acp-'+def[i].id,def[i].type,num,def[i].x,def[i].y);
                        if(point.icon&&point.urlStr){
                            var pointIcon=point.icon;
                            pTiles.appendChild(pointIcon);
                            pointIcon.src=point.urlStr
                        }
                    }
                    else if(num=='result'){
                        var point=mainObj.points.makeMultiResult(pointX,pointY,def[i].z,def[i].title,'acp-'+def[i].id,def[i].type,num,def[i].x,def[i].y);
                        pTiles.appendChild(point)
                    }
                }
                else{
                    
                }
            }
        }
        def=null;
        if(mainObj.points.fillFlag=='result'){
            
        }
        else if(!isNaN(mainObj.points.fillFlag)&&(mainObj.points.fillFlag<mainObj.points.pointServers.length)){
            mainObj.points.fillActive(mainObj.points.fillFlag)
        }
        else if(mainObj.points.fillFlag=='ext'){
            
        }
        else{
            
        }
        if(userPoint&&!gEl('userIcon')){
            mainObj.userIcon.paste()
        }
        if((typeof(mainObj.vizitka)!='undefined')&&mainObj.vizitka.bubleFold.opened&&!gEl('buble')){
            mainObj.vizitka.bubleSwitch()
        }
    };
    this.setOpacity=function(trg,opacity){
        opacity=(opacity==100)?99.999:opacity;
        trg.style.filter="alpha(opacity:"+opacity+",style=0)";
        trg.style.KHTMLOpacity=opacity/100
    }

};

function StatusObj(mainObj,name){
    this.set=function(param,value){
        var obj=mainObj[name];
        obj[param]=value
    };
    this.get=function(param){
        var obj=mainObj[name];
        return obj[param]
    }

};

function ControlsObj(mainObj,name){
    this.type='';
    this.data=0;
    this.dataAllow=[];
    this.basis=[];
    this.basisName=[];
    this.move=0;
    this.zoom=0;
    this.zoomAllow=new Array();
    this.zoomLimit=new Object();
    this.extend=[];
    this.button=new Object();
    this.allow=true;
    this.mouseAllow=1;
    this.kbdAllow=0;
    this.mapMove=0;
    this.mapCut=0;
    this.bsSwitch=[];
    this.bsSwitchSet=new Object();
    this.tmp=new Object();
    this.moveFold=new Object();
    this.init=function(){
        var obj=mainObj[name];
        obj.type=config.mapControls.type;
        obj.bsSwitch=config.mapControls.bsSwitch;
        obj.dataAllow=config.mapControls.dataAllow;
        obj.basis=config.mapControls.basis;
        obj.basisName=config.mapControls.basisName;
        obj.moveMouse=new MoveMouse(mainObj,obj,'moveMouse');
        obj.moveFold.foldX=0;
        obj.moveFold.foldY=0;
        if(obj.type=='none'){
            return
        }
        if(config.mapControls.menuBg){
            var bg=cEl('div');
            bg.id='mainBg';
            gEl('port').insertBefore(bg,gEl('obal'));
            bg.style.display='block';
            dom.addEvent(bg,'click',dom.stopEvent,false,1);
            dom.addEvent(bg,'mousedown',dom.stopEvent,false,1)
        }
        obj.clickAction=new ClickActionObj(mainObj,obj,'clickAction');
        if(obj.bsSwitch.length>0){
            obj.basisSwitch=new BasisSwitch(mainObj,obj,'basisSwitch');
            obj.basisSwitch.init()
        }
        if(obj.kbdAllow){
            obj.kbd=new KbdObj(mainObj,obj,'kbd');
            obj.kbd.init()
        }
        if(config.mapControls.data){
            obj.menu=new MenuObj(mainObj,obj,'menu');
            obj.menu.init()
        }
        if(config.mapControls.move){
            obj.move=new MoveObj(mainObj,obj,'move');
            obj.move.make();
            obj.move.init()
        }
        if(config.mapControls.zoom){
            obj.zoom=new ZoomObj(mainObj,obj,'zoom');
            var typ=config.mapControls.zoom;
            if((typ=='full')||(typ=='button')||(typ=='bar')){
                obj.zoom.init(typ)
            }
            else if(config.mapControls.zoom=='none'){
                
            }
            else{
                throw new Error('zoom definition error');
                var a
            }
        }
        obj.gps=new GpsObj(mainObj,obj,'gps');
        obj.makeExtend();
        if(obj.mapCut){
            obj.selection=new SelectionObj(mainObj,obj,'selection');
            obj.selection.init()
        }
        obj.activeMouse()
    };
    this.activeMouse=function(){
        var obj=mainObj[name];
        if(engine.controls.mapMove){
            dom.addEvent(mainObj.port.el,'mousedown',obj.moveMouse.init,false,1)
        }
        if(obj.mapCut&&obj.selection){
            dom.addEvent(mainObj.map.el,'mousedown',obj.selection.startSelect,false,1)
        }
        if((browser.klient=='ie')||(browser.klient=='kon')||(browser.klient=='saf')){
            obj.button.move=1;
            obj.button.select=2
        }
        else if((browser.klient=='op')&&(browser.version==0)){
            obj.button.move=1;
            obj.button.select=2
        }
        else{
            obj.button.move=0;
            obj.button.select=2
        }
    };
    function MoveMouse(main,parent,name){
        this.portObj=null;
        this.portPos=new Object();
        this.init=function(e){
            var obj=parent[name];
            if(!parent.allow){
                return
            }
            var e=dom.getEvent(e);
            if(e.button!=parent.button.move){
                return
            }
            if(main.status.get('rulerActive')){
                return
            }
            engine.controls.moveMouse.handle();
            parent.moveFold.startX=e.clientX;
            parent.moveFold.startY=e.clientY;
            parent.moveFold.posXX=parseInt(main.map.el.style.left);
            parent.moveFold.posYY=parseInt(main.map.el.style.top)
        };
        this.handle=function(){
            var obj=parent[name];
            obj.portObj=main.port.getSize();
            obj.portPos.top=main.getPosition(gEl('port'),'top');
            obj.portPos.left=main.getPosition(gEl('port'),'left');
            dom.addEvent(main.port.el,'mousemove',obj.move,false);
            dom.addEvent(document,'mouseup',obj.endMouseMove,false);
            dom.addEvent(document,'mouseover',obj.breakMouseMove,false);
            dom.removeEvent(document,'mouseup',obj.clickBreak,false);
            obj.mouseMoving=true;
            gEl('port').style.cursor='move';
            if(gEl('buble')){
                engine.vizitka.moveSwitch()
            }
        };
        this.clickBreak=function(){
            
        };
        this.move=function(e){
            var obj=parent[name];
            parent.tmp.clickStop=true;
            if(e){
                if(e.type=='mousemove'){
                    var e=dom.getEvent(e)
                }
            }
            if(browser.platform!='win'){
                if(!e){
                    return
                }
                if(parent.moveFold.noMove){
                    parent.moveFold.fake=new Object();
                    parent.moveFold.fake.clientX=e.clientX;
                    parent.moveFold.fake.clientY=e.clientY;
                    parent.moveFold.fake.type='fake';
                    return
                }
                rtrn=function(){
                    window.setTimeout(function(){
                        parent.moveFold.noMove=false;
                        parent.moveMouse.move(parent.moveFold.fake)
                    }
                    ,100)
                };
                rtrn();
                parent.moveFold.noMove=true;
                parent.moveFold.fake=null
            }
            var moveX=e.clientX;
            var moveY=e.clientY;
            var newXX=moveX-parent.moveFold.startX;
            var newYY=moveY-parent.moveFold.startY;
            parent.moveFold.foldX+=newXX;
            parent.moveFold.foldY+=newYY;
            if(((Math.abs(parent.moveFold.foldX)>128)||(Math.abs(parent.moveFold.foldY)>128))){
                if(parent.tmp.timer){
                    window.clearTimeout(parent.tmp.timer)
                }
                parent.tmp.timer=window.setTimeout('engine.mpLayer.mapLayers["basis"].fill()',10);
                main.mpLayer.mapLayers.basis.fill();
                parent.moveFold.foldX=0;
                parent.moveFold.foldY=0
            }
            var ctrX=(main.map.center.x+main.map.tileDrift.x)-newXX;
            var ctrY=(main.map.height-(main.map.center.y-main.map.tileDrift.y))+newYY;
            newXX=(ctrX>main.map.limit.left)&&(ctrX<main.map.limit.right)?newXX:0;
            newYY=(ctrY>main.map.limit.bottom)&&(ctrY<main.map.limit.top)?newYY:0;
            main.map.center.x=main.map.center.x-newXX;
            main.map.center.y=main.map.center.y-newYY;
            parent.moveFold.posXX=parent.moveFold.posXX+newXX;
            parent.moveFold.posYY=parent.moveFold.posYY+newYY;
            main.map.el.style.left=(parent.moveFold.posXX)+'px';
            main.map.el.style.top=(parent.moveFold.posYY)+'px';
            parent.moveFold.startX=moveX;
            parent.moveFold.startY=moveY
        };
        this.breakMouseMove=function(e){
            if(browser.klient=='geck'){
                var p=e.target
            }
            else{
                return
            }
            if(p.nodeName.toLowerCase()=='html'){
                var obj=parent[name];
                obj.endMouseMove()
            }
        };
        this.endMouseMove=function(e){
            var obj=parent[name];
            if(parent.tmp.timer){
                window.clearTimeout(parent.tmp.timer)
            }
            gEl('port').style.cursor='default';
            window.setTimeout('engine.controls.tmp.clickStop = false',10);
            obj.mouseMoving=false;
            dom.removeEvent(main.port.el,'mousemove',obj.move,false);
            dom.removeEvent(document,'mouseup',obj.endMouseMove,false);
            dom.removeEvent(document,'mouseover',obj.breakMouseMove,false);
            main.map.endMove()
        }
    };
    function BasisSwitch(main,parent,name){
        this.init=function(){
            var bss=parent.bsSwitch;
            var allow=parent.dataAllow;
            var basis=parent.basis;
            var basisName=parent.basisName;
            var set=parent.bsSwitchSet;
            var menuPos=0;
            var box=cEl('div');
            box.id='bsSwitch';
            gEl('obal').appendChild(box);
            for(var i=0;i<bss.length;i++){
                menuPos+=28;
                set[basis[bss[i]]]=new Object();
                set[basis[bss[i]]].idx=bss[i];
                if(allow[bss[i]]){
                    set[basis[bss[i]]].active=1
                }
                else{
                    set[basis[bss[i]]].active=0
                }
                set[basis[bss[i]]].pressed=0;
                if(basis[bss[i]]==engine.map.typ){
                    set[basis[bss[i]]].pressed=1
                }
                parent.basisSwitch.makeButton(basis[bss[i]],basisName[bss[i]])
            }
            parent.basisSwitch.set();
            gEl('bsSwitch').style.display='block'
        };
        this.set=function(){
            var obj=parent.bsSwitchSet;
            for(var i in obj){
                main.deactiveItem(gEl(i),'click',parent[name].change);
                var button=gEl(i);
                if(obj[i].pressed){
                    gEl(i).className='press';
                    button.style.color='blue';
                    main.deactiveItem(gEl(i),'click',parent[name].change)
                }
                else{
                    gEl(i).className='ac'
                }
                if(obj[i].active&&(!obj[i].pressed)){
                    button.style.color='blue';
                    main.activeItem(button,'click',parent[name].change)
                }
                else if(!obj[i].active){
                    gEl(i).className='press';
                    main.deactiveItem(gEl(i),'click',parent[name].change)
                }
            }
        };
        this.reSet=function(){
            var obj=parent.bsSwitchSet;
            for(var i in obj){
                if(i==main.map.typ){
                    obj[i].pressed=1;
                    obj[i].active=1
                }
                else{
                    obj[i].pressed=0;
                    obj[i].active=parent.dataAllow[obj[i].idx]?1:0
                }
            }
            parent[name].set()
        };
        this.change=function(e){
            var obj=parent.bsSwitchSet;
            var menuSet=gEl('act')?gEl('act').getElementsByTagName('span')[0]:null;
            if(menuSet){
                menuSet.firstChild.nodeValue='Vice map'
            }
            var trg=dom.getTarget(e).id?dom.getTarget(e):dom.getTarget(e).parentNode;
            var fc=new Object();
            fc.id=trg.id;
            fc.type='fake';
            for(var i in obj){
                var button=gEl(i);
                if(i==trg.id){
                    gEl(i).className='press';
                    obj[i].pressed=1;
                    obj[i].active=1
                }
                else{
                    gEl(i).className='ac';
                    obj[i].pressed=0;
                    if(obj[i].active){
                        
                    }
                }
            }
            main.map.changeBasis(fc)
        };
        this.makeButton=function(ids,layerName){
            var trg=gEl('bsSwitch');
            var obal=cEl('div');
            obal.className='stinButt';
            var butt=cEl('div');
            dom.addEvent(obal,'click',dom.stopEvent,false,1);
            dom.addEvent(obal,'mousedown',dom.stopEvent,false,1);
            butt.id=ids;
            butt.className=ids==main.map.typ?'press':'ac';
            var cont=cEl('span');
            var txt=cTxt(layerName);
            cont.appendChild(txt);
            butt.appendChild(cont);
            obal.appendChild(butt);
            trg.appendChild(obal)
        }
    };
    function MenuObj(main,parent,name){
        this.init=function(){
            var obj=parent[name];
            obj.makeVisibleBox();
            var menu=gEl('mapMenu');
            var button=menu.getElementsByTagName('span')[0];
            var allow=parent.dataAllow;
            var basis=parent.basis;
            var basisName=parent.basisName;
            if(basis.length==basisName.length){
                obj.makeItem(allow,basis,basisName)
            }
            else{
                throw new Error('data definition error - wrong basis setting');
                var a
            }
            main.activeItem(gEl('act'),'click',obj.show);
            main.showItem([menu])
        };
        this.makeVisibleBox=function(){
            var menuBox=cEl('div');
            var topBox=cEl('div');
            var actBox=cEl('div');
            var actCntBox=cEl('span');
            var txt=cTxt('Vice map');
            menuBox.id='mapMenu';
            topBox.id='first';
            topBox.className='stinButt';
            actBox.id='act';
            actBox.className='ac';
            actCntBox.appendChild(txt);
            actBox.appendChild(actCntBox);
            topBox.appendChild(actBox);
            menuBox.appendChild(topBox);
            gEl('obal').appendChild(menuBox)
        };
        this.makeItemBox=function(){
            var obj=parent[name];
            var co=cEl('div');
            var tTop=cEl('div');
            var where=cEl('div');
            var tBott=cEl('div');
            co.id='next';
            tTop.className='topBg';
            where.className='bodyBg';
            tBott.className='bottBg';
            co.appendChild(tTop);
            co.appendChild(where);
            co.appendChild(tBott);
            gEl('mapMenu').appendChild(co);
            return where
        };
        this.makeItem=function(al,bas,basName){
            var obj=parent[name];
            var where=obj.makeItemBox();
            var items=new Array(bas.length);
            var bss=parent.bsSwitch;
            var skip=bss.length!=0?true:false;
            var obj=parent.bsSwitchSet;
            var actLay=gEl('act').getElementsByTagName('span')[0];
            for(var i=0;i<bas.length;i++){
                if((!obj[bas[i]])){
                    var item=cEl('p');
                    var fd=cEl('span');
                    var txt=cTxt(basName[i]);
                    item.className=(i==(bas.length-1))?'item last':'item';
                    item.id=bas[i];
                    if(main.map.typ==bas[i]){
                        actLay.firstChild.nodeValue=basName[i]
                    }
                    if(al[i]==1){
                        item.style.color='blue';
                        main.activeItem(item,'click',main.map.changeBasis)
                    }
                    else{
                        item.style.color='#d2d2d2'
                    }
                    if(browser.klient=='ie'){
                        dom.addEvent(item,'mouseover',parent.menu.changeBg,1);
                        dom.addEvent(item,'mouseout',parent.menu.changeBg,1)
                    }
                    fd.appendChild(txt);
                    item.appendChild(fd);
                    where.appendChild(item)
                }
            }
        };
        this.show=function(e){
            var trg=gEl('next');
            if(trg.style.display!='block'){
                trg.style.display='block';
                dom.stopEvent(e);
                dom.addEvent(document,'click',parent.menu.hide,false,1)
            }
        };
        this.hide=function(e){
            var trg=gEl('next');
            if(trg.style.display=='block'){
                trg.style.display='none';
                dom.stopEvent(e);
                dom.removeEvent(document,'click',parent.menu.hide,false)
            }
        };
        this.change=function(param){
            parent.dataAllow=param;
            var where=gEl('next');
            var items=where.getElementsByTagName('p');
            var field=engine.controls.menu.setSkip();
            var step=0;
            for(var i=0;i<field.length;i++){
                if(field[i]){
                    if(param[i]){
                        items[step].className=(i==(items.length-1))?'item last':'item';
                        items[step].style.color='blue';
                        engine.activeItem(items[step],'click',main.map.changeBasis)
                    }
                    else{
                        items[step].style.color='#d2d2d2';
                        engine.deactiveItem(items[step],'click',main.map.changeBasis)
                    }
                    step++
                }
            }
            parent.basisSwitch.reSet()
        };
        this.setSkip=function(){
            var cnt=parent.dataAllow;
            var bss=parent.bsSwitch;
            var work=new Array();
            for(var i=0;i<cnt.length;i++){
                for(var k=0;k<bss.length;k++){
                    if(bss[k]==i){
                        work[i]=0;
                        break
                    }
                    else{
                        work[i]=1
                    }
                }
            }
            return work
        };
        this.changeBg=function(e){
            var trg=dom.getTarget(e).nodeName.toLowerCase()!='p'?dom.getTarget(e).parentNode:dom.getTarget(e);
            if(trg.style.backgroundColor!='silver'){
                trg.style.backgroundColor='silver'
            }
            else{
                trg.style.backgroundColor='#ffffff'
            }
        }
    };
    function MoveObj(main,parent,name){
        this.init=function(){
            var obj=parent[name];
            var buttons=gEl('dirBox');
            if(!config.mapControls.data&&config.mapControls.move){
                var dir=gEl('dirBox')
            }
            var items=buttons.getElementsByTagName('img');
            for(i=0;i<items.length;i++){
                main.activeItem(items[i],'click',obj.setDirection)
            }
            main.showItem([buttons])
        };
        this.make=function(){
            var box=cEl('div');
            box.id='dirBox';
            var wrk=cEl('div');
            box.appendChild(wrk);
            var field=new Array(['ss',17,20],['vv',20,17],['jj',17,20],['zz',20,17]);
            for(var i=0;i<field.length;i++){
                var pic=cEl('img');
                pic.width=field[i][1];
                pic.height=field[i][2];
                var urlStr='http://1.im.cz/mapyp/img/'+field[i][0]+'.gif';
                pic.src=urlStr;
                pic.id=field[i][0];
                wrk.appendChild(pic)
            }
            gEl('obal').appendChild(box)
        };
        this.setDirection=function(e){
            if(!parent.allow){
                return
            }
            if(e.type=='click'){
                var trg=dom.getTarget(e);
                dom.stopEvent(e);
                dom.cancelDef(e)
            }
            else{
                var trg=arguments[0]
            }
            if(main.map.tmp.move){
                var port=main.port.getSize();
                var posunH=Math.round(port.width/3);
                var posunV=Math.round(port.height/3);
                switch(trg.id){
                    case'ss':var top=main.map.limit.top>=main.map.height+main.map.tileDrift.y?main.map.height+main.map.tileDrift.y:main.map.limit.top;
                    if((main.map.realCenter.y+posunV)<=top){
                        main.map.move(0,1,posunV)
                    }
                    else if((main.map.realCenter.y+posunV)>top){
                        var posun=(top-main.map.realCenter.y)-1;
                        engine.map.move(0,1,posun)
                    }
                    else{
                        var a=parent.kbdAllow?parent.kbd.init():0
                    }
                    break;
                    case'zz':var left=main.map.limit.left<=0?main.map.tileDrift.x:main.map.limit.left;
                    if((main.map.realCenter.x-posunV)>=left){
                        main.map.move(1,0,posunV)
                    }
                    else if((main.map.realCenter.x-posunV)<left){
                        var posun=-(left-main.map.realCenter.x);
                        main.map.move(1,0,posun)
                    }
                    else{
                        var a=parent.kbdAllow?parent.kbd.init():0
                    }
                    break;
                    case'vv':var right=main.map.limit.right>=main.map.width+main.map.tileDrift.x?main.map.width+main.map.tileDrift.x:main.map.limit.right;
                    if((main.map.realCenter.x+posunH)<=right){
                        main.map.move(-1,0,posunH)
                    }
                    else if((main.map.realCenter.x+posunH)>right){
                        var posun=(right-main.map.realCenter.x)-1;
                        main.map.move(-1,0,posun)
                    }
                    else{
                        var a=parent.kbdAllow?parent.kbd.init():0
                    }
                    break;
                    case'jj':var bottom=main.map.limit.bottom<=0?main.map.tileDrift.y:main.map.limit.bottom;
                    if((main.map.realCenter.y-posunH)>=bottom){
                        main.map.move(0,-1,posunH)
                    }
                    else if((main.map.realCenter.y-posunH)<bottom){
                        var posun=-(bottom-main.map.realCenter.y);
                        main.map.move(0,-1,posun)
                    }
                    else{
                        var a=parent.kbdAllow?parent.kbd.init():0
                    }
                    break;
                    default:break
                }
            }
        }
    };
    function ZoomObj(main,parent,name){
        this.tmp=new Object();
        this.tempZoom=[];
        this.init=function(param){
            var obj=parent[name];
            obj.tmp.oldZoom=mainObj.map.scale;
            if(param=='full'){
                obj.makeAll();
                engine.activeItem(gEl('plus'),'click',obj.byButton);
                engine.activeItem(gEl('minus'),'click',obj.byButton);
                engine.activeItem(gEl('plus'),'mousedown',dom.stopEvent);
                engine.activeItem(gEl('minus'),'mousedown',dom.stopEvent);
                engine.activeItem(gEl('jezdec'),'mousedown',obj.byMouse);
                engine.controls.zoom.makeLevels()
            }
            else if(param=='bar'){
                obj.makeBar();
                engine.activeItem(gEl('jezdec'),'mousedown',obj.byMouse);
                engine.controls.zoom.makeLevels()
            }
            else{
                obj.makeButtons();
                engine.activeItem(gEl('plus'),'click',obj.byButton);
                engine.activeItem(gEl('minus'),'click',obj.byButton);
                engine.activeItem(gEl('plus'),'mousedown',dom.stopEvent);
                engine.activeItem(gEl('minus'),'mousedown',dom.stopEvent)
            }
        };
        this.makeAll=function(){
            var zoomBox=cEl('div');
            zoomBox.id='zoom';
            zoomBox.className='all';
            var box=cEl('div');
            var minus=cEl('img');
            minus.width=17;
            minus.height=16;
            minus.src='http://1.im.cz/mapyp/img/zoomMinus.gif';
            minus.alt='minus';
            minus.title='minus';
            minus.id='minus';
            var plus=cEl('img');
            plus.width=17;
            plus.height=16;
            plus.src='http://1.im.cz/mapyp/img/zoomPlus.gif';
            plus.alt='plus';
            plus.title='plus';
            plus.id='plus';
            var barBox=cEl('div');
            barBox.id='lineBox';
            var barStin=cEl('div');
            barStin.id='jezdecStin';
            var bar=cEl('img');
            bar.id='line';
            bar.width=5;
            bar.height=68;
            bar.src='http://1.im.cz/mapyp/img/zoomLine.gif';
            bar.alt='';
            var jezdec=cEl('img');
            jezdec.id='jezdec';
            jezdec.width=17;
            jezdec.height=9;
            jezdec.src='http://1.im.cz/mapyp/img/zoomJezdec.gif';
            jezdec.alt='';
            jezdec.title='';
            barBox.appendChild(barStin);
            barBox.appendChild(bar);
            barBox.appendChild(jezdec);
            box.appendChild(plus);
            box.appendChild(barBox);
            box.appendChild(minus);
            zoomBox.appendChild(box);
            gEl('obal').appendChild(zoomBox)
        };
        this.makeButtons=function(){
            var zoomBox=cEl('div');
            zoomBox.id='zoom';
            zoomBox.className='button';
            var box=cEl('div');
            var minus=cEl('img');
            minus.width=17;
            minus.height=16;
            minus.src='http://1.im.cz/mapyp/img/zoomMinus.gif';
            minus.alt='minus';
            minus.title='minus';
            minus.id='minus';
            var plus=cEl('img');
            plus.width=17;
            plus.height=16;
            plus.src='http://1.im.cz/mapyp/img/zoomPlus.gif';
            plus.alt='plus';
            plus.title='plus';
            plus.id='plus';
            box.appendChild(plus);
            box.appendChild(minus);
            zoomBox.appendChild(box);
            gEl('obal').appendChild(zoomBox)
        };
        this.makeBar=function(){
            var zoomBox=cEl('div');
            zoomBox.id='zoom';
            zoomBox.className='line';
            var box=cEl('div');
            var barBox=cEl('div');
            barBox.id='lineBox';
            var barStin=cEl('div');
            barStin.id='jezdecStin';
            var bar=cEl('img');
            bar.id='line';
            bar.width=5;
            bar.height=68;
            bar.src='http://1.im.cz/mapyp/img/zoomLine.gif';
            bar.alt='';
            var jezdec=cEl('img');
            jezdec.id='jezdec';
            jezdec.width=17;
            jezdec.height=9;
            jezdec.src='http://1.im.cz/mapyp/img/zoomJezdec.gif';
            jezdec.alt='';
            jezdec.title='';
            barBox.appendChild(barStin);
            barBox.appendChild(bar);
            barBox.appendChild(jezdec);
            box.appendChild(barBox);
            zoomBox.appendChild(box);
            gEl('obal').appendChild(zoomBox)
        };
        this.makeLevels=function(){
            var obj=parent[name];
            var where=gEl('lineBox');
            for(var i=0;i<17;i++){
                var level=cEl('span');
                level.className='level';
                level.style.top=((i*4))+'px';
                level.id='lv'+(17-i);
                level.title=17-i;
                if((browser.klient=='ie')&&(browser.version!=1)){
                    level.style.backgroundColor='white';
                    level.style.filter='alpha(opacity=0)'
                }
                main.activeItem(level,'click',obj.skip);
                main.activeItem(level,'mousedown',dom.stopEvent);
                where.insertBefore(level,where.lastChild)
            }
        };
        this.byButton=function(e){
            var obj=parent[name];
            if(!obj){
                dom.stopEvent(e);
                dom.cancelDef(e);
                return
            }
            if(!parent.allow){
                return
            }
            if(e.type=='click'){
                var trg=dom.getTarget(e);
                dom.stopEvent(e);
                dom.cancelDef(e)
            }
            else{
                var trg=arguments[0]
            }
            var go=true;
            obj.tmp.oldZoom=main.map.scale;
            if((trg.id=='minus')&&(main.map.scale>=parent.zoomLimit.min[0])){
                var zoom=obj.getAllow(main.map.scale,-1);
                main.map.scale=zoom?zoom:main.map.scale
            }
            else if((trg.id=='plus')&&(main.map.scale<=parent.zoomLimit.max[0])){
                var zoom=obj.getAllow(main.map.scale,1);
                main.map.scale=zoom?zoom:main.map.scale
            }
            else{
                go=false
            }
            if(go){
                obj.end()
            }
        };
        this.skip=function(e){
            var obj=parent[name];
            if(!parent.allow){
                return
            }
            dom.stopEvent(e);
            trg=dom.getTarget(e);
            scale=parseInt(trg.title);
            obj.tmp.oldZoom=main.map.scale;
            num=obj.checkAvailable(scale);
            if(num>parent.zoomLimit.max[0]){
                scale=parent.zoomLimit.max[0]
            }
            else if(num<parent.zoomLimit.min[0]){
                scale=parent.zoomLimit.min[0]
            }
            else{
                scale=num
            }
            main.map.scale=scale;
            obj.end()
        };
        this.byMouse=function(e){
            var obj=parent[name];
            if(!obj){
                dom.stopEvent(e);
                dom.cancelDef(e);
                return
            }
            if(!engine.controls.allow){
                return
            }
            e=dom.getEvent(e);
            obj.tmp.startY=e.clientY;
            obj.tmp.foldY=parseInt(gEl('jezdec').style.top);
            obj.tmp.oldZoom=main.map.scale;
            parent.zoomLimit=obj.setLimit();
            dom.addEvent(gEl('port'),'mousemove',obj.moveBar,false);
            dom.addEvent(document,'mouseup',obj.end,false);
            dom.stopEvent(e);
            dom.cancelDef(e)
        };
        this.moveBar=function(e){
            var obj=parent[name];
            obj.tmp.zoomMouse=true;
            e=dom.getEvent(e);
            var moveY=e.clientY;
            var newY=moveY-obj.tmp.startY;
            if((obj.tmp.foldY+newY)<parent.zoomLimit.max[1]){
                gEl('jezdec').style.top=parent.zoomLimit.max[1]+'px';
                gEl('jezdecStin').style.top=(parent.zoomLimit.max[1]-6)+'px'
            }
            else if((obj.tmp.foldY+newY)>parent.zoomLimit.min[1]){
                gEl('jezdec').style.top=parent.zoomLimit.min[1]+'px';
                gEl('jezdecStin').style.top=(parent.zoomLimit.min[1]-6)+'px'
            }
            else{
                gEl('jezdec').style.top=(obj.tmp.foldY+newY)+'px';
                gEl('jezdecStin').style.top=(obj.tmp.foldY+newY-6)+'px'
            }
            dom.stopEvent(e);
            dom.cancelDef(e)
        };
        this.end=function(e){
            var obj=parent[name];
            if(obj.tmp.zoomMouse){
                var pos=parseInt(gEl('jezdec').style.top)+6;
                var scale=Math.round((17-(pos/4)))+1;
                num=obj.checkAvailable(scale);
                scale=num?num:main.map.scale;
                dom.removeEvent(gEl('port'),'mousemove',obj.moveBar,false);
                dom.removeEvent(document,'mouseup',obj.end,false);
                main.map.scale=scale?scale:main.map.scale
            }
            obj.setBar(main.map.scale);
            obj.tmp.zoomMouse=false;
            if(obj.tmp.oldZoom!=main.map.scale){
                obj.holdPosition(obj.tmp.oldZoom);
                main.map.update()
            }
        };
        this.holdPosition=function(oldZoom){
            var obj=parent[name];
            if(main.vizitka.bubleFold.opened){
                var point=new Object();
                var limit=main.vizitka.setResultBublePos();
                point.x=main.vizitka.bubleFold.x;
                point.y=main.vizitka.bubleFold.y;
                var refX=point.x>>(main.BPT-oldZoom);
                var refY=point.y>>(main.BPT-oldZoom);
                if((refY>limit.top)||(refY<limit.bottom)||(refX>limit.right)||(refX<limit.left)){
                    return
                }
                var exp=engine.map.scale-oldZoom>0?engine.map.scale-oldZoom:oldZoom-engine.map.scale;
                var dir=(engine.map.scale-oldZoom)>0?1:-1;
                if(dir>0){
                    for(var i=0;i<exp;i++){
                        var vX=(main.center.x-point.x);
                        var vY=(main.center.y-point.y);
                        var driftX=parseInt(vX/2);
                        var driftY=parseInt(vY/2);
                        main.center.x=main.center.x-driftX;
                        main.center.y=main.center.y-driftY
                    }
                }
                else if(dir<0){
                    for(var i=0;i<exp;i++){
                        var vX=(main.center.x-point.x);
                        var vY=(main.center.y-point.y);
                        var driftX=parseInt(vX);
                        var driftY=parseInt(vY);
                        main.center.x=main.center.x+driftX;
                        main.center.y=main.center.y+driftY
                    }
                }
                else{
                    return
                }
            }
            else{
                return
            }
        };
        this.setBar=function(sc){
            if(gEl('line')){
                var obj=parent[name];
                var line=gEl('line');
                var bar=gEl('jezdec');
                var stin=gEl('jezdecStin');
                var lineHeight=line.offsetHeight;
                var barHeight=bar.offsetHeight;
                var drift=sc==1?3:2;
                var barPosition=((lineHeight/(17))*(17-sc))-drift;
                bar.style.top=(Math.round(barPosition))+'px';
                stin.style.top=(Math.round(barPosition)-6)+'px';
                gEl('jezdec').title=sc
            }
        };
        this.getAllow=function(st,dir){
            var obj=parent[name];
            if(arguments[2]){
                var inp=arguments[2]
            }
            else{
                var inp=parent.zoomAllow
            }
            var output=null;
            var i=st-1;
            while((i<inp.length)&&(i>=0)){
                i=i+(dir*1);
                if(inp[i]){
                    var output=i+1;
                    break
                }
            }
            return output
        };
        this.setLimit=function(){
            var obj=parent[name];
            if(arguments[0]){
                var inp=arguments[0]
            }
            else{
                var inp=parent.zoomAllow
            }
            var max=null;
            var min=null;
            for(var i=0;i<inp.length;i++){
                if(max&&min){
                    break
                }
                else{
                    if(!min&&inp[i]){
                        min=i+1
                    }
                    if(!max&&inp[inp.length-i]){
                        max=(inp.length-i)+1
                    }
                }
            }
            maxPos=-2+((68)-(max*4));
            minPos=-3+(68-(min*4));
            return{
                min:[min,minPos],max:[max,maxPos]
            }
        };
        this.checkAvailable=function(num){
            var obj=parent[name];
            if(arguments[1]){
                var zoomField=arguments[1];
                obj.tmp.oldZoom=engine.map.scale
            }
            else{
                var zoomField=parent.zoomAllow
            }
            if(obj.tmp.oldZoom>num){
                if(zoomField[num-1]){
                    main.map.scale=num
                }
                else{
                    var num=!arguments[1]?obj.getAllow(num,1):obj.getAllow(num,1,zoomField)
                }
            }
            else if(obj.tmp.oldZoom<num){
                if(zoomField[num-1]){
                    main.map.scale=num
                }
                else{
                    var num=!arguments[1]?obj.getAllow(num,-1):obj.getAllow(num,-1,zoomField)
                }
            }
            return num
        }
    };
    function KbdObj(main,parent,name){
        this.inited=0;
        this.init=function(){
            var obj=parent[name];
            if(!obj.inited){
                dom.addEvent(document,'keypress',obj.readKey,false,1);
                dom.addEvent(document,'keydown',obj.readKey,false,1);
                obj.inited=1
            }
            return 0
        };
        this.disable=function(){
            var obj=parent[name];
            if(obj.inited){
                dom.removeEvent(document,'keypress',obj.readKey,false);
                dom.removeEvent(document,'keydown',obj.readKey,false);
                obj.inited=0
            }
            return 0
        };
        this.readKey=function(e){
            var obj=parent[name];
            var e=dom.getEvent(e);
            var trg=dom.getTarget(e);
            if(main.status.get('rulerActive')){
                return
            }
            var test=browser.klient=='ie'?trg.nodeName.toLowerCase():e.target.nodeName.toLowerCase();
            if((test=='input')||(test=='select')||(test=='option')||(test=='textarea')){
                return
            }
            var key=e.keyCode?e.keyCode:e.which;
            var num=(!parent.kbdAllow&&key!=27)?0:key;
            if((e.type=='keypress')&&!e.altKey&&!e.ctrlKey&&!e.shiftKey){
                obj.goZoom(num)
            }
            else{
                obj.goDir(num)
            }
            dom.stopEvent(e);
            return 0
        };
        this.goZoom=function(param){
            var obj=parent[name];
            if(main.status.get('rulerActive')){
                return
            }
            switch(param){
                case 43:obj.disable();
                var m=obj.doFakeEvent('plus');
                if(parent.zoom.byButton){
                    parent.zoom.byButton(m)
                }
                obj.init();
                break;
                case 45:obj.disable();
                var m=obj.doFakeEvent('minus');
                if(parent.zoom.byButton){
                    parent.zoom.byButton(m)
                }
                obj.init();
                break;
                default:break
            }
            return 0
        };
        this.goDir=function(param){
            var obj=parent[name];
            switch(param){
                case 38:obj.disable();
                var m=obj.doFakeEvent('ss');
                parent.move.setDirection(m);
                break;
                case 40:obj.disable();
                var m=obj.doFakeEvent('jj');
                parent.move.setDirection(m);
                break;
                case 37:obj.disable();
                var m=obj.doFakeEvent('zz');
                parent.move.setDirection(m);
                break;
                case 39:obj.disable();
                var m=obj.doFakeEvent('vv');
                parent.move.setDirection(m);
                break;
                default:break
            }
            return 0
        };
        this.doFakeEvent=function(param){
            typ='kbd';
            ids=param;
            return{
                type:typ,id:ids
            }
        }
    };
    function ClickActionObj(main,parent,name){
        this.init=function(){
            var obj=parent[name];
            dom.addEvent(main.map.el,'mouseup',obj.go,false,1);
            dom.addEvent(main.map.el,'dblclick',obj.go,false,1)
        };
        this.go=function(e){
            var obj=parent[name];
            var e=dom.getEvent(e);
            if(main.status.get('rulerActive')){
                return
            }
            if(e.button==parent.button.select){
                if(e.type=='mouseup'){
                    var zmDir=-1
                }
            }
            else if(e.type=='dblclick'){
                var zmDir=1
            }
            else{
                if(!parent.tmp.clickStop){
                    main.status.set('changeCenter',false)
                }
                return
            }
            var port=main.port.getSize();
            var clickX=e.clientX-main.getPosition(gEl('port'),'left')+main.scrollPos()['x'];
            var clickY=e.clientY-main.getPosition(gEl('port'),'top')+main.scrollPos()['y'];
            var driftX=Math.round(port.width/2)-((main.map.center.x))-main.map.drift/2;
            var driftY=(main.map.height-main.map.center.y)+Math.round(port.height/2);
            var xx=(clickX-driftX)+main.map.tileDrift.x;
            var yy=(driftY-clickY)+main.map.tileDrift.y;
            var x=(xx)<<(main.BPT-main.map.scale);
            var y=(yy)<<(main.BPT-main.map.scale);
            main.map.scale=parent.zoom.getAllow(main.map.scale,zmDir)?parent.zoom.getAllow(main.map.scale,zmDir):main.map.scale;
            if(gEl('line')){
                parent.zoom.setBar(main.map.scale)
            }
            main.center.x=x;
            main.center.y=y;
            main.map.update()
        };
        this.nothing=function(e){
            dom.stopEvent(e);
            return
        }
    };
    function GpsObj(main,parent,name){
        this.trg=null;
        this.box=null;
        this.x=null;
        this.y=null,this.places=new Object();
        this.run=new Object();
        this.title=null;
        this.active=0;
        this.limits=new Object();
        this.height=0;
        this.tmp=new Object();
        this.clBox=null,this.init=function(){
            var obj=parent[name];
            obj.initButton();
            obj.box=obj.makeBox();
            obj.box.className='gpsBox';
            obj.box.style.display='none';
            var txt=obj.box.getElementsByTagName('span');
            for(var i=0;i<txt.length;i++){
                var co=cTxt(' xxx ');
                txt[i].appendChild(co)
            }
            gEl('port').appendChild(obj.box);
            obj.places.lot=gEl('dg').getElementsByTagName('span')[1].firstChild;
            obj.places.lat=gEl('dg').getElementsByTagName('span')[0].firstChild;
            obj.places.north=gEl('utm').getElementsByTagName('span')[1].firstChild;
            obj.places.east=gEl('utm').getElementsByTagName('span')[2].firstChild;
            obj.places.zone=gEl('utm').getElementsByTagName('span')[0].firstChild;
            obj.limits.top=parseInt('3800000',16);
            obj.limits.left=parseInt('3800000',16);
            obj.limits.bottom=parseInt('d000000',16);
            obj.limits.right=parseInt('c800000',16)
        };
        this.initButton=function(){
            var obj=parent[name];
            var button=parent.makeButton(gEl('nextObal'),'gps_off.gif','gpsButt','gps');
            main.activeItem(button,'click',obj.buttonAction);
            main.activeItem(button,'mousedown',dom.stopEvent)
        };
        this.makeBox=function(){
            var box=cEl('div');
            box.id='gpsBox';
            var wgsLine=cEl('p');
            wgsLine.id='dg';
            var utmLine=cEl('p');
            utmLine.id='utm';
            var objLine=cEl('p');
            objLine.id='obj';
            var wgsCnt='Loc:<span></span>N, <span></span>E';
            var utmCnt='UTM zone <span></span>: N <span></span>, E <span></span>';
            utmLine.style.display='none';
            var objCnt='<strong>Objekt: </strong><span></span>';
            wgsLine.innerHTML=wgsCnt;
            utmLine.innerHTML=utmCnt;
            objLine.innerHTML=objCnt;
            box.appendChild(wgsLine);
            box.appendChild(utmLine);
            box.appendChild(objLine);
            return box
        };
        this.makeClips=function(){
            var obj=parent[name];
            var clBox=cEl('div');
            clBox.className='gpsClBox';
            var cnt=cEl('p');
            cnt.appendChild(cTxt('loc: N/A'));
            var inf=cEl('p');
            inf.className='hlp';
            clBox.appendChild(cnt);
            clBox.appendChild(inf);
            inf.appendChild(cTxt('Kliknutím v mapě vložíte GPS souřadnice pro další použití.'));
            obj.clBox=clBox;
            gEl('port').appendChild(obj.clBox);
            dom.addEvent(gEl('port'),'click',obj.fillClips,false);
            dom.addEvent(obj.clBox,'mousedown',dom.stopEvent,false);
            dom.addEvent(obj.clBox,'click',dom.stopEvent,false);
            dom.addEvent(obj.clBox,'mousemove',dom.stopEvent,false)
        };
        this.removeClips=function(){
            var obj=parent[name];
            dom.removeEvent(gEl('port'),'click',obj.fillClips,false);
            dom.removeEvent(obj.clBox,'mousedown',dom.stopEvent,false);
            dom.removeEvent(obj.clBox,'click',dom.stopEvent,false);
            dom.removeEvent(obj.clBox,'mousemove',dom.stopEvent,false);
            gEl('port').removeChild(obj.clBox);
            obj.clBox=null
        };
        this.fillClips=function(e){
            var obj=parent[name];
            obj.x=e.clientX-(main.getPosition(gEl('port'),'left')+main.scrollPos()['x']);
            obj.y=e.clientY-(main.getPosition(gEl('port'),'top')+main.scrollPos()['y']);
            var limitLeft=parseInt(main.map.el.style.left);
            var limitRight=parseInt(main.map.el.style.left)+main.map.width;
            var limitTop=parseInt(main.map.el.style.top);
            var limitBottom=parseInt(main.map.el.style.top)+main.map.height;
            obj.run.x=(obj.x>limitLeft)&&(obj.x<limitRight)?true:false;
            obj.run.y=(obj.y>limitTop)&&(obj.y<limitBottom)?true:false;
            var port=main.port.getSize();
            var driftX=Math.round(port.width/2)-((main.map.realCenter.x)+main.map.drift);
            var driftY=(engine.map.realCenter.y)+Math.round(port.height/2);
            var x=(obj.x-driftX);
            var y=(driftY-obj.y);
            var realX=x<<(main.BPT-main.map.scale);
            var realY=y<<(main.BPT-main.map.scale);
            lim=obj.limits;
            if((realX<lim.left)||(realX>lim.right)||(realY>lim.bottom)||(realY<lim.top)){
                return
            }
            ver=(realY)*Math.pow(2,-5)+1300000;
            hor=(realX)*Math.pow(2,-5)+(-3700000);
            var coord=obj.calc(ver,hor);
            var trg=obj.clBox.getElementsByTagName('p')[0];
            trg.innerHTML='Loc:<span>'+coord.lat.locNum+'</span>N, <span>'+coord.lot.locNum+'</span>'+coord.lot.locStr
        };
        this.buttonAction=function(e){
            var obj=parent[name];
            if(e.type!='fake'){
                dom.stopEvent(e)
            }
            if(!obj.tmp.gpsRun){
                gEl('port').appendChild(obj.box);
                obj.go();
                gEl('gpsButt').src='http://1.im.cz/mapyp/img/gps_on.gif';
                obj.tmp.gpsRun=1;
                main.cookie.set('gps','1')
            }
            else{
                obj.end();
                obj.tmp.gpsRun=0;
                gEl('gpsButt').src='http://1.im.cz/mapyp/img/gps_off.gif';
                main.cookie.set('gps','0')
            }
        };
        this.go=function(){
            var obj=parent[name];
            obj.trg=gEl('port');
            obj.active=1;
            dom.addEvent(obj.trg,'mousemove',obj.move,false);
            obj.makeClips()
        };
        this.end=function(){
            var obj=parent[name];
            window.clearTimeout(obj.tmp.gpsObjTimer);
            dom.removeEvent(obj.trg,'mousemove',obj.move,false);
            obj.active=0;
            obj.box.style.display='none';
            obj.removeClips()
        };
        this.move=function(e){
            e=dom.getEvent(e);
            var obj=parent[name];
            obj.box.style.visibility='hidden';
            obj.x=e.clientX-(main.getPosition(gEl('port'),'left')+main.scrollPos()['x']);
            obj.y=e.clientY-(main.getPosition(gEl('port'),'top')+main.scrollPos()['y']);
            var limitLeft=parseInt(main.map.el.style.left);
            var limitRight=parseInt(main.map.el.style.left)+main.map.width;
            var limitTop=parseInt(main.map.el.style.top);
            var limitBottom=parseInt(main.map.el.style.top)+main.map.height;
            obj.run.x=(obj.x>limitLeft)&&(obj.x<limitRight)?true:false;
            obj.run.y=(obj.y>limitTop)&&(obj.y<limitBottom)?true:false;
            obj.box.style.display='none';
            window.clearTimeout(obj.tmp.gpsObjTimer);
            obj.box.style.top=null;
            obj.box.style.left=null;
            obj.box.style.right=null;
            obj.box.style.bottom=null;
            if((obj.box.style.display!='block')&&!main.controls.moveMouse.mouseMoving){
                obj.tmp.gpsObjTimer=window.setTimeout('engine.controls.gps.show()',300)
            }
            if(!obj.run.x||!obj.run.y||main.controls.moveMouse.mouseMoving){
                window.clearTimeout(obj.tmp.gpsObjTimer)
            }
        };
        this.show=function(){
            var obj=parent[name];
            if(main.controls.moveMouse.mouseMoving){
                return
            }
            var box=obj.box;
            var port=main.port.getSize();
            var driftX=Math.round(port.width/2)-((main.map.realCenter.x)+main.map.drift);
            var driftY=(engine.map.realCenter.y)+Math.round(port.height/2);
            var x=(obj.x-driftX);
            var y=(driftY-obj.y);
            var realX=x<<(main.BPT-main.map.scale);
            var realY=y<<(main.BPT-main.map.scale);
            lim=obj.limits;
            if((realX<lim.left)||(realX>lim.right)||(realY>lim.bottom)||(realY<lim.top)){
                return
            }
            ver=(realY)*Math.pow(2,-5)+1300000;
            hor=(realX)*Math.pow(2,-5)+(-3700000);
            var coord=obj.calc(ver,hor);
            if(obj.x<(port.width+2-main.map.drift)){
                box.style.display='block';
                box.style.visibility='hidden';
                gEl('obj').parentNode.style.height=null;
                obj.places.zone.nodeValue=coord.zone;
                obj.places.north.nodeValue=Math.round(coord.north);
                obj.places.east.nodeValue=Math.round(coord.east);
                var wgsCnt='Loc:<span>'+coord.lat.locNum+'</span>N, <span>'+coord.lot.locNum+'</span>'+coord.lot.locStr;
                gEl('dg').innerHTML=wgsCnt;
                main.setElPosition(obj,box,obj.x,obj.y);
                box.style.zIndex=99;
                obj.height=parseInt(box.style.height);
                window.setTimeout("gEl('gpsBox').style.visibility = 'visible'",5)
            }
        };
        this.removeTitle=function(e){
            var obj=parent[name];
            if(obj.active){
                var trg=dom.getTarget(e);
                obj.title=trg.title;
                trg.title='';
                gEl('obj').getElementsByTagName('span')[0].innerHTML=obj.title;
                gEl('obj').parentNode.style.height=(obj.height+20)+'px';
                gEl('obj').style.display='block'
            }
        };
        this.repairTitle=function(e){
            var obj=parent[name];
            if(obj.active){
                var trg=dom.getTarget(e);
                trg.title=obj.title;
                obj.title='';
                gEl('obj').getElementsByTagName('span')[0].innerHTML='';
                gEl('obj').parentNode.style.height=obj.height?(obj.height-20)+'px':obj.height;
                gEl('obj').style.display='none'
            }
        };
        this.calc=function(sever,vychod){
            var obj=parent[name];
            function deg2rad(x){
                x=x*pi/180;
                return x
            };
            function rad2deg(x){
                x=x*180/pi;
                return x
            };
            var pi=Math.PI;
            var units=1;
            var k=0.9996;
            var a=6378137;
            var f=1/298.257223563;
            var b=a*(1-f);
            var e2=(a*a-b*b)/(a*a);
            var e=Math.sqrt(e2);
            var ei2=(a*a-b*b)/(b*b);
            var ei=Math.sqrt(ei2);
            var n=(a-b)/(a+b);
            var G=a*(1-n)*(1-n*n)*(1+(9/4)*n*n+(255/64)*Math.pow(n,4))*(pi/180);
            var north=(sever-0)*units;
            var east=(vychod-500000)*units;
            var m=north/k;
            var sigma=(m*pi)/(180*G);
            var footlat=sigma+((3*n/2)-(27*Math.pow(n,3)/32))*Math.sin(2*sigma)+((21*n*n/16)-(55*Math.pow(n,4)/32))*Math.sin(4*sigma)+(151*Math.pow(n,3)/96)*Math.sin(6*sigma)+(1097*Math.pow(n,4)/512)*Math.sin(8*sigma);
            var rho=a*(1-e2)/Math.pow(1-(e2*Math.sin(footlat)*Math.sin(footlat)),(3/2));
            var nu=a/Math.sqrt(1-(e2*Math.sin(footlat)*Math.sin(footlat)));
            var psi=nu/rho;
            var t=Math.tan(footlat);
            var x=east/(k*nu);
            var laterm1=(t/(k*rho))*(east*x/2);
            var laterm2=(t/(k*rho))*(east*Math.pow(x,3)/24)*(-4*psi*psi+9*psi*(1-t*t)+12*t*t);
            var laterm3=(t/(k*rho))*(east*Math.pow(x,5)/720)*(8*Math.pow(psi,4)*(11-24*t*t)-12*Math.pow(psi,3)*(21-71*t*t)+15*psi*psi*(15-98*t*t+15*Math.pow(t,4))+180*psi*(5*t*t-3*Math.pow(t,4))+360*Math.pow(t,4));
            var laterm4=(t/(k*rho))*(east*Math.pow(x,7)/40320)*(1385+3633*t*t+4095*Math.pow(t,4)+1575*Math.pow(t,6));
            var latrad=footlat-laterm1+laterm2-laterm3+laterm4;
            var lat=obj.resToString(rad2deg(latrad),'lat');
            var seclat=1/Math.cos(footlat);
            var loterm1=x*seclat;
            var loterm2=(Math.pow(x,3)/6)*seclat*(psi+2*t*t);
            var loterm3=(Math.pow(x,5)/120)*seclat*(-4*Math.pow(psi,3)*(1-6*t*t)+psi*psi*(9-68*t*t)+72*psi*t*t+24*Math.pow(t,4));
            var loterm4=(Math.pow(x,7)/5040)*seclat*(61+662*t*t+1320*Math.pow(t,4)+720*Math.pow(t,6));
            var w=loterm1-loterm2+loterm3-loterm4;
            var longrad=deg2rad(15)+w;
            var lot=obj.resToString(rad2deg(longrad),'lot');
            var utm=obj.calcToUTM(latrad,longrad);
            return{
                lat:lat,lot:lot,zone:utm.zone,east:utm.east,north:utm.north
            }
        };
        this.calcToUTM=function(la,lo){
            function deg2rad(num){
                var num=num*pi/180;
                return num
            };
            function rad2deg(num2){
                var num2=num2*180/pi;
                return num2
            };
            function roundoff(x,y){
                var x=parseFloat(x);
                var y=parseFloat(y);
                x=Math.round(x*Math.pow(10,y))/Math.pow(10,y);
                return x
            };
            var pi=Math.PI;
            var units=1;
            var distsize=3;
            var latrad=la;
            var lonrad=lo;
            var latddd=rad2deg(la);
            var londdd=rad2deg(lo);
            var zone=Math.round((londdd+183)/6);
            var k=0.9996;
            var a=6378137;
            var f=1/298.257223563;
            var b=a*(1-f);
            var e2=(a*a-b*b)/(a*a);
            var e=Math.sqrt(e2);
            var ei2=(a*a-b*b)/(b*b);
            var ei=Math.sqrt(ei2);
            var n=(a-b)/(a+b);
            var G=a*(1-n)*(1-n*n)*(1+(9/4)*n*n+(255/64)*Math.pow(n,4))*(pi/180);
            var w=londdd-parseFloat(zone*6-183);
            w=deg2rad(w);
            var t=Math.tan(latrad);
            var rho=a*(1-e2)/Math.pow(1-(e2*Math.sin(latrad)*Math.sin(latrad)),(3/2));
            var nu=a/Math.sqrt(1-(e2*Math.sin(latrad)*Math.sin(latrad)));
            var psi=nu/rho;
            var coslat=Math.cos(latrad);
            var sinlat=Math.sin(latrad);
            var A0=1-(e2/4)-(3*e2*e2/64)-(5*Math.pow(e2,3)/256);
            var A2=(3/8)*(e2+(e2*e2/4)+(15*Math.pow(e2,3)/128));
            var A4=(15/256)*(e2*e2+(3*Math.pow(e2,3)/4));
            var A6=35*Math.pow(e2,3)/3072;
            var m=a*((A0*latrad)-(A2*Math.sin(2*latrad))+(A4*Math.sin(4*latrad))-(A6*Math.sin(6*latrad)));
            var eterm1=(w*w/6)*coslat*coslat*(psi-t*t);
            var eterm2=(Math.pow(w,4)/120)*Math.pow(coslat,4)*(4*Math.pow(psi,3)*(1-6*t*t)+psi*psi*(1+8*t*t)-psi*2*t*t+Math.pow(t,4));
            var eterm3=(Math.pow(w,6)/5040)*Math.pow(coslat,6)*(61-479*t*t+179*Math.pow(t,4)-Math.pow(t,6));
            var dE=k*nu*w*coslat*(1+eterm1+eterm2+eterm3);
            var east=roundoff(parseFloat(500000)+(dE/units),distsize);
            var nterm1=(w*w/2)*nu*sinlat*coslat;
            var nterm2=(Math.pow(w,4)/24)*nu*sinlat*Math.pow(coslat,3)*(4*psi*psi+psi-t*t);
            var nterm3=(Math.pow(w,6)/720)*nu*sinlat*Math.pow(coslat,5)*(8*Math.pow(psi,4)*(11-24*t*t)-28*Math.pow(psi,3)*(1-6*t*t)+psi*psi*(1-32*t*t)-psi*2*t*t+Math.pow(t,4));
            var nterm4=(Math.pow(w,8)/40320)*nu*sinlat*Math.pow(coslat,7)*(1385-3111*t*t+543*Math.pow(t,4)-Math.pow(t,6));
            var dN=k*(m+nterm1+nterm2+nterm3+nterm4);
            var north=roundoff(parseFloat(0)+(dN/units),distsize);
            return{
                zone:zone,east:east,north:north
            }
        };
        this.resToString=function(num,dir){
            function roundoff(x,y){
                x=parseFloat(x);
                y=parseFloat(y);
                x=Math.round(x*Math.pow(10,y))/Math.pow(10,y);
                return x
            }
            var secsize=2;
            var y=Math.abs(parseFloat(num));
            degree=parseInt(y+1)-1;
            var x=y-degree;
            minut=parseInt(x*60+1)-1;
            second=((x*60)-minut)*60;
            second=roundoff(second,secsize);
            if(second==60){
                second="0";
                minut=minut*1+1;
                if(minut==60){
                    minut="0";
                    degree=degree*1+1
                }
            }
            if(num<0){
                if(degree!=0){
                    degree=-degree
                }
                else if(minut!=0){
                    minut=-minut
                }
                else{
                    second=-second
                }
            }
            if(dir=='lot'){
                var str=degree<0?'W':'E';
                degree=degree<0?Math.abs(degree):degree
            }
            else{
                var str='N'
            }
            value=degree+"\260 "+minut+"\' "+second+'\" ';
            return{
                locNum:value,locStr:str
            }
        }
    };
    function SelectionObj(main,parent,name){
        this.posX=0;
        this.posY=0;
        this.limits=new Object();
        this.init=function(){
            var obj=parent[name];
            var box=cEl('div');
            box.id='slBox';
            obj.box=box
        };
        this.startSelect=function(e){
            var obj=parent[name];
            var e=dom.getEvent(e);
            if((e.button!=main.controls.button.move)||main.status.get('rulerActive')){
                return
            }
            if(!e.ctrlKey&&(browser.klient!='saf')){
                return
            }
            else if((browser.klient=='saf')&&!e.metaKey){
                return
            }
            parent.tmp.clickStop=true;
            obj.limits.top=main.getPosition(gEl('port'),'top');
            obj.limits.left=main.getPosition(gEl('port'),'left');
            obj.limits.bott=main.getPosition(gEl('port'),'top')+main.port.getSize().height;
            obj.limits.right=main.getPosition(gEl('port'),'left')+main.port.getSize().width-main.map.drift;
            var startX=e.clientX-main.getPosition(gEl('port'),'left')+main.scrollPos()['x'];
            var startY=e.clientY-main.getPosition(gEl('port'),'top')+main.scrollPos()['y'];
            gEl('port').appendChild(obj.box);
            obj.box.style.width=obj.box.style.height='1px';
            obj.box.style.display='block';
            obj.box.style.overflow='hidden';
            obj.box.style.top=startY+'px';
            obj.box.style.left=startX+'px';
            obj.refX=startX;
            obj.refY=startY;
            dom.addEvent(document,'mousemove',obj.makeSelect,false);
            dom.addEvent(document,'mousemove',dom.stopEvent,false);
            dom.addEvent(document,'mouseup',obj.endSelect,false);
            dom.addEvent(document,'keypress',obj.keyBreak,false);
            dom.addEvent(document,'keydown',obj.keyBreak,false);
            dom.stopEvent(e)
        };
        this.makeSelect=function(e){
            e=dom.getEvent(e);
            var obj=parent[name];
            var clientX=e.clientX+main.scrollPos()['x'];
            var clientY=e.clientY+main.scrollPos()['y'];
            if((clientX>obj.limits.left)&&(clientX<obj.limits.right)&&(clientY>obj.limits.top)&&(clientY<obj.limits.bott)){
                var moveX=clientX-main.getPosition(gEl('port'),'left');
                var moveY=clientY-main.getPosition(gEl('port'),'top');
                var dirY='';
                if((moveX>=obj.refX)){
                    var mLeft=obj.refX;
                    var mWidth=moveX-obj.refX;
                    dirX='right'
                }
                else{
                    var mLeft=obj.refX-(obj.refX-moveX);
                    var mWidth=obj.refX-moveX;
                    dirX='left'
                }
                if(moveY>=obj.refY){
                    var mTop=obj.refY;
                    var mHeight=moveY-obj.refY;
                    dirY='bott'
                }
                else{
                    var mTop=obj.refY-(obj.refY-moveY);
                    var mHeight=obj.refY-moveY;
                    dirY='top'
                }
                obj.box.style.top=mTop+'px';
                obj.box.style.left=mLeft+'px';
                obj.box.style.width=mWidth+'px';
                obj.box.style.height=mHeight+'px'
            }
            dom.stopEvent(e)
        };
        this.endSelect=function(e){
            e=dom.getEvent(e);
            var obj=parent[name];
            dom.removeEvent(document,'mouseup',obj.endSelect,false);
            dom.removeEvent(document,'mousemove',obj.makeSelect,false);
            dom.removeEvent(document,'mousemove',dom.stopEvent,false);
            var result=new Object();
            result.left=parseInt(obj.box.style.left);
            result.top=parseInt(obj.box.style.top);
            result.width=parseInt(obj.box.style.width);
            result.height=parseInt(obj.box.style.height);
            obj.box.style.width=obj.box.style.height='1px';
            obj.box.style.display='none';
            dom.removeEvent(document,'keypress',obj.keyBreak,false);
            dom.removeEvent(document,'keydown',obj.keyBreak,false);
            parent.tmp.clickStop=false;
            obj.calc(result)
        };
        this.cancel=function(){
            var obj=parent[name];
            var box=obj.box;
            if(box.style.dispay!='none'){
                dom.removeEvent(document,'mouseup',obj.endSelect,false);
                dom.removeEvent(document,'mousemove',obj.makeSelect,false);
                dom.removeEvent(document,'keypress',obj.keyBreak,false);
                dom.removeEvent(document,'keydown',obj.keyBreak,false);
                obj.box.style.dispaly='none';
                obj.box.style.width=obj.box.style.height='1px';
                obj.box.style.display='none'
            }
        };
        this.calc=function(param){
            var port=main.port.getSize();
            var obj=parent[name];
            var driftX=Math.round(port.width/2)-((main.map.center.x)+main.map.drift);
            var driftY=(main.map.height-main.map.center.y)+Math.round(port.height/2);
            var x=(param.left-driftX)+main.map.tileDrift.x;
            var y=(driftY-param.top)+main.map.tileDrift.y;
            var xSize=parseInt(port.width/param.width);
            var ySize=parseInt(port.height/param.height);
            if(xSize>ySize){
                var res=parseInt(Math.log(xSize)/Math.log(2))
            }
            else if(xSize<ySize){
                var res=parseInt(Math.log(ySize)/Math.log(2))
            }
            else{
                var res=parseInt(Math.log(xSize)/Math.log(2))
            }
            var newX=(x+Math.round(param.width/2))<<(engine.BPT-main.map.scale);
            var newY=(y-Math.round(param.height/2))<<(engine.BPT-main.map.scale);
            obj.reSetMap(newX,newY,res)
        };
        this.reSetMap=function(x,y,num){
            var obj=parent[name];
            main.center.x=x;
            main.center.y=y;
            var scale=main.map.scale+num;
            scale=scale>main.controls.zoomLimit.max[0]?main.controls.zoomLimit.max[0]:scale;
            scale=main.controls.zoomAllow[scale-1]?scale:main.map.scale;
            main.map.scale=scale;
            main.controls.zoom.setBar(scale);
            main.map.update()
        };
        this.keyBreak=function(e){
            var obj=parent[name];
            var e=dom.getEvent(e);
            var num=e.keyCode?e.keyCode:e.which;
            if(num==27){
                obj.cancel()
            }
        }
    };
    this.makeButton=function(nd,pic,idsButt,idsBox){
        var obj=parent[name];
        var buttBox=cEl('div');
        buttBox.id=idsBox;
        var box=cEl('div');
        var butt=cEl('img');
        butt.width=74;
        butt.height=18;
        butt.src='http://1.im.cz/mapyp/img/'+pic;
        butt.id=idsButt;
        butt.alt='';
        box.appendChild(butt);
        buttBox.appendChild(box);
        nd.appendChild(buttBox);
        return buttBox
    };
    this.makeExtend=function(){
        var obj=mainObj[name];
        if(config.mapControls.extend.length>0){
            var nextObal=cEl('div');
            nextObal.id='nextObal';
            nextObal.className='nextObal';
            gEl('obal').appendChild(nextObal);
            for(i=0;i<config.mapControls.extend.length;i++){
                if(config.mapControls.extend[i]=='gps'){
                    obj.gps.init()
                }
                if(config.mapControls.extend[i]!='gps'){
                    var objName=config.mapControls.extend[i].toLowerCase();
                    obj[objName]=new window[config.mapControls.extend[i]](mainObj,obj,objName);
                    obj[objName].init()
                }
            }
        }
    }

};

function Fullscreen(main,parent,name){
    this.run=false;
    this.button=null;
    this.init=function(){
        var obj=parent[name];
        obj.initButton()
    };
    this.initButton=function(){
        var obj=parent[name];
        obj.button=parent.makeButton(gEl('nextObal'),'full_off.gif','fullButt','fullScr');
        main.activeItem(obj.button,'click',obj.buttonAction);
        main.activeItem(obj.button,'mousedown',dom.stopEvent)
    };
    this.buttonAction=function(){
        var obj=parent[name];
        if(!obj.run){
            obj.button.getElementsByTagName('img')[0].src='http://1.im.cz/mapyp/img/full_on.gif';
            obj.run=true;
            obj.setFull();
            main.cookie.set('fullscreen','1')
        }
        else{
            obj.button.getElementsByTagName('img')[0].src='http://1.im.cz/mapyp/img/full_off.gif';
            obj.run=false;
            obj.setStandard();
            main.cookie.set('fullscreen','0')
        }
    };
    this.setFull=function(){
        var obj=parent[name];
        if(!printFlag){
            gEl('content').style.marginTop='0';
            gEl('port').style.marginRight='6px';
            gEl('submenu').style.display='none';
            gEl('mainNavLine').style.display='none';
            gEl('searchTips').style.display='none';
            gEl('logoMp').style.marginTop='0';
            gEl('logoForm').style.marginTop='0';
            gEl('srchForm').style.marginTop='15px';
            main.port.setSize()
        }
    };
    this.setStandard=function(){
        var obj=parent[name];
        gEl('content').style.marginTop='10px';
        gEl('port').style.marginRight='336px';
        gEl('submenu').style.display='block';
        gEl('mainNavLine').style.display='block';
        gEl('searchTips').style.display='block';
        gEl('logoMp').style.marginTop='15px';
        gEl('logoForm').style.marginTop='8px';
        gEl('srchForm').style.marginTop='0';
        main.port.setSize()
    }

};

function Ruler(main,parent,name){
    this.tmp=new Object();
    this.port=null;
    this.line=null;
    this.ctx=null;
    this.ctx1=null;
    this.canvas=null;
    this.canvasI=null;
    this.show=false;
    this.layer=null;
    this.box=null;
    this.boxSummary=null;
    this.PP_CONST=32;
    this.rulerPoints=null;
    this.tBoxStartX=null;
    this.tBoxStartY=null;
    this.tBoxEndX=null;
    this.tBoxEndY=null;
    this.StartX=0;
    this.StartY=0;
    this.EndX=0;
    this.EndY=0;
    this.init=function(){
        var obj=parent[name];
        try{
            var test=cEl('canvas');
            var ctx=test.getContext("2d");
            test=null;
            var cnvs=true
        }
        catch(e){
            var cnvs=false
        }
        if(browser.klient=='saf'){
            var cnvs=false
        }
        if(((browser.klient=='ie')&&(browser.version>1))||cnvs){
            
        }
        else{
            return
        }
        obj.initButton();
        obj.port=gEl('port')
    };
    this.menuSwitch=function(type){
        var obal=gEl('obal');
        var nds=obal.childNodes;
        for(var i=0;i<nds.length;i++){
            if((nds[i].nodeType==1)&&(nds[i].id!='ruler')){
                nds[i].style.display=type
            }
        }
    };
    this.go=function(){
        var obj=parent[name];
        obj.port.style.cursor='crosshair';
        obj.menuSwitch('none');
        main.status.set('rulerActive',1);
        this.rulerPoints=new Array();
        var summBox=cEl('div');
        summBox.id='summBox';
        summBox.className='gpsClBox';
        summBox.innerHTML='<div id="totalDistance"><span class="stage">Celková délka:</span><span class="unit">0 km</span></div>'+'<input type="button" class="rSR" id="reSetRuler" value="Nové měření" disabled="disabled" />'+' <input type="button" class="rSR" id="endRulerAction" value="Ukončit měření" disabled="disabled" />'+'<p id="titleStages"><strong>Délky jednotlivých úseků:</strong></p>';
        if(main.controls.gps.clBox){
            summBox.style.position='absolute';
            summBox.style.top=(main.controls.gps.clBox.offsetHeight-1)+'px'
        }
        obj.boxSummary=summBox;
        obj.port.appendChild(summBox);
        dom.addEvent(gEl('reSetRuler'),'click',obj.reset,false);
        dom.addEvent(gEl('endRulerAction'),'click',obj.halt,false);
        if(browser.klient=='ie'){
            var l=cEl("v:line");
            l.id="rulerMeter";
            l.filled="Off";
            l.style.position='absolute';
            obj.port.appendChild(l);
            obj.line=l;
            var s=cEl('v:stroke');
            s.weight="2px";
            s.color="#0000CC";
            s.opacity="100%";
            s.fill="Solid";
            s.dashstyle="ShortDot";
            l.appendChild(s);
            var ss=cEl('v:shadow');
            ss.on="True";
            ss.color="#ffffff";
            ss.opacity="100%";
            ss.offset="0,2px";
            l.appendChild(ss);
            obj.layer=main.map.el
        };
        if(browser.klient=='geck'){
            var canvas=cEl("canvas");
            canvas.width='1600';
            canvas.height='1200';
            canvas.style.position='absolute';
            canvas.style.zIndex='9';
            obj.port.appendChild(canvas);
            obj.canvas=canvas;
            var ctx=canvas.getContext("2d");
            ctx.strokeStyle="#0000cc";
            obj.ctx=ctx;
            obj.layer=obj.canvas;
            var canvasI=cEl("canvas");
            canvasI.width='1600';
            canvasI.height='1200';
            canvasI.style.position='absolute';
            canvasI.style.zIndex='8';
            obj.port.appendChild(canvasI);
            obj.canvasI=canvasI;
            var ctxI=canvasI.getContext("2d");
            ctxI.strokeStyle="#0000cc";
            obj.ctxI=ctxI
        };
        dom.addEvent(obj.layer,'click',obj.startMeasure,false)
    };
    this.end=function(){
        var obj=parent[name];
        obj.port.style.cursor='default';
        obj.menuSwitch('block');
        main.status.set('rulerActive',0);
        if(obj.layer!=null){
            dom.removeEvent(obj.layer,'click',obj.startMeasure,false);
            dom.removeEvent(obj.layer,'click',obj.startMeasure,false);
            dom.removeEvent(obj.layer,'mousemove',obj.processMeasure,false);
            if(browser.klient=='ie'){
                dom.removeEvent(obj.line,'mousemove',obj.processMeasure,false)
            }
            dom.removeEvent(obj.layer,'mousedown',obj.endMeasure,false);
            if(browser.klient=='ie'){
                dom.removeEvent(obj.line,'mousedown',obj.endMeasure,false)
            }
            obj.boxSummary.parentNode.removeChild(obj.boxSummary)
        }
        dom.removeEvent(document,'keypress',obj.halt,false);
        dom.removeEvent(document,'keydown',obj.halt,false);
        if(obj.line!=null){
            obj.line.parentNode.removeChild(obj.line);
            var lines=obj.port.getElementsByTagName('line');
            while(lines.length>0){
                obj.port.removeChild(lines[lines.length-1])
            }
        }
        if(obj.canvas!=null){
            obj.canvas.parentNode.removeChild(obj.canvas);
            obj.canvasI.parentNode.removeChild(obj.canvasI)
        }
        obj.line=null;
        obj.ctx=null;
        obj.ctxI=null;
        obj.canvas=null;
        obj.canvasI=null;
        obj.box=null;
        obj.boxSummary=null;
        obj.layer=null;
        obj.rulerPoints=null
    };
    this.reset=function(){
        var obj=parent[name];
        if(browser.klient=='ie'){
            var lines=obj.port.getElementsByTagName('line');
            while(lines.length>0){
                obj.port.removeChild(lines[lines.length-1])
            }
            if(browser.klient=='ie'){
                var l=cEl("v:line");
                l.id="rulerMeter";
                l.filled="Off";
                l.style.position='absolute';
                obj.port.appendChild(l);
                obj.line=l;
                var s=cEl('v:stroke');
                s.weight="2px";
                s.color="#0000CC";
                s.opacity="100%";
                s.fill="Solid";
                s.dashstyle="ShortDot";
                l.appendChild(s);
                var ss=cEl('v:shadow');
                ss.on="True";
                ss.color="#ffffff";
                ss.opacity="100%";
                ss.offset="0,2px";
                l.appendChild(ss);
                obj.layer=main.map.el
            }
        }
        else if(browser.klient=='geck'){
            obj.ctxI.clearRect(0,0,1600,1200)
        }
        var trg=gEl('summBox').getElementsByTagName('p');
        while(trg[1]){
            trg[1].parentNode.removeChild(trg[1])
        }
        gEl('summBox').getElementsByTagName('span')[1].innerHTML='0 km';
        gEl('reSetRuler').disabled=true;
        obj.rulerPoints=new Array();
        dom.addEvent(obj.layer,'click',obj.startMeasure,false)
    };
    this.halt=function(e){
        var obj=parent[name];
        var e=dom.getEvent(e);
        var num=e.keyCode?e.keyCode:e.which;
        var unit='m';
        gEl('reSetRuler').disabled=false;
        gEl('endRulerAction').disabled=true;
        if(num==27||e.type=='click'){
            var total=0;
            for(i=0;i<obj.rulerPoints.length;i++){
                total=total+parseInt(obj.rulerPoints[i].realLength,10)
            }
            var userFriendlyUnit=obj.convertUnit(total);
            var text='<span class="stage">Celková délka:</span><span class="unit">'+userFriendlyUnit.dist+' '+userFriendlyUnit.unit+'</span>';
            gEl('totalDistance').innerHTML=text;
            if(browser.klient=='ie'){
                obj.line.parentNode.removeChild(obj.line)
            }
            if(browser.klient=='geck'){
                obj.ctx.clearRect(0,0,1600,1200)
            }
            dom.removeEvent(obj.layer,'click',obj.startMeasure,false);
            dom.removeEvent(obj.layer,'click',obj.startMeasure,false);
            dom.removeEvent(obj.layer,'mousemove',obj.processMeasure,false);
            if(browser.klient=='ie'){
                dom.removeEvent(obj.line,'mousemove',obj.processMeasure,false)
            }
            dom.removeEvent(obj.layer,'mousedown',obj.endMeasure,false);
            if(browser.klient=='ie'){
                dom.removeEvent(obj.line,'mousedown',obj.endMeasure,false)
            }
            dom.removeEvent(document,'keypress',obj.halt,false);
            dom.removeEvent(document,'keydown',obj.halt,false)
        }
    };
    this.fillPointArray=function(sX,sY,clickX,clickY){
        var obj=parent[name];
        var length=obj.getDistance(sX,sY,clickX,clickY);
        var point=new Object();
        point.x=clickX;
        point.y=clickY;
        point.length=length.lZ;
        point.realLength=length.real;
        obj.rulerPoints[obj.rulerPoints.length]=point
    };
    this.startMeasure=function(e){
        var obj=parent[name];
        var e=dom.getEvent(e);
        gEl('endRulerAction').disabled=false;
        var clickX=e.clientX-main.getPosition(gEl('port'),'left')+main.scrollPos()['x'];
        var clickY=e.clientY-main.getPosition(gEl('port'),'top')+main.scrollPos()['y'];
        obj.StartX=clickX;
        obj.StartY=clickY;
        obj.fillPointArray(clickX,clickY,clickX,clickY);
        if(browser.klient=='ie'){
            obj.line.from=obj.rulerPoints[obj.rulerPoints.length-1].x+','+obj.rulerPoints[obj.rulerPoints.length-1].y
        }
        dom.addEvent(obj.layer,'mousemove',obj.processMeasure,false);
        if(browser.klient=='ie'){
            dom.addEvent(obj.line,'mousemove',obj.processMeasure,false)
        }
        dom.removeEvent(obj.layer,'click',obj.startMeasure,false);
        dom.addEvent(obj.layer,'mousedown',obj.endMeasure,false);
        if(browser.klient=='ie'){
            dom.addEvent(obj.line,'mousedown',obj.endMeasure,false)
        }
        dom.addEvent(document,'keypress',obj.halt,false);
        dom.addEvent(document,'keydown',obj.halt,false)
    };
    this.processMeasure=function(e){
        var obj=parent[name];
        var e=dom.getEvent(e);
        var clickX=e.clientX-main.getPosition(gEl('port'),'left')+main.scrollPos()['x'];
        var clickY=e.clientY-main.getPosition(gEl('port'),'top')+main.scrollPos()['y'];
        if(browser.klient=='ie'){
            obj.line.to=clickX+','+clickY
        }
        if(browser.klient=='geck'){
            obj.ctx.clearRect(0,0,1600,1200);
            obj.ctx.beginPath();
            obj.ctx.moveTo(obj.rulerPoints[obj.rulerPoints.length-1].x,obj.rulerPoints[obj.rulerPoints.length-1].y);
            obj.ctx.lineTo(clickX,clickY);
            obj.ctx.lineWidth=2;
            obj.ctx.stroke();
            obj.ctx.closePath()
        }
        obj.EndX=clickX;
        obj.EndY=clickY
    };
    this.endMeasure=function(){
        var obj=parent[name];
        obj.fillPointArray(obj.rulerPoints[obj.rulerPoints.length-1].x,obj.rulerPoints[obj.rulerPoints.length-1].y,obj.EndX,obj.EndY);
        if(browser.klient=='ie'){
            var l=cEl("v:line");
            l.id="rulerMeter"+obj.rulerPoints.length;
            l.filled="Off";
            l.from=obj.rulerPoints[obj.rulerPoints.length-2].x+", "+obj.rulerPoints[obj.rulerPoints.length-2].y;
            l.to=obj.rulerPoints[obj.rulerPoints.length-1].x+" ,"+obj.rulerPoints[obj.rulerPoints.length-1].y;
            obj.port.appendChild(l);
            var s=cEl('v:stroke');
            s.weight="2px";
            s.color="#0000CC";
            s.opacity="100%";
            s.fill="Solid";
            s.dashstyle="ShortDot";
            l.appendChild(s);
            var ss=cEl('v:shadow');
            ss.on="True";
            ss.color="#ffffff";
            ss.opacity="100%";
            ss.offset="0,2px";
            l.appendChild(ss);
            obj.line.from=obj.rulerPoints[obj.rulerPoints.length-1].x+','+obj.rulerPoints[obj.rulerPoints.length-1].y
        }
        if(browser.klient=='geck'){
            obj.ctxI.beginPath();
            obj.ctxI.moveTo(obj.rulerPoints[obj.rulerPoints.length-2].x,obj.rulerPoints[obj.rulerPoints.length-2].y);
            obj.ctxI.lineTo(obj.rulerPoints[obj.rulerPoints.length-1].x,obj.rulerPoints[obj.rulerPoints.length-1].y);
            obj.ctxI.lineWidth=2;
            obj.ctxI.stroke();
            obj.ctxI.closePath()
        }
        obj.updateSummary()
    };
    this.updateSummary=function(){
        var obj=parent[name];
        var dist=obj.getDistance(obj.rulerPoints[obj.rulerPoints.length-2].x,obj.rulerPoints[obj.rulerPoints.length-2].y,obj.EndX,obj.EndY);
        var pElement=cEl('p');
        pElement.className='item';
        pElement.innerHTML='<span class="stage">'+obj.boxSummary.getElementsByTagName('p').length+'. úsek:</span> <span class="unit">'+dist.lZ+' '+dist.lU+'</span>';
        obj.boxSummary.appendChild(pElement);
        var total=0;
        for(i=0;i<obj.rulerPoints.length;i++){
            total=total+parseInt(obj.rulerPoints[i].realLength,10)
        }
        var userFriendlyUnit=obj.convertUnit(total);
        var text='<span class="stage">Celková délka:</span><span class="unit">'+userFriendlyUnit.dist+' '+userFriendlyUnit.unit+'</span>';
        gEl('totalDistance').innerHTML=text
    };
    this.getDistance=function(sX,sY,eX,eY){
        var obj=parent[name];
        var realCordStart=obj.realCoords(sX,sY);
        var realCordEnd=obj.realCoords(eX,eY);
        var lX=Math.abs(realCordStart.x-realCordEnd.x);
        var lY=Math.abs(realCordStart.y-realCordEnd.y);
        var dist=Math.sqrt(Math.pow(lX,2)+Math.pow(lY,2));
        var unit='m';
        var realDist=0,dist=dist/obj.PP_CONST;
        realDist=dist;
        var userFriendlyUnit=obj.convertUnit(dist);
        return{
            lZ:userFriendlyUnit.dist,lU:userFriendlyUnit.unit,real:realDist
        }
    };
    this.convertUnit=function(dist){
        var unit='m';
        var dist=dist;
        if(dist>999){
            dist=dist/1000;
            unit='km'
        };
        dist=dist.toString();
        if(dist.indexOf('.')!=-1){
            dist=dist.substr(0,dist.indexOf('.')+3);
            if(dist.length>6){
                dist=dist.substr(0,dist.length-6)+' '+dist.substr(dist.length-6,dist.length)
            }
        }
        return{
            dist:dist,unit:unit
        }
    };
    this.realCoords=function(pixelX,pixelY){
        var obj=parent[name];
        var port=main.port.getSize();
        var driftX=Math.round(port.width/2)-((main.map.realCenter.x)+main.map.drift);
        var x=(pixelX-driftX);
        realX=x<<(main.BPT-main.map.scale);
        var driftY=(engine.map.realCenter.y)+Math.round(port.height/2);
        var y=(driftY-pixelY);
        realY=y<<(main.BPT-main.map.scale);
        return{
            x:realX,y:realY
        }
    };
    this.initButton=function(){
        var obj=parent[name];
        var button=parent.makeButton(gEl('nextObal'),'ruler_off.gif','rulerButt','ruler');
        main.activeItem(button,'click',obj.buttonAction);
        main.activeItem(button,'mousedown',dom.stopEvent)
    };
    this.buttonAction=function(e){
        var obj=parent[name];
        if(!obj.tmp.rulerRun){
            obj.go();
            gEl('obal').appendChild(gEl('ruler'));
            gEl('rulerButt').src='http://1.im.cz/mapyp/img/ruler_on.gif';
            obj.tmp.rulerRun=1
        }
        else{
            obj.end();
            obj.tmp.rulerRun=0;
            gEl('nextObal').appendChild(gEl('ruler'));
            gEl('rulerButt').src='http://1.im.cz/mapyp/img/ruler_off.gif'
        }
    }

};

function DynamicRequestObj(){
    this.allowXMLHTTPREQUEST=true;
    this.init=function(allowXMLHTTPREQUEST){
        this.allowXMLHTTPREQUEST=allowXMLHTTPREQUEST
    };
    this.send=function(url,dataFold){
        if(url!=''){
            try{
                var a=this.allowXMLHTTPREQUEST==true?this.httpRequestRoutine(url,dataFold):this.scriptRoutine(url,dataFold)
            }
            catch(e){
                this.scriptRoutine(url,dataFold)
            }
        }
    };
    this.httpRequestRoutine=function(url,dataFold){
        if(typeof(XMLHttpRequest)!='undefined'){
            var XHR=new XMLHttpRequest()
        }
        else{
            try{
                var XHR=new ActiveXObject("Msxml2.XMLHTTP")
            }
            catch(e){
                return this.scriptRoutine(url,dataFold)
            }
        }
        try{
            XHR.open("GET",url,true)
        }
        catch(e){
            return this.scriptRoutine(url,dataFold)
        }
        function stateAction(){
            if(XHR.readyState==4){
                if(XHR.status==200){
                    eval(XHR.responseText)
                }
            }
            else if(XHR.readyState==0){
                this.scriptRoutine(url,dataFold)
            }
        }
        XHR.onreadystatechange=stateAction;
        if(this.allowXMLHTTPREQUEST==true){
            XHR.send(null)
        }
        return 0
    };
    this.scriptRoutine=function(url,dataFold){
        var trg=dataFold;
        this.loader=document.createElement('script');
        this.loader.type="text/javascript";
        this.loader.src=url;
        trg.appendChild(this.loader);
        return 0
    }

};

function PointsObj(mainObj,name){
    this.pointsDef=null;
    this.points=null;
    this.pointServers=null;
    this.extServer=null;
    this.extCustomQuery=null;
    this.fillFlag=0;
    this.clickFlag=null;
    this.resultFlag=false;
    this.init=function(){
        var obj=mainObj[name];
        obj.pointsDef=config.pointDef;
        obj.pointServers=config.pointServer;
        obj.points=config.mapPoints;
        if(config.extend.allow){
            obj.extServer=config.extend.extApiServer;
            obj.extCustomQuery=config.extend.customQuery
        }
        if(result){
            obj.resultFlag=true
        }
    };
    this.makeIcon=function(x,y,zPos,title,ids,typ,num,icX,icY){
        var obj=mainObj[name];
        var pic=cEl('img');
        var src=obj.pointsDef[mainObj.map.typ][num].points['tp'+typ];
        pic.width=obj.points.icon[0];
        pic.height=obj.points.icon[1];
        pic.style.position='absolute';
        if(zPos){
            pic.style.zIndex=zPos
        }
        pic.style.top=(256-y-pic.height/2)+'px';
        pic.style.left=(x-pic.width/2)+'px';
        pic.title=title;
        pic.title=title.replace(/&quot;/g,'\"');
        pic.title=pic.title.replace(/&apos;/g,"\'");
        pic.id=ids+'-'+num;
        pic.style.cursor=browser.klient!='ie'?'pointer':'hand';
        dom.addEvent(pic,'click',engine.points.getInfo,false);
        dom.addEvent(pic,'mousedown',dom.stopEvent,false);
        if((browser.klient!='kon')||((browser.klient=='kon')&&(browser.version>3.4))){
            dom.addEvent(pic,'mouseover',mainObj.controls.gps.removeTitle,false);
            dom.addEvent(pic,'mouseout',mainObj.controls.gps.repairTitle,false)
        }
        dom.addEvent(pic,'mousedown',dom.stopEvent,false);
        dom.addEvent(pic,'click',dom.stopEvent,false);
        pic.px=icX;
        pic.py=icY;
        return{
            icon:pic,urlStr:src
        }
    };
    this.makeMultiResult=function(x,y,zPos,title,ids,typ,num,icX,icY){
        var obj=mainObj[name];
        var icon=cEl('div');
        var obal=cEl('div');
        obal.className='rsObal';
        obal.style.position='absolute';
        var numTxt=cEl('div');
        numTxt.id=ids+'-'+num;
        numTxt.title=title.replace(/&amp;quot;/g,'\"');
        numTxt.title=title.replace(/&quot;/g,'\"');
        numTxt.title=numTxt.title.replace(/&apos;/g,"\'");
        var idInfo=ids.split('-');
        var number=parseInt(idInfo[1],10);
        var pg=result.pg;
        numTxt.innerHTML=(((pg)*resultPerPage)+(number+1));
        if(result.type=='route'){
            var iconName='rIconNone';
            numTxt.className='icCntNone'
        }
        else{
            var iconName='rIconPs';
            numTxt.className='icCnt'
        }
        icon.className=iconName;
        icon.id=ids+'-'+num+'-pd';
        obal.style.zIndex=30+number;
        obal.style.top=(256-y-36)+'px';
        obal.style.left=(x-14)+'px';
        obal.appendChild(icon);
        obal.appendChild(numTxt);
        numTxt.style.top=5+'px';
        numTxt.style.left=5+'px';
        dom.addEvent(numTxt,'click',engine.points.getInfo,false);
        dom.addEvent(numTxt,'mousedown',dom.stopEvent,false);
        if((browser.klient!='kon')||((browser.klient=='kon')&&(browser.version>3.4))){
            dom.addEvent(numTxt,'mouseover',mainObj.controls.gps.removeTitle,false);
            dom.addEvent(numTxt,'mouseout',mainObj.controls.gps.repairTitle,false)
        }
        dom.addEvent(numTxt,'mouseover',obj.changeBg,false);
        dom.addEvent(numTxt,'mouseout',obj.changeBg,false);
        dom.addEvent(numTxt,'mousedown',dom.stopEvent,false);
        dom.addEvent(numTxt,'click',dom.stopEvent,false);
        numTxt.zi=obal.style.zIndex;
        numTxt.px=icX;
        numTxt.py=icY;
        if(result.type=='route'){
            
        }
        return obal
    };
    this.changeBg=function(e){
        var obj=mainObj[name];
        var e=dom.getEvent(e);
        var trg=dom.getTarget(e);
        var idf=trg.id.split('-');
        var resId='item-'+(parseInt(idf[1])+1);
        if(e.type=='mouseover'){
            gEl(resId).className=gEl(resId).className+' active';
            gEl(trg.id).parentNode.getElementsByTagName('div')[0].className='rIconPa';
            gEl(trg.id).className='icCntA';
            if(browser.klient=='ie'){
                gEl(trg.id).parentNode.parentNode.style.zIndex=99
            }
            gEl(trg.id).parentNode.style.zIndex=parseInt(gEl(trg.id).parentNode.style.zIndex)+15
        }
        else if(e.type=='mouseout'){
            gEl(resId).className='item';
            gEl(trg.id).parentNode.getElementsByTagName('div')[0].className='rIconPs';
            gEl(trg.id).className='icCnt';
            if(browser.klient=='ie'){
                gEl(trg.id).parentNode.parentNode.style.zIndex=0
            }
            gEl(trg.id).parentNode.style.zIndex=gEl(trg.id).zi
        }
    };
    this.fillActive=function(serverTyp){
        var obj=mainObj[name];
        var query=mainObj.tileQuery;
        var typeQuery='';
        var rnd=mainObj.getRandom();
        obj.fillFlag=serverTyp;
        if((!isNaN(serverTyp))&&(serverTyp<obj.pointServers.length)){
            var server=obj.pointServers[serverTyp];
            try{
                typeQuery=obj.pointsDef[mainObj.map.typ][serverTyp].qs
            }
            catch(e){
                typeQuery=''
            }
            var qs=server+query+typeQuery+'encoding='+mainObj.encoding+rnd;
            gEl('pointsFold').innerHTML='';
            var fld=gEl('pointsFold');
            if(typeQuery){
                mainObj.getData(fld,qs)
            }
            else{
                engine.makePoints([])
            }
        }
        else if((!isNaN(serverTyp))&&(obj.pointServers.length==0)){
            engine.makePoints([])
        }
        else if(serverTyp=='ext'){
            engine.makePoints([])
        }
        else if(serverTyp=='result'){
            if(result){
                engine.makePoints(result.point)
            }
            else{
                engine.makePoints([])
            }
        }
        else{
            
        }
    };
    this.getInfo=function(e){
        var obj=mainObj[name];
        if(mainObj.status.get('rulerActive')){
            return
        }
        var trg=dom.getTarget(e);
        dom.stopEvent(e);
        var str=trg.id;
        mainObj[name].clickFlag=str;
        mainObj.vizitka.pointStr=str;
        var num=str.split('-');
        var rnd=mainObj.getRandom();
        mainObj.vizitka.bubleData.opened=true;
        mainObj.vizitka.bubleData.ids=str;
        if(userPoint){
            mainObj.vizitka.poiIds=0
        }
        else if(num[2]=='result'){
            mainObj.vizitka.poiIds=result.poiIds[num[1]]
        }
        else{
            mainObj.vizitka.poiIds={
                poi_id:num[1]
            }
        }
        if(!isNaN(num[2])){
            mainObj.vizitka.bubleFold.driftX=trg.width/2;
            mainObj.vizitka.bubleFold.driftY=trg.height/2;
            var server=obj.pointServers[num[2]]
        }
        else if(num[2]=='ext'){
            mainObj.vizitka.bubleFold.driftX=trg.width/2;
            mainObj.vizitka.bubleFold.driftY=trg.height/2;
            var server=obj.extServer
        }
        else if(num[2]=='result'){
            mainObj.vizitka.bubleFold.driftX=9;
            mainObj.vizitka.bubleFold.driftY=31;
            mainObj.status.set('noBubleMove',0);
            mainObj.vizitka.pointStr=trg.id;
            mainObj.vizitka.makeVizitka(result.buble[num[1]]);
            return
        }
        var qs=server+'/detail?adr='+num[1]+'&encoding='+mainObj.encoding+rnd;
        var fld=gEl('dataFold');
        mainObj.getData(fld,qs)
    };
    this.make=function(param){
        var obj=mainObj[name];
        mainObj.mpLayer.mapLayers.active.fill(param)
    }

};

function VizitkaObj(mainObj,name){
    this.pointStr='';
    this.oldPoint='';
    this.poiIds=0;
    this.resultField=new Array();
    this.bubleData={
        opened:false,ids:''
    };
    this.bubleFold=new Object();
    this.init=function(){
        
    };
    this.makeVizitka=function(dataField){
        var obj=mainObj[name];
        if(gEl('buble')){
            gEl('buble').parentNode.removeChild(gEl('buble'))
        }
        var type=0;
        var num=dataField.length;
        obj.resultField=dataField;
        obj.bubleFold.content=dataField;
        obj.bubleFold.opened=true;
        obj.bubleFold.pointId=obj.pointStr;
        obj.bubleFold.poiIds=obj.poiIds;
        obj.bubleFold.x=gEl(obj.pointStr)?gEl(obj.pointStr).px:obj.bubleFold.x;
        obj.bubleFold.y=gEl(obj.pointStr)?gEl(obj.pointStr).py:obj.bubleFold.y;
        if(num==1){
            obj.makeSimplBox(dataField)
        }
        else if((num>1)&&(num<=3)){
            obj.makeExtendBox(dataField)
        }
        else{
            return
        }
    };
    this.makeSimplBox=function(field){
        var obj=mainObj[name];
        var mainBox=cEl('div');
        var obal1='<div class="vzTop"></div><div class="vzBody">';
        var obsahObal0='<div class="vzCnt"><div class="cntObsah" id="cO">';
        var obsahObal1='</div>';
        var addOns0='<div class="addonsBox" id="aB">';
        if(obj.bubleFold.poiIds!=0){
            var addOnsLine='<p class="addLine"><span id="gpsB">GPS</span> &nbsp;|&nbsp; <span id="rtB">Opravit polohu</span></p>'
        }
        else{
            var addOnsLine='<p class="addLine"><span id="gpsB">GPS</span><span id="rtB" style="display:none;"></span></p>'
        }
        var addOns1='</div>';
        var rtBox='<div id="boxRoute"><div class="rbx rtEx"><span class="lft">Původní poloha: </span><span class="rght"></span></div><form id="repCoord" method="post" action="/err404.html"><div class="rbx rtLa"><label class="lft"><strong>Nová:</strong></label><span class="rght"><input id="whTrg" type="text" /></span><input id="sendNew" class="poslat" type="submit" value="Odeslat" /></div></form></div>';
        var gpBox='<div id="boxGps"><p>bbbb bbbb bbbb</p><p>aaaaa aaaa aaa</p></div>';
        var addOns=addOns0+addOnsLine+rtBox+gpBox+addOns1;
        var bottomPart='</div></div><img id="infClose"  src="http://1.im.cz/mapyp/img/ico_close.gif" width="15" height="15" alt="" title="Zavřít" /><div class="vzBottom"></div><div id="tail" class="vzTail"></div>';
        var rep=field[0].content.replace(/&amp;quot;/g,'"');
        rep=rep.replace(/&apos;/g,"'");
        var data='<div id="cntFold'+i+'" style="display:block;">'+rep+'</div>';
        var obsah=obal1+obsahObal0+data+obsahObal1+addOns+bottomPart;
        mainBox.innerHTML=obsah;
        mainBox.id='buble';
        mainBox.className='vzSimpl';
        obj.pasteBox(mainBox)
    };
    this.makeExtendBox=function(field){
        var obj=mainObj[name];
        var mainBox=cEl('div');
        var fldBox0='<div class="foldMenu">';
        var fldBox1='</div>';
        var helperDiv='<div id="fK"></div>';
        var topPart='<div class="vzFold" id="aaa"></div>'+helperDiv+'<div class="vzTop"></div><div class="vzBody">';
        var contentBox0='<div class="vzCnt"><div class="cntObsah" id="cO">';
        var contentBox10='</div>';
        var addOns0='<div class="addonsBox" id="aB">';
        if(obj.bubleFold.poiIds!=0){
            var addOnsLine='<p class="addLine"><span id="gpsB">GPS</span> &nbsp;|&nbsp; <span id="rtB">Opravit polohu</span></p>'
        }
        else{
            var addOnsLine='<p class="addLine"><span id="gpsB">GPS</span><span id="rtB" style="display:none;"></span></p>'
        }
        var addOns1='</div>';
        var rtBox='<div id="boxRoute"><div class="rbx rtEx"><span class="lft">Původní poloha: </span><span class="rght"></span></div><form id="repCoord" method="post" action="/err404.html"><div class="rbx rtLa"><label class="lft"><strong>Nová:</strong> </label><span class="rght"><input id="whTrg" type="text" /></span><input class="poslat" id="sendNew" type="submit" value="Odeslat" /></div></form></div>';
        var gpBox='<div id="boxGps"><p>N/A</p><p>N/A</p></div>';
        var addOns=addOns0+addOnsLine+rtBox+gpBox+addOns1;
        var contentBox11='</div>';
        var bottomPart='</div><img id="infClose"  src="http://1.im.cz/mapyp/img/ico_close.gif" width="15" height="15" alt="" title="Zavřít" /><div class="vzBottom"></div><div id="tail" class="vzTail"></div>';
        var folders='';
        var data='';
        var k=0;
        for(var i=0;i<field.length;i++){
            if(field[i]){
                folders+='<div class="fold'+k+'"><span>'+field[i].name+'</span></div>';
                var rep=field[i].content.replace(/&amp;quot;/g,'"');
                rep=rep.replace(/&apos;/g,"'");
                data+='<div id="cntFold'+k+'">'+rep+'</div>';
                k++
            }
        }
        var obsah=fldBox0+folders+fldBox1+topPart+contentBox0+data+contentBox10+addOns+contentBox11+bottomPart;
        mainBox.innerHTML=obsah;
        mainBox.id='buble';
        mainBox.className='vzFold'+k;
        obj.pasteBox(mainBox)
    };
    this.showExtras=function(e){
        var obj=mainObj[name];
        trg=dom.getTarget(e);
        ids=trg.id;
        if(browser.klient=='ie'){
            var bType=gEl('buble').className=="vzSimpl"?'simpl':'folds';
            if(bType=='folds'){
                var ieDrift=4
            }
            else{
                var ieDrift=2
            }
        }
        else{
            var ieDrift=0
        }
        var elmActive=ids=='rtB'?gEl('boxRoute'):gEl('boxGps');
        var pasiveIds=ids=='rtB'?'gpsB':'rtB';
        var drift=ids=='rtB'?'-88px':'-62px';
        var elmPasive='';
        var elms=gEl('aB').getElementsByTagName('p')[0].getElementsByTagName('span');
        var boxs=new Array('boxRoute','boxGps');
        for(var i=0;i<elms.length;i++){
            if(gEl(boxs[i]).style.display=='block'){
                var elmPasive=gEl(boxs[i])
            }
        }
        if(elmActive.style.display!='block'){
            elmActive.style.display='block';
            gEl('aB').style.height=ids=='rtB'?(64-ieDrift)+'px':'38px';
            obj.bubleFold.height+=ids=='rtB'?(44-ieDrift):18;
            gEl(ids).style.color='#000';
            gEl(ids).style.fontWeight='bold';
            if(elmActive.id=='boxRoute'){
                
            }
            if(elmPasive){
                gEl(pasiveIds).style.color='blue';
                gEl(pasiveIds).style.fontWeight='normal';
                elmPasive.style.display='none';
                var num=ids=='rtB'?18:-18;
                if(elmActive!=elmPasive){
                    var num=ids=='rtB'?(num+8)+ieDrift:(num-8)-ieDrift;
                    obj.bubleFold.height-=ids=='rtB'?18:(44-ieDrift)
                }
            }
            else{
                var num=ids=='rtB'?46:20;
                num=ids=='rtB'?num+ieDrift:num
            }
        }
        else{
            if(elmActive==elmPasive){
                var num=ids=='rtB'?-46:-20;
                num=ids=='rtB'?num-ieDrift:num;
                obj.bubleFold.height-=ids=='rtB'?(44-ieDrift):18
            }
            gEl('aB').style.height='18px';
            elmActive.style.display='none';
            gEl(ids).style.color='blue';
            gEl(ids).style.fontWeight='normal'
        }
        gEl('buble').style.top=(parseInt(gEl('buble').style.top)-num)+'px';
        gEl('tail').style.bottom='-43px';
        if(browser.klient=='op'){
            gEl('tail').style.visibility='hidden';
            gEl('buble').lastChild.style.bottom=elmActive.style.display=='block'?drift:'-42px';
            gEl('tail').style.visibility='visible'
        }
        else if(browser.klient=='geck'){
            gEl('tail').style.bottom=elmActive.style.display=='block'?drift:'-42px'
        }
        else{
            var bottNum=-42;
            gEl('tail').style.bottom=bottNum+'px'
        }
        obj.moveToBubleVisible()
    };
    this.bubleSwitch=function(){
        var obj=mainObj[name];
        var ids=obj.bubleFold.pointId;
        var data=obj.bubleFold.content;
        obj.pointStr=ids;
        obj.resultField=data;
        obj.makeVizitka(data)
    };
    this.setGPS=function(x,y){
        var obj=mainObj[name];
        var realX=x;
        var realY=y;
        ver=(realY)*Math.pow(2,-5)+1300000;
        hor=(realX)*Math.pow(2,-5)+(-3700000);
        var coord=mainObj.controls.gps.calc(ver,hor);
        wgsString='Loc: '+coord.lat.locNum+'N, '+coord.lot.locNum+coord.lot.locStr;
        utmString='UTM zone '+coord.zone+': '+'N '+Math.round(coord.north)+' E '+Math.round(coord.east);
        return[wgsString,utmString]
    };
    this.pasteBox=function(box){
        var obj=mainObj[name];
        var ref=obj.pointStr;
        if(ref!=obj.oldPoint){
            obj.oldPoint=ref;
            mainObj.status.set('noBubleMove',0)
        }
        else{
            mainObj.status.set('noBubleMove',1)
        }
        var posXX=((obj.bubleFold.x>>(mainObj.BPT-mainObj.map.scale))-mainObj.map.tileDrift.x)-obj.bubleFold.driftX;
        var posYY=(engine.map.height-((obj.bubleFold.y>>(mainObj.BPT-mainObj.map.scale))-mainObj.map.tileDrift.y))-obj.bubleFold.driftY;
        box.ids=ref;
        var pointData=ref.split('-');
        if(pointData[2]=='result'){
            var driftX=12;
            var driftY=5
        }
        else{
            var driftX=12;
            var driftY=21
        }
        box.style.display='block';
        box.style.visibility='hidden';
        mainObj.map.el.appendChild(box);
        var gps=obj.setGPS(obj.bubleFold.x,obj.bubleFold.y);
        gEl('boxGps').getElementsByTagName('p')[1].style.display='none';
        gEl('boxGps').getElementsByTagName('p')[0].innerHTML=gps[0];
        gEl('boxGps').getElementsByTagName('p')[1].innerHTML=gps[1];
        gEl('boxRoute').getElementsByTagName('span')[1].innerHTML=gps[0];
        var num=box.offsetHeight+driftY;
        gEl('cO').style.overflow='auto';
        box.style.height=box.offsetHeight+'px';
        var co=box.getElementsByTagName('div')[0].getElementsByTagName('div');
        for(var i=0;i<co.length;i++){
            if(i==0){
                co[i].className=co[i].className+' act';
                co[i].id='act'
            }
            dom.addEvent(co[i],'click',obj.showTab,false)
        }
        if(gEl('rtB')){
            dom.addEvent(gEl('rtB'),'click',obj.showExtras,false);
            gEl('sendNew').disabled=false;
            dom.addEvent(gEl('repCoord'),'submit',mainObj.repairCoord.send,false)
        }
        if(gEl('gpsB')){
            dom.addEvent(gEl('gpsB'),'click',obj.showExtras,false)
        }
        if(gEl('rtB')){
            
        }
        dom.addEvent(gEl('infClose'),'click',obj.delInfo,false);
        dom.addEvent(gEl('tail'),'click',obj.delInfo,false);
        box.style.top=(posYY-num)+'px';
        box.style.left=(posXX-driftX)+'px';
        box.style.visibility='visible';
        obj.bubleFold.width=box.offsetWidth;
        obj.bubleFold.height=box.offsetHeight+42;
        dom.addEvent(box,'click',dom.stopEvent,false);
        dom.addEvent(box,'mousedown',dom.stopEvent,false);
        if(mainObj.status.get('noVisibleMove')){
            mainObj.status.set('noVisibleMove',false);
            return
        }
        else{
            mainObj.status.set('noVisibleMove',false);
            obj.moveToBubleVisible()
        }
    };
    this.showTab=function(e){
        var obj=mainObj[name];
        var trg=dom.getTarget(e);
        if(trg.nodeName.toLowerCase()!='div'){
            trg=trg.parentNode
        }
        var num=browser.klient=='ie'?6:7;
        var co=gEl('buble').getElementsByTagName('div')[0].getElementsByTagName('div');
        for(var i=0;i<co.length;i++){
            if(co[i]==trg){
                co[i].className=co[i].className+' act';
                co[i].id='act';
                gEl('cntFold'+i).style.display='block';
                if(i!=0){
                    var vyska=gEl('aB').offsetHeight?gEl('aB').offsetHeight+num:0;
                    gEl('cO').style.height=(parseInt(gEl('cO').offsetHeight)+vyska)+'px';
                    gEl('cO').style.overflow='auto';
                    gEl('aB').style.display='none'
                }
                else{
                    gEl('cO').style.height=(browser.klient=='ie')?'120px':'125px';
                    gEl('cO').style.overflow='hidden';
                    gEl('aB').style.display='block';
                    var a=gEl('boxRoute').offsetHeight
                }
            }
            else{
                co[i].className='fold'+i;
                co[i].id='';
                gEl('cntFold'+i).style.display='none'
            }
        }
    };
    this.moveSwitch=function(){
        if(browser.klient=='geck'){
            var trg=gEl('cO');
            if(trg.style.overflow!='hidden'){
                trg.style.overflow='hidden'
            }
            else{
                trg.style.overflow='auto'
            }
        }
    };
    this.showFromResult=function(e){
        var obj=mainObj[name];
        if(mainObj.status.get('rulerActive')){
            return
        }
        var trg=dom.getTarget(e);
        var port=mainObj.port.getSize();
        var scale=mainObj.map.scale;
        while(trg.id.indexOf('item')==-1){
            trg=trg.parentNode
        }
        var ids=trg.id;
        var dt=ids.split('-');
        var num=dt[1];
        var pointId='acp-'+(num-1)+'-result';
        if(window.interval2){
            window.clearInterval(interval2);
            window.interval2=0;
            engine.map.center.y=Math.round(port.height/2)-parseInt(engine.map.el.style.top);
            engine.map.center.x=(Math.round(port.width/2))-parseInt(engine.map.el.style.left)-engine.map.drift;
            engine.map.updateCenter()
        }
        var limit=obj.setResultBublePos();
        if(gEl('buble')){
            obj.delInfo();
            obj.showFromResult(e);
            return
        }
        obj.poiIds=userPoint?0:result.poiIds[num-1];
        obj.bubleFold.x=result.point[num-1].x;
        obj.bubleFold.y=result.point[num-1].y;
        mainObj.vizitka.bubleFold.driftX=9;
        mainObj.vizitka.bubleFold.driftY=31;
        var x=result.point[num-1].x>>(mainObj.BPT-scale);
        var y=result.point[num-1].y>>(mainObj.BPT-scale);
        if(result&&(result.type=='route')&&(engine.map.scale<12)){
            mainObj.map.scale=12;
            mainObj.controls.zoom.setBar(12);
            scale=12;
            mainObj.center.x=result.point[num-1].x;
            mainObj.center.y=result.point[num-1].y;
            mainObj.map.update();
            mainObj.vizitka.pointStr=pointId;
            mainObj.vizitka.resultField=result.buble[num-1];
            obj.showFromResult(e);
            return
        }
        var calc=obj.calcNewPosition(x,y,limit);
        var dirX=calc.dirX;
        var dirY=calc.dirY;
        var update=calc.update;
        mainObj.status.set('fromResult',0);
        if(update){
            mainObj.center.x=obj.bubleFold.x;
            mainObj.center.y=obj.bubleFold.y;
            mainObj.status.set('noVisibleMove',true);
            mainObj.map.update();
            obj.bubleFold.pointId=pointId;
            obj.bubleFold.content=result.buble[num-1];
            mainObj.vizitka.bubleSwitch(pointId,obj.bubleFold.content)
        }
        else if(dirX||dirY){
            obj.bubleFold.pointId=pointId;
            obj.bubleFold.content=result.buble[num-1];
            mainObj.vizitka.bubleSwitch(pointId,obj.bubleFold.content);
            mainObj.status.set('fromResult',0)
        }
        else{
            obj.bubleFold.pointId=pointId;
            obj.bubleFold.content=result.buble[num-1];
            if(obj.bubleFold.content.length>0){
                mainObj.vizitka.bubleSwitch(pointId,obj.bubleFold.content);
                mainObj.status.set('fromResult',0)
            }
        }
    };
    this.setVisibleBox=function(){
        var obj=mainObj[name];
        var bublePos=new Object();
        var brd=obj.setResultBublePos();
        var port=mainObj.port.getSize();
        bublePos.x=(obj.bubleFold.x>>(mainObj.BPT-mainObj.map.scale));
        bublePos.y=(obj.bubleFold.y>>(mainObj.BPT-mainObj.map.scale));
        var posunX=0;
        var dirX=0;
        var posunY=0;
        var dirY=0;
        if((bublePos.x+obj.bubleFold.width)>(brd.right-100)){
            posunX=Math.ceil(Math.abs((bublePos.x+obj.bubleFold.width)-(brd.right-100)));
            if((mainObj.map.realCenter.x+posunX-port.width/2)>(bublePos.x)){
                posunX=posunX-(((mainObj.map.realCenter.x+posunX-port.width/2))-bublePos.x)-10
            }
            dirX=-1
        }
        if(bublePos.x<brd.left){
            posunX=Math.ceil(Math.abs(bublePos.x-brd.left))+20;
            dirX=1
        }
        if((bublePos.y+obj.bubleFold.height)>brd.top){
            posunY=Math.ceil(Math.abs((bublePos.y+obj.bubleFold.height)-brd.top))+5;
            if((mainObj.map.realCenter.y+posunY-port.height/2)>(bublePos.y)){
                posunY=posunY-(((mainObj.map.realCenter.y+posunY-port.height/2))-bublePos.y)-10
            }
            dirY=1
        }
        if(bublePos.y<brd.bottom){
            posunY=Math.ceil(Math.abs(bublePos.y-brd.bottom))+20;
            dirY=-1
        }
        return{
            posunX:posunX,dirX:dirX,posunY:posunY,dirY:dirY
        }
    };
    this.moveToBubleVisible=function(){
        var obj=mainObj[name];
        var move=obj.setVisibleBox();
        if(mainObj.status.get('noBubleMove')){
            mainObj.status.set('noBubleMove',0);
            return
        }
        if((move.posunX>0)||(move.posunY>0)){
            if((move.posunX<1600)&&(move.posunY<1600)){
                mainObj.map.moveX(move.dirX,move.dirY,move.posunX,move.posunY,0)
            }
            else{
                newCenterX=mainObj.map.realCenter.x+(-move.dirX*move.posunX);
                newCenterY=mainObj.map.realCenter.y+(move.dirY*move.posunY);
                engine.center.x=newCenterX<<(mainObj.BPT-mainObj.map.scale);
                engine.center.y=newCenterY<<(mainObj.BPT-mainObj.map.scale);
                mainObj.map.update()
            }
        }
    };
    this.calcNewPosition=function(x,y,limit){
        var obj=mainObj[name];
        var dirX=0;
        var dirY=0;
        var update=0;
        var port=mainObj.port.getSize();
        if(y>limit.top){
            if((y-limit.top)>1600){
                update=1
            }
            else{
                dirY=1
            }
        }
        else if(y<limit.bottom){
            if((limit.bottom-y)>1600){
                update=1
            }
            else{
                dirY=-1
            }
        }
        if(x>limit.right){
            if((x-limit.right)>1600){
                update=1
            }
            else{
                dirX=-1
            }
        }
        else if(x<limit.left){
            if((limit.left-x)>1600){
                update=1
            }
            else{
                dirX=1
            }
        }
        return{
            dirX:dirX,dirY:dirY,update:update
        }
    };
    this.setResultBublePos=function(){
        var obj=mainObj[name];
        var port=mainObj.port.getSize();
        var pos=new Object();
        pos.top=(mainObj.map.realCenter.y+port.height/2);
        pos.right=(mainObj.map.realCenter.x+port.width/2);
        pos.bottom=(mainObj.map.realCenter.y-port.height/2);
        pos.left=(mainObj.map.realCenter.x-port.width/2);
        return pos
    };
    this.delInfo=function(){
        if(!mainObj){
            var mainObj=engine;
            var name='vizitka'
        }
        var obj=mainObj[name];
        gEl('buble').parentNode.removeChild(gEl('buble'));
        obj.pointStr='';
        obj.oldPoint='';
        obj.bubleFold=new Object();
        obj.resultField=new Array();
        mainObj.vizitka.bubleData.opened=false
    }

};

function RepairCoord(mainObj,name){
    this.msgId=new Object();
    this.send=function(e){
        var obj=mainObj[name];
        dom.cancelDef(e);
        if(!mainObj){
            return
        }
        var ids=mainObj.vizitka.bubleFold.poiIds;
        var co=gEl('whTrg').value;
        if(co==''){
            alert('Nebyly zadány žádné souřadnice')
        }
        else{
            gEl('sendNew').disabled=true;
            obj.msgId[mainObj.vizitka.bubleFold.pointId]=progressBar.open('Čekejte prosím, probíhá zpracování požadavku.');
            obj.sendAnswer()
        }
        return false
    };
    this.sendAnswer=function(){
        var obj=mainObj[name];
        var address='http://'+window.location.host+'/repairloc.py?';
        var ids=mainObj.vizitka.bubleFold.poiIds;
        var newLoc='new_loc='+encodeURI(gEl('whTrg').value);
        var poiIdsStr='';
        for(var name in ids){
            poiIdsStr+='&'+name+'='+encodeURI(ids[name])
        }
        var rpUrl=address+newLoc+poiIdsStr+'&loop=coord_'+mainObj.vizitka.bubleFold.pointId;
        var fold=gEl('repFold');
        mainObj.getData(fold,rpUrl)
    };
    this.makeAnswer=function(answerData,loop){
        var obj=mainObj[name];
        var eFake=new Object();
        var fld=loop.split('_');
        eFake.type='fake';
        eFake.msgid=obj.msgId[fld[1]];
        progressBar.close(eFake);
        if(gEl('sendNew')){
            gEl('sendNew').disabled=false
        }
        if(answerData.ok){
            if(fld[1]==mainObj.vizitka.bubleFold.pointId){
                mainObj.vizitka.delInfo()
            }
            alert('Děkujeme za opravu polohy. Údaje byly odeslány administrátorům ke zpracování, změna se projeví do tří dnů.')
        }
        else{
            if(typeof(answerData.coordinatesError)!='undefined'){
                alert('Nové souřadnice byly chybně zadány!')
            }
            if(typeof(answerData.serverError)!='undefined'){
                alert('Údaje se nepodařilo zpracovat!')
            }
        }
    }

};

function UrlMakerObj(mainObj,name){
    this.host='';
    this.url='';
    this.status='';
    this.mainData=new Object();
    this.searchData=new Object();
    this.nearData=new Object();
    this.routeData=new Object();
    this.userPointData=new Object();
    this.init=function(){
        var obj=mainObj[name];
        obj.host='http://'+window.location.host
    };
    this.setBase=function(){
        var obj=mainObj[name];
        obj.mainData=new Object();
        var port=mainObj.port.getSize();
        obj.mainData.mp=mainObj.map.typ;
        obj.mainData.zm=mainObj.map.scale;
        obj.mainData.cx=mainObj.center.x;
        obj.mainData.cy=mainObj.center.y;
        obj.mainData.pw=port.width-mainObj.map.drift;
        obj.mainData.ph=port.height
    };
    this.setSearch=function(){
        var obj=mainObj[name];
        obj.searchData=new Object();
        obj.searchData.fr=result.fr?encodeURI(mainObj.checkStr(result.fr)):'';
        obj.searchData.pg=result.pg?result.pg:0;
        obj.searchData.ppg=result.ppg?result.ppg:0
    };
    this.setNear=function(){
        var obj=mainObj[name];
        obj.nearData=new Object();
        obj.nearData.typ=near.typ;
        obj.nearData.ocx=near.x;
        obj.nearData.ocy=near.y
    };
    this.setRoute=function(){
        var obj=mainObj[name];
        obj.routeData=new Object();
        obj.routeData.fr=result.fr?encodeURI(mainObj.checkStr(result.fr)):'';
        if(gEl('criterium3')&&gEl('criterium3').checked){
            obj.routeData.q=gEl('criterium3').value
        }
        else if((gEl('criterium1')&&gEl('criterium1').checked)){
            obj.routeData.q=gEl('criterium1').value
        }
        else{
            obj.routeData.q=result.q
        }
        if(gEl('pay1')&&gEl('pay1').checked){
            obj.routeData.tr=gEl('pay1').value
        }
        else if((gEl('pay0')&&gEl('pay0').checked)){
            obj.routeData.tr=gEl('pay0').value
        }
        else{
            obj.routeData.tr=result.q
        }
    };
    this.setUserPoint=function(){
        var obj=mainObj[name];
        if((typeof(userPoint)!='undefined')&&userPoint){
            for(var i in userPoint){
                obj.userPointData[i]=userPoint[i]
            }
        }
    };
    this.set=function(){
        var obj=mainObj[name];
        obj.setBase();
        obj.status='';
        if(result){
            stStr=(result.type=='route')?'search':result.type;
            obj.status='st='+stStr;
            if(result.type=='search'){
                obj.setSearch()
            }
            else if(result.type=='near'){
                obj.setNear()
            }
            else if(result.type=='route'){
                obj.setRoute()
            }
            if((typeof(userPoint)!='undefined')&&userPoint){
                obj.setUserPoint()
            }
        }
    };
    this.makeSearchListing=function(){
        var obj=mainObj[name];
        obj.get();
        var query=obj.host+'/?'+'st='+result.type+'&fr='+encodeURI(mainObj.checkStr(result.fr));
        return query
    };
    this.get=function(){
        var obj=mainObj[name];
        var host=obj.host;
        var base=obj.getBase();
        var action='';
        var buble='';
        var arrow='';
        var custom='';
        var loop='';
        if(result){
            if(result.type=='search'){
                action=obj.getSearch()
            }
            else if(result.type=='near'){
                action=obj.getNear()
            }
            else if(result.type=='route'){
                action=obj.getRoute()
            }
            if((typeof(userPoint)!='undefined')&&userPoint){
                arrow=obj.getUserPoint()
            }
        }
        var ocStr='';
        if(typeof(result)!='undefined'){
            if((typeof(result.ocx)!='undefined')&&(typeof(result.ocy)!='undefined')){
                ocStr='&ocx='+result.ocx+'&ocy='+result.ocy
            }
        }
        var query=obj.status+base+action+arrow+buble+custom+ocStr+loop;
        if(!obj.status){
            query=query.substring(1)
        }
        if(arguments[0]){
            return obj.host+'/'+arguments[0]+'?'+query
        }
        else{
            obj.url=obj.host+'/?'+query
        }
    };
    this.make=function(){
        var obj=mainObj[name];
        obj.set();
        obj.get();
        if(result&&result.tooltip&&resultMaker){
            resultMaker.updateTooltipLink()
        }
        var urlTxt=obj.url.replace(/&/g,"&<wbr />");
        var urlTxt=urlTxt.replace(/%20/g,"%20<wbr />");
        var urlTxt=urlTxt.replace(/,/g,",<wbr />");
        if(gEl('linkBox')){
            mainMenu.linkSet(obj.url,urlTxt)
        }
        if(gEl('paging')){
            resultMaker.updatePagging()
        }
    };
    this.getBase=function(){
        var obj=mainObj[name];
        var str='';
        for(i in obj.mainData){
            str+='&'+i+'='+obj.mainData[i]
        }
        return str
    };
    this.getSearch=function(){
        var obj=mainObj[name];
        var str='';
        for(i in obj.searchData){
            str+='&'+i+'='+obj.searchData[i]
        }
        return str
    };
    this.getNear=function(){
        var obj=mainObj[name];
        var str='';
        for(i in obj.nearData){
            str+='&'+i+'='+obj.nearData[i]
        }
        return str
    };
    this.getRoute=function(){
        var obj=mainObj[name];
        var str='';
        for(i in obj.routeData){
            str+='&'+i+'='+obj.routeData[i]
        }
        return str
    };
    this.getUserPoint=function(){
        var obj=mainObj[name];
        var str='';
        for(i in obj.userIconData){
            str+='&'+i+'='+obj.userIconData[i]
        }
        return str
    }

};

function MapScaleObj(mainObj,name){
    this.pic=null;
    this.init=function(){
        var obj=mainObj[name];
        if(config.mapControls.scale){
            obj.pic=cEl('img');
            obj.pic.id='mpSc';
            gEl('port').appendChild(obj.pic);
            obj.set()
        }
    };
    this.set=function(){
        var obj=mainObj[name];
        var str=mainObj.map.apiServer+'/scale?center='+mainObj.map.scale+'_'+mainObj.getNumToHex(engine.center.x)+'_'+mainObj.getNumToHex(mainObj.center.y);
        obj.pic.src=str
    }

};

function MapNorthObj(mainObj,name){
    this.pic=null,this.init=function(){
        var obj=mainObj[name];
        if(config.mapControls.north){
            obj.pic=cEl('img');
            obj.pic.id='mpNt';
            gEl('port').appendChild(obj.pic);
            obj.set()
        }
    };
    this.set=function(){
        var obj=mainObj[name];
        var str=mainObj.map.apiServer+'/north?center='+mainObj.map.scale+'_'+mainObj.getNumToHex(mainObj.center.x)+'_'+mainObj.getNumToHex(mainObj.center.y);
        obj.pic.src=str
    }

};

function UserIconObj(mainObj,name){
    this.data=null;
    this.init=function(){
        var obj=mainObj[name];
        obj.data=userPoint
    };
    this.removeIcon=function(){
        var obj=mainObj[name];
        if(gEl('userIcon')){
            gEl('userIcon').parentNode.removeChild(gEl('userIcon'));
            userPoint=false;
            obj.data=null
        }
    };
    this.makeIcon=function(x,y){
        var obj=mainObj[name];
        var icon=cEl('div');
        var obal=cEl('div');
        obal.className='rsIcon';
        obal.style.position='absolute';
        obal.id='userIcon';
        var numTxt=cEl('div');
        numTxt.id='acp-'+'0'+'-result';
        numTxt.title=obj.data.stxt;
        numTxt.className='icCnt';
        numTxt.innerHTML='X';
        icon.className='rIconPs';
        obal.style.zIndex=61;
        obal.style.top=(256-y-28)+'px';
        obal.style.left=(x-14)+'px';
        obal.appendChild(icon);
        obal.appendChild(numTxt);
        numTxt.style.top=5+'px';
        numTxt.style.left=5+'px';
        numTxt.px=obj.data.sx;
        numTxt.py=obj.data.sy;
        return obal
    };
    this.paste=function(){
        var obj=mainObj[name];
        var idTileX=obj.data.sx.toString(16);
        var idTileY=obj.data.sy.toString(16);
        var rTileX=((obj.data.sx>>(mainObj.BPN-mainObj.map.scale))<<(mainObj.BPN-mainObj.map.scale));
        var rTileY=((obj.data.sy>>(mainObj.BPN-mainObj.map.scale))<<(mainObj.BPN-mainObj.map.scale));
        var hexX=rTileX.toString(16);
        var hexY=rTileY.toString(16);
        var str='active'+'-'+mainObj.map.scale+'-'+hexX+'-'+hexY;
        var picStr='basis'+'-'+mainObj.map.scale+'-'+hexX+'-'+hexY;
        if(mainObj.mpLayer.mapLayers['basis'].folder[picStr]){
            if(!gEl(str)){
                var tileX=(rTileX>>(mainObj.BPT-mainObj.map.scale))-mainObj.map.tileDrift.x;
                var tileY=mainObj.map.height-((rTileY>>(mainObj.BPT-mainObj.map.scale))-mainObj.map.tileDrift.y);
                var pTiles=cEl('div');
                pTiles.style.position='absolute';
                pTiles.style.top=(tileY-256)+'px';
                pTiles.style.left=tileX+'px';
                pTiles.style.width=mainObj.tileSize+'px';
                pTiles.style.height=mainObj.tileSize+'px';
                pTiles.style.backgroundColor=color;
                pTiles.id=str;
                mainObj.mpLayer.mapLayers.active.folder[pTiles.id]=pTiles;
                mainObj.mpLayer.mapLayers.active.box.appendChild(pTiles)
            }
            else{
                var pTiles=mainObj.mpLayer.mapLayers.active.folder[str]
            }
            var pointX=(obj.data.sx>>(mainObj.BPT-mainObj.map.scale))&255;
            var pointY=(obj.data.sy>>(mainObj.BPT-mainObj.map.scale))&255;
            pointX=pointX<0?0:pointX;
            pointY=pointY<0?0:pointY;
            var point=obj.makeIcon(pointX,pointY);
            pTiles.appendChild(point)
        }
    }

};

function RouteMakerObj(mainObj,name){
    this.formsIds=['routeForm'];
    this.activeForm='';
    this.formTranslate={
        from:'st',to:'en',th:'through',criterium1:'q',criterium3:'q',pay0:'tr',pay1:'tr'
    };
    this.oldFrom='';
    this.oldTo='';
    this.init=function(){
        var obj=mainObj[name];
        for(var i=0;i<obj.formsIds.length;i++){
            if(gEl(obj.formsIds[i])){
                obj.handl(obj.formsIds[i])
            }
        }
        if(result&&(result.type=='route')&&!printFlag){
            gEl('subRoute').style.visibility='visible';
            gEl('subRoute').style.display='block';
            obj.setForm()
        }
    };
    this.setForm=function(){
        var obj=mainObj[name];
        var data=result.data;
        for(var i in obj.formTranslate){
            if(data[obj.formTranslate[i]]||(data[obj.formTranslate[i]]==0)){
                if((gEl(i).type!='radio')&&(gEl(i).nodeName.toLowerCase!='select')){
                    gEl(i).value=data[obj.formTranslate[i]]
                }
                if(gEl(i).type=='radio'){
                    if((data[obj.formTranslate[i]]==2)&&(i=='criterium1')){
                        gEl(i).checked=true
                    }
                    else if((data[obj.formTranslate[i]]==1)&&(i=='criterium3')){
                        gEl(i).checked=true
                    }
                    else if((data[obj.formTranslate[i]]==0)&&(i=='pay0')){
                        gEl(i).checked=true
                    }
                    else if((data[obj.formTranslate[i]]==1)&&(i=='pay1')){
                        gEl(i).checked=true
                    }
                }
            }
        }
        if(routeMsg){
            obj.makeAnswer(routeMsg,'route')
        }
    };
    this.handl=function(frId){
        var obj=mainObj[name];
        dom.addEvent(gEl(frId),'submit',obj.sendData,false)
    };
    this.getData=function(){
        var obj=mainObj[name];
        var trgForm=obj.activeForm;
        var data=new Object();
        var inputs=trgForm.getElementsByTagName('input');
        var selects=trgForm.getElementsByTagName('select');
        for(var i=0;i<inputs.length;i++){
            if(inputs[i].type!='radio'){
                data[obj.formTranslate[inputs[i].id]]=inputs[i].value
            }
            else{
                if(inputs[i].checked==true){
                    data[obj.formTranslate[inputs[i].id]]=inputs[i].value
                }
            }
        }
        for(var i=0;i<selects.length;i++){
            data[obj.formTranslate[selects[i].id]]=selects[i].value
        }
        return data
    };
    this.makeUrl=function(ids,typ){
        var obj=mainObj[name];
        var data=obj.getData(ids);
        var str='';
        var stPrefix='fr=route:';
        var strFrom=mainObj.checkStr(gEl('from').value);
        var strThrough=mainObj.checkStr(gEl('through').value)?'>'+mainObj.checkStr(gEl('through').value):'';
        var strTo='>'+mainObj.checkStr(gEl('to').value);
        var stString=strFrom+strTo;
        var pay='&q='+data.q;
        var cr='&tr='+data.tr;
        var loop=!typ?'&loop=route':'';
        var server=window.location.host;
        var urlStr='http://'+server+'/?'+stPrefix+encodeURI(stString)+pay+cr+loop+'&st=search'+'&mp='+engine.map.typ+'&ocx='+engine.center.x+'&ocy='+engine.center.y+'&ozm='+engine.map.scale;
        return urlStr
    };
    this.sendData=function(e){
        var obj=mainObj[name];
        dom.stopEvent(e);
        dom.cancelDef(e);
        var trg=dom.getTarget(e);
        obj.activeForm=trg;
        var urlStr=obj.makeUrl(trg.ids,0);
        var fold=gEl('routeFold');
        fold.innerHTML='';
        mainObj.getData(fold,urlStr)
    };
    this.reSetForm=function(answ,ids){
        var obj=mainObj[name];
        var specStr='';
        var err='';
        if(answ.endMsg){
            var sel=obj.makeSelect(answ.endMsg);
            var trg=gEl('to');
            obj.oldTo=trg.value;
            trg.parentNode.replaceChild(sel,trg);
            sel.id='to';
            dom.addEvent(sel,'change',obj.backToInput,false);
            specStr+='Je potřeba upřesnit cíl cesty \n'
        }
        if(answ.startMsg){
            var sel=obj.makeSelect(answ.startMsg);
            var trg=gEl('from');
            obj.oldFrom=trg.value;
            trg.parentNode.replaceChild(sel,trg);
            sel.id='from';
            dom.addEvent(sel,'change',obj.backToInput,false);
            specStr+='Je potřeba upřesnit začátek cesty \n'
        }
        if(answ.errorMsg){
            for(var i=0;i<answ.errorMsg.length;i++){
                if(answ.errorMsg[i]==0){
                    err+='Nebyla nalezana žádná trasa \n'
                }
                if(answ.errorMsg[i]==1){
                    err+='Nebyla nalezena výchozí lokace \n'
                }
                if(answ.errorMsg[i]==2){
                    err+='Nebyla nalezana cílová lokace \n'
                }
                if(answ.errorMsg[i]==3){
                    err+='Nebyla nalezana průchozí lokace \n'
                }
                if(answ.errorMsg[i]==4){
                    err+='Došlo k chybě na straně serveru \n'
                }
                if(isNaN(answ.errorMsg[i])){
                    err+=answ.errorMsg[i]
                }
            }
        }
        if((err!='')||(specStr!='')){
            var note='Trasu nelze zobrazit z těchto důvodů: \n';
            if(err!=''){
                note+=err
            }
            if(specStr!=''){
                note+=specStr
            }
        }
        alert(note)
    };
    this.makeSelect=function(data){
        var obj=mainObj[name];
        var co=cEl('select');
        co.name='en';
        co.id='fakeItem';
        var str='';
        for(var i=0;i<data.length;i++){
            var op=cEl('option');
            var txt=cTxt(data[i]);
            op.value=data[i];
            op.appendChild(txt);
            co.appendChild(op)
        }
        var usOp=cEl('option');
        usOp.value='byHand';
        usOp.appendChild(cTxt('Zadat adresu znovu...'));
        dom.addEvent(co,'keypress',dom.stopEvent,false);
        dom.addEvent(co,'keydown',dom.stopEvent,false);
        co.appendChild(usOp);
        return co
    };
    this.backToInput=function(e){
        var obj=mainObj[name];
        var trg=dom.getTarget(e);
        if(trg.value=='byHand'){
            var co=cEl('input');
            co.name=gEl(trg.id).name;
            co.value=(trg.id=='to')?obj.oldTo:obj.oldFrom;
            var newIds=(trg.id=='to')?'to':'from';
            dom.removeEvent(trg,'change',obj.backToInput,false);
            var a=trg.parentNode.replaceChild(co,trg);
            a=null;
            co.id=newIds;
            gEl(newIds).focus();
            dom.addEvent(gEl(newIds),'keypress',dom.stopEvent,false);
            dom.addEvent(gEl(newIds),'keydown',dom.stopEvent,false)
        }
    };
    this.makeAnswer=function(answerData,answerIds){
        var obj=mainObj[name];
        if(answerData.ok){
            window.location=obj.makeUrl(obj.activeForm.id,1)
        }
        else{
            obj.reSetForm(answerData,answerIds)
        }
    }

};

function fillPointInfo(field){
    engine.vizitka.makeVizitka(field)
};

function reSetFromCheck(checkField){
    if(!engine){
        firstCheck.set(checkField)
    }
    else{
        engine.reSet(checkField)
    }

};

function exeAnswer(data,loop){
    if(loop=='route'){
        engine.routeMaker.makeAnswer(data,'loop')
    }
    else if(loop=='mail'){
        if(typeof(sendMail)!='undefined'){
            sendMail.makeAnswer(data,'loop')
        }
    }
    else if(loop.indexOf('coord')!=-1){
        engine.repairCoord.makeAnswer(data,loop)
    }

};

var config={
    tileSize:256,encoding:'utf8',portOnly:false,map:{
        type:'base',mapServer:'http://mapserver.mapy.cz',apiServer:'chili:8520',firmServer:null,zoom:10,maxSize:6
    }
    ,basisAuthor:new Array(),mapLimit:{
        top:0,right:0,bottom:0,left:0,drift:5
    }
    ,mapLayers:{
        basis:{
            position:'bott',type:'tile',source:'def',running:1,customQuery:null
        }
        ,trasa:{
            position:'1',type:'tile',source:'',running:0,customQuery:null
        }
        ,active:{
            position:'top',type:'point',source:'def',running:1,customQuery:null
        }
    }
    ,mapPort:{
        type:'dynamic',size:[]
    }
    ,mapControls:{
        type:'full',menuBg:0,menu:1,data:0,dataAllow:[1,1,0,1,1],bsSwitch:[0,1],basis:['base','ophoto','hybrid','tourist','ophoto5x'],basisName:['zakladni','ortofoto','hybridni','turisticka','historicka'],move:1,zoom:'full',zoomAllow:[0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0],extend:['Fullscreen','gps','Ruler'],mouse:1,keyboard:1,scale:1,north:1
    }
    ,authors:new Object(),mapCommands:{
        move:1,cut:1
    }
    ,mapCenter:{
        x:'134217728',y:'134217728'

    }
    ,mapPoints:{
        info:0,icon:[20,20],logo:[20,20],result:[32,41]
    }
    ,iconUrls:new Array(),pointServer:new Array(),pointDef:new Object(),disabledPoints:new Array(),extend:{
        allow:0,extApiServer:null,customQuery:null
    }

};

function pt(){
    this.endFlag=0;
    this.typeFold=new Object();
    this.foldInited=false;
    this.code=typeof(config.encoding)!='undefined'?'encoding='+config.encoding:'';
    this.init=function(){
        for(i=0;i<config.mapControls.basis.length;i++){
            config.pointDef[config.mapControls.basis[i]]=new Array()
        }
    };
    this.getPt=function(){
        var str=config.map.apiServer;
        document.write("<sc"+"ript src='"+str+"/baseparams?"+this.code+"' type='text/javascript'></sc"+"ript>")
    };
    this.setPointServer=function(param){
        for(var i=0;i<param.length;i++){
            config.pointServer[i]=param[i];
            document.write("<sc"+"ript src='"+param[i]+"/poiparams?loop="+i+"' type='text/javascript'></sc"+"ript>")
        }
        this.endFlag=param.length-1
    };
    this.initPointDef=function(num){
        for(i in config.pointDef){
            this.typeFold[i]=new Object();
            for(var j=0;j<num;j++){
                config.pointDef[i][j]=new Object();
                config.pointDef[i][j].qs='';
                config.pointDef[i][j].points=new Object()
            }
        }
    };
    this.setPointsDef=function(param,num){
        if(!this.foldInited){
            this.init();
            this.initPointDef(config.pointServer.length);
            this.foldInited=true
        }
        var cnt=0;
        var field=new Array();
        if(config.disabledPoints.length>0){
            for(var i=0;i<param.length;i++){
                for(var j=0;j<config.disabledPoints.length;j++){
                    if(param[i].typ!=config.disabledPoints[j]){
                        field[cnt]=param[i];
                        cnt++
                    }
                }
            }
        }
        else{
            field=param
        }
        for(var i=0;i<field.length;i++){
            for(var k=0;k<field[i].basis.length;k++){
                this.typeFold[field[i].basis[k]]['pt'+field[i].typ]=new Object();
                this.typeFold[field[i].basis[k]]['pt'+field[i].typ].server=num;
                this.typeFold[field[i].basis[k]]['pt'+field[i].typ].typ=field[i].typ;
                this.typeFold[field[i].basis[k]]['pt'+field[i].typ].iconUrl=field[i].url;
                this.typeFold[field[i].basis[k]]['pt'+field[i].typ].base=field[i].basis[k]
            }
        }
        if(num==this.endFlag){
            this.setPointConf()
        }
    };
    this.setPointConf=function(){
        var obj=config.pointDef;
        for(i in this.typeFold){
            var fold=this.typeFold[i];
            for(j in fold){
                num=fold[j].server;
                base=fold[j].base;
                typ=fold[j].typ;
                icon=fold[j].iconUrl;
                obj[base][num].points['tp'+typ]=icon;
                obj[base][num].qs+='type='+typ+'&'
            }
        }
        this.typeFold=null
    }

};

if(mapiServer!=''){
    config.map.apiServer=mapiServer
};

if(routeSource!=''){
    config.mapLayers.trasa.source=routeSource
};

pointConfig=new pt();
pointConfig.getPt();
function FirstCheckObj(){
    this.init=function(){
        this.x=this.getNumToHex(config.mapCenter.x);
        this.y=this.getNumToHex(config.mapCenter.y);
        this.zoom=config.map.zoom;
        this.basis=config.map.type;
        this.server=config.map.apiServer;
        this.check()
    };
    this.getNumToHex=function(num){
        var c=new Number(num);
        var out=c.toString(16);
        while(out.length<7){
            out='0'+out
        }
        return out
    };
    this.check=function(){
        var urlStr=''+this.server+'/check?'+'encoding='+config.encoding+'&typ='+this.basis+'&tile='+this.zoom+'_'+this.x+'_'+this.y;
        document.write('<sc'+'ript type="text/javascript" src="'+urlStr+'"><\/scr'+'ipt>')
    };
    this.set=function(field){
        if(field.scale[config.map.zoom-1]==0){
            var zoom=this.checkScale(field.scale,config.map.zoom);
            if(zoom.min){
                config.map.zoom=zoom.min
            }
            else if(zoom.max){
                config.map.zoom=zoom.max
            }
            else{
                window.location='http://'+window.location.host
            }
        }
    };
    this.checkScale=function(sc,num){
        var min=null;
        var max=null;
        var cntMin=num;
        var cntMax=num;
        for(var i=0;i<sc.length;i++){
            if(!sc[cntMin]&&!min){
                cntMin--
            }
            else{
                min=cntMin+1;
                break
            }
            if(!sc[cntMax]&&!max){
                cntMax++
            }
            else{
                max=cntMax+1
            }
        }
        return{
            min:min,max:max
        }
    }

};

function Cookie(){
    this.cookieName='maps';
    this.cook=null;
    this.set=function(name,value){
        if(!this.cook){
            this.cook=new Object()
        }
        this.cook[name]=value;
        document.cookie=this.cookieName+"="+serialize(this.cook)+";path=/"
    };
    this.read=function(name){
        if(!this.cook){
            var userCookies=document.cookie.split(";");
            for(i in userCookies){
                var userCookie=userCookies[i].split("=");
                var cookieName=userCookie[0].replace(/^\s*|\s*$/g,"");
                if(cookieName==this.cookieName){
                    this.cook=new Object();
                    this.cook=unserialize(userCookie[1])
                }
            }
        }
        if(this.cook&&this.cook[name]){
            return this.cook[name]
        }
        else{
            return false
        }
    };
    this.reSet=function(){
        var showMenu=false;
        if(engine.cookie.read('fullscreen')==1){
            engine.controls.fullscreen.buttonAction()
        }
        if(engine.cookie.read('gps')==1){
            var param=new Object();
            param.type='fake';
            engine.controls.gps.buttonAction(param)
        }
        if(engine.cookie.read('extend')==1){
            extend.open()
        }
    }

};

var progressBar={
    pBarDiv:null,cnt:0,pos:5,msgs:new Array,open:function(title){
        var pBarDiv=cEl('div');
        pBarDiv.className='pBar';
        pBarDiv.id='pd_'+this.cnt;
        pBarDiv.style.position='absolute';
        var size=engine.getDocSize();
        pBarDiv.style.top=Math.round(parseInt(size.height)/2-30+this.pos)+'px';
        pBarDiv.style.left=Math.round(parseInt(size.width)/2-300+this.pos)+'px';
        document.getElementsByTagName('body')[0].appendChild(pBarDiv);
        var text=document.createTextNode(title);
        pBarDiv.appendChild(text);
        var info=cEl('span');
        info.innerHTML='Kliknutím zavřete toto okno, požadovaná operace poběží dále na pozadí.';
        info.className='infoText';
        pBarDiv.appendChild(info);
        var closeIM=cEl('img');
        closeIM.src='http://1.im.cz/mapyp/img/ico_close.gif';
        closeIM.className='closeImg';
        closeIM.id='pb_'+this.cnt;
        closeIM.title='Kliknutím zavřete toto okno, požadovaná operace poběží dále na pozadí.';
        dom.addEvent(closeIM,'click',progressBar.close,false);
        pBarDiv.appendChild(closeIM);
        this.msgs[this.msgs.length]=pBarDiv,this.cnt=this.cnt+1;
        this.pos=this.pos+10;
        return pBarDiv.id
    }
    ,close:function(e){
        if(e.type=='fake'){
            var trg=e.msgid
        }
        else{
            var trg=dom.getTarget(e);
            var trg=trg.id
        }
        var tmp=new Array();
        var br=null;
        for(i=0;i<progressBar.msgs.length;i++){
            if(progressBar.msgs[i].id==trg.replace("pb_","pd_")){
                br=progressBar.msgs[i];
                for(k=0;k<progressBar.msgs.length;k++){
                    if(progressBar.msgs[i]!=progressBar.msgs[k]){
                        tmp[tmp.length++]=progressBar.msgs[k]
                    }
                }
                progressBar.msgs=tmp
            }
        }
        if(br){
            br.parentNode.removeChild(br)
        }
    }

};

