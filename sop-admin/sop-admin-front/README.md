# admin前端页面(停止维护，改用sop-admin-vue)

做到前后端分离。

当然也可以不分离，直接把前端页面放入到server中。这样做的好处是启动服务器即可访问页面。

步骤如下：

- cd到sop-admin-front目录，执行命令`mvn clean install`
- sop-admin-server工程添加maven依赖

```
<dependency>
    <groupId>com.zyd.sop</groupId>
    <artifactId>sop-admin-front</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

启动服务端，访问:http://localhost:8082

原理就是springboot访问webjars资源，建议在开发的时候前后端分离，开发完成后再打包成webjars。

