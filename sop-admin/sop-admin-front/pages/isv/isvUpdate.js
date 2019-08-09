lib.importJs('../../assets/js/routerole.js')
    .use(['element', 'table', 'form'], function () {
    var table = layui.table;
    var layer = layui.layer;
    var form = layui.form;
    var $ = layui.jquery;

    var updateForm = layui.Form('updateForm');

    $('#createFormDataBtn').click(function () {
        ApiUtil.post('isv.form.gen', {}, function (resp) {
            var data = resp.data;
            var appKey = updateForm.getData('appKey');
            data.appKey = appKey;
            updateForm.setData(data);

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

    form.on('submit(updateFormSubmitFilter)', function (data) {
        var param = updateForm.getData();
        ApiUtil.post('isv.info.update', param, function (resp) {
            layer.alert('修改成功', function () {
                location.href = 'isvList.html';
            })
        })
        return false;
    });

    RouteRole.loadAllRole(form, 'roleArea', function () {
        loadFormData();
    })

    function loadFormData() {
        var id = ApiUtil.getParam('id');
        if (!id) {
            alert('id错误');
            return;
        }
        ApiUtil.post('isv.info.get', {id: id}, function (resp) {
            var isvInfo = resp.data;
            var roleList = isvInfo.roleList;
            var roleCode = [];
            for (var i = 0; i < roleList.length; i++) {
                roleCode.push(roleList[i].roleCode);
            }
            isvInfo.roleCode = roleCode;
            updateForm.setData(isvInfo);
        });
    }
});