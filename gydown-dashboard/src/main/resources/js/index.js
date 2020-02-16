window.onload = function (ev) {

    //http请求后端--------------------------------------------------------------------------------------------------------
    postAjaxByObj = function (url, obj) {
        $.ajax({
            url: url,
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify({
                "downloadPath": $(obj).attr("path"),
                "downloadUrl": $(obj).attr("url")
            }),
            headers: {
                token: "admin"
            },
            success: function (data) {
                console.log(data)
            },
            error: function (err) {
                console.log(err)
            }
        });
    };

    startDownLoad = function (downUrl, path) {
        var url = "http://127.0.0.1:8080/httpDownload";
        $.ajax({
            url: url,
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify({
                "downloadPath": path,
                "downloadUrl": downUrl
            }),
            headers: {
                token: "admin"
            },
            success: function (data) {
                console.log(data)
            },
            error: function (err) {
                console.log(err)
            }
        });
    };

    startStopJob = function (obj) {
        var url = "http://127.0.0.1:8080/httpDownload"
        if ($(obj).attr("status") == 1) {
            url = "http://127.0.0.1:8080/download-stop";
        }
        postAjaxByObj(url, obj);
    };

    deleteJob = function startStop(obj) {
        var url = "http://127.0.0.1:8080/download-delete"
        postAjaxByObj(url, obj);
    };

    //隐藏元素
    deleteShade = function () {
        $("#shade").css("display", "none");
        $("#loading").css("visibility", "hidden");
    };

    deleteEle = function () {
        deleteShade();
        $("#emityJob").css("display", "none");
    };

    //http下载
    $("#startDown").click(function () {
        $("#downpathBody").css("display", "block");
        $("#shade").css("display", "block");
    });

    hideDown = function () {
        $("#downpathBody").css("display", "none");
        $("#shade").css("display", "none");
        $("#downpathBody input").eq(0).css("border", "");
        $("#downpathBody input").eq(1).css("border", "");
        $("#downpathBody input").eq(0).val("");
        $("#downpathBody input").eq(1).val("");
    };

    //点击空白隐藏弹窗
    $("#shade").click(function () {
        hideDown();
    });

    //选择下载地址文件夹
    $("#httpDirPath").click(function () {

    });

    //提交下载
    $("#subumitHttp").click(function () {
        //收集下载数据
        var inputVal = $("#downpathBody input").eq(0).val();
        if (inputVal.length == 0 || inputVal.trim() == '') {
            $("#downpathBody input").eq(0).css("border", "red solid 1px");
            return;
        }
        var pathVal = $("#downpathBody input").eq(1).val();
        if (pathVal.length == 0 || pathVal.trim() == '') {
            $("#downpathBody input").eq(1).css("border", "red solid 1px");
            return;
        }
        startDownLoad(inputVal, pathVal);
        hideDown();
    });


    //上传种子数据
    $("#uploadBtn").click(function () {
        $("#upload").click();
        $("#upload").on("change", function () {
            alert(this.files[0].name)
        });
    });


//websocket交互--------------------------------------------------------------------------------------------------------

    var ws = new WebSocket("ws://localhost:8080/gyws?token=admin");
    ws.onerror = function (ev1) {
        $("#emityJob").css("display", "block")
    };
    ws.onopen = function (evt) {
        console.log("ws open");
        ws.send("first");
        var intervalId = window.setInterval(function () {
            ws.send("ping");
        }, 5000);
    };

    var startIcon = "<svg t=\"1580370522578\" class=\"icon\" viewBox=\"0 0 1024 1024\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" p-id=\"14462\" width=\"14\" height=\"14\"><path d=\"M460.844544 0h102.4v512H460.844544z\" p-id=\"14463\" fill=\"#1afa29\"></path><path d=\"M665.644544 23.552v109.056a409.6 409.6 0 1 1-307.2 0V23.552a512 512 0 1 0 307.2 0z\" p-id=\"14464\" fill=\"#1afa29\"></path></svg>";
    var deleteIcon = "<svg t=\"1580370377245\" class=\"icon\" viewBox=\"0 0 1024 1024\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" p-id=\"14153\" width=\"14\" height=\"14\"><path d=\"M910.336 186.368l-72.704-72.704L512 439.808 186.368 113.664 113.664 186.368 439.808 512l-326.144 325.632 72.704 72.704L512 584.192l325.632 326.144 72.704-72.704L584.192 512l326.144-325.632z\" p-id=\"14154\" fill=\"#d81e06\"></path></svg>";

    ws.onmessage = function (event) {
        if (event.data == "pong") {
            console.log(event.data);
            return;
        }
        console.log($.parseJSON(event.data));
        var json = $.parseJSON(event.data);

        $("#downingTbody").html("");
        var html;
        console.log(json.length + "---");
        if (json.length < 1) {
            $("#shade").css("display", "block");
            $("#loading").css("visibility", "visible");
            $("#emityJob").css("display", "block");
            window.setTimeout(deleteShade, 200)
            return;
        }

        deleteEle();

        for (var i = 0; i < json.length; i++) {
            var status = json[i].status;
            var url = json[i].downloadUrl;
            var downLoadSpeed = json[i].downLoadSpeed;
            var path = json[i].downloadPath;
            console.log(status);
            if (status == -1) {
                downLoadSpeed = 0;
            }
            html += " <tr>\n" +
                "                            <th scope=\"row\">" + i + 1 + "</th>\n" +
                "                            <td>" + url.substr(url.lastIndexOf("/") + 1) + "</td>\n" +
                "                            <td>" + path + "</td>\n" +
                "                            <td>\n" +
                "                                <div class=\"progress\" id=\"bar\">\n" +
                "                                    <div class=\"progress-bar\" role=\"progressbar\" style=\"width:" + json[i].percentagePoint + "%\" aria-valuenow=\"25\"\n" +
                "                                         aria-valuemin=\"0\" aria-valuemax=\"100\">\n" +
                "                                        " + json[i].percentagePoint + "\n" +
                "                                    </div>\n" +
                "                                </div>\n" +
                "                            </td>\n" +
                "                            <td>" + downLoadSpeed + " m/s" + "</td>\n" +
                "                            <td><button url='" + url + "' path='" + path + "' status='" + status + "'   onclick=\"startStopJob(this)\">" + startIcon + "</button></td>\n" +
                "                            <td><button url='" + url + "' path='" + path + "' status='" + status + "' onclick=\"deleteJob(this)\">" + deleteIcon + "</button></td>\n" +
                "                        </tr>"
        }
        $("#downingTbody").append(html)
    };
}
;