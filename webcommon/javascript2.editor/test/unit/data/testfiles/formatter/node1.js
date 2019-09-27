#!/usr/bin/env node
'use strict';


function getStdin(cb) {
	var ret = '';

}

getStdin(function (data) {
	process.stdout.write(strip(data));
});
