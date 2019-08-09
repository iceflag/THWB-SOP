lib.use('element', function () {
    var $ = layui.jquery;
    var $pageFrame = $('#pageFrame');
    var $selectLi = null;
    $('.left-nav').on('click', 'li', function (event) {
        var $tagLi = $(this);
        var $tagA = $tagLi.find('a').eq(0);
        var href = $tagA.prop('href');
        if (href) {
            $pageFrame.prop('src', href + '?q=' + new Date().getTime());
            if ($selectLi) {
                $selectLi.removeClass('active')
            }
            $selectLi = $tagLi.addClass('active');
        }
        return false;
    });

    $('.container .left_open i').click(function(event) {
        if($('.left-nav').css('left')=='0px'){
            $('.left-nav').animate({left: '-221px'}, 100);
            $('.page-content').animate({left: '0px'}, 100);
            $('.page-content-bg').hide();
        }else{
            $('.left-nav').animate({left: '0px'}, 100);
            $('.page-content').animate({left: '221px'}, 100);
            if($(window).width()<768){
                $('.page-content-bg').show();
            }
        }
    });

    ApiUtil.post('admin.userinfo.get', {}, function (resp) {
        var userInfo = resp.data;
        $('#username').text(userInfo.username);
    });

});