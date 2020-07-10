var page = require('webpage').create();
var system = require('system');
var address_ = system.args[1];
var indexs = system.args[2];
var path = system.args[3];
//var address = "http://192.168.0.12:800/FastUI/special/pages/epidemic_shot.html?conf=dt_shotAll_conf&keys="+address_;
//var address = "http://192.168.0.12:800/FastUI/special/pages/epidemic_shot.html?conf=dt_shotAll_conf&keys=%7B%22city%22:%22TIANJIN%22,%22startTime%22:%222020-01-20%2000:00:00%22,%22endTime%22:%222020-02-20%2023:59:59%22,%22userType%22:%22Single%20Confirmed%20Case%22,%22userInput%22:%2227947723613%22,%22curCoord%22:%22117.2351%7C39.1707%22,%22userId%22:%2215%22%7D";
var address = replaceAll(address_,"%A","&");
page.viewportSize = {
    width: 1500,
    height: 700
};
phantom.outputEncoding="gb2312";

var index =0;
path = replaceAll(path,"%20"," ");
var indexArray = [0,1,2,3,4,5,6,'all'];
if (indexs !== "null") {
    if (indexs.indexOf("-") >0){
        indexArray = indexs.split("-")
    } else {
        indexArray = [indexs];
    }
}

phantom.onError = function (msg, trace) {
    console.error("error:" + msg);

};

page.onError = function (msg, trace) {
    console.error("error:" + msg);

};

page.onAlert = function(msg) {
    console.info("no data");
};
var id;
var flag_ = false;
page.onConsoleMessage = function(msg) {
    //console.info(index + ":" +msg);
    if(msg.indexOf('complete!')>=0 || msg.indexOf("HeatMap")>=0 || flag_){
        flag_ = true;
        var delay = 5*1000;
        window.clearInterval(id);
        id=window.setInterval("screenShot()",delay);
    }
};

window.setTimeout(function () {
    console.info("over-time");
    phantom.exit(1);
}, 180*1000);

page.open(address, function (status) {
    if (status !== 'success') {
        console.log('Unable to load the address!');
        phantom.exit(1);
    } else {
        //console.log('loaded the address!');
        window.setTimeout(function () {
            length = page.evaluate(function (indexArray) {
                var div = document.getElementById("mapDetail"); //要截图的div的id
                var bc = div.getBoundingClientRect();
                var topX = bc.top;               //避免使用关键字
                var left = bc.left;
                var width = bc.width;
                var height = bc.height;
                createData(0,indexArray[0]);
                return [topX, left, width, height];
            },indexArray);
            page.clipRect = {                     //截图的偏移和宽高
                top: length[0],
                left: length[1],
                width: length[2],
                height: length[3]
            };
        }, 0*1000);

        window.setTimeout(function () {
            console.info("over-time");
            phantom.exit(1);
        }, 120*1000);
    }
});

function page_exit() {
    window.setTimeout(function () {
        phantom.exit(1);
    },1000);
}

function replaceAll(s,s1, s2) {
    return s.replace(new RegExp(s1, "gm"), s2);
}


function screenShot() {
    var nameIndex = indexArray[index];
    if (nameIndex === 'all') {
        nameIndex = 7;
    }
    var filename = path + nameIndex + ".png";

    page.render(filename);
    index ++;
    if (index > indexArray.length-1) {
        phantom.exit(1);
    }else {
        var dataIndex = indexArray[index];
        page.evaluate(function (dataIndex) {
            createData(0,dataIndex);
        },dataIndex);
        window.clearInterval(id);
    }
}