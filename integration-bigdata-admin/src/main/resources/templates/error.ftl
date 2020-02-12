<#include "macro/head.ftl"/>
<!DOCTYPE html>
<html>
<head>
    <@head title="Error"/>
</head>
<body class="hold-transition sidebar-mini skin-black">
<section class="content">
    <div class="error-page">
        <h2 class="headline text-red">${status!}</h2>
        <div class="error-content">
            <h3><i class="fa fa-warning text-red"></i> ${error!}</h3>
            <p>
                ${message!}
            </p>
            <p>
                ${trace!}
            </p>
            <p>
                <a href="${request.contextPath}/">回到首页</a>
            </p>
        </div>
    </div>
</section>
</body>
</html>