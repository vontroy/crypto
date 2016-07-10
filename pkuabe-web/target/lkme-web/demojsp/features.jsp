﻿<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
    String path = request.getContextPath();
    //String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
    String basePath = "https://lkme.cc/";
%>
<!DOCTYPE html>
<html>
<head>
    <title>产品特点</title>
    <meta charset="utf-8" />
    <link href="<%=basePath %>demoh5/CSS/LinkedMe.css" rel="stylesheet" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body>
<div id="lb" class="banner">
    <div class="banner_left" onclick="javascript:location.href = '#';">
        <!--<div style="display:inline-block">
                <img class="close_img" src="<%=basePath %>demoh5/Images/Close.png" onclick="javascript:document.getElementById('lb').style.display='none';" />
            </div>-->
        <div style="display:inline-block;vertical-align:middle;">
            <img class="banner_img" src="<%=basePath %>demoh5/Images/LinkedME.png" />
        </div>
        <div class="banner_text">
            LinkedME 跨平台链接
        </div>
    </div>
    <div style="float:right;margin-top:12px;top:0;">
        <a href="" id="link">
            <img src="<%=basePath %>demoh5/Images/open.png" height="30" style="vertical-align:middle;margin-right:5px;" />
        </a>
        <script>
            var btn = document.getElementById('link');
            var search = location.search;
            var linkedme = /^\?linkedme=(.*)/.exec(search);
            linkedme && btn.setAttribute('href', linkedme[1]);
        </script>
    </div>
    <div style="clear:both"></div>
