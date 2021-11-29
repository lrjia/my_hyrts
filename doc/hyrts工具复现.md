hyrts是基于asm框架自己实现了一个插桩，但是实际上这个插桩和一般的java代码覆盖并没有什么区别。所以一开始我尝试使用现有的java代码覆盖工具，即jacoco来记录测试依赖的代码。

但是在实际去做的时候遇到了几个问题，其中最大的问题就是在收集每个测试用例的依赖信息。

## 如何在junit每运行一个测试用例结束后收集代码覆盖信息？
hyrts使用自定义junit runner的方式在每个测试结束后收集代码覆盖信息，但是正常情况下使用自定义junit runner需要去修改测试代码，在测试类中指定junit runner这对于实际项目中的代码有较大的侵入性。hyrts使用asm修改junit代码的实现方式同junit代码实现强耦合，也最终导致对于junit高版本不兼容。

另外一种方式是使用junit listener，junit listener可以通过在`pom.xml`只指定，但是只支持junit4，这可能是hyrts实现中没有使用这种方式的原因。junit listener支持在每个测试执行之前、之后执行一段自定义的代码。

jacoco并不能直接支持收集单个测试的代码覆盖，所以并不能简单的使用jacoco的api运行测试。jacoco的文档中提到可以通过JMX接口在运行时多次收集覆盖率信息，所以一种可行的方式是通过添加自定义的junit listener，在每个测试用例运行结束后去收集jacoco的覆盖信息。最后因为时间的原因，我没有使用jacoco，但是还是使用junit listener做了一个尝试。


## 复现
整个工具的复现是在hyrts代码的基础上进行的，修改了其中部分实现方式。
提交的代码一部分是hyrts反编译的代码，加上一点bug修复，另一部分相当于是对hyrts核心代码的调用。
主要的修改有几部分。
- 去掉了hyrts中maven扩展和Java agent的部分，使用离线插桩的方式（hyrts原本使用junit动态插桩），即对编译好的.class文件插桩。
- 使用junit listener在运行时收集每个测试用例的依赖（hyrts原本使用asm来指定自定义的junit runner）。
- 不支持自动运行筛选后的测试，只能显示筛选结果，因为自动运行测试需要使用maven 扩展。

## demo运行过程
- 运行`RunDemo.main`，传入参数`instrument`，完成代码插桩
- 执行 `maven surefire:tets`，运行所有测试，收集测试依赖
- 执行 `maven clean`，清除插桩过代码
- 修改待测代码`demo/examCode/src/`
- 运行`RunDemo.main`，传入参数`select`，控制台输出RTS结果

### 如果需要运行其他代码
- 根据需要运行的项目路径和包名修改`demo/RunDemo.java`中的`TARGET_CLASSPATH`和`TARGET_PACKET_NAME`(代码已经设置指定的目标类路径为`demo/examCode/src`)。
- 注意`pom.xml`中的`plugin`中指定了`junit listener`路径
