# 01｜原始IoC：如何通过BeanFactory实现原始版本的IoC容器？

你好，我是郭屹，从今天开始我们来学习手写MiniSpring。

这一章，我们将从一个最简单的程序开始，一步步堆积演化，最后实现Spring这一庞大框架的核心部分。这节课，我们就来构造第一个程序，也是最简单的一个程序，将最原始的IoC概念融入我们的框架之中， **我们就用这个原始的IoC容器来管理一个Bean。** 不过要说的是，它虽然原始，却也是一个可以运行的IoC容器。

## IoC容器

如果你使用过Spring或者了解Spring框架，肯定会对IoC容器有所耳闻。它的意思是使用Bean容器管理一个个的Bean，最简单的Bean就是一个Java的业务对象。在Java中，创建一个对象最简单的方法就是使用 new 关键字。IoC容器，也就是 **BeanFactory，存在的意义就是将创建对象与使用对象的业务代码解耦**，让业务开发人员无需关注底层对象（Bean）的构建和生命周期管理，专注于业务开发。

那我们可以先想一想，怎样实现Bean的管理呢？我建议你不要直接去参考Spring的实现，那是大树长成之后的模样，复杂而庞大，令人生畏。

作为一颗种子，它其实可以非常原始、非常简单。实际上我们只需要几个简单的部件：我们用一个部件来对应Bean内存的映像，一个定义在外面的Bean在内存中总是需要有一个映像的；一个XML reader 负责从外部XML文件获取Bean的配置，也就是说这些Bean是怎么声明的，我们可以写在一个外部文件里，然后我们用XML reader从外部文件中读取进来；我们还需要一个反射部件，负责加载Bean Class并且创建这个实例；创建实例之后，我们用一个Map来保存Bean的实例；最后我们提供一个getBean() 方法供外部使用。我们这个IoC容器就做好了。

![图片](assets/a382d7774c7aa504231721c7d28028c3.png)

好，接下来我们一步步来构造。

## 实现一个原始版本的IoC容器

对于现在要着手实现的原始版本Bean，我们先只管理两个属性： **id与class**。其中，class表示要注入的类，而id则是给这个要注入的类一个别名，它可以简化记忆的成本。我们要做的是把Bean通过XML的方式注入到框架中，你可以看一下XML的配置。

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<beans>
    <bean id = "xxxid" class = "com.minis.xxxclass"></bean>
</beans>

```

接下来我们要做一些准备工作。首先，新建一个Java项目，导入dom4j-1.6.1.jar包。这里导入的dom4j包封装了许多操作XML文件的方法，有助于我们快速处理XML文件中的各种属性，这样就不需要我们自己再写一个XML的解析工具了，同时它也为我们后续处理依托于XML注入的Bean提供了便利。

另外要说明的是，我们写MiniSpring是为了学习Spring框架，所以我们会尽量少地去依赖第三方包，多自己动手，以原始社会刀耕火种的方式写程序，这可以让我们彻底地理解底层原理。希望你能够跟我一起动手，毕竟编程说到底是一个手艺活，要想提高编程水平，唯一的方法就是动手去写。只要不断学，不断想，不断做，就能大有成效。

### 构建BeanDefinition

好了，在有了第一个Java项目后，我们创建com.minis包，我们所有的程序都是放在这个包下的。在这个包下构建第一个类，对应Bean的定义，命名为BeanDefinition。我们在这个类里面定义两个最简单的域：id与className。你可以看一下相关代码。

```java
public class BeanDefinition {
    private String id;
    private String className;
    public BeanDefinition(String id, String className) {
        this.id = id;
        this.className = className;
    }
    //省略getter和setter

```

可以看到，这段代码为这样一个Bean提供了全参数的构造方法，也提供了基本的getter和setter方法，方便我们获取域的值以及对域里的值赋值。

### 实现ClassPathXmlApplicationContext

接下来，我们假定已经存在一个用于注入Bean的XML文件。那我们要做的自然是，按照一定的规则将这个XML文件的内容解析出来，获取Bean的配置信息。我们的第二个类ClassPathXmlApplicationContext就可以做到这一点。通过这个类的名字也可以看出，它的作用是解析某个路径下的XML来构建应用上下文。让我们来看看如何初步实现这个类。

```java
public class ClassPathXmlApplicationContext {
    private List<BeanDefinition> beanDefinitions = new ArrayList<>();
    private Map<String, Object> singletons = new HashMap<>();
    //构造器获取外部配置，解析出Bean的定义，形成内存映像
    public ClassPathXmlApplicationContext(String fileName) {
        this.readXml(fileName);
        this.instanceBeans();
    }
    private void readXml(String fileName) {
        SAXReader saxReader = new SAXReader();
        try {
            URL xmlPath =
this.getClass().getClassLoader().getResource(fileName);
            Document document = saxReader.read(xmlPath);
            Element rootElement = document.getRootElement();
            //对配置文件中的每一个<bean>，进行处理
            for (Element element : (List<Element>) rootElement.elements()) {
                //获取Bean的基本信息
                String beanID = element.attributeValue("id");
                String beanClassName = element.attributeValue("class");
                BeanDefinition beanDefinition = new BeanDefinition(beanID,
beanClassName);
                //将Bean的定义存放到beanDefinitions
                beanDefinitions.add(beanDefinition);
            }
        }
    }
    //利用反射创建Bean实例，并存储在singletons中
    private void instanceBeans() {
        for (BeanDefinition beanDefinition : beanDefinitions) {
            try {
                singletons.put(beanDefinition.getId(),
Class.forName(beanDefinition.getClassName()).newInstance());
            }
        }
    }
    //这是对外的一个方法，让外部程序从容器中获取Bean实例，会逐步演化成核心方法
    public Object getBean(String beanName) {
        return singletons.get(beanName);
    }
}

```

由上面这一段代码可以看出，ClassPathXmlApplicationContext定义了唯一的构造函数，构造函数里会做两件事：一是提供一个readXml()方法，通过传入的文件路径，也就是XML文件的全路径名，来获取XML内的信息，二是提供一个instanceBeans()方法，根据读取到的信息实例化Bean。接下来让我们看看，readXml和instanceBeans这两个方法分别做了什么。

首先来看readXML，这也是我们解析Bean的核心方法，因为配置在XML内的Bean信息都是文本信息，需要解析之后变成内存结构才能注入到容器中。该方法最开始创建了SAXReader对象，这个对象是dom4j包内提供的。随后，它通过传入的fileName，也就是定义的XML名字，获取根元素，也就是XML里最外层的标签。然后它循环遍历标签中的属性，通过 `element.attributeValue("id")` 和 `element.attributeValue("class")` 拿到配置信息，接着用这些配置信息构建BeanDefinition对象，然后把BeanDefinition对象加入到BeanDefinitions列表中，这个地方就保存了所有Bean的定义。

接下来，我们看看instanceBeans方法实现的功能：实例化一个Bean。因为BeanDefinitions存储的BeanDefinition的class只是一个类的全名，所以我们现在需要将这个名字转换成一个具体的类。我们可以通过Java里的反射机制，也就是Class.forName将一个类的名字转化成一个实际存在的类，转成这个类之后，我们把它放到singletons这个Map里，构建 ID 与实际类的映射关系。

到这里，我们就把XML文件中的Bean信息注入到了容器中。你可能会问，我到现在都没看到BeanFactory呀，是不是还没实现完？

其实不是的，目前的ClassPathXmlApplicationContext兼具了BeanFactory的功能，它通过singletons和beanDefinitions初步实现了Bean的管理，其实这也是Spring本身的做法。后面我会进一步扩展的时候，会分离这两部分功能，来剥离出一个独立的BeanFactory。

### 验证功能

现在，我们已经实现了第一个管理Bean的容器，但还要验证一下我们的功能是不是真的实现了。下面我们就编写一下测试代码。在com.minis目录下，新增test包。你可以看一下相关的测试代码。

```java
public interface AService {
    void sayHello();
}

```

这里，我们定义了一个sayHello接口，该接口的实现是在控制台打印出“a service 1 say hello”这句话。

```java
public class AServiceImpl implements AService {
    public void sayHello() {
        System.out.println("a service 1 say hello");
    }
}

```

我们将XML文件命名为beans.xml，注入AServiceImpl类，起个别名，为aservice。

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<beans>
    <bean id = "aservice" class = "com.minis.test.AServiceImpl"></bean>
</beans>

```

除了测试代码，我们还需要启动类，定义main函数。

```java
public class Test1 {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext ctx = new
ClassPathXmlApplicationContext("beans.xml");
        AService aService = (AService)ctx.getBean("aservice");
        aService.sayHello();
    }
}

```

在启动函数中可以看到，我们构建了ClassPathXmlApplicationContext，传入文件名为“beans.xml”，也就是我们在测试代码中定义的XML文件名。随后我们通过getBean方法，获取注入到singletons里的这个类AService。aService在这儿是AService接口类型，其底层实现是AServiceImpl，这样再调用AServiceImpl类中的sayHello方法，就可以在控制台打印出“a service 1 say hello”这一句话。

到这里，我们已经成功地构造了一个最简单的程序： **最原始的IoC容器**。在这个过程中我们引入了BeanDefinition的概念，也实现了一个应用的上下文ClassPathXmlApplicationContext，从外部的XML文件中获取文件信息。只用了很少的步骤就实现了IoC容器对Bean的管理，后续就不再需要我们手动地初始化这些Java对象了。

## 解耦ClassPathXmlApplicationContext

但是我们也可以看到，这时的 ClassPathXmlApplicationContext 承担了太多的功能，这并不符合我们常说的对象单一功能的原则。因此，我们需要做的优化扩展工作也就呼之欲出了：分解这个类，主要工作就是两个部分，一是提出一个最基础的核心容器，二是把XML这些外部配置信息的访问单独剥离出去，现在我们只有XML这一种方式，但是之后还有可能配置到Web或数据库文件里，拆解出去之后也便于扩展。

为了看起来更像Spring，我们以Spring的目录结构为范本，重新构造一下我们的项目代码结构。

```java
com.minis.beans;
com.minis.context;
com.minis.core;
com.minis.test;

```

### 定义BeansException

在正式开始解耦工作之前，我们先定义属于我们自己的异常处理类：BeansException。我们来看看异常处理类该如何定义。

```java
public class BeansException extends Exception {
  public BeansException(String msg) {
    super(msg);
  }
}

```

可以看到，现在的异常处理类比较简单，它是直接调用父类（Exception）处理并抛出异常。有了这个基础的BeansException之后，后续我们可以根据实际情况对这个类进行拓展。

### 定义 BeanFactory

首先要拆出一个基础的容器来，刚才我们反复提到了 BeanFactory 这个词，现在我们正式引入BeanFactory这个接口，先让这个接口拥有两个特性：一是获取一个Bean（getBean），二是注册一个BeanDefinition（registerBeanDefinition）。你可以看一下它们的定义。

```java
public interface BeanFactory {
    Object getBean(String beanName) throws BeansException;
    void registerBeanDefinition(BeanDefinition beanDefinition);
}

```

### 定义Resource

刚刚我们将BeanFactory的概念进行了抽象定义。接下来我们要定义Resource这个概念，我们把外部的配置信息都当成Resource（资源）来进行抽象，你可以看下相关接口。

```java
public interface Resource extends Iterator<Object> {
}

```

### 定义ClassPathXmlResource

目前我们的数据来源比较单一，读取的都是XML文件配置，但是有了Resource这个接口后面我们就可以扩展，从数据库还有Web网络上面拿信息。现在有BeanFactory了，有Resource接口了，拆解这两部分的接口也都有了。接下来就可以来实现了。

现在我们读取并解析XML文件配置是在ClassPathXmlApplicationContext类中完成的，所以我们下一步的解耦工作就是定义ClassPathXmlResource，将解析XML的工作交给它完成。

```java
public class ClassPathXmlResource implements Resource{
    Document document;
    Element rootElement;
    Iterator<Element> elementIterator;
    public ClassPathXmlResource(String fileName) {
        SAXReader saxReader = new SAXReader();
        URL xmlPath = this.getClass().getClassLoader().getResource(fileName);
        //将配置文件装载进来，生成一个迭代器，可以用于遍历
        try {
            this.document = saxReader.read(xmlPath);
            this.rootElement = document.getRootElement();
            this.elementIterator = this.rootElement.elementIterator();
        }
    }
    public boolean hasNext() {
        return this.elementIterator.hasNext();
    }
    public Object next() {
        return this.elementIterator.next();
    }
}

```

操作XML文件格式都是dom4j帮我们做的。

> 注：dom4j这个外部jar包方便我们读取并解析XML文件内容，将XML的标签以及参数转换成Java的对象。当然我们也可以自行写代码来解析文件，但是为了简化代码，避免重复造轮子，这里我们选择直接引用第三方包。

### XmlBeanDefinitionReader

现在我们已经解析好了XML文件，但解析好的XML如何转换成我们需要的BeanDefinition呢？这时XmlBeanDefinitionReader就派上用场了。

```java
public class XmlBeanDefinitionReader {
    BeanFactory beanFactory;
    public XmlBeanDefinitionReader(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
    public void loadBeanDefinitions(Resource resource) {
        while (resource.hasNext()) {
            Element element = (Element) resource.next();
            String beanID = element.attributeValue("id");
            String beanClassName = element.attributeValue("class");
            BeanDefinition beanDefinition = new BeanDefinition(beanID, beanClassName);
            this.beanFactory.registerBeanDefinition(beanDefinition);
        }
    }
}

```

可以看到，在XmlBeanDefinitionReader中，有一个loadBeanDefinitions方法会把解析的XML内容转换成BeanDefinition，并加载到BeanFactory中。

### BeanFactory功能扩展

首先，定义一个简单的BeanFactory实现类SimpleBeanFactory。

```java
public class SimpleBeanFactory implements BeanFactory{
    private List<BeanDefinition> beanDefinitions = new ArrayList<>();
    private List<String> beanNames = new ArrayList<>();
    private Map<String, Object> singletons = new HashMap<>();
    public SimpleBeanFactory() {
    }

    //getBean，容器的核心方法
    public Object getBean(String beanName) throws BeansException {
        //先尝试直接拿Bean实例
        Object singleton = singletons.get(beanName);
        //如果此时还没有这个Bean的实例，则获取它的定义来创建实例
        if (singleton == null) {
            int i = beanNames.indexOf(beanName);
            if (i == -1) {
                throw new BeansException();
            }
            else {
                //获取Bean的定义
                BeanDefinition beanDefinition = beanDefinitions.get(i);
                try {
                    singleton = Class.forName(beanDefinition.getClassName()).newInstance();
                }
                //注册Bean实例
                singletons.put(beanDefinition.getId(), singleton);
            }
        }
        return singleton;
    }

    public void registerBeanDefinition(BeanDefinition beanDefinition) {
        this.beanDefinitions.add(beanDefinition);
        this.beanNames.add(beanDefinition.getId());
    }
}

```

由SimpleBeanFactory的实现不难看出，这就是把ClassPathXmlApplicationContext中有关BeanDefinition实例化以及加载到内存中的相关内容提取出来了。提取完之后ClassPathXmlApplicationContext就是一个“空壳子”了，一部分交给了BeanFactory，一部分又交给了Resource和Reader。这时候它又该如何发挥“集成者”的功能呢？我们看看它现在是什么样子的。

```java
public class ClassPathXmlApplicationContext implements BeanFactory{
    BeanFactory beanFactory;
    //context负责整合容器的启动过程，读外部配置，解析Bean定义，创建BeanFactory
    public ClassPathXmlApplicationContext(String fileName) {
        Resource resource = new ClassPathXmlResource(fileName);
        BeanFactory beanFactory = new SimpleBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
        reader.loadBeanDefinitions(resource);
        this.beanFactory = beanFactory;
    }
    //context再对外提供一个getBean，底下就是调用的BeanFactory对应的方法
    public Object getBean(String beanName) throws BeansException {
        return this.beanFactory.getBean(beanName);
    }
    public void registerBeanDefinition(BeanDefinition beanDefinition) {
        this.beanFactory.registerBeanDefinition(beanDefinition);
    }
}

```

可以看到，当前的ClassPathXmlApplicationContext在实例化的过程中做了三件事。

1. 解析XML文件中的内容。
2. 加载解析的内容，构建BeanDefinition。
3. 读取BeanDefinition的配置信息，实例化Bean，然后把它注入到BeanFactory容器中。

通过上面几个步骤，我们把XML中的配置转换成Bean对象，并把它交由BeanFactory容器去管理，这些功能都实现了。虽然功能与原始版本相比没有发生任何变化，但这种 **一个类只做一件事的思想** 是值得我们在编写代码的过程中借鉴的。

## 小结

好了，这节课就讲到这里。通过简简单单几个类，我们就初步构建起了MiniSpring的核心部分：Bean和IoC。

![](assets/d1bf4d02a949ff0aac9e07fdafa92a83.jpg)

可以看到，通过这节课的构建，我们在业务程序中不需要再手动new一个业务类，只要把它交由框架容器去管理就可以获取我们所需的对象。另外还支持了Resource和BeanFactory，用Resource定义Bean的数据来源，让BeanFactory负责Bean的容器化管理。通过功能解耦，容器的结构会更加清晰明了，我们阅读起来也更加方便。当然最重要的是，这可以方便我们今后对容器进行扩展，适配更多的场景。

以前看似高深的Spring核心概念之一的IoC，就这样被我们拆解成了最简单的概念。它虽然原始，但已经具备了基本的功能，是一颗可以生长发育的种子。我们后面把其他功能一步步添加上去，这个可用的小种子就能发育成一棵大树。

完整源代码参见 [https://github.com/YaleGuo/minis](https://github.com/YaleGuo/minis)

## 课后题

学完这节课，我也给你留一道思考题。IoC的字面含义是“控制反转”，那么它究竟“反转”了什么？又是怎么体现在代码中的？欢迎你在留言区与我交流讨论，也欢迎你把这节课分享给需要的朋友。我们下节课见！

# 02｜扩展Bean：如何配置constructor、property和init-method？

你好，我是郭屹。

上节课，我们初步实现了一个MiniSpring框架，它很原始也很简单。我们实现了一个BeanFactory，作为一个容器对Bean进行管理，我们还定义了数据源接口Resource，可以将多种数据源注入Bean。

这节课，我们继续增强IoC容器，我们要做的主要有3点。

1. 增加单例Bean的接口定义，然后把所有的Bean默认为单例模式。
2. 预留事件监听的接口，方便后续进一步解耦代码逻辑。
3. 扩展BeanDefinition，添加一些属性，现在它只有id和class两个属性，我们要进一步地丰富它。

## 构建单例的Bean

首先我们来看看如何构建单例的Bean，并对该Bean进行管理。

单例（Singleton）是指某个类在整个系统内只有唯一的对象实例。只要能达到这个目的，采用什么技术手段都是可以的。常用的实现单例的方式有不下五种，因为我们构建单例的目的是深入理解Spring框架，所以我们会按照Spring的实现方式来做。

为了和Spring框架内的方法名保持一致，我们把BeanFactory接口中定义的registryBeanDefinition方法修改为registryBean，参数修改为beanName与obj。其中，obj为Object类，指代与beanName对应的Bean的信息。你可以看下修改后的BeanFactory。

```java
public interface BeanFactory {
    Object getBean(String beanName) throws BeansException;
    Boolean containsBean(String name);
    void registerBean(String beanName, Object obj);
}

```

既然要管理单例Bean，接下来我们就定义一下SingletonBeanRegistry，将管理单例Bean的方法规范好。

```java
public interface SingletonBeanRegistry {
    void registerSingleton(String beanName, Object singletonObject);
    Object getSingleton(String beanName);
    boolean containsSingleton(String beanName);
    String[] getSingletonNames();
}

```

你看这个类的名称上带有Registry字样，所以让人一眼就能知道这里面存储的就是Bean。从代码可以看到里面的方法 名称简单直接，分别对应单例的注册、获取、判断是否存在，以及获取所有的单例Bean等操作。

接口已经定义好了，接下来我们定义一个默认的实现类。这也是从Spring里学的方法，它作为一个框架并不会把代码写死，所以这里面的很多实现类都是默认的，默认是什么意思呢？就是我们可以去替换，不用这些默认的类也是可以的。我们就按照同样的方法，来为我们的默认实现类取个名字DefaultSingletonBeanRegistry。

```java
public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {
    //容器中存放所有bean的名称的列表
    protected List<String> beanNames = new ArrayList<>();
    //容器中存放所有bean实例的map
    protected Map<String, Object> singletons = new ConcurrentHashMap<>(256);

    public void registerSingleton(String beanName, Object singletonObject) {
        synchronized (this.singletons) {
            this.singletons.put(beanName, singletonObject);
            this.beanNames.add(beanName);
        }
    }
    public Object getSingleton(String beanName) {
        return this.singletons.get(beanName);
    }
    public boolean containsSingleton(String beanName) {
        return this.singletons.containsKey(beanName);
    }
    public String[] getSingletonNames() {
        return (String[]) this.beanNames.toArray();
    }
    protected void removeSingleton(String beanName) {
        synchronized (this.singletons) {
            this.beanNames.remove(beanName);
            this.singletons.remove(beanName);
        }
    }
}

```

我们在默认的这个类中，定义了beanNames列表和singletons的映射关系，beanNames用于存储所有单例Bean的别名，singletons则存储Bean名称和实现类的映射关系。

这段代码中要留意的是，我们将 singletons 定义为了一个ConcurrentHashMap，而且在实现 registrySingleton 时前面加了一个关键字synchronized。这一切都是为了确保在多线程并发的情况下，我们仍然能安全地实现对单例Bean的管理，无论是单线程还是多线程，我们整个系统里面这个Bean总是唯一的、单例的。

还记得我们有SimpleBeanFactory这样一个简单的BeanFactory实现类吗？接下来我们修改这个类，让它继承上一步创建的DefaultSingletonBeanRegistry，确保我们通过SimpleBeanFactory创建的Bean默认就是单例的，这也和Spring本身的处理方式一致。

```java
public class SimpleBeanFactory extends DefaultSingletonBeanRegistry implements BeanFactory{
    private Map<String, BeanDefinition> beanDefinitions = new ConcurrentHashMap<>(256);
    public SimpleBeanFactory() {
    }

    //getBean，容器的核心方法
    public Object getBean(String beanName) throws BeansException {
        //先尝试直接拿bean实例
        Object singleton = this.getSingleton(beanName);
        //如果此时还没有这个bean的实例，则获取它的定义来创建实例
        if (singleton == null) {
            //获取bean的定义
            BeanDefinition beanDefinition = beanDefinitions.get(beanName);
            if (beanDefinition == null) {
                throw new BeansException("No bean.");
            }
            try {
                singleton = Class.forName(beanDefinition.getClassName()).newInstance();
            }
            //新注册这个bean实例
            this.registerSingleton(beanName, singleton);
        }
        return singleton;
    }
    public void registerBeanDefinition(BeanDefinition beanDefinition) {
        this.beanDefinitions.put(beanDefinition.getId(), beanDefinition);
    }
    public Boolean containsBean(String name) {
        return containsSingleton(name);
    }
    public void registerBean(String beanName, Object obj) {
        this.registerSingleton(beanName, obj);
    }
}

```

我们对 SimpleBeanFactory 的主要改动是增加了对containsBean和registerBean的实现。通过代码可以看出，这两处实现都是对单例Bean的操作。

这部分还有两个类需要调整：ClassPathXmlApplicationContext和XmlBeanDefinitionReader。其中ClassPathXmlApplicationContext里增加了对containsBean和registerBean的实现。

```java
public Boolean containsBean(String name) {
    return this.beanFactory.containsBean(name);
}
public void registerBean(String beanName, Object obj) {
    this.beanFactory.registerBean(beanName, obj);
}

```

XmlBeanDefinitionReader调整后如下：

```java
public class XmlBeanDefinitionReader {
    SimpleBeanFactory simpleBeanFactory;
    public XmlBeanDefinitionReader(SimpleBeanFactory simpleBeanFactory) {
        this.simpleBeanFactory = simpleBeanFactory;
    }
    public void loadBeanDefinitions(Resource resource) {
        while (resource.hasNext()) {
            Element element = (Element) resource.next();
            String beanID = element.attributeValue("id");
            String beanClassName = element.attributeValue("class");
            BeanDefinition beanDefinition = new BeanDefinition(beanID, beanClassName);
            this.simpleBeanFactory.registerBeanDefinition(beanDefinition);
        }
    }
}

```

## 增加事件监听

构建好单例Bean之后，为了监控容器的启动状态，我们要增加事件监听。

我们先定义一下ApplicationEvent和ApplicationEventPublisher。通过名字可以看出，一个是用于监听应用的事件，另一个则是发布事件。

- ApplicationEventPublisher的实现

```java
public interface ApplicationEventPublisher {
    void publishEvent(ApplicationEvent event);
}

```

- ApplicationEvent的实现

```java
public class ApplicationEvent  extends EventObject {
    private static final long serialVersionUID = 1L;
    public ApplicationEvent(Object arg0) {
        super(arg0);
    }
}

```

可以看出，ApplicationEvent继承了Java工具包内的EventObject，我们是在Java的事件监听的基础上进行了简单的封装。虽然目前还没有任何实现，但这为我们后续使用观察者模式解耦代码提供了入口。

到此为止，我们进一步增强了IoC容器，还引入了两个新概念： **单例Bean和事件监听。** 其中，事件监听这部分目前只预留了入口，方便我们后续扩展。而单例Bean则是Spring框架默认的实现，我们提供了相关实现方法，并考虑到多线程高并发的场景，引入了ConcurrentHashMap来存储Bean信息。

到这一步，我们容器就变成了管理单例Bean的容器了。下面我们做一点准备工作，为后面对这些Bean注入属性值做铺垫。

## 注入

Spring中有三种属性注入的方式，分别是 **Field注入、Setter注入和构造器（Constructor）注入。** Field注入是指我们给Bean里面某个变量赋值。Setter注入是提供了一个setter方法，调用setXXX()来注入值。constructor就是在构造器/构造函数里传入参数来进行注入。Field注入我们后面会实现，这节课我们先探讨Setter注入和构造器注入两种方式。

### 配置Setter注入

首先我们来看下配置，在XML文件中我们是怎么声明使用Setter注入方式的。

```xml
<beans>
    <bean id="aservice" class="com.minis.test.AServiceImpl">
        <property type="String" name="property1" value="Hello World!"/>
    </bean>
</beans>

```

由上面的示例可以看出，我们在 `<bean>` 标签下引入了 `<property>` 标签，它又包含了type、name和value，分别对应属性类型、属性名称以及赋值。你可以看一下这个Bean的代码。

```java
public class AServiceImpl {
  private String property1;

  public void setProperty1(String property1) {
    this.property1 = property1;
  }
}

```

### 配置构造器注入

接下来我们再看看怎么声明构造器注入，同样是在XML里配置。

```xml
<beans>
    <bean id="aservice" class="com.minis.test.AServiceImpl">
      <constructor-arg type="String" name="name" value="abc"/>
      <constructor-arg type="int" name="level" value="3"/>
    </bean>
</beans>

```

可以看到，与Setter注入类似，我们只是把 `<property>` 标签换成了 `<constructor-args>` 标签。

```java
public class AServiceImpl {

  private String name;
  private int level;

  public AServiceImpl(String name, int level) {
    this.name = name;
    this.level = level;
  }
}

```

由上述两种方式可以看出， **注入操作的本质，就是给Bean的各个属性进行赋值。** 具体方式取决于实际情况，哪一种更便捷就可以选择哪一种。如果采用构造器注入的方式满足不了对域的赋值，也可以将构造器注入和Setter注入搭配使用。

```xml
<beans>
    <bean id="aservice" class="com.minis.test.AServiceImpl">
        <constructor-arg type="String" name="name" value="abc"/>
        <constructor-arg type="int" name="level" value="3"/>
        <property type="String" name="property1" value="Someone says"/>
        <property type="String" name="property2" value="Hello World!"/>
    </bean>
</beans>

```

现在我们已经明确了 `<property>` 和 `<constructor-args>` 标签的定义，但是只有外部的XML文件配置定义肯定是不行的，还要去实现。这就是我们接下来需要完成的工作。

## 实现属性类

与这个定义相关，我们要配置对应的属性类，分别命名为ArgumentValue和PropertyValue。

```java
public class ArgumentValue {
    private Object value;
    private String type;
    private String name;
    public ArgumentValue(Object value, String type) {
        this.value = value;
        this.type = type;
    }
    public ArgumentValue(Object value, String type, String name) {
        this.value = value;
        this.type = type;
        this.name = name;
    }
    //省略getter和setter
}

```

```java
public class PropertyValue {
    private final String name;
    private final Object value;
    public PropertyValue(String name, Object value) {
        this.name = name;
        this.value = value;
    }
    //省略getter
}

```

我们看Value这个词，后面不带“s”就表示他只是针对的某一个属性或者某一个参数，但一个Bean里面有很多属性、很多参数，所以我们就需要一个带“s”的集合类。 在Spring中也是这样的，所以我们参考Spring的方法，提供了ArgumentValues和PropertyValues两个类，封装、 增加、获取、判断等操作方法，简化调用。既给外面提供单个的参数/属性的对象，也提供集合对象。

- ArgumentValues类

```java
public class ArgumentValues {
    private final Map<Integer, ArgumentValue> indexedArgumentValues = new HashMap<>(0);
    private final List<ArgumentValue> genericArgumentValues = new LinkedList<>();
    public ArgumentValues() {
    }
    private void addArgumentValue(Integer key, ArgumentValue newValue) {
        this.indexedArgumentValues.put(key, newValue);
    }
    public boolean hasIndexedArgumentValue(int index) {
        return this.indexedArgumentValues.containsKey(index);
    }
    public ArgumentValue getIndexedArgumentValue(int index) {
        return this.indexedArgumentValues.get(index);
    }
    public void addGenericArgumentValue(Object value, String type) {
        this.genericArgumentValues.add(new ArgumentValue(value, type));
    }
    private void addGenericArgumentValue(ArgumentValue newValue) {
        if (newValue.getName() != null) {
            for (Iterator<ArgumentValue> it =
                 this.genericArgumentValues.iterator(); it.hasNext(); ) {
                ArgumentValue currentValue = it.next();
                if (newValue.getName().equals(currentValue.getName())) {
                    it.remove();
                }
            }
        }
        this.genericArgumentValues.add(newValue);
    }
    public ArgumentValue getGenericArgumentValue(String requiredName) {
        for (ArgumentValue valueHolder : this.genericArgumentValues) {
            if (valueHolder.getName() != null && (requiredName == null || !valueHolder.getName().equals(requiredName))) {
                continue;
            }
            return valueHolder;
        }
        return null;
    }
    public int getArgumentCount() {
        return this.genericArgumentValues.size();
    }
    public boolean isEmpty() {
        return this.genericArgumentValues.isEmpty();
    }
}

```

- PropertyValues类

```java
public class PropertyValues {
    private final List<PropertyValue> propertyValueList;
    public PropertyValues() {
        this.propertyValueList = new ArrayList<>(0);
    }
    public List<PropertyValue> getPropertyValueList() {
        return this.propertyValueList;
    }
    public int size() {
        return this.propertyValueList.size();
    }
    public void addPropertyValue(PropertyValue pv) {
        this.propertyValueList.add(pv);
    }
    public void addPropertyValue(String propertyName, Object propertyValue) {
        addPropertyValue(new PropertyValue(propertyName, propertyValue));
    }
    public void removePropertyValue(PropertyValue pv) {
        this.propertyValueList.remove(pv);
    }
    public void removePropertyValue(String propertyName) {
        this.propertyValueList.remove(getPropertyValue(propertyName));
    }
    public PropertyValue[] getPropertyValues() {
        return this.propertyValueList.toArray(new PropertyValue[this.propertyValueList.size()]);
    }
    public PropertyValue getPropertyValue(String propertyName) {
        for (PropertyValue pv : this.propertyValueList) {
            if (pv.getName().equals(propertyName)) {
                return pv;
            }
        }
        return null;
    }
    public Object get(String propertyName) {
        PropertyValue pv = getPropertyValue(propertyName);
        return pv != null ? pv.getValue() : null;
    }
    public boolean contains(String propertyName) {
        return getPropertyValue(propertyName) != null;
    }
    public boolean isEmpty() {
        return this.propertyValueList.isEmpty();
    }
}

```

上面这些代码整体还是比较简单的，根据各个封装方法的名称，也基本能明确它们的用途，这里就不再赘述了。对于构造器注入和Setter注入两种方式，这里我们只是初步定义相关类，做一点准备，后面我们将实现具体解析以及注入的过程。

接下来，我们还要做两件事。

1. 扩展BeanDefinition的属性，在原有id与name两个属性的基础上，新增lazyInit、dependsOn、initMethodName等属性。
2. 继续扩展BeanFactory接口，增强对Bean的处理能力。

## 扩展BeanDefinition

我们先给BeanDefinition和BeanFactory增加新的接口，新增接口基本上是适配BeanDefinition新增属性的。

我们给BeanDefinition类添加了哪些属性呢？一起来看下。

```java
public class BeanDefinition {
    String SCOPE_SINGLETON = "singleton";
    String SCOPE_PROTOTYPE = "prototype";
    private boolean lazyInit = false;
    private String[] dependsOn;
    private ArgumentValues constructorArgumentValues;
    private PropertyValues propertyValues;
    private String initMethodName;
    private volatile Object beanClass;
    private String id;
    private String className;
    private String scope = SCOPE_SINGLETON;
    public BeanDefinition(String id, String className) {
        this.id = id;
        this.className = className;
    }
    //省略getter和setter
}

```

从上面代码可以看出，之前我们只有id和className属性，现在增加了scope属性，表示bean是单例模式还是原型模式，还增加了lazyInit属性，表示Bean要不要在加载的时候初始化，以及初始化方法initMethodName的声明，当一个Bean构造好并实例化之后是否要让框架调用初始化方法。还有dependsOn属性记录Bean之间的依赖关系，最后还有构造器参数和property列表。

## 集中存放BeanDefinition

接下来，我们新增BeanDefinitionRegistry接口。它类似于一个存放BeanDefinition的仓库，可以存放、移除、获取及判断BeanDefinition对象。所以，我们初步定义四个接口对应这四个功能，分别是register、remove、get、contains。

```java
public interface BeanDefinitionRegistry {
    void registerBeanDefinition(String name, BeanDefinition bd);
    void removeBeanDefinition(String name);
    BeanDefinition getBeanDefinition(String name);
    boolean containsBeanDefinition(String name);
}

```

随后调整BeanFactory，新增Singleton、Prototype的判断，获取Bean的类型。

```java
public interface BeanFactory {
    Object getBean(String name) throws BeansException;
    boolean containsBean(String name);
    boolean isSingleton(String name);
    boolean isPrototype(String name);
    Class<?> getType(String name);
}

```

通过代码可以看到，我们让SimpleBeanFactory实现了BeanDefinitionRegistry，这样SimpleBeanFactory既是一个工厂同时也是一个仓库，你可以看下调整后的部分代码。

```java
public class SimpleBeanFactory extends DefaultSingletonBeanRegistry implements BeanFactory, BeanDefinitionRegistry{
    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);
    private List<String> beanDefinitionNames = new ArrayList<>();

    public void registerBeanDefinition(String name, BeanDefinition beanDefinition) {
        this.beanDefinitionMap.put(name, beanDefinition);
        this.beanDefinitionNames.add(name);
        if (!beanDefinition.isLazyInit()) {
            try {
                getBean(name);
            } catch (BeansException e) {
            }
        }
    }
    public void removeBeanDefinition(String name) {
        this.beanDefinitionMap.remove(name);
        this.beanDefinitionNames.remove(name);
        this.removeSingleton(name);
    }
    public BeanDefinition getBeanDefinition(String name) {
        return this.beanDefinitionMap.get(name);
    }
    public boolean containsBeanDefinition(String name) {
        return this.beanDefinitionMap.containsKey(name);
    }
    public boolean isSingleton(String name) {
        return this.beanDefinitionMap.get(name).isSingleton();
    }
    public boolean isPrototype(String name) {
        return this.beanDefinitionMap.get(name).isPrototype();
    }
    public Class<?> getType(String name) {
        return this.beanDefinitionMap.get(name).getClass();
    }
}

```

修改完BeanFactory这个核心之后，上层对应的 ClassPathXmlApplicationContext部分作为外部集成包装也需要修改。

```java
public class ClassPathXmlApplicationContext implements BeanFactory,
ApplicationEventPublisher{
    public void publishEvent(ApplicationEvent event) {
    }
    public boolean isSingleton(String name) {
        return false;
    }
    public boolean isPrototype(String name) {
        return false;
    }
    public Class<?> getType(String name) {
        return null;
    }
}

```

## 小结

![](assets/4868fb2cc4f11bd1e578c9c68430d58d.jpg)

这节课，我们模仿Spring构造了单例Bean，还增加了容器事件监听处理，完善了BeanDefinition的属性。此外，参照Spring的实现，我们增加了一些有用的特性，例如lazyInit，initMethodName等等，BeanFactory也做了相应的修改。同时，我们还提前为构造器注入、Setter注入提供了基本的实例类，这为后面实现上述两种依赖注入方式提供了基础。

通过对上一节课原始IoC容器的扩展和丰富，它已经越来越像Spring框架了。

完整源代码参见 [https://github.com/YaleGuo/minis](https://github.com/YaleGuo/minis)

## 课后题

学完这节课，我也给你留一道思考题。你认为构造器注入和Setter注入有什么异同？它们各自的优缺点是什么？欢迎你在留言区与我交流讨论，也欢迎你把这节课分享给需要的朋友。我们下节课见！

# 03｜依赖注入：如何给Bean注入值并解决循环依赖问题？

你好，我是郭屹，今天我们继续手写MiniSpring，探讨Bean的依赖注入。

上节课，我们定义了在XML配置文件中使用setter注入和构造器注入的配置方式，但同时也留下了一个悬念：这些配置是如何生效的呢？

## 值的注入

要理清这个问题，我们要先来看看 **Spring是如何解析 `<property>` 和 `<constructor-arg>` 标签。**

我们以下面的XML配置为基准进行学习。

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<beans>
    <bean id="aservice" class="com.minis.test.AServiceImpl">
        <constructor-arg type="String" name="name" value="abc"/>
        <constructor-arg type="int" name="level" value="3"/>
        <property type="String" name="property1" value="Someone says"/>
        <property type="String" name="property2" value="Hello World!"/>
    </bean>
</beans>

```

和上面的配置属性对应，在测试类AServiceImpl中，要有相应的name、level、property1、property2字段来建立映射关系，这些实现体现在构造函数以及settter、getter等方法中。

```java
public class AServiceImpl implements AService {
    private String name;
    private int level;
    private String property1;
    private String property2;

    public AServiceImpl() {
    }
    public AServiceImpl(String name, int level) {
        this.name = name;
        this.level = level;
        System.out.println(this.name + "," + this.level);
    }
    public void sayHello() {
        System.out.println(this.property1 + "," + this.property2);
    }
    // 在此省略property1和property2的setter、getter方法
}

```

接着，简化ArgumentValues类，移除暂时未用到的方法。

```java
public class ArgumentValues {
    private final List<ArgumentValue> argumentValueList = new ArrayList<>();
    public ArgumentValues() {
    }
    public void addArgumentValue(ArgumentValue argumentValue) {
        this.argumentValueList.add(argumentValue);
    }
    public ArgumentValue getIndexedArgumentValue(int index) {
        ArgumentValue argumentValue = this.argumentValueList.get(index);
        return argumentValue;
    }
    public int getArgumentCount() {
        return (this.argumentValueList.size());
    }
    public boolean isEmpty() {
        return (this.argumentValueList.isEmpty());
    }
}

```

做完准备工作之后，我们重点来看核心工作：解析 `<property>` 和 `<constructor-arg>` 两个标签。我们要在XmlBeanDefinitionReader类中处理这两个标签。

```java
 public void loadBeanDefinitions(Resource resource) {
        while (resource.hasNext()) {
            Element element = (Element) resource.next();
            String beanID = element.attributeValue("id");
            String beanClassName = element.attributeValue("class");
            BeanDefinition beanDefinition = new BeanDefinition(beanID,
beanClassName);
            //处理属性
            List<Element> propertyElements = element.elements("property");
            PropertyValues PVS = new PropertyValues();
            for (Element e : propertyElements) {
                String pType = e.attributeValue("type");
                String pName = e.attributeValue("name");
                String pValue = e.attributeValue("value");
                PVS.addPropertyValue(new PropertyValue(pType, pName, pValue));
            }
            beanDefinition.setPropertyValues(PVS);

            //处理构造器参数
            List<Element> constructorElements = element.elements("constructor-
arg");
            ArgumentValues AVS = new ArgumentValues();
            for (Element e : constructorElements) {
                String aType = e.attributeValue("type");
                String aName = e.attributeValue("name");
                String aValue = e.attributeValue("value");
                AVS.addArgumentValue(new ArgumentValue(aType, aName, aValue));
            }
            beanDefinition.setConstructorArgumentValues(AVS);

            this.simpleBeanFactory.registerBeanDefinition(beanID,
beanDefinition);
        }
    }
}

```

从上述代码可以看出，程序在加载Bean的定义时要获取 `<property>` 和 `<constructor-arg>`，只要循环处理它们对应标签的属性：type、name、value即可。随后，我们通过addPropertyValue和addArgumentValue两个方法就能将注入的配置读取进内存。

那么，将这些配置的值读取进内存之后，我们怎么把它作为Bean的属性注入进去呢？这要求我们在创建Bean的时候就要做相应的处理，给属性赋值。针对XML配置的Value值，我们要按照数据类型分别将它们解析为字符串、整型、浮点型等基本类型。在SimpleBeanFactory类中，调整核心的createBean方法，我们修改一下。

```java
    private Object createBean(BeanDefinition beanDefinition) {
        Class<?> clz = null;
        Object obj = null;
        Constructor<?> con = null;
        try {
            clz = Class.forName(beanDefinition.getClassName());
            // 处理构造器参数
            ArgumentValues argumentValues =
beanDefinition.getConstructorArgumentValues();
            //如果有参数
            if (!argumentValues.isEmpty()) {
                Class<?>[] paramTypes = new Class<?>
[argumentValues.getArgumentCount()];
                Object[] paramValues = new
Object[argumentValues.getArgumentCount()];
                //对每一个参数，分数据类型分别处理
                for (int i = 0; i < argumentValues.getArgumentCount(); i++) {
                    ArgumentValue argumentValue =
argumentValues.getIndexedArgumentValue(i);
                    if ("String".equals(argumentValue.getType()) ||
"java.lang.String".equals(argumentValue.getType())) {
                        paramTypes[i] = String.class;
                        paramValues[i] = argumentValue.getValue();
                    } else if ("Integer".equals(argumentValue.getType()) ||
"java.lang.Integer".equals(argumentValue.getType())) {
                        paramTypes[i] = Integer.class;
                        paramValues[i] =
Integer.valueOf((String)argumentValue.getValue());
                    } else if ("int".equals(argumentValue.getType())) {
                        paramTypes[i] = int.class;
                        paramValues[i] = Integer.valueOf((String)
argumentValue.getValue());
                    } else { //默认为string
                        paramTypes[i] = String.class;
                        paramValues[i] = argumentValue.getValue();
                    }
                }
                try {
                    //按照特定构造器创建实例
                    con = clz.getConstructor(paramTypes);
                    obj = con.newInstance(paramValues);
                }
            } else { //如果没有参数，直接创建实例
                obj = clz.newInstance();
            }
        } catch (Exception e) {
        }
        // 处理属性
        PropertyValues propertyValues = beanDefinition.getPropertyValues();
        if (!propertyValues.isEmpty()) {
            for (int i = 0; i < propertyValues.size(); i++) {
                //对每一个属性，分数据类型分别处理
                PropertyValue propertyValue =
propertyValues.getPropertyValueList().get(i);
                String pType = propertyValue.getType();
                String pName = propertyValue.getName();
                Object pValue = propertyValue.getValue();
                Class<?>[] paramTypes = new Class<?>[1];
               if ("String".equals(pType) || "java.lang.String".equals(pType))
{
                    paramTypes[0] = String.class;
                } else if ("Integer".equals(pType) ||
"java.lang.Integer".equals(pType)) {
                    paramTypes[0] = Integer.class;
                } else if ("int".equals(pType)) {
                    paramTypes[0] = int.class;
                } else { // 默认为string
                    paramTypes[0] = String.class;
                }
                Object[] paramValues = new Object[1];
                paramValues[0] = pValue;

                //按照setXxxx规范查找setter方法，调用setter方法设置属性
                String methodName = "set" + pName.substring(0, 1).toUpperCase()
+ pName.substring(1);
                Method method = null;
                try {
                    method = clz.getMethod(methodName, paramTypes);
                }
                try {
                    method.invoke(obj, paramValues);
                }
            }
        }
        return obj;
    }
}

```

我们这里的代码主要可以分成两个部分：一部分是处理constructor的里面的参数，另外一部分是处理各个property的属性。现在程序的代码是写在一起的，后面我们还会抽出单独的方法。

### 如何处理constructor？

首先，获取XML配置中的属性值，这个时候它们都是通用的Object类型，我们需要根据type字段的定义判断不同Value所属的类型，作为一个原始的实现这里我们只提供了String、Integer 和 int三种类型的判断。最终通过反射构造对象，将配置的属性值注入到了Bean对象中，实现构造器注入。

### 如何处理property？

和处理constructor相同，我们依然要通过type字段确定Value的归属类型。但不同之处在于，判断好归属类型后，我们还要手动构造setter方法，通过反射将属性值注入到setter方法之中。通过这种方式来实现对属性的赋值。

可以看出，其实代码的核心是通过Java的反射机制调用构造器及setter方法，在调用过程中根据具体的类型把属性值作为一个参数赋值进去。这也是所有的框架在实现IoC时的思路。 **反射技术是IoC容器赖以工作的基础。**

到这里，我们就完成了对XML配置的解析，实现了Spring中Bean的构造器注入与setter注入方式。回到我们开头的问题：配置文件中的属性设置是如何生效的？到这里我们就有答案了，就是 **通过反射给Bean里面的属性赋值，就意味着配置文件生效了。**

这里，我还想带你理清一个小的概念问题。在实现过程中，我们经常会用到依赖注入和IoC这两个术语，初学者很容易被这两个术语弄糊涂。其实，一开始只有IoC，也就是控制反转，但是这个术语让人很难快速理解，我们不知道反转了什么东西。但是通过之前的实现过程，我们就可以理解这个词了。

![图片](assets/d508800320aa0f8688b7c986e0148e4b.png)

一个“正常”的控制过程是由调用者直接创建Bean，但是IoC的过程正好相反，是由框架来创建Bean，然后注入给调用者，这与“正常”的过程是反的，控制反转就是这个意思。但是总的来说，这个术语还是过于隐晦，引发了很长一段时间的争议，直到传奇程序员Martin Fowler一锤定音，将其更名为“依赖注入”，一切才尘埃落定，“依赖注入”从此成为大家最常使用的术语。

## Bean之间的依赖问题

现在我们进一步考虑一个问题。在注入属性值的时候，如果这个属性本身是一个对象怎么办呢？这就是Bean之间的依赖问题了。

这个场景在我们进行代码开发时还是非常常见的。比如，操作MySQL数据库的时候，经常需要引入Mapper类，而Mapper类本质上也是在IoC容器在启动时加载的一个Bean对象。

或许有人会说，我们就按照前面的配置方式，在type里配置需要配置Bean的绝对包路径，name里对应Bean的属性，不就好了吗？但这样还是会存在一个问题， **如何用Value这样一个简单的值表示某个对象中所有的域呢？**

为此，Spring做了一个很巧妙的事情，它在标签里增加了 **ref属性（引用）**，这个属性就记录了需要引用的另外一个Bean，这就方便多了。你可以参考下面的配置文件。

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<beans>
    <bean id="basebaseservice" class="com.minis.test.BaseBaseService">
        <property type="com.minis.test.AServiceImpl" name="as" ref="aservice" />
    </bean>
    <bean id="aservice" class="com.minis.test.AServiceImpl">
        <constructor-arg type="String" name="name" value="abc"/>
        <constructor-arg type="int" name="level" value="3"/>
        <property type="String" name="property1" value="Someone says"/>
        <property type="String" name="property2" value="Hello World!"/>
        <property type="com.minis.test.BaseService" name="ref1" ref="baseservice"/>
    </bean>
    <bean id="baseservice" class="com.minis.test.BaseService">
        <property type="com.minis.test.BaseBaseService" name="bbs" ref="basebaseservice" />
    </bean>

```

在上面的XML配置文件中，我们配置了一个Bean，ID命名为baseservice，随后在aservice bean的标签中设置ref=“baseservice”，也就是说我们希望此处注入的是一个Bean而不是一个简单的值。所以在对应的AServiceImpl里，也得有类型为BaseService的域ref1。

```java
public class AServiceImpl implements AService {
    private String name;
    private int level;
    private String property1;
    private String property2;
    private BaseService ref1;

    public AServiceImpl() {
    }
    public AServiceImpl(String name, int level) {
        this.name = name;
        this.level = level;
        System.out.println(this.name + "," + this.level);
    }
    public void sayHello() {
        System.out.println(this.property1 + "," + this.property2);
    }

    // 在此省略property1和property2的setter、getter方法
}

```

既然添加了ref属性，接下来我们很自然地会想到，要解析这个属性。下面我们就来解析一下ref，看看Spring是如何将配置的Bean注入到另外一个Bean中的。

我们为PropertyValue.java程序增加isRef字段，它可以判断属性是引用类型还是普通的值类型，我们看下修改后的代码。

```java
public class PropertyValue {
    private final String type;
    private final String name;
    private final Object value;
    private final boolean isRef;
    public PropertyValue(String type, String name, Object value, boolean isRef)
{
        this.type = type;
        this.name = name;
        this.value = value;
        this.isRef = isRef;
}

```

在这里我们调整了PropertyValue的构造函数，增加了isRef参数。

接下来我们看看如何解析ref属性，我们还是在XmlBeanDefinitionReader类中来处理。

```java
 public void loadBeanDefinitions(Resource resource) {
        while (resource.hasNext()) {
            Element element = (Element) resource.next();
            String beanID = element.attributeValue("id");
            String beanClassName = element.attributeValue("class");
            BeanDefinition beanDefinition = new BeanDefinition(beanID,
beanClassName);
            // handle constructor
            List<Element> constructorElements = element.elements("constructor-
arg");
            ArgumentValues AVS = new ArgumentValues();
            for (Element e : constructorElements) {
                String aType = e.attributeValue("type");
                String aName = e.attributeValue("name");
                String aValue = e.attributeValue("value");
                AVS.addArgumentValue(new ArgumentValue(aType, aName, aValue));
            }
            beanDefinition.setConstructorArgumentValues(AVS);

            // handle properties
            List<Element> propertyElements = element.elements("property");
            PropertyValues PVS = new PropertyValues();
            List<String> refs = new ArrayList<>();
            for (Element e : propertyElements) {
                String pType = e.attributeValue("type");
                String pName = e.attributeValue("name");
                String pValue = e.attributeValue("value");
                String pRef = e.attributeValue("ref");
                String pV = "";
                boolean isRef = false;
                if (pValue != null && !pValue.equals("")) {
                    isRef = false;
                    pV = pValue;
                } else if (pRef != null && !pRef.equals("")) {
                    isRef = true;
                    pV = pRef;
                    refs.add(pRef);
                }
                PVS.addPropertyValue(new PropertyValue(pType, pName, pV,
isRef));
            }
            beanDefinition.setPropertyValues(PVS);

            String[] refArray = refs.toArray(new String[0]);
            beanDefinition.setDependsOn(refArray);
            this.simpleBeanFactory.registerBeanDefinition(beanID,
beanDefinition);
        }
   }

```

由上述代码可以看出，程序解析 `<property>` 标签后，获取了ref的参数，同时有针对性地设置了isRef的值，把它添加到了PropertyValues内，最后程序调用setDependsOn方法，它记录了某一个Bean引用的其他Bean。这样，我们引用ref的配置就定义好了。

然后，我们改造一下以前的createBean()方法，抽取出一个单独处理属性的方法。

```java
	private Object createBean(BeanDefinition bd) {
		... ...
		handleProperties(bd, clz, obj);
		return obj;
	}

	private void handleProperties(BeanDefinition bd, Class<?> clz, Object obj) {
        // 处理属性
		System.out.println("handle properties for bean : " + bd.getId());
		PropertyValues propertyValues = bd.getPropertyValues();
        //如果有属性
		if (!propertyValues.isEmpty()) {
			for (int i=0; i<propertyValues.size(); i++) {
				PropertyValue propertyValue = propertyValues.getPropertyValueList().get(i);
				String pName = propertyValue.getName();
				String pType = propertyValue.getType();
    			Object pValue = propertyValue.getValue();
    			boolean isRef = propertyValue.getIsRef();
    			Class<?>[] paramTypes = new Class<?>[1];
				Object[] paramValues =   new Object[1];
    			if (!isRef) { //如果不是ref，只是普通属性
                    //对每一个属性，分数据类型分别处理
					if ("String".equals(pType) || "java.lang.String".equals(pType)) {
						paramTypes[0] = String.class;
					}
					else if ("Integer".equals(pType) || "java.lang.Integer".equals(pType)) {
						paramTypes[0] = Integer.class;
					}
					else if ("int".equals(pType)) {
						paramTypes[0] = int.class;
					}
					else {
						paramTypes[0] = String.class;
					}

					paramValues[0] = pValue;
    			}
    			else { //is ref, create the dependent beans
    				try {
						paramTypes[0] = Class.forName(pType);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
    				try {
                        //再次调用getBean创建ref的bean实例
						paramValues[0] = getBean((String)pValue);
					}
    			}

                //按照setXxxx规范查找setter方法，调用setter方法设置属性
    			String methodName = "set" + pName.substring(0,1).toUpperCase() + pName.substring(1);
    			Method method = null;
				try {
					method = clz.getMethod(methodName, paramTypes);
				}
    			try {
					method.invoke(obj, paramValues);
				}
			}
		}
	}

```

这里的重点是处理ref的这几行代码。

```plain
//is ref, create the dependent beans
paramTypes[0] = Class.forName(pType);
paramValues[0] = getBean((String)pValue);

```

这段代码实现的思路就是，对ref所指向的另一个Bean再次调用getBean()方法，这个方法会获取到另一个Bean实例，这样就实现了另一个Bean的注入。

这样一来，如果有多级引用，就会形成一个多级的getBean()调用链。由于在调用getBean()的时候会判断容器中是否包含了bean instance，没有的话会立即创建，所以XML配置文件中声明Bean的先后次序是任意的。

## 循环依赖问题

这又引出了另一个问题，在某个Bean需要注入另一个Bean的时候，如果那个Bean还不存在，该怎么办？

请你想象一个场景，Spring扫描到了ABean，在解析它并设置内部属性时，发现某个属性是另一个BBean，而此时Spring内部还不存在BBean的实例。这就要求Spring在创建ABean的过程中，能够再去创建一个BBean，继续推衍下去，BBean可能又会依赖第三个CBean。事情还可能进一步复杂化，如果CBean又反过来依赖ABean，就会形成循环依赖。

在逻辑上，我们好像陷入了一个死结，我们必须想办法打破这个循环。我们来看看Spring是如何解决这个问题的。

请你回顾一下创建Bean的过程。我们根据Bean的定义配置生成了BeanDefinition，然后根据定义加载Bean类，再进行实例化，最后在Bean中注入属性。

从这个过程中可以看出，在注入属性之前，其实这个Bean的实例已经生成出来了，只不过此时的实例还不是一个完整的实例，它还有很多属性没有值，可以说是一个早期的毛胚实例。而我们现在讨论的Bean之间的依赖是在属性注入这一阶段，因此我们可以在实例化与属性注入这两个阶段之间增加一个环节，确保给Bean注入属性的时候，Spring内部已经准备好了Bean的实例。

Spring的做法是在BeanFactory中引入一个结构： **earlySingletonObjects**，这里面存放的就是早期的毛胚实例。创建Bean实例的时候，不用等到所有步骤完成，而是可以在属性还没有注入之前，就把早期的毛胚实例先保存起来，供属性注入时使用。

这时再回到我们的复杂依赖场景，ABean依赖BBean，BBean又依赖CBean，而CBean反过来还要依赖ABean。现在，我们可以这样实现依赖注入。

![图片](assets/f4a1a6b8973eae18d9edb54cd8277bee.png)

第一步，先实例化ABean，此时它是早期的不完整毛胚实例，好多属性还没被赋值，将实例放置到earlySingletonObjects中备用。然后给ABean注入属性，这个时候发现它还要依赖BBean。

第二步，实例化BBean，它也是早期的不完整毛胚实例，我们也将实例放到earlySingletonObjects中备用。然后再给BBean注入属性，又发现它依赖CBean。

第三步，实例化CBean，此时它仍然是早期的不完整的实例，同样将实例放置到earlySingletonObjects中备用，然后再给CBean属性赋值，这个时候又发现它反过来还要依赖ABean。

第四步，我们从earlySingletonObjects结构中找到ABean的早期毛胚实例，取出来给CBean注入属性，这意味着这时CBean所用的ABean实例是那个早期的毛胚实例。这样就先创建好了CBean。

第五步，程序控制流回到第二步，完成BBean的属性注入。

第六步，程序控制流回到第一步，完成ABean的属性注入。至此，所有的Bean就都创建完了。

通过上述过程可以知道，这一系列的Bean是纠缠在一起创建的，我们不能简单地先后独立创建它们，而是要作为一个整体来创建。

相应的程序代码，反映在getBean(), createBean() 和 doCreateBean()中。

```java
@Override
public Object getBean(String beanName) throws BeansException {
    //先尝试直接从容器中获取bean实例
    Object singleton = this.getSingleton(beanName);
    if (singleton == null) {
        //如果没有实例，则尝试从毛胚实例中获取
        singleton = this.earlySingletonObjects.get(beanName);
        if (singleton == null) {
            //如果连毛胚都没有，则创建bean实例并注册
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            singleton = createBean(beanDefinition);
            this.registerSingleton(beanName, singleton);
            // 预留beanpostprocessor位置
            // step 1: postProcessBeforeInitialization
            // step 2: afterPropertiesSet
            // step 3: init-method
            // step 4: postProcessAfterInitialization
        }
    }
    return singleton;
  }

private Object createBean(BeanDefinition beanDefinition) {
    Class<?> clz = null;
    //创建毛胚bean实例
    Object obj = doCreateBean(beanDefinition);
    //存放到毛胚实例缓存中
    this.earlySingletonObjects.put(beanDefinition.getId(), obj);
    try {
        clz = Class.forName(beanDefinition.getClassName());
    }
    //处理属性
    handleProperties(beanDefinition, clz, obj);
    return obj;
}

//doCreateBean创建毛胚实例，仅仅调用构造方法，没有进行属性处理
private Object doCreateBean(BeanDefinition bd) {
		Class<?> clz = null;
		Object obj = null;
		Constructor<?> con = null;

		try {
    		clz = Class.forName(bd.getClassName());

    		//handle constructor
    		ArgumentValues argumentValues = bd.getConstructorArgumentValues();
    		if (!argumentValues.isEmpty()) {
        		Class<?>[] paramTypes = new Class<?>[argumentValues.getArgumentCount()];
        		Object[] paramValues =   new Object[argumentValues.getArgumentCount()];
    			for (int i=0; i<argumentValues.getArgumentCount(); i++) {
    				ArgumentValue argumentValue = argumentValues.getIndexedArgumentValue(i);
    				if ("String".equals(argumentValue.getType()) || "java.lang.String".equals(argumentValue.getType())) {
    					paramTypes[i] = String.class;
        				paramValues[i] = argumentValue.getValue();
    				}
    				else if ("Integer".equals(argumentValue.getType()) || "java.lang.Integer".equals(argumentValue.getType())) {
    					paramTypes[i] = Integer.class;
        				paramValues[i] = Integer.valueOf((String) argumentValue.getValue());
    				}
    				else if ("int".equals(argumentValue.getType())) {
    					paramTypes[i] = int.class;
        				paramValues[i] = Integer.valueOf((String) argumentValue.getValue()).intValue();
    				}
    				else {
    					paramTypes[i] = String.class;
        				paramValues[i] = argumentValue.getValue();
    				}
    			}
				try {
					con = clz.getConstructor(paramTypes);
					obj = con.newInstance(paramValues);
				}
    		}
    		else {
    			obj = clz.newInstance();
    		}
		}

		System.out.println(bd.getId() + " bean created. " + bd.getClassName() + " : " + obj.toString());
		return obj;

}

```

createBean()方法中调用了一个 **doCreateBean(bd)方法**，专门负责创建早期的毛胚实例。毛胚实例创建好后会放在earlySingletonObjects结构中，然后createBean()方法再调用handleProperties()补齐这些property的值。

在getBean()方法中，首先要判断有没有已经创建好的bean，有的话直接取出来，如果没有就检查earlySingletonObjects中有没有相应的毛胚Bean，有的话直接取出来，没有的话就去创建，并且会根据Bean之间的依赖关系把相关的Bean全部创建好。

很多资料把这个过程叫做bean的“三级缓存”，这个术语来自于Spring源代码中的程序注释。实际上我们弄清楚了这个getBean()的过程后就会知道这段注释并不是很恰当。只不过这是Spring发明人自己写下的注释，大家也都这么称呼而已。

## 包装方法refresh()

可以看出，在Spring体系中，Bean是结合在一起同时创建完毕的。为了减少它内部的复杂性，Spring对外提供了一个很重要的包装方法： **refresh()**。具体的包装方法也很简单，就是对所有的Bean调用了一次getBean()，利用getBean()方法中的createBean()创建Bean实例，就可以只用一个方法把容器中所有的Bean的实例创建出来了。

我们先在SimpleBeanFactory中实现一个最简化的refresh()方法。

```java
public void refresh() {
    for (String beanName : beanDefinitionNames) {
        try {
            getBean(beanName);
        }
    }
}

```

然后我们改造ClassPathXmlApplicationContext，配合我们上一步增加的refresh()方法使用，你可以看下相应的代码。

```java
public class ClassPathXmlApplicationContext implements BeanFactory, ApplicationEventPublisher{

  SimpleBeanFactory beanFactory;
  public ClassPathXmlApplicationContext(String fileName) {
      this(fileName, true);
  }
  public ClassPathXmlApplicationContext(String fileName, boolean isRefresh) {
      Resource resource = new ClassPathXmlResource(fileName);
      SimpleBeanFactory simpleBeanFactory = new SimpleBeanFactory();
      XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(simpleBeanFactory);
      reader.loadBeanDefinitions(resource);
      this.beanFactory = simpleBeanFactory;
      if (isRefresh) {
          this.beanFactory.refresh();
      }
  }
  // 省略方法实现
 }

```

到这里，我们的ClassPAthXmlApplicationContext用一个refresh() 就将整个IoC容器激活了，运行起来，加载所有配置好的Bean。

你可以试着构建一下的测试代码。

```java
public class BaseBaseService {
    private AServiceImpl as;
    // 省略 getter、setter方法
}

```

```java
public class BaseService {
    private BaseBaseService bbs;
    // 省略 getter、setter方法
}

```

```java
public class AServiceImpl implements AService {
    private String name;
    private int level;
    private String property1;
    private String property2;
    private BaseService ref1;
    // 省略 getter、setter方法
}

```

相应的XML配置如下：

```java
<?xml version="1.0" encoding="UTF-8" ?>
<beans>
    <bean id="aservice" class="com.minis.test.AServiceImpl">
        <constructor-arg type="String" name="name" value="abc"/>
        <constructor-arg type="int" name="level" value="3"/>
        <property type="String" name="property1" value="Someone says"/>
        <property type="String" name="property2" value="Hello World!"/>
        <property type="com.minis.test.BaseService" name="ref1"
ref="baseservice"/>
    </bean>
    <bean id="basebaseservice" class="com.minis.test.BaseBaseService">
        <property type="com.minis.test.AServiceImpl" name="as" ref="aservice" />
    </bean>
    <bean id="baseservice" class="com.minis.test.BaseService">
        <property type="com.minis.test.BaseBaseService" name="bbs"
ref="basebaseservice" />
    </bean>

```

然后运行测试程序，可以看到我们自己的IoC容器运行起来了。

## 小结

这节课，我们紧接着上一节课对XML配置的解析，实现了Spring中Bean的构造器注入与setter注入两种方式。

在将属性注入Bean的过程中，我们还增加了ref属性，它可以在一个Bean对象中引入另外的Bean对象。我们还通过引入“毛胚Bean”的概念解决了循环依赖的问题。

我们还为容器增加了refresh()方法，这个方法包装了容器启动的各个步骤，从Bean工厂的创建到Bean对象的实例化和初始化，再到完成Spring容器加载，一切Bean的处理都能在这里完成，可以说是Spring中的核心方法了。

完整源代码参见 [https://github.com/YaleGuo/minis](https://github.com/YaleGuo/minis)

## 课后题

学完这节课内容，我也给你留一道思考题。你认为能不能在一个Bean的构造器中注入另一个Bean？欢迎你在留言区与我交流讨论，也欢迎你把这节课分享给需要的朋友。我们下节课见！

# 04｜增强IoC容器：如何让我们的Spring支持注解？

你好，我是郭屹。

上节课我们通过一系列的操作使XML使配置文件生效，然后实现了Spring中Bean的构造器注入与setter注入，通过引入“早期毛胚Bean”的概念解决了循环依赖的问题，我们还为容器增加了Spring中的一个核心方法refresh()，作为整个容器启动的入口。现在我们的容器已经初具模型了，那如何让它变得更强大，从种子长成一株幼苗呢？

这节课我们就来实现一个增强版的IoC容器，支持通过注解的方式进行依赖注入。注解是我们在编程中常用的技术，可以减少配置文件的内容，便于管理的同时还能提高开发效率。所以这节课我们将 **实现Autowired注解，并用这个方式进行依赖注入**。

## 目录结构

我们手写MiniSpring的目的是更好地学习Spring。因此，我们会时不时回头来整理整个项目的目录结构，和Spring保持一致。

现在我们先参考Spring框架的结构，来调整我们的项目结构，在beans目录下新增factory目录，factory目录中则新增xml、support、config与annotation四个目录。

```java
├── beans
│   └── factory
│       ├── xml
│       └── support
│       └── config
│       └── annotation

```

接下来将之前所写的类文件移动至新增目录下，你可以看一下移动后的结构。

```java
factory —— BeanFactory.java
factory.xml —— XmlBeanDefinitionReader.java
factory.support —— DefaultSingletonBeanRegistry.java、
BeanDefinitionRegistry.java、SimpleBeanFactory.java
factory.config —— SingletonBeanRegistry.java、ConstructorArgumentValues.java、
ConstructorArgumentValue.java、BeanDefinition.java

// 注：
// ConstructorArgumentValues由ArgumentValues改名而来
// ConstructorArgumentValue由ArgumentValue改名而来

```

熟悉了这个项目结构后，你再回头去看Spring框架的结构，会发现它们是一样的，不光目录一样，文件名也是一样的，类中的主要方法名和属性名也是一样的。我这么做的目的是便于你之后自己继续学习。

## 注解支持

如果你用过Spring的话，对Autowired注解想必不陌生，这也是常用的依赖注入的方式，在需要注入的对象上增加@Autowired注解就可以了，你可以参考下面这个例子。

```java
public class Test {
  @Autowired
  private TestAutowired testAutowired;
}

```

这种方式的好处在于，不再需要显式地在XML配置文件中使用ref属性，指定需要依赖的对象，直接在代码中加上这个注解，就能起到同样的依赖注入效果。但是你要知道，计算机运行程序是机械式的，并没有魔法，加的这一行注解不会自我解释，必须有另一个程序去解释它，否则注解就变成了注释。

那么，问题就来了， **我们要在哪一段程序、哪个时机去解释这个注解呢？**

简单分析一下，这个注解是作用在一个实例变量上的，为了生效，我们首先必须创建好这个对象，也就是在createBean时机之后。

回顾前面几节课的内容，我们通过一个refresh()方法包装了整个Bean的创建过程，我们能看到在创建Bean实例之后，要进行初始化工作，refresh()方法内预留了postProcessBeforeInitialization、init-method与postProcessAfterInitialization的位置，根据它们的名称也能看出是在初始化前、中、后分别对Bean进行处理。这里就是很好的时机。

接下来我们一起看看这些功能是如何实现的。

在这个预留的位置，我们可以考虑调用一个Bean处理器Processor，由处理器来解释注解。我们首先来定义BeanPostProcessor，它内部的两个方法分别用于Bean初始化之前和之后。

1. Bean初始化之前

```java
public interface BeanPostProcessor {
    Object postProcessBeforeInitialization(Object bean, String beanName) throws
BeansException;
}

```

1. Bean初始化之后

```java
public interface BeanPostProcessor {
    Object postProcessAfterInitialization(Object bean, String beanName) throws
BeansException;
}

```

接下来我们定义Autowired注解，很简单，你可以参考一下。

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired {
}

```

根据这个定义可以知道，Autowired修饰成员变量（属性），并且在运行时生效。

为了实现@Autowired这个注解，我们很自然地会想到，利用反射获取所有标注了Autowired注解的成员变量，把它初始化成一个Bean，然后注入属性。结合前面我们定义的BeanPostProcessor接口，我们来定义Autowired的处理类AutowiredAnnotationBeanPostProcessor。

```java
public class AutowiredAnnotationBeanPostProcessor implements BeanPostProcessor {
    private AutowireCapableBeanFactory beanFactory;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
throws BeansException {
        Object result = bean;

        Class<?> clazz = bean.getClass();
        Field[] fields = clazz.getDeclaredFields();
        if(fields!=null){
            //对每一个属性进行判断，如果带有@Autowired注解则进行处理
            for(Field field : fields){
                boolean isAutowired =
field.isAnnotationPresent(Autowired.class);
                if(isAutowired){
                    //根据属性名查找同名的bean
                    String fieldName = field.getName();
                    Object autowiredObj =
this.getBeanFactory().getBean(fieldName);
                    //设置属性值，完成注入
                    try {
                        field.setAccessible(true);
                        field.set(bean, autowiredObj);
                        System.out.println("autowire " + fieldName + " for bean
" + beanName);
                    }
                }
            }
        }
        return result;
    }
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
throws BeansException {
        return null;
    }
    public AutowireCapableBeanFactory getBeanFactory() {
        return beanFactory;
    }
    public void setBeanFactory(AutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
}

```

其实，核心代码就只有几行。

```java
boolean isAutowired = field.isAnnotationPresent(Autowired.class);
if(isAutowired){
    String fieldName = field.getName();
    Object autowiredObj =  this.getBeanFactory().getBean(fieldName);
    field.setAccessible(true);
    field.set(bean, autowiredObj);

```

判断类里面的每一个属性是不是带有Autowired注解，如果有，就根据属性名获取Bean。从这里我们可以看出，属性名字很关键，我们就是靠它来获取和创建的Bean。有了Bean之后，我们通过反射设置属性值，完成依赖注入。

## 新的BeanFactory

在这里我们引入了AutowireCapableBeanFactory，这个BeanFactory就是专为Autowired注入的Bean准备的。

在此之前我们已经定义了BeanFactory接口，以及一个SimpleBeanFactory的实现类。现在我们又需要引入另外一个BeanFactory—— **AutowireCapableBeanFactory**。基于代码复用、解耦的原则，我们可以对通用部分代码进行抽象，抽象出一个AbstractBeanFactory类。

目前，我们可以把refresh()、getBean()、registerBeanDefinition()等方法提取到抽象类，因为我们提供了默认实现，确保这些方法即使不再被其他BeanFactory实现也能正常生效。改动比较大，所以这里我贴出完整的类代码，下面就是AbstractBeanFactory的完整实现。

```java
public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry
implements BeanFactory, BeanDefinitionRegistry {
    private Map<String, BeanDefinition> beanDefinitionMap = new
ConcurrentHashMap<>(256);
    private List<String> beanDefinitionNames = new ArrayList<>();
    private final Map<String, Object> earlySingletonObjects = new HashMap<>(16);
    public AbstractBeanFactory() {
    }
    public void refresh() {
        for (String beanName : beanDefinitionNames) {
            try {
                getBean(beanName);
            }
        }
    }
    @Override
    public Object getBean(String beanName) throws BeansException {
        //先尝试直接从容器中获取bean实例
        Object singleton = this.getSingleton(beanName);
        if (singleton == null) {
            //如果没有实例，则尝试从毛胚实例中获取
            singleton = this.earlySingletonObjects.get(beanName);
            if (singleton == null) {
                //如果连毛胚都没有，则创建bean实例并注册
                System.out.println("get bean null -------------- " + beanName);
                BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
                singleton = createBean(beanDefinition);
                this.registerBean(beanName, singleton);
                // 进行beanpostprocessor处理
                // step 1: postProcessBeforeInitialization
                applyBeanPostProcessorBeforeInitialization(singleton, beanName);
                // step 2: init-method
                if (beanDefinition.getInitMethodName() != null &&
!beanDefinition.equals("")) {
                    invokeInitMethod(beanDefinition, singleton);
                }
                // step 3: postProcessAfterInitialization
                applyBeanPostProcessorAfterInitialization(singleton, beanName);
            }
        }

        return singleton;
    }
    private void invokeInitMethod(BeanDefinition beanDefinition, Object obj) {
        Class<?> clz = beanDefinition.getClass();
        Method method = null;
        try {
            method = clz.getMethod(beanDefinition.getInitMethodName());
        }
        try {
            method.invoke(obj);
        }
    }
    @Override
    public Boolean containsBean(String name) {
        return containsSingleton(name);
    }
   public void registerBean(String beanName, Object obj) {
        this.registerSingleton(beanName, obj);
    }
    @Override
    public void registerBeanDefinition(String name, BeanDefinition
beanDefinition) {
        this.beanDefinitionMap.put(name, beanDefinition);
        this.beanDefinitionNames.add(name);
        if (!beanDefinition.isLazyInit()) {
            try {
                getBean(name);
            }
        }
    }
    @Override
    public void removeBeanDefinition(String name) {
        this.beanDefinitionMap.remove(name);
        this.beanDefinitionNames.remove(name);
        this.removeSingleton(name);
    }
    @Override
    public BeanDefinition getBeanDefinition(String name) {
        return this.beanDefinitionMap.get(name);
    }
    @Override
    public boolean containsBeanDefinition(String name) {
        return this.beanDefinitionMap.containsKey(name);
    }
    @Override
    public boolean isSingleton(String name) {
        return this.beanDefinitionMap.get(name).isSingleton();
    }
    @Override
    public boolean isPrototype(String name) {
        return this.beanDefinitionMap.get(name).isPrototype();
    }
    @Override
    public Class<?> getType(String name) {
        return this.beanDefinitionMap.get(name).getClass();
    }
    private Object createBean(BeanDefinition beanDefinition) {
        Class<?> clz = null;
        //创建毛胚bean实例
        Object obj = doCreateBean(beanDefinition);
        //存放到毛胚实例缓存中
        this.earlySingletonObjects.put(beanDefinition.getId(), obj);
        try {
            clz = Class.forName(beanDefinition.getClassName());
        }
        //完善bean，主要是处理属性
        populateBean(beanDefinition, clz, obj);
        return obj;
    }
    //doCreateBean创建毛胚实例，仅仅调用构造方法，没有进行属性处理
    private Object doCreateBean(BeanDefinition beanDefinition) {
        Class<?> clz = null;
        Object obj = null;
        Constructor<?> con = null;
        try {
            clz = Class.forName(beanDefinition.getClassName());
            // handle constructor
            ConstructorArgumentValues constructorArgumentValues =
beanDefinition.getConstructorArgumentValues();
            if (!constructorArgumentValues.isEmpty()) {
                Class<?>[] paramTypes = new Class<?>
[constructorArgumentValues.getArgumentCount()];
                Object[] paramValues = new
Object[constructorArgumentValues.getArgumentCount()];
                for (int i = 0; i <
constructorArgumentValues.getArgumentCount(); i++) {
                    ConstructorArgumentValue constructorArgumentValue =
constructorArgumentValues.getIndexedArgumentValue(i);
                    if ("String".equals(constructorArgumentValue.getType()) ||
"java.lang.String".equals(constructorArgumentValue.getType())) {
                        paramTypes[i] = String.class;
                        paramValues[i] = constructorArgumentValue.getValue();
                    } else if
("Integer".equals(constructorArgumentValue.getType()) ||
"java.lang.Integer".equals(constructorArgumentValue.getType())) {
                        paramTypes[i] = Integer.class;
                        paramValues[i] = Integer.valueOf((String)
constructorArgumentValue.getValue());
                    } else if ("int".equals(constructorArgumentValue.getType()))
{
                        paramTypes[i] = int.class;
                        paramValues[i] = Integer.valueOf((String)
constructorArgumentValue.getValue());
                    } else {
                        paramTypes[i] = String.class;
                        paramValues[i] = constructorArgumentValue.getValue();
                    }
                }
                try {
                    con = clz.getConstructor(paramTypes);
                    obj = con.newInstance(paramValues);
                }
            }
        }
        System.out.println(beanDefinition.getId() + " bean created. " +
beanDefinition.getClassName() + " : " + obj.toString());
        return obj;
    }
    private void populateBean(BeanDefinition beanDefinition, Class<?> clz,
Object obj) {
        handleProperties(beanDefinition, clz, obj);
    }
    private void handleProperties(BeanDefinition beanDefinition, Class<?> clz,
Object obj) {
        // handle properties
        System.out.println("handle properties for bean : " +
beanDefinition.getId());
        PropertyValues propertyValues = beanDefinition.getPropertyValues();
        //如果有属性
        if (!propertyValues.isEmpty()) {
            for (int i = 0; i < propertyValues.size(); i++) {
                PropertyValue propertyValue =
propertyValues.getPropertyValueList().get(i);
                String pType = propertyValue.getType();
                String pName = propertyValue.getName();
                Object pValue = propertyValue.getValue();
                boolean isRef = propertyValue.getIsRef();
                Class<?>[] paramTypes = new Class<?>[1];
                Object[] paramValues = new Object[1];
                if (!isRef) { //如果不是ref，只是普通属性
                    //对每一个属性，分数据类型分别处理
                    if ("String".equals(pType) ||
"java.lang.String".equals(pType)) {
                        paramTypes[0] = String.class;
                    } else if ("Integer".equals(pType) ||
"java.lang.Integer".equals(pType)) {
                        paramTypes[i] = Integer.class;
                    } else if ("int".equals(pType)) {
                        paramTypes[i] = int.class;
                    } else {
                        paramTypes[i] = String.class;
                    }
                    paramValues[0] = pValue;
                } else {//is ref, create the dependent beans
                    try {
                        paramTypes[0] = Class.forName(pType);
                    }
                    try {//再次调用getBean创建ref的bean实例
                        paramValues[0] = getBean((String) pValue);
                    }
                }
                //按照setXxxx规范查找setter方法，调用setter方法设置属性
                String methodName = "set" + pName.substring(0, 1).toUpperCase()
+ pName.substring(1);
                Method method = null;
                try {
                    method = clz.getMethod(methodName, paramTypes);
                }
                try {
                    method.invoke(obj, paramValues);
                }
            }
        }
    }
    abstract public Object applyBeanPostProcessorBeforeInitialization(Object
existingBean, String beanName) throws BeansException;
    abstract public Object applyBeanPostProcessorAfterInitialization(Object
existingBean, String beanName) throws BeansException;
}

```

上面的代码较长，但仔细一看可以发现绝大多数是我们原本已经实现的方法，只是移动到了AbstractBeanFactory这个抽象类之中。最关键的代码是getBean()中的这一段。

```java
BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
singleton = createBean(beanDefinition);
this.registerBean(beanName, singleton);

// beanpostprocessor
// step 1: postProcessBeforeInitialization
applyBeanPostProcessorBeforeInitialization(singleton, beanName);
// step 2: init-method
if (beanDefinition.getInitMethodName() != null &&
!beanDefinition.equals("")) {
    invokeInitMethod(beanDefinition, singleton);
}
// step 3: postProcessAfterInitialization
applyBeanPostProcessorAfterInitialization(singleton, beanName);

```

先获取Bean的定义，然后创建Bean实例，再进行Bean的后处理并初始化。在这个抽象类里，我们需要关注两个核心的改动。

1. 定义了抽象方法applyBeanPostProcessorBeforeInitialization与applyBeanPostProcessorAfterInitialization，由名字可以看出，分别是在Bean处理类初始化之前和之后执行的方法。这两个方法交给具体的继承类去实现。
2. 在getBean()方法中，在以前预留的位置，实现了对Bean初始化前、初始化和初始化后的处理。

```java
  // step 1: postProcessBeforeInitialization
  applyBeanPostProcessorBeforeInitialization(singleton, beanName);
  // step 2: init-method
  if (beanDefinition.getInitMethodName() != null && !beanDefinition.equals("")) {
      invokeInitMethod(beanDefinition, singleton);
  }
  // step 3: postProcessAfterInitialization
  applyBeanPostProcessorAfterInitialization(singleton, beanName);

```

现在已经抽象出了一个AbstractBeanFactory，接下来我们看看具体的AutowireCapableBeanFactory是如何实现的。

```java
public class AutowireCapableBeanFactory extends AbstractBeanFactory{
    private final List<AutowiredAnnotationBeanPostProcessor> beanPostProcessors =
new ArrayList<>();
    public void addBeanPostProcessor(AutowiredAnnotationBeanPostProcessor
beanPostProcessor) {
        this.beanPostProcessors.remove(beanPostProcessor);
        this.beanPostProcessors.add(beanPostProcessor);
    }
    public int getBeanPostProcessorCount() {
        return this.beanPostProcessors.size();
    }
    public List<AutowiredAnnotationBeanPostProcessor> getBeanPostProcessors() {
        return this.beanPostProcessors;
    }
    public Object applyBeanPostProcessorsBeforeInitialization(Object
existingBean, String beanName) throws BeansException {
        Object result = existingBean;
        for (AutowiredAnnotationBeanPostProcessor beanProcessor :
getBeanPostProcessors()) {
            beanProcessor.setBeanFactory(this);
            result = beanProcessor.postProcessBeforeInitialization(result,
beanName);
            if (result == null) {
                return result;
            }
        }
        return result;
    }
    public Object applyBeanPostProcessorsAfterInitialization(Object existingBean,
String beanName) throws BeansException {
        Object result = existingBean;
        for (BeanPostProcessor beanProcessor : getBeanPostProcessors()) {
            result = beanProcessor.postProcessAfterInitialization(result,
beanName);
            if (result == null) {
                return result;
            }
        }
        return result;
    }
}

```

从代码里也可以看出，它实现起来并不复杂，用一个列表beanPostProcessors记录所有的Bean处理器，这样可以按照需求注册若干个不同用途的处理器，然后调用处理器。

```java
for (AutowiredAnnotationBeanPostProcessor beanProcessor :
getBeanPostProcessors()) {
    beanProcessor.setBeanFactory(this);
    result = beanProcessor.postProcessBeforeInitialization(result,
beanName);
}

```

代码一目了然，就是对每个Bean处理器，调用方法postProcessBeforeInitialization。

最后则是调整ClassPathXmlApplicationContext，引入的成员变量由SimpleBeanFactory改为新建的AutowireCapableBeanFactory，并在构造函数里增加上下文刷新逻辑。

```java
public ClassPathXmlApplicationContext(String fileName, boolean isRefresh) {
        Resource resource = new ClassPathXmlResource(fileName);
        AutowireCapableBeanFactory beanFactory = new
AutowireCapableBeanFactory();
        XmlBeanDefinitionReader reader = new
XmlBeanDefinitionReader(beanFactory);
        reader.loadBeanDefinitions(resource);
        this.beanFactory = beanFactory;
        if (isRefresh) {
            try {
                refresh();
            }
        }
    }

    public List<BeanFactoryPostProcessor> getBeanFactoryPostProcessors() {
        return this.beanFactoryPostProcessors;
    }
    public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor
postProcessor) {
        this.beanFactoryPostProcessors.add(postProcessor);
    }
    public void refresh() throws BeansException, IllegalStateException {
        // Register bean processors that intercept bean creation.
        registerBeanPostProcessors(this.beanFactory);
        // Initialize other special beans in specific context subclasses.
        onRefresh();
    }
    private void registerBeanPostProcessors(AutowireCapableBeanFactory
beanFactory) {
        beanFactory.addBeanPostProcessor(new
AutowiredAnnotationBeanPostProcessor());
    }
    private void onRefresh() {
        this.beanFactory.refresh();
    }

```

新的refresh()方法，会先注册BeanPostProcessor，这样BeanFactory里就有解释注解的处理器了，然后在getBean()的过程中使用它。

最后，我们来回顾一下完整的过程。

1. 启动ClassPathXmlApplicationContext容器，执行refresh()。
2. 在refresh执行过程中，调用registerBeanPostProcessors()，往BeanFactory里注册Bean处理器，如AutowiredAnnotationBeanPostProcessor。
3. 执行onRefresh()， 执行AbstractBeanFactory的refresh()方法。
4. AbstractBeanFactory的refresh()获取所有Bean的定义，执行getBean()创建Bean实例。
5. getBean()创建完Bean实例后，调用Bean处理器并初始化。

```plain
applyBeanPostProcessorBeforeInitialization(singleton, beanName);
invokeInitMethod(beanDefinition, singleton);
applyBeanPostProcessorAfterInitialization(singleton, beanName);

```

1. applyBeanPostProcessorBeforeInitialization由具体的BeanFactory，如AutowireCapableBeanFactory，来实现，这个实现也很简单，就是对BeanFactory里已经注册好的所有Bean处理器调用相关方法。

```plain
beanProcessor.postProcessBeforeInitialization(result, beanName);
beanProcessor.postProcessAfterInitialization(result, beanName);

```

1. 我们事先准备好的AutowiredAnnotationBeanPostProcessor方法里面会解释Bean中的Autowired注解。

## 测试注解

到这里，支持注解的工作就完成了，接下来就是测试Autowired注解了。在这里我们做两个改动。

1. 在测试类中增加Autowired注解。

```java
package com.minis.test;
import com.minis.beans.factory.annotation.Autowired;
public class BaseService {
    @Autowired
    private BaseBaseService bbs;
    public BaseBaseService getBbs() {
        return bbs;
    }
    public void setBbs(BaseBaseService bbs) {
        this.bbs = bbs;
    }
    public BaseService() {
    }
    public void sayHello() {
        System.out.println("Base Service says Hello");
        bbs.sayHello();
    }
}

```

1. 注释XML配置文件中关于循环依赖的配置。

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<beans>
    <bean id="bbs" class="com.minis.test.BaseBaseService">
        <property type="com.minis.test.AServiceImpl" name="as" ref="aservice" />
    </bean>
    <bean id="aservice" class="com.minis.test.AServiceImpl">
        <constructor-arg type="String" name="name" value="abc"/>
        <constructor-arg type="int" name="level" value="3"/>
        <property type="String" name="property1" value="Someone says"/>
        <property type="String" name="property2" value="Hello World!"/>
        <property type="com.minis.test.BaseService" name="ref1"
ref="baseservice"/>
    </bean>
    <bean id="baseservice" class="com.minis.test.BaseService">
<!--        <property type="com.minis.test.BaseBaseService" name="bbs"
ref="basebaseservice" />-->
    </bean>
</beans>

```

## 小结

这节课我们丰富了原来的框架，支持了注解，让它更有模有样了。

注解是现代最受程序员欢迎的特性，我们通过Autowired这个注解实现了Bean的注入，这样程序员不用再在XML配置文件中手动配置property，而是在类中声明property的时候直接加上注解即可，框架使用的机制是名称匹配，这也是Spring所支持的一种匹配方式。

接着我们提取了BeanFactory接口，定义了一个抽象的AbstractBeanFactory。通过这个抽象类，将Bean工厂需要做的事情的框架搭建出来，然后在具体实现类中完善细节。这种程序结构称为interface-abstract class-class（接口抽象类），是一种做框架时常用的设计模式。

![](assets/141ec0beb22e6525cb3fe484be337638.jpg)

我们自己手写MiniSpring，不仅仅是要学习一个功能如何实现，还要学习大师的做法，模仿他们的代码和设计，练习得多了就能像专业程序员一样地写代码了。

完整源代码参见 [https://github.com/YaleGuo/minis](https://github.com/YaleGuo/minis)

## 课后题

学完这节课，我也给你留一道思考题。我们实现了Autowired注解，在现有框架中能否支持多个注解？欢迎你在留言区与我交流讨论，也欢迎你把这节课分享给需要的朋友。我们下节课见！

# 05｜实现完整的IoC容器：构建工厂体系并添加容器事件

你好，我是郭屹。

前面我们已经实现了IoC的核心部分，骨架已经有了，那怎么让这个IoC丰满起来呢？这就需要实现更多的功能，让我们的IoC更加完备。所以这节课我们将通过建立BeanFactory体系，添加容器事件等一系列操作，进一步完善IoC的功能。

## 实现一个完整的IoC容器

为了让我们的MiniSpring更加专业一点，也更像Spring一点，我们将实现3个功能点。

1. 进一步增强扩展性，新增4个接口。

- ListableBeanFactory
- ConfigurableBeanFactory
- ConfigurableListableBeanFactory
- EnvironmentCapable

1. 实现DefaultListableBeanFactory，该类就是Spring IoC的引擎。
2. 改造ApplicationContext。

下面我们就一条条来看。

### 增强扩展性

首先我们来增强BeanFactory的扩展性，使它具有不同的特性。

我们以前定义的AutowireCapableBeanFactory就是在通用的BeanFactory的基础上添加了Autowired注解特性。比如可以将Factory内部管理的Bean作为一个集合来对待，获取Bean的数量，得到所有Bean的名字，按照某个类型获取Bean列表等等。这个特性就定义在ListableBeanFactory中。

```java
public interface ListableBeanFactory extends BeanFactory {
    boolean containsBeanDefinition(String beanName);
    int getBeanDefinitionCount();
    String[] getBeanDefinitionNames();
    String[] getBeanNamesForType(Class<?> type);
    <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException;
}

```

我们还可以将维护Bean之间的依赖关系以及支持Bean处理器也看作一个独立的特性，这个特性定义在ConfigurableBeanFactory接口中。

```java
public interface ConfigurableBeanFactory extends
BeanFactory,SingletonBeanRegistry {
    String SCOPE_SINGLETON = "singleton";
    String SCOPE_PROTOTYPE = "prototype";
    void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);
    int getBeanPostProcessorCount();
    void registerDependentBean(String beanName, String dependentBeanName);
    String[] getDependentBeans(String beanName);
    String[] getDependenciesForBean(String beanName);
}

```

然后还可以集成，用一个ConfigurableListableBeanFactory接口把AutowireCapableBeanFactory、ListableBeanFactory和ConfigurableBeanFactory合并在一起。

```java
package com.minis.beans.factory.config;
import com.minis.beans.factory.ListableBeanFactory;
public interface ConfigurableListableBeanFactory
        extends ListableBeanFactory, AutowireCapableBeanFactory,
ConfigurableBeanFactory {
}

```

由上述接口定义的方法可以看出，这些接口都给通用的BeanFactory与BeanDefinition新增了众多处理方法，用来增强各种特性。

在Java语言的设计中，一个Interface代表的是一种特性或者能力，我们把这些特性或能力一个个抽取出来，各自独立互不干扰。如果一个具体的类，想具备某些特性或者能力，就去实现这些interface，随意组合。这是一种良好的设计原则，叫 **interface segregation**（接口隔离原则）。这条原则在Spring框架中用得很多，你可以注意一下。

由于ConfigurableListableBeanFactory继承了AutowireCapableBeanFactory，所以我们需要调整之前定义的AutowireCapableBeanFactory，由class改为interface。

```java
public interface AutowireCapableBeanFactory  extends BeanFactory{
    int AUTOWIRE_NO = 0;
    int AUTOWIRE_BY_NAME = 1;
    int AUTOWIRE_BY_TYPE = 2;
    Object applyBeanPostProcessorsBeforeInitialization(Object existingBean,
String beanName) throws BeansException;
    Object applyBeanPostProcessorsAfterInitialization(Object existingBean,
String beanName) throws BeansException;
}

```

新增抽象类AbstractAutowireCapableBeanFactory替代原有的实现类。

```java
public abstract class AbstractAutowireCapableBeanFactory
                        extends AbstractBeanFactory implements
AutowireCapableBeanFactory{
    private final List<BeanPostProcessor> beanPostProcessors = new
ArrayList<BeanPostProcessor>();

    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        this.beanPostProcessors.remove(beanPostProcessor);
        this.beanPostProcessors.add(beanPostProcessor);
    }
    public int getBeanPostProcessorCount() {
        return this.beanPostProcessors.size();
    }
    public List<BeanPostProcessor> getBeanPostProcessors() {
        return this.beanPostProcessors;
    }
    public Object applyBeanPostProcessorsBeforeInitialization(Object
existingBean, String beanName)
            throws BeansException {
        Object result = existingBean;
        for (BeanPostProcessor beanProcessor : getBeanPostProcessors()) {
            beanProcessor.setBeanFactory(this);
            result = beanProcessor.postProcessBeforeInitialization(result,
beanName);
            if (result == null) {
                return result;
            }
        }
        return result;
    }
    public Object applyBeanPostProcessorsAfterInitialization(Object
existingBean, String beanName)
            throws BeansException {
        Object result = existingBean;
        for (BeanPostProcessor beanProcessor : getBeanPostProcessors()) {
            result = beanProcessor.postProcessAfterInitialization(result,
beanName);
            if (result == null) {
                return result;
            }
        }
        return result;
    }
}

```

上述代码与之前的实现类一致，在此不多赘述。

### 环境

除了扩充BeanFactory体系，我们还打算给容器增加一些环境因素，使一些容器整体所需要的属性有个地方存储访问。

在core目录下新建env目录，增加PropertyResolver.java、EnvironmentCapable.java、Environment.java三个接口类。EnvironmentCapable主要用于获取Environment实例，Environment则继承PropertyResoulver接口，用于获取属性。所有的ApplicationContext都实现了Environment接口。

Environment.java 接口

```java
public interface Environment extends PropertyResolver {
    String[] getActiveProfiles();
    String[] getDefaultProfiles();
    boolean acceptsProfiles(String... profiles);
}

```

EnvironmentCapable.java 接口

```java
public interface EnvironmentCapable {
    Environment getEnvironment();
}

```

PropertyResolver.java 接口

```java
public interface PropertyResolver {
    boolean containsProperty(String key);
    String getProperty(String key);
    String getProperty(String key, String defaultValue);
    <T> T getProperty(String key, Class<T> targetType);
    <T> T getProperty(String key, Class<T> targetType, T defaultValue);
    <T> Class<T> getPropertyAsClass(String key, Class<T> targetType);
    String getRequiredProperty(String key) throws IllegalStateException;
    <T> T getRequiredProperty(String key, Class<T> targetType) throws
IllegalStateException;
    String resolvePlaceholders(String text);
    String resolveRequiredPlaceholders(String text) throws
IllegalArgumentException;
}

```

### IoC引擎

接下来我们看看IoC引擎——DefaultListableBeanFactory的实现。

```java
public class DefaultListableBeanFactory extends
AbstractAutowireCapableBeanFactory
                    implements ConfigurableListableBeanFactory{
    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }
    public String[] getBeanDefinitionNames() {
        return (String[]) this.beanDefinitionNames.toArray();
    }
    public String[] getBeanNamesForType(Class<?> type) {
        List<String> result = new ArrayList<>();
        for (String beanName : this.beanDefinitionNames) {
            boolean matchFound = false;
            BeanDefinition mbd = this.getBeanDefinition(beanName);
            Class<?> classToMatch = mbd.getClass();
            if (type.isAssignableFrom(classToMatch)) {
                matchFound = true;
            }
            else {
                matchFound = false;
            }
            if (matchFound) {
                result.add(beanName);
            }
        }
        return (String[]) result.toArray();
    }
    @SuppressWarnings("unchecked")
    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException
{
        String[] beanNames = getBeanNamesForType(type);
        Map<String, T> result = new LinkedHashMap<>(beanNames.length);
        for (String beanName : beanNames) {
            Object beanInstance = getBean(beanName);
            result.put(beanName, (T) beanInstance);
        }
        return result;
    }
}

```

从上述代码中，似乎看不出这个类是如何成为IoC引擎的，因为它的实现都是很简单地获取各种属性的方法。它成为引擎的秘诀在于 **它继承了其他BeanFactory类来实现Bean的创建管理功能**。从代码可以看出它继承了AbstractAutowireCapableBeanFactory并实现了 ConfigurableListableBeanFactory接口。

参看Spring框架的这一部分，整个继承体系图。

![图片](assets/b9dc766efc3425a77fbb3d87c5dc7ec1.png)

可以看出，我们的MiniSpring跟Spring框架设计得几乎是一模一样。当然，这是我们有意为之，我们手写MiniSpring就是为了深入理解Spring。

当ClassPathXmlApplicationContext这个Spring核心启动类运行时，注入了DefaultListableBeanFactory，为整个Spring框架做了默认实现，这样就完成了框架内部的逻辑闭环。

### 事件

接着我们来完善事件的发布与监听，包括ApplicationEvent、ApplicationListener、ApplicationEventPublisher以及ContextRefreshEvent，事件一经发布就能让监听者监听到。

ApplicationEvent

```java
public class ApplicationEvent extends EventObject {
    private static final long serialVersionUID = 1L;
    protected String msg = null;
    public ApplicationEvent(Object arg0) {
        super(arg0);
        this.msg = arg0.toString();
    }
}

```

ApplicationListener

```java
public class ApplicationListener implements EventListener {
    void onApplicationEvent(ApplicationEvent event) {
        System.out.println(event.toString());
    }
}

```

ContextRefreshEvent

```java
public class ContextRefreshEvent extends ApplicationEvent{
    private static final long serialVersionUID = 1L;
    public ContextRefreshEvent(Object arg0) {
        super(arg0);
    }

    public String toString() {
        return this.msg;
    }
}

```

ApplicationEventPublisher

```java
public interface ApplicationEventPublisher {
    void publishEvent(ApplicationEvent event);
    void addApplicationListener(ApplicationListener listener);
}

```

可以看出，框架的EventPublisher，本质是对JDK事件类的封装。接口已经定义好了，接下来我们实现一个最简单的事件发布者SimpleApplicationEventPublisher。

```java
public class SimpleApplicationEventPublisher implements
ApplicationEventPublisher{
    List<ApplicationListener> listeners = new ArrayList<>();
    @Override
    public void publishEvent(ApplicationEvent event) {
        for (ApplicationListener listener : listeners) {
            listener.onApplicationEvent(event);
        }
    }
    @Override
    public void addApplicationListener(ApplicationListener listener) {
        this.listeners.add(listener);
    }
}

```

这个事件发布监听机制就可以为后面ApplicationContext的使用服务了。

## 完整的ApplicationContext

最后，我们来完善ApplicationContext，并把它作为公共接口，所有的上下文都实现自

ApplicationContext，支持上下文环境和事件发布。

```java
public interface ApplicationContext
        extends EnvironmentCapable, ListableBeanFactory, ConfigurableBeanFactory,
ApplicationEventPublisher{
}

```

我们计划做4件事。

1. 抽取ApplicationContext接口，实现更多有关上下文的内容。
2. 支持事件的发布与监听。
3. 新增AbstractApplicationContext，规范刷新上下文refresh方法的步骤规范，且将每一步骤进行抽象，提供默认实现类，同时支持自定义。
4. 完成刷新之后发布事件。

首先我们来增加ApplicationContext接口的内容，丰富它的功能。

```java
public interface ApplicationContext
        extends EnvironmentCapable, ListableBeanFactory,
ConfigurableBeanFactory, ApplicationEventPublisher{
    String getApplicationName();
    long getStartupDate();
    ConfigurableListableBeanFactory getBeanFactory() throws
IllegalStateException;
    void setEnvironment(Environment environment);
    Environment getEnvironment();
    void addBeanFactoryPostProcessor(BeanFactoryPostProcessor postProcessor);
    void refresh() throws BeansException, IllegalStateException;
    void close();
    boolean isActive();
}

```

还是按照以前的模式，先定义接口，然后用一个抽象类搭建框架，最后提供一个具体实现类进行默认实现。Spring的这个interface-abstract-class模式是值得我们学习的，它极大地增强了框架的扩展性。

我们重点看看AbstractApplicationContext的实现。因为现在我们只做到了从XML里读取配置，用来获取应用的上下文信息，但实际Spring框架里不只支持这一种方式。但无论哪种方式，究其本质都是对应用上下文的处理，所以我们来抽象ApplicationContext的公共部分。

```java
public abstract class AbstractApplicationContext implements ApplicationContext{
    private Environment environment;
    private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors = new
ArrayList<>();
    private long startupDate;
    private final AtomicBoolean active = new AtomicBoolean();
    private final AtomicBoolean closed = new AtomicBoolean();
    private ApplicationEventPublisher applicationEventPublisher;
    @Override
    public Object getBean(String beanName) throws BeansException {
        return getBeanFactory().getBean(beanName);
    }
    public List<BeanFactoryPostProcessor> getBeanFactoryPostProcessors() {
        return this.beanFactoryPostProcessors;
    }
    public void refresh() throws BeansException, IllegalStateException {
        postProcessBeanFactory(getBeanFactory());
        registerBeanPostProcessors(getBeanFactory());
        initApplicationEventPublisher();
        onRefresh();
        registerListeners();
        finishRefresh();
    }
    abstract void registerListeners();
    abstract void initApplicationEventPublisher();
    abstract void postProcessBeanFactory(ConfigurableListableBeanFactory
beanFactory);
    abstract void registerBeanPostProcessors(ConfigurableListableBeanFactory
beanFactory);
    abstract void onRefresh();
    abstract void finishRefresh();
    @Override
    public String getApplicationName() {
        return "";
    }
    @Override
    public long getStartupDate() {
        return this.startupDate;
    }
    @Override
    public abstract ConfigurableListableBeanFactory getBeanFactory() throws
IllegalStateException;
    @Override
    public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor
postProcessor) {
        this.beanFactoryPostProcessors.add(postProcessor);
    }
    @Override
    public void close() {
    }
    @Override
    public boolean isActive(){
        return true;
    }
    //省略包装beanfactory的方法
}

```

上面这段代码的核心是refresh()方法的定义，而这个方法又由下面这几个步骤组成。

```java
    abstract void registerListeners();
    abstract void initApplicationEventPublisher();
    abstract void postProcessBeanFactory(ConfigurableListableBeanFactory
beanFactory);
    abstract void registerBeanPostProcessors(ConfigurableListableBeanFactory
beanFactory);
    abstract void onRefresh();
    abstract void finishRefresh();

```

看名字就比较容易理解，首先是注册监听者，接下来初始化事件发布者，随后处理Bean以及对Bean的状态进行一些操作，最后是将初始化完毕的Bean进行应用上下文刷新以及完成刷新后进行自定义操作。因为这些方法都有abstract修饰，允许把这些步骤交给用户自定义处理，因此极大地增强了扩展性。

我们现在已经拥有了一个ClassPathXmlApplicationContext，我们以这个类为例，看看如何实现上面的几个步骤。ClassPathXmlApplicationContext代码改造如下：

```java
public class ClassPathXmlApplicationContext extends AbstractApplicationContext{
    DefaultListableBeanFactory beanFactory;
    private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors = new
ArrayList<>();
    public ClassPathXmlApplicationContext(String fileName) {
        this(fileName, true);
    }
    public ClassPathXmlApplicationContext(String fileName, boolean isRefresh) {
        Resource resource = new ClassPathXmlResource(fileName);
        DefaultListableBeanFactory beanFactory = new
DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new
XmlBeanDefinitionReader(beanFactory);
        reader.loadBeanDefinitions(resource);
        this.beanFactory = beanFactory;
        if (isRefresh) {
            try {
                refresh();
            }
       }
    }
    @Override
    void registerListeners() {
        ApplicationListener listener = new ApplicationListener();
        this.getApplicationEventPublisher().addApplicationListener(listener);
    }
    @Override
    void initApplicationEventPublisher() {
        ApplicationEventPublisher aep = new SimpleApplicationEventPublisher();
        this.setApplicationEventPublisher(aep);
    }
    @Override
    void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    }
    @Override
    public void publishEvent(ApplicationEvent event) {
        this.getApplicationEventPublisher().publishEvent(event);
    }
    @Override
    public void addApplicationListener(ApplicationListener listener) {
        this.getApplicationEventPublisher().addApplicationListener(listener);
    }
    public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor
postProcessor) {
        this.beanFactoryPostProcessors.add(postProcessor);
    }
    @Override
    void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory)
{
        this.beanFactory.addBeanPostProcessor(new
AutowiredAnnotationBeanPostProcessor());
    }
    @Override
    void onRefresh() {
        this.beanFactory.refresh();
    }
    @Override
    public ConfigurableListableBeanFactory getBeanFactory() throws
IllegalStateException {
        return this.beanFactory;
    }
    @Override
    void finishRefresh() {
        publishEvent(new ContextRefreshEvent("Context Refreshed..."));
    }
}

```

上述代码分别实现了几个抽象方法，就很高效地把ClassPathXmlApplicationContext类融入到了ApplicationContext框架里了。Spring的这个设计模式值得我们学习，采用抽象类的方式来解耦，为用户提供了极大的扩展性的便利，这也是Spring框架强大的原因之一。Spring能集成MyBatis、MySQL、Redis等框架，少不了设计模式在背后支持。

至此，我们的IoC容器就完成了，它很简单，但是这个容器麻雀虽小五脏俱全，关键是为我们深入理解Spring框架提供了很好的解剖样本。

![](assets/8d7cbd21555d7676c9d75c05f66d23a1.jpg)

## 小结

经过这节课的学习，我们初步构造了一个完整的IoC容器，目前它的功能包括4项。

1. 识别配置文件中的Bean定义，创建Bean，并放入容器中进行管理。
2. 支持配置方式或者注解方式进行Bean的依赖注入。
3. 构建了BeanFactory体系。
4. 容器应用上下文和事件发布。


   对照Spring框架，上述几点就是Spring IoC的核心。通过这个容器，我们构建应用程序的时候，将业务逻辑封装在Bean中，把对Bean的创建管理交给框架，即所谓的“控制反转”，应用程序与框架程序互动，共同运行完整程序。

实现这些概念和特性的手段和具体代码，我们都有意模仿了Spring，它们的结构和名字都是一样的，所以你回头阅读Spring框架本身代码的时候，会觉得很熟悉，学习曲线平滑。我们沿着大师的脚步往前走，不断参照大师的作品，吸收大师的养分培育自己，让我们的MiniSpring一步步成长为一棵大树。

完整源代码参见 [https://github.com/YaleGuo/minis](https://github.com/YaleGuo/minis)

## 课后题

学完这节课，我也给你留一道思考题。我们的容器以单例模式管理所有的Bean，那么怎么应对多线程环境？欢迎你在留言区与我交流讨论，也欢迎你把这节课分享给需要的朋友，我们下节课见！

# 06｜再回首：如何实现一个IoC容器？

你好，我是郭屹。

第一阶段的学习完成啦，你是不是自己也实现出了一个简单可用的IoC容器呢？如果已经完成了，欢迎你把你的实现代码放到评论区，我们一起交流讨论。

我们这一章学的IoC（Inversion of Control）是我们整个MiniSpring框架的基石，也是框架中最核心的一个特性，为了让你更好地掌握这节课的内容，我们对这一整章的内容做一个重点回顾。

### IoC重点回顾

IoC是面向对象编程里的一个重要原则，目的是从程序里移出原有的控制权，把控制权交给了容器。IoC容器是一个中心化的地方，负责管理对象，也就是Bean的创建、销毁、依赖注入等操作，让程序变得更加灵活、可扩展、易于维护。

在使用IoC容器时，我们需要先配置容器，包括注册需要管理的对象、配置对象之间的依赖关系以及对象的生命周期等。然后，IoC容器会根据这些配置来动态地创建对象，并把它们注入到需要它们的位置上。当我们使用IoC容器时，需要将对象的配置信息告诉IoC容器，这个过程叫做依赖注入（DI），而IoC容器就是实现依赖注入的工具。因此，理解IoC容器就是理解它是如何管理对象，如何实现DI的过程。

举个例子来说，我们有一个程序需要使用A对象，这个A对象依赖于一个B对象。我们可以把A对象和B对象的创建、配置工作都交给IoC容器来处理。这样，当程序需要使用A对象的时候，IoC容器会自动创建A对象，并将依赖的B对象注入到A对象中，最后返回给程序使用。

### 我们在课程中是如何一步步实现IoC容器的呢？

我们先是抽象出了Bean的定义，用一个XML进行配置，然后通过一个简单的Factory读取配置，创建bean的实例。这个极简容器只有一两个类，但是实现了bean的读取，这是原始的种子。

然后再扩展Bean，给Bean增加一些属性，如constructor、property和init-method。此时的属性值还是普通数据类型，没有对象。然后我们将属性值扩展到引用另一个Bean，实现依赖注入，同时解决了循环依赖问题。之后通过BeanPostProcessor机制让容器支持注解。

最后我们将BeanFactory扩展成一个体系，并增加应用上下文和容器事件侦听机制，完成一个完整的IoC容器。

![图片](assets/80c220588783f8c67c098275e7df0112.png)

你可以根据这些内容再好好回顾一下这个过程。另外每节课后我都留了一道思考题，你是不是认真做了呢？如果没做的话，我建议你做完再来看答案。

### 01｜原始IoC：如何通过BeanFactory实现原始版本的IoC容器？

#### 思考题

IoC的字面含义是“控制反转”，那么它究竟“反转”了什么？又是怎么体现在代码中的？

#### 参考答案

在传统的程序设计中，当需要一个对象时，我们通常使用new操作符手动创建一个对象，并且在创建对象时需要手动管理对象所依赖的其他对象。但是，在IoC控制反转中，这个过程被翻转了过来，对象的创建和依赖关系的管理被转移到了IoC容器中。

具体来说，在IoC容器中，对象的创建和依赖关系的管理大体分为两个过程。

1. 对象实例化：IoC容器负责创建需要使用的对象实例。这意味着如果一个对象需要使用其他对象，IoC容器会自动处理这些对象的创建，并且将它们注入到需要它们的对象中。
2. 依赖注入：IoC容器负责把其他对象注入到需要使用这些依赖对象的对象中。这意味着我们不需要显式地在代码中声明依赖关系，IoC容器会自动将依赖注入到对象中，从而解耦了对象之间的关系。

这些过程大大简化了对象创建和依赖关系的管理，使代码更加易于维护和扩展。下面是一个简单的Java代码示例，展示了IoC控制反转在代码中的体现。

```java
public class UserController {
    private UserService userService; // 对象不用手动创建，由容器负责创建

    public void setUserService(UserService userService) { // 不用手动管理依赖关系，由容器注入
        this.userService = userService;
    }

    public void getUser() {
        userService.getUser();
    }
}

```

在上面的示例中，UserController依赖于UserService，但是它并没有手动创建UserService对象，而是通过IoC容器自动注入。这种方式使得代码更加简洁，同时也简化了对象之间的依赖关系。

### 02｜扩展Bean：如何配置constructor、property和init-method？

### 思考题

你认为通过构造器注入和通过Setter注入有什么异同？它们各自的优缺点是什么？

#### 参考答案

先来说说它们之间的相同之处吧，首先它们都是为了把依赖的对象传递给类或对象，从而在运行时减少或消除对象之间的依赖关系，它们都可以用来注入复杂对象和多个依赖对象。此外Setter注入和构造器注入都可以用来缩小类与依赖对象之间的耦合度，让代码更加灵活、易于维护。

但同时它们之间之间也存在很多的差异。我把它们各自的优缺点整理成了一张表格放到了下面，你可以参考。

![图片](assets/f66d663e26415aebdf67a8d5c5d92bfa.png)

两者之间的优劣，人们有不同的观点，存在持久的争议。Spring团队本身的观点也在变，早期版本他们推荐使用Setter注入，Spring5之后推荐使用构造器注入。当然，我们跟随Spring团队，现在也是建议用构造器注入。

### 03｜依赖注入：如何给Bean注入值并解决循环依赖问题？

#### 思考题

你认为能不能在一个Bean的构造器中注入另一个Bean？

#### 参考答案

可以在一个Bean的构造器中注入另一个Bean。具体的做法就是通过构造器注入或者通过构造器注解方式注入。

方式一：构造器注入

在一个Bean的构造器中注入另一个Bean，可以使用构造器注入的方式。例如：

```java
public class ABean {
    private final BBean Bbean;

    public ABean(BeanB Bbean) {
        this.Bbean = Bbean;
    }

    // ...
}

public class BBean {
    // ...
}

```

可以看到，上述代码中的 ABean 类的构造器使用了 BBean 类的实例作为参数进行构造的方式，通过这样的方式可以将 BBean 实例注入到 ABean 中。

方式二：构造器注解方式注入

在Spring中，我们也可以通过在Bean的构造器上增加注解来注入另一个Bean，例如：

```java
public class ABean {
    private final BBean Bbean;

    @Autowired
    public ABean(BBean Bbean) {
        this.Bbean = Bbean;
    }

    // ...
}

public class BBean {
    // ...
}

```

在上述代码中，ABean 中的构造器使用了 @Autowired 注解，这个注解可以将 BBean 注入到 ABean 中。

通过这两种方式，我们都可以在一个Bean的构造器中注入另一个Bean，需要根据具体情况来选择合适的方式。通常情况下，通过构造器注入是更优的选择，可以确保依赖项的完全初始化，避免对象状态的污染。

对MiniSpring来讲，只需要做一点改造，在用反射调用Constructor的过程中处理参数的时候增加Bean类型的判断，然后对这个构造器参数再调用一次getBean()就可以了。

当然，我们要注意了。构造器注入是在Bean实例化过程中起作用的，一个Bean没有实例化完成的时候就去实例化另一个Bean，这个时候连“早期的毛胚Bean”都没有，因此解决不了循环依赖的问题。

### 04｜增强IoC容器：如何让我们的Spring支持注解？

#### 思考题

我们实现了Autowired注解，在现有框架中能否支持多个注解？

#### 参考答案

如果这些注解是不同作用的，那么在现有架构中是可以支持多个注解并存的。比如要给某个属性上添加一个@Require注解，表示这个属性不能为空，我们来看下实现的思路。

MiniSpring中，对注解的解释是通过BeanPostProcessor来完成的。我们增加一个RequireAnnotationBeanPostProcessor类，在它的postProcessAfterInitialization()方法中解释这个注解，判断是不是为空，如果为空则抛出BeanException。

然后改写ClassPathXmlApplicationContext类中的registerBeanPostProcessors()方法，将这个新定义的beanpostprocessor注册进去。

```java
beanFactory.addBeanPostProcessor(new
RequireAnnotationBeanPostProcessor());

```

这样，在getBean()方法中就会在init-method被调用后用到这个RequireAnnotationBeanPostProcessor。

### 05｜实现完整的IoC容器：构建工厂体系并添加容器事件

### 思考题

我们的容器以单例模式管理所有的bean，那么怎么应对多线程环境？

#### 参考答案

第二节课我们曾经提到过这个问题。这里我们来概括一下。

> 我们将 singletons 定义为了一个ConcurrentHashMap，而且在实现 registrySingleton 时前面加了一个关键字synchronized。这一切都是为了确保在多线程并发的情况下，我们仍然能安全地实现对单例Bean的管理，无论是单线程还是多线程，我们整个系统里面这个Bean总是唯一的、单例的。——内容来自第 2 课

在单例模式下，容器管理所有的 Bean 时，多线程环境下可能存在线程安全问题。为了避免这种问题，我们可以采取一些措施。

1. 避免共享数据

在单例模式下，所有的 Bean 都是单例的，如果 Bean 中维护了共享数据，那么就可能出现线程安全问题。为了避免共享数据带来的问题，我们可以采用一些方法来避免数据共享。例如，在 Bean 中尽量使用方法局部变量而不是成员变量，并且保证方法中不修改成员变量。

1. 使用线程安全的数据结构

在单例模式下，如果需要使用一些共享数据的数据结构，建议使用线程安全的数据结构，比如 ConcurrentHashMap 代替 HashMap，使用 CopyOnWriteArrayList 代替 ArrayList 等。这些线程安全的数据结构能够确保在多线程环境下安全地进行并发读写操作。

1. 同步

在单例模式下，如果需要操作共享数据，并且不能使用线程安全的数据结构，那么就需要使用同步机制。可以通过 synchronized 关键字来实现同步，也可以使用一些更高级的同步机制，例如 ReentrantLock、ReadWriteLock 等。

需要注意的是，使用同步机制可能会影响系统性能，并且容易出现死锁等问题，所以需要合理使用。

1. 使用ThreadLocal

如果我们需要在多线程环境下共享某些数据，但是又想保证数据的线程安全性，可以使用 ThreadLocal 来实现。ThreadLocal 可以保证每个线程都拥有自己独立的数据副本，从而避免多个线程对同一数据进行竞争。

综上所述，在单例模式下，为了避免多线程环境下的线程安全问题，我们需要做好线程安全的设计工作，避免共享数据，选用线程安全的数据结构，正确使用同步机制，以及使用ThreadLocal等方法保证数据的线程安全性。

# 07｜原始MVC：如何通过单一的Servlet拦截请求分派任务？

你好，我是郭屹。从这节课开始，我们开启一个新的部分：MVC。

前面一章，我们实现了一个简单的IoC。麻雀虽小，五脏俱全，相比原生Spring框架而言，我们写的MiniSpring功能简单，但其核心功能已具备。我们会在这个基础上进一步扩展我们的框架。

这一章我们来实现Spring MVC。MVC，全名对应Model（模型）、View（视图）、Controller（控制器）。它的基本流程是：前端发送请求到控制器，控制器寻找对应模型，找到模型后返回结果，渲染视图返回给前端生成页面。这是标准的前端请求数据的模型。实现了MVC之后，我们会把MVC和之前我们已经实现的IoC结合起来，这是我们这一章的整体思路。

![图片](assets/a79dc2ca9b96c2f4904c2f389926fb41.png)

这节课我们就开启Spring MVC的第一步，先实现一个原始的MVC。目标是通过一个Controller来拦截用户请求，找到相应的处理类进行逻辑处理，然后将处理的结果发送给客户端。

## 调整目录

按照惯例，我们还是参照Spring的目录结构来调整。MVC是Web模型，所以我们先调整一下目前的项目结构，采用Web的项目结构。同时，我们还要引入Tomcat服务器以及Tomcat的jar包。

你可以看一下项目目录结构，主要是新增一个和src目录同级的WebContent目录，在这个目录里存储部分前端页面需要的静态资源，还有各项XML配置文件。

```java
src
└── com
│ ├── minis
│ │ ├── web
│ │ ├── util
│ │ └── test
WebContent
├── WEB-INF
│ ├── lib
│ ├── web.xml
│ ├── minisMVC-servlet.xml
└── META-INF
│ └── MANIFEST.MF

```

参考Spring MVC，我们定义web.xml和minisMVC-servlet.xml这两个配置文件的内容。

1. minisMVC-servlet.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<beans>
  <bean id="/helloworld" class="com.minis.test.HelloWorldBean" value="doGet"/>
</beans>

```

1. web.xml

```java
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xmlns:web="http://xmlns.jcp.org/xml/ns/javaee"
xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd" id="WebApp_ID">
  <servlet>
    <servlet-name>minisMVC</servlet-name>
    <servlet-class>com.minis.web.DispatcherServlet</servlet-class>
      <init-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>/WEB-INF/minisMVC-servlet.xml</param-value>
      </init-param>
      <load-on-startup>1</load-on-startup>
  </servlet>
  <servlet-mapping>
    <servlet-name>minisMVC</servlet-name>
    <url-pattern>/</url-pattern>
  </servlet-mapping>
</web-app>

```

这两个XML文件里，minisMVC-servlet.xml是我们很熟悉的Bean配置，只是把id设置成了一个URL的形式，来匹配后端的程序，访问/helloworld的时候，对应调用HelloWorldBean类里的doGet()方法。

## Servlet

接下来我们重点关注web.xml。MVC里有一个核心概念是Servlet，通俗理解成运行在Web服务器上的程序。针对上面的XML配置，我们解读一下里面几个标签的含义。

![](assets/3f618deba5608e66ca0174ac1ba82ef6.png)

整个结构就是一个标准的JavaEE结构，我们按照规范解释它，就是当Servlet容器启动的时候，先读取web.xml配置，加载配置文件中的servlet，也就是DispatcherServlet，并规定它拦截所有的HTTP请求，所以它就是控制器。

我们注意到这个控制器DispatcherServlet有一个参数 contextConfigLocation，它配置了控制器要找的逻辑处理类的文件minisMVC-servlet.xml。

```java
      <init-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>/WEB-INF/minisMVC-servlet.xml</param-value>
      </init-param>

```

因此，为了启动这个servlet，我们要提前解析minisMVC-servlet.xml文件。

### 解析servlet.xml

首先定义实体类MappingValue里的三个属性：uri、clz与method，分别与minisMVC-servlet.xml中标签的属性id、class与value对应。

```java
package com.minis.web;

public class MappingValue {
	String uri;
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	String clz;
	public String getClz() {
		return clz;
	}
	public void setClz(String clz) {
		this.clz = clz;
	}
	String method;
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}

	public MappingValue(String uri, String clz, String method) {
		this.uri = uri;
		this.clz = clz;
		this.method = method;
	}
}

```

然后我们定义Resource用来加载配置文件。

```java
package com.minis.web;
import java.util.Iterator;
public interface Resource extends Iterator<Object>{
}

```

这是具体的实现。

```java
package com.minis.web;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class ClassPathXmlResource implements Resource {
	Document document;
	Element rootElement;
	Iterator<Element> elementIterator;

	public ClassPathXmlResource(URL xmlPath) {
        SAXReader saxReader=new SAXReader();
        try {
			this.document = saxReader.read(xmlPath);
			this.rootElement=document.getRootElement();
			this.elementIterator=this.rootElement.elementIterator();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	@Override
	public boolean hasNext() {
		return this.elementIterator.hasNext();
	}
	@Override
	public Object next() {
		return this.elementIterator.next();
	}
}

```

```java
package com.minis.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dom4j.Element;

public class XmlConfigReader {
	public XmlConfigReader() {
	}
	public Map<String,MappingValue> loadConfig(Resource res) {
		Map<String,MappingValue> mappings = new HashMap<>();

        while (res.hasNext()) { //读所有的节点，解析id, class和value
        	Element element = (Element)res.next();
            String beanID=element.attributeValue("id");
            String beanClassName=element.attributeValue("class");
            String beanMethod=element.attributeValue("value");

            mappings.put(beanID, new MappingValue(beanID,beanClassName,beanMethod));
        }

        return mappings;
	}
}

```

上述几段代码，是不是似曾相识？和我们前一部分编写的解析IoC的配置文件基本没什么差别，通过这些方法就能把XML里配置的Bean加载到内存里了，这里我就不再多说了。

### 实现MVC的核心启动类DispatcherSevlet

现在项目的搭建和前期准备工作已经完成，我们开始着手实现web.xml中配置的com.minis.web.DispatcherServlet这个MVC的核心启动类，完成URL映射机制。

**MVC的基本思路是屏蔽Servlet的概念，让程序员主要写业务逻辑代码**。浏览器访问的URL通过映射机制找到实际的业务逻辑方法。按照Servlet规范，可以通过Filter拦截，也可以通过Servlet拦截。MiniSpring的实现过程中，我模仿Spring MVC通过Servlet拦截所有请求，处理映射关系，调用业务逻辑代码，处理返回值回递给浏览器。程序员写的业务逻辑程序，也叫做Bean。

在DispatcherSevlet内，定义了三个Map，分别记录URL对应的MappingValue对象、对应的类和对应的方法。

```java
private Map<String, MappingValue> mappingValues;
private Map<String, Class<?>> mappingClz = new HashMap<>();
private Map<String, Object> mappingObjs = new HashMap<>();

```

随后实现Servlet初始化方法，初始化主要处理从外部传入的资源，将XML文件内容解析后存入mappingValues内。最后调用Refresh()函数创建Bean，这节课的例子就是HelloWorldBean，这些Bean的类和实例存放在mappingClz和mappingObjs里。

```java
    public void init(ServletConfig config) throws ServletException {
    	super.init(config);

        sContextConfigLocation = config.getInitParameter("contextConfigLocation");
        URL xmlPath = null;
		try {
			xmlPath = this.getServletContext().getResource(sContextConfigLocation);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		Resource rs = new ClassPathXmlResource(xmlPath);
        XmlConfigReader reader = new XmlConfigReader();
        mappingValues = reader.loadConfig(rs);
        Refresh();
    }

```

下面是Refresh()方法。

```plain
//对所有的mappingValues中注册的类进行实例化，默认构造函数
protected void Refresh() {
 	for (Map.Entry<String,MappingValue> entry : mappingValues.entrySet()) {
    	String id = entry.getKey();
    	String className = entry.getValue().getClz();
    	Object obj = null;
    	Class<?> clz = null;
		try {
			clz = Class.forName(className);
			obj = clz.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mappingClz.put(id, clz);
    	mappingObjs.put(id, obj);
    }
}

```

Refresh()就是通过读取mappingValues中的Bean定义，加载类，创建实例。这个方法完成之后，整个DispatcherSevlet就准备好了。

DispatcherSevlet用来处理所有的Web请求，但是目前我们只是简单地实现了Get请求的处理，通过Bean的id获取其对应的类和方法，依赖反射机制进行调用。你可以看一下相关代码。

```java
protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String sPath = request.getServletPath(); //获取请求的path
	if (this.mappingValues.get(sPath) == null) {
		return;
	}

    Class<?> clz = this.mappingClz.get(sPath); //获取bean类定义
    Object obj = this.mappingObjs.get(sPath);  //获取bean实例
    String methodName = this.mappingValues.get(sPath).getMethod(); //获取调用方法名
    Object objResult = null;
    try {
        Method method = clz.getMethod(methodName);
        objResult = method.invoke(obj); //方法调用
    } catch (Exception e) {
    }
    //将方法返回值写入response
    response.getWriter().append(objResult.toString());
}

```

到这里，一个最简单的DispatcherServlet就完成了，DispatcherServlet就是一个普通的Servlet，并不神秘，只要我们有一个Servlet容器，比如Tomcat，它就能跑起来。

这个实现很简陋，调用的方法没有参数，返回值只是String，直接通过response回写。

我们试一个简单的测试类。

```java
package com.minis.test;

public class HelloWorldBean {
	public String doGet() {
		return "hello world!";
	}
	public String doPost() {
		return "hello world!";
	}
}

```

启动Tomcat，在浏览器内键入localhost:8080/helloworld，就能显示返回结果"hello world for doGet!"。

到这里，我们初步实现了MVC的框架，支持了一个简单的请求由Controller控制器（DispatcherServlet），到底层查找模型结构Model（helloWorldBean），最后返回前端渲染视图View（response.getWriter().append()）的过程。

## 扩展MVC

在这个简陋的模型基础之上，我们一步步扩展，引入@RequestMapping，还会实现ComponentScan，简化配置工作。

### 简化配置

首先我们来简化XML中的繁琐配置，在minisMVC-servlet.xml里新增和两个标签，分别表示组件配置以及组件的扫描配置。也就是说，扫描一个包，自动配置包内满足条件的类，省去手工配置过程。你可以参考下面的代码。

```xml
(minisMVC-servlet.xml)
<?xml version="1.0" encoding="UTF-8" ?>
<components>
    <component-scan base-package="com.minis.test" />
</components>

```

上述文件将扫描com.minis.test里所有的类文件，加载并实例化它们。

### 引入@RequestMapping

接下来我们引入@RequestMapping，将 URL 和业务处理类中的某个方法对应起来，这样也就不再需要手工地将映射关系写到XML配置文件里，省去我们的手工配置工作。在Spring框架里， @RequestMapping 注解可支持定义在类上，但我们这里暂时不支持该注解定义在类上，只定义在方法上。我们看一下注解定义。

```java
package com.minis.web;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

@Target(value={ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    String value() default "";
}

```

@RequestMapping定义很简单，现在只有value一个字段，用来接收配置的URL。

有了注解定义，我们就可以动手编程实现了。因为修改了minisMVC-servlet.xml这个文件内的标签结构，因此我们提供一个新类 XmlScanComponentHelper，专门用来解析新定义的标签结构。

```java
package com.minis.web;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

public class XmlScanComponentHelper {
    public static List<String> getNodeValue(URL xmlPath) {
        List<String> packages = new ArrayList<>();
        SAXReader saxReader = new SAXReader();
        Document document = null;
        try {
            document = saxReader.read(xmlPath); //加载配置文件
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        Element root = document.getRootElement();
        Iterator it = root.elementIterator();
        while (it.hasNext()) { //得到XML中所有的base-package节点
            Element element = (Element) it.next();
            packages.add(element.attributeValue("base-package"));              }
        return packages;
    }
}

```

程序也很简单，原有的XmlConfigReadder 、Resource 、MappingValue 和ClassPathXmlResource 不再需要使用，取而代之的是XmlScanComponentHelper ，把扫描到的package 存储在List packages 这个结构里。代码的核心就是获取“base-package”参数值，加载到内存里。

### 修改 DispatcherServlet

经过上面这些步骤之后，接下来我们需要进一步修改 DispatcherServlet ，因为最终一切的落脚点都在这个类里，这个类承载了所有请求的解析和处理请求的步骤。我们在 DispatcherServlet 里使用下面的数据结构来存储配置。

```java
private List<String> packageNames = new ArrayList<>();
private Map<String,Object> controllerObjs = new HashMap<>();
private List<String> controllerNames = new ArrayList<>();
private Map<String,Class<?>> controllerClasses = new HashMap<>();         private List<String> urlMappingNames = new ArrayList<>();
private Map<String,Object> mappingObjs = new HashMap<>();
private Map<String,Method> mappingMethods = new HashMap<>();

```

我们看下这些变量的作用。

![](assets/2ae701e90ef7b180646a1a9f3fa6bac9.png)

接下来，Servlet初始化时我们把 minisMVC-servlet.xml 里扫描出来的 package 名称存入 packageNames 列表，初始化方法 init 中增加以下这行代码。

```java
this.packageNames = XmlScanComponentHelper.getNodeValue(xmlPath);

```

注：原有的与 ClassPathXmlResource 、Resource 相关代码要清除。

我们再将 refresh()方法分成两步：第一步初始化 controller ，第二步则是初始化 URL 映射。

对应的 refresh() 方法进行如下抽象：

```java
protected void refresh() {
    initController(); // 初始化 controller
    initMapping(); // 初始化 url 映射
}

```

接下来完善initController() ，其主要功能是对扫描到的每一个类进行加载和实例化，与类的名字建立映射关系，分别存在 controllerClasses 和 controllerObjs 这两个map里，类名就是key的值。

```java
protected void initController() {
    //扫描包，获取所有类名
    this.controllerNames = scanPackages(this.packageNames);
    for (String controllerName : this.controllerNames) {
        Object obj = null;
        Class<?> clz = null;
        try {
            clz = Class.forName(controllerName); //加载类
            this.controllerClasses.put(controllerName, clz);
        } catch (Exception e) {
        }
        try {
            obj = clz.newInstance(); //实例化bean
            this.controllerObjs.put(controllerName, obj);
        } catch (Exception e) {
        }
    }

```

扫描程序是对文件目录的递归处理，最后的结果就是把所有的类文件扫描出来。

```java
private List<String> scanPackages(List<String> packages) {
    List<String> tempControllerNames = new ArrayList<>();
    for (String packageName : packages) {
        tempControllerNames.addAll(scanPackage(packageName));
    }
    return tempControllerNames;
}
private List<String> scanPackage(String packageName) {
    List<String> tempControllerNames = new ArrayList<>();
    URI uri = null;
    //将以.分隔的包名换成以/分隔的uri
    try {
        uri = this.getClass().getResource("/" +
packageName.replaceAll("\\.", "/")).toURI();
    } catch (Exception e) {
    }
    File dir = new File(uri);
    //处理对应的文件目录
    for (File file : dir.listFiles()) { //目录下的文件或者子目录
        if(file.isDirectory()){ //对子目录递归扫描
            scanPackage(packageName+"."+file.getName());
        }else{ //类文件
            String controllerName = packageName +"."
+file.getName().replace(".class", "");
            tempControllerNames.add(controllerName);
        }
    }
    return tempControllerNames;
}

```

然后完善initMapping() ，功能是初始化 URL 映射，找到使用了注解@RequestMapping 的方法，URL 存放到 urlMappingNames 里，映射的对象存放到 mappingObjs 里，映射的方法存放到 mappingMethods 里。用这个方法取代了过去解析 Bean 得到的映射。

```java
protected void initMapping() {
    for (String controllerName : this.controllerNames) {
        Class<?> clazz = this.controllerClasses.get(controllerName);
             Object obj = this.controllerObjs.get(controllerName);
        Method[] methods = clazz.getDeclaredMethods();
        if (methods != null) {
            for (Method method : methods) {
                //检查所有的方法
                boolean isRequestMapping =
method.isAnnotationPresent(RequestMapping.class);
                if (isRequestMapping) { //有RequestMapping注解
                    String methodName = method.getName();
                    //建立方法名和URL的映射
                    String urlMapping =
method.getAnnotation(RequestMapping.class).value();
                    this.urlMappingNames.add(urlMapping);
                    this.mappingObjs.put(urlMapping, obj);
                    this.mappingMethods.put(urlMapping, method);
                }
            }
        }
    }
}

```

最后略微调整 doGet() 方法内的代码，去除不再使用的结构。

```java
protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String sPath = request.getServletPath();
	if (!this.urlMappingNames.contains(sPath)) {
		return;
	}
    Object obj = null;
    Object objResult = null;
    try {
        Method method = this.mappingMethods.get(sPath);
        obj = this.mappingObjs.get(sPath);
        objResult = method.invoke(obj);
    } catch (Exception e) {
    }
    response.getWriter().append(objResult.toString());
}

```

修改一下测试类，在com.minis.test.HelloworldBean内的测试方法上，增加@RequestMapping注解。

```java
package com.minis.test;

import com.minis.web.RequestMapping;

public class HelloWorldBean {
    @RequestMapping("/test")
    public String doTest() {
        return "hello world for doGet!";
    }
}

```

启动Tomcat进行测试，在浏览器输入框内键入：localhost:8080/test。

## 小结

![](assets/a36a0e7a21cdb86d7d9975d932b99364.jpg)

我们这节课构建了一个DispatcherServlet，它是Tomcat中注册的唯一的Servlet，它承担了所有请求的处理功能。由它来解析请求中的路径与业务类Bean中方法的映射关系，调用Bean的相应方法，返回给response。

这种映射关系的建立，我们一开始是让用户自己在XML配置文件中手动声明，然后我们引入RequestMapping注解，扫描包中的类，检查注解，自动注册映射关系。这样我们初步实现了比较原始的MVC。在这个框架下，应用程序员不用再关心Servlet的使用，他们可以直接建立业务类，加上注解就可以运行。

完整源代码参见 [https://github.com/YaleGuo/minis](https://github.com/YaleGuo/minis)，mvc分支。

## 课后题

学完这节课，我也给你留一道思考题。我们在MVC中也使用了Bean这个概念，它跟我们以前章节中的Bean是什么关系？欢迎你在留言区与我交流讨论，也欢迎你把这节课分享给需要的朋友。我们下节课见！

# 08｜整合IoC和MVC：如何在Web环境中启动IoC容器？

你好，我是郭屹。

通过上节课的工作，我们就初步实现了一个原始的MVC框架，并引入了@RequestMapping注解，还通过对指定的包进行全局扫描来简化XML文件配置。但是这个MVC框架是独立运行的，它跟我们之前实现的IoC容器还没有什么关系。

那么这节课，我们就把前面实现的IoC容器与MVC结合在一起，使MVC的Controller可以引用容器中的Bean，这样整合成一个大的容器。

## Servlet服务器启动过程

IoC容器是一个自我实现的服务器，MVC是要符合Web规范的，不能自己想怎么来就怎么来。为了融合二者，我们有必要了解一下Web规范的内容。在Servlet规范中，服务器启动的时候，会根据web.xml文件来配置。下面我们花点时间详细介绍一下这个配置文件。

这个web.xml文件是Java的Servlet规范中规定的，它里面声明了一个Web应用全部的配置信息。按照规定，每个Java Web应用都必须包含一个web.xml文件，且必须放在WEB-INF路径下。它的顶层根是web-app，指定命名空间和schema规定。通常，我们会在web.xml中配置context-param、Listener、Filter和Servlet等元素。

下面是常见元素的说明。

```plain
<display-name></display-name>
声明WEB应用的名字
<description></description>
 声明WEB应用的描述信息
<context-param></context-param>
声明应用全局的初始化参数。
<listener></listener>
声明监听器，它在建立、修改和删除会话或servlet环境时得到事件通知。
<filter></filter>
声明一个实现javax.servlet.Filter接口的类。
<filter-mapping></filter-mapping>
声明过滤器的拦截路径。
<servlet></servlet>
声明servlet类。
<servlet-mapping></servlet-mapping>
声明servlet的访问路径，试一个方便访问的URL。
<session-config></session-config>
session有关的配置，超时值。
<error-page></error-page>
在返回特定HTTP状态代码时，或者特定类型的异常被抛出时，能够制定将要显示的页面。

```

当Servlet服务器如Tomcat启动的时候，要遵守下面的时序。

1. 在启动Web项目时，Tomcat会读取web.xml中的comtext-param节点，获取这个Web应用的全局参数。
2. Tomcat创建一个ServletContext实例，是全局有效的。
3. 将context-param的参数转换为键值对，存储在ServletContext里。
4. 创建listener中定义的监听类的实例，按照规定Listener要继承自ServletContextListener。监听器初始化方法是contextInitialized(ServletContextEvent event)。初始化方法中可以通过event.getServletContext().getInitParameter(“name”)方法获得上下文环境中的键值对。
5. 当Tomcat完成启动，也就是contextInitialized方法完成后，再对Filter过滤器进行初始化。
6. servlet初始化：有一个参数load-on-startup，它为正数的值越小优先级越高，会自动启动，如果为负数或未指定这个参数，会在servlet被调用时再进行初始化。init-param 是一个servlet整个范围之内有效的参数，在servlet类的init()方法中通过 this.getInitParameter(″param1″)方法获得。

规范中规定的这个时序，就是我们整合两者的关键所在。

## Listener初始化启动IoC容器

由上述服务器启动过程我们知道，我们把web.xml文件里定义的元素加载过程简单归总一下：先获取全局的参数context-param来创建上下文，之后如果配置文件里定义了Listener，那服务器会先启动它们，之后是Filter，最后是Servlet。因此我们可以利用这个时序，把容器的启动放到Web应用的Listener中。

Spring MVC就是这么设计的，它按照这个规范，用ContextLoaderListener来启动容器。我们也模仿它同样来实现这样一个Listener。

```java
package com.minis.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ContextLoaderListener implements ServletContextListener {
	public static final String CONFIG_LOCATION_PARAM = "contextConfigLocation";
	private WebApplicationContext context;

	public ContextLoaderListener() {
	}
	public ContextLoaderListener(WebApplicationContext context) {
		this.context = context;
	}
	@Override
	public void contextDestroyed(ServletContextEvent event) {
	}
	@Override
	public void contextInitialized(ServletContextEvent event) {
		initWebApplicationContext(event.getServletContext());
	}
	private void initWebApplicationContext(ServletContext servletContext) {
		String sContextLocation = servletContext.getInitParameter(CONFIG_LOCATION_PARAM);
		WebApplicationContext wac = new AnnotationConfigWebApplicationContext(sContextLocation);
		wac.setServletContext(servletContext);
		this.context = wac;
		servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, this.context);
	}
}

```

ContextLoaderListener这个类里，先声明了一个常量CONFIG\_LOCATION\_PARAM，它的默认值是contextConfigLocation，这是代表配置文件路径的一个变量，也就是IoC容器的配置文件。这也就意味着，Listener期望web.xml里有一个参数用来配置文件路径。我们可以看一下web.xml文件。

```plain
  <context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>applicationContext.xml</param-value>
  </context-param>
  <listener>
    <listener-class>
	        com.minis.web.ContextLoaderListener
	    </listener-class>
  </listener>

```

上面这个文件，定义了这个Listener，还定义了全局参数指定配置文件路径。

ContextLoaderListener这个类里还定义了WebApplicationContext对象，目前还不存在这个类。但通过名字可以知道，WebApplicationContext 是一个上下文接口，应用在Web项目里。我们看看如何定义WebApplicationContext。

```java
package com.minis.web;

import javax.servlet.ServletContext;
import com.minis.context.ApplicationContext;

public interface WebApplicationContext extends ApplicationContext {
	String ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE = WebApplicationContext.class.getName() + ".ROOT";

	ServletContext getServletContext();
	void setServletContext(ServletContext servletContext);
}

```

可以看出，这个上下文接口指向了Servlet容器本身的上下文ServletContext。

接下来我们继续完善 ContextLoaderListener 这个类， 在初始化的过程中初始化WebApplicationContext， 并把这个上下文放到 servletContext 的 Attribute 某个属性里面。

```java
public void contextInitialized(ServletContextEvent event) {
    initWebApplicationContext(event.getServletContext());
}
private void initWebApplicationContext(ServletContext servletContext) {
    String sContextLocation =
servletContext.getInitParameter(CONFIG_LOCATION_PARAM);
    WebApplicationContext wac = new
AnnotationConfigWebApplicationContext(sContextLocation);
    wac.setServletContext(servletContext);
    this.context = wac;
    servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ ATTRIBUTE, this.context);

```

在这段代码中，通过配置文件参数从web.xml中得到配置文件路径，如applicationContext.xml，然后用这个配置文件创建了AnnotationConfigWebApplicationContext这一对象，我们叫WAC，这就成了新的上下文。然后调用servletContext.setAttribute()方法，按照默认的属性值将WAC设置到servletContext里。这样，AnnotationConfigWebApplicationContext 和 servletContext 就能够互相引用了，很方便。

而这个AnnotationConfigWebApplicationContext又是什么呢？我们看下它的定义。

```java
package com.minis.web;

import javax.servlet.ServletContext;
import com.minis.context.ClassPathXmlApplicationContext;

public class AnnotationConfigWebApplicationContext
					extends ClassPathXmlApplicationContext implements WebApplicationContext{
	private ServletContext servletContext;

	public AnnotationConfigWebApplicationContext(String fileName) {
		super(fileName);
	}
	@Override
	public ServletContext getServletContext() {
		return this.servletContext;
	}
	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
}

```

由 AnnotationConfigWebApplicationContext 的继承关系可看出，该类其实质就是我们IoC容器中的ClassPathXmlApplicationContext，只是在此基础上增加了 servletContext 的属性，这样就成了一个适用于Web场景的上下文。

我们在这个过程中用到了一个配置文件applicationContext.xml，它是由定义在web.xml里的一个参数指明的。

```plain
  <context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>applicationContext.xml</param-value>
  </context-param>

```

这个配置文件就是我们现在的IoC容器的配置文件，主要作用是声明Bean，如：

```plain
<?xml version="1.0" encoding="UTF-8"?>
<beans>
	<bean id="bbs" class="com.test.service.BaseBaseService">
	    <property type="com.test.service.AServiceImpl" name="as" ref="aservice"/>
	</bean>
	<bean id="aservice" class="com.test.service.AServiceImpl">
		<constructor-arg type="String" name="name" value="abc"/>
		<constructor-arg type="int" name="level" value="3"/>
        <property type="String" name="property1" value="Someone says"/>
        <property type="String" name="property2" value="Hello World!"/>
        <property type="com.test.service.BaseService" name="ref1" ref="baseservice"/>
	</bean>
	<bean id="baseservice" class="com.test.service.BaseService">
	</bean>
</beans>

```

回顾一下，现在完整的过程是：当Sevlet服务器启动时，Listener会优先启动，读配置文件路径，启动过程中初始化上下文，然后启动IoC容器，这个容器通过refresh()方法加载所管理的Bean对象。这样就实现了Tomcat启动的时候同时启动IoC容器。

## 改造DispatcherServlet，关联WAC

好了，到了这一步，IoC容器启动了，我们回来再讨论MVC这边的事情。我们已经知道，在服务器启动的过程中，会注册 Web应用上下文，也就是WAC。 这样方便我们通过属性拿到启动时的 WebApplicationContext 。

```java
this.webApplicationContext = (WebApplicationContext) this.getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION _CONTEXT_ATTRIBUTE);

```

因此我们改造一下DispatcherServlet这个核心类里的init()方法。

```java
public void init(ServletConfig config) throws ServletException {          super.init(config);
    this.webApplicationContext = (WebApplicationContext)
this.getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION _CONTEXT_ATTRIBUTE);
    sContextConfigLocation = config.getInitParameter("contextConfigLocation");
    URL xmlPath = null;
	try {
		xmlPath = this.getServletContext().getResource(sContextConfigLocation);
	} catch (MalformedURLException e) {
		e.printStackTrace();
	}
    this.packageNames = XmlScanComponentHelper.getNodeValue(xmlPath);        Refresh();
}

```

首先在Servlet初始化的时候，从sevletContext里获取属性，拿到Listener启动的时候注册好的WebApplicationContext，然后拿到Servlet配置参数contextConfigLocation，这个参数代表的是配置文件路径，这个时候是我们的MVC用到的配置文件，如minisMVC-servlet.xml，之后再扫描路径下的包，调用refresh()方法加载Bean。这样，DispatcherServlet也就初始化完毕了。

然后是改造initMapping()方法，按照新的办法构建URL和后端程序之间的映射关系：查找使用了注解 @RequestMapping 的方法，将 URL 存放到 urlMappingNames 里，再把映射的对象存放到 mappingObjs 里，映射的方法存放到 mappingMethods 里。用这个方法取代过去解析 Bean 得到的映射，省去了XML文件里的手工配置。你可以看一下相关代码。

```java
protected void initMapping() {
    for (String controllerName : this.controllerNames) {
        Class<?> clazz = this.controllerClasses.get(controllerName);             Object obj = this.controllerObjs.get(controllerName);
        Method[] methods = clazz.getDeclaredMethods();
        if (methods != null) {
            for (Method method : methods) {
                boolean isRequestMapping =
method.isAnnotationPresent(RequestMapping.class);
                if (isRequestMapping) {
                    String methodName = method.getName();
                    String urlMapping =
method.getAnnotation(RequestMapping.class).value();
                    this.urlMappingNames.add(urlMapping);
                    this.mappingObjs.put(urlMapping, obj);
                    this.mappingMethods.put(urlMapping, method);
                }
            }
        }
    }
}

```

最后稍微调整一下 doGet() 方法内的代码，去除不再使用的结构。

```java
protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String sPath = request.getServletPath();
	if (!this.urlMappingNames.contains(sPath)) {
		return;
	}

    Object obj = null;
    Object objResult = null;
    try {
        Method method = this.mappingMethods.get(sPath);
        obj = this.mappingObjs.get(sPath);
        objResult = method.invoke(obj);
    } catch (Exception e) {
		e.printStackTrace();
	}
    response.getWriter().append(objResult.toString());
}

```

代码里的这个doGet()方法从请求中获取访问路径，按照路径和后端程序的映射关系，获取到需要调用的对象和方法，调用方法后直接把结果返回给response。

到这里，整合了IoC容器的MVC就完成了。

## 验证

下面进行测试，我们先看一下Tomcat使用的web.xml文件配置。

```plain
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:web="http://xmlns.jcp.org/xml/ns/javaee" xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd" id="WebApp_ID">
  <context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>applicationContext.xml</param-value>
  </context-param>
  <listener>
    <listener-class>
	        com.minis.web.ContextLoaderListener
	    </listener-class>
  </listener>
  <servlet>
    <servlet-name>minisMVC</servlet-name>
    <servlet-class>com.minis.web.DispatcherServlet</servlet-class>
    <init-param>
      <param-name>contextConfigLocation</param-name>
      <param-value> /WEB-INF/minisMVC-servlet.xml </param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
  </servlet>
  <servlet-mapping>
    <servlet-name>minisMVC</servlet-name>
    <url-pattern>/</url-pattern>
  </servlet-mapping>
</web-app>

```

然后是IoC容器使用的配置文件applicationContext.xml。

```plain
<?xml version="1.0" encoding="UTF-8"?>
<beans>
	<bean id="bbs" class="com.test.service.BaseBaseService">
	    <property type="com.test.service.AServiceImpl" name="as" ref="aservice"/>
	</bean>
	<bean id="aservice" class="com.test.service.AServiceImpl">
		<constructor-arg type="String" name="name" value="abc"/>
		<constructor-arg type="int" name="level" value="3"/>
        <property type="String" name="property1" value="Someone says"/>
        <property type="String" name="property2" value="Hello World!"/>
        <property type="com.test.service.BaseService" name="ref1" ref="baseservice"/>
	</bean>
	<bean id="baseservice" class="com.test.service.BaseService">
	</bean>
</beans>

```

MVC扫描的配置文件minisMVC-servlet.xml。

```plain
<?xml version="1.0" encoding="UTF-8" ?>
<components>
<component-scan base-package="com.test"/>
</components>

```

最后，在com.minis.test.HelloworldBean内的测试方法上，增加@RequestMapping注解。

```java
package com.test;

import com.minis.web.RequestMapping;

public class HelloWorldBean {
    @RequestMapping("/test")
    public String doTest() {
        return "hello world for doGet!";
    }
}

```

启动Tomcat进行测试，在浏览器输入框内键入：localhost:8080/test。

注：这个端口号可以自定义，也可依据实际情况在请求路径前增加上下文。

运行成功，学到这里，看到这个结果，你应该很开心吧。

## 小结

这节课，我们把MVC与IoC整合在了一起。具体过程是这样的：在Tomcat启动的过程中先拿context-param，初始化Listener，在初始化过程中，创建IoC容器构建WAC（WebApplicationContext），加载所管理的Bean对象，并把WAC关联到servlet context里。

然后在DispatcherServlet初始化的时候，从sevletContext里获取属性拿到WAC，放到servlet的属性中，然后拿到Servlet的配置路径参数，之后再扫描路径下的包，调用refresh()方法加载Bean，最后配置url mapping。

我们之所以有办法整合这二者，核心的原因是 **Servlet规范中规定的时序**，从listerner到filter再到servlet，每一个环节都预留了接口让我们有机会干预，写入我们需要的代码。我们在学习过程中，更重要的是要学习如何构建可扩展体系的思路，在我们自己的软件开发过程中，记住 **不要将程序流程固定死**，那样没有任何扩展的余地，而应该想着预留出一些接口理清时序，让别人在关节处也可以插入自己的逻辑。

容器是一个框架，之所以叫做框架而不是应用程序，关键就在于这套可扩展的体系，留给其他程序员极大的空间。读Rodd Johnson这些大师的源代码，就像欣赏一本优美的世界名著，每每都会发出“春风大雅能容物，秋水文章不染尘”的赞叹。希望你可以学到其中的精髓。

完整源代码参见 [https://github.com/YaleGuo/minis](https://github.com/YaleGuo/minis)

## 课后题

学完这节课，我也给你留一道思考题。我们看到从Dispatcher 内可访问WebApplicationContext里面管理的Bean，那通过WebApplicationContext 可以访问Dispatcher内管理的Bean吗？欢迎你在留言区和我交流讨论，也欢迎你把这节课分享给需要的朋友。我们下节课见！

# 09｜分解Dispatcher：如何把专门的事情交给专门的部件去做？

你好，我是郭屹。今天我们继续手写MiniSpring。

经过上节课的工作，我们已经实现了IoC与MVC的结合，还定义了Dispatcher与WebApplicationContext两个相对独立又互相关联的结构。

这节课我们计划在已有的ApplicationConfigWebApplicationContext 和DispatcherServlet基础上，把功能做进一步地分解，让Dispatcher只负责解析request请求，用Context 专门用来管理各个Bean。

## 两级ApplicationContext

按照通行的Web分层体系，一个程序它在结构上会有Controller和Service 两层。在我们的程序中，Controller由DispatcherServlet负责启动，Service由Listener负责启动。我们计划把这两部分所对应的容器进行进一步地切割，拆分为XmlWebApplicationContext和AnnotationConfigWebApplicationContext。

首先在 DispatcherServlet 这个类里，增加一个对WebApplicationContext 的引用，命名为parentApplicationContext。这样，当前这个类里就有了两个对WebApplicationContext 的引用。

```java
private WebApplicationContext webApplicationContext;
private WebApplicationContext parentApplicationContext;

```

新增parentApplicationContext 的目的是，把Listener启动的上下文和DispatcherServlet启动的上下文两者区分开来。按照时序关系，Listener启动在前，对应的上下文我们把它叫作parentApplicationContext。

我们调整一下init() 方法。

```java
public void init(ServletConfig config) throws ServletException {
    super.init(config);
    this.parentApplicationContext = (WebApplicationContext)
this.getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION
_CONTEXT_ATTRIBUTE);
    sContextConfigLocation =
config.getInitParameter("contextConfigLocation");

    URL xmlPath = null;
	try {
		xmlPath = this.getServletContext().getResource(sContextConfigLocation);
	} catch (MalformedURLException e) {
		e.printStackTrace();
	}
    this.packageNames = XmlScanComponentHelper.getNodeValue(xmlPath);
    this.webApplicationContext = new
AnnotationConfigWebApplicationContext(sContextConfigLocation,
this.parentApplicationContext);
    Refresh();
}

```

初始化的时候先从ServletContext里拿属性WebApplicationContext.ROOT\_WEB\_APPLICATION\_CONTEXT\_ATTRIBUTE，得到的是前一步Listener存放在这里的那个parentApplicationContext。然后通过contextConfigLocation配置文件，创建一个新的WebApplicationContext。

从上述代码，我们可以发现，里面构建了一个AnnotationConfigWebApplicationContext对象，这个对象的构造函数需要两个参数，一个是配置文件路径，另一个是父上下文。但以前AnnotationConfigWebApplicationContext只有一个参数为String的构造函数。所以这里我们需要扩展改造一下，把DispatcherServlet里一部分和扫描包相关的代码移到AnnotationConfigWebApplicationContext里。你可以看一下修改后的AnnotationConfigWebApplicationContext代码。

```java
package com.minis.web;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;
import com.minis.beans.BeansException;
import com.minis.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import com.minis.beans.factory.config.BeanDefinition;
import com.minis.beans.factory.config.BeanFactoryPostProcessor;
import com.minis.beans.factory.config.ConfigurableListableBeanFactory;
import com.minis.beans.factory.support.DefaultListableBeanFactory;
import com.minis.context.AbstractApplicationContext;
import com.minis.context.ApplicationEvent;
import com.minis.context.ApplicationEventPublisher;
import com.minis.context.ApplicationListener;
import com.minis.context.SimpleApplicationEventPublisher;

public class AnnotationConfigWebApplicationContext
					extends AbstractApplicationContext implements WebApplicationContext{
	private WebApplicationContext parentApplicationContext;
	private ServletContext servletContext;
	DefaultListableBeanFactory beanFactory;
	private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors =
			new ArrayList<BeanFactoryPostProcessor>();

	public AnnotationConfigWebApplicationContext(String fileName) {
		this(fileName, null);
	}
	public AnnotationConfigWebApplicationContext(String fileName, WebApplicationContext parentApplicationContext) {
		this.parentApplicationContext = parentApplicationContext;
		this.servletContext = this.parentApplicationContext.getServletContext();
        URL xmlPath = null;
		try {
			xmlPath = this.getServletContext().getResource(fileName);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

        List<String> packageNames = XmlScanComponentHelper.getNodeValue(xmlPath);
        List<String> controllerNames = scanPackages(packageNames);
    	DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        this.beanFactory = bf;
        this.beanFactory.setParent(this.parentApplicationContext.getBeanFactory());
        loadBeanDefinitions(controllerNames);

        if (true) {
            try {
				refresh();
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
	}
	public void loadBeanDefinitions(List<String> controllerNames) {
        for (String controller : controllerNames) {
            String beanID=controller;
            String beanClassName=controller;
            BeanDefinition beanDefinition=new BeanDefinition(beanID,beanClassName);
            this.beanFactory.registerBeanDefinition(beanID,beanDefinition);
        }
	}
    private List<String> scanPackages(List<String> packages) {
    	List<String> tempControllerNames = new ArrayList<>();
    	for (String packageName : packages) {
    		tempControllerNames.addAll(scanPackage(packageName));
    	}
    	return tempControllerNames;
    }
    private List<String> scanPackage(String packageName) {
    	List<String> tempControllerNames = new ArrayList<>();
        URL url  =this.getClass().getClassLoader().getResource("/"+packageName.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if(file.isDirectory()){
            	scanPackage(packageName+"."+file.getName());
            }else{
                String controllerName = packageName +"." +file.getName().replace(".class", "");
                tempControllerNames.add(controllerName);
            }
        }
        return tempControllerNames;
    }
	public void setParent(WebApplicationContext parentApplicationContext) {
		this.parentApplicationContext = parentApplicationContext;
		this.beanFactory.setParent(this.parentApplicationContext.getBeanFactory());
	}
	public ServletContext getServletContext() {
		return this.servletContext;
	}
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
	public void publishEvent(ApplicationEvent event) {
		this.getApplicationEventPublisher().publishEvent(event);
	}
	public void addApplicationListener(ApplicationListener listener) {
		this.getApplicationEventPublisher().addApplicationListener(listener);
	}
	public void registerListeners() {
		ApplicationListener listener = new ApplicationListener();
		this.getApplicationEventPublisher().addApplicationListener(listener);
	}
	public void initApplicationEventPublisher() {
		ApplicationEventPublisher aep = new SimpleApplicationEventPublisher();
		this.setApplicationEventPublisher(aep);
	}
	public void postProcessBeanFactory(ConfigurableListableBeanFactory bf) {
	}
	public void registerBeanPostProcessors(ConfigurableListableBeanFactory bf) {
		this.beanFactory.addBeanPostProcessor(new AutowiredAnnotationBeanPostProcessor());
	}
	public void onRefresh() {
		this.beanFactory.refresh();
	}
	public void finishRefresh() {
	}
	public ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException {
		return this.beanFactory;
	}
}

```

这段代码的核心是扩充原有的构造方法。通过下面两行代码得到parentApplicationContext和servletContext的引用。

```plain
 this.parentApplicationContext = parentApplicationContext;
 this.servletContext = this.parentApplicationContext.getServletContext();

```

为了兼容原有构造方法，在只有1个参数的时候，给WebApplicationContext传入了一个null。可以看到，修改后的AnnotationConfigWebApplicationContext继承自抽象类AbstractApplicationContext，所以也具备了上下文的通用功能，例如注册监听器、发布事件等。

其次是改造 DefaultListableBeanFactory，因为AnnotationConfigWebApplicationContext里调用了DefaultListableBeanFactory的setParent方法，所以我们需要提供相应的实现方法，你可以看一下相关代码。

```java
    ConfigurableListableBeanFactory parentBeanFactory;

    public void setParent(ConfigurableListableBeanFactory beanFactory) {
        this.parentBeanFactory = beanFactory;
    }

```

接下来我们还要改造XmlWebApplicationContext，在继承ClassPathXmlApplicationContext的基础上实现WebApplicationContext接口，基本上我们可以参考AnnotationConfigWebApplicationContext来实现。

```java
package com.minis.web;

import javax.servlet.ServletContext;
import com.minis.context.ClassPathXmlApplicationContext;

public class XmlWebApplicationContext
					extends ClassPathXmlApplicationContext implements WebApplicationContext{
	private ServletContext servletContext;

	public XmlWebApplicationContext(String fileName) {
		super(fileName);
	}

	public ServletContext getServletContext() {
		return this.servletContext;
	}
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
}

```

到这里，我们就进一步拆解了DispatcherServlet，拆分出两级ApplicationContext，当然启动过程还是由Listener来负责。所以最后ContextLoaderListener初始化时是创建XmlWebApplicationContext对象。

```java
WebApplicationContext wac = new XmlWebApplicationContext(sContextLocation);

```

到这里，Web环境下的两个ApplicationContext都构建完毕了，WebApplicationContext持有对parentApplicationContext的单向引用。当调用getBean()获取Bean时，先从WebApplicationContext中获取，若为空则通过parentApplicationContext获取，你可以看一下代码。

```java
    public Object getBean(String beanName) throws BeansException {
        Object result = super.getBean(beanName);
        if (result == null) {
            result = this.parentBeanFactory.getBean(beanName);
        }
        return result;
    }

```

## 抽取调用方法

拆解的工作还要继续进行，基本的思路是将专业事情交给不同的专业部件来做，我们来看看还有哪些工作是可以分出来的。从代码可以看到现在doGet()方法是这样实现的。

```plain
	Method method = this.mappingMethods.get(sPath);
	obj = this.mappingObjs.get(sPath);
	objResult = method.invoke(obj);
	response.getWriter().append(objResult.toString());

```

这个程序就是简单地根据URL找到对应的方法和对象，然后通过反射调用方法，最后把方法执行的返回值写到response里。我们考虑把通过URL映射到某个实例方法的过程抽取出来，还要考虑把对方法的调用也单独抽取出来。仿照Spring框架，我们新增RequestMappingHandlerMapping与RequestMappingHandlerAdapter，分别对应这两个独立的部件。

首先将HandlerMapping与HandlerAdapter抽象出来，定义接口，然后基于接口来编程。

```java
package com.minis.web.servlet;

import javax.servlet.http.HttpServletRequest;

public interface HandlerMapping {
	HandlerMethod getHandler(HttpServletRequest request) throws Exception;
}

package com.minis.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HandlerAdapter {
	void handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;
}

```

其中可以看到，HandlerMapping中定义的getHandler方法参数是http request，返回一个HandlerMethod对象，这个地方就是封装的这种映射关系。你可以看一下HandlerMethod对象的定义。

```java
package com.minis.web.servlet;

import java.lang.reflect.Method;

public class HandlerMethod {
	private  Object bean;
	private  Class<?> beanType;
	private  Method method;
	private  MethodParameter[] parameters;
	private  Class<?> returnType;
	private  String description;
	private  String className;
	private  String methodName;

	public HandlerMethod(Method method, Object obj) {
		this.setMethod(method);
		this.setBean(obj);
	}
	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	}
	public Object getBean() {
		return bean;
	}
	public void setBean(Object bean) {
		this.bean = bean;
	}
}

```

接下来增加一个MappingRegistry类，这个类有三个属性：urlMappingNames、mappingObjs和mappingMethods，用来存储访问的URL名称与对应调用方法及Bean实例的关系。你可以看一下相关定义。

```java
package com.minis.web.servlet;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappingRegistry {
    private List<String> urlMappingNames = new ArrayList<>();
    private Map<String,Object> mappingObjs = new HashMap<>();
    private Map<String,Method> mappingMethods = new HashMap<>();

	public List<String> getUrlMappingNames() {
		return urlMappingNames;
	}
	public void setUrlMappingNames(List<String> urlMappingNames) {
		this.urlMappingNames = urlMappingNames;
	}
	public Map<String,Object> getMappingObjs() {
		return mappingObjs;
	}
	public void setMappingObjs(Map<String,Object> mappingObjs) {
		this.mappingObjs = mappingObjs;
	}
	public Map<String,Method> getMappingMethods() {
		return mappingMethods;
	}
	public void setMappingMethods(Map<String,Method> mappingMethods) {
		this.mappingMethods = mappingMethods;
	}
}

```

通过上面的代码可以看出，这三个属性以前其实都已经存在了，是定义在DispatcherServlet里的，现在换一个位置，通过MappingRegistry这个单独的部件来存放和管理这个映射关系。

好了，有了这些准备之后，我们来看RequestMappingHandlerMapping的实现，它要实现HandlerMapping 接口，初始化过程就是遍历WAC中已经注册的所有的Bean，并处理带有@RequestMapping注解的类，使用mappingRegistry存储URL地址与方法和实例的映射关系。对外它要实现getHandler()方法，通过URL拿到method的调用。

相关源代码如下：

```java
package com.minis.web.servlet;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import com.minis.beans.BeansException;
import com.minis.web.RequestMapping;
import com.minis.web.WebApplicationContext;

public class RequestMappingHandlerMapping implements HandlerMapping{
    WebApplicationContext wac;
    private final MappingRegistry mappingRegistry = new MappingRegistry();
    public RequestMappingHandlerMapping(WebApplicationContext wac) {
        this.wac = wac;
        initMapping();
    }
    //建立URL与调用方法和实例的映射关系，存储在mappingRegistry中
    protected void initMapping() {
        Class<?> clz = null;
        Object obj = null;
        String[] controllerNames = this.wac.getBeanDefinitionNames();
        //扫描WAC中存放的所有bean
        for (String controllerName : controllerNames) {
            try {
                clz = Class.forName(controllerName);
                obj = this.wac.getBean(controllerName);
            } catch (Exception e) {
				e.printStackTrace();
			}
            Method[] methods = clz.getDeclaredMethods();
            if (methods != null) {
                //检查每一个方法声明
                for (Method method : methods) {
                    boolean isRequestMapping =
method.isAnnotationPresent(RequestMapping.class);
                    //如果该方法带有@RequestMapping注解,则建立映射关系
                    if (isRequestMapping) {
                        String methodName = method.getName();
                        String urlmapping =
method.getAnnotation(RequestMapping.class).value();

                        this.mappingRegistry.getUrlMappingNames().add(urlmapping);
                        this.mappingRegistry.getMappingObjs().put(urlmapping,
obj);
                        this.mappingRegistry.getMappingMethods().put(urlmapping,
method);
                    }
                }
            }
        }
    }

    //根据访问URL查找对应的调用方法
    public HandlerMethod getHandler(HttpServletRequest request) throws Exception
{
        String sPath = request.getServletPath();
		if (!this.mappingRegistry.getUrlMappingNames().contains(sPath)) {
			return null;
		}
        Method method = this.mappingRegistry.getMappingMethods().get(sPath);
        Object obj = this.mappingRegistry.getMappingObjs().get(sPath);
        HandlerMethod handlerMethod = new HandlerMethod(method, obj);
        return handlerMethod;
    }
}

```

这样我们就得到了独立的RequestMappingHandlerMapping部件，把以前写在DispatcherServlet里的代码移到这里来了。

接下来就轮到RequestMappingHandlerAdapter的实现了，它要实现HandlerAdapter接口，主要就是实现handle()方法，基本过程是接受前端传request、 response与handler，通过反射中的invoke调用方法并处理返回数据。

相关源代码如下：

```java
package com.minis.web.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.minis.web.WebApplicationContext;

public class RequestMappingHandlerAdapter implements HandlerAdapter {
	WebApplicationContext wac;

	public RequestMappingHandlerAdapter(WebApplicationContext wac) {
		this.wac = wac;
	}

	public void handle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		handleInternal(request, response, (HandlerMethod) handler);
	}
	private void handleInternal(HttpServletRequest request, HttpServletResponse response,
			HandlerMethod handler) {
		Method method = handler.getMethod();
		Object obj = handler.getBean();
		Object objResult = null;
		try {
			objResult = method.invoke(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			response.getWriter().append(objResult.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

```

重点看一下handleInternal()方法就知道了，这里就是简单地通过反射调用某个方法，然后把返回值写到response里。这些程序代码以前就有，只不过现在移到单独的这个部件中了。

最后需要修改DispatcherServlet中的实现，相关代码移走，放到了上面的两个部件中。所以在DispatcherServlet类中需要增加对HandlerMapping与HandlerAdapter的引用，在初始化方法refresh()中增加initHandlerMapping 与initHandlerAdapter两个方法，为引用的HandlerMapping与HandlerAdapter赋值。

你可以看下DispatcherServlet的refresh()的改造结果。

```plain
refresh()	{
    	initController();

		initHandlerMappings(this.webApplicationContext);
		initHandlerAdapters(this.webApplicationContext);
}

```

初始化这两个部件的代码如下：

```plain
    protected void initHandlerMappings(WebApplicationContext wac) {
    	this.handlerMapping = new RequestMappingHandlerMapping(wac);
    }
    protected void initHandlerAdapters(WebApplicationContext wac) {
    	this.handlerAdapter = new RequestMappingHandlerAdapter(wac);
    }

```

DispatcherServlet的分发过程也要改造一下，不再通过doGet()方法了，而是通过重写的service方法来实现的，而service方法则调用了doDispatch方法，这个方法内部通过handlerMapping获取到对应handlerMethod，随后通过HandlerAdapter进行处理，你可以看一下这个类修改后的源代码。

```java
protected void service(HttpServletRequest request, HttpServletResponse
response) {
    request.setAttribute(WEB_APPLICATION_CONTEXT_ATTRIBUTE,
this.webApplicationContext);
	try {
		doDispatch(request, response);
	} catch (Exception e) {
		e.printStackTrace();
	}
	finally {
	}
}
protected void doDispatch(HttpServletRequest request, HttpServletResponse
response) throws Exception{
    HttpServletRequest processedRequest = request;
    HandlerMethod handlerMethod = null;
    handlerMethod = this.handlerMapping.getHandler(processedRequest);
    if (handlerMethod == null) {
		return;
	}
    HandlerAdapter ha = this.handlerAdapter;
    ha.handle(processedRequest, response, handlerMethod);
}

```

可以看到，经过这么一改造，相比之前DispatcherServlet的代码简化了很多，并且当前业务程序不用再固定写死在doGet()方法里面，可以按照自身的业务需求随意使用任何方法名，也为今后提供多种请求方式，例如POST、PUT、DELETE等提供了便利。

以前，用原始的Servlet规范，我们的业务逻辑全部写在doGet()、doPost()等方法中，每一个业务逻辑程序都是一个独立的Servlet。现在经过我们这几节课的操作，整个系统用一个唯一的DispatcherServlet来拦截请求，并根据注解，定位需要调用的方法，我们就能够更加专注于本身业务代码的实现。这种我们称之为Dispatcher的设计模式也是要用心学习的。

## 小结

这节课我们的主要工作就是拆解Dispatcher。首先拆解的是ApplicationContext，现在我们有了两级上下文，一级用于IoC容器，我们叫parent上下文，一级用于Web上下文，WebApplicationContext持有对parent上下文的引用。方便起见，我们还增加了@RequestMapping注解来声明URL映射，然后新增RequestMappingHandlerMapping 与RequestMappingHandlerAdapter，分别包装URL映射关系和映射后的处理过程。

通过这些拆解工作，我们就把DispatcherServlet的功能进行了分治，把专门的事情交给专门的部件去完成，有利于今后的扩展。

完整源代码参见 [https://github.com/YaleGuo/minis](https://github.com/YaleGuo/minis)

## 课后题

学完这节课，我也给你留一道思考题。目前，我们只支持了GET方法，你能不能尝试自己增加POST方法。想一想，需要改变现有的程序结构吗？欢迎你在留言区和我交流讨论，也欢迎你把这节课分享给需要的朋友。我们下节课见！

# 10｜数据绑定: 如何自动转换传入的参数？

你好，我是郭屹。今天我们继续手写MiniSpring，这节课我们讨论传入参数的转换问题。

上节课，我们已经基本完成了对Dispatcher的扩展，用HanderMapping来处理映射关系，用HandlerAdapter来处理映射后具体方法的调用。

在处理请求的过程中，我们用ServletRequest接收请求参数，而获取参数用的是getParameter()方法，它的返回值是String字符串，这也意味着无论是获取字符串参数、数字参数，还是布尔型参数，它获取到的返回值都是字符串。而如果要把请求参数转换成Java对象，就需要再处理，那么每一次获取参数后，都需要显式地编写大量重复代码，把String类型的参数转换成其他类型。显然这不符合我们对框架的期望，我们希望框架能帮助我们自动处理这些常规数据格式的转换。

再扩大到整个访问过程，后端处理完毕后，返回给前端的数据再做返回，也存在格式转换的问题，传入传出两个方向我们都要处理。而这节课我们讨论的重点是“传入”方向。

## 传入参数的绑定

我们先考虑传入方向的问题：请求参数怎么和Java对象里的属性进行自动映射？

这里，我们引入WebDataBinder来处理。这个类代表的是一个内部的目标对象，用于将Request请求内的字符串参数转换成不同类型的参数，来进行适配。所以比较自然的想法是这个类里面要持有一个目标对象target，然后还要定义一个bind()方法，通过来绑定参数和目标对象，这是WebDataBinder里的核心。

```java
    public void bind(HttpServletRequest request) {
        PropertyValues mpvs = assignParameters(request);
        addBindValues(mpvs, request);
        doBind(mpvs);
    }

```

通过bind方法的实现，我们可以看出，它主要做了三件事。

1. 把Request里的参数解析成PropertyValues。
2. 把Request里的参数值添加到绑定参数中。
3. 把两者绑定在一起。

你可以看一下WebDataBinder的详细实现。

```java
package com.minis.web;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import com.minis.beans.PropertyValues;
import com.minis.util.WebUtils;

public class WebDataBinder {
    private Object target;
    private Class<?> clz;
    private String objectName;
    public WebDataBinder(Object target) {
        this(target, "");
    }
    public WebDataBinder(Object target, String targetName) {
        this.target = target;
        this.objectName = targetName;
        this.clz = this.target.getClass();
    }
    //核心绑定方法，将request里面的参数值绑定到目标对象的属性上
    public void bind(HttpServletRequest request) {
        PropertyValues mpvs = assignParameters(request);
        addBindValues(mpvs, request);
        doBind(mpvs);
    }
    private void doBind(PropertyValues mpvs) {
        applyPropertyValues(mpvs);
    }
    //实际将参数值与对象属性进行绑定的方法
    protected void applyPropertyValues(PropertyValues mpvs) {
        getPropertyAccessor().setPropertyValues(mpvs);
    }
    //设置属性值的工具
    protected BeanWrapperImpl getPropertyAccessor() {
        return new BeanWrapperImpl(this.target);
    }
    //将Request参数解析成PropertyValues
    private PropertyValues assignParameters(HttpServletRequest request) {
        Map<String, Object> map = WebUtils.getParametersStartingWith(request, "");
        return new PropertyValues(map);
    }
    protected void addBindValues(PropertyValues mpvs, HttpServletRequest request) {
    }
}

```

从这个实现方法里可以看出，先是调用了assignParameters()，把Request里的参数换成内存里的一个map对象，这一步用到了底层的WebUtils工具类，这个转换对我们来说比较简单。而最核心的方法是getPropertyAccessor().setPropertyValues(mpvs);，这个getPropertyAccessor则是内置了一个BeanWrapperImpl对象，内部包含了target。由名字可以看出它是Bean的包装实现类，把属性map绑定到目标对象上去。

有了这个大流程，我们再来探究一下一个具体的参数是如何转换的，我们知道Request的转换都是从字符串转为其他类型，所以我们可以定义一个通用接口，名叫PropertyEditor，内部提供一些方法可以让字符串和Obejct之间进行双向灵活转换。

```java
package com.minis.beans;

public interface PropertyEditor {
    void setAsText(String text);
    void setValue(Object value);
    Object getValue();
    Object getAsText();
}

```

现在我们来定义两个PropertyEditor的实现类：CustomNumberEditor和StringEditor，分别处理Number类型和其他类型，并进行类型转换。你可以看一下CustomNumberEditor的相关源码。

```java
package com.minis.beans;

import java.text.NumberFormat;
import com.minis.util.NumberUtils;
import com.minis.util.StringUtils;

public class CustomNumberEditor implements PropertyEditor{
    private Class<? extends Number> numberClass; //数据类型
    private NumberFormat numberFormat; //指定格式
    private boolean allowEmpty;
    private Object value;
    public CustomNumberEditor(Class<? extends Number> numberClass, boolean allowEmpty) throws IllegalArgumentException {
        this(numberClass, null, allowEmpty);
    }
    public CustomNumberEditor(Class<? extends Number> numberClass, NumberFormat numberFormat, boolean allowEmpty) throws IllegalArgumentException {
        this.numberClass = numberClass;
        this.numberFormat = numberFormat;
        this.allowEmpty = allowEmpty;
    }
    //将一个字符串转换成number赋值
    public void setAsText(String text) {
		if (this.allowEmpty && !StringUtils.hasText(text)) {
			setValue(null);
		}
		else if (this.numberFormat != null) {
			// 给定格式
			setValue(NumberUtils.parseNumber(text, this.numberClass, this.numberFormat));
		}
		else {
			setValue(NumberUtils.parseNumber(text, this.numberClass));
		}
    }
    //接收Object作为参数
    public void setValue(Object value) {
        if (value instanceof Number) {
            this.value = (NumberUtils.convertNumberToTargetClass((Number) value, this.numberClass));
        }
        else {
            this.value = value;
        }
    }
    public Object getValue() {
        return this.value;
    }
    //将number表示成格式化串
    public Object getAsText() {
        Object value = this.value;
		if (value == null) {
			return "";
		}
		if (this.numberFormat != null) {
			// 给定格式.
			return this.numberFormat.format(value);
		}
		else {
			return value.toString();
		}
    }
}

```

整体实现也比较简单，在内部定义一个名为value的域，接收传入的格式化text或者value值。如果遇到的值是Number类型的子类，比较简单，就进行强制转换。这里我们用到了一个底层工具类NumberUtils，它提供了一个NumberUtils.parseNumber(text, this.numberClass, this.numberFormat)方法，方便我们在数值和文本之间转换。

你可以看下StringEditor实现的相关源代码。

```java
package com.minis.beans;

import java.text.NumberFormat;
import com.minis.util.NumberUtils;
import com.minis.util.StringUtils;

public class StringEditor implements PropertyEditor{
    private Class<String> strClass;
    private String strFormat;
    private boolean allowEmpty;
    private Object value;
    public StringEditor(Class<String> strClass,
                        boolean allowEmpty) throws IllegalArgumentException {
         this(strClass, "", allowEmpty);
    }
    public StringEditor(Class<String> strClass,
                        String strFormat, boolean allowEmpty) throws IllegalArgumentException {
        this.strClass = strClass;
        this.strFormat = strFormat;
        this.allowEmpty = allowEmpty;
    }
    public void setAsText(String text) {
        setValue(text);
    }
    public void setValue(Object value) {
        this.value = value;
    }
    public String getAsText() {
        return value.toString();
    }
    public Object getValue() {
        return this.value;
    }
}

```

StringEditor的实现类就更加简单了，因为它是字符串本身的处理，但它的构造函数有些不一样，支持传入字符串格式strFormat，这也是为后续类型转换格式留了一个“口子”。

有了两个基本类型的Editor作为工具，现在我们再来看关键的类BeanWapperImpl的实现。

```java
package com.minis.web;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.minis.beans.PropertyEditor;
import com.minis.beans.PropertyEditorRegistrySupport;
import com.minis.beans.PropertyValue;
import com.minis.beans.PropertyValues;

public class BeanWrapperImpl extends PropertyEditorRegistrySupport {
    Object wrappedObject; //目标对象
    Class<?> clz;
    PropertyValues pvs; //参数值
    public BeanWrapperImpl(Object object) {
        registerDefaultEditors(); //不同数据类型的参数转换器editor
        this.wrappedObject = object;
        this.clz = object.getClass();
    }
    public void setBeanInstance(Object object) {
        this.wrappedObject = object;
    }
    public Object getBeanInstance() {
        return wrappedObject;
    }
    //绑定参数值
    public void setPropertyValues(PropertyValues pvs) {
        this.pvs = pvs;
        for (PropertyValue pv : this.pvs.getPropertyValues()) {
          setPropertyValue(pv);
        }
    }
    //绑定具体某个参数
    public void setPropertyValue(PropertyValue pv) {
        //拿到参数处理器
        BeanPropertyHandler propertyHandler = new BeanPropertyHandler(pv.getName());
        //找到对该参数类型的editor
        PropertyEditor pe = this.getDefaultEditor(propertyHandler.getPropertyClz());
        //设置参数值
        pe.setAsText((String) pv.getValue());
        propertyHandler.setValue(pe.getValue());
    }
    //一个内部类，用于处理参数，通过getter()和setter()操作属性
    class BeanPropertyHandler {
        Method writeMethod = null;
        Method readMethod = null;
        Class<?> propertyClz = null;
        public Class<?> getPropertyClz() {
            return propertyClz;
        }
        public BeanPropertyHandler(String propertyName) {
			try {
                //获取参数对应的属性及类型
                Field field = clz.getDeclaredField(propertyName);
                propertyClz = field.getType();
                //获取设置属性的方法，按照约定为setXxxx（）
                this.writeMethod = clz.getDeclaredMethod("set" +
    propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1), propertyClz);
                //获取读属性的方法，按照约定为getXxxx（）
                this.readMethod = clz.getDeclaredMethod("get" +
    propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1), propertyClz);
            } catch (Exception e) {
				e.printStackTrace();
			}        }
        //调用getter读属性值
        public Object getValue() {
            Object result = null;
            writeMethod.setAccessible(true);
			try {
                result = readMethod.invoke(wrappedObject);
			} catch (Exception e) {
				e.printStackTrace();
			}
            return result;
        }
        //调用setter设置属性值
        public void setValue(Object value) {
            writeMethod.setAccessible(true);
			try {
                writeMethod.invoke(wrappedObject, value);
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
    }
}

```

这个类的核心在于利用反射对Bean属性值进行读写，具体是通过setter和getter方法。但具体的实现，则有赖于继承的PropertyEditorRegistrySupport这个类。我们再来看看PropertyEditorRegistrySupport是如何实现的。

```java
package com.minis.beans;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class PropertyEditorRegistrySupport {
    private Map<Class<?>, PropertyEditor> defaultEditors;
    private Map<Class<?>, PropertyEditor> customEditors;
    //注册默认的转换器editor
    protected void registerDefaultEditors() {
        createDefaultEditors();
    }
    //获取默认的转换器editor
    public PropertyEditor getDefaultEditor(Class<?> requiredType) {
        return this.defaultEditors.get(requiredType);
    }
    //创建默认的转换器editor，对每一种数据类型规定一个默认的转换器
    private void createDefaultEditors() {
        this.defaultEditors = new HashMap<>(64);
        // Default instances of collection editors.
        this.defaultEditors.put(int.class, new CustomNumberEditor(Integer.class, false));
        this.defaultEditors.put(Integer.class, new CustomNumberEditor(Integer.class, true));
        this.defaultEditors.put(long.class, new CustomNumberEditor(Long.class, false));
        this.defaultEditors.put(Long.class, new CustomNumberEditor(Long.class, true));
        this.defaultEditors.put(float.class, new CustomNumberEditor(Float.class, false));
        this.defaultEditors.put(Float.class, new CustomNumberEditor(Float.class, true));
        this.defaultEditors.put(double.class, new CustomNumberEditor(Double.class, false));
        this.defaultEditors.put(Double.class, new CustomNumberEditor(Double.class, true));
        this.defaultEditors.put(BigDecimal.class, new CustomNumberEditor(BigDecimal.class, true));
        this.defaultEditors.put(BigInteger.class, new CustomNumberEditor(BigInteger.class, true));
        this.defaultEditors.put(String.class, new StringEditor(String.class, true));
    }
    //注册客户化转换器
    public void registerCustomEditor(Class<?> requiredType, PropertyEditor propertyEditor) {
        if (this.customEditors == null) {
            this.customEditors = new LinkedHashMap<>(16);
        }
        this.customEditors.put(requiredType, propertyEditor);
    }
    //查找客户化转换器
    public PropertyEditor findCustomEditor(Class<?> requiredType) {
        Class<?> requiredTypeToUse = requiredType;
        return getCustomEditor(requiredTypeToUse);
    }
    public boolean hasCustomEditorForElement(Class<?> elementType) {
        return (elementType != null && this.customEditors != null && this.customEditors.containsKey(elementType));
    }
    //获取客户化转换器
    private PropertyEditor getCustomEditor(Class<?> requiredType) {
        if (requiredType == null || this.customEditors == null) {
            return null;
        }
        PropertyEditor editor = this.customEditors.get(requiredType);
        return editor;
    }
}

```

从这段源码里可以看到，PropertyEditorRegistrySupport 的核心实现是createDefaultEditors方法，它里面内置了大量基本类型或包装类型的转换器Editor，还定义了可以定制化的转换器Editor，这也是WebDataBinder能做不同类型转换的原因。不过我们目前的实现，只支持数字和字符串几个基本类型的转换，暂时不支持数组、列表、map等格式。

现在，我们已经实现了一个完整的WebDataBinder，用来绑定数据。我们接下来将提供一个WebDataBinderFactory，能够更方便、灵活地操作WebDataBinder。

```java
package com.minis.web;

import javax.servlet.http.HttpServletRequest;

public class WebDataBinderFactory {
    public WebDataBinder createBinder(HttpServletRequest request, Object target, String objectName) {
        WebDataBinder wbd = new WebDataBinder(target, objectName);
        initBinder(wbd, request);
        return wbd;
    }
    protected void initBinder(WebDataBinder dataBinder, HttpServletRequest request) {
    }
}

```

有了上面一系列工具之后，我们看怎么使用它们进行数据绑定。从前面的讲解中我们已经知道，这个 HTTP Request请求最后会找到映射的方法上，也就是通过RequestMappingHandlerAdapter里提供的handleInternal 方法，来调用invokeHandlerMethod 方法，所以我们从这个地方切入，改造 invokeHandlerMethod 方法，实现参数绑定。

```java
    protected void invokeHandlerMethod(HttpServletRequest request,
HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
        WebDataBinderFactory binderFactory = new WebDataBinderFactory();
        Parameter[] methodParameters =
handlerMethod.getMethod().getParameters();
        Object[] methodParamObjs = new Object[methodParameters.length];
        int i = 0;
        //对调用方法里的每一个参数，处理绑定
        for (Parameter methodParameter : methodParameters) {
            Object methodParamObj = methodParameter.getType().newInstance();
            //给这个参数创建WebDataBinder
            WebDataBinder wdb = binderFactory.createBinder(request,
methodParamObj, methodParameter.getName());
            wdb.bind(request);
            methodParamObjs[i] = methodParamObj;
            i++;
        }
        Method invocableMethod = handlerMethod.getMethod();
        Object returnObj = invocableMethod.invoke(handlerMethod.getBean(), methodParamObjs);
        response.getWriter().append(returnObj.toString());
    }

```

在invokeHandlerMethod 方法的实现代码中，methodParameters 变量用来存储调用方法的所有参数，针对它们进行循环，还有一个变量methodParamObj，是一个新创建的空对象，也是我们需要进行绑定操作的目标，binderFactory.createBinder则是创建了WebDtaBinder，对目标对象进行绑定。整个循环结束之后，Request里面的参数就绑定了调用方法里的参数，之后就可以被调用。

我们从这个绑定过程中可以看到，循环过程就是按照参数在方法中出现的次序逐个绑定的，所以这个次序是很重要的。

## 客户化转换器

现在我们已经实现了Request数据绑定过程，也提供了默认的CustomNumberEditor和StringEditor，来进行数字和字符串两种类型的转换，从而把ServletRequest里的请求参数转换成Java对象里的数据类型。但这种默认的方式比较固定，如果你希望转换成自定义的类型，那么原有的两个Editor就没办法很好地满足需求了。

因此我们要继续探讨，如何支持自定义的Editor，让我们的框架具有良好的扩展性。其实上面我们看到PropertyEditorRegistrySupport里，已经提前准备好了客户化转换器的地方，你可以看下代码。

```java
public class PropertyEditorRegistrySupport {
    private Map<Class<?>, PropertyEditor> defaultEditors;
    private Map<Class<?>, PropertyEditor> customEditors;

```

我们利用客户化Editor这个“口子”，新建一个部件，把客户自定义的Editor注册进来就可以了。

我们先在原有的WebDataBinder 类里，增加registerCustomEditor方法，用来注册自定义的Editor，你可以看一下相关代码。

```java
public void registerCustomEditor(Class<?> requiredType, PropertyEditor propertyEditor) {
      getPropertyAccessor().registerCustomEditor(requiredType, propertyEditor);
}

```

在这里，可以自定义属于我们自己的CustomEditor ，比如在com.test 包路径下，自定义CustomDateEditor，这是一个自定义的日期格式处理器，来配合我们的测试。

```java
package com.test;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import com.minis.beans.PropertyEditor;
import com.minis.util.NumberUtils;
import com.minis.util.StringUtils;

public class CustomDateEditor implements PropertyEditor {
    private Class<Date> dateClass;
    private DateTimeFormatter datetimeFormatter;
    private boolean allowEmpty;
    private Date value;
	public CustomDateEditor() throws IllegalArgumentException {
		this(Date.class, "yyyy-MM-dd", true);
	}
	public CustomDateEditor(Class<Date> dateClass) throws IllegalArgumentException {
		this(dateClass, "yyyy-MM-dd", true);
	}
	public CustomDateEditor(Class<Date> dateClass,
				  boolean allowEmpty) throws IllegalArgumentException {
		this(dateClass, "yyyy-MM-dd", allowEmpty);
	}
	public CustomDateEditor(Class<Date> dateClass,
				String pattern, boolean allowEmpty) throws IllegalArgumentException {
		this.dateClass = dateClass;
		this.datetimeFormatter = DateTimeFormatter.ofPattern(pattern);
		this.allowEmpty = allowEmpty;
	}
    public void setAsText(String text) {
		if (this.allowEmpty && !StringUtils.hasText(text)) {
			setValue(null);
		}
		else {
			LocalDate localdate = LocalDate.parse(text, datetimeFormatter);
			setValue(Date.from(localdate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
		}
    }
    public void setValue(Object value) {
            this.value = (Date) value;
    }
    public String getAsText() {
        Date value = this.value;
		if (value == null) {
			return "";
		}
		else {
			LocalDate localDate = value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			return localDate.format(datetimeFormatter);
		}
    }
    public Object getValue() {
        return this.value;
    }
}

```

程序也比较简单，用DateTimeFormatter来转换字符串和日期就可以了。

接下来我们定义一个WebBindingInitializer，其中有一个initBinder实现方法，为自定义的CustomEditor注册做准备。

```java
public interface WebBindingInitializer {
    void initBinder(WebDataBinder binder);
}

```

下面，我们再实现WebBindingInitializer接口，在实现方法initBinder里，注册自定义的CustomDateEditor，你可以看下相关代码。

```java
package com.test;

import java.util.Date;
import com.minis.web.WebBindingInitializer;
import com.minis.web.WebDataBinder;

public class DateInitializer implements WebBindingInitializer{
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Date.class, new CustomDateEditor(Date.class,"yyyy-MM-dd", false));
	}
}

```

通过上述实现可以看到，我们自定义了“yyyy-MM-dd”这样一种日期格式，也可以根据具体业务需要，自定义其他日期格式。

然后，我们要使用它们，回到RequestMappingHandlerAdapter 这个类里，新增WebBindingInitializer 的属性定义，调整原有的RequestMappingHandlerAdapter(WebApplicationContext wac)这个构造方法的具体实现，你可以看下调整后的代码。

```java
    public RequestMappingHandlerAdapter(WebApplicationContext wac) {         this.wac = wac;
       this.webBindingInitializer = (WebBindingInitializer)
this.wac.getBean("webBindingInitializer");
    }

```

其实也就是增加了webBindingInitializer属性的设置。

然后再利用IoC容器，让这个构造方法，支持用户通过applicationContext.xml 配置webBindingInitializer，我们可以在applicationContext.xml里新增下面这个配置。

```xml
<bean id="webBindingInitializer" class="com.test.DateInitializer">    </bean>

```

最后我们只需要在BeanWrapperImpl 实现类里，修改setPropertyValue(PropertyValue pv)这个方法的具体实现，把最初我们直接获取DefaultEditor的代码，改为先获取CustomEditor ，如果它不存在，再获取DefaultEditor，你可以看下相关实现。

```java
    public void setPropertyValue(PropertyValue pv) {
        BeanPropertyHandler propertyHandler = new BeanPropertyHandler(pv.getName());
        PropertyEditor pe = this.getCustomEditor(propertyHandler.getPropertyClz());
        if (pe == null) {
            pe = this.getDefaultEditor(propertyHandler.getPropertyClz());
        }

        pe.setAsText((String) pv.getValue());
        propertyHandler.setValue(pe.getValue());
}

```

改造后，就能支持用户自定义的CustomEditor ，增强了扩展性。同样的类型，如果既有用户自定义的实现，又有框架默认的实现，那用户自定义的优先。

到这里，传入参数的处理问题我们就探讨完了。

## 小结

这节课，我们重点探讨了MVC里前后端参数的自动转换，把Request里的参数串自动转换成调用方法里的参数对象。

为了完成传入参数的自动绑定，我们使用了WebDataBinder，它内部用BeanWrapperImpl对象，把属性值的map绑定到目标对象上。绑定的过程中，要对每一种数据类型分别进行格式转换，对基本的标准数据类型，由框架给定默认的转换器，但是对于别的数据类型或者是文化差异很大的数据类型，如日期型，我们可以通过CustomEditor机制让用户自定义。

通过数据的自动绑定，我们不用再通过request.getParameter()方法手动获取参数值，再手动转成对象了，这些HTTP请求里的参数值就自动变成了后端方法里的参数对象值，非常便利。实际上后面我们会看到，这种两层之间的数据自动绑定和转换，在许多场景中都非常有用，比如Jdbc Template。所以这节课的内容需要你好好消化，灵活运用。

完整源代码参见 [https://github.com/YaleGuo/minis](https://github.com/YaleGuo/minis)

## 课后题

学完这节课的内容，我也给你留一道思考题。我们现在的实现是把Request里面的参数值，按照内部的次序隐含地自动转成后台调用方法参数对象中的某个属性值，那么可不可以使用一个手段，让程序员手动指定某个调用方法的参数跟哪个Request参数进行绑定呢？欢迎你在留言区和我交流讨论，也欢迎你把这节课分享给需要的朋友。我们下节课见！

# 11｜ModelAndView ：如何将处理结果返回给前端？

你好，我是郭屹。今天我们继续手写MiniSpring。这也是MVC内容的最后一节。

上节课，我们对HTTP请求传入的参数进行了自动绑定，并调用了目标方法。我们再看一下整个MVC的流程，现在就到最后一步了，也就是把返回数据回传给前端进行渲染。

![图片](assets/a51576e7bc6a3dba052274546f5311f3.png)

调用目标方法得到返回值之后，我们有两条路可以返回给前端。第一，返回的是简单的纯数据，第二，返回的是一个页面。

最近几年，第一种情况渐渐成为主流，也就是我们常说的“前后端分离”，后端处理完成后，只是把数据返回给前端，由前端自行渲染界面效果。比如前端用React或者Vue.js自行组织界面表达，这些前端脚本只需要从后端service拿到返回的数据就可以了。

第二种情况，由后端controller根据某种规则拿到一个页面，把数据整合进去，然后整个回传给前端浏览器，典型的技术就是JSP。这条路前些年是主流，最近几年渐渐不流行了。

我们手写MiniSpring的目的是深入理解Spring框架，剖析它的程序结构，所以作为学习的对象，这两种情况我们都会分析到。

## 处理返回数据

和绑定传入的参数相对，处理返回数据是反向的，也就是说，要从后端把方法得到的返回值（一个Java对象）按照某种字符串格式回传给前端。我们以这个@ResponseBody注解为例，来分析一下。

先定义一个接口，增加一个功能，让controller返回给前端的字符流数据可以进行格式转换。

```plain
package com.minis.web;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

public interface HttpMessageConverter {
	void write(Object obj, HttpServletResponse response) throws IOException;
}

```

我们这里给一个默认的实现——DefaultHttpMessageConverter，把Object转成JSON串。

```plain
package com.minis.web;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;

public class DefaultHttpMessageConverter implements HttpMessageConverter {
	String defaultContentType = "text/json;charset=UTF-8";
	String defaultCharacterEncoding = "UTF-8";
	ObjectMapper objectMapper;

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}
	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	public void write(Object obj, HttpServletResponse response) throws IOException {
        response.setContentType(defaultContentType);
        response.setCharacterEncoding(defaultCharacterEncoding);
        writeInternal(obj, response);
        response.flushBuffer();
	}
	private void writeInternal(Object obj, HttpServletResponse response) throws IOException{
		String sJsonStr = this.objectMapper.writeValuesAsString(obj);
		PrintWriter pw = response.getWriter();
		pw.write(sJsonStr);
	}
}

```

这个message converter很简单，就是给response写字符串，用到的工具是ObjectMapper。我们就重点看看这个mapper是怎么做的。

定义一个接口ObjectMapper。

```plain
package com.minis.web;
public interface ObjectMapper {
	void setDateFormat(String dateFormat);
	void setDecimalFormat(String decimalFormat);
	String writeValuesAsString(Object obj);
}

```

最重要的接口方法就是writeValuesAsString()，将对象转成字符串。

我们给一个默认的实现——DefaultObjectMapper，在writeValuesAsString中拼JSON串。

```plain
package com.minis.web;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DefaultObjectMapper implements ObjectMapper{
	String dateFormat = "yyyy-MM-dd";
	DateTimeFormatter datetimeFormatter = DateTimeFormatter.ofPattern(dateFormat);

	String decimalFormat = "#,##0.00";
	DecimalFormat decimalFormatter = new DecimalFormat(decimalFormat);

	public DefaultObjectMapper() {
	}

	@Override
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
		this.datetimeFormatter = DateTimeFormatter.ofPattern(dateFormat);
	}

	@Override
	public void setDecimalFormat(String decimalFormat) {
		this.decimalFormat = decimalFormat;
		this.decimalFormatter = new DecimalFormat(decimalFormat);
	}
	public String writeValuesAsString(Object obj) {
		String sJsonStr = "{";
		Class<?> clz = obj.getClass();

		Field[] fields = clz.getDeclaredFields();
        //对返回对象中的每一个属性进行格式转换
		for (Field field : fields) {
			String sField = "";
			Object value = null;
			Class<?> type = null;
			String name = field.getName();
			String strValue = "";
			field.setAccessible(true);
			value = field.get(obj);
			type = field.getType();

            //针对不同的数据类型进行格式转换
			if (value instanceof Date) {
				LocalDate localDate = ((Date)value).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				strValue = localDate.format(this.datetimeFormatter);
			}
			else if (value instanceof BigDecimal || value instanceof Double || value instanceof Float){
				strValue = this.decimalFormatter.format(value);
			}
			else {
				strValue = value.toString();
			}

            //拼接Json串
			if (sJsonStr.equals("{")) {
				sField = "\"" + name + "\":\"" + strValue + "\"";
			}
			else {
				sField = ",\"" + name + "\":\"" + strValue + "\"";
			}

			sJsonStr += sField;
		}
		sJsonStr += "}";
		return sJsonStr;
	}
}

```

实际转换过程用到了LocalDate和DecimalFormatter。从上述代码中也可以看出，目前为止，我们也只支持Date、Number和String三种类型。你自己可以考虑扩展到更多的数据类型。

那么我们在哪个地方用这个工具来处理返回的数据呢？其实跟绑定参数一样，数据返回之前，也是要经过方法调用。所以我们还是要回到RequestMappingHandlerAdapter这个类，增加一个属性messageConverter，通过它来转换数据。

程序变成了这个样子。

```plain
	public class RequestMappingHandlerAdapter implements HandlerAdapter {
		private WebBindingInitializer webBindingInitializer = null;
		private HttpMessageConverter messageConverter = null;

```

现在既有传入的webBingingInitializer，也有传出的messageConverter。

在关键方法invokeHandlerMethod()里增加对@ResponseBody的处理，也就是调用messageConverter.write()把方法返回值转换成字符串。

```plain
	protected ModelAndView invokeHandlerMethod(HttpServletRequest request,
		HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
		... ...
		if (invocableMethod.isAnnotationPresent(ResponseBody.class)){ //ResponseBody
	        this.messageConverter.write(returnObj, response);
		}
		... ...
	}

```

同样的webBindingInitializer和messageConverter都可以通过配置注入。

```plain
	<bean id="handlerAdapter" class="com.minis.web.servlet.RequestMappingHandlerAdapter">
	 <property type="com.minis.web.HttpMessageConverter" name="messageConverter" ref="messageConverter"/>
	 <property type="com.minis.web.WebBindingInitializer" name="webBindingInitializer" ref="webBindingInitializer"/>
	</bean>

	<bean id="webBindingInitializer" class="com.test.DateInitializer" />

	<bean id="messageConverter" class="com.minis.web.DefaultHttpMessageConverter">
	 <property type="com.minis.web.ObjectMapper" name="objectMapper" ref="objectMapper"/>
	</bean>
	<bean id="objectMapper" class="com.minis.web.DefaultObjectMapper" >
	 <property type="String" name="dateFormat" value="yyyy/MM/dd"/>
	 <property type="String" name="decimalFormat" value="###.##"/>
	</bean>

```

最后在DispatcherServlet里，通过getBean获取handlerAdapter，当然这里需要约定一个名字，整个过程就连起来了。

```plain
	protected void initHandlerAdapters(WebApplicationContext wac) {
 		this.handlerAdapter = (HandlerAdapter) wac.getBean(HANDLER_ADAPTER_BEAN_NAME);
    }

```

测试的客户程序HelloWorldBean修改如下：

```plain
	@RequestMapping("/test7")
	@ResponseBody
	public User doTest7(User user) {
		user.setName(user.getName() + "---");
		user.setBirthday(new Date());
		return user;
	}

```

程序里面声明了一个注解@ResponseBody，程序中返回的是对象User，框架处理的时候用message converter将其转换成JSON字符串返回。

到这里，我们就知道MVC是如何把方法返回对象自动转换成response字符串的了。我们在调用目标方法后，通过messageConverter进行转换，它要分别转换每一种数据类型的格式，同时格式可以由用户自己指定。

## ModelAndView

调用完目标方法，得到返回值，把数据按照指定格式转换好之后，就该处理它们，并把它们送到前端去了。我们用一个统一的结构，包装调用方法之后返回的数据，以及需要启动的前端页面，这个结构就是ModelAndView，我们看下它的定义。

```plain
package com.minis.web.servlet;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {
	private Object view;
	private Map<String, Object> model = new HashMap<>();

	public ModelAndView() {
	}
	public ModelAndView(String viewName) {
		this.view = viewName;
	}
	public ModelAndView(View view) {
		this.view = view;
	}
	public ModelAndView(String viewName, Map<String, ?> modelData) {
		this.view = viewName;
		if (modelData != null) {
			addAllAttributes(modelData);
		}
	}
	public ModelAndView(View view, Map<String, ?> model) {
		this.view = view;
		if (model != null) {
			addAllAttributes(model);
		}
	}
	public ModelAndView(String viewName, String modelName, Object modelObject) {
		this.view = viewName;
		addObject(modelName, modelObject);
	}
	public ModelAndView(View view, String modelName, Object modelObject) {
		this.view = view;
		addObject(modelName, modelObject);
	}
	public void setViewName(String viewName) {
		this.view = viewName;
	}
	public String getViewName() {
		return (this.view instanceof String ? (String) this.view : null);
	}
	public void setView(View view) {
		this.view = view;
	}
	public View getView() {
		return (this.view instanceof View ? (View) this.view : null);
	}
	public boolean hasView() {
		return (this.view != null);
	}
	public boolean isReference() {
		return (this.view instanceof String);
	}
	public Map<String, Object> getModel() {
		return this.model;
	}
	private void addAllAttributes(Map<String, ?> modelData) {
		if (modelData != null) {
			model.putAll(modelData);
		}
	}
	public void addAttribute(String attributeName, Object attributeValue) {
		model.put(attributeName, attributeValue);
	}
	public ModelAndView addObject(String attributeName, Object attributeValue) {
		addAttribute(attributeName, attributeValue);
		return this;
	}
}

```

这个类里面定义了Model和View，分别代表返回的数据以及前端表示，我们这里就是指JSP。

有了这个结构，我们回头看调用目标方法之后返回的那段代码，把类RequestMappingHandlerAdapter的方法invokeHandlerMethod()返回值改为ModelAndView。

```plain
protected ModelAndView invokeHandlerMethod(HttpServletRequest request,
       			HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
	ModelAndView mav = null;
    //如果是ResponseBody注解，仅仅返回值，则转换数据格式后直接写到response
	if (invocableMethod.isAnnotationPresent(ResponseBody.class)){ //ResponseBody
	        this.messageConverter.write(returnObj, response);
	}
	else { //返回的是前端页面
		if (returnObj instanceof ModelAndView) {
			mav = (ModelAndView)returnObj;
		}
		else if(returnObj instanceof String) { //字符串也认为是前端页面
			String sTarget = (String)returnObj;
			mav = new ModelAndView();
			mav.setViewName(sTarget);
		}
	}

	return mav;
}

```

通过上面这段代码我们可以知道，调用方法返回的时候，我们处理了三种情况。

1. 如果声明返回的是ResponseBody，那就用MessageConvert把结果转换一下，之后直接写回response。
2. 如果声明返回的是ModelAndView，那就把结果包装成一个ModelAndView对象返回。
3. 如果声明返回的是字符串，就以这个字符串为目标，最后还是包装成ModelAndView返回。

## View

到这里，调用方法就返回了。不过事情还没完，之后我们就把注意力转移到MVC环节的最后一部分：View层。View，顾名思义，就是负责前端界面展示的部件，当然它最主要的功能就是，把数据按照一定格式显示并输出到前端界面上，因此可以抽象出它的核心方法render()，我们可以看下View接口的定义。

```plain
package com.minis.web.servlet;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface View {
	void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception;
	default String getContentType() {
		return null;
	}
	void setContentType(String contentType);
	void setUrl(String url);
	String getUrl();
	void setRequestContextAttribute(String requestContextAttribute);
	String getRequestContextAttribute();
}

```

这个render()方法的思路很简单，就是获取HTTP请求的request和response，以及中间产生的业务数据Model，最后写到response里面。request和response是HTTP访问时由服务器创建的，ModelAndView是由我们的MiniSpring创建的。

准备好数据之后，我们以JSP为例，来看看怎么把结果显示在前端界面上。其实，这跟我们自己手工写JSP是一样的，先设置属性值，然后把请求转发（forward）出去，就像下面我给出的这几行代码。

```plain
	request.setAttribute(key1, value1);
	request.setAttribute(key2, value2);
	request.getRequestDispatcher(url).forward(request, response);

```

照此办理，DispatcherServlet的doDispatch()方法调用目标方法后，可以通过一个render()来渲染这个JSP，你可以看一下doDispatch()相关代码。

```plain
	HandlerAdapter ha = this.handlerAdapter;
	mv = ha.handle(processedRequest, response, handlerMethod);
	render(processedRequest, response, mv);

```

这个render()方法可以考虑这样实现。

```plain
	//用jsp 进行render
	protected void render( HttpServletRequest request, HttpServletResponse response,ModelAndView mv) throws Exception {
		//获取model，写到request的Attribute中：
		Map<String, Object> modelMap = mv.getModel();
		for (Map.Entry<String, Object> e : modelMap.entrySet()) {
			request.setAttribute(e.getKey(),e.getValue());
		}
        //输出到目标JSP
		String sTarget = mv.getViewName();
		String sPath = "/" + sTarget + ".jsp";
		request.getRequestDispatcher(sPath).forward(request, response);
	}

```

我们看到了，程序从Model里获取数据，并将其作为属性值写到request的attribute里，然后获取页面路径，再显示出来，跟手工写JSP过程一样，简明有效。

但是上面的程序有两个问题，一是这个程序是怎么找到显示目标View的呢？上面的例子，我们是写了一个固定的路径/xxxx.jsp，但实际上这些应该是可以让用户自己来配置的，不应该写死在代码中。二是拿到View后，直接用的是request的forward()方法，这只对JSP有效，没办法扩展到别的页面，比如说Excel、PDF。所以上面的render()是需要改造的。

先解决第一个问题，怎么找到需要显示的目标View? 这里又得引出了一个新的部件ViewResolver，由它来根据某个规则或者是用户配置来确定View在哪里，下面是它的定义。

```plain
package com.minis.web.servlet;

public interface ViewResolver {
	View resolveViewName(String viewName) throws Exception;
}

```

这个ViewResolver就是根据View的名字找到实际的View，有了这个ViewResolver，就不用写死JSP路径，而是可以通过resolveViewName()方法来获取一个View。拿到目标View之后，我们把实际渲染的功能交给View自己完成。我们把程序改成下面这个样子。

```plain
	protected void render( HttpServletRequest request, HttpServletResponse response,ModelAndView mv) throws Exception {
		String sTarget = mv.getViewName();
		Map<String, Object> modelMap = mv.getModel();
		View view = resolveViewName(sTarget, modelMap, request);
		view.render(modelMap, request, response);
	}

```

在MiniSpring里，我们提供一个InternalResourceViewResolver，作为启动JSP的默认实现，它是这样定位到显示目标View的。

```plain
package com.minis.web.servlet.view;

import com.minis.web.servlet.View;
import com.minis.web.servlet.ViewResolver;

public class InternalResourceViewResolver implements ViewResolver{
	private Class<?> viewClass = null;
	private String viewClassName = "";
	private String prefix = "";
	private String suffix = "";
	private String contentType;

	public InternalResourceViewResolver() {
		if (getViewClass() == null) {
			setViewClass(JstlView.class);
		}
	}

	public void setViewClassName(String viewClassName) {
		this.viewClassName = viewClassName;
		Class<?> clz = null;
		try {
			clz = Class.forName(viewClassName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		setViewClass(clz);
	}

	protected String getViewClassName() {
		return this.viewClassName;
	}
	public void setViewClass(Class<?> viewClass) {
		this.viewClass = viewClass;
	}
	protected Class<?> getViewClass() {
		return this.viewClass;
	}
	public void setPrefix(String prefix) {
		this.prefix = (prefix != null ? prefix : "");
	}
	protected String getPrefix() {
		return this.prefix;
	}
	public void setSuffix(String suffix) {
		this.suffix = (suffix != null ? suffix : "");
	}
	protected String getSuffix() {
		return this.suffix;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	protected String getContentType() {
		return this.contentType;
	}

	@Override
	public View resolveViewName(String viewName) throws Exception {
		return buildView(viewName);
	}

	protected View buildView(String viewName) throws Exception {
		Class<?> viewClass = getViewClass();

		View view = (View) viewClass.newInstance();
		view.setUrl(getPrefix() + viewName + getSuffix());

		String contentType = getContentType();
		view.setContentType(contentType);

		return view;
	}
}

```

从代码里可以知道，它先创建View实例，通过配置生成URL定位到显示目标，然后设置ContentType。这个过程也跟我们手工写JSP是一样的。通过这个resolver，就解决了第一个问题，框架会根据配置从/jsp/路径下拿到xxxx.jsp页面。

对于第二个问题，DispatcherServlet是不应该负责实际的渲染工作的，它只负责控制流程，并不知道如何渲染前端，这些工作由具体的View实现类来完成。所以我们不再把request forward()这样的代码写到DispatcherServlet里，而是写到View的render()方法中。

MiniSpring也提供了一个默认的实现：JstlView。

```plain
package com.minis.web.servlet.view;

import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.minis.web.servlet.View;

public class JstlView implements View{
	public static final String DEFAULT_CONTENT_TYPE = "text/html;charset=ISO-8859-1";
	private String contentType = DEFAULT_CONTENT_TYPE;
	private String requestContextAttribute;
	private String beanName;
	private String url;

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getContentType() {
		return this.contentType;
	}
	public void setRequestContextAttribute(String requestContextAttribute) {
		this.requestContextAttribute = requestContextAttribute;
	}
	public String getRequestContextAttribute() {
		return this.requestContextAttribute;
	}
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}
	public String getBeanName() {
		return this.beanName;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUrl() {
		return this.url;
	}
	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		for (Entry<String, ?> e : model.entrySet()) {
			request.setAttribute(e.getKey(),e.getValue());
		}
		request.getRequestDispatcher(getUrl()).forward(request, response);
	}
}

```

从代码里可以看到，程序其实还是一样的，因为要完成的任务是一样的，只不过现在这个代码移到了View这个位置。但是这个位置的移动，就让前端的渲染工作解耦了，DispatcherServlet不负责渲染了，我们可以由此扩展到多种前端，如Excel、PDF等等。

然后，对于InternalResourceViewResolver和JstlView，我们可以再次利用IoC容器机制通过配置进行注入。

```plain
    <bean id="viewResolver" class="com.minis.web.servlet.view.InternalResourceViewResolver" >
	 <property type="String" name="viewClassName" value="com.minis.web.servlet.view.JstlView" />
	 <property type="String" name="prefix" value="/jsp/" />
	 <property type="String" name="suffix" value=".jsp" />
    </bean>

```

当DispatcherServlet初始化的时候，根据配置获取实际的ViewResolver和View。

整个过程就完美结束了。

## 小结

这节课，我们重点探讨了MVC调用目标方法之后的处理过程，如何自动转换数据、如何找到指定的View、如何去渲染页面。我们可以看到，作为一个框架，我们没有规定数据要如何转换格式，而是交给了MessageConverter去做；我们也没有规定如何找到这些目标页面，而是交给了ViewResolver去做；我们同样没有规定如何去渲染前端界面，而是通过View这个接口去做。我们可以自由地实现具体的场景。

这里，我们的重点并不是去看具体代码如何实现，而是要学习Spring框架如何分解这些工作，把专门的事情交给专门的部件去完成。虽然现在已经不流行JSP，我们不用特地去学习它，但是把这些部件解耦的框架思想，却是值得我们好好琢磨的。

完整源代码参见： [https://github.com/YaleGuo/minis](https://github.com/YaleGuo/minis)

## 课后题

学完这节课，我也给你留一道思考题。现在我们返回的数据只支持Date、Number和String三种类型，如何扩展到更多的数据类型？现在也只支持JSP，如何扩展到别的前端？欢迎你在留言区和我交流讨论，也欢迎你把这节课分享给需要的朋友。我们下节课见！

# 12｜再回首：如何实现Spring MVC？

你好，我是郭屹。

恭喜你学完MiniSpring的第二部分——MVC了。你是不是跟着我的脚步自己也实现了一个MVC呢？如果已经完成了，欢迎你把你的实现代码放到评论区，我们一起交流讨论。

为了让你更好地掌握这部分内容，我们来对这一整章做一个重点回顾。

### MVC重点回顾

MVC是Spring框架的核心组成部分之一，负责实现一个Web应用程序的模型视图控制器模式。Spring框架提供了丰富的组件和工具，而MVC负责处理一个Web应用程序中的核心过程，例如请求处理、数据转换、模板渲染和视图管理等。而MVC和Spring的结合，就像是车与引擎的结合一样，给Web应用程序提供了强大而且可靠的性能和灵活性，让我们能够快速、方便地搭建高性能、可靠的Web应用程序。

因为Spring是Java实现的，也因为发明人Rod Johnson先生自己是Java团队Servlet规范的专家组成员，所以很自然，他的MVC的实现是基于Servlet的。利用Servlet的机制，把这种功效发挥到了极致，快速构造了完整的Web程序结构，让我们大开眼界。

### 那我们在课程中是怎么实现MVC的呢？

首先我们利用Servlet机制，用一个单一的Servlet拦截所有请求，由它来分派任务，这样实现了原始的MVC结构。然后呢，我们把MVC和IoC结合在一起，在Servlet容器启动的时候，给上下文环境里注入IoC容器，使得在Servlet里可以访问到IoC容器里的Bean。

之后我们进一步解耦MVC结构，独立出请求处理器，还用一个简洁统一的注解方式，把Web请求方便地定位到后台处理的类和方法里，实现Spring的RequestHandler。

在前后台打通的时候，实现数据参数的自动转换，也就是说先把Web请求的传入参数，自动地从文本转换成对象，实现数据绑定功能。对于返回数据，也自动根据用户需求进行格式化转换，这样实现了Spring里面的data binder和data conversion。最后回到前端View，如果有前端引擎，在Spring中引用，把数据自动渲染到前端。

我们可以利用Servlet机制、MVC结构、IoC容器、RequestHandler和数据绑定等功能，确保前后台的有效沟通和良好的交互体验，实现一个高效可靠的Web应用程序。你学会了吗？

最后，我们再来看一下这一章的几道思考题，我在文稿里都给出了参考答案。我建议你也可以把你的思考分享在留言区，看看自己的思路是不是对的。题目我也就不一道一道地说了，你在做完思考题之后，自己来对比。

### 07｜原始MVC：如何通过单一的Servlet拦截请求分派任务？

#### 思考题

我们在MVC中也使用了Bean这个概念，它跟我们以前章节中的Bean是什么关系？

#### 参考答案

MVC（Model-View-Controller）和IoC（Inversion of Control）是两个不同的设计模式，它们都使用“Bean”这个概念，但是在不同的层级和实现方式上有所不同。

- 在MVC中，“Bean”通常指代模型对象。模型对象是业务逻辑层的核心，用于实现数据访问和业务逻辑处理等功能。在MVC中，模型对象通常是由控制器（Controller）创建并向视图（View）传递的。
- 在IoC中，“Bean”指代由IoC容器管理的对象。IoC容器负责创建及管理应用程序中的所有Bean对象。通过IoC容器，应用程序能够实现“控制反转”，即由IoC容器统一管理和调度应用程序中的各个组件。在IoC中，Bean是由IoC容器创建、初始化、配置和装配的。

因此，尽管MVC和IoC都使用了“Bean”这个概念，但它们的含义及在系统中的作用是不同的。MVC中的Bean一般是Web相关的业务逻辑，IoC中的Bean可能是一些更加基础性的逻辑。从MVC中可以访问到IoC容器中的Bean。

### 08｜整合IoC和MVC：如何在Web环境中启动IoC容器？

#### 思考题

我们看到从Dispatcher里可以访问WebApplicationContext里管理的Bean，那通过 WebApplicationContext 可以访问Dispatcher内管理的Bean吗？

#### 参考答案

不可以。

Servlet容器启动的时候，按照时序，是先启动Listener，在Listener的初始化过程中创建IoC容器，放到ServletContext里，这就是WAC。这之后再初始化的Servlet。所以Dispatcher可以访问到WAC，但是WAC访问不到DispatcherServlet，这个是单向的。

### 09｜分解Dispatcher：如何把专门的事情交给专门的部件去做？

#### 思考题

目前，我们只支持了GET方法，你能不能尝试自己增加POST方法。想一想，需要改变现有的程序结构吗？

#### 参考答案

增加POST方法支持不需要改变现有程序结构。因为我们的DispatcherServlet现在统一用service()方法处理所有请求，之后调用doDispatch()方法，最后通过this.handlerMapping.getHandler()找到需要调用的方法。无论对于GET还是POST，都是同样的流程，统一由handlerMapping来区分不同的调用方法。

所以如果要区分GET和POST，则可以在RequestMapping注解上增加METHOD属性，表示GET还是POST，然后handlerMapping.getHandler()中根据GET和POST匹配实际的调用方法。

### 10｜数据绑定: 如何自动转换传入的参数？

#### 思考题

我们现在的实现是把request里面的参数值，按照内部的次序隐含地自动转成后台调用方法参数对象中的某个属性值，那么可不可以使用一个手段，让程序员手动指定某个调用方法的参数跟哪个request参数进行绑定呢？

#### 参考答案

参数绑定的处理，是在RequestMappingHandlerAdatper的invokeHandlerMethod()方法中处理的，它拿到调用方法的所有参数，一个参数一个参数进行绑定： `WebDataBinder wdb = binderFactory.createBinder(request, methodParamObj, methodParameter.getName());`。所以我们在这里可以考虑给参数增加一个注解@RequestParam。对于带有这个注解的参数，就不是隐含地按照参数名去匹配，而是按照指定的名字去request中匹配。

### 11｜ModelAndView ：如何将处理结果返回到前端？

现在返回的数据只支持Date、Number和String三种类型，如何扩展到更多的数据类型？现在也只支持JSP，如何扩展到别的前端？

#### 参考答案

返回数据的格式处理是通过ObjectMapper来实现的。我们有一个默认实现DefaultObjectMapper，只要在它的writeValuesAsString()里判断数据类型的时候，增加别的类型就可以了。

对于JSP之外的View，我们现在的结构是可扩展的。只要自己另外实现一个View和一个View resolver即可。

# 13｜JDBC访问框架：如何抽取JDBC模板并隔离数据库？

你好，我是郭屹，今天我们继续手写MiniSpring。从这节课开始我们进入MiniSpring一个全新的部分：JdbcTemplate。

到现在为止，我们的MiniSpring已经成了一个相对完整的简易容器，具备了基本的IoC和MVC功能。现在我们就要在这个简易容器的基础之上，继续添加新的特性。首先就是 **数据访问的特性**，这是任何一个应用系统的基本功能，所以我们先实现它。这之后，我们的MiniSpring就基本落地了，你真的可以以它为框架进行编程了。

我们还是先从标准的JDBC程序开始探讨。

## JDBC通用流程

在Java体系中，数据访问的规范是JDBC，也就是Java Database Connectivity，想必你已经熟悉或者至少听说过，一个简单而典型的JDBC程序大致流程是怎样的呢？我们一步步来看，每一步我也会给你放上一两个代码示例帮助你理解。

第一步，加载数据库驱动程序。

```plain
	Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

```

或者直接new Driver();也可以。

这是第一步，因为JDBC只是提供了一个访问的API，具体访问数据库的工作是由不同厂商提供的数据库driver来实现的，Java只是规定了这个通用流程。对同一种数据库，可以有不同的driver，我们也可以自己按照协议实现一个driver，我自己就曾在1996年实现了中国第一个JDBC Driver。

这里我多提一句，Java的这种设计很是巧妙，让应用程序的API与对应厂商的SPI分隔开了，它们可以各自独立进化，这是通过一种叫“桥接模式”的办法达到的。这节课你就能切身感受到这种模式的应用效果了。

第二步，获取数据库连接。

```plain
	con = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databasename=DEMO;user=testuser;password=test;");

```

getConnection()方法的几个参数，分别表示数据库URL、登录数据库的用户名和密码。

这个时候，我们利用底层driver的功能建立了对数据库的连接。不过要注意了，建立和断开连接的过程是很费时间的，所以后面我们会利用数据库连接池技术来提高性能。

第三步，通过Connection对象创建Statement对象，比如下面这两条。

```plain
	stmt = con.createStatement(sql);

```

```plain
	stmt = con.prepareStatement(sql);

```

Statement是对一条SQL命令的包装。

第四步，使用Statement执行SQL语句，还可以获取返回的结果集ResultSet。

```plain
	rs = stmt.executeQuery();

```

```plain
stmt.executeUpdate();

```

第五步，操作ResultSet结果集，形成业务对象，执行业务逻辑。

```plain
	User rtnUser = null;
	if (rs.next()) {
		rtnUser = new User();
		rtnUser.setId(rs.getInt("id"));
		rtnUser.setName(rs.getString("name"));
	}

```

第六步，回收数据库资源，关闭数据库连接，释放资源。

```plain
	rs.close();
	stmt.close();
	con.cloase();

```

这个数据访问的套路或者定式，初学Java的程序员都比较熟悉。写多了JDBC程序，我们会发现Java里面访问数据的程序结构都是类似的，不一样的只是具体的SQL语句，然后还有一点就是执行完SQL语句之后，每个业务对结果的处理是不同的。只要稍微用心思考一下，你就会想到应该把它做成一个模板，方便之后使用，自然会去抽取JdbcTemplate。

## 抽取JdbcTemplate

抽取的基本思路是 **动静分离，将固定的套路作为模板定下来，变化的部分让子类重写**。这是常用的设计模式，基于这个思路，我们考虑提供一个JdbcTemplate抽象类，实现基本的JDBC访问框架。

以数据查询为例，我们可以在这个框架中，让应用程序员传入具体要执行的SQL语句，并把返回值的处理逻辑设计成一个模板方法让应用程序员去具体实现。

```plain
package com.minis.jdbc.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public abstract class JdbcTemplate {
	public JdbcTemplate() {
	}
	public Object query(String sql) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Object rtnObj = null;

		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			con = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databasename=DEMO;user=sa;password=Sql2016;");

			stmt = con.prepareStatement(sql);
			rs = stmt.executeQuery();

			//调用返回数据处理方法，由程序员自行实现
			rtnObj = doInStatement(rs);
		}
		catch (Exception e) {
				e.printStackTrace();
		}
		finally {
			try {
				rs.close();
				stmt.close();
				con.close();
			} catch (Exception e) {
			}
		}
		return rtnObj;
	}

	protected abstract  Object doInStatement(ResultSet rs);
}

```

通过上述代码我们可以看到，query()里面的代码都是模式化的，SQL语句作为参数传进来，最后处理SQL返回数据的业务代码，留给应用程序员自己实现，就是这个模板方法doInStatement()。这样就实现了动静分离。

比如说，我们数据库里有一个数据表User，程序员可以用一个数据访问类UserJdbcImpl进行数据访问，你可以看一下代码。

```plain
package com.test.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.minis.jdbc.core.JdbcTemplate;
import com.test.entity.User;

public class UserJdbcImpl extends JdbcTemplate {
	@Override
	protected Object doInStatement(ResultSet rs) {
        //从jdbc数据集读取数据，并生成对象返回
		User rtnUser = null;
		try {
			if (rs.next()) {
				rtnUser = new User();
				rtnUser.setId(rs.getInt("id"));
				rtnUser.setName(rs.getString("name"));
				rtnUser.setBirthday(new java.util.Date(rs.getDate("birthday").getTime()));
			} else {
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return rtnUser;
	}
}

```

应用程序员在自己实现的doInStatement()里获得SQL语句的返回数据集并进行业务处理，返回一个业务对象给用户类。

而对外提供服务的UserService用户类就可以简化成下面这样。

```plain
package com.test.service;

import com.minis.jdbc.core.JdbcTemplate;
import com.test.entity.User;

public class UserService {
	public User getUserInfo(int userid) {
		String sql = "select id, name,birthday from users where id="+userid;
		JdbcTemplate jdbcTemplate = new UserJdbcImpl();
		User rtnUser = (User)jdbcTemplate.query(sql);

		return rtnUser;
	}
}

```

我们看到，用户类简单地创建一个UserJdbcImpl对象，然后执行query()即可，很简单。

有了这个简单的模板，我们就做到了把JDBC程序流程固化下来，分离出变化的部分，让应用程序员只需要管理SQL语句并处理返回的数据就可以了。

这是一个实用的结构，我们就基于这个结构继续往前走。

## 通过Callback模式简化业务实现类

上面抽取出来的Tempalte，我们也看到了，如果只是停留在现在的这一步，那应用程序的工作量还是很大的，对每一个数据表的访问都要求手写一个对应的JdbcImpl实现子类，很繁琐。为了不让每个实体类都手写一个类似于UserJdbcImpl的类，我们可以采用Callback模式来达到目的。

先介绍一下Callback模式，它是把一个需要被调用的函数作为一个参数传给调用函数。你可以看一下基本的做法。

先定义一个回调接口。

```plain
public interface Callback {
    void call();
}

```

有了这个Callback接口，任务类中可以把它作为参数，比如下面的业务任务代码。

```plain
public class Task {
    public void executeWithCallback(Callback callback) {
        execute(); //具体的业务逻辑处理
        if (callback != null) callback.call();
    }
}

```

这个任务类会先执行具体的业务逻辑，然后调用Callback的回调方法。

用户程序如何使用它呢？

```plain
    public static void main(String[] args) {
        Task task = new Task();
        Callback callback = new Callback() {
            public void call() {
                System.out.println("callback...");
            }
        };
        task.executeWithCallback(callback);
    }

```

先创建一个任务类，然后定义具体的回调方法，最后执行任务的同时将Callback作为参数传进去。这里可以看到，回调接口是一个单一方法的接口，我们可以采用函数式编程进一步简化它。

```plain
    public static void main(String[] args) {
        Task task = new Task();
        task.executeWithCallback(()->{System.out.println("callback;")});
    }

```

上面就是Callback模式的实现，我们把一个回调函数作为参数传给了调用者，调用者在执行完自己的任务后调用这个回调函数。

现在我们就按照这个模式改写JdbcTemplate 的query()方法。

```plain
	public Object query(StatementCallback stmtcallback) {
		Connection con = null;
		Statement stmt = null;

		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			con = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databasename=DEMO;user=sa;password=Sql2016;");

			stmt = con.createStatement();

			return stmtcallback.doInStatement(stmt);
		}
		catch (Exception e) {
				e.printStackTrace();
		}
		finally {
			try {
				stmt.close();
				con.close();
			} catch (Exception e) {
			}
		}
		return null;
	}

```

从代码中可以看出，在query()方法中增加了一个参数：StatementCallback，这就是需要回调的方法。这里我还要提醒你一下，Java是纯粹的面向对象编程，没有真正的全局函数，所以实际代码中是一个类。

有了这个回调参数，就不需要给每一个数据访问增加一个子类来实现doInStatemnt()了，而是作为参数传进去。

你可以看一下Callback接口。

```plain
package com.minis.jdbc.core;

import java.sql.SQLException;
import java.sql.Statement;

public interface StatementCallback {
	Object doInStatement(Statement stmt) throws SQLException;
}

```

可以看出这是一个函数式接口。

现在，应用程序就只需要用一个JdbcTemplate类就可以了，不用再为每一个业务类单独做一个子类。就像我们前面说的，用户类需要使用Callback动态匿名类的方式进行改造。

代码如下：

```plain
	public User getUserInfo(int userid) {
		final String sql = "select id, name,birthday from users where id="+userid;
		return (User)jdbcTemplate.query(
				(stmt)->{
					ResultSet rs = stmt.executeQuery(sql);
					User rtnUser = null;
					if (rs.next()) {
						rtnUser = new User();
						rtnUser.setId(userid);
						rtnUser.setName(rs.getString("name"));
						rtnUser.setBirthday(new java.util.Date(rs.getDate("birthday").getTime()));
					}
					return rtnUser;
				}
		);
	}

```

从代码中可以看到，以前写在UserJdbcImpl里的业务代码，也就是对SQL语句返回值的处理逻辑，现在成了匿名类，作为参数传入query()里，最后在query()里会回调到它。

按照同样的办法我们还可以支持PreparedStatement类型，方法调用时带上SQL语句需要的参数值。

```plain
	public Object query(String sql, Object[] args, PreparedStatementCallback pstmtcallback) {
	    //省略获取connection等代码
	    pstmt = con.prepareStatement(sql);
	    for (int i = 0; i < args.length; i++) { //设置参数
    		Object arg = args[i];
            //按照不同的数据类型调用JDBC的不同设置方法
	    	if (arg instanceof String) {
		      pstmt.setString(i+1, (String)arg);
		    } else if (arg instanceof Integer) {
		      pstmt.setInt(i+1, (int)arg);
		    }
        }
	    return pstmtcallback.doInPreparedStatement(pstmt);
	}

```

通过代码可以知道，和普通的Statement相比，这个PReparedStatement场景只是需要额外对SQL参数一个个赋值。这里我们还要注意一点，当SQL语句里有多个参数的时候，MiniSpring会按照参数次序赋值，和参数名没有关系。

我们再来看一下为PreparedStement准备的Callback接口。

```plain
package com.minis.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementCallback {
	Object doInPreparedStatement(PreparedStatement stmt) throws SQLException;
}

```

这也是一个函数式接口。

用户服务类代码改造如下：

```plain
public User getUserInfo(int userid) {
		final String sql = "select id, name,birthday from users where id=?";
		return (User)jdbcTemplate.query(sql, new Object[]{new Integer(userid)},
			(pstmt)->{
				ResultSet rs = pstmt.executeQuery();
				User rtnUser = null;
				if (rs.next()) {
					rtnUser = new User();
					rtnUser.setId(userid);
					rtnUser.setName(rs.getString("name"));
				}
				return rtnUser;
			}
		);
	}

```

到这里，我们就用一个单一的JdbcTemplate类实现了数据访问。

## 结合IoC容器

当然，我们还可以更进一步，既然我们的MiniSpring是个IoC容器，可以管理一个一个的Bean对象，那么我们就要好好利用它。由于只需要唯一的一个JdbcTemplate类，我们就可以事先把它定义为一个Bean，放在IoC容器里，然后通过@Autowired自动注入。

在XML配置文件中声明一下。

```plain
	<bean id="jdbcTemplate" class="com.minis.jdbc.core.JdbcTemplate" />

```

上层用户service程序中就不需要自己手动创建JdbcTemplate，而是通过Autowired注解进行注入就能得到了。

```plain
package com.test.service;

import java.sql.ResultSet;
import com.minis.beans.factory.annotation.Autowired;
import com.minis.jdbc.core.JdbcTemplate;
import com.test.entity.User;

public class UserService {
		@Autowired
		JdbcTemplate jdbcTemplate;
}

```

我们需要记住，MiniSpring只支持按照名字匹配注入，所以UserService类里的实例变量JdbcTemplate这个名字必须与XML文件中配置的Bean的id是一致的。如果不一致就会导致程序找不到JdbcTemplate。

这样一来，应用程序中和数据库访问相关的代码就全部剥离出去了，应用程序只需要声明使用它，而它的创建、管理都由MiniSpring框架来完成。从这里我们也能看出IoC容器带来的便利，事实上，我们需要用到的很多工具，都会以Bean的方式在配置文件中声明，交给IoC容器来管理。

## 数据源

我们注意到，JdbcTemplate中获取数据库连接信息等套路性语句仍然是硬编码的（hard coded）。

```plain
Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
con = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databasename=DEMO;user=sa;password=Sql2016;");

```

现在我们动手把这一部分代码包装成DataSource，通过它获取数据库连接。假设有了这个工具，上层应用程序就简单了。你可以看一下使用者的代码示例。

```plain
con = dataSource.getConnection();

```

这个Data Source被JdbcTemplate使用。

```plain
public class JdbcTemplate {
	private DataSource dataSource;
}

```

而这个属性可以通过配置注入，你可以看下配置文件。

```plain
<bean id="dataSource" class="com.minis.jdbc.datasource.SingleConnectionDataSource">
	<property type="String" name="driverClassName" value="com.microsoft.sqlserver.jdbc.SQLServerDriver"/>
	<property type="String" name="url" value="jdbc:sqlserver://localhost:1433;databasename=DEMO;"/>
	<property type="String" name="username" value="sa"/>
	<property type="String" name="password" value="Sql2016"/>
</bean>
<bean id="jdbcTemplate" class="com.minis.jdbc.core.JdbcTemplate" >
	<property type="javax.sql.DataSource" name="dataSource" ref="dataSource"/>
</bean>

```

在DataSource这个Bean初始化的时候，设置Property时会加载相应的JDBC Driver，然后注入给JdbcTemplate来使用。

我们再次看到，独立抽取这些部件，加上IoC容器的Bean管理，给系统构造带来许多便利。

上面描述的是假定有了一个DataSource之后怎么使用，现在回头再来看DataSource本身是怎么构造出来的。其实Java里已经给出了这个接口，是javax.sql.DataSource。我们就遵守这个规范，做一个简单的实现。

```plain
package com.minis.jdbc.datasource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;
import javax.sql.DataSource;

public class SingleConnectionDataSource implements DataSource {
	private String driverClassName;
	private String url;
	private String username;
	private String password;
	private Properties connectionProperties;
	private Connection connection;

    //默认构造函数
	public SingleConnectionDataSource() {
	}
    //一下是属性相关的getter和setter
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Properties getConnectionProperties() {
		return connectionProperties;
	}
	public void setConnectionProperties(Properties connectionProperties) {
		this.connectionProperties = connectionProperties;
	}
	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return null;
	}
	@Override
	public int getLoginTimeout() throws SQLException {
		return 0;
	}
	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return null;
	}
	@Override
	public void setLogWriter(PrintWriter arg0) throws SQLException {
	}
	@Override
	public void setLoginTimeout(int arg0) throws SQLException {
	}
	@Override
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		return false;
	}
	@Override
	public <T> T unwrap(Class<T> arg0) throws SQLException {
		return null;
	}
    //设置driver class name的方法，要加载driver类
	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
		try {
			Class.forName(this.driverClassName);
		}
		catch (ClassNotFoundException ex) {
			throw new IllegalStateException("Could not load JDBC driver class [" + driverClassName + "]", ex);
		}
	}
    //实际建立数据库连接
	@Override
	public Connection getConnection() throws SQLException {
		return getConnectionFromDriver(getUsername(), getPassword());
	}
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return getConnectionFromDriver(username, password);
	}
    //将参数组织成Properties结构，然后拿到实际的数据库连接
	protected Connection getConnectionFromDriver(String username, String password) throws SQLException {
		Properties mergedProps = new Properties();
		Properties connProps = getConnectionProperties();
		if (connProps != null) {
			mergedProps.putAll(connProps);
		}
		if (username != null) {
			mergedProps.setProperty("user", username);
		}
		if (password != null) {
			mergedProps.setProperty("password", password);
		}

		this.connection = getConnectionFromDriverManager(getUrl(),mergedProps);
		return this.connection;
	}
    //通过DriverManager.getConnection()建立实际的连接
	protected Connection getConnectionFromDriverManager(String url, Properties props) throws SQLException {
		return DriverManager.getConnection(url, props);
	}
}

```

这个类很简单，封装了和数据访问有关的信息，除了getter和setter之外，它最核心的方法就是getConnection()，这个方法又会调用getConnectionFromDriver()，最后会调用到getConnectionFromDriverManager()。你看一下这个方法，里面就是我们熟悉的DriverManager.getConnection()，一层层调用，最后还是落实到这里了。

所以我们看实际的数据库连接是什么时候创建的呢？这个可以采用不同的策略，可以在初始化Bean的时候创建，也可以延后到实际使用的时候。MiniSpring到现在这一步，采取的是后面这个策略，在应用程序dataSource.getConnection()的时候才实际生成数据库连接。

## 小结

我们这节课通过三个手段叠加，简化了数据库操作，重构了数据访问的程序结构。第一个手段是 **模板化**，把通用代码写到一个JdbcTemplate模板里，把变化的部分交给具体的类来实现。第二个手段就是通过 **Callback模式**，把具体类里实现的业务逻辑包装成一个回调函数，作为参数传给JdbcTemplate模板，这样就省去了要为每一个数据表单独增加一个具体实现类的工作。第三个手段就是结合IoC容器， **把JdbcTemplate声明成一个Bean**，并利用@Autowired注解进行自动注入。

之后我们抽取出了数据源的概念，包装connection，让应用程序和底下的数据库分隔开。

当然，程序走到这一步，还是有很多不足，主要的就是JdbcTemplate中还保留了很多固定的代码，比如SQL结果和业务对象的自动匹配问题，而且也没有考虑数据库连接池等等。这些都需要我们在后面的课程中一个个解决。

完整源代码参见 [https://github.com/YaleGuo/minis](https://github.com/YaleGuo/minis)

## 课后题

学完这节课，我也给你留一道思考题。我们现在只实现了query，想一想如果想要实现update应该如何做呢？欢迎你在留言区与我交流讨论，也欢迎你把这节课分享给需要的朋友。我们下节课见！

# 14｜增强模板：如何抽取专门的部件完成专门的任务？

你好，我是郭屹，今天我们继续手写MiniSpring。

上节课，我们从JDBC这些套路性的程序流程中抽取出了一个通用模板。然后进行了拆解，将SQL语句当作参数传入，而SQL语句执行之后的结果处理逻辑也作为一个匿名类传入，又抽取出了数据源的概念。下面我们接着上节课的思路，继续拆解JDBC程序。

我们现在观察应用程序怎么使用的JdbcTemplate，看这些代码，还是会发现几个问题。

1. SQL语句参数的传入还是一个个写进去的，没有抽取出一个独立的部件进行统一处理。
2. 返回的记录是单行的，不支持多行的数据集，所以能对上层应用程序提供的API非常有限。
3. 另外每次执行SQL语句都会建立连接、关闭连接，性能会受到很大影响。

这些问题，我们都需要在这节课上一个个解决。

## 参数传入

先看SQL语句参数的传入问题，我们注意到现在往PreparedStatement中传入参数是这样实现的。

```plain
	for (int i = 0; i < args.length; i++) {
		Object arg = args[i];
		if (arg instanceof String) {
			pstmt.setString(i+1, (String)arg);
		}
		else if (arg instanceof Integer) {
			pstmt.setInt(i+1, (int)arg);
		}
		else if (arg instanceof java.util.Date) {
			pstmt.setDate(i+1, new java.sql.Date(((java.util.Date)arg).getTime()));
		}
	}

```

简单地说，这些参数都是一个个手工传入进去的。但我们想让参数传入的过程自动化一点，所以现在我们来修改一下，把JDBC里传参数的代码进行包装，用一个专门的部件专门做这件事情，于是我们引入 **ArgumentPreparedStatementSetter**，通过里面的setValues()方法把参数传进PreparedStatement。

```plain
package com.minis.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArgumentPreparedStatementSetter {
	private final Object[] args; //参数数组

	public ArgumentPreparedStatementSetter(Object[] args) {
		this.args = args;
	}
    //设置SQL参数
	public void setValues(PreparedStatement pstmt) throws SQLException {
		if (this.args != null) {
			for (int i = 0; i < this.args.length; i++) {
				Object arg = this.args[i];
				doSetValue(pstmt, i + 1, arg);
			}
		}
	}
    //对某个参数，设置参数值
	protected void doSetValue(PreparedStatement pstmt, int parameterPosition, Object argValue) throws SQLException {
		Object arg = argValue;
        //判断参数类型，调用相应的JDBC set方法
		if (arg instanceof String) {
			pstmt.setString(parameterPosition, (String)arg);
		}
		else if (arg instanceof Integer) {
			pstmt.setInt(parameterPosition, (int)arg);
		}
		else if (arg instanceof java.util.Date) {
			pstmt.setDate(parameterPosition, new java.sql.Date(((java.util.Date)arg).getTime()));
		}
	}
}

```

从代码中可以看到，核心仍然是JDBC的set方法，但是包装成了一个独立部件。现在的示例程序只是针对了String、Int和Date三种数据类型，更多的数据类型我们留到后面再扩展。

有了这个专门负责参数传入的setter之后，query()就修改成这个样子。

```plain
	public Object query(String sql, Object[] args, PreparedStatementCallback pstmtcallback) {
		Connection con = null;
		PreparedStatement pstmt = null;

		try {
            //通过data source拿数据库连接
			con = dataSource.getConnection();

			pstmt = con.prepareStatement(sql);
            //通过argumentSetter统一设置参数值
			ArgumentPreparedStatementSetter argumentSetter = new ArgumentPreparedStatementSetter(args);
			argumentSetter.setValues(pstmt);

			return pstmtcallback.doInPreparedStatement(pstmt);
		}
		catch (Exception e) {
				e.printStackTrace();
		}
		finally {
			try {
				pstmt.close();
				con.close();
			} catch (Exception e) {
			}
		}
		return null;
	}

```

我们可以看到，代码简化了很多，手工写的一大堆设置参数的代码不见了，这就体现了专门的部件做专门的事情的优点。

## 对返回结果的处理

JDBC来执行SQL语句，说起来很简单，就三步，一准备参数，二执行语句，三处理返回结果。准备参数和执行语句这两步我们上面都已经抽取了。接下来我们再优化一下处理返回值的代码，看看能不能提供更多便捷的方法。

我们先看一下现在是怎么处理的，程序体现在pstmtcallback.doInPreparedStatement(pstmt)这个方法里，这是一个callback类，由用户程序自己给定，一般会这么做。

```plain
	return (User)jdbcTemplate.query(sql, new Object[]{new Integer(userid)},
		(pstmt)->{
			ResultSet rs = pstmt.executeQuery();
			User rtnUser = null;
			if (rs.next()) {
				rtnUser = new User();
				rtnUser.setId(userid);
				rtnUser.setName(rs.getString("name"));
				rtnUser.setBirthday(new java.util.Date(rs.getDate("birthday").getTime()));
			} else {
			}
			return rtnUser;
		}
	);

```

这个本身没有什么问题，这部分逻辑实际上已经剥离出去了。只不过，它限定了用户只能用这么一种方式进行。有时候很不便利，我们还应该考虑给用户程序提供多种方式。比如说，我们想返回的不是一个对象（对应数据库中一条记录），而是对象列表（对应数据库中多条记录）。这种场景很常见，需要我们再单独提供一个便利的工具。

所以我们设计一个接口RowMapper，把JDBC返回的ResultSet里的某一行数据映射成一个对象。

```plain
package com.minis.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowMapper<T> {
	T mapRow(ResultSet rs, int rowNum) throws SQLException;
}

```

再提供一个接口ResultSetExtractor，把JDBC返回的ResultSet数据集映射为一个集合对象。

```plain
package com.minis.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetExtractor<T> {
	T extractData(ResultSet rs) throws SQLException;
}

```

利用上面的两个接口，我们来实现一个RowMapperResultSetExtractor。

```plain
package com.minis.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RowMapperResultSetExtractor<T> implements ResultSetExtractor<List<T>> {
	private final RowMapper<T> rowMapper;

	public RowMapperResultSetExtractor(RowMapper<T> rowMapper) {
		this.rowMapper = rowMapper;
	}

	@Override
	public List<T> extractData(ResultSet rs) throws SQLException {
		List<T> results = new ArrayList<>();
		int rowNum = 0;
        //对结果集，循环调用mapRow进行数据记录映射
		while (rs.next()) {
			results.add(this.rowMapper.mapRow(rs, rowNum++));
		}
		return results;
	}
}

```

这样，SQL语句返回的数据集就自动映射成对象列表了。我们看到，实际的数据映射工作其实不是我们实现的，而是由RowMapper实现的，这个RowMapper既是作为一个参数又是作为一个用户程序传进去的。这很合理，因为确实只有用户程序自己知道自己的数据要如何映射。

好，有了这个工具，我们可以提供一个新的query()方法来返回SQL语句的结果集，代码如下：

```plain
	public <T> List<T> query(String sql, Object[] args, RowMapper<T> rowMapper) {
		RowMapperResultSetExtractor<T> resultExtractor = new RowMapperResultSetExtractor<>(rowMapper);
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
            //建立数据库连接
			con = dataSource.getConnection();

            //准备SQL命令语句
			pstmt = con.prepareStatement(sql);
            //设置参数
			ArgumentPreparedStatementSetter argumentSetter = new ArgumentPreparedStatementSetter(args);
			argumentSetter.setValues(pstmt);
            //执行语句
			rs = pstmt.executeQuery();

            //数据库结果集映射为对象列表，返回
			return resultExtractor.extractData(rs);
		}
		catch (Exception e) {
				e.printStackTrace();
		}
		finally {
			try {
				pstmt.close();
				con.close();
			} catch (Exception e) {
			}
		}
		return null;
	}

```

那么上层应用程序的service层要改成这样：

```plain
	public List<User> getUsers(int userid) {
		final String sql = "select id, name,birthday from users where id>?";
		return (List<User>)jdbcTemplate.query(sql, new Object[]{new Integer(userid)},
				new RowMapper<User>(){
					public User mapRow(ResultSet rs, int i) throws SQLException {
						User rtnUser = new User();
						rtnUser.setId(rs.getInt("id"));
						rtnUser.setName(rs.getString("name"));
						rtnUser.setBirthday(new java.util.Date(rs.getDate("birthday").getTime()));

						return rtnUser;
					}
				}
		);
	}

```

service程序里面执行SQL语句，直接按照数据记录的字段的mapping关系，返回一个对象列表。这样，到此为止，MiniSpring的JdbcTemplate就可以提供3种query()方法了。

1. public Object query(StatementCallback stmtcallback) {}
2. public Object query(String sql, Object\[\] args, PreparedStatementCallback pstmtcallback) {}
3. public  List query(String sql, Object\[\] args, RowMapper rowMapper){}

实际上我们还可以提供更多的工具，你可以举一反三思考一下应该怎么做，这里我就不多说了。

## 数据库连接池

到现在这一步，我们的MiniSpring仍然是在执行SQL语句的时候，去新建数据库连接，使用完之后就释放掉了。我们知道，数据库连接的建立和释放，是很费资源和时间的。所以这个方案不是最优的，那怎样才能解决这个问题呢？有一个方案可以试一试，那就是 **池化技术**。提前在一个池子里预制多个数据库连接，在应用程序来访问的时候，就给它一个，用完之后再收回到池子中，整个过程中数据库连接一直保持不关闭，这样就大大提升了性能。

所以我们需要改造一下原有的数据库连接，不把它真正关闭，而是设置一个可用不可用的标志。我们用一个新的类，叫PooledConnection，来实现Connetion接口，里面包含了一个普通的Connection，然后用一个标志Active表示是否可用，并且永不关闭。

```plain
package com.minis.jdbc.pool;
public class PooledConnection implements Connection{
	private Connection connection;
	private boolean active;

	public PooledConnection() {
	}
	public PooledConnection(Connection connection, boolean active) {
		this.connection = connection;
		this.active = active;
	}

	public Connection getConnection() {
		return connection;
	}
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public void close() throws SQLException {
		this.active = false;
	}
	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return this.connection.prepareStatement(sql);
	}
}

```

实际代码很长，因为要实现JDBC Connection接口里所有的方法，你可以参考上面的示例代码，别的可以都留空。

最主要的，我们要注意close()方法，它其实不会关闭连接，只是把这个标志设置为false。

基于上面的PooledConnection，我们把原有的DataSource改成PooledDataSource。首先在初始化的时候，就激活所有的数据库连接。

```plain
package com.minis.jdbc.pool;

public class PooledDataSource implements DataSource{
	private List<PooledConnection> connections = null;
	private String driverClassName;
	private String url;
	private String username;
	private String password;
	private int initialSize = 2;
	private Properties connectionProperties;

	private void initPool() {
		this.connections = new ArrayList<>(initialSize);
		for(int i = 0; i < initialSize; i++){
			Connection connect = DriverManager.getConnection(url, username, password);
			PooledConnection pooledConnection = new PooledConnection(connect, false);
			this.connections.add(pooledConnection);
		}
	}
}

```

获取数据库连接的代码如下：

```plain
	PooledConnection pooledConnection= getAvailableConnection();
	while(pooledConnection == null){
		pooledConnection = getAvailableConnection();
		if(pooledConnection == null){
			try {
				TimeUnit.MILLISECONDS.sleep(30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
    return pooledConnection;

```

可以看出，我们的策略是死等这一个有效的连接。而获取有效连接的代码如下：

```plain
	private PooledConnection getAvailableConnection() throws SQLException{
		for(PooledConnection pooledConnection : this.connections){
			if (!pooledConnection.isActive()){
				pooledConnection.setActive(true);
				return pooledConnection;
			}
		}

		return null;
	}

```

通过代码可以知道，其实它就是拿一个空闲标志的数据库连接来返回。逻辑上这样是可以的，但是，这段代码就会有一个并发问题，多线程的时候不好用，需要改造一下才能适应多线程环境。我们注意到这个池子用的是一个简单的ArrayList，这个默认是不同步的，我们需要手工来做同步，比如使用Collections.synchronizedList()，或者用两个LinkedBlockingQueue，一个用于active连接，一个用于inactive连接。

同样，对DataSource里数据库的相关信息，可以通过配置来注入的。

```plain
<bean id="dataSource" class="com.minis.jdbc.pool.PooledDataSource">
    <property name="url" value="jdbc:sqlserver://localhost:1433;databasename=DEMO"/>
    <property name="driverClassName" value="com.microsoft.sqlserver.jdbc.SQLServerDriver"/>
    <property name="username" value="sa"/>
    <property name="password" value="Sql2016"/>
    <property type="int" name="initialSize" value="3"/>
</bean>

```

整个程序的结构实际上没有什么改动，只是将DataSource的实现变成了支持连接池的实现。从这里也可以看出，独立抽取部件、解耦这些手段给程序结构带来了极大的灵活性。

## 小结

我们这节课，在已有的JdbcTemplate基础之上，仍然按照专门的事情交给专门的部件来做的思路，一步步拆解。

我们把SQL语句参数的处理独立成一个ArgumentPreparedStatementSetter，由它来负责参数的传入。之后对返回结果，我们提供了RowMapper和RowMapperResultSetExtractor，将数据库记录集转换成一个对象的列表，便利了上层应用程序。最后考虑到性能，我们还引入了一个简单的数据库连接池。在这一步步地拆解过程中，JdbcTemplate这个工具越来越完整、便利了。

完整源代码参见 [https://github.com/YaleGuo/minis](https://github.com/YaleGuo/minis)。

## 课后题

学完这节课的内容，我也给你留一道思考题。你想一想我们应该怎么改造数据库连接池，保证多线程安全？欢迎你在留言区与我交流讨论，也欢迎你把这节课分享给需要的朋友。我们下节课见！

# 15｜mBatis：如何将SQL语句配置化？

你好，我是郭屹。今天我们继续手写MiniSpring。这节课我们要模仿MyBatis，将SQL语句配置化。

上一节课，在已有的JDBC Template基础之上，我们按照专门的事情交给专门的部件来做的思路，对它做了进一步地拆解，抽取出了数据源DataSource这个部件，然后我们把SQL语句参数的处理独立成了一个ArgumentPreparedStatementSetter，之后对于返回结果，我们提供了两个东西，一个RowMapper和一个RowMapperResultSetExtractor，把一条数据库记录和一个记录集转换成对象和对象列表，便利了上层应用程序。最后为了提高性能，我们还引入了一个简单的数据库连接池。

现在执行的SQL语句本身还是硬编码在程序中的，所以这节课，我们就模仿MyBatis，把SQL语句放到程序外面的配置文件中。

## MyBatis简介

我们先来简单了解一下MyBatis。

> 官方说法：MyBatis is a first class persistence framework with support for custom SQL, stored procedures and advanced mappings. MyBatis eliminates almost all of the JDBC code and manual setting of parameters and retrieval of results. MyBatis can use simple XML or Annotations for configuration and map primitives, Map interfaces and Java POJOs (Plain Old Java Objects) to database records.

从官方的资料里我们知道，MyBatis的目标是构建一个框架来支持自定义SQL、存储过程和复杂的映射，它将手工的JDBC代码都简化掉了，通过配置完成数据库记录与Java对象的转换。当然，MyBatis不只是把SQL语句写到外部配置文件这么简单，它还干了好多别的工作，比如ORM、缓存等等，我们这里只讨论SQL语句配置化。

在MyBatis的常规使用办法中，程序以这个SqlSessionFactory为中心，来创建一个SqlSession，然后执行SQL语句。

你可以看一下简化后的代码。

```plain
try (SqlSession session = sqlSessionFactory.openSession()) {
    Blog blog = session.selectOne(
             "org.mybatis.example.BlogMapper.selectBlog", 101);
}

```

上面代码的大意是先用SqlSessionFactory创建一个SqlSession，然后把要执行的SQL语句的id（org.mybatis.example.BlogMapper.selectBlog）和SQL参数（101），传进session.selectOne()方法，返回查询结果对象值Blog。

凭直觉，这个session应当是包装了数据库连接信息，而这个SQL id应当是指向的某处定义的SQL语句，这样就能大体跟传统的JDBC代码对应上了。

我们就再往下钻研一下。先看这个SqlSessionFactory是怎么来的，一般在应用程序中这么写。

```plain
String resource = "org/mybatis/example/mybatis-config.xml";
InputStream inputStream = Resources.getResourceAsStream(resource);
SqlSessionFactory sqlSessionFactory =
          new SqlSessionFactoryBuilder().build(inputStream);

```

可以看出，它是通过一个配置文件由一个SqlSessionFactoryBuider工具来生成的，我们看看配置文件的简单示例。

```plain
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
  PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
  "https://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
  <environments default="development">
    <environment id="development">
      <transactionManager type="JDBC"/>
      <dataSource type="POOLED">
        <property name="driver" value="${driver}"/>
        <property name="url" value="${url}"/>
        <property name="username" value="${username}"/>
        <property name="password" value="${password}"/>
      </dataSource>
    </environment>
  </environments>
  <mappers>
    <mapper resource="org/mybatis/example/BlogMapper.xml"/>
  </mappers>
</configuration>

```

没有什么神奇的地方，果然就是数据库连接信息还有一些mapper文件的配置。用这些配置信息创建一个Factory，这个Factory就知道该如何访问数据库了，至于具体执行的SQL语句，则是放在mapper文件中的，你可以看一下示例。

```plain
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
  PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="org.mybatis.example.BlogMapper">
  <select id="selectBlog" resultType="Blog">
    select * from Blog where id = #{id}
  </select>
</mapper>

```

我们看这个mapper文件，里面就是包含了SQL语句，给了select语句一个namespace（org.mybatis.example.BlogMapper）以及id（selectBlog），它们拼在一起就是上面程序中写的SQL语句的sqlid（org.mybatis.example.BlogMapper.selectBlog）。我们还要注意这个SQL的参数占位符 #{id} 以及返回结果对象Blog，它们的声明格式是MyBatis自己规定的。

转换成JDBC的语言，这里定义了这个SQL语句是一个select语句，命令是select \* from Blog where id = #{id}，参数是id，返回结果对象对应的是Blog，这条SQL语句有一个唯一的sqlid来代表。

现在我们几乎能想象出应用程序执行下面这行的时候在做什么了。

```plain
    Blog blog = session.selectOne(
                    "org.mybatis.example.BlogMapper.selectBlog", 101);

```

一定就是用这个id去mapper文件里找对应的SQL语句，替换参数，然后执行，最后将数据库记录按照某种规则转成一个对象返回。整个过程跟我们在JdbcTemplate中做得很类似。

有了这个思路，我们就可以着手实现自己的mBatis了。

## Mapper配置

我们仿照MyBatis，把SQL语句放在外部配置文件中。先在resources目录下建一个mapper目录，然后把SQL语句配置在这里，如mapper/User\_Mapper.xml文件。

```plain
	<?xml version="1.0" encoding="UTF-8"?>
	<mapper namespace="com.test.entity.User">
	    <select id="getUserInfo" parameterType="java.lang.Integer" resultType="com.test.entity.User">
	        select id, name,birthday
	        from users
	        where id=?
	    </select>
	</mapper>

```

这个配置中，也同样有基本的一些元素：SQL类型、SQL的id、参数类型、返回结果类型、SQL语句、条件参数等等。

自然，我们需要在内存中用一个结构来对应上面的配置，存放系统中的SQL语句的定义。

```plain
package com.minis.batis;

public class MapperNode {
    String namespace;
    String id;
    String parameterType;
    String resultType;
    String sql;
    String parameter;

	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getParameterType() {
		return parameterType;
	}
	public void setParameterType(String parameterType) {
		this.parameterType = parameterType;
	}
	public String getResultType() {
		return resultType;
	}
	public void setResultType(String resultType) {
		this.resultType = resultType;
	}
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	public String getParameter() {
		return parameter;
	}
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}
	public String toString(){
		return this.namespace+"."+this.id+" : " +this.sql;
	}
}

```

对它们的处理工作，我们仿照MyBatis，用一个SqlSessionFactory来处理，并默认实现一个DefaultSqlSessionFactory来负责。

你可以看一下SqlSessionFactory接口定义。

```plain
package com.minis.batis;

public interface SqlSessionFactory {
	SqlSession openSession();
	MapperNode getMapperNode(String name);
}

```

同时，我们仍然使用IoC来管理，将默认的DefaultSqlSessionFactory配置在IoC容器的applicationContext.xml文件里。

```plain
    <bean id="sqlSessionFactory" class="com.minis.batis.DefaultSqlSessionFactory" init-method="init">
        <property type="String" name="mapperLocations" value="mapper"></property>
    </bean>

```

我们并没有再用一个builder来生成Factory，这是为了简单一点。

这个Bean，也就是这里配置的默认的SqlSessionFactory，它在初始化过程中会扫描这个mapper目录。

```plain
	public void init() {
	    scanLocation(this.mapperLocations);
	}

```

而这个扫描跟以前的Servlet也是一样的，用递归的方式访问每一个文件。

```plain
	private void scanLocation(String location) {
    	String sLocationPath = this.getClass().getClassLoader().getResource("").getPath()+location;
        File dir = new File(sLocationPath);
        for (File file : dir.listFiles()) {
            if(file.isDirectory()){ //递归扫描
            	scanLocation(location+"/"+file.getName());
            }else{ //解析mapper文件
                buildMapperNodes(location+"/"+file.getName());
            }
        }
    }

```

最后对扫描到的每一个文件，要进行解析处理，把SQL定义写到内部注册表Map里。

```plain
	private Map<String, MapperNode> buildMapperNodes(String filePath) {
        SAXReader saxReader=new SAXReader();
        URL xmlPath=this.getClass().getClassLoader().getResource(filePath);

		Document document = saxReader.read(xmlPath);
		Element rootElement=document.getRootElement();

		String namespace = rootElement.attributeValue("namespace");

        Iterator<Element> nodes = rootElement.elementIterator();;
        while (nodes.hasNext()) { //对每一个sql语句进行解析
        	Element node = nodes.next();
            String id = node.attributeValue("id");
            String parameterType = node.attributeValue("parameterType");
            String resultType = node.attributeValue("resultType");
            String sql = node.getText();

            MapperNode selectnode = new MapperNode();
            selectnode.setNamespace(namespace);
            selectnode.setId(id);
            selectnode.setParameterType(parameterType);
            selectnode.setResultType(resultType);
            selectnode.setSql(sql);
            selectnode.setParameter("");

            this.mapperNodeMap.put(namespace + "." + id, selectnode);
        }
	    return this.mapperNodeMap;
	}

```

程序很简单，就是拿这个配置文件中的节点，读取节点的各项属性，然后设置到MapperNode结构中。注意，上面的解析可以看到最后这个完整的id是namespace+“.”+id，对应上面例子里的就是com.test.entity.User.getUserInfo。还有，作为一个原理性示例，我们现在只能处理select这一种SQL语句，update之类的语句留着之后扩展。考虑到有多种SQL命令，扩展的时候需要增加一个属性，表明这条SQL语句是读语句还是写语句。

你可以看一下DefaultSqlSessionFactory的完整代码。

```plain
package com.minis.batis;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import com.minis.beans.factory.annotation.Autowired;
import com.minis.jdbc.core.JdbcTemplate;

public class DefaultSqlSessionFactory implements SqlSessionFactory{
	@Autowired
	JdbcTemplate jdbcTemplate;

	String mapperLocations;
	public String getMapperLocations() {
		return mapperLocations;
	}
	public void setMapperLocations(String mapperLocations) {
		this.mapperLocations = mapperLocations;
	}
	Map<String,MapperNode> mapperNodeMap = new HashMap<>();
	public Map<String, MapperNode> getMapperNodeMap() {
		return mapperNodeMap;
	}
	public DefaultSqlSessionFactory() {
	}

	public void init() {
	    scanLocation(this.mapperLocations);
	}
    private void scanLocation(String location) {
    	String sLocationPath = this.getClass().getClassLoader().getResource("").getPath()+location;
        File dir = new File(sLocationPath);
        for (File file : dir.listFiles()) {
            if(file.isDirectory()){
            	scanLocation(location+"/"+file.getName());
            }else{
                buildMapperNodes(location+"/"+file.getName());
            }
        }
    }

	private Map<String, MapperNode> buildMapperNodes(String filePath) {
		System.out.println(filePath);
        SAXReader saxReader=new SAXReader();
        URL xmlPath=this.getClass().getClassLoader().getResource(filePath);
        try {
			Document document = saxReader.read(xmlPath);
			Element rootElement=document.getRootElement();

			String namespace = rootElement.attributeValue("namespace");

	        Iterator<Element> nodes = rootElement.elementIterator();;
	        while (nodes.hasNext()) {
	        	Element node = nodes.next();
	            String id = node.attributeValue("id");
	            String parameterType = node.attributeValue("parameterType");
	            String resultType = node.attributeValue("resultType");
	            String sql = node.getText();

	            MapperNode selectnode = new MapperNode();
	            selectnode.setNamespace(namespace);
	            selectnode.setId(id);
	            selectnode.setParameterType(parameterType);
	            selectnode.setResultType(resultType);
	            selectnode.setSql(sql);
	            selectnode.setParameter("");

	            this.mapperNodeMap.put(namespace + "." + id, selectnode);
	        }
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	    return this.mapperNodeMap;
	}

	public MapperNode getMapperNode(String name) {
		return this.mapperNodeMap.get(name);
	}

	@Override
	public SqlSession openSession() {
		SqlSession newSqlSession = new DefaultSqlSession();
		newSqlSession.setJdbcTemplate(jdbcTemplate);
		newSqlSession.setSqlSessionFactory(this);

		return newSqlSession;
	}
}

```

## 使用Sql Session访问数据

有了上面的准备工作，上层的应用程序在使用的时候，就可以通过Aurowired注解直接拿到这个SqlSessionFactory了，然后通过工厂创建一个Sql Session，再执行SQL命令。你可以看一下示例。

```plain
package com.test.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.minis.batis.SqlSession;
import com.minis.batis.SqlSessionFactory;
import com.minis.beans.factory.annotation.Autowired;
import com.minis.jdbc.core.JdbcTemplate;
import com.minis.jdbc.core.RowMapper;
import com.test.entity.User;

public class UserService {
		@Autowired
		SqlSessionFactory sqlSessionFactory;

		public User getUserInfo(int userid) {
			//final String sql = "select id, name,birthday from users where id=?";
			String sqlid = "com.test.entity.User.getUserInfo";
			SqlSession sqlSession = sqlSessionFactory.openSession();
			return (User)sqlSession.selectOne(sqlid, new Object[]{new Integer(userid)},
					(pstmt)->{
						ResultSet rs = pstmt.executeQuery();
						User rtnUser = null;
						if (rs.next()) {
							rtnUser = new User();
							rtnUser.setId(userid);
							rtnUser.setName(rs.getString("name"));
							rtnUser.setBirthday(new java.util.Date(rs.getDate("birthday").getTime()));
						} else {
						}
						return rtnUser;
					}
			);
		}
	}

```

从代码里可以看出，程序基本上与以前直接用JdbcTemplate一样，只是变成通过sqlSession.selectOne来执行了。

这个SqlSession是由工厂生成的： `SqlSession sqlSession = sqlSessionFactory.openSession();`。你可以看一下它在DefaultSqlSessionFactory类中的定义。

```plain
	public SqlSession openSession() {
		SqlSession newSqlSession = new DefaultSqlSession();
		newSqlSession.setJdbcTemplate(jdbcTemplate);
		newSqlSession.setSqlSessionFactory(this);

		return newSqlSession;
	}

```

由上面代码可见，这个Sql Session也就是对JdbcTemplate进行了一下包装。

定义接口：

```plain
package com.minis.batis;

import com.minis.jdbc.core.JdbcTemplate;
import com.minis.jdbc.core.PreparedStatementCallback;

public interface SqlSession {
	void setJdbcTemplate(JdbcTemplate jdbcTemplate);
	void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory);
	Object selectOne(String sqlid, Object[] args, PreparedStatementCallback pstmtcallback);
}

```

需要注意的是，我们是在openSession()的时候临时设置的JdbcTemplate，而不是在Factory中设置的。这个设计留下了灵活性，意味着我们每一次真正执行某条SQL语句的时候可以替换这个JdbcTemplate，这个时序的设计使动态数据源成为可能，这在读写分离的时候特别有用。

我们也默认给一个实现类DefaultSqlSession。

```plain
package com.minis.batis;

import javax.sql.DataSource;
import com.minis.jdbc.core.JdbcTemplate;
import com.minis.jdbc.core.PreparedStatementCallback;

public class DefaultSqlSession implements SqlSession{
	JdbcTemplate jdbcTemplate;
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	public JdbcTemplate getJdbcTemplate() {
		return this.jdbcTemplate;
	}
	SqlSessionFactory sqlSessionFactory;
	public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
		this.sqlSessionFactory = sqlSessionFactory;
	}
	public SqlSessionFactory getSqlSessionFactory() {
		return this.sqlSessionFactory;
	}
	@Override
	public Object selectOne(String sqlid, Object[] args, PreparedStatementCallback pstmtcallback) {
		String sql = this.sqlSessionFactory.getMapperNode(sqlid).getSql();
		return jdbcTemplate.query(sql, args, pstmtcallback);
	}

	private void buildParameter(){
	}

	private Object resultSet2Obj() {
		return null;
	}
}

```

这个session实现很单薄，对外就是一个selectOne()，可以看出，程序最终还是落到了jdbcTemplate.query(sql, args, pstmtcallback)方法上，像一个洋葱一样一层层包起来的。但是原理的说明还是都反映出来了。

到这里，我们就实现了一个极简的MyBatis。

## 小结

我们这节课仿照MyBatis将SQL语句进行了配置化。通过一个SqlSessionFactory解析配置文件，以一个id来代表使用的SQL语句。应用程序使用的时候，给SqlSession传入一个SQL的id号就可以执行。我们看到最后还是落到了JdbcTemplate方法中。

当然，这个是极简版本，远远没有实现MyBatis丰富的功能。比如现在只有select语句，没有update；比如SqlSession对外只有一个selectOne接口，非常单薄；比如没有SQL数据集缓存，每次都要重新执行；比如没有读写分离的配置。当然，如何在这个极简版本的基础上进行扩展，就需要你动动脑筋，好好思考一下了。

还是那句老话，我们在一步步构建框架的过程中，主要学习的是搭建框架的思路，拆解部件，让专门的部件去处理专门的事情，让自己的框架具有扩展性。

完整源代码参见 [https://github.com/YaleGuo/minis](https://github.com/YaleGuo/minis)

## 课后题

学完这节课，我也给你留一道思考题。我们只是简单地实现了select语句的配置，如何扩展到update语句？还有进一步地，如何实现读写分离？比如说select的时候从一个数据库来取，update的时候从另一个数据库来取。欢迎你在留言区与我交流讨论，也欢迎你把这节课分享给需要的朋友。我们下节课见！

# 16｜再回首：JdbcTemplate章节小结

你好，我是郭屹。

恭喜你学完了MiniSpring的第三部分——JdbcTemplate了。JdbcTemplate在Spring 框架里，扮演着非常重要的角色。通过它，我们可以更加便捷地进行数据库操作，缩短了开发周期和开发成本，同时也降低了出错的风险。

它对Spring应用程序的稳定性和性能表现有着至关重要的影响，已经成为开发高效、高质量应用程序的不可或缺的一部分。

为了让你更好地掌握这部分内容，下面我们对这一整章做一个重点回顾。

### JdbcTemplate重点回顾

JdbcTemplate是Spring框架中的一部分，是Spring对数据访问的一个实现，在Spring应用程序中被广泛采用。它这个实现特别好地体现了Rod Johnson对简洁实用的原则的把握。JdbcTemplate封装了JDBC的 API，并提供了更为便捷的访问方式，使得开发人员在不需要编写大量代码的情况下，能够高效、灵活地进行数据库操作。

我们知道，JDBC的程序都是类似的，所以这个部分我们提取出一个JDBC访问的模板，同时引入DataSource概念，屏蔽具体的数据库，就便利了上层应用业务程序员。然后，我们再进行SQL参数的处理，SQL请求带有参数，实现把数据转换成SQL语句所需要的参数格式，对SQL语句执行后的返回结果，又要自动绑定为业务对象。

之后，为了支持大量的数据访问，我们实现了数据库连接池提高性能，并且把连接池构造变成一个Bean注入到IoC容器里，还可以让用户自行配置连接池的参数。最后，进一步把程序里的SQL语句也抽取出来，配置到外部文件中，实现一个简单的MyBatis。

这就是这一章实现JdbcTemplate的过程，你可以再回顾一下。另外我们每一节课后面都给了一道思考题，让你在我们实现的这个极简框架上进行扩展，如果你认真学习了这一章的内容，相信你是可以举一反三的，自己提出解决方案。

方法可能不同，但目标是一样的。我把参考答案写在文稿中了，你可以看一下，如果你有更好的思路和想法，也欢迎和我分享。下节课我们马上要进入AOP的环节了，一起期待一下吧！

### 13｜JDBC访问框架：如何抽取JDBC模板并隔离数据库？

#### 思考题

我们现在只实现了query，想一想如果想要实现update应该如何做呢？

#### 参考答案

我们现在JdbcTemplate类的结构，对于query()和update()是并列设计的，只要在类中对应的提供一个方法，形如：int update(String sql, Object\[\] args, int\[\] argTypes)。这个方法内部是一个PreparedStatement，SQL是要执行的SQL语句，args是SQL参数，argTypes是数据类型，返回值是受影响的行数。

### 14｜增强模板：如何抽取专门的部件完成专门的任务？

#### 思考题

你想一想我们应该怎么改造数据库连接池，保证多线程安全？

#### 参考答案

这个问题有不同的方案，下面是一种思路供参考。

提供两个队列，一个用于忙的连接，一个用于空闲连接：

```plain
    private BlockingQueue<PooledConnection> busy;
    private BlockingQueue<PooledConnection> idle;

```

获取数据库连接就从idle队列中获取，程序大体如下：

```plain
while (true) {
conn = idle.poll();
}

```

就是死等一个空闲连接。然后加入忙队列。

当然，进一步考虑，还应当判断连接数是否到了最大，如果没有，则要先创建一个新的连接。创建的时候要小心了，因为是多线程的，所以要再次校验是否超过最大连接数，如使用CAS技术：

```plain
if (size.get() < getPoolProperties().getMaxActive()) {
            if (size.addAndGet(1) > getPoolProperties().getMaxActive()) {
                size.decrementAndGet();
            } else {
                return createConnection(now, con, username, password);
            }
        }

```

而且还应当设置一个timeout，如果在规定的时间内还没有拿到一个连接，就要抛出一个异常。

```plain
if ((System.currentTimeMillis() - now) >= maxWait) {
                throw new PoolExhaustedException(
                    "Timeout: Unable to fetch a connection in " + (maxWait / 1000) +
                    " seconds.");
        } else {
                continue;
        }

```

关闭连接，也就是从busy队列移除，然后加入到idle队列中。

### 15｜mBatis : 如何将SQL语句配置化？

#### 思考题

我们只是简单地实现了select语句的配置，如何扩展到update语句？进一步，如何实现读写分离？

#### 参考答案

我们可以在sql节点类MapperNode中增加一个属性sqltype，表示sql语句的类型，比如0表示select，1表示update，2表示insert，3表示delete。这样我们就知道了一个sql语句是read还是write。

然后datasource变成两个，一个是readDatasource，一个是writeDatasource，可以配置在外部文件中。JdbcTemplate也提供一个setDatasource()允许动态设置数据源。

DefaultSqlSession类中配置两个data source，形如：

```plain
	private DataSource readDataSource;
	private DataSource writeDataSource;

```

然后在selectOne()中这么判断：

```plain
	public Object selectOne(String sqlid, Object[] args, PreparedStatementCallback pstmtcallback) {
		int sqltype = this.sqlSessionFactory.getMapperNode(sqlid).getSqlType();
 if (sqltype==0)  {//read
jdbcTemplate.setDatasource(readDataSource);
		}
		return jdbcTemplate.query(sql, args, pstmtcallback);
	}

```

也就是说，每一次用SqlSession执行SQL语句的时候，都判断一下SQL类型，如果是read，则设置readDatasource，否则设置writeDatasource.

# 17｜动态代理：如何在运行时插入逻辑？

你好，我是郭屹。今天我们继续手写MiniSpring。

从这节课开始，我们就要进入AOP环节了。在学习之前，我们先来了解一下是AOP怎么回事。

AOP，就是面向切面编程（Aspect Orient Programming），这是一种思想，也是对OOP面向对象编程的一种补充。你可能会想：既然已经存在OOP面向对象编程了，为什么还需要AOP面向切面编程呢？

这是因为在许多场景下，一个类的方法中，除了业务逻辑，通常还会包括其他比较重要但又不算主业务逻辑的例行性逻辑代码，比如常见的日志功能，它不影响我们的主业务逻辑，但又能在必要时定位问题，几乎每一个业务方法中都需要。又比如权限检查、事务处理，还有性能监控等等，都是这种情况。

显而易见，日志这类例行性逻辑，在任何一个业务方法实现中都是需要的。如果简单地将这些代码写在业务方法中，会出现两个后果，第一，我们就会将日志之类的代码重复地编写多次；第二，一个业务方法中会包含很多行例行代码，去看源代码会发现方法中多数语句不是在做业务处理。

有专业进取心的程序员就会思考一个问题， **有没有办法将这些例行性逻辑单独抽取出来，然后在程序运行的时候动态插入到业务逻辑中呢？** 正是因为这个疑问，AOP应运而生了。这个问题听起来似乎无解，程序在运行时改变程序本身，似乎有点不可思议。我们研究一下Java，就会惊奇地发现，Java里面早就给我们提供了一个手段： **动态代理**。我们可以利用它来开展我们的工作。

## 代理模式

我们一步步来，先从代理讲起。

![图片](assets/5e31827e2dec92103754abfc45f67a4c.png)

看图，我们知道真正干活儿的类是RealSubject，具体则是由DoAction()执行任务。Proxy作为代理提供一个同样的DoAction()，然后调用RealSubject的DoAction()。它们都实现Subject接口，而Client应用程序操作的是Subject 接口。

简单说来，就是在Client应用程序与真正的服务程序RealSubject之间增加了一个Proxy。

我们举例说明，先定义一个服务类接口。

```plain
public interface Subject {
	String doAction(String name);
}

```

再定义具体的服务类。

```plain
public class RealSubject implements Subject {
	public String doAction(String name) {
		System.out.println("real subject do action "+name);
		return "SUCCESS";
	}
}

```

最后再定义一个代理类。

```plain
public class ProxySubject implements Subject {
	Subject realsubject;
	public ProxySubject() {
		this.realsubject = new RealSubject();
	}
	public String doAction(String name) {
		System.out.println("proxy control");
		String rtnValue = realsubject.doAction(name);
		return "SUCCESS";
	}
}

```

通过代码我们看到，代理类内部包含了一个真正的服务类，而代理类给外部程序提供了和真正的服务类同样的接口。当外部应用程序调用代理类的方法时，代理类内部实际上会转头调用真正的服务类的相应方法，然后把真正的服务类的返回值直接返回给外部程序。这样做就隐藏了真正的服务类的实现细节。

同时，在调用真正的服务方法之前和之后，我们还可以在代理类里面做一点手脚，加上额外的逻辑，比如上面程序中的 `System.out.println("proxy control");`，这些额外的代码，大部分都是一些例行性的逻辑，如权限和日志等。

最后我们提供一个客户程序使用这个代理类。

```plain
public class Client {
	public static void main(String[] args) {
		Subject subject = new ProxySubject();
		subject.doAction("Test");
	}
}

```

总结一下，代理模式能够让我们在业务处理之外添加例行性逻辑。但是这个经典的模式在我们这里不能直接照搬，因为这个代理是静态的，要事先准备好。而我们需要的是在相关的业务逻辑执行的时候，动态插入例行性逻辑，不需要事先手工静态地准备这些代理类。解决方案就是 **Java中的动态代理技术。**

## 动态代理

Java提供的动态代理可以对接口进行代理，在代理的过程中主要做三件事。

1. 实现InvocationHandler接口，重写接口内部唯一的方法invoke。
2. 使用Proxy类，通过newProxyInstance，初始化一个代理对象。
3. 通过代理对象，代理其他类，对该类进行增强处理。

这里我们还是举例说明。首先定义一个IAction接口。

```java
package com.test.service;
public interface IAction {
   void doAction();
}

```

提供一个具体实现类。

```java
package com.test.service;
public class Action1 implements IAction {
   @Override
   public void doAction() {
      System.out.println("really do action");
   }
}

```

我们定义了一个DynamicProxy类，用来充当代理对象的类。

```java
package com.test.service;
public class DynamicProxy {
   private Object subject = null;

   public DynamicProxy(Object subject) {
         this.subject = subject;
   }

   public Object getProxy() {
      return Proxy.newProxyInstance(DynamicProxy.class
            .getClassLoader(), subject.getClass().getInterfaces(),
            new InvocationHandler() {
         public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("doAction")) {
                System.out.println("before call real object........");
                return method.invoke(subject, args);
            }
            return null;
         }
      });
   }
}

```

通过这个类的实现代码可以看出，我们使用了Proxy类，调用newProxyInstance方法构建IAction接口的代理对象，而且重写了InvocationHandler接口中的invoke方法。在重写的方法中我们判断方法名称是否与接口中的doAction方法保持一致，随后加上例行性逻辑（print语句），最后通过反射调用接口IAction中的doAction方法。

通过这个操作，例行性逻辑就在业务程序运行的时候，动态地添加上去了。

我们编写一个简单的测试程序，就能直观感受到代理的效果了。

```java
package com.test.controller;
public class HelloWorldBean {
    @Autowired
    IAction action;

    @RequestMapping("/testaop")
    public void doTestAop(HttpServletRequest request, HttpServletResponse response) {
     DynamicProxy proxy = new DynamicProxy(action);
     IAction p = (IAction)proxy.getProxy();
     p.doAction();

     String str = "test aop, hello world!";
     try {
        response.getWriter().write(str);
     } catch (IOException e) {
        e.printStackTrace();
     }
  }
}

```

运行这个程序，返回内容是“test aop，hello world！”。这个时候查看命令行里的内容，你就会发现还有两行输出。

```java
before call real object........
really do action

```

第一行是代理对象中的输出，第二行是Action1中doAction方法的实现。

根据这个输出顺序我们发现，这个代理对象达到了代理的效果，在调用IAction的具体实现类之前进行了额外的操作，从而增强了代理类。而这个代理是我们动态增加的，而不是事先静态地手工编写一个代理类。

但是读代码，这种方式显然是不美观的，需要在业务逻辑程序中写上，对代码的侵入性太强了。

```java
DynamicProxy proxy = new DynamicProxy(action);
IAction p = (IAction)proxy.getProxy();

```

这个写法跟我们手工写一个代理类实际上相差不多。这种侵入式的做法不是我们推崇的，所以我们要继续前进。

## 引入FactoryBean

我们的目标是 **非侵入式编程，** 也就是应用程序在编程的时候，它不应该手工去创建一个代理，而是使用本来的业务接口，真正的实现类配置在外部，代理类也是配置在外部。

```java
@Autowired
IAction action;

@RequestMapping("/testaop")
public void doTestAop(HttpServletRequest request, HttpServletResponse response) {
    action.doAction();
}

```

配置如下：

```xml
<bean id="realaction" class="com.test.service.Action1" />
<bean id="action" class="com.minis.aop.ProxyFactoryBean" >
    <property type="java.lang.Object" name="target" ref="realaction"/>
</bean>

```

业务类中自动注入的是一个action，也就是上面代码里的ProxyFactoryBean类，这个类内部包含了真正干活儿的类realaction。

这里就有一个初看起来非常奇怪的需求：注册的action bean是ProxyFactoryBean类，而业务程序使用getBean(“action”)的时候，期待返回的又不是这个Bean本身，而是内部那个target。因为只有这样才能让业务程序实际调用target中的方法，外面的这个ProxyFactoryBean对我们来讲是一个入口，而不是目标。这也就要求，当业务程序使用getBean(“action”)方法的时候，这个ProxyFactoryBean应该在内部进行进一步地处理，根据target再动态生成一个代理返回，达到侵入式编程中下面这两句话的效果。

```java
DynamicProxy proxy = new DynamicProxy(action);
IAction p = (IAction)proxy.getProxy();

```

上面的方案，看起来奇怪，但是确实能解决动态代理的问题。

好，现在我们就按照这个思路动手去实现。首先我们参考Spring框架，定义FactoryBean接口。

相关代码参考：

```java
package com.minis.beans.factory;
public interface FactoryBean<T> {
    T getObject() throws Exception;
    Class<?> getObjectType();
    default boolean isSingleton() {
        return true;
    }
}

```

主要的方法就是getObject()，从Factory Bean中获取内部包含的对象。

接着定义FactoryBeanRegistrySupport，提供一部分通用的方法。

```java
package com.minis.beans.factory.support;
public abstract class FactoryBeanRegistrySupport extends DefaultSingletonBeanRegistry{
    protected Class<?> getTypeForFactoryBean(final FactoryBean<?> factoryBean) {
        return factoryBean.getObjectType();
    }
    protected Object getObjectFromFactoryBean(FactoryBean<?> factory, String beanName) {
        Object object = doGetObjectFromFactoryBean(factory, beanName);
        try {
            object = postProcessObjectFromFactoryBean(object, beanName);
        } catch (BeansException e) {
            e.printStackTrace();
        }
        return object;
    }
    //从factory bean中获取内部包含的对象
    private Object doGetObjectFromFactoryBean(final FactoryBean<?> factory, final String beanName) {
        Object object = null;
        try {
            object = factory.getObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return object;
    }
}

```

最重要的是这个方法：doGetObjectFromFactoryBean()，从一个Factory Bean里面获取内部包含的那个target对象。

因为FactoryBeanRegistrySupport继承了DefaultSingletonBeanRegistry，所以我们接下来可以改写AbstractBeanFactory，由原本继承DefaultSingletonBeanRegistry改成继承FactoryBeanRegistrySupport，保留原有功能的同时增加了功能扩展。

我们重点要修改核心的getBean()方法。

```java
package com.minis.beans.factory.support;
public abstract class AbstractBeanFactory extends FactoryBeanRegistrySupport implements ConfigurableBeanFactory,BeanDefinitionRegistry{
   public Object getBean(String beanName) throws BeansException{
      Object singleton = this.getSingleton(beanName);
      if (singleton == null) {
         singleton = this.earlySingletonObjects.get(beanName);
         if (singleton == null) {
            System.out.println("get bean null -------------- " + beanName);
            BeanDefinition bd = beanDefinitionMap.get(beanName);
            if (bd != null) {
               singleton=createBean(bd);
               this.registerBean(beanName, singleton);
               //beanpostprocessor
               //step 1 : postProcessBeforeInitialization
               applyBeanPostProcessorsBeforeInitialization(singleton, beanName);
               //step 2 : init-method
               if (bd.getInitMethodName() != null && !bd.getInitMethodName().equals("")) {
                  invokeInitMethod(bd, singleton);
               }
               //step 3 : postProcessAfterInitialization
               applyBeanPostProcessorsAfterInitialization(singleton, beanName);
            }
            else {
               return null;
            }
         }
      }
      else {
      }
      //处理factorybean
      if (singleton instanceof FactoryBean) {
         return this.getObjectForBeanInstance(singleton, beanName);
      }
      else {
      }
      return singleton;
   }

```

我们看到在getBean()这一核心方法中，原有的逻辑处理完毕后，我们新增下面这一段。

```java
//process Factory Bean
if (singleton instanceof FactoryBean) {
   return this.getObjectForBeanInstance(singleton, beanName);
}

```

根据代码实现可以看出，这里增加了一个判断，如果Bean对象是FactoryBean类型时，则调用getObjectForBeanInstance方法。

```java
protected Object getObjectForBeanInstance(Object beanInstance, String beanName) {
    // Now we have the bean instance, which may be a normal bean or a FactoryBean.
    if (!(beanInstance instanceof FactoryBean)) {
         return beanInstance;
    }
    Object object = null;
    FactoryBean<?> factory = (FactoryBean<?>) beanInstance;
    object = getObjectFromFactoryBean(factory, beanName);
    return object;
}

```

代码显示，getObjectForBeanInstance又会调用doGetObjectFromFactoryBean方法。

```java
private Object doGetObjectFromFactoryBean(final FactoryBean<?> factory, final String beanName) {
    Object object = null;
    try {
        object = factory.getObject();
    } catch (Exception e) {
        e.printStackTrace();
    }
    return object;
}

```

最后落实到了factory.getObject()里。由此可以看出，我们通过AbstractBeanFactory获取Bean的时候，对FactoryBean进行了特殊处理，获取到的已经不是FactoryBean本身了，而是它内部包含的那一个对象。而这个对象，也不是真正底层对应的Bean。它仍然只是一个代理的对象，我们继续往下看。

我们这个getObject()只是FactoryBean里的一个接口，接下来我们提供一下它的接口实现——ProxyFactoryBean。

```java
package com.minis.aop;
public class ProxyFactoryBean implements FactoryBean<Object> {
    private AopProxyFactory aopProxyFactory;
    private String[] interceptorNames;
    private String targetName;
    private Object target;
    private ClassLoader proxyClassLoader = ClassUtils.getDefaultClassLoader();
    private Object singletonInstance;
    public ProxyFactoryBean() {
        this.aopProxyFactory = new DefaultAopProxyFactory();
    }
    public void setAopProxyFactory(AopProxyFactory aopProxyFactory) {
        this.aopProxyFactory = aopProxyFactory;
    }
    public AopProxyFactory getAopProxyFactory() {
        return this.aopProxyFactory;
    }
    protected AopProxy createAopProxy() {
        return getAopProxyFactory().createAopProxy(target);
    }
    public void setInterceptorNames(String... interceptorNames) {
        this.interceptorNames = interceptorNames;
    }
    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }
    public Object getTarget() {
        return target;
    }
    public void setTarget(Object target) {
        this.target = target;
    }
    @Override
    public Object getObject() throws Exception {//获取内部对象
        return getSingletonInstance();
    }
    private synchronized Object getSingletonInstance() {//获取代理
        if (this.singletonInstance == null) {
            this.singletonInstance = getProxy(createAopProxy());
        }
        return this.singletonInstance;
    }
    protected Object getProxy(AopProxy aopProxy) {//生成代理对象
        return aopProxy.getProxy();
    }
    @Override
    public Class<?> getObjectType() {
        return null;
    }
}

```

这段代码的核心在于，ProxyFactoryBean在getObject()方法中生成了一个代理getProxy(createAopProxy())，同样也是通过这种方式，拿到了要代理的目标对象。这里的工作就是 **创建动态代理**。

## 基于JDK的实现

Spring作为一个雄心勃勃的框架，自然不会把自己局限于JDK提供的动态代理一个技术上，所以，它再次进行了包装，提供了AopProxy的概念，JDK只是其中的一种实现。

```java
package com.minis.aop;
public interface AopProxy {
    Object getProxy();
}

```

还定义了factory。

```java
package com.minis.aop;
public interface AopProxyFactory {
    AopProxy createAopProxy(Object target);
}

```

然后给出了基于JDK的实现。

```java
package com.minis.aop;
public class JdkDynamicAopProxy implements AopProxy, InvocationHandler {
    Object target;
    public JdkDynamicAopProxy(Object target) {
        this.target = target;
    }
    @Override
    public Object getProxy() {
        Object obj = Proxy.newProxyInstance(JdkDynamicAopProxy.class.getClassLoader(), target.getClass().getInterfaces(), this);
        return obj;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("doAction")) {
            System.out.println("-----before call real object, dynamic proxy........");
            return method.invoke(target, args);
        }
        return null;
    }
}

```

```java
package com.minis.aop;
public class DefaultAopProxyFactory implements AopProxyFactory{
    @Override
    public AopProxy createAopProxy(Object target) {
		return new JdkDynamicAopProxy(target);
    }
}

```

在这个实现里，我们终于看到了我们曾经熟悉的Proxy.newProxyInstance()和invoke()。利用Java的动态代理技术代理了目标对象，而这也是ProxyFactoryBean里真正要返回的Object。

这就是Spring AOP的实现原理。

## 测试

有了上面的工具，我们的测试程序就不需要再手动构建代理对象了，而是交给框架本身处理。而注入的对象，则通过配置文件注入属性值。

applicationContext.xml配置中新增一段内容。

```xml
<bean id="realaction" class="com.test.service.Action1" />
<bean id="action" class="com.minis.aop.ProxyFactoryBean" >
    <property type="java.lang.Object" name="target" ref="realaction"/>
</bean>

```

通过配置，我们在HelloWorldBean里注入的IAction对象就纳入了容器管理之中，因此后续测试的时候，直接使用action.doAction()，就能实现手动初始化JDK代理对象的效果。

```java
package com.test.controller;
public class HelloWorldBean {
   @Autowired
   IAction action;

   @RequestMapping("/testaop")
   public void doTestAop(HttpServletRequest request, HttpServletResponse response) {
      action.doAction();

      String str = "test aop, hello world!";
      try {
         response.getWriter().write(str);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}

```

我们终于看到了动态代理的结果。

## 小结

这节课我们 **利用JDK动态代理技术实现了AOP这个概念**。

我们介绍了代理模式实现的静态代理，然后使用了JDK的动态代理技术。在使用动态代理技术的程序代码中，我们发现它是侵入式的，不理想，所以我们就想办法把代理配置在XML文件里了。但是如果按照原有的Bean的定义，这个配置在外部文件里的代理Bean本身不能代理业务类，我们真正需要的是通过这个代理Bean来创建一个动态代理，于是引入了FactoryBean的概念，不是直接获取这个Bean本身，而是通过里面的getObject()获取到Factory Bean里面包含的对象。

这样将IoC容器里的Bean分成了两类：一是普通的Bean，二是Factory Bean。在getObject()的实现中，我们使用JDK的动态代理技术创建了一个代理。这样就实现了AOP。

另外，Spring中的代理支持JDK代理与Cglib代理两种，目前MiniSpring定义的DefaultAopProxyFactory只支持JDK代理。另一种方式我留作思考题，你可以先想一想要怎么实现。

AOP还有别的实现方案，比如AspectJ，也比较常用，在实际工程实践中，一般采用的就是AspectJ，而不是Spring AOP，因为AspectJ更加高效，功能更强。比如，AspectJ是编译时创建的代理，性能高十倍以上，而且切入点不仅仅在方法上，而是可以在类的任何部分。所以AspectJ才是完整的AOP解决方案，Spring AOP不是成功的工业级方案。之所以保留Spring AOP，一个原因是原理简单、利于理解，另一个是Rod Johnson不忍抛弃自己的心血。

完整源代码参见 [https://github.com/YaleGuo/minis](https://github.com/YaleGuo/minis)

## 课后题

学完这节课，我也给你留一道思考题。如果MiniSpring想扩展到支持Cglib，程序应该从哪里下手改造？欢迎你在留言区与我交流讨论，也欢迎你把这节课分享给需要的朋友。我们下节课见！

# 18｜拦截器 ：如何在方法前后进行拦截？

你好，我是郭屹，今天我们继续手写MiniSpring。

前面，我们用JDK动态代理技术实现了AOP，并且进行了解耦，采用IoC容器来管理代理对象，实现了非侵入式编程。我们现在能在不影响业务代码的前提下，进行逻辑的增强工作，比如打印日志、事务处理、统计接口耗时等等，将这些例行性逻辑作为一种增强放在代理中，运行时动态插入（编织）进去。

有了这个雏形，我们自然就会进一步考虑，在这个代理结构的基础上，将动态添加逻辑这件事情做得更加结构化一点，而不是全部简单地堆在invoke()方法里。

## 引入三个概念

我们先来看看invoke()这个方法的代码在结构方面有什么问题。

```plain
public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
	if (method.getName().equals("doAction")) {
		 System.out.println("-----before call real object, dynamic proxy........");
		 return method.invoke(target, args);
	}
	return null;
}

```

我们看到，在实际调用某个方法的时候，是用的反射直接调用method，对应在代码里也就是 `method.invoke(target,args);` 这一句。而增强的例行性代码是直接写在method.invoke()这个方法前面的，也就是上面代码里的 `System.out.println())`。这么做当然没有错，不过扩展性不好。这里我们还是使用那个老办法， **不同的功能由不同的部件来做**，所以这个增强逻辑我们可以考虑抽取出一个专门的部件来做，实际业务方法的调用也可以包装一下。

所以这节课，我们引入以下几个概念。

- Advice：表示这是一个增强操作。
- Interceptor：拦截器，它实现的是真正的增强逻辑。
- MethodInterceptor：调用方法上的拦截器，也就是它实现在某个方法上的增强。

通过这几个概念，我们就可以把例行性逻辑单独剥离出来了。现在我们要做一个切面，只需要实现某个Interceptor就可以了。

对应地，我们定义一下Advice、Interceptor、MethodInterceptor这几个接口。

```java
package com.minis.aop;
public interface Advice {
}

```

```java
package com.minis.aop;
public interface Interceptor extends Advice{
}

```

```java
package com.minis.aop;
public interface MethodInterceptor extends Interceptor{
    Object invoke(MethodInvocation invocation) throws Throwable;
}

```

MethodInterceptor就是方法上的拦截器，对外就是一个invoke()方法。拦截器不仅仅会增强逻辑，它内部也会调用业务逻辑方法。因此，对外部程序而言，只需要使用这个MethodInterceptor就可以了。

它需要传入一个MethodInvocation，然后调用method invocation的proceed()方法，MethodInvocation实际上就是以前通过反射方法调用业务逻辑的那一段代码的包装。。

```java
public interface MethodInvocation {
   Method getMethod();
   Object[] getArguments();
   Object getThis();
   Object proceed() throws Throwable;
}

```

我们再来看一下应用程序员的工作，为了插入切面，需要在invoke()中实现自己的业务增强代码。

```plain
public class TracingInterceptor implements MethodInterceptor {
	public Object invoke(MethodInvocation i) throws Throwable {
		System.out.println("method "+i.getMethod()+" is called on "+
	                        i.getThis()+" with args "+i.getArguments());
		Object ret=i.proceed();
		System.out.println("method "+i.getMethod()+" returns "+ret);
		return ret;
   }
}

```

中间的i.proceed()才是真正的目标对象的方法调用。

```plain
public Object proceed() throws Throwable {
	return this.method.invoke(this.target, this.arguments);
}

```

## 改造代理类

有了上面准备好的这些部件，我们在动态代理中如何使用它们呢？这里我们再引入一个Advisor接口。

```plain
	public interface Advisor {
		MethodInterceptor getMethodInterceptor();
		void setMethodInterceptor(MethodInterceptor methodInterceptor);
	}

```

在代理类ProxyFactoryBean里增加Advisor属性和拦截器。

```plain
    private String interceptorName;
    private Advisor advisor;

```

这样，我们的代理类里就有跟拦截器关联的点了。

接下来，为了在目标对象调用前进行拦截，我们就需要调整这个ProxyFactoryBean，并设置其Advisor属性，同时定义这个initializeAdvisor方法来进行关联。

```java
package com.minis.aop;
public class ProxyFactoryBean implements FactoryBean<Object> {
    private BeanFactory beanFactory;
    private String interceptorName;
    private Advisor advisor;

    private synchronized void initializeAdvisor() {
        Object advice = null;
        MethodInterceptor mi = null;
        try {
            advice = (MethodInterceptor) this.beanFactory.getBean(this.interceptorName);
        } catch (BeansException e) {
            e.printStackTrace();
        }
        advisor = new DefaultAdvisor();
        advisor.setMethodInterceptor((MethodInterceptor)advice);
    }
}

```

通过ProxyFactoryBean代码实现可以看出，里面新增了initializeAdvisor处理，将应用程序自定义的拦截器获取到Advisor里。并且，可以在IoC容器中配置这个Interceptor名字。

在initializeAdvisor里，我们把Advisor初始化工作交给了DefaultAdvisor。

```java
package com.minis.aop;
public class DefaultAdvisor implements Advisor{
    private MethodInterceptor methodInterceptor;
    public DefaultAdvisor() {
    }
    public void setMethodInterceptor(MethodInterceptor methodInterceptor) {
        this.methodInterceptor = methodInterceptor;
    }
    public MethodInterceptor getMethodInterceptor() {
        return this.methodInterceptor;
    }
}

```

随后，我们修改AopProxyFactory中createAopProxy接口的方法签名，新增Advisor参数。

```java
package com.minis.aop;
public interface AopProxyFactory {
    AopProxy createAopProxy(Object target, Advisor advisor);
}

```

修改接口后，我们需要相应地修改其实现方法。在ProxyFactoryBean中，唯一的实现方法就是createAopProxy()。

```java
protected AopProxy createAopProxy() {
    return getAopProxyFactory().createAopProxy(target);
}

```

在这个方法中，我们对前面引入的Advisor进行了赋值。修改之后，代码变成了这样。

```java
protected AopProxy createAopProxy() {
    return getAopProxyFactory().createAopProxy(target，this.advisor);
}

```

默认实现是DefaultAopProxyFactory与JdkDynamicAopProxy，这里要一并修改。

```java
package com.minis.aop;
public class DefaultAopProxyFactory implements AopProxyFactory{
    @Override
    public AopProxy createAopProxy(Object target, Advisor advisor) {
        return new JdkDynamicAopProxy(target, advisor);
    }
}

```

```java
package com.minis.aop;
public class JdkDynamicAopProxy implements AopProxy, InvocationHandler {
    Object target;
    Advisor advisor;
    public JdkDynamicAopProxy(Object target, Advisor advisor) {
        this.target = target;
        this.advisor = advisor;
    }
    @Override
    public Object getProxy() {
        Object obj = Proxy.newProxyInstance(JdkDynamicAopProxy.class.getClassLoader(), target.getClass().getInterfaces(), this);
        return obj;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("doAction")) {
            Class<?> targetClass = (target != null ? target.getClass() : null);
            MethodInterceptor interceptor = this.advisor.getMethodInterceptor();
            MethodInvocation invocation =
                    new ReflectiveMethodInvocation(proxy, target, method, args, targetClass);
            return interceptor.invoke(invocation);
        }
        return null;
    }
}

```

在JdkDynamicAopProxy里我们发现，invoke方法和之前相比有了不小的变化，在调用某个方法的时候，不再是直接用反射调用方法了，而是先拿到Advisor里面的Interceptor，然后把正常的method调用包装成ReflectiveMethodInvocation，最后调用interceptor.invoke(invocation)，对需要调用的方法进行了增强处理。

你把这一段和之前的invoke()进行比对，可以看出，通过Interceptor这个概念，我们就把增强逻辑单独剥离出来了。

你可以看一下实际的ReflectiveMethodInvocation类，其实就是对反射调用方法进行了一次包装。

```java
package com.minis.aop;
public class ReflectiveMethodInvocation implements MethodInvocation{
    protected final Object proxy;
    protected final Object target;
    protected final Method method;
    protected Object[] arguments;
    private Class<?> targetClass;
    protected ReflectiveMethodInvocation(
            Object proxy,  Object target, Method method,  Object[] arguments,
            Class<?> targetClass) {
        this.proxy = proxy;
        this.target = target;
        this.targetClass = targetClass;
        this.method = method;
        this.arguments = arguments;
    }

    //省略getter/setter

    public Object proceed() throws Throwable {
        return this.method.invoke(this.target, this.arguments);
    }
}

```

## 测试

我们现在可以来编写一下测试代码，定义TracingInterceptor类模拟业务拦截代码。

```java
package com.test.service;
import com.minis.aop.MethodInterceptor;
import com.minis.aop.MethodInvocation;
public class TracingInterceptor implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation i) throws Throwable {
        System.out.println("method "+i.getMethod()+" is called on "+
                i.getThis()+" with args "+i.getArguments());
        Object ret=i.proceed();
        System.out.println("method "+i.getMethod()+" returns "+ret);
        return ret;
    }
}

```

applicationContext.xml配置文件：

```xml
   <bean id="myInterceptor" class="com.test.service.TracingInterceptor" />
   <bean id="realaction" class="com.test.service.Action1" />
   <bean id="action" class="com.minis.aop.ProxyFactoryBean" >
      <property type="java.lang.Object" name="target" ref="realaction"/>
      <property type="String" name="interceptorName" value="myInterceptor"/>
   </bean>

```

配置文件里，除了原有的target，我们还增加了一个interceptorName属性，让程序员指定需要启用什么样的增强。

到这里，我们就实现了MethodInterceptor。

## 在方法前后拦截

我们现在实现的方法拦截，允许程序员自行编写invoke()方法，进行任意操作。但是在许多场景下，调用方式实际上是比较固定的，即在某个方法调用之前或之后，允许程序员插入业务上需要的增强。为了满足这种情况，我们可以提供特定的方法拦截，并允许程序员在这些拦截点之前和之后进行业务增强的操作。这种方式就大大简化了程序员的工作。

所以这里我们新增两种advice：MethodBeforeAdvice和AfterReturningAdvice。根据名字也可以看出来，它们分别对应方法调用前处理和返回后的处理。你可以看一下它们的定义。

```java
package com.minis.aop;
public interface BeforeAdvice extends Advice{
}

```

```java
package com.minis.aop;
public interface AfterAdvice extends Advice{
}

```

```java
package com.minis.aop;
import java.lang.reflect.Method;
public interface MethodBeforeAdvice extends BeforeAdvice {
    void before(Method method, Object[] args, Object target) throws Throwable;
}

```

```java
package com.minis.aop;
import java.lang.reflect.Method;
public interface AfterReturningAdvice extends AfterAdvice{
    void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable;
}

```

首先我们定义通用接口BeforeAdvice与AfterAdvice，随后定义核心的MethodBeforeAdvice与AfterReturningAdvice接口，它们分别内置了before方法和afterReturning方法。由方法签名可以看出，这两者的区别在于afterReturning它内部传入了返回参数，说明是目标方法执行返回后，再调用该方法，在方法里面可以拿到返回的参数。

有了新的Advice的定义，我们就可以实现新的Interceptor了。你可以看下实现的代码。

```xml
package com.minis.aop;
public class MethodBeforeAdviceInterceptor implements MethodInterceptor, BeforeAdvice {
    private final MethodBeforeAdvice advice;
    public MethodBeforeAdviceInterceptor(MethodBeforeAdvice advice) {
        this.advice = advice;
    }
    @Override
    public Object invoke(MethodInvocation mi) throws Throwable {
        this.advice.before(mi.getMethod(), mi.getArguments(), mi.getThis());
        return mi.proceed();
    }
}

```

在这个Interceptor里，invoke()方法的实现实际上就是限制性地使用advice.before()方法，然后执行目标方法的调用，也意味着这是在方法调用之前插入的逻辑。由于这是针对before这种行为的特定Interceptor，因此上层应用程序员无需自己再进行实现，而是可以直接使用这个Interceptor。

```xml
package com.minis.aop;
public class AfterReturningAdviceInterceptor implements MethodInterceptor, AfterAdvice{
    private final AfterReturningAdvice advice;
    public AfterReturningAdviceInterceptor(AfterReturningAdvice advice) {
        this.advice = advice;
    }
    @Override
    public Object invoke(MethodInvocation mi) throws Throwable {
        Object retVal = mi.proceed();
        this.advice.afterReturning(retVal, mi.getMethod(), mi.getArguments(), mi.getThis());
        return retVal;
    }
}

```

同样，由AfterReturningAdviceInterceptor类中对invoke方法的实现可以看出，是先调用mi.proceed()方法获取到了返回值retVal，再调用afterReturning方法，实现的是方法调用之后的逻辑增强，这个时序也是固定的。所以注意了，在advice.afterReturing()方法中，是可以拿到目标方法的返回值的。

在拦截器的使用中，存在一个有意思的问题，同时也是一个有着广泛争议的话题：拦截器是否应该影响业务程序的流程？比如，在before()拦截器中加入一个返回标志（true/false），当其为false时，我们就中止业务流程并且不再调用目标方法。

不同的开发者对于这个问题有着不同的主张。一方面，这种机制使得开发者能够根据需要对业务逻辑进行精细控制；另一方面，过度使用这种机制也可能会导致代码难度增加、可维护性降低等问题。因此，在使用拦截器的时候，需要在开发效率和程序可维护性之间做出一个平衡，并根据实际情况做出相应的选择。

现在我们手上有三种Advice类型了，普通的MethodInterceptor，还有特定的MethodBeforeAdviceInterceptor和AfterReturningAdviceInterceptor，自然在ProxyFactoryBean中也要对这个initializeAdvisor方法进行改造，分别支持三种不同类型的Advice。

```java
package com.minis.aop;
public class ProxyFactoryBean implements FactoryBean<Object>, BeanFactoryAware {
    private synchronized void initializeAdvisor() {
        Object advice = null;
        MethodInterceptor mi = null;
        try {
            advice = this.beanFactory.getBean(this.interceptorName);
        } catch (BeansException e) {
            e.printStackTrace();
        }
        if (advice instanceof BeforeAdvice) {
            mi = new MethodBeforeAdviceInterceptor((MethodBeforeAdvice)advice);
        }
        else if (advice instanceof AfterAdvice) {
            mi = new AfterReturningAdviceInterceptor((AfterReturningAdvice)advice);
        }
        else if (advice instanceof MethodInterceptor) {
            mi = (MethodInterceptor)advice;
        }
        advisor = new DefaultAdvisor();
        advisor.setMethodInterceptor(mi);
    }
}

```

上述实现比较简单，根据不同的Advice类型进行判断，最后统一用MethodInterceptor来封装。

## 测试

在这一步改造完毕后，我们测试一下，这里我们提供的是比较简单的实现，实际开发过程中你可以跟据自己的需求定制开发。

我们先提供两个Advice。

```java
package com.test.service;
public class MyAfterAdvice implements AfterReturningAdvice {
    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
        System.out.println("----------my interceptor after method call----------");
    }
}

```

```java
package com.test.service;
public class MyBeforeAdvice implements MethodBeforeAdvice {
    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        System.out.println("----------my interceptor before method call----------");
    }
}

```

上述的测试代码都很简单，在此不多赘述。相应的applicationContext.xml这个配置文件里面的内容也要发生变化。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans>
   <bean id="myBeforeAdvice" class="com.test.service.MyBeforeAdvice" />
   <bean id="realaction" class="com.test.service.Action1" />
   <bean id="action" class="com.minis.aop.ProxyFactoryBean" >
      <property type="java.lang.Object" name="target" ref="realaction"/>
      <property type="String" name="interceptorName" value="myBeforeAdvice"/>
   </bean>
</beans>

```

将beforeAdvice或者afterAdvice放在配置文件里，除了注册的Bean类名有一些修改，其配置是没有发生任何别的变化的，但经过这样一番改造，我们就能使用上述三类Advice，来对我们的业务代码进行拦截增强处理了。

## 小结

这节课我们在简单动态代理结构的基础上， **将动态添加的逻辑设计得更加结构化一点，而不是全部简单地堆在invoke()一个方法中**。为此，我们提出了Advice的概念，表示这是一个增强操作。然后提出Interceptor拦截器的概念，它实现了真正的增强逻辑并包装了目标方法的调用，应用程序中实际使用的就是这个Interceptor。我们实际实现的是MethodInterceptor，它表示的是调用方法上的拦截器。

我们注意到大部分拦截的行为都是比较固定的，或者在方法调用之前，或者在之后，为了方便处理这些常见的场景，我们进一步分离出了beforeAdvice和afterAdvice。通过这些工作，用户希望插入的例行性逻辑现在都单独抽取成一个部件了，应用程序员只要简单地实现MethodBeforeAdvice和AfterReturningAdvice即可。整个软件结构化很好，完全解耦。

完整源代码参见 [https://github.com/YaleGuo/minis](https://github.com/YaleGuo/minis)

## 课后题

学完这节课的内容，我也给你留一道思考题。如果我们希望beforeAdvice能在某种情况下阻止目标方法的调用，应该从哪里下手改造？欢迎你在留言区与我交流讨论，也欢迎你把这节课分享给需要的朋友。我们下节课见！

# 19｜Pointcut ：如何批量匹配代理方法？

你好，我是郭屹。今天我们继续手写MiniSpring。

到目前为止，我们已经初步实现了简单的AOP，做到了封装JDK的动态代理，并且定义了Advice，实现了调用前、调用时、调用后三个不同位置对代理对象进行增强的效果，而这些切面的定义也是配置在外部文件中的。我们现在在这个基础之上继续前进，引入Pointcut这个概念，批量匹配需要代理的方法。

## 引入Pointcut

我们再回头看一下代码，前面所有的代理方法，都是同一个名字——doAction。我们用以下代码将该方法名写死了，也就是说我们只认定这一个方法名为代理方法，而且名字是不能改的。

```java
if (method.getName().equals("doAction")) {
}

```

如果我们需要增加代理方法，或者就算不增加，只是觉得这个方法名不好想换一个，怎么办呢？当前这种方法自然不能满足我们的需求了。而这种对多个方法的代理需求又特别重要，因为业务上有可能会想对某一类方法进行增强，统一加上监控日志什么的，这种情况下，如果要逐个指定方法名就太麻烦了。

进一步考虑，即便我们这里可以支持多个方法名，但是匹配条件仍然是equals，也就是说，规则仅仅是按照方法名精确匹配的，这样做太不灵活了。

因此这节课我们考虑用方法名匹配规则进行通配，而这个配置则允许应用开发程序员在XML文件中自定义。这就是我们常说的 **切点（Pointcut），按照规则匹配需要代理的方法**。

我们先确定一下，这节课代码改造完毕后，配置文件是什么样子的，我把变动最大的地方放在下面，供你参考。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans>
   <bean id="realaction" class="com.test.service.Action1" />
   <bena id="beforeAdvice" class="com.test.service.MyBeforeAdvice" />
   <bean id="advisor" class="com.minis.aop.NameMatchMethodPointcutAdvisor">
      <property type="com.minis.aop.Advice" name="advice" ref="beforeAdvice"/>
      <property type="String" name="mappedName" value="do*"/>
   </bean>
   <bean id="action" class="com.minis.aop.ProxyFactoryBean">
      <property type="String" name="interceptorName" value="advisor" />
      <property type="java.lang.Object" name="target" ref="realaction"/>
   </bean>
</beans>

```

由上述改动可以看出，我们新定义了一个NameMatchMethodPointcutAdvisor类作为Advisor，其中property属性中的value值为do\*，这就是我们说的方法规则，也就是匹配所有以do开头的方法名称。这里你也可以根据实际的业务情况按照一定的规则配置自定义的代理方法，而不仅仅局限于简单的方法名精确相等匹配。

有了这个Pointcut，我们就能用一条规则来支持多个代理方法了，这非常有用。如果能实现这个配置，就达到了我们想要的效果。

为了实现这个目标，最后构建出一个合适的NameMatchMethodPointcutAdvisor，我们定义了MethodMatcher、Pointcut与PointcutAdvisor三个接口。

MethodMatcher这个接口代表的是方法的匹配算法，内部的实现就是看某个名是不是符不符合某个模式。

```java
package com.minis.aop;
public interface MethodMatcher {
    boolean matches(Method method, Class<?> targetCLass);
}

```

Pointcut接口定义了切点，也就是返回一条匹配规则。

```java
package com.minis.aop;
public interface Pointcut {
    MethodMatcher getMethodMatcher();
}

```

PointcutAdvisor接口扩展了Advisor，内部可以返回Pointcut，也就是说这个Advisor有一个特性：能支持切点Pointcut了。这也是一个常规的Advisor，所以可以放到我们现有的AOP框架中，让它负责来增强。

```java
package com.minis.aop;
public interface PointcutAdvisor extends Advisor{
    Pointcut getPointcut();
}

```

接口定义完毕之后，接下来就要有这些接口对应的实现。实际我们在原理上可以实现一系列不同的规则，但是现在我们只能简单地使用名称进行模式匹配，不过能通过这个搞清楚原理就可以了。

## 如何匹配？

我们先来看核心问题： **如何匹配到方法？** 我们默认的实现是NameMatchMethodPointcut和NameMatchMethodPointcutAdvisor。

```java
package com.minis.aop;
public class NameMatchMethodPointcut implements MethodMatcher, Pointcut{
    private String mappedName = "";
    public void setMappedName(String mappedName) {
        this.mappedName = mappedName;
    }
    @Override
    public boolean matches(Method method, Class<?> targetCLass) {
        if (mappedName.equals(method.getName()) || isMatch(method.getName(), mappedName)) {
            return true;
        }
        return false;
    }
    //核心方法，判断方法名是否匹配给定的模式
    protected boolean isMatch(String methodName, String mappedName) {
        return PatternMatchUtils.simpleMatch(mappedName, methodName);
    }
    @Override
    public MethodMatcher getMethodMatcher() {
        return null;
    }
}

```

我们看到了，这个类的核心方法就是 **isMatch()**，它用到了一个工具类叫 **PatterMatchUtils**。我们看一下这个工具类是怎么进行字符串匹配的。

```plain
/**
 * 用给定的模式匹配字符串。
 * 模式格式: "xxx*", "*xxx", "*xxx*" 以及 "xxx*yyy"，*代表若干个字符。
 */
public static boolean simpleMatch( String pattern,  String str) {
    //先判断串或者模式是否为空
	if (pattern == null || str == null) {
		return false;
	}
    //再判断模式中是否包含*
	int firstIndex = pattern.indexOf('*');
	if (firstIndex == -1) {
		return pattern.equals(str);
	}
    //是否首字符就是*,意味着这个是*XXX格式
    if (firstIndex == 0) {
		if (pattern.length() == 1) {  //模式就是*,通配全部串
			return true;
		}
		//尝试查找下一个*
        int nextIndex = pattern.indexOf('*', 1);
		if (nextIndex == -1) { //没有下一个*，说明后续不需要再模式匹配了，直接endsWith判断
			return str.endsWith(pattern.substring(1));
		}
        //截取两个*之间的部分
		String part = pattern.substring(1, nextIndex);
		if (part.isEmpty()) { //这部分为空，形如**，则移到后面的模式进行匹配
			return simpleMatch(pattern.substring(nextIndex), str);
		}
        //两个*之间的部分不为空，则在串中查找这部分子串
		int partIndex = str.indexOf(part);
		while (partIndex != -1) {
            //模式串移位到第二个*之后，目标字符串移位到字串之后，递归再进行匹配
			if (simpleMatch(pattern.substring(nextIndex), str.substring(partIndex + part.length()))) {
				return true;
			}
			partIndex = str.indexOf(part, partIndex + 1);
		}
		return false;
	}

    //对不是*开头的模式，前面部分要精确匹配，然后后面的子串重新递归匹配
	return (str.length() >= firstIndex &&
		pattern.substring(0, firstIndex).equals(str.substring(0, firstIndex)) &&
		simpleMatch(pattern.substring(firstIndex), str.substring(firstIndex)));
}

```

看代码，整个匹配过程是一种扫描算法，从前往后扫描，按照 `*` 分节段一节一节匹配，因为长度不定，所以要用递归，详细说明代码上有注释。模式格式可以是: `"xxx*", "*xxx", "*xxx*"` 以及 `"xxx*yyy"` 等。

有了上面的实现，我们就有了具体的匹配工具了。下面我们就来使用PatternMatchUtils这个工具类来进行字符串的匹配。

NameMatchMethodPointcutAdvisor的实现也比较简单，就是在内部增加了NameMatchMethodPointcut属性和MappedName属性。

```java
package com.minis.aop;
public class NameMatchMethodPointcutAdvisor implements PointcutAdvisor{
	private Advice advice = null;
	private MethodInterceptor methodInterceptor;
	private String mappedName;
	private final NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
	public NameMatchMethodPointcutAdvisor() {
	}
	public NameMatchMethodPointcutAdvisor(Advice advice) {
		this.advice = advice;
	}
	public void setMethodInterceptor(MethodInterceptor methodInterceptor) {
		this.methodInterceptor = methodInterceptor;
	}
	public MethodInterceptor getMethodInterceptor() {
		return this.methodInterceptor;
	}
	public void setAdvice(Advice advice) {
		this.advice = advice;
		MethodInterceptor mi = null;
		if (advice instanceof BeforeAdvice) {
			mi = new MethodBeforeAdviceInterceptor((MethodBeforeAdvice)advice);
		}
		else if (advice instanceof AfterAdvice){
			mi = new AfterReturningAdviceInterceptor((AfterReturningAdvice)advice);
		}
		else if (advice instanceof MethodInterceptor) {
			mi = (MethodInterceptor)advice;
		}
		setMethodInterceptor(mi);
	}
	@Override
	public Advice getAdvice() {
		return this.advice;
	}
	@Override
	public Pointcut getPointcut() {
		return pointcut;
	}
	public void setMappedName(String mappedName) {
		this.mappedName = mappedName;
		this.pointcut.setMappedName(this.mappedName);
	}
}

```

上述实现代码对新增的Pointcut和MappedName属性进行了处理，这正好与我们定义的XML配置文件保持一致。而匹配的工作，则交给NameMatchMethodPointcut中的matches方法完成。如配置文件中的mappedName设置成了 `"do*"`，意味着所有do开头的方法都会匹配到。

```plain
<bean id="advisor" class="com.minis.aop.NameMatchMethodPointcutAdvisor">
    <property type="com.minis.aop.Advice" name="advice" ref="beforeAdvice"/>
    <property type="String" name="mappedName" value="do*"/>
</bean>

```

另外，我们还要注意setAdvice()这个方法，它现在通过advice来设置相应的Intereceptor，这一段逻辑以前是放在ProxyFactoryBean的initializeAdvisor()方法中的，现在移到了这里。现在这个新的Advisor就可以支持按照规则匹配方法来进行逻辑增强了。

## 相关类的改造

在上述工作完成后，相关的一些类也需要改造。JdkDynamicAopProxy类中的实现，现在我们不再需要将方法名写死了。你可以看一下改造之后的代码。

```java
package com.minis.aop;
public class JdkDynamicAopProxy implements AopProxy, InvocationHandler {
    Object target;
    PointcutAdvisor advisor;
    public JdkDynamicAopProxy(Object target, PointcutAdvisor advisor) {
        this.target = target;
        this.advisor = advisor;
    }
    @Override
    public Object getProxy() {
        Object obj = Proxy.newProxyInstance(JdkDynamicAopProxy.class.getClassLoader(), target.getClass().getInterfaces(), this);
        return obj;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> targetClass = (target != null ? target.getClass() : null);
        if (this.advisor.getPointcut().getMethodMatcher().matches(method, targetClass)) {
            MethodInterceptor interceptor = this.advisor.getMethodInterceptor();
            MethodInvocation invocation =
                    new ReflectiveMethodInvocation(proxy, target, method, args, targetClass);
            return interceptor.invoke(invocation);
        }
        return null;
    }
}

```

看核心方法 **invoke()**，以前的代码是 method.getName().equals(“doAction”)，即判断名字必须等于"doAction"，现在的判断条件则更具备扩展性了，是用Pointcut的matcher进行匹配校验。代码是 `this.advisor.getPointcut().getMethodMatcher().matches(method, targetClass))` 这一句。

原本定义的Advisor改为了更加具有颗粒度的PointcutAdvisor，自然连带着其他引用类也要一并修改。

DefaultAopProxyFactory的createAopProxy()方法中，Advisor参数现在就可以使用PointcutAdvisor类型了。

```java
package com.minis.aop;
public class DefaultAopProxyFactory implements AopProxyFactory{
    @Override
    public AopProxy createAopProxy(Object target, PointcutAdvisor advisor) {
        return new JdkDynamicAopProxy(target, advisor);
    }
}

```

而ProxyFactoryBean可以简化一下。

```java
package com.minis.aop;
public class ProxyFactoryBean implements FactoryBean<Object>, BeanFactoryAware {
    private BeanFactory beanFactory;
    private AopProxyFactory aopProxyFactory;
    private String interceptorName;
    private String targetName;
    private Object target;
    private ClassLoader proxyClassLoader = ClassUtils.getDefaultClassLoader();
    private Object singletonInstance;
    private PointcutAdvisor advisor;
    public ProxyFactoryBean() {
        this.aopProxyFactory = new DefaultAopProxyFactory();
    }

    //省略一些getter/setter

    protected AopProxy createAopProxy() {
        return getAopProxyFactory().createAopProxy(target, this.advisor);
    }
    @Override
    public Object getObject() throws Exception {
        initializeAdvisor();
        return getSingletonInstance();
    }
    private synchronized void initializeAdvisor() {
        Object advice = null;
        MethodInterceptor mi = null;
        try {
            advice = this.beanFactory.getBean(this.interceptorName);
        } catch (BeansException e) {
            e.printStackTrace();
        }
        this.advisor = (PointcutAdvisor) advice;
    }
    private synchronized Object getSingletonInstance() {
        if (this.singletonInstance == null) {
            this.singletonInstance = getProxy(createAopProxy());
        }
        return this.singletonInstance;
    }
}

```

可以看到，ProxyFactoryBean中的initializeAdvisor方法里，不再需要判断不同的Interceptor类型，相关实现被抽取到了NameMatchMethodPointcutAdvisor这个类中。

## 测试

最后，我们还是用以前的HelloWorldBean作为测试，现在可以这么写测试程序了。

```plain
	@Autowired
	IAction action;

	@RequestMapping("/testaop")
	public void doTestAop(HttpServletRequest request, HttpServletResponse response) {
		action.doAction();
	}
	@RequestMapping("/testaop2")
	public void doTestAop2(HttpServletRequest request, HttpServletResponse response) {
		action.doSomething();
	}

```

配置文件就是我们最早希望达成的样子。

```plain
<bean id="realaction" class="com.test.service.Action1" />
<bena id="beforeAdvice" class="com.test.service.MyBeforeAdvice" />
<bean id="advisor" class="com.minis.aop.NameMatchMethodPointcutAdvisor">
    <property type="com.minis.aop.Advice" name="advice" ref="beforeAdvice"/>
    <property type="String" name="mappedName" value="do*"/>
</bean>
<bean id="action" class="com.minis.aop.ProxyFactoryBean">
    <property type="String" name="interceptorName" value="advisor" />
    <property type="java.lang.Object" name="target" ref="realaction"/>
</bean>

```

使用了新的Advisor， **匹配规则是 `"do*"`，真正执行的类是Action1**。

```plain
package com.test.service;
public class Action1 implements IAction {
	@Override
	public void doAction() {
		System.out.println("really do action1");
	}
	@Override
	public void doSomething() {
		System.out.println("really do something");
	}
}

```

这个Action1里面有两个方法， **doAction和doSomething**，名字都是以do开头的。因此，上面的配置规则会使业务程序在调用它们二者的时候，动态插入定义在MyBeforeAdvice里的逻辑。

## 小结

这节课，我们对查找方法名的办法进行了扩展，让系统可以按照某个规则来匹配方法名，这样便于统一处理。这个概念叫做Pointcut，熟悉数据库操作的人，可以把这个概念类比为SQL语句中的where条件。

基本的实现思路是使用一个特殊的Advisor，这个Advisor接收一个模式串，而这个模式串也是可以由用户配置在外部文件中的，然后提供isMatch() 方法，支持按照名称进行模式匹配。具体的字符串匹配工作，采用从前到后的扫描技术，分节段进行校验。

这两节课我们接触到了几个概念，我们再梳理一下。

- Join Point：连接点，连接点的含义是指明切面可以插入的地方，这个点可以在函数调用时，或者正常流程中某一行等位置，加入切面的处理逻辑，来实现代码增强的效果。
- Advice：通知，表示在特定的连接点采取的操作。
- Advisor：通知者，它实现了Advice。
- Interceptor：拦截器，作用是拦截流程，方便处理。
- Pointcut：切点。

完整源代码参见 [https://github.com/YaleGuo/minis](https://github.com/YaleGuo/minis)。

## 课后题

学完这节课的内容，我也给你留一道思考题。

我们现在实现的匹配规则是按照\*模式串进行匹配，如果需要支持不同的规则，应该如何改造我们的框架呢？

欢迎你在留言区与我交流讨论，也欢迎你把这节课分享给需要的朋友。我们下节课见！

# 20｜AutoProxyCreator：如何自动添加动态代理？

你好，我是郭屹，今天我们继续手写MiniSpring，这也是AOP正文部分的最后一节。今天我们将完成一个有模有样的AOP解决方案。

## 问题的引出

前面，我们已经实现了通过动态代理技术在运行时进行逻辑增强，并引入了Pointcut，实现了代理方法的通配形式。到现在，AOP的功能貌似已经基本实现了，但目前还有一个较大的问题，具体是什么问题呢？我们查看aplicationContext.xml里的这段配置文件来一探究竟。

```xml
<bean id="realaction" class="com.test.service.Action1" />
<bean id="action" class="com.minis.aop.ProxyFactoryBean">
    <property type="String" name="interceptorName" value="advisor" />
    <property type="java.lang.Object" name="target" ref="realaction"/>
</bean>

```

看这个配置文件可以发现，在ProxyFactoryBean的配置中，有个Object类型的属性：target。在这里我们的赋值ref是realactionbean，对应Action1这个类。也就是说，给Action1这个Bean动态地插入逻辑，达成AOP的目标。

在这里，一次AOP的配置对应一个目标对象，如果整个系统就只需要为一个对象进行增强操作，这自然没有问题，配置一下倒也不会很麻烦，但在一个稍微有规模的系统中，我们有成百上千的目标对象，在这种情况下一个个地去配置则无异于一场灾难。

一个实用的AOP解决方案，应该可以 **用一个简单的匹配规则代理多个目标对象**。这是我们这节课需要解决的问题。

## 匹配多个目标对象的思路

在上节课，我们其实处理过类似的问题，就是当时我们的目标方法只能是一个固定的方法名doAction()，我们就提出了Pointcut这个概念，用一个模式来通配方法名，如 `do*`、 `do*Action` 之类的字符串模式。

Pointcut这个概念解决了一个目标对象内部多个方法的匹配问题。这个办法也能给我们灵感，我们就借鉴这个思路，用类似的手段来解决匹配多个目标对象的问题。

因此，我们想象中当解决方案实现之后，应该是这么配置的。

```plain
<bean id="genaralProxy" class="GeneralProxy" >
    <property type="String" name="pattern" value="action*" />
    <property type="String" name="interceptorName" value="advisor" />
</bean>

```

上面的配置里有一个通用的ProxyBean，它用一个模式串pattern来匹配目标对象，作为例子这里就是 `action*`，表示所有名字以action开头的对象都是目标对象。

这个想法好像成立，但是我们知道，IoC容器内部所有的Bean是相互独立且平等的，这个GeneralProxy也就是一个普通的Bean。那么作为一个普通的Bean，它怎么能影响到别的Bean呢？它如何能做到给别的Bean动态创建代理呢？这个办法有这样一个关键的难点。

我们反过来思考，如果能找个办法让这个General Proxy影响到别的Bean，再根据规则决定给这些Bean加上动态代理（这一点我们之前就实现过了），是不是就可以了？

那么在哪个时序点能做这个事情呢？我们再回顾一下Bean的创建过程：第一步，IoC容器扫描配置文件，加载Bean的定义。第二步，通过getBean()这个方法创建Bean实例，这一步又分成几个子步骤：

1. 创建Bean的毛坯实例；
2. 填充Properties；
3. 执行postProcessBeforeInitialization；
4. 调用init-method方法；
5. 执行postProcessAfterInitialization。

后三个子步骤，实际上都是在每一个Bean实例创建好之后可以进行的后期处理。那么我们就可以利用这个时序，把自动生成代理这件事情交给后期处理来完成。在我们的IoC容器里，有一个现成的机制，叫 **BeanPostProcessor**，它能在每一个Bean创建的时候进行后期修饰，也就是上面的3和5两个子步骤其实都是调用的BeanPostProcessor里面的方法。所以现在就比较清晰了，我们考虑用BeanPostProcessor实现自动生成目标对象代理。

## 利用BeanPostProcessor自动创建代理

创建动态代理的核心是 **把传进来的Bean包装成一个ProxyFactoryBean**，改头换面变成一个动态的代理，里面包含了真正的业务对象，这一点我们已经在前面的工作中做好了。现在是要自动创建这个动态代理，它的核心就是通过BeanPostProcessor来为每一个Bean自动完成创建动态代理的工作。

我们用一个BeanNameAutoProxyCreator类实现这个功能，顾名思义，这个类就是根据Bean的名字匹配来自动创建动态代理的，你可以看一下相关代码。

```java
package com.minis.aop.framework.autoproxy;
public class BeanNameAutoProxyCreator implements BeanPostProcessor{
    String pattern; //代理对象名称模式，如action*
    private BeanFactory beanFactory;
    private AopProxyFactory aopProxyFactory;
    private String interceptorName;
    private PointcutAdvisor advisor;
    public BeanNameAutoProxyCreator() {
        this.aopProxyFactory = new DefaultAopProxyFactory();
    }
    //核心方法。在bean实例化之后，init-method调用之前执行这个步骤。
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (isMatch(beanName, this.pattern)) {
            ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean(); //创建以恶ProxyFactoryBean
            proxyFactoryBean.setTarget(bean);
            proxyFactoryBean.setBeanFactory(beanFactory);
            proxyFactoryBean.setAopProxyFactory(aopProxyFactory);
            proxyFactoryBean.setInterceptorName(interceptorName);
            return proxyFactoryBean;
        }
        else {
            return bean;
        }
    }
    protected boolean isMatch(String beanName, String mappedName) {
        return PatternMatchUtils.simpleMatch(mappedName, beanName);
    }
}

```

通过代码可以知道，在postProcessBeforeInitialization方法中，判断了Bean的名称是否符合给定的规则，也就是isMatch(beanName, this.pattern)这个方法。往下追究一下，发现这个isMatch()就是直接调用的PatternMatchUtils.simpleMatch()，跟上一节课的通配方法名一样。所以如果Bean的名称匹配上了，那我们就用和以前创建动态代理一样的办法来自动生成代理。

```java
ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
proxyFactoryBean.setTarget(bean);
proxyFactoryBean.setBeanFactory(beanFactory);
proxyFactoryBean.setAopProxyFactory(aopProxyFactory);
proxyFactoryBean.setInterceptorName(interceptorName);

```

这里我们还是用到了ProxyFactoryBean，跟以前一样，只不过这里是经过了BeanPostProcessor。因此，按照IoC容器的规则，这一切不再是手工的了，而是对每一个符合规则Bean都会这样做一次动态代理，就可以完成我们的工作了。

现在我们只要把这个BeanPostProcessor配置到XML文件里就可以了。

```plain
<bean id="autoProxyCreator" class="com.minis.aop.framework.autoproxy.BeanNameAutoProxyCreator" >
    <property type="String" name="pattern" value="action*" />
    <property type="String" name="interceptorName" value="advisor" />
</bean>

```

IoC容器扫描配置文件的时候，会把所有的BeanPostProcessor对象加载到Factory中生效，每一个Bean都会过一遍手。

## getBean方法的修改

工具准备好了，这个BeanPostProcessor会自动创建动态代理。为了使用这个Processor，对应的AbstractBeanFactory类里的getBean()方法需要同步修改。你可以看一下修改后getBean的实现。

```java
    public Object getBean(String beanName) throws BeansException{
      Object singleton = this.getSingleton(beanName);
      if (singleton == null) {
         singleton = this.earlySingletonObjects.get(beanName);
         if (singleton == null) {
            BeanDefinition bd = beanDefinitionMap.get(beanName);
            if (bd != null) {
               singleton=createBean(bd);
               this.registerBean(beanName, singleton);
               if (singleton instanceof BeanFactoryAware) {
                  ((BeanFactoryAware) singleton).setBeanFactory(this);
               }
               //用beanpostprocessor进行后期处理
               //step 1 : postProcessBeforeInitialization调用processor相关方法
               singleton = applyBeanPostProcessorsBeforeInitialization(singleton, beanName);
               //step 2 : init-method
               if (bd.getInitMethodName() != null && !bd.getInitMethodName().equals("")) {
                  invokeInitMethod(bd, singleton);
               }
               //step 3 : postProcessAfterInitialization
               applyBeanPostProcessorsAfterInitialization(singleton, beanName);
               this.removeSingleton(beanName);
               this.registerBean(beanName, singleton);
            }
            else {
               return null;
            }
         }
      }
      else {
      }
      //process Factory Bean
      if (singleton instanceof FactoryBean) {
         return this.getObjectForBeanInstance(singleton, beanName);
      }
      else {
      }
      return singleton;
   }

```

上述代码中主要修改这一行：

```java
singleton = applyBeanPostProcessorsBeforeInitialization(singleton, beanName);

```

代码里会调用Processor的postProcessBeforeInitialization方法，并返回singleton。这一段代码的功能是如果这个Bean的名称符合某种规则，就会自动创建Factory Bean，这个Factory Bean里面会包含一个动态代理对象用来返回自定义的实例。

于是，getBean的时候，除了创建Bean实例，还会用BeanPostProcessor进行后期处理，对满足规则的Bean进行包装，改头换面成为一个Factory Bean。

## 测试

到这里，我们就完成自动创建动态代理的工作了，简单测试一下。

修改applicationContext.xml配置文件，增加一些配置。

```xml
<bean id="autoProxyCreator" class="com.minis.aop.framework.autoproxy.BeanNameAutoProxyCreator" >
    <property type="String" name="pattern" value="action*" />
    <property type="String" name="interceptorName" value="advisor" />
</bean>

<bean id="action" class="com.test.service.Action1" />
<bean id="action2" class="com.test.service.Action2" />

<bena id="beforeAdvice" class="com.test.service.MyBeforeAdvice" />
<bean id="advisor" class="com.minis.aop.NameMatchMethodPointcutAdvisor">
    <property type="com.minis.aop.Advice" name="advice" ref="beforeAdvice"/>
    <property type="String" name="mappedName" value="do*"/>
</bean>

```

这里我们配置了两个Bean，BeanPostProcessor和Advisor。

相应地，controller层的HelloWorldBean增加一段代码。

```plain
@Autowired
IAction action;

@RequestMapping("/testaop")
public void doTestAop(HttpServletRequest request, HttpServletResponse response) {
	action.doAction();
}
@RequestMapping("/testaop2")
public void doTestAop2(HttpServletRequest request, HttpServletResponse response) {
	action.doSomething();
}

@Autowired
IAction action2;

@RequestMapping("/testaop3")
public void doTestAop3(HttpServletRequest request, HttpServletResponse response) {
	action2.doAction();
}
@RequestMapping("/testaop4")
public void doTestAop4(HttpServletRequest request, HttpServletResponse response) {
	action2.doSomething();
}

```

这里，我们用到了这两个Bean，action和action2，每个Bean里面都有doAction()和doSomething()两个方法。

通过配置文件可以看到，在Processor的Pattern配置里，通配 `action*` 可以匹配所有以action开头的Bean。在Advisor的MappedName配置里，通配 `do*`，就可以匹配所有以do开头的方法。

运行一下，就可以看到效果了。这两个Bean里的两个方法都加上了增强，说明系统在调用这些Bean的方法时自动插入了逻辑。

## 小结

这节课，我们对匹配Bean的办法进行了扩展，使系统可以按照某个规则来匹配某些Bean，这样就不用一个Bean一个Bean地配置动态代理了。

实现的思路是利用Bean的时序，使用一个BeanPostProcessor进行后期处理。这个Processor接收一个模式串，而这个模式也是可以由用户配置在外部文件里的，然后提供isMatch() 方法，支持根据名称进行模式匹配。具体的字符串匹配工作，和上节课一样，也是采用从前到后的扫描技术，分节段进行校验。匹配上之后，还是利用以前的ProxyFactoryBean创建动态代理。这里要理解一点，就是系统会自动把应用程序员配置的业务Bean改头换面，让它变成一个Factory Bean，里面包含的是业务Bean的动态代理。

这个方案能用是因为之前IoC容器里提供的这个BeanPostProcessor机制，所以这里我们再次看到了IoC容器的强大之处。

到这里，我们的AOP方案就完成了。这是基于JDK的方案，对于理解AOP原理很有帮助。

完整源代码参见 [https://github.com/YaleGuo/minis](https://github.com/YaleGuo/minis)。

## 课后题

学完这节课，我也给你留一道思考题。AOP经常用来处理数据库事务，如何用我们现在的AOP架构实现简单的事务处理呢？欢迎你在留言区与我交流讨论，也欢迎你把这节课分享给需要的朋友。我们下节课见！

# 21｜再回首： 如何实现Spring AOP?

你好，我是郭屹。

到这一节课，我们的Spring AOP部分也就结束了，你是不是跟随我的这个步骤也实现了自己的AOP呢？欢迎你把你的实现代码分享出来，我们一起讨论，共同进步！为了让你对这一章的内容掌握得更加牢固，我们对AOP的内容做一个重点回顾。

### 重点回顾

Spring AOP是Spring框架的一个核心组件之一，是Spring面向切面编程的探索。面向对象和面向切面，两者一纵一横，编织成一个完整的程序结构。

在AOP编程中，Aspect指的是横切逻辑（cross-cutting concerns），也就是那些和基本业务逻辑无关，但是却是很多不同业务代码共同需要的功能，比如日志记录、安全检查、事务管理，等等。Aspect能够通过Join point，Advice和Pointcut来定义，在运行的时候，能够自动在Pointcut范围内的不同类型的Advice作用在不同的Join point上，实现对横切逻辑的处理。

所以，这个AOP编程可以看作是一种以Aspect为核心的编程方式，它强调的是将横切逻辑作为一个独立的属性进行处理，而不是直接嵌入到基本业务逻辑中。这样做，可以提高代码的可复用性、可维护性和可扩展性，使得代码更容易理解和设计。

AOP的实现，是基于JDK动态代理的，站在Java的角度，这很自然，概念很容易实现，但是效率不高，限制也比较多。可以说AOP的实现是Spring框架中少数不尽人意的一部分，也可以看出世界顶级高手也有考虑不周到的地方。

那我们在课程中是如何一步步实现AOP的呢？

我们是基于JDK来实现的，因为比较自然、容易。我们先是引入了Java的动态代理技术，探讨如何用这个技术动态插入业务逻辑。然后我们进一步抽取动态业务逻辑，引入Spring里的Interceptor和Advice的概念。之后通过引入Spring的PointCut概念，进行advice作用范围的定义，让系统知道前面定义的Advice 会对哪些对象产生影响。最后为了免除手工逐个配置PointCut和Interceptor的工作，我们就通过一个自动化的机制自动生成动态代理。最终实现了一个有模有样的AOP解决方案。

好了，回顾完这一章的重点，我们再来看一下我每节课后给你布置的思考题。题目和答案我都放到下面了，不要偷懒，好好思考之后再来看答案。

### 17｜动态代理：如何在运行时插入逻辑？

#### 思考题

如果MiniSpring想扩展到支持Cglib，程序应该从哪里下手改造？

#### 参考答案

我们的动态代理包装在AopProxy这个接口中，对JDK动态代理技术，使用了JdkDynamicAopProxy这个类来实现，所以平行的做法，对于Cglib技术，我们就可以新增一个CglibAopProxy类进行实现。

同时，采用哪一种AOP Proxy可以由工厂方法决定，也就是在ProxyFactoryBean中所使用的aopProxyFactory，它在初始化的时候有个默认实现，即DefaultAopProxyFactory。我们可以将这个类的createAopProxy()方法改造一下。

```plain
	public class DefaultAopProxyFactory implements AopProxyFactory {
		public AopProxy createAopProxy(Object target) {
			if (targetClass.isInterface() || Proxy.isProxyClass(targetClass)) {
				return new JdkDynamicAopProxy(target);
			}
			return new CglibAopProxy(config);
		}
	}

```

根据某些条件决定使用JdkDynamicAopProxy还是CglibAopProxy，或者通过配置文件给一个属性来配置也可以。

### 18｜拦截器 ：如何在方法前后进行拦截？

#### 思考题

如果我们希望beforeAdvice能在某种情况下阻止目标方法的调用，应该从哪里下手改造改造我们的程序？

#### 参考答案

答案在MethodBeforeAdviceInterceptor 的实现中，看它的invoke方法。

```plain
	public class MethodBeforeAdviceInterceptor implements MethodInterceptor {
		public Object invoke(MethodInvocation mi) throws Throwable {
			this.advice.before(mi.getMethod(), mi.getArguments(), mi.getThis());
			return mi.proceed();
		}
	}

```

这个方法先调用advice.before()，然后再调用目标方法。所以如果我们希望beforeAdvice能够阻止流程继续，可以将advice.before()接口改造成有一个boolean返回值，规定返回false则不调用mi.proceed()。

### 19｜Pointcut ：如何批量匹配代理方法？

#### 思考题

我们现在实现的匹配规则是按照 `*` 模式串进行匹配，如果有不同的规则，应该如何改造呢？

#### 参考答案

如果仍然按照名字来匹配，那就可以改造NameMatchMethodPointcut类，它现在的核心代码是：

```plain
	public class NameMatchMethodPointcut implements MethodMatcher,Pointcut{
		private String mappedName = "";
		protected boolean isMatch(String methodName, String mappedName) {
			return PatternMatchUtils.simpleMatch(mappedName, methodName);
		}
	}

```

默认的实现用的是PatternMatchUtils.simpleMatch()，比较简单的模式串。我们可以给PatternMatchUtils增加一个方法，如regExprMatch()正则表达式匹配，在这里接收正则表达式串，进行匹配校验。

如果超出名字匹配的范围，需要用到不一样的匹配规则，就可以并列增加一个OtherMatchMethodPointcut类h和响应的advisor类，自己实现。并在配置文件里指定使用这个Advisor。

```plain
	<bean id="advisor" class="com.minis.aop.OtherMatchMethodPointcutAdvisor">
    </bean>
    <bean id="action" class="com.minis.aop.ProxyFactoryBean">
        <property type="String" name="interceptorName" value="advisor" />
    </bean>

```

### 20｜AutoProxyCreator：如何自动添加动态代理？

#### 思考题

AOP时常用于数据库事务处理，如何用我们现在的AOP架构实现简单的事务处理？

#### 参考答案

针对数据库事务，手工代码简化到了极致，就是执行SQL之前执行conn.setAutoCommit(false),在执行完SQL之后，再执行conn.commit()。因此，我们用一个MethodInterceptor就可以简单实现。

假定有了这样一个interceptor。

```plain
<bean id="transactionInterceptor" class="TransactionInterceptor" />

```

这个Interceptor拦截目标方法后添加事务处理逻辑，因此需要改造一下。

```plain
public class TransactionInterceptor implements MethodInterceptor{
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
conn.setAutoCommit(false);
Object ret=invocation.proceed();
conn.commit();
		return ret;
	}
}

```

从代码里可以看到，这里需要一个conn，因此我们要设法将数据源信息注入到这里。

我们可以抽取出一个TranactionManager类，大体如下：

```plain
public class TransactionManager {
	@Autowired
	private DataSource dataSource;
	Connection conn = null;

	protected void doBegin() {
		conn = dataSource.getConnection();
		if (conn.getAutoCommit()) {
			conn.setAutoCommit(false);
		}
	}
	protected void doCommit() {
		conn.commit();
	}
}

```

由这个transaction manager负责数据源以及开始和提交事务，然后将这个transaction manager作为一个Bean注入Interceptor，因此配置应该是这样的。

```plain
<bean id="transactionInterceptor" class="TransactionInterceptor" >
    <property type="TransactionManager" name="transactionManager" value="txManager" />
</bean>
<bean id="txManager" class="TransactionManager">
</bean>

```

所以Interceptor最后应该改造成这个样子：

# 21｜再回首： 如何实现Spring AOP?

你好，我是郭屹。

到这一节课，我们的Spring AOP部分也就结束了，你是不是跟随我的这个步骤也实现了自己的AOP呢？欢迎你把你的实现代码分享出来，我们一起讨论，共同进步！为了让你对这一章的内容掌握得更加牢固，我们对AOP的内容做一个重点回顾。

### 重点回顾

Spring AOP是Spring框架的一个核心组件之一，是Spring面向切面编程的探索。面向对象和面向切面，两者一纵一横，编织成一个完整的程序结构。

在AOP编程中，Aspect指的是横切逻辑（cross-cutting concerns），也就是那些和基本业务逻辑无关，但是却是很多不同业务代码共同需要的功能，比如日志记录、安全检查、事务管理，等等。Aspect能够通过Join point，Advice和Pointcut来定义，在运行的时候，能够自动在Pointcut范围内的不同类型的Advice作用在不同的Join point上，实现对横切逻辑的处理。

所以，这个AOP编程可以看作是一种以Aspect为核心的编程方式，它强调的是将横切逻辑作为一个独立的属性进行处理，而不是直接嵌入到基本业务逻辑中。这样做，可以提高代码的可复用性、可维护性和可扩展性，使得代码更容易理解和设计。

AOP的实现，是基于JDK动态代理的，站在Java的角度，这很自然，概念很容易实现，但是效率不高，限制也比较多。可以说AOP的实现是Spring框架中少数不尽人意的一部分，也可以看出世界顶级高手也有考虑不周到的地方。

那我们在课程中是如何一步步实现AOP的呢？

我们是基于JDK来实现的，因为比较自然、容易。我们先是引入了Java的动态代理技术，探讨如何用这个技术动态插入业务逻辑。然后我们进一步抽取动态业务逻辑，引入Spring里的Interceptor和Advice的概念。之后通过引入Spring的PointCut概念，进行advice作用范围的定义，让系统知道前面定义的Advice 会对哪些对象产生影响。最后为了免除手工逐个配置PointCut和Interceptor的工作，我们就通过一个自动化的机制自动生成动态代理。最终实现了一个有模有样的AOP解决方案。

好了，回顾完这一章的重点，我们再来看一下我每节课后给你布置的思考题。题目和答案我都放到下面了，不要偷懒，好好思考之后再来看答案。

### 17｜动态代理：如何在运行时插入逻辑？

#### 思考题

如果MiniSpring想扩展到支持Cglib，程序应该从哪里下手改造？

#### 参考答案

我们的动态代理包装在AopProxy这个接口中，对JDK动态代理技术，使用了JdkDynamicAopProxy这个类来实现，所以平行的做法，对于Cglib技术，我们就可以新增一个CglibAopProxy类进行实现。

同时，采用哪一种AOP Proxy可以由工厂方法决定，也就是在ProxyFactoryBean中所使用的aopProxyFactory，它在初始化的时候有个默认实现，即DefaultAopProxyFactory。我们可以将这个类的createAopProxy()方法改造一下。

```plain
	public class DefaultAopProxyFactory implements AopProxyFactory {
		public AopProxy createAopProxy(Object target) {
			if (targetClass.isInterface() || Proxy.isProxyClass(targetClass)) {
				return new JdkDynamicAopProxy(target);
			}
			return new CglibAopProxy(config);
		}
	}

```

根据某些条件决定使用JdkDynamicAopProxy还是CglibAopProxy，或者通过配置文件给一个属性来配置也可以。

### 18｜拦截器 ：如何在方法前后进行拦截？

#### 思考题

如果我们希望beforeAdvice能在某种情况下阻止目标方法的调用，应该从哪里下手改造改造我们的程序？

#### 参考答案

答案在MethodBeforeAdviceInterceptor 的实现中，看它的invoke方法。

```plain
	public class MethodBeforeAdviceInterceptor implements MethodInterceptor {
		public Object invoke(MethodInvocation mi) throws Throwable {
			this.advice.before(mi.getMethod(), mi.getArguments(), mi.getThis());
			return mi.proceed();
		}
	}

```

这个方法先调用advice.before()，然后再调用目标方法。所以如果我们希望beforeAdvice能够阻止流程继续，可以将advice.before()接口改造成有一个boolean返回值，规定返回false则不调用mi.proceed()。

### 19｜Pointcut ：如何批量匹配代理方法？

#### 思考题

我们现在实现的匹配规则是按照 `*` 模式串进行匹配，如果有不同的规则，应该如何改造呢？

#### 参考答案

如果仍然按照名字来匹配，那就可以改造NameMatchMethodPointcut类，它现在的核心代码是：

```plain
	public class NameMatchMethodPointcut implements MethodMatcher,Pointcut{
		private String mappedName = "";
		protected boolean isMatch(String methodName, String mappedName) {
			return PatternMatchUtils.simpleMatch(mappedName, methodName);
		}
	}

```

默认的实现用的是PatternMatchUtils.simpleMatch()，比较简单的模式串。我们可以给PatternMatchUtils增加一个方法，如regExprMatch()正则表达式匹配，在这里接收正则表达式串，进行匹配校验。

如果超出名字匹配的范围，需要用到不一样的匹配规则，就可以并列增加一个OtherMatchMethodPointcut类h和响应的advisor类，自己实现。并在配置文件里指定使用这个Advisor。

```plain
	<bean id="advisor" class="com.minis.aop.OtherMatchMethodPointcutAdvisor">
    </bean>
    <bean id="action" class="com.minis.aop.ProxyFactoryBean">
        <property type="String" name="interceptorName" value="advisor" />
    </bean>

```

### 20｜AutoProxyCreator：如何自动添加动态代理？

#### 思考题

AOP时常用于数据库事务处理，如何用我们现在的AOP架构实现简单的事务处理？

#### 参考答案

针对数据库事务，手工代码简化到了极致，就是执行SQL之前执行conn.setAutoCommit(false),在执行完SQL之后，再执行conn.commit()。因此，我们用一个MethodInterceptor就可以简单实现。

假定有了这样一个interceptor。

```plain
<bean id="transactionInterceptor" class="TransactionInterceptor" />

```

这个Interceptor拦截目标方法后添加事务处理逻辑，因此需要改造一下。

```plain
public class TransactionInterceptor implements MethodInterceptor{
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
conn.setAutoCommit(false);
Object ret=invocation.proceed();
conn.commit();
		return ret;
	}
}

```

从代码里可以看到，这里需要一个conn，因此我们要设法将数据源信息注入到这里。

我们可以抽取出一个TranactionManager类，大体如下：

```plain
public class TransactionManager {
	@Autowired
	private DataSource dataSource;
	Connection conn = null;

	protected void doBegin() {
		conn = dataSource.getConnection();
		if (conn.getAutoCommit()) {
			conn.setAutoCommit(false);
		}
	}
	protected void doCommit() {
		conn.commit();
	}
}

```

由这个transaction manager负责数据源以及开始和提交事务，然后将这个transaction manager作为一个Bean注入Interceptor，因此配置应该是这样的。

```plain
<bean id="transactionInterceptor" class="TransactionInterceptor" >
    <property type="TransactionManager" name="transactionManager" value="txManager" />
</bean>
<bean id="txManager" class="TransactionManager">
</bean>

```

所以Interceptor最后应该改造成这个样子：

```Java
public class TransactionInterceptor implements MethodInterceptor{
  TransactionManager transactionManager;
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
transactionManager.doBegin();
Object ret=invocation.proceed();
transactionManager.doCommit();
		return ret;
	}
}
```

