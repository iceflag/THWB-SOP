var RouteRole = {
    loadAllRole: function (form, contentId, callback) {
        ApiUtil.post('role.listall', {}, function (resp) {
            var roles = resp.data;
            var html = []
            for (var i = 0; i < roles.length; i++) {
                if (i > 0 && i % 5 === 0) {
                    html.push('<br>');
                }
                var role = roles[i];
                html.push('<input type="checkbox" name="roleCode" value="'+role.roleCode+'" lay-skin="primary" title="'+role.description+'">');
            }
            $('#' + contentId).html(html.join(''));

            // 如果你的HTML是动态生成的，自动渲染就会失效
            // 因此你需要在相应的地方，执行下述方法来手动渲染，跟这类似的还有 element.init();
            form.render();

            callback && callback();
        });
    }
}