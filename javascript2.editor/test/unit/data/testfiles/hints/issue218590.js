var Test = {
    old: 1,
    set years(count) {
        this.old = count + 1; // dupl. name of prop. count
    },
    get years() { // // dupl. name of prop. years
        return this.old; // dupl. name of prop. old
    }
};