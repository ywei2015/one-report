var page = require('webpage').create();
var system = require('system');
var address = system.args[1];
//图保存地址
var path = system.args[2];
var cellIds = system.args[3];
//var address = "http://125.227.251.19:9340/taiwan/pages/heatmap_network_test.html?city=GAOXIONGSHI-cx=120.360178-cy=22.719892-dateformat=YYYYMM-datatype=MTH-sdate=201812-mr=RRC%20Connection%20Drop%20%25"; //填写需要打开的地址路径
var mr = getQueryVariable("mr");
phantom.outputEncoding="gb2312";
phantom.onError = function (msg, trace) {
    console.info("error: " + msg);
    phantom.exit(1);
};
page.onAlert = function(msg) {
    console.info("no data");
    phantom.exit(1);
};
var cellIdArray = [];
if (cellIds !== undefined && cellIds !== ""){
    cellIdArray = cellIds.split("-")
}
var length;
page.onConsoleMessage = function(msg) {
    console.log('CONSOLE: ' + msg );
    if (cellIds === undefined || cellIds === ""){
        if (msg === (mr+"-ready")){
            length = page.evaluate(function () {
                var div = document.getElementById("map0"); //要截图的div的id
                var bc = div.getBoundingClientRect();
                var topX = bc.top;               //避免使用关键字
                var left = bc.left;
                var width = bc.width;
                var height = bc.height;
                return [topX, left, width, height];
            });
            page.clipRect = {                     //截图的偏移和宽高
                top: length[0],
                left: length[1],
                width: length[2],
                height: length[3]
            };
            var filename = path + ".png";
            page.render(filename);
            phantom.exit();
        }
    }else {
        if (msg === (mr+"-ready")){
            //检测页面初始渲染完成
            //"等待 8秒";
            window.setTimeout(function () {
                renderFirstPicture();
            },8000);
        }else if(msg.indexOf("-ready") > 0){
            for (var i =0; i<cellIdArray.length; i++){
                var ready = cellIdArray[i]+"-ready";
                if (msg === ready){
                    window.setTimeout(function (i) {
                        var fileName = path + "-" + cellIdArray[i] + ".png";
                        page.render(fileName);
                        if (i < cellIdArray.length-1){
                            var nextId = cellIdArray[i+1] + "";
                            page.evaluate(function (nextId) {
                                AutoInfo(nextId);
                            },nextId);
                        }
                        if(i === (cellIdArray.length-1)){
                            page_exit();
                        }
                    },150,i);
                }
            }
        }

    }
};

page.viewportSize = {
    width: 1920,
    height: 943
};
var address_ = replaceAll(address,"-","&");
page.open(address_, function (status) {
    if (status !== 'success') {
       console.log('Unable to load the address!');
        phantom.exit(1);
    } else {
        //console.log('loaded the address!');
        window.setTimeout(function () {
            console.info("over-time");
            phantom.exit(1);
        }, 3*60*1000);
    }
});

function renderFirstPicture() {
    length = page.evaluate(function (cellIdArray) {
        // 开始第一次截图
        AutoInfo(cellIdArray[0]+'');
        var div = document.getElementById("map0"); //要截图的div的id
        var bc = div.getBoundingClientRect();
        var topX = bc.top;               //避免使用关键字
        var left = bc.left;
        var width = bc.width;
        var height = bc.height;
        return [topX, left, width, height];
    },cellIdArray);
    page.clipRect = {                     //截图的偏移和宽高
        top: length[0],
        left: length[1],
        width: length[2],
        height: length[3]
    };
}
function randomNum(minNum,maxNum){
    switch(arguments.length){
        case 1:
            return parseInt(Math.random()*minNum+1,10);
            break;
        case 2:
            return parseInt(Math.random()*(maxNum-minNum+1)+minNum,10);
            break;
        default:
            return 0;
            break;
    }
}
function page_exit() {
    window.setTimeout(function () {
        phantom.exit(1);
    });
}

function getQueryVariable(variable) {
    var query = address.substring(address.indexOf("?")+1,address.length);
    var vars = query.split("-");
    var str;
    for (var i=0;i<vars.length;i++) {
        var pair = vars[i].split("=");
        if(pair[0] == variable){
            str = pair[1]
            if (variable === "mr"){
                str = replaceAll(str,"%20"," ");
                str = replaceAll(str,"%25","%");
            }
            return str;
        }
    }
    return(false);
}

function replaceAll(s,s1, s2) {
    return s.replace(new RegExp(s1, "gm"), s2);
}