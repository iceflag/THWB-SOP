lib.importJs('../../assets/js/routerole.js')
    .use(['element', 'table', 'tree', 'form'], function () {
    var LIMIT_STATUS = {
        '1': '<span class="x-green">已开启</span>'
        ,'0': '<span class="x-red">已关闭</span>'
    }
    // 限流策略，1：漏桶策略，2：令牌桶策略
    var LIMIT_TYPE = {
        '1': '漏桶策略'
        ,'2': '令牌桶策略'
    }
    var element = layui.element;
    var form = layui.form;
    var updateForm = layui.Form('updateForm');
    var addForm = layui.Form('addForm');
    var authForm = layui.Form('authForm');
    var table = layui.table;

    var currentServiceId;
    var limitTable;
    var smTitle;


    form.on('submit(searchFilter)', function (data) {
        var param = data.field;
        param.serviceId = currentServiceId;
        searchTable(param)
        return false;
    });

    // 监听修改提交
    form.on('submit(updateWinSubmitFilter)', function(data) {
        var formData = updateForm.getData();
        if (!checkUpdateForm(formData)) {
            return false;
        }
        ApiUtil.post('route.limit.update', formData, function (resp) {
            layer.closeAll();
            limitTable.reload();
        });
        return false;
    });

    form.on('radio(limitTypeFilter)', function(data){
        $('.limit-type').hide();
        $('.type' + data.value).show();
    });

    element.on('tab(serviceTabFilter)', function(data){
        loadLimitTable(this.innerHTML);
    });

    function checkUpdateForm(formData) {
        var type = formData.limitType;
        if (type == 1) {
            if (!/^[1-9]\d*$/.test(formData.execCountPerSecond)) {
                layer.alert('每秒可处理请求数必须大于0')
                return false;
            }
            if (!formData.limitCode || formData.limitCode.length == 0) {
                layer.alert('错误码不能为空')
                return false;
            }
            if (!formData.limitMsg || formData.limitMsg.length == 0) {
                layer.alert('错误信息不能为空')
                return false;
            }
        } else if (type == 2) {
            if (!/^[1-9]\d*$/.test(formData.tokenBucketCount)) {
                layer.alert('令牌桶容量必须大于0')
                return false;
            }
        }
        return true;
    }


    function initServiceTab() {
        ApiUtil.post('service.list', {}, function (resp) {
            var serviceList = resp.data;
            var html = [];
            for (var i = 0; i < serviceList.length; i++) {
                var serviceInfo = serviceList[i];
                var clazz = i === 0 ? 'class="layui-this"' : '';
                html.push('<li ' + clazz + '>' + serviceInfo.serviceId + '</li>');
            }
            $('#serviceTab').html(html.join(''));

            if (serviceList.length > 0) {
                loadLimitTable(serviceList[0].serviceId);
            }
        });
    }


    function loadLimitTable(serviceId) {
        currentServiceId = serviceId;
        searchTable({
            serviceId: serviceId
        });
        smTitle = '[ <strong>serviceId：</strong>' + currentServiceId + ' ]';
    }

    /**
     * 查询表格
     * @param params
     */
    function searchTable(params) {
        var postData = {
            data: JSON.stringify(params)
        };
        if (!limitTable) {
            limitTable = renderTable(postData);
        } else {
            limitTable.reload({
                where: postData
            })
        }
    }

    var tipType = '<a href="#" onclick="showLimitTypeTip()" title="说明"><i class="layui-icon layui-icon-help"></i></a>'

    function renderTable(postData) {
        var limitTable = table.render({
            elem: '#limitTable'
            , url: ApiUtil.createUrl('route.limit.list')
            , where: postData
            , headers: {access_token: ApiUtil.getAccessToken()}
            , cellMinWidth: 80 //全局定义常规单元格的最小宽度，layui 2.2.1 新增
            , cols: [[
                {field: 'name', title: '接口名', width: 200}
                , {field: 'version', title: '版本号', width: 80}
                , {
                    field: 'limitType', title: '限流策略 ' + tipType, width: 100, templet: function (row) {
                        return LIMIT_TYPE[row.limitType + ''];
                    }
                }
                , {
                    field: 'info', title: '限流信息', width: 500, templet: function (row) {
                        if (!row.hasRecord) {
                            return '--'
                        }
                        var html = [];
                        if (row.limitType == 1) {
                            html.push('每秒可处理请求数：' + row.execCountPerSecond);
                            html.push('subCode：' + row.limitCode);
                            html.push('subMsg：' + row.limitMsg);
                        } else if(row.limitType == 2) {
                            html.push('令牌桶容量：' + row.tokenBucketCount);
                        }
                        return html.join('，');
                    }
                }
                , {
                    field: 'limitStatus', title: '状态', width: 80, templet: function (row) {
                        return LIMIT_STATUS[row.limitStatus + ''];
                    }
                }
                , {
                    fixed: 'right', title: '操作', width: 100, templet: function (row) {
                        var html = ['<a class="layui-btn layui-btn-xs layui-btn-normal" lay-event="edit">修改</a>'];
                        return html.join('');
                    }
                }
            ]]
        });

        //监听单元格事件
        table.on('tool(limitTableFilter)', function(obj) {
            var data = obj.data;
            var event = obj.event;
            if(event === 'edit'){
                //表单初始赋值
                data.serviceId = currentServiceId;

                updateForm.setData(data);
                $('.limit-type').hide();
                $('.type' + data.limitType).show();

                layer.open({
                    type: 1
                    ,title: '修改限流' + smTitle
                    ,area: ['600px', '460px']
                    ,content: $('#updateWin') //这里content是一个DOM，注意：最好该元素要存放在body最外层，否则可能被其它的相对元素所影响
                });
            }
        });

        return limitTable;
    }

    initServiceTab();

    RouteRole.loadAllRole(form, 'roleArea');

});

function showLimitTypeTip() {
    var leakyRemark = '漏桶策略：每秒处理固定数量的请求，超出请求返回错误信息。';
    var tokenRemark = '令牌桶策略：每秒放置固定数量的令牌数，每个请求进来后先去拿令牌，拿到了令牌才能继续，拿不到则等候令牌重新生成了再拿。';
    var content = '<div style="font-size: 14px;">'
        + leakyRemark
        + '<br>'
        + tokenRemark
        + '</div>';

    layer.open({
        title: '限流策略说明'
        ,area: ['600px', 'auto']
        ,content: content
    });
}