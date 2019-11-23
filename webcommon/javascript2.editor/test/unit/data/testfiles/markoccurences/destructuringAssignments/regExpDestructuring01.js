var url = "https://netbeans.org/bugzilla/show_bug.cgi?id=242387";

var parsedURL = /^(\w+)\:\/\/([^\/]+)\/(.*)$/.exec(url);
console.log(parsedURL); // ["https://netbeans.org/bugzilla/show_bug.cgi?id=242387", "https", "netbeans.org", "bugzilla/show_bug.cgi?id=242387", index: 0, input: "https://netbeans.org/bugzilla/show_bug.cgi?id=242387"]

var [, protocol, fullhost, fullpath] = parsedURL;

console.log(protocol); // "https"
console.log(fullhost); // netbeans.org
console.log(fullpath); // bugzilla/show_bug.cgi?id=242387
