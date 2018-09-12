var man1 = {
    name: "xyz",
    addr: {
        h_addr: {
            doorNo: "10",
        }
    }
};


var man2 = man1;
var addr1 = man2.addr;
var addr2 = addr1;
var add = addr2.h_addr;
add.doorNo = "11";


var company = {
    name: "CCC",
    manager: {
        dept: "something",
        dtl: man2
    }
}

var addr3 = company.manager.dtl.addr.h_addr;
addr3.doorNo = "50";

var addr4 = company.manager.dtl.addr;
addr4.h_addr.doorNo = "70";