</div>
    
    <div class="char_center">
        <div class="char_row">
            <div class="char_div_index">
                <img class="char_index" src="<%=basePath %>demoh5/Images/Characters/1.png" />
            </div>
            <div class="char_title">
                <img class="char_title_image" src="<%=basePath %>demoh5/Images/Characters/title.png" /> 跨平台深度链接机制
            </div>
            <div class="char_div_ill">
                <img class="char_ill" src="<%=basePath %>demoh5/Images/Characters/chart1.png" />
            </div>
            <div class="char_content">
                深度链接将打破移动互联网的信息孤岛，使得应用与应用之间互联！中国的应用数量已经达到千万级别，但几乎所有的APP都没有实现深度链接，LinkedME以深度链接技术为基础为企业提供一些列的产品服务，帮助企业增加运营的效益。
                LinkedME的深度链接机制的重要特性是跨平台，该特性主要体现在三个方面，第一，跨开发平台，支持Android，iOS，Hybrid App；第二，跨不同终端，支持Phone，Pad，Pc等等；第三，跨应用商店，不因应用商店下载，而丢掉链接，
                支持应用宝，豌豆荚，360手机助手，百度手机助手，小米商店等等；
            </div>
        </div>
        <div class="char_row">
            <div class="char_div_index">
                <img class="char_index" src="<%=basePath %>demoh5/Images/Characters/2.png" />
            </div>
            <div class="char_title">
                <img class="char_title_image" src="<%=basePath %>demoh5/Images/Characters/title.png" /> 深度分享
            </div>
            <div class="char_div_ill">
                <img class="char_ill" src="<%=basePath %>demoh5/Images/Characters/chart2.png" />
            </div>
            <div class="char_content">
                所有的APP都实现了分享组件，但分享的链接不是移动应用具体页面内容的链接，而是针对移动应用内容开发的H5页面链接。
                当用户把APP内容分享到微信，微博，短信，邮箱，知乎，豆瓣，QQ及QQ空间时，其他用户不用下载APP就能查看APP的内容，确实给用户带了较好的体验，但是用户想直接去APP内容查看，几乎所有的APP都不支持！
                LinkedME针对已集成SDK的APP，为APP的所有页面统一分配唯一的链接，当用户在APP的外部点击该链接时，LinkedME检测用户是否安装该链接对应的APP，已安装直接打开APP具体页面，未安装跳转应用商店，下载安装后依然打开具体页面。
                LinkedME提高了APP的下载量和阅读量，也为用户提供了极致的浏览体验。
            </div>
        </div>
        <div class="char_row">
            <div class="char_div_index">
                <img class="char_index" src="<%=basePath %>demoh5/Images/Characters/3.png" />
            </div>
            <div class="char_title">
                <img class="char_title_image" src="<%=basePath %>demoh5/Images/Characters/title.png" /> App互联
            </div>
            <div class="char_div_ill">
                <img class="char_ill" src="<%=basePath %>demoh5/Images/Characters/chart3.png" />
            </div>
            <div class="char_content">
                信息在于互联，知识在于联想！当下互联网的业务越来越体现出垂直化，细分化，但用户的需求是多样化，致使App之间互通互联是必然之路。当下的App互联主要是问题是本身App没有实现深度链接技术，导致无法互联，及时实现深度链接技术但有没有标准，
                导致App互联也为企业增加了很大开发负担。LinkedME为所有集成SDK的App，统一分配唯一链接提供一系列的衍生服务，致使App的互联互联不再困难！

            </div>
        </div>
        <div class="char_row">
            <div class="char_div_index">
                <img class="char_index" src="<%=basePath %>demoh5/Images/Characters/4.png" />
            </div>
            <div class="char_title">
                <img class="char_title_image" src="<%=basePath %>demoh5/Images/Characters/title.png" /> 自定义及智能化
            </div>
            <div class="char_div_ill">
                <img class="char_ill" src="<%=basePath %>demoh5/Images/Characters/chart4.png" />
            </div>
            <div class="char_content">
                LinkedME提供自定义，智能化特点。自定义是指企业用户针对具体的运营活动生成LinkedME链接，由该链接可以清晰分析运营活动的效果，比如生成LinkedME时，支持feature，tags，channel等等属性字段。智能化是指LinkedME的所有链接
                能够自动检测是桌面用户，手机用户，平板电脑用户点击链接，从而LinkedME针对不同的用户应为引导用户做出不同的动作。
            </div>
        </div>
        <div class="char_row">
            <div class="char_div_index">
                <img class="char_index" src="<%=basePath %>demoh5/Images/Characters/5.png" />
            </div>
            <div class="char_title">
                <img class="char_title_image" src="<%=basePath %>demoh5/Images/Characters/title.png" /> 可视化数据分析
            </div>
            <div class="char_div_ill">
                <img class="char_ill" src="<%=basePath %>demoh5/Images/Characters/chart5.png" />
            </div>
            <div class="char_content">
                LinkedME为所有集成SDK的APP，统一分配唯一链接，并记录链接的所有动态信息，比如点击次数、点击时间、打开或安装APP、流量渠道等等，从而为企业用户提供了友好的可视化数据分析控制后台，方便企业查看并分析所有的链接数据，
                提高企业的运营效率，增加公司效益。比如，企业用户能够一目了然的查看到某个时间段App的打开和安装数量，以及来自那些渠道，比如微博，微信，还是自定于渠道。
            </div>
        </div>
        <div class="char_row">
            <div class="char_div_index">
                <img class="char_index" src="<%=basePath %>demoh5/Images/Characters/6.png" />
            </div>
            <div class="char_title">
                <img class="char_title_image" src="<%=basePath %>demoh5/Images/Characters/title.png" /> 企业级服务
            </div>

            <div class="char_content">
                LinkedME提供企业级，免费的链接服务，主要特性体现在以下几个方面：第一，快速集成，LinkedME提供的iOS、Android、Hybrid的SDK非常轻量，提供的API也非常简单，企业开发人员十几分即可熟悉API功能；
                第二，LinkedME始终支持Android，iOS，Hybrid APP技术的更新，并及时更新LinkedME所有技术服务，比如iOS9增加了很多特性，LinkedME将逐步依次实现。
                第三，稳定可靠，LinkedME的后台经过巨大的压力测试，支持每日过亿访问量，与此同时，对企业所有链接数据提供备灾方案；
                第四，隐私保护，LinkedME针对企业的所有链接数据绝对安全保护；
            </div>
        </div>

    </div>

    <div class="footer">
        Copyright@2014-2015 LinkedME 北京微方程科技有限公司
    </div>

</body>
</html>
