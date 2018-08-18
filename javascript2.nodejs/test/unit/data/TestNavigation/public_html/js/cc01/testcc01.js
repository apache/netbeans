var simple = require('./simpleModule');
var mess = require('./simpleModule').message;
var mOut = require('./simpleModule').Out;


simple.message.setCode(23);
mess.setCode(25);
mOut.output = 'bug';


