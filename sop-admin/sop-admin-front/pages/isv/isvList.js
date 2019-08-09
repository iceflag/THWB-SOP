lib.use(['element', 'table', 'form'], function () {
    var table = layui.table;
    var layer = layui.layer;
    var form = layui.form;
    var isvTable;

    var STATUS_ENUM = {
        '1': '<span class="x-green">已启用</span>'
        ,'2': '<span class="x-red">已禁用</span>'
    }
    var SIGN_TYPE_ENUM = {
        '1': 'RSA2'
        ,'2': 'MD5'
    }

    // 渲染表格
    var renderTable = function (postData) {
        layer.load(2);
        isvTable = table.render({
            elem: '#isvTable'
            , toolbar: '#toolbar'
            , url: ApiUtil.createUrl('isv.info.page')
            // 对分页请求的参数：page、limit重新设定名称
            ,request: {
                pageName: 'pageIndex' //页码的参数名称，默认：page
                ,limitName: 'pageSize' //每页数据量的参数名，默认：limit
            }
            , page: true
            , headers: {access_token: ApiUtil.getAccessToken()}
            , where: postData
            , cellMinWidth: 80 //全局定义常规单元格的最小宽度，layui 2.2.1 新增
            , cols: [[
                {field: 'id', title: 'id', width: 80}
                , {field: 'appKey', title: 'appKey', width: 250}
                , {field: 'secret', title: 'secret', width: 80, templet: function (row) {
                        if (row.signType == 2) {
                            return '<button class="layui-btn layui-btn-xs" onclick="View.secret(\'' + row.secret + '\')">查看</button>';
                        } else {
                            return '';
                        }
                }}
                , {field: '', title: '公私钥', width: 80, templet: function (row) {
                        if (row.signType == 1) {
                            return '<button class="layui-btn layui-btn-xs" onclick="View.pubPriKey(\'' + row.pubKey + '\', \'' + row.priKey + '\')">查看</button>';
                        } else {
                            return '';
                        }
                }}
                , {field: 'signType', title: '签名类型', width: 80, templet: function (row) {
                        return SIGN_TYPE_ENUM[row.signType+''] || 'unknown';
                    }}
                , {field: 'roleList', title: '角色', templet: function (row) {
                        var html = [];
                        var roleList = row.roleList;
                        for (var i = 0; i < roleList.length; i++) {
                            html.push(roleList[i].description);
                        }
                        return html.join(', ');
                    }}
                , {field: 'status', title: '状态', width: 80, templet: function (row) {
                        var display = STATUS_ENUM[row.status + ''];
                        return display ? display : 'unknown';
                    }}
                , {field: 'gmtCreate', title: '添加时间', width: 160}
                , {field: 'gmtModified', title: '修改时间', width: 160}
                , {
                    fixed: 'right', title: '操作', width: 100, templet: function (row) {
                        return '<a class="layui-btn layui-btn-xs layui-btn-normal" lay-event="edit">修改</a>';
                    }
                }
            ]]
            ,parseData: function(res){ //将原始数据解析成 table 组件所规定的数据
                return {
                    "code": res.code, //解析接口状态
                    "msg": res.msg, //解析提示文本
                    "count": res.data.total, //解析数据长度
                    "data": res.data.list //解析数据列表
                };
            }
            ,done: function () {
                layer.closeAll('loading');
            }
        });

        //监听单元格事件
        table.on('tool(isvTableFilter)', function(obj) {
            if (obj.event === 'edit') {
                location.href = 'isvUpdate.html?id=' + obj.data.id;
            }
        });

        table.on('toolbar(isvTableFilter)', function(obj) {
            if (obj.event === 'add') {
                location.href = 'isvAdd.html';
            }
        });
    };

    form.on('submit(searchFilter)', function(data){
        var param = data.field;
        searchTable(param)
        return false;
    });

    /**
     * 查询表格
     * @param params
     */
    function searchTable(params) {
        var postData = {
            data: JSON.stringify(params || {})
        };
        if (!isvTable) {
            isvTable = renderTable(postData);
        } else {
            isvTable.reload({
                where: postData
            })
        }
    }

    searchTable();


    window.View = {
        secret: function (secret) {
            layer.alert(secret);
        }
        ,pubPriKey: function (pubKey, priKey) {
            var content = '<div style="width: 550px;padding: 10px;">公钥：<textarea class="layui-textarea" readonly="readonly">' + pubKey + '</textarea><br>' +
                '私钥：<textarea class="layui-textarea" readonly="readonly">' + priKey + '</textarea></div>';
            layer.open({
                type: 1,
                area: ['600px', '400px'],
                fix: false, //不固定
                shadeClose: true,
                shade:0.4,
                title: '公私钥',
                content: content
            });
        }
    }

});