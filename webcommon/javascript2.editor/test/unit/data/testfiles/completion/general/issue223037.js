var slideIndex = {
    host: host,
    content: {
        title: "",
        numberOfSlide: 0,
        slides: {
            titles: [],
            simpleSlide: []

        }
    },
    sendResponse: function() {
        this.content.slides.titles[0] = {};
        var index = 0;
        for (index; index < 10; index++) {
            this.content.slides.titles[index] = {
                note: " some note"
            };
        }

        this.content.slides.titles[2] = {
            author: "Matous",
            note: "another note" ,
            getDescription : function () {
                return this.note + this.author;
            }
        };
        this.content.slides.titles.reverse();
    }
};