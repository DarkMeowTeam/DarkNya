<h1>IQ启示录 - 构建</h1>
<h4>CreateProcess error=206，文件名或扩展名太长...</h4>

打开项目.idea目录下的workspace.xml

在 `PropertiesComponent` 中插入下方内容 即可解决

````
<property name="dynamic.classpath" value="true" />
````

