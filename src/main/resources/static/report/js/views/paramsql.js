var cellplanServerIP = "http://localhost:9009/ExportServer/";
var rowInfo;
var textarea1="";
$(function () {
    textarea1=getCodeMirror("sqlContext");
    textarea1.setValue("");
    $("#textsqldiv").hide();

    $("#textBtn").on("click",function(){
        var flag =$("#textBtn").text();
        flag=flag.trim();
    });

    getParentValue();

    $("#cancelBtn").on("click",function () {
        parent.layer.closeAll();
    });

    $("#saveBtn").on("click",function () {
        saveSqlParamModel();
    })
    $("#sqlType").change(function(){
        //PROJECT值
        // querysqlMap_project();
    })

});

//获取父窗口的值
function getParentValue() {
    var id=parent.$("#config_id").val();
    var value = parent.$('#value').val();
    var template = parent.$('#templateN').val();
    var taskName=parent.$('#task_name').val();
    var screenShot=parent.$('#screenShot').val();
    var withCell=parent.$('#withCell').val();
    var bigTask=parent.$('#bigTask').val();
    var sqlContext=parent.$('#sqlContext').val();
    $("#id").val(id);
    $("#taskName").val(taskName);
    $("#screenShot").val(screenShot);
    $("#withCell").val(withCell);
    $("#bigTask").val(bigTask);
    $("#value").val(value);
    $("#template").val(template);
    textarea1.setValue(sqlContext);
}


function getCodeMirror(id) {
    var mime = 'text/x-plsql';
    var textarea=CodeMirror.fromTextArea(document.getElementById(id), {
        mode: mime,
        indentWithTabs: true,
        smartIndent: true,//是否智能缩进
        lineNumbers: true,
        matchBrackets : true,
        lineWrapping: true,//自动折行
        extraKeys: {"Ctrl-Space": "autocomplete"},
        hintOptions: {tables: {
                users: ["name", "score", "birthDate"],
                countries: ["name", "population", "size"]
            }}
    });
    return textarea;
}
function saveSqlParamModel(){
	var taskName = $('#taskName').val();
    var template = $('#template').val();
	var screenShot = $('#screenShot').val();
	var withCell = $('#withCell').val();
    var bigTask= $('#bigTask').val();
    var value = $('#value').val();
    var id = $('#id').val();
	if(taskName === ""){
		alert("请填写sqlName");
		return;
	}
    var sqltext = textarea1.getValue();
    var SqlParamModel = {
        id : id,
        taskName : taskName,
        screenShot : screenShot,
        withCell : withCell,
        bigTask:bigTask,
        sqlContext :sqltext,
        templateName:template,
        value:value
    };
    $.ajax({
        xhrFields: {
            withCredentials: true //执行跨域名请求
        },
        url:cellplanServerIP+"report/saveConfig",
        type:"post",
        contentType : 'application/json',
        data: JSON.stringify(SqlParamModel),
        success:function(data){
            var status =data.status;
            var message =data.msg;
            alert(message);
            if(status=='0'){
                return;
            }
            var opt = {
                url: cellplanServerIP+'report/config',
                silent: true,
            };
            parent.$("#taskConfigTable").bootstrapTable('refresh', opt);
            parent.layer.closeAll(); //再执行关闭
        }
    });
}
