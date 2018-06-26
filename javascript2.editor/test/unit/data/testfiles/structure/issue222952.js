config = loadConfig('./server-config.json') || {name: "ppp", hh: 10};

function Query(booleanQ, valueQ){

    this.setResults = function(items){
        var tmp = [];
        for(var j=0;j<items.length;j++){
            tmp[j]={
                slideid: items[j].slideid
            };
        }
        this.results = tmp;
    };
}