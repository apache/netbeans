Ext.application({
    name: 'HelloExt',
    launch: function() {
        Ext.create('Ext.container.Viewport', { 
            layout: 'fit',
            items: [
            // strange coloring after this line
                {
                    title: 'Hello Ext',
                    html: 'Hello! Welcome to Ext JS.'
                }
            ]
        });
    } 
});