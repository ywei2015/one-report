var cellplanServerIP = "http://localhost:8081/";
var rowInfo;
var i;//dom序号

$(function () {
	//初始化加载第一栏
	$("#titleItem a:first").tab("show");
    var oTable = new TableInit();
    oTable.Init('#templateConfigTable',cellplanServerIP+'report/template','#templateConfigToolbar','name',
    	    [{field: 'name',title: 'Name',sortable: 'true'}
    	    ,{field: 'createTime',title: 'createTime'}
    	    ,{field: 'path', title: 'path'}
    	    ,{field: 'url', title: 'url'}
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
            });
    $("#btn_query").on("click",function(){
        var opt = {
            url: cellplanServerIP+'report/config',
            silent: true,
        };
        $("#taskConfigTable").bootstrapTable('refresh', opt);
    });
    $("#query").on("click",function(){
        var opt = {
            url: cellplanServerIP+'report/template',
            silent: true,
        };
        $("#templateConfigTable").bootstrapTable('refresh', opt);
    });

    $('#collapseOne').on('hide.bs.collapse', function () {
        // 执行一些动作...
        $("#select_span").removeClass("caret");
        $("#select_span").addClass("caret2");
    })

    $('#collapseOne').on('shown.bs.collapse', function () {
        // 执行一些动作...
        $("#select_span").removeClass("caret2");
        $("#select_span").addClass("caret");
    })

   //SQL配置页查询参数重置
    $("#btn_reset").on("click",function(){
        $("#formSearch input").val("");
    });
    $("#reset").on("click",function(){
        $("#formSearch_template input").val("");
    });
    initSelect(oTable);
});

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
function initTableDataConfig(oTable) {
    oTable.Init('#taskConfigTable',cellplanServerIP+'report/config','#taskConfigToolbar','id',
        [{field: 'id',title: 'id',visible: false
        },{field: 'templateName',title: '模板',sortable: 'true'
        },{field: 'taskName',title: '指标名称',sortable: 'true'
        },{field: 'bigTask',title: '大指标',sortable: 'true',formatter:function (value, row, index) {
                if (value == 1){
                    return "是";
                } else { return "否"}
            }
        },{field: 'screenShot',title: '截图URL',sortable: 'true',formatter:function (value, row, index) {
                if (value === null || value === undefined) {
                    return "";
                } else {
                    return value
                }
            }
        },{field: 'withCell',title: '栅格相关性',sortable: 'true',formatter:function (value, row, index) {
                if (value == 1){
                    return "是";
                } else { return "否"}
            }
        },{field: 'value',title: 'value',formatter:function (value, row, index) {
                if (value === null || value === undefined) {
                    return "";
                } else {
                    return value
                }
            }
        }, {field: 'sqlContext',title: 'sqlContext',formatter:function (value, row, index) {
            if (value != null){
                var str = value.substring(0,15);
                return str;
            }else {
                return "";
            }
        }
        }],false,function queryParams(params) {
            return {   //这里的键的名字和控制器的变量名必须一直，这边改动，控制器也需要改成一样的
                limit: params.limit,   //页面大小
                page: params.offset/params.limit,  //页码
                search: params.search,//关键字
                sortName: params.sort,//排序列名
                sortOrder: params.order,//排位命令（desc，asc）
                taskName: $("#taskName").val(),
                templateName: $("#template").val(),
            };
        });
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

//sqlparam
function addTaskConfig(){
    var template = $("#template").val();
    $("#config_id").val("");
    $('#task_name').val("");
    $('#templateN').val(template);
    $('#screenShot').val(1);
    $('#bigTask').val(1);
    $('#withCell').val(1);
    $('#sqlContext').val("");
    $('#value').val("");
    openlayer();
}

function openlayer(){
    layer.open({
        type: 2,
        title:'编辑',
        resize:true,
        scrollbar: false,
        area:["800px","600px"],
        content:["paramsql.html","yes"]
    })
}

function editTaskConfig(){
    if(rowInfo!=undefined){
		$("#config_id").val(rowInfo["id"]);
        $("#task_name").val(rowInfo["taskName"]);
        $('#screenShot').val(rowInfo["screenShot"]);
        $('#withCell').val(rowInfo["withCell"]);
        $('#sqlContext').val(rowInfo["sqlContext"]);
        $('#bigTask').val(rowInfo["bigTask"]);
        $('#templateN').val(rowInfo["templateName"]);
        $('#value').val(rowInfo["value"]);
        openlayer();
    }else{
        alert("请选择一行记录！");
    }
}
function deleteTaskConfig(){
    var flag=confirm("删除是不可恢复的，你确认要删除吗？");
    if(flag==false){
        return;
    }
    if(rowInfo!=undefined){
        var id = rowInfo['id'];
        $.ajax({
            xhrFields: {
                withCredentials: true
            },
            url:cellplanServerIP+"report/deleteConfig",
            type:"post",
            data: {id : id},
            success:function(data){
            	var status =data.status;
            	if(status=='0'){
            		return;
				}
                var opt = {
                    url: cellplanServerIP+'report/config',
                    silent: true,
                };
                $("#taskConfigTable").bootstrapTable('refresh', opt);
                rowInfo = undefined;
            }
        });
    }else{
        alert("请选择一行记录！");
    }
}

function tabCallback(){
	//更换Tab页，清除选中行记录
    rowInfo = undefined;
    $('.success').removeClass('success');//去除之前选中的行的，选中样式
}

function saveTemplate() {
    if (editOrSave === "save"){
        var type = "file";          //后台接收时需要的参数名称，自定义即可
        var id = "templateFile";
        var url = $("#url").val();
        var formData = new FormData();
        formData.append(type, $("#"+id)[0].files[0]);    //生成一对表单属性
        formData.set("url",url);
        $.ajax({
            type: "POST",           //因为是传输文件，所以必须是post
            url: cellplanServerIP+'report/uploadFile',         //对应的后台处理类的地址
            data: formData,
            processData: false,
            contentType: false,
            success: function (data) {
                var status =data.code;
                var mgs = data.msg;
                if(status === 2000){
                    layer.alert("save file succeed");
                    var opt = {
                        url: cellplanServerIP+'report/template',
                        silent: true,
                    };
                    $("#templateConfigTable").bootstrapTable('refresh', opt);
                    rowInfo = undefined;
                }else {
                    alert(mgs);
                }
            }
        });
    }else {
        editTemplate();
    }
}
function editTemplate() {
    var url = $("#url").val();
    var data = rowInfo;
    data.url = url;
    $.ajax({
        xhrFields: {
            withCredentials: true //执行跨域名请求
        },
        url:cellplanServerIP+"report/saveTemplate",
        type:"post",
        contentType : 'application/json',
        data: JSON.stringify(data),
        success:function(data){
            layer.close();
            var status =data.code;
            var message =data.msg;
            if(status === 2000){
                var opt = {
                    url: cellplanServerIP+'report/template',
                    silent: true,
                };
                $("#templateConfigTable").bootstrapTable('refresh', opt);
            }else {
                layer.alert(message);
            }
        }
    });
}
function downTemplateFile(event) {
    var target = event.target;
    var path = $(target).closest("tr").find("td").eq(2).html();
    if (path == undefined || path == ""){
        alert("文件路径不存在");
    } else {
        path = path.trim();
        window.location.href = cellplanServerIP+"getFile?filePath="+path;
    }
}
var editOrSave = "save";
function addTemplateConfig() {
    editOrSave = "save";
    $("#templateModal").modal('show');
    $("#templateFile_div").show();
}
function editTemplateConfig() {
    editOrSave = "edit";
    $("#url").val(rowInfo['url']);
    $("#templateModal").modal('show');
    $("#templateFile_div").hide();
}

function deleteTemplateConfig() {
    var flag=confirm("删除是不可恢复的，你确认要删除吗？");
    if(flag==false){
        return;
    }
    if(rowInfo!=undefined){
        var id = rowInfo['name'];
        $.ajax({
            xhrFields: {
                withCredentials: true
            },
            url:cellplanServerIP+"report/deleteTemplate",
            type:"post",
            data: {name : id},
            success:function(data){
                var status =data.code;
                var mgs = data.msg;
                if(status === 2000){
                    var opt = {
                        url: cellplanServerIP+'report/template',
                        silent: true,
                    };
                    $("#templateConfigTable").bootstrapTable('refresh', opt);
                    rowInfo = undefined;
                }else {
                    alert(mgs);
                }
            }
        });
    }else{
        alert("请选择一行记录！");
    }
}

function initSelect(oTable) {
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
                initTableDataConfig(oTable);
            }else {
                alert(mgs);
            }
        }
    });
}