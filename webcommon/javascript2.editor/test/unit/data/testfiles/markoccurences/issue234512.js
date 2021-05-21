function getFile(file) {
    var contentType = guessContentType();
    return function serveFile(req, res) {
        fs.stat(pth, function(err, stat) {
            var mtime = stat.mtime;
            var hdrs = {
                'Content-Type': contentType,
                'Last-Modified': mtime
            };
            
        });
    }        
} 