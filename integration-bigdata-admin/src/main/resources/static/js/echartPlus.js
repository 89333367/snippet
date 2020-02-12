;
(function(window, echarts) {
  window.UML = window.UML || {}
  window.UML.echartPlus = {}

  /**
   *
   *
   * @param {Object} config
   *    el: create chart DOM ID
   *    height: height
   *    width: width
   *    chartType: chert type
   *    url: chert data url
   *    onBefore: ajax 发送前执行
   *    onSuccess: ajax 接收后执行
   *    dataZoomMax: chart dataZoom 生成的阈值
   * @param {Object} option chart option 
   * @returns
   */
  window.UML.echartPlus.create = function(config, option) {

    var option = option || {}
    var myChart = null;
    
    // 指定图表的基础配置项和数据
    var initOption = {
      title: option.title || {},
      tooltip: option.tooltip || {},
      legend: option.legend || {},
      xAxis: option.xAxis || {},
      yAxis: option.yAxis || {},
      color: ['#3580B7'],
      grid: {
        left: '4%',
        right: '3%',
        containLabel: true
      }
    };
    $.extend(initOption, option)

    // 根据类型生成配置
    var optionObj = {
      bar: {
        xAxis: {
          nameLocation:'start',
          data: []
        },
        series: [{
          type: 'bar',
          data: []
        }]
      }
    }

    // 组合配置
    $.extend(initOption, optionObj[config.chartType]);

    // init echartDOM 结构
    function init() {

      $('#' + config.el).css({
        height: config.height || 300,
        width: config.width || '100%',
      })
    }

    // 绑定图表事件
    function bindEvent() {
      $(window).on('resize', function() {
        myChart.resize()
      })
    }

    //回调用户的onBefore方法
    config.onBefore.call(null, config);

    // 接收数据
    $.ajax({
      type: config.type || 'post',
      url: config.url,
      data: config.queryParams,
      success: function(data) {
        if (data.rows && data.rows.length > 0) {
          init()
          // 基于准备好的dom，初始化echarts实例
          myChart = echarts.init(document.getElementById(config.el));
          // 使用 初始化 配置 设置图表
          myChart.setOption(initOption);
          bindEvent()
          // 当数据量超过阈值时，配置dataZoom
          if (data.rows.length >= (config.dataZoomMax || 30)) {
            myChart.setOption({
              dataZoom: [
              {
                type: 'slider',
                show: true,
                xAxisIndex: [0],
                start: 1,
                end: 35
              },
              {
                type: 'inside',
                xAxisIndex: [0],
                start: 1,
                end: 35
              }]
            });
          }
          //回调用户的onSuccess方法
          config.onSuccess.call(null, data, config, myChart);
        }
      }
    })

    return {
      setOption: function(option) {
        myChart.setOption(option);
      },
      getOption: function() {
        return myChart.getOption()
      }
    }
  }

})(window, echarts)