lib.importJs('../../assets/js/routerole.js')
    .use(['element', 'table', 'form'], function () {
    var table = layui.table;
    var layer = layui.layer;
    var form = layui.form;
    var $ = layui.jquery;

    var addForm = layui.Form('addForm');

    $('#createFormDataBtn').click(function () {
        ApiUtil.post('isv.form.gen', {}, function (resp) {
            addForm.setData(resp.data);
        });
        return false;
    });

    form.on('radio(signTypeFilter)', function(data){
        if (data.value == 1) {
            $('.sign-type-rsa2').show();
            $('.sign-type-md5').hide();
        } else {
            $('.sign-type-rsa2').hide();
            $('.sign-type-md5').show();
        }
    });

    form.on('submit(addFormSubmitFilter)', function (data) {
        var param = addForm.getData();
        ApiUtil.post('isv.info.add', param, function (resp) {
            layer.alert('添加成功', function () {
                location.href = 'isvList.html';
            })
        })
        return false;
    });

    RouteRole.loadAllRole(form, 'roleArea');

});