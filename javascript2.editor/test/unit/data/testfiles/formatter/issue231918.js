panels.elements.createSidebarPane("AngularJS Properties",
        function(sidebar) {
            panels.elements.onSelectionChanged.addListener(function test() {
                sidebar.setExpression("(" + getPanelContents.toString() + ")()");
            });
        });


        var angularPanel = panels.create();
