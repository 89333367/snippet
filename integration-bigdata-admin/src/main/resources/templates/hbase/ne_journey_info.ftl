<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no"/>
    <style type="text/css">
        body, html, #allmap {
            width: 100%;
            height: 100%;
            overflow: hidden;
            margin: 0;
            font-family: "微软雅黑";
        }
    </style>
    <!-- jQuery -->
    <script src="https://cdn.bootcss.com/jquery/3.3.1/jquery.min.js"></script>
    <script type="text/javascript"
            src="http://api.map.baidu.com/api?v=3.0&ak=V3Z6Lh7nDrbrmU7IzbMxeyGnqzBKtQeo"></script>
    <title>轨迹回放</title>
</head>
<body>
<div id="allmap"></div>
</body>
</html>
<script type="text/javascript">
    var map = new BMap.Map("allmap");
    map.enableScrollWheelZoom(true);     //开启鼠标滚轮缩放

    $.post('${request.contextPath}/hbase/ne/journey_info', {
        did: '${did}',
        time: '${time}',
        no: '${no}'
    }, function (result) {
        if (result && result.length > 0) {
            var pts = [];
            for (var i = 0; i < result.length; i++) {
                pts.push(new BMap.Point(result[i].lon, result[i].lat));
            }
            map.setViewport(pts);
            // 画线
            var polyline = new BMap.Polyline(pts, {
                strokeColor: "#ad0e21",
                strokeWeight: 3,
                setStrokeStyle: "dashed",
                strokeOpacity: 1
            });
            map.addOverlay(polyline);

            var marker = new BMap.Marker(pts[0]);
            marker.setLabel(new BMap.Label("起点"));
            map.addOverlay(marker);

            var paths = pts.length;
            var marker1 = new BMap.Marker(pts[paths - 1]);
            marker1.setLabel(new BMap.Label("终点"));
            map.addOverlay(marker1);

            var myIcon = new BMap.Icon("http://lbsyun.baidu.com/jsdemo/img/Mario.png", new BMap.Size(32, 70), {    //小车图片
                //offset: new BMap.Size(0, -5),    //相当于CSS精灵
                imageOffset: new BMap.Size(0, 0)    //图片的偏移量。为了是图片底部中心对准坐标点。
            });
            var carMk = new BMap.Marker(pts[0], {icon: myIcon});
            map.addOverlay(carMk);
            var i = 0;

            function resetMkPoint(i) {
                carMk.setPosition(pts[i]);
                if (i < paths) {
                    setTimeout(function () {
                        i++;
                        resetMkPoint(i);
                    }, 100);
                }
            }

            setTimeout(function () {
                resetMkPoint(1);
            }, 100)
        }
    }, 'json');
</script>