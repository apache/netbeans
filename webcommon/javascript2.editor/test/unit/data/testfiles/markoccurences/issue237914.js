var job = {a: 1}; 

function JobParser(job, results) {
    this.parse = function() {
        var test = job;
        return job; // this is purple, should be black
    };   

}
job.a = 3;
window.console.log(new JobParser({d: 1}).parse());