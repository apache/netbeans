function MyViewModel() {
    this.buyer = {name: 'Franklin', credits: 250, date: new Date(), test: {a: 1}};
    this.seller = {name: 'Mario', credits: 5800, date: new Date(), test: {a: 1}};
    this.people1 = [
        {name1: 'Franklin', credits1: 250, date1: new Date(), test1: {a: 1}},
        {name1: 'Mario', credits1: 5800, date1: new Date(), test1: {a: 1}}
    ];

    this.people5 = {
        "id": 1,
        "group": [
            {name: 'Bert', lastName: 'Bert'},
            {name: 'Charles', lastName: 'Bert'},
            {name: 'Denise', lastName: 'Bert'}
        ]
    };

    this.seasons = ko.observableArray([
        {name: 'Spring', months: ['March', 'April', 'May']},
        {name: 'Summer', months: ['June', 'July', 'August']},
        {name: 'Autumn', months: ['September', 'October', 'November']},
        {name: 'Winter', months: ['December', 'January', 'February']}
    ]);

}
ko.applyBindings(new MyViewModel());
