Ext.onReady(function() {

    var recordFields = [
    {
        name : 'id',
        mapping : 'service_id'
    },

    {
        name : 'displayname',
        mapping : 'display_name'
    },

    {
        name : 'checkcommandargs',
        mapping : 'check_command_args'
    },

    {
        name : 'checkinterval',
        mapping : 'check_interval'
    },

    {
        name : 'retryinterval',
        mapping : 'retry_interval'
    },

    {
        name : 'notifyonwarning',
        mapping : 'notify_on_warning'
    }
    ];

    var remoteJsonStore = new Ext.data.JsonStore ({
        fields          : recordFields,
        url             : 'http://localhost:8080/ext-test/chapter-07/dataQuery.php',
        totalProperty   : 'totalCount',
        root            : 'records',
        id              : 'ourRemoteStore',
        autoLoad        : false,
        remoteSort      : true
    });

    var colorTextBlue = function(id) {
        return '<span style="color: #0000FF;">' + id + '</span>';
    }

    var stylizeDisplayName = function(displayname, column, record) {
        var displayname = record.get('displayname');

        return String.format('{0}<br />', displayname);
    }

    var columnModel = [
    {
        header      : 'Service ID',
        dataIndex   : 'serviceid',
        sortable    : true,
        width       : 50,
        resizable   : false,
        hidden      : true,
        renderer    : colorTextBlue
    },
    {
        header      : 'Display Name',
        dataIndex   : 'displayname',
        sortable    : true,
        hideable    : false,
        width       : 75,
        id          : 'displaynameCol',
        renderer    : stylizeDisplayName
    },
    {
        header      : 'Check Command Arguments',
        dataIndex   : 'checkcommanargs',
        sortable    : false,
        hideable    : false,
        width       : 150
    },
    {
        header      : 'Check Interval',
        dataIndex   : 'checkinterval',
        sortable    : false,
        hideable    : false,
        width       : 85
    },
    {
        header      : 'Retry Interval',
        dataIndex   : 'retryinterval',
        sortable    : false,
        hideable    : false,
        width       : 85
    },
    {
        header      : 'Retry on Warning',
        dataIndex   : 'retryonwarning',
        sortable    : false,
        hideable    : false,
        width       : 95
    }
    ];

    var pagingToolbar = {
        xtype       : 'paging',
        store       : remoteJsonStore,
        pageSize    : 50,
        displayInfo : true
    }

    var grid = {
        xtype               : 'grid',
        columns             : columnModel,
        store               : remoteJsonStore,
        loadMask            : true,
        bbar                : pagingToolbar,
        autoExpandColumn    : 'displaynameCol'
    }

    new Ext.Window({
        height  : 350,
        width   : 540,
        border  : false,
        layout  : 'fit',
        items   : grid
    }).show();

    Ext.StoreMgr.get('ourRemoteStore').load({
        params : {
            start : 0,
            limit : 50
        }
    });

});
