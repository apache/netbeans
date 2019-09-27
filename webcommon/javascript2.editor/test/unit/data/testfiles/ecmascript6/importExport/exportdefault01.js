export default extend({

    content: computed("source.[]", function() {
        return this.compute();
    }),

    compute() { }
});