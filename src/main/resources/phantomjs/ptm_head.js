var page = require('webpage').create();
var system = require('system');
var address = system.args[1];
var path = system.args[2];
//var address = "http://125.227.251.19:9340/taiwan/pages/heatmap_network_test.html?city=GAOXIONGSHI-cx=120.360178-cy=22.719892-dateformat=YYYYMM-datatype=MTH-sdate=201812-mr=RRC%20Connection%20Drop%20%25";
phantom.outputEncoding="gb2312";

phantom.onError = function (msg, trace) {
    console.log(msg);
    phantom.exit(1);
};
page.viewportSize = {
    width: 1920,
    height: 943
};
var wh = [200,200];
page.onConsoleMessage = function(msg) {
    //console.log('CONSOLE: ' + msg );
    if (msg.indexOf("-ready-1") > 0){
        var length = page.evaluate(function (wh) {
            var view = soMap0.getMap().getView();
            view.setZoom(17);
            var div = document.getElementById("map0"); //要截图的div的id
            var bc = div.getBoundingClientRect();
            var topX = (bc.top)+bc.height/2-wh[1]/2;               //避免使用关键字
            var left = bc.left+bc.width/2-wh[0]/2;
            var width = wh[0];
            var height = wh[1];
            return [topX, left, width, height];
        },wh);
        page.clipRect = {                     //截图的偏移和宽高
            top: length[0],
            left: length[1],
            width: length[2],
            height: length[3]
        };
    }
    if (msg.indexOf("-ready-2") > 0){
        window.setTimeout(function () {
            var filename = path + "head.png";
            page.render(filename);
            page_exit();
        },1000);
    }
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



function page_exit() {
    window.setTimeout(function () {
        phantom.exit(1);
    });
}

function getQueryVariable(variable) {
    var query = address.substring(address.indexOf("?")+1,address.length);
    var vars = query.split("-");
    for (var i=0;i<vars.length;i++) {
        var pair = vars[i].split("=");
        if(pair[0] == variable){return pair[1];}
    }
    return(false);
}

function replaceAll(s,s1, s2) {
    return s.replace(new RegExp(s1, "gm"), s2);
}