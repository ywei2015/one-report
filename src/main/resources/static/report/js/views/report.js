var cellplanServerIP = "http://localhost:8081/";
var rowInfo;
var i;//dom序号
$(function () {
	//初始化加载第一栏
	$("#titleItem a:first").tab("show");
    var oTable = new TableInit();
    /*oTable.Init('#reportTable',cellplanServerIP+'report/record',null,'id',
    	    [{field: 'name',title: 'Name',sortable: 'true'}
    	    ,{field: 'createTime',title: 'createTime'}
    	    ,{field: 'path', title: 'path'}
    	    ,{field: 'path', title: 'down'
                , formatter: function (value, row, index) {
    	            return "<button type=\"button\" onclick='downTemplateFile(event)' class=\"btn btn-default\">down</button>";
                }
            }],false,
            function queryParams (params) {
                return {//这里的键的名字和控制器的变量名必须一直，这边改动，控制器也需要改成一样的
                    limit: params.limit,   //页面大小
                    page: params.offset/params.limit,  //页码
                    search: params.search,//关键字
                    sortName: params.sort,//排序列名
                    sortOrder: params.order,//排位命令（desc，asc）
                    name: $("#templateName").val(),
                };
            });*/

    $("#query").on("click",function(){
        var template = $("#template").val();
        var city = $("#city").val();
        var site = $("#cell").val();
        var date = $("#date").val();
        var dateType = $("#dateType").val();
        var param = {
            template:template,
            city:city,
            site:site,
            date:date,
            dateType:dateType
        }
        layer.load("执行中");
        $.ajax({
            xhrFields: {
                withCredentials: true
            },
            url:cellplanServerIP+"report/ppt",
            type:"post",
            data:param,
            success:function(data){
                layer.closeAll();
                var status =data.code;
                var msg =data.msg;
                if(status === 2000){
                    layer.alert("success");
                    var path = data.data;
                    window.location.href = cellplanServerIP+"getFile?filePath="+path;
                    var opt = {
                        url: cellplanServerIP+'report/record',
                        silent: true,
                    };
                    $("#reportTable").bootstrapTable('refresh', opt);
                }else {
                    alert(msg);
                }
            }
        });
    });
    initSelect();
});

function initSelect() {
    var template = $("#template");
    $.ajax({
        xhrFields: {
            withCredentials: true
        },
        url:cellplanServerIP+"report/templateNames",
        type:"get",
        success:function(data){
            var status =data.code;
            var mgs =data.msg;
            var names = data.data;
            if(status === 2000){
                var all = "<option value=''>all</option>";
                template.append(all);
                for (var i =0; i<names.length; i++) {
                    var str = names[i];
                    var option = "<option value='"+str+"'>"+str+"</option>";
                    template.append(option);
                }
            }else {
                alert(mgs);
            }
        }
    });
}
function getRequest() {
    var url =decodeURI(decodeURI(location.search)); //获取url中"?"符后的字串，使用了两次decodeRUI解码
    var theRequest = new Object();
    if (url.indexOf("?") != -1) {
        var str = url.substr(1);
        strs = str.split("&");
        for (var i = 0; i < strs.length; i++) {
            theRequest[strs[i].split("=")[0]] = unescape(strs[i].split("=")[1]);
        }
        return theRequest;
    }
}

var TableInit = function () {
    var oTableInit = new Object();
    //初始化Table
    oTableInit.Init = function (dom,url,toolbar,primaryKey,columns,search,queryParams) {
        $(dom).bootstrapTable({
            url: url,         //请求后台的URL（*）
            method: 'get',                      //请求方式（*）
            contentType:'application/x-www-form-urlencoded; charset=UTF-8',
            toolbar: toolbar,                //工具按钮用哪个容器
            striped: true,                      //是否显示行间隔色
            cache: false,                       //是否使用缓存，默认为true，所以一般情况下需要设置一下这个属性（*）
            pagination: true,                   //是否显示分页（*）
            paginationShowPageGo: true,         //跳页
            sortable: true,                     //是否启用排序
            sortOrder: "asc",                   //排序方式
            queryParams: queryParams,           //传递参数（*）
            sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
            pageNumber:1,                       //初始化加载第一页，默认第一页
            pageSize: 10,                       //每页的记录行数（*）
            pageList: [10, 25, 50, 100],        //可供选择的每页的行数（*）
            search: search,                       //显示查询框
            strictSearch: false,                //精确查询
            showColumns: true,                  //是否显示所有的列
            showRefresh: true,                  //是否显示刷新按钮
            minimumCountColumns: 2,             //最少允许的列数
            clickToSelect: true,                //是否启用点击选中行
            height: 500,                        //行高，如果没有设置height属性，表格自动根据记录条数觉得表格高度
            uniqueId: primaryKey,               //每一行的唯一标识，一般为主键列
            showToggle:true,                    //是否显示详细视图和列表视图的切换按钮
            cardView: false,                    //是否显示详细视图
            detailView: false,                   //是否显示父子表
            columns: columns,
            onClickRow : function(row, $element) {
                editMemberInfoShow(row, $element)
            }
        });
    };
    return oTableInit;
};


//注册选中行事件
function editMemberInfoShow(row, $element){
    $('.success').removeClass('success');//去除之前选中的行的，选中样式
    $element.addClass('success');//添加当前选中的 success样式用于区别
    var index = $('#formTempDetailTable_new').find('tr.success').data('index');//获得选中的行的id
    rowInfo = row;
}