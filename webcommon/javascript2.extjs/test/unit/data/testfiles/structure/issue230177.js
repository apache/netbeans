var ExampleDefaults = {
	width   : '100%',
	height  : '100%'
};
Ext.ns('App');
Ext.Loader.setConfig({ enabled : true, disableCaching : true });
Ext.Loader.setPath('Sch', '../../js/Sch');

Ext.require([
    'Sch.panel.SchedulerGrid'
]);


Ext.onReady(function() {
    App.SchedulerDemo.init();
});

Ext.define('MyTimeAxis', {
    extend : "Sch.data.TimeAxis", 
    continuous : false,

    generateTicks : function(start, end, unit, increment) {
        // Use our own custom time intervals for day time-axis
        if (unit === Sch.util.Date.DAY) {
            var ticks = [],
                intervalEnd;

            while (start < end) {
                if (start.getDay() === 6) {
                    // Saturday
                    start.setHours(7);
                    intervalEnd = Sch.util.Date.add(start, Sch.util.Date.HOUR, 6);
                } else {
                    start.setHours(7);
                    intervalEnd = Sch.util.Date.add(start, Sch.util.Date.HOUR, 13);
                }
            
                ticks.push({
                    start : start,
                    end : intervalEnd
                });
                start = Sch.util.Date.add(start, Sch.util.Date.DAY, 1);
            }
            return ticks;
        } else {
            return MyTimeAxis.superclass.generateTicks.apply(this, arguments);
        }
    }
});  
 
App.SchedulerDemo = {
    
    // Initialize application
    init : function() {
        Ext.define('Event', {
            extend : 'Sch.model.Event',
            fields : [
                {name: 'Title'},
                {name: 'Details'}
            ]
        });

        Ext.define('Zone', {
            extend : 'Sch.model.Range',
            fields: [
                'Type'
             ]
        });


		Sch.preset.Manager.registerPreset("cintelDay",
		{
			timeColumnWidth : 20,
			rowHeight: 20,              // Only used in horizontal orientation
			resourceColumnWidth : 120,  // Only used in vertical orientation
			displayDateFormat : 'Y-m-d',
			shiftUnit : "MINUTE",
			shiftIncrement : 30,
			defaultSpan : 6,            // By default, show 6 weeks

			timeResolution : {
				unit : "MINUTE",
				increment : 30
			},

			headerConfig : {
				middle : {
					unit : "MINUTE",
					
					increment : 30,
					renderer : function(start, end, cfg) {
						cfg.align = 'left';
						return Ext.Date.format(start, 'H:i');
					}
				},
				top : {
					unit : "MONTH",
					dateFormat : 'M Y'
				}
			}
		});

		Sch.preset.Manager.registerPreset('cintelWeek', {
				timeColumnWidth : 10,
			displayDateFormat : 'G:i',
			shiftIncrement : 1,
			shiftUnit : "WEEK",
			timeResolution : {
				unit : "MINUTE",
				increment : 30
			},
			headerConfig : {
				middle: {
					unit : "DAY",
					//dateFormat:'D'
					
					renderer : function(start, end, cfg) {
						cfg.headerCls = 'sch-hdr-startend';
						return Ext.String.format('<span class="sch-hdr-start">{0}</span><span class="sch-hdr-end">{1}</span>', Ext.Date.format(start, 'G'), Ext.Date.format(end, 'G'));
					}
				},
				top : {
					unit : "DAY",
					dateFormat : 'D d M'
				}
			}
		});

        var start = Ext.Date.clearTime(new Date());
        var end = Ext.Date.clearTime(new Date());
        start.setHours(7, 0, 0, 0);
        end.setHours(20, 0, 0, 0);
		
        var startRepas = Ext.Date.clearTime(new Date());
        var endRepas = Ext.Date.clearTime(new Date());
        startRepas.setHours(11, 30, 0, 0);
        endRepas.setHours(14, 30, 0, 0);
		
		
	zoneStore = Ext.create('Ext.data.JsonStore', {
            model : 'Zone',
            data : [
                {
                    StartDate   : startRepas,
                    EndDate     : endRepas,
                    Type        : '',
                    Cls         : 'myZoneStyle'
                }
            ]
        });

	var ta = new MyTimeAxis();	

	var sched = Ext.create("Sch.panel.SchedulerGrid", {
		height : ExampleDefaults.height,
		width : ExampleDefaults.width,

		passStartEndParameters : true,
		eventBarTextField : 'Title',
		timeAxis : ta,
		
		viewPreset  : 'cintelDay',
		startDate   : start,
		endDate     : end,
				  
		orientation     : 'vertical',
		
		constrainDragToResource : false,
		snapToIncrement         : false,
		
		eventResizeHandles : 'end',

		eventBodyTemplate : new Ext.XTemplate(
			'{Title} {Type}'
		),

		eventRenderer : function(event, resource, data) {
			return event.data;
		},

		resizeValidatorFn : function(resourceRecord, eventRecord, start, end) {
			// If the view is per week, duration not allowed
			if (sched.viewPreset === "cintelDay") {
				return true;
			}
			return false;
		},

		lockedViewConfig : {
			stripeRows : false,
			getRowClass : function(resource) {
				return resource.data.Name;
			}
		},

		// Setup static columns
		columns : [
			{header : 'Name', sortable:true, width:100, dataIndex : 'Name'}
		],


					
		// Store holding all the resources
		resourceStore : Ext.create("Sch.data.ResourceStore", {
			autoLoad: true,
			model : 'Sch.model.Resource',
			proxy : {
				type : 'ajax',
				url : '../rest/schedulerResources',
				reader : {
					type : 'json'
				}
			}
		}),

		// Store holding all the events
		eventStore : Ext.create("Sch.data.EventStore", {
			autoLoad: true,
			autoSync: true,
			model : 'Event',
			proxy : {
					type : 'ajax',
					url : '../rest/schedulerEvents',
					reader : {
							type : 'json'
					}
			}

		}),

		listeners : {
			eventcontextmenu    : this.onEventContextMenu,
			eventdblclick : this.onEventDblClick,
			schedulecontextmenu : this.onScheduleContextMenu,
		},
		plugins : [
//                    'bufferedrenderer',
				this.zonePlugin = Ext.create("Sch.plugin.Zones", {
						// If you want, show some extra meta data for each zone
						innerTpl : '<span class="zone-type">{Type}</span>',
						store : zoneStore
				})
		],
		tbar : [
			{
				id: 'span3',
				enableToggle: true,
				text: 'Date',
				toggleGroup: 'span',
				scope : this,
				menu :     Ext.create('Ext.menu.DatePicker', {
					handler: function(dp, date){

						if (sched.viewPreset === "cintelDay")
						{
							sched.setTimeSpan(Ext.Date.add(date, Ext.Date.HOUR, 7), Ext.Date.add(date, Ext.Date.HOUR, 20));
							var startRepas = new Date(date);
							var endRepas = new Date(date);
							startRepas.setHours(11, 30, 0, 0);
							endRepas.setHours(14, 30, 0, 0);
							zoneStore.clearData();
							zoneStore.add( new Zone(                
								{
									StartDate   : startRepas,
									EndDate     : endRepas,
									Type        : '',
									Cls         : 'myZoneStyle'
								}));
						} else {
							sched.setTimeSpan(date, Ext.Date.add(date, Ext.Date.DAY, 7));
							zoneStore.clearData();
							for (var i=0; i<7 ; i++) {
								var startRepas = Ext.Date.add(date, Ext.Date.DAY, i);
								var endRepas = Ext.Date.add(date, Ext.Date.DAY, i);
								startRepas.setHours(11, 30, 0, 0);
								endRepas.setHours(14, 30, 0, 0);
								
								zoneStore.add( new Zone(                
									{
										StartDate   : startRepas,
										EndDate     : endRepas,
										Type        : '',
										Cls         : 'myZoneStyle'
									})
								);
							}							
						}
						sched.eventStore.load();
						//sched.getDockedItems('toolbar')[ 0 ].child('span3').setValue(date);
					},
					scope : this
				})
			},
			{
				text : 'Jour',
				pressed : true,
				//iconCls : 'icon-vertical',
				enableToggle : true,
				toggleGroup : 'echelle',
				handler : function() {
					
					var date = Ext.Date.clearTime(sched.startDate);
					sched.eventResizeHandles="end";
					sched.setTimeSpan(Ext.Date.add(date, Ext.Date.HOUR, 7), Ext.Date.add(date, Ext.Date.HOUR, 20));
					sched.switchViewPreset('cintelDay', Ext.Date.add(date, Ext.Date.HOUR, 7), Ext.Date.add(date, Ext.Date.HOUR, 20));
					sched.setOrientation('vertical');

					sched.eventStore.load();
					
					var startRepas = new Date(date);
					var endRepas = new Date(date);
					startRepas.setHours(11, 30, 0, 0);
					endRepas.setHours(14, 30, 0, 0);
					zoneStore.clearData();
					zoneStore.add( new Zone(                
						{
							StartDate   : startRepas,
							EndDate     : endRepas,
							Type        : '',
							Cls         : 'myZoneStyle'
						}));
				}
			},
			{
				text : 'Semaine',
				enableToggle : true,
				//iconCls : 'icon-horizontal',
				toggleGroup : 'echelle',
				handler : function() {
					var date = Ext.Date.clearTime(sched.startDate);
					sched.setTimeSpan(date, Ext.Date.add(date, Ext.Date.DAY, 7));

					sched.eventResizeHandles="none";
					
					sched.switchViewPreset('cintelWeek', date, Ext.Date.add(date, Ext.Date.DAY, 7));
					sched.setOrientation('horizontal');
					sched.eventStore.load();
					zoneStore.clearData();
					for (var i=0; i<7 ; i++) {
						var startRepas = Ext.Date.add(date, Ext.Date.DAY, i);
						var endRepas = Ext.Date.add(date, Ext.Date.DAY, i);
						startRepas.setHours(11, 30, 0, 0);
						endRepas.setHours(14, 30, 0, 0);
						
						zoneStore.add( new Zone(                
							{
								StartDate   : startRepas,
								EndDate     : endRepas,
								Type        : '',
								Cls         : 'myZoneStyle'
							})
						);
					}
				}
			}				
		]
		,
		tooltipTpl : new Ext.XTemplate(
			'<dl class="eventTip">', 
			'{Title} {Type}',
			'</dl>'
		).compile()

	});

	sched.render(Ext.getBody());
	
	var task = {
			run: function(){
					sched.eventStore.load();
					sched.refresh();
				},
				interval:60000
	};
	var runner = new Ext.util.TaskRunner(60000); // 50 ms timer resolution
	runner.start(task);		
},

//"eventdblclick", "eventcontextmenu", "scheduledblclick", "schedulecontextmenu"
    onEventContextMenu: function (s, rec, e) {
		e.stopEvent();
		console.log("PlannerCommand;onEventContextMenu;" + rec.data.Id);
    },
	onEventDblClick:function (s, rec, e) {
		e.stopEvent();
		console.log("PlannerCommand;onEventDblClick;" + rec.data.Id);
	},
	onScheduleContextMenu:function (scheduler, date, index, resource, e) {
		e.stopEvent();
	}
};