<#include "macro/head.ftl"/>
<!DOCTYPE html>
<html>
<head>
    <@head title="博创联动 | 支撑平台"/>
    <style>
        body {
            background-color: #f5f5f5;
        }

        img {
            vertical-align: top;
        }

        nav.navbar {
            border-radius: 0;
            background-color: #000;
            border: none;
            margin-bottom: 60px;
        }

        .navbar-default .navbar-nav > li > a {
            color: #fff;
            font-size: 20px;
            height: 80px;
            line-height: 50px;
        }

        .navbar-brand {
            margin-left: 0 !important;
            padding: 20px 0 0 0;
            height: 80px;
        }

        .navbar-default .navbar-nav > .active > a,
        .navbar-default .navbar-nav > .active > a:focus,
        .navbar-default .navbar-nav > .active > a:hover,
        .navbar-default .navbar-nav > li > a:focus,
        .navbar-default .navbar-nav > li > a:hover {
            color: #1991f7;
            background-color: #000;
        }

        .enter-item .link-item {
            display: block;
            background-color: #fff;
            width: 290px;
            height: 212px;
            font-size: 16px;
            color: #fff;
            margin: 0 auto;
            margin-bottom: 74px;
            box-shadow: 0 0 10px #e3e3e3;
            text-align: center;
        }


        .enter-item a.link-item,
        .enter-item a.link-item:link,
        .enter-item a.link-item:visited,
        .enter-item a.link-item:hover,
        .enter-item a.link-item:active {
            position: relative;
            color: #000;
            text-decoration: none;
            line-height: 1;
            border: 1px solid #FFF;
        }

        .enter-item .item-decorate {
            display: block;
            width: 302px;
            height: 8px;
            margin-top: -4px;
            margin-left: -6px;
            border-radius: 4px;
        }

        .enter-item.blue .item-decorate {
            background-color: #BDD0E1;
        }

        .enter-item.cyan .item-decorate {
            background-color: #C4D7C8;
        }

        .enter-item.brown .item-decorate {
            background-color: #D0C6B3;
        }

        .enter-item.taupe .item-decorate {
            background-color: #C7B4B4;
        }

        .enter-item .item-content {
            background-color: #FFF;
            position: relative;
            margin-top: -4px;
        }

        .enter-item .link-item h1 {
            margin: 0;
            font-size: 22px;
            white-space: nowrap;
            overflow: hidden;
            padding-top: 30px;
            padding-bottom: 22px;
        }

        .enter-item .link-item h1 span:first-child {
            padding-right: 10px;
        }

        .enter-item .link-item h1 span:last-child {
            padding-left: 10px;
        }

        .enter-item .link-item p {
            height: 60px;
            padding-bottom: 43px;
            margin: 0;
        }

        .enter-item .link-item button {
            padding: 12px 50px;
        }

        @media (min-width: 1600px) {
            .navbar-default .navbar-nav > li > a {
                padding-left: 40px;
                padding-right: 40px;
            }

            .navbar-brand {
                margin-right: 60px;
            }
        }

        @media (min-width: 1300px) {
            .container-fluid {
                padding: 0 200px;
            }
        }

        @media (min-width: 1200px) and (max-width: 1600px) {
            .enter-item {
                width: 33.33333333% !important;
            }
        }
    </style>
</head>
<body class="hold-transition">
<nav class="navbar navbar-default">
    <div class="container-fluid">
        <!-- Brand and toggle get grouped for better mobile display -->
        <div class="navbar-header">
            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse"
                    data-target="#bs-example-navbar-collapse-1" aria-expanded="false">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand hidden-xs hidden-sm" href="/"><img src="${request.contextPath}/img/logo.png" alt=""/></a>
        </div>

        <!-- Collect the nav links, forms, and other content for toggling -->
        <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
            <ul class="nav navbar-nav">
                <li class="active"><a href="bus">业务系统</a></li>
                <li><a href="pro">生产环境</a></li>
                <li><a href="cg">山西成功环境</a></li>
                <li><a href="dev">开发环境</a></li>
                <li><a href="test">测试环境</a></li>
            </ul>
        </div>
        <!-- /.navbar-collapse -->
    </div>
    <!-- /.container-fluid -->
</nav>
<div class="link-box">
    <div class="container-fluid">
        <div class="row"></div>
    </div>
</div>
<#include "macro/js.ftl"/>

</body>
<script>
    $(function () {
        // 模块相关信息
        var currentModule = null
        var moduleConfig = {
            bus: [{
                name: "禅道",
                url: "http://zt.uml-tech.com/zentao/my/",
                alt: "项目管理"
            },
                {
                    name: "Jenkins",
                    url: "http://192.168.11.86:8080/jenkins",
                    alt: "项目自动化编译部署"
                },
                {
                    name: "GitLab",
                    url: "http://git.bcnyyun.com/dashboard/activity",
                    alt: "项目版本控制"
                },
                {
                    name: "Maven私服",
                    url: "http://192.168.11.86:8081/nexus",
                    alt: "jar包版本管理"
                },
                {
                    name: "项目发布包",
                    url: "http://192.168.11.86/build/",
                    alt: "项目发布管理"
                },
                {
                    name: "跳板机",
                    url: "http://223.223.187.55:8000/juser/user/detail/",
                    alt: "跳板机配置"
                },
                {
                    name: 'Wiki',
                    url: 'http://cwiki.uml-tech.com/#all-updates',
                    alt: 'Wiki'
                }
            ],
            pro: [{
                name: "大数据监控",
                url: "http://223.223.187.55:10000/dashboard/dashboard",
                alt: "大数据状态、硬件联调、历史数据"
            },
                {
                    name: "开放平台接口",
                    url: "http://172.16.1.25:8086/doc.html",
                    alt: "Dubbo接口(内网)"
                },
                {
                    name: "开放平台接口",
                    url: "http://openapi.uml-tech.com/doc.html",
                    alt: "Dubbo接口"
                },
                {
                    name: "开放平台接口服务监控",
                    url: "http://172.16.1.26:8090/#/service?filter=%2a&pattern=service",
                    alt: "Dubbo Admin"
                },
                {
                    name: "Azkaban",
                    url: "http://172.16.1.20:8081",
                    alt: "定时任务管理"
                },
                {
                    name: "CDH",
                    url: "http://172.16.1.9:7180",
                    alt: "大数据集群管理"
                },
                {
                    name: "内部协议配置",
                    url: "http://172.16.1.245:8000/config.xml",
                    alt: ""
                },
                {
                    name: "内部报警编码配置",
                    url: "http://172.16.1.245:8000/alarm_code.xml",
                    alt: ""
                },
                {
                    name: "短信接口",
                    url: "http://172.16.1.247:8081/uml-web-sms",
                    alt: ""
                },
                {
                    name: "经纬度转换地址接口",
                    url: "http://172.16.1.245:9094/DiscInfo/getDisc?pointX=101.8016950000&pointY=38.397628789",
                    alt: ""
                },
                {
                    name: "开放平台",
                    url: "http://open.umlxny.com",
                    alt: ""
                },
                {
                    name: "车联网智慧数据服务平台",
                    url: "http://www.umlxny.com",
                    alt: ""
                },
                {
                    name: "Moatn RPC1.0",
                    url: "	http://172.16.1.33:8089",
                    alt: "大数据Motan接口1.0"
                },
                {
                    name: "Moatn RPC2.0",
                    url: "http://172.16.1.25:8084/doc.html,alt",
                    alt: "大数据Motan接口2.0"
                },
                {
                    name: "Motan RPC服务监控",
                    url: "http://172.16.1.25:8080",
                    alt: "Motan RPC Admin"
                }
            ],
            cg: [{
                name: "大数据监控",
                url: "http://60.220.209.50:10000/dashboard/dashboard",
                alt: "	大数据状态、硬件联调、历史数据"
            },
                {
                    name: "车联网智慧数据服务平台",
                    url: "http://60.220.209.50:81",
                    alt: ""
                }
            ],
            dev: [{
                name: "大数据监控",
                url: "http://192.168.11.8:10000/dashboard/dashboard",
                alt: "大数据状态、硬件联调、历史数据"
            },
                {
                    name: "开放平台接口",
                    url: "http://192.168.11.8:8090/doc.html",
                    alt: "Dubbo接口(内网)"
                },
                {
                    name: "开放平台接口",
                    url: "http://openapi.qas.uml-tech.com/doc.html",
                    alt: "Dubbo接口"
                },
                {
                    name: "开放平台接口服务监控",
                    url: "http://192.168.11.8:10005/#/service?filter=%2a&pattern=service",
                    alt: "Dubbo Admin"
                },
                {
                    name: "Azkaban",
                    url: "http://192.168.11.132:8081/",
                    alt: "定时任务管理"
                },
                {
                    name: "CDH",
                    url: "http://192.168.11.130:7180",
                    alt: "大数据集群管理"
                },
                {
                    name: "内部协议配置",
                    url: "http://192.168.11.8/config.xml",
                    alt: ""
                },
                {
                    name: "内部报警编码配置",
                    url: "http://192.168.11.8/alarm_code.xml",
                    alt: ""
                },
                {
                    name: "MoatnRPC1.0",
                    url: "http://192.168.11.126:8081",
                    alt: "大数据Motan接口1.0"
                },
                {
                    name: "MoatnRPC2.0",
                    url: "http://192.168.11.8:8081/doc.html",
                    alt: "大数据Motan接口2.0"
                },
                {
                    name: "短信接口",
                    url: "http://192.168.11.126:8080/uml-web-sms",
                    alt: ""
                },
                {
                    name: "经纬度转换地址接口",
                    url: "http://192.168.11.169:9094/DiscInfo/getDisc?pointX=101.8016950000&pointY=38.397628789",
                    alt: ""
                }
            ],
            test: [{
                name: "大数据监控",
                url: "http://192.168.12.21:10000/dashboard/dashboard",
                alt: "大数据状态、硬件联调、历史数据"
            },
                {
                    name: "开放平台接口",
                    url: "http://192.168.12.29:8080/doc.html",
                    alt: "Dubbo接口"
                },
                {
                    name: "开放平台接口服务监控",
                    url: "http://192.168.12.29:10005/#/service?filter=%2a&pattern=service",
                    alt: "Dubbo Admin"
                },
                {
                    name: "Azkaban",
                    url: "http://192.168.12.21:8081",
                    alt: "定时任务管理"
                },
                {
                    name: "CDH",
                    url: "http://192.168.12.20:7180",
                    alt: "大数据集群管理"
                },
                {
                    name: "车联网智慧数据服务平台",
                    url: "http://qas.umlxny.com",
                    alt: ""
                },
                {
                    name: "MoatnRPC1.0",
                    url: "http://192.168.12.29:8083",
                    alt: "大数据Motan接口1.0"
                },
                {
                    name: "MoatnRPC2.0",
                    url: "http://192.168.12.29:8084/doc.html",
                    alt: "大数据Motan接口2.0"
                }
            ]
        };
        var colorArr = ['blue', 'cyan', 'brown', 'taupe']
        var colorIndex = 0

        // 入口渲染
        function render(type) {
            if (currentModule && currentModule == type) {
                return
            }
            $('.link-box .row').empty()
            currentModule = type || "bus";
            var vDOM = "";
            $.each(moduleConfig[currentModule], function (i, item) {

                vDOM +=
                    '<div class="col-lg-3 col-md-4 col-sm-6 col-xs-12 enter-item ' + colorArr[
                        colorIndex] + '"><a target="_blank" href="' +
                    item.url +
                    '" class="link-item"><span class="item-decorate"></span><div class="item-content"><h1><span>●</span>' +
                    item.name +
                    "<span>●</span></h1><p>" +
                    item.alt +
                    "</p><button type='button' class='btn btn-primary'>查看服务</button>" +
                    "</div></a></div>";

                colorIndex++
                if (colorIndex == 4) {
                    colorIndex = 0
                }
            });
            $(".link-box .row").append(vDOM);
        }

        // 跳转 module
        $('.nav.navbar-nav li').on('click', function (e) {
            e.preventDefault()
            $(this).siblings().removeClass('active').end().addClass('active')
            var href = $(this).find('a').attr('href')
            render(href)
        })

        // 入口初始化
        render();
    });
</script>
</html>