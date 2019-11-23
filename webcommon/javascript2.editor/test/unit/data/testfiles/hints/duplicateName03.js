var CONFIG = {
    YEAR_OFFSET : {
        key:"year_offset", 
        value:0, 
        supercedes:[
            "pagedate", 
            "selected", 
            "mindate", 
            "maxdate",
            "pagedate",
            "pagedate",
            ]
        },
    TODAY : {
        key:"today", 
        value:new Date(), 
        supercedes:["pagedate"],
        key: "tommorow"
    },
    TODAY : {
        key:"today", 
        value:new Date(), 
        supercedes:["pagedate"],
    },
};