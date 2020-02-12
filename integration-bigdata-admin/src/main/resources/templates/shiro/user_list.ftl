<#include "../macro/head.ftl"/>
<!DOCTYPE html>
<html>
<head>
    <@head title="用户管理"/>
</head>
<body class="hold-transition sidebar-mini skin-black">
<div class="wrapper">
    <#include "../macro/navbar.ftl"/>

    <#include "../macro/menu.ftl"/>

    <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper">
        <!-- Content Header (Page header) -->
        <section class="content-header">
            <h1>
                系统管理
                <small>用户管理</small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> 系统管理</a></li>
                <li><a href="#">权限管理</a></li>
                <li class="active">用户管理</li>
            </ol>
        </section>

        <!-- Main content -->
        <section class="content">
            <div class="row">
                <div class="col-xs-12">
                    <div class="box box-solid">
                        <div class="box-header">
                            <i class="fa fa-search"></i>

                            <h3 class="box-title">查询条件</h3>
                            <!-- tools box -->
                            <div class="pull-right box-tools">
                                <button type="button" class="btn btn-danger btn-sm" data-widget="collapse"><i
                                            class="fa fa-minus"></i>
                                </button>
                            </div>
                            <!-- /. tools -->
                        </div>
                        <!-- /.box-header -->
                        <div class="box-body no-padding">
                            <div style="width: 100%"></div>
                        </div>
                        <!-- /.box-body -->
                        <div class="box-footer">
                            <form id="searchForm" class="form-horizontal"
                                  action="${request.contextPath}/shiro/user/list" method="get">
                                <div class="box-body">
                                    <div class="form-group">
                                        <label for="name" class="col-md-2 col-lg-1 control-label">用户名称</label>
                                        <div class="col-md-10 col-lg-11">
                                            <input class="form-control" id="name" name="name" placeholder="用户名称"
                                                   value="${name}">
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label for="email" class="col-md-2 col-lg-1 control-label">邮箱</label>
                                        <div class="col-md-10 col-lg-11">
                                            <input class="form-control" id="email" name="email" placeholder="邮箱"
                                                   value="${email}">
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label for="disabled" class="col-md-2 col-lg-1 control-label">状态</label>
                                        <div class="col-md-10 col-lg-11">
                                            <select class="form-control" id="disabled" name="disabled">
                                                <option value="">检索禁用/启用类型</option>
                                                <option value="1" <#if disabled = '1'>selected</#if>>禁用</option>
                                                <option value="0" <#if disabled = '0'>selected</#if>>启用</option>
                                            </select>
                                        </div>
                                    </div>
                                </div>
                                <!-- /.box-body -->

                                <div class="box-footer">
                                    <button type="submit" class="btn btn-info pull-right">查询</button>

                                    <button id="clearForm" type="button" class="btn btn-default">清空条件</button>
                                </div>
                                <!-- /.box-footer -->
                            </form>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-xs-12" style="display: none;">
                    <div class="box">
                        <div class="box-body">
                            <div id="resultTable" class="table table-striped"></div>
                        </div>
                        <!-- /.box-body -->
                    </div>
                    <!-- /.box -->
                </div>
            </div>
        </section>
        <!-- /.content -->
    </div>
    <!-- /.content-wrapper -->

    <#include "../macro/footer.ftl"/>
</div>
<#include "../macro/js.ftl"/>

<script>
    function resetPwd(id) {
        Pace.restart();
        $.post('${request.contextPath}/shiro/resetPwd', {userId: id}, function (result) {
            if (result.status == 10000) {
                Swal.fire({
                    position: 'top-end',
                    type: 'success',
                    title: result.message,
                    showConfirmButton: false
                });
            } else {
                Swal.fire({
                    position: 'top-end',
                    type: 'error',
                    title: result.error,
                    showConfirmButton: false
                });
            }
        }, 'json');
    }

    function changeStatus(id) {
        Pace.restart();
        $.post('${request.contextPath}/shiro/changeStatus', {userId: id}, function (result) {
            if (result.status == 10000) {
                Swal.fire({
                    position: 'top-end',
                    type: 'success',
                    title: result.message,
                    showConfirmButton: false,
                    onClose: function () {
                        location.href = location.href;
                    }
                });
            } else {
                Swal.fire({
                    position: 'top-end',
                    type: 'error',
                    title: result.error,
                    showConfirmButton: false
                });
            }
        }, 'json');
    }

    $(function () {
        UML.tablePlus({
            el: 'resultTable',
            columns: [
                {data: 'name', title: '名称'}
                , {data: 'email', title: '邮箱'}
                , {
                    data: 'disabled', title: '状态', render: function (value, row) {
                        if (value && value == '1') {
                            return '<span class="bg-red-active">禁用</span>';
                        } else {
                            return '<span>启用</span>';
                        }
                    }
                }
                , {
                    data: 'id', title: '操作', render: function (value, row) {
                        var p = '<a href="javascript:void(0);" onclick="resetPwd({0});">重置密码</a>'.replace('{0}', value);
                        var ed = '<a href="javascript:void(0);" onclick="changeStatus({0});">{1}</a>';
                        if (row.disabled == '1') {
                            ed = ed.replace('{0}', value);
                            ed = ed.replace('{1}', '启用');
                        } else {
                            ed = ed.replace('{0}', value);
                            ed = ed.replace('{1}', '禁用');
                        }
                        return p + ' / ' + ed;
                    }
                }],
            onBefore: function (opts, table) {
                opts.queryParams = $('#searchForm').serializeObject();
                if (table.page > 0) {
                    opts.queryRowKey.page = table.page + 1;
                    opts.queryRowKey.pageSize = 10;
                }
            },
            onSuccess: function (opts, result, table) {
                if (result.rows.length > 0) {
                    $('#' + opts.el).parent().parent().parent().show();
                }
            },
            url: $('#searchForm').attr('action'),
            info: 'right'
        });
    });
</script>
</body>
</html>