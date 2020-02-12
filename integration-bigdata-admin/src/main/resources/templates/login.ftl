<#include "macro/head.ftl"/>
<!DOCTYPE html>
<html>
<head>
    <@head title="AdminLTE | Login"/>
</head>
<body class="hold-transition">
<div class="login-box">
    <div class="login-logo">
        <b>BigData</b>Admin
    </div>
    <!-- /.login-logo -->
    <div class="login-box-body">
        <p class="login-box-msg">
            <#if error??>
                ${error}
            <#else >
                Sign in to start your session
            </#if>
        </p>

        <form id="login_form" action="${request.contextPath}/login" method="post">
            <div class="form-group has-feedback">
                <input id="email" name="email" required value="${email!}" type="email" class="form-control"
                       placeholder="Email">
                <span class="glyphicon glyphicon-envelope form-control-feedback"></span>
            </div>
            <div class="form-group has-feedback">
                <input id="password" name="password" required value="${password!}" type="password" class="form-control"
                       placeholder="Password">
                <span class="glyphicon glyphicon-lock form-control-feedback"></span>
            </div>
            <div class="row">
                <div class="col-xs-8">
                    <div class="checkbox icheck">
                        <label>
                            <input id="remember" type="checkbox"> Remember Me
                        </label>
                    </div>
                </div>
                <!-- /.col -->
                <div class="col-xs-4">
                    <button type="submit" class="btn btn-primary btn-block btn-flat">Sign In</button>
                </div>
                <!-- /.col -->
            </div>
        </form>
    </div>
    <!-- /.login-box-body -->
</div>
<!-- /.login-box -->

<#include "macro/js.ftl"/>
<script>
    $('#remember').iCheck({
        checkboxClass: 'icheckbox_square-blue',
        radioClass: 'iradio_square-blue',
        increaseArea: '20%' /* optional */
    });

    $('#remember').on('ifChecked', function () {
        //选中
        console.log('选中');
    });
    $('#remember').on('ifUnchecked', function () {
        //取消选中
        console.log('取消选中');
    });

    var email = localStorage.getItem('login_email');
    if (email) {
        $('#email').val(email);
    }
    var password = localStorage.getItem('login_password');
    if (password) {
        $('#password').val(password);
    }
    var remember = localStorage.getItem('login_remember');
    if (remember) {
        $('#remember').iCheck('check');
    }

    $('#login_form').on('submit', function () {
        console.log('登录');
        if ($('#remember').is(':checked')) {
            localStorage.setItem('login_email', $('#email').val());
            localStorage.setItem('login_password', $('#password').val());
            localStorage.setItem('login_remember', 'true');
        } else {
            localStorage.setItem('login_email', '');
            localStorage.setItem('login_password', '');
            localStorage.setItem('login_remember', '');
        }
    });
</script>
</body>
</html>
