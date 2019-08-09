lib.config({
    base: '../../assets/lib/layuiext/module/'
}).extend({
    treetable: 'treetable-lay/treetable'
}).use(['element', 'table', 'form', 'treetable'], function () {
    var table = layui.table;
    var layer = layui.layer;
    var form = layui.form;
    var treetable = layui.treetable;

    // 渲染表格
    var renderTable = function (postData) {
        layer.load(2);
        var params = {
            data: JSON.stringify(postData || {})
        };
        treetable.render({
            elem: '#treeTable',
            treeColIndex: 1,
            treeSpid: 0,
            treeIdName: 'id',
            treePidName: 'parentId',
            treeDefaultClose: false,
            treeLinkage: false,
            url: ApiUtil.createUrl('service.instance.list'),
            headers: {access_token: ApiUtil.getAccessToken()},
            where: params,
            page: false,
            cols: [[
                {type: 'numbers'},
                {field: 'name', title: '服务名称(serviceId)', width: 200},
                {field: 'instanceId', title: 'instanceId', width: 250},
                {field: 'ipAddr', title: 'IP地址', width: 150},
                {field: 'serverPort', title: '端口号', width: 100},
                {field: 'status', title: '服务状态', width: 100, templet: function (row) {
                    if (row.parentId > 0) {
                        var html = [];
                        if (row.status === 'UP') {
                            return '<span class="x-green">已上线</span>'
                        }
                        if (row.status === 'OUT_OF_SERVICE') {
                            return '<span class="x-red">已下线</span>'
                        }
                    }
                    return '';
                }},
                {field: 'updateTime', title: '最后更新时间', width: 150},
                {fixed: 'right', title: '操作', width: 150, templet: function (row) {
                    if (row.parentId > 0) {
                        var html = [];
                        if (row.status === 'UP') {
                            html.push('<a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="offline">下线</a>');
                        }
                        if (row.status === 'OUT_OF_SERVICE') {
                            html.push('<a class="layui-btn layui-btn-xs" lay-event="online">上线</a>');
                        }
                        return html.join('');
                    }
                    return '';
                }}
            ]],
            done: function () {
                layer.closeAll('loading');
            }
        });

        //监听单元格事件
        table.on('tool(treeTableFilter)', function(obj) {
            if (obj.event === 'offline') {
                var data = obj.data;
                layer.confirm('确定要下线【'+data.name+'】吗?', {icon: 3, title:'提示'}, function(index){
                    var params = {
                        serviceId: data.name
                        , instanceId: data.instanceId
                    };
                    ApiUtil.post('service.instance.offline', params, function (resp) {
                        layer.alert('修改成功');
                    });
                    layer.close(index);
                });
            }
            if (obj.event === 'online') {
                var data = obj.data;
                layer.confirm('确定要上线【'+data.name+'】吗?', {icon: 3, title:'提示'}, function(index){
                    var params = {
                        serviceId: data.name
                        , instanceId: data.instanceId
                    };
                    ApiUtil.post('service.instance.online', params, function (resp) {
                        layer.alert('修改成功');
                    });
                    layer.close(index);
                });
            }
        });
    };

    renderTable();

    form.on('submit(searchFilter)', function(data){
        var param = data.field;
        renderTable(param)
        return false;
    });

});