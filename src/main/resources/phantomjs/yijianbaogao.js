'use strict';
var page = require('webpage').create(),
    page1 = require('webpage').create(),
    system = require('system');
var login_url = "http://125.227.251.19:9340/cas/login?service=http%3A%2F%2F125.227.251.19%3A9340%2Fportal%2F";
var page_url = "http://125.227.251.19:9340/taiwan/pages/heatmap_network_test.html?__PID=NetWork_Test";
phantom.outputEncoding="gb2312";
page1.onConsoleMessage = function(msg) {
    console.log('CONSOLE: ' + msg );
};
/*page1.onResourceRequested = function (requestData, networkRequest) {
    var url = requestData.url;
    if (url.indexOf("login.js") >= 0){
        console.info(url)
        networkRequest.abort();
    }
}*/
function login(){
    page.open(login_url,function(status){
        if("success" === status){
            page.evaluate(function(){
                function getBase64Image(img) {
                    var canvas = document.createElement("canvas");
                    canvas.width = img.width;
                    canvas.height = img.height;
                    var ctx = canvas.getContext("2d");
                    ctx.drawImage(img, 0, 0, img.width, img.height);
                    var dataURL = canvas.toDataURL("image/png");
                    return dataURL.replace("data:image/png;base64,", "");
                }
                function getCode() {
                    var img = document.getElementsByTagName("img")[0];
                    var code_ = getBase64Image(img);
                    var url = 'http://localhost:8081/pyjs/code';
                    $.ajax({
                        url : url,
                        type : 'post',
                        async: false,
                        data:{code:code_},
                        success : function(data){
                            $("#username").val("admin");
                            $("#password").val("fast*1234");
                            $("#j_captcha_response").val(data);
                            var button = $("#fm1 .btn-submit");
                            button.click();
                        }
                    });
                }
                getCode();
            });
            setTimeout('openPage()',1500);
        }else {
            phantom.exit(1);
        }
    });
}

function openPage(){
    console.log("ready to capture");

    page.close();
    page1.open(page_url,function(status){
        if("success" === status){
            console.log("open page succeed");
            onPageReady();
        }else{
            console.log("open page failed");
            onPageReady();
            //phantom.exit(1);
        }
    });
}

function onPageReady(){
    page1.viewportSize = {
        width: 1920,
        height: 943
    };
    setTimeout(function(){
        /*var length = page1.evaluate(function () {
            //var div = $(window.frames["link1"].document).find("#myTabContent .col-md-7 .widget").first()[0];
            //var div = $("[name='link1']").contents().find("#map0");
            var ol_ = $(window.parent.document).contents().find("[name='link1']")[0].contentWindow.ol;
            var to = ol_.proj.fromLonLat([Number(121.599792), Number(25.064186)]);
            var soMap0_ = $(window.parent.document).contents().find("[name='link1']")[0].contentWindow.soMap0;
            var view = soMap0_.getMap().getView();
            view.setZoom(18);
            view.animate({
                center: to,
                duration: 0
            });
            var bc_doc = document.getElementsByTagName("iframe")[0].getBoundingClientRect();
            var topX = bc_doc.top+(bc_doc.top/2-30);               //避免使用关键字
            var left = bc_doc.left+(bc_doc.left/2-30);
            var width = 60;
            var height = 60;
            return  [topX, left, width, height];
        });
        console.log("length-----"+length);*/
        /*page1.clipRect = {                     //截图的偏移和宽高
            top: length[0],
            left: length[1],
            width: length[2],
            height: length[3]
        };*/
        var filename = "C:\\export\\phantom\\img\\"+"page1-"+randomNum(0,200)+".png";
        console.info(page1.loadingProgress);
        //outPutCookie(page1.cookies);
        page1.render(filename);
        phantom.exit(1);
    },1000*45);
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

function outPutCookie(cookies) {
    var cookies_ = [];
    if (cookies) {
        cookies_.push(' cookies:');
        cookies.forEach(function (t) {
            cookies_.push( 'name' + ':' + t.name + "-" +"path:"+t.path + "-" +"value:"+t.value);
        });
        console.info(cookies_.join('\n'));
    }
}
/*setTimeout(function(){
    console.info("over_time");
    phantom.exit(1);
    },3*60*1000);*/
login();