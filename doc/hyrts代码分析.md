# hyrts工具运行

HyRTS的作者在maven上提供了他的工具，但是并没有开源，[文档](http://hyrts.org/)也不是很详细。

我一开始尝试运行HyRTS的工具，但是没有运行成功，在收集测试依赖的过程中报错StackOverflowError。后来我发现是我的junit版本和作者不同，作者使用的junit版本是4.10而我使用的junit版本是4.13.2，虽然在hyrts的文档中说明支持junit4。

![[Pasted image 20211128115430.png]]

之后我使用了junit4.10的版本，尝试了一段时间后，成功运行hyrts工具，这里对hyrts的文档做一个补充。

## 基本的hyrts运行步骤

- 创建一个基于junit4.10测试的maven项目，添加[文档](http://hyrts.org/)中的hyrts plugin
- 执行`mvn hyrts:HyRTS`，
	- 首先这一步会运行项目中所有的junit测试，运行的过程中动态插桩，收集测试依赖并记录到文件夹 `hyrts-files`；
	- 同时这个命令也会将当前的代码的校验和保存到 `hyrts-files`中，用于之后计算变更
- 为了测试hyrts的效果，我们对源代码进行修改
- 再次执行`mvn hyrts:HyRTS`，
	- 这一步会首先将当前的代码的校验和与之前的`hyrts-files`记录的代码校验和进行比较，计算出变化的代码。
	- 结合之前记录的测试依赖，通过hyrts的算法计算出需要执行的测试用例。
	- 调用junit test，执行被筛选过后测试用例，同时动态插桩收集依赖（因为默认是在线模式）并记录到`hyrts-files`。
	- 保存当前代码的校验和到`hyrts-files`用于之后的rts。
- 重复前两步，可以再开发过程中不断同前一个执行`mvn hyrts:HyRTS`的版本比较，进行测试筛选并执行

# 代码分析
整个hyrts最后是实现为一个maven插件的形式。hyrts的执行流程整体上分为几个部分，插桩，运行测试收集依赖，记录文件校验和计算diff，选择并执行需要执行的测试。

## 准备
hyrts的工具并没有开源，也没有说明其存储的数据结构的格式，所以我反编译了作者在maven上提供的工具。方法是使用idea自带的反编译插件。
`java -cp "$IDEA_PATH\plugins\java-decompiler\lib\java-decompiler.jar"org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler -dgs=trueBehinder.jar BehinderDeCompiler`
在反编译之后从中取出hyrts的代码，然后再自己创建一个maven项目。因为hyrts中依赖的asm版本在maven central中找不到，所以就直接以原来的hyrts作为项目的依赖（具体查看pom.xml）。最后的效果是在运行到hyrts核心代码时用反编译的代码，进行debug，在运行到项目依赖的时候直接使用原来hyrts jar包的依赖。

## 插桩代码
hyrts使用的是基于javaagent的运行时动态插桩。在这里给出`JUnitAgent.java`中的关键的部分。

```java
//set/hyrts/coverage/agent/JUnitAgent.java
public class JUnitAgent {  
    public static void premain(String args, Instrumentation inst) throws Exception {  
        ...
		if (Properties.TRACER_COV_TYPE != null) {  
            if (!Properties.EXECUTION_ONLY) {  
                inst.addTransformer(new ClassTransformer());  
            }  
  
            inst.addTransformer(new JUnitTestClassLevelTransformer());  
        }  
  
  	...
    }
	...
}
```

这里使用java instrument api来动态修改代码。其中`ClassTransformer`用于插桩代码, `JUnitTestClassLevelTransformer`用于修改junit代码，只执行经过筛选的测试用例。

下面简单解释`ClassTransformer`，同样这里只解释关键代码。`JUnitTestClassLevelTransformer`和收集插桩信息相关，之后再介绍。

```java
//set/hyrts/coverage/agent/ClassTransformer.java
import set.hyrts.org.objectweb.asm.ClassVisitor;  
import set.hyrts.org.objectweb.asm.MethodVisitor;  
import set.hyrts.org.objectweb.asm.Opcodes;
public class ClassTransformer implements ClassFileTransformer {
	public byte[] transform(ClassLoader loader, String slashClassName, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {  
		if (isExcluded(loader, slashClassName)) {  
			return classfileBuffer;  
		} else {  
			String dotClassName = Classes.toDotClassName(slashClassName);  
			int clazzId = CoverageData.registerClass(slashClassName, dotClassName);  
			ClassReader reader = new ClassReader(classfileBuffer);  
			ClassWriter writer = new ComputeClassWriter(FrameOptions.pickFlags(classfileBuffer));  
			ClassTransformVisitor cv = new ClassTransformVisitor(clazzId, slashClassName, dotClassName, writer);  
			reader.accept(cv, 8);  
			byte[] result = writer.toByteArray();  
			return result;  
		}  
	}
}
```
这里基于asm框架，通过实现一个自定义的`ClassVisitor`即`ClassTransformVisitor`来实现代码插桩。`ClassReader`读取字节码，使用visitor模式，accept`ClassVisitor`，`ClassVisitor`将接受到的类结构信息记录到`ClassWriter`，同时为了实现插桩加入部分插桩代码，最后`ClassWriter`会将转化后的classfile返回。

具体的`ClassTransformVisitor`实现比较复杂，因为要对各种类、方法、基本快进行插桩，在这里举例其中的一小部分

```java
//set/hyrts/coverage/core/MethodVisitorMethCov.java
class MethodVisitorMethCov extends MethodVisitor implements Opcodes {
	public void visitCode() {  
		this.mv.visitLdcInsn(this.clazzId);  
		this.mv.visitLdcInsn(this.methId);  
		this.mv.visitMethodInsn(184, "set/hyrts/coverage/core/Tracer", "traceMethCovInfo", "(II)V", false);  
		super.visitCode();  
	}
}
```
这一段代码的功能是在每个方法的代码之前都添加一个`Tracer.traceMethCovInfo(clazzId,methId)`的函数调用，其中`classId`，`methodId`是hyrts给每个类和方法的编号，`Tracer`是hyrts中用于记录插桩信息的数据结构。

这是插桩之后的结果，可以看到在类的构造方法的开头添加了一个`Tracer.traceMethCovInfo`的函数调用。
```java
public class parent {  
    public parent() {  
        Tracer.traceMethCovInfo(1, 1);  
        super();  
    }  
}
```

## 收集依赖
通过上面的分析，我们知道运行时的插桩记录是保存在`set/hyrts/coverage/core/Tracer.java`中的，因为hyrts是以测试类为单位收集插桩信息，所以在每个测试运行完成之后都需要保存记录到`Tracer`中的信息。具体来说，需要每一个测试类执行完成之后调用`FTracerJUnitUtils.dumpCoverage()`方法，保存到`hyrts-files`中。想法很直接，但是实现起来有一点曲折。

hyrts实现的方法同插桩一样，是基于junit，就是前文提到的`JUnitTestClassLevelTransformer`，通过对junit代码进行修改，具体来说来说是对`org/junit/runner/Runner`的`runnerForClass`方法进行修改，使用自定义的junit runner，即`FTracerJUnit4Runner`，从而实现在每次测试类运行结束之后保存插桩记录的运行信息。

可以说这里的实现是同junit的实现强耦合的，这也是为什么junit升级到4.13之后，hyrts就不能正常运行的原因。


## 记录文件校验和，计算diff
入口是`set/hyrts/diff/ClassContentParser.java`中的`parseAndSerializeNewContents`方法，这个方法是通过读取`target`目录中编译后的class文件，然后通过asm框架解析，记录下需要的类文件信息以及相关的校验和，然后保存到`hyrts-files/HyRTS-checksum.gz`中。

`ClassContentParser`中的`deserializeOldContents`与之相反，是用于从`hyrts-files/HyRTS-checksum.gz`读取之前记录的类文件信息。

计算diff的方法是`VersionDiff.diff`，就是将两个版本代码之前记录的信息按照论文中的几个不同的变更类型，像是文件变更，方法变更进行比较，并记录。

## 选择并执行需要执行的测试。
基础版本的hyrts的入口方法是 `HybridRTS.main()` ，这个方法读取之前记录的`hyrts-files`中的插桩得到的测试运行代码依赖，以及两次代码版本的checksum，通过论文中介绍的算法，计算出需要执行的测试。

选择执行筛选过之后的测试应该是通过自定义maven扩展向`SurefirePlugin`传递参数实现的，但是具体细节就不讨论了，一是之后的demo并没有涉及这一部分，另外也确实是有点复杂，因为这里涉及到很多maven的api和内部实现。
