var pdel = (function() {
    return {
    f2: function() {
        return this.f1(); // ctr+click does not work on f1
    },         
    f1: function() {
        return 'ahoj';
    }
  }
  })();