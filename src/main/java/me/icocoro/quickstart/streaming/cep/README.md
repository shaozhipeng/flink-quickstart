### FlinkCEP API

需要注意的是：

> 进行模式匹配的DataStream中的事件对象，必须实现正确的equals()和hashCode()方法，因为FlinkCEP使用它们来比较和匹配事件。

模式API允许我们定义要从输入流中提取的复杂模式序列。  
每个复杂模式序列是由多个简单模式组成，例如模式：寻找具有相同属性的单个事件。  
我们将简单模式称为模式，将最终在数据流中进行搜索匹配的复杂模式序列称为模式序列。  
我们可以把模式序列看做是多个模式组成的图，其中还有基于用户指定的条件【如：event.getName().equals("end")】进行的从一个模式到下一个模式的各种转换。  
什么是【匹配】（这里指匹配到的“结果”）？  
匹配是一系列输入事件，这些事件通过一系列有效的模式转换，能够访问复杂模式图的所有模式。

需要注意的是：

> 每个模式必须具有唯一的名称，我们可以使用模式名称来标识该模式匹配到的事件。
> 模式名称不能包含【:】冒号字符。

### 单个模式

一个模式既可以是单例的，也可以是循环的。  
单例模式接受单个事件，而循环模式可以接受多个事件。  
在模式匹配符号中，模式【a b+ c？d】（【a】，后跟一个或多个【b】，可选地后跟【c】，后跟【d】），其中【a】、【c?】、和【d】是单例模式，而【b+】是循环模式。  
一般情况下，模式都是单例模式，我们可以使用量词【Quantifiers】将其转换为循环模式。  
每个模式可以带有一个或多个条件，这些条件是基于事件接收进行定义的。或者说，每个模式通过一个或多个条件来匹配和接收事件。

#### 量词

在FlinkCEP中，我们可以使用以下方式指定循环模式：
1. pattern.oneOrMore() 一个给定的事件有一次或多次出现，例如上面提到的b+。
2. pattern.times(#ofTimes) 一个给定类型的事件出现了指定次数，例如4次。
3. pattern.times(#fromTimes, #toTimes) 一个给定类型的事件出现的次数在指定次数范围内，例如2~4次。

可以使用pattern.greedy()方法将模式变成循环模式，但是不能让一组模式都变成循环模式。  
使用pattern.optional()方法将循环模式变成可选的，即可以是循环模式也可以是单个模式。  
greedy：repeating as many as possible 所谓贪婪的意思就是尽可能的重复【还是不够形象】。

##### FlinkCEPTest

times(4)

```bash
2> LoginWarning{userId='1', type='192.168.0.1', ip='fail'}
```

times(2, 4)

```bash
2> LoginWarning{userId='1', type='192.168.0.1', ip='fail'}
1> LoginWarning{userId='4', type='192.168.10.10', ip='fail'}
2> LoginWarning{userId='1', type='192.168.0.1', ip='fail'}
2> LoginWarning{userId='1', type='192.168.0.2', ip='fail'}
1> LoginWarning{userId='4', type='192.168.10.10', ip='fail'}
1> LoginWarning{userId='4', type='192.168.10.11', ip='fail'}
2> LoginWarning{userId='1', type='192.168.0.1', ip='fail'}
2> LoginWarning{userId='1', type='192.168.0.2', ip='fail'}
2> LoginWarning{userId='1', type='192.168.0.3', ip='fail'}
2> LoginWarning{userId='3', type='192.168.10.10', ip='fail'}
```

oneOrMore() 排列组合 一共19个-有点像量子

```bash
1> LoginWarning{userId='4', type='192.168.10.10', ip='fail'}
2> LoginWarning{userId='1', type='192.168.0.1', ip='fail'}
1> LoginWarning{userId='4', type='192.168.10.10', ip='fail'}
1> LoginWarning{userId='4', type='192.168.10.11', ip='fail'}
2> LoginWarning{userId='1', type='192.168.0.1', ip='fail'}
2> LoginWarning{userId='1', type='192.168.0.2', ip='fail'}
2> LoginWarning{userId='1', type='192.168.0.1', ip='fail'}
2> LoginWarning{userId='1', type='192.168.0.2', ip='fail'}
2> LoginWarning{userId='1', type='192.168.0.3', ip='fail'}
2> LoginWarning{userId='1', type='192.168.0.1', ip='fail'}
2> LoginWarning{userId='1', type='192.168.0.2', ip='fail'}
2> LoginWarning{userId='1', type='192.168.0.3', ip='fail'}
2> LoginWarning{userId='1', type='192.168.0.4', ip='fail'}
1> LoginWarning{userId='4', type='192.168.10.10', ip='fail'}
1> LoginWarning{userId='4', type='192.168.10.11', ip='fail'}
1> LoginWarning{userId='4', type='192.168.10.12', ip='fail'}
2> LoginWarning{userId='3', type='192.168.10.10', ip='fail'}
2> LoginWarning{userId='3', type='192.168.10.10', ip='fail'}
2> LoginWarning{userId='3', type='192.168.10.11', ip='fail'}

timesOrMore(2);

```bash
1> LoginWarning{userId='4', type='192.168.10.10', ip='fail'}
1> LoginWarning{userId='4', type='192.168.10.10', ip='fail'}
1> LoginWarning{userId='4', type='192.168.10.11', ip='fail'}
2> LoginWarning{userId='1', type='192.168.0.1', ip='fail'}
2> LoginWarning{userId='1', type='192.168.0.1', ip='fail'}
2> LoginWarning{userId='1', type='192.168.0.2', ip='fail'}
2> LoginWarning{userId='1', type='192.168.0.1', ip='fail'}
2> LoginWarning{userId='1', type='192.168.0.2', ip='fail'}
2> LoginWarning{userId='1', type='192.168.0.3', ip='fail'}
2> LoginWarning{userId='3', type='192.168.10.10', ip='fail'}
```

##### 使用机架温度功率数据进行更多测试


#### 条件

为了让传入事件被模式所接受，我们给模式指定传入事件必须满足的条件，这些条件由事件本身的属性或者前面匹配过的事件的属性统计量等来设定。  
比如，事件的某个值大于5，或者大于先前接受事件的某个值的平均值。  
可以使用pattern.where()、pattern.or()、pattern.until()方法来指定条件。  
条件既可以是迭代条件IterativeConditions ，也可以是简单条件SimpleConditions。

##### 迭代条件

迭代条件：该条件基于先前接收事件的属性或其子集的统计信息来接收后续事件。  

需要注意的是：

> ctx.getEventsForPattern(...) 操作代价较大，尽量减少使用。

```java
env.setParallelism(1);


.where(new IterativeCondition<LoginEvent>() {
    @Override
    public boolean filter(LoginEvent value, Context<LoginEvent> ctx) throws Exception {
        if (!value.getType().equals("fail")) {
            return false;
        }

        int sum = value.getWeight();
        int i = 0;
        System.out.println("begin value: " + value + " value i: " + i);
//  System.out.println("xx: " + ctx.getEventsForPattern("begin").iterator().hasNext());
        for (LoginEvent event : ctx.getEventsForPattern("begin")) {
            sum += event.getWeight();
            i = i + 1;
            System.out.println("event: " + event);
        }
        System.out.println("after value: " + value + " value i: " + i);
        if (i == 0) {
            return true;
        }
        return Double.compare(sum / i, 5.0) > 0;
    }
})
.times(4);
```

匹配结果：

```bash
begin value: LoginEvent{userId='1', ip='192.168.0.1', type='fail', weight=8} value i: 0
after value: LoginEvent{userId='1', ip='192.168.0.1', type='fail', weight=8} value i: 0

begin value: LoginEvent{userId='1', ip='192.168.0.2', type='fail', weight=9} value i: 0
event: LoginEvent{userId='1', ip='192.168.0.1', type='fail', weight=8}
after value: LoginEvent{userId='1', ip='192.168.0.2', type='fail', weight=9} value i: 1

begin value: LoginEvent{userId='1', ip='192.168.0.2', type='fail', weight=9} value i: 0
event: LoginEvent{userId='1', ip='192.168.0.1', type='fail', weight=8}
after value: LoginEvent{userId='1', ip='192.168.0.2', type='fail', weight=9} value i: 1

begin value: LoginEvent{userId='1', ip='192.168.0.2', type='fail', weight=9} value i: 0
after value: LoginEvent{userId='1', ip='192.168.0.2', type='fail', weight=9} value i: 0

begin value: LoginEvent{userId='1', ip='192.168.0.3', type='fail', weight=10} value i: 0
event: LoginEvent{userId='1', ip='192.168.0.1', type='fail', weight=8}
event: LoginEvent{userId='1', ip='192.168.0.2', type='fail', weight=9}
after value: LoginEvent{userId='1', ip='192.168.0.3', type='fail', weight=10} value i: 2

begin value: LoginEvent{userId='1', ip='192.168.0.3', type='fail', weight=10} value i: 0
event: LoginEvent{userId='1', ip='192.168.0.1', type='fail', weight=8}
event: LoginEvent{userId='1', ip='192.168.0.2', type='fail', weight=9}
after value: LoginEvent{userId='1', ip='192.168.0.3', type='fail', weight=10} value i: 2

begin value: LoginEvent{userId='1', ip='192.168.0.3', type='fail', weight=10} value i: 0
event: LoginEvent{userId='1', ip='192.168.0.2', type='fail', weight=9}
after value: LoginEvent{userId='1', ip='192.168.0.3', type='fail', weight=10} value i: 1

begin value: LoginEvent{userId='1', ip='192.168.0.3', type='fail', weight=10} value i: 0
event: LoginEvent{userId='1', ip='192.168.0.2', type='fail', weight=9}
after value: LoginEvent{userId='1', ip='192.168.0.3', type='fail', weight=10} value i: 1

begin value: LoginEvent{userId='1', ip='192.168.0.3', type='fail', weight=10} value i: 0
after value: LoginEvent{userId='1', ip='192.168.0.3', type='fail', weight=10} value i: 0

begin value: LoginEvent{userId='1', ip='192.168.0.4', type='fail', weight=10} value i: 0
event: LoginEvent{userId='1', ip='192.168.0.1', type='fail', weight=8}
event: LoginEvent{userId='1', ip='192.168.0.2', type='fail', weight=9}
event: LoginEvent{userId='1', ip='192.168.0.3', type='fail', weight=10}
after value: LoginEvent{userId='1', ip='192.168.0.4', type='fail', weight=10} value i: 3

begin value: LoginEvent{userId='1', ip='192.168.0.4', type='fail', weight=10} value i: 0
event: LoginEvent{userId='1', ip='192.168.0.1', type='fail', weight=8}
event: LoginEvent{userId='1', ip='192.168.0.2', type='fail', weight=9}
event: LoginEvent{userId='1', ip='192.168.0.3', type='fail', weight=10}
after value: LoginEvent{userId='1', ip='192.168.0.4', type='fail', weight=10} value i: 3

begin value: LoginEvent{userId='1', ip='192.168.0.4', type='fail', weight=10} value i: 0
event: LoginEvent{userId='1', ip='192.168.0.2', type='fail', weight=9}
event: LoginEvent{userId='1', ip='192.168.0.3', type='fail', weight=10}
after value: LoginEvent{userId='1', ip='192.168.0.4', type='fail', weight=10} value i: 2

begin value: LoginEvent{userId='1', ip='192.168.0.4', type='fail', weight=10} value i: 0
event: LoginEvent{userId='1', ip='192.168.0.2', type='fail', weight=9}
event: LoginEvent{userId='1', ip='192.168.0.3', type='fail', weight=10}
after value: LoginEvent{userId='1', ip='192.168.0.4', type='fail', weight=10} value i: 2

begin value: LoginEvent{userId='1', ip='192.168.0.4', type='fail', weight=10} value i: 0
event: LoginEvent{userId='1', ip='192.168.0.3', type='fail', weight=10}
after value: LoginEvent{userId='1', ip='192.168.0.4', type='fail', weight=10} value i: 1

begin value: LoginEvent{userId='1', ip='192.168.0.4', type='fail', weight=10} value i: 0
event: LoginEvent{userId='1', ip='192.168.0.3', type='fail', weight=10}
after value: LoginEvent{userId='1', ip='192.168.0.4', type='fail', weight=10} value i: 1

begin value: LoginEvent{userId='1', ip='192.168.0.4', type='fail', weight=10} value i: 0
after value: LoginEvent{userId='1', ip='192.168.0.4', type='fail', weight=10} value i: 0

LoginWarning{userId='1', type='192.168.0.1', ip='fail'}

begin value: LoginEvent{userId='3', ip='192.168.10.10', type='fail', weight=5} value i: 0
after value: LoginEvent{userId='3', ip='192.168.10.10', type='fail', weight=5} value i: 0

begin value: LoginEvent{userId='3', ip='192.168.10.11', type='fail', weight=6} value i: 0
event: LoginEvent{userId='3', ip='192.168.10.10', type='fail', weight=5}
after value: LoginEvent{userId='3', ip='192.168.10.11', type='fail', weight=6} value i: 1

begin value: LoginEvent{userId='3', ip='192.168.10.11', type='fail', weight=6} value i: 0
event: LoginEvent{userId='3', ip='192.168.10.10', type='fail', weight=5}
after value: LoginEvent{userId='3', ip='192.168.10.11', type='fail', weight=6} value i: 1

begin value: LoginEvent{userId='3', ip='192.168.10.11', type='fail', weight=6} value i: 0
after value: LoginEvent{userId='3', ip='192.168.10.11', type='fail', weight=6} value i: 0

begin value: LoginEvent{userId='4', ip='192.168.10.10', type='fail', weight=6} value i: 0
after value: LoginEvent{userId='4', ip='192.168.10.10', type='fail', weight=6} value i: 0

begin value: LoginEvent{userId='4', ip='192.168.10.11', type='fail', weight=7} value i: 0
event: LoginEvent{userId='4', ip='192.168.10.10', type='fail', weight=6}
after value: LoginEvent{userId='4', ip='192.168.10.11', type='fail', weight=7} value i: 1

begin value: LoginEvent{userId='4', ip='192.168.10.11', type='fail', weight=7} value i: 0
event: LoginEvent{userId='4', ip='192.168.10.10', type='fail', weight=6}
after value: LoginEvent{userId='4', ip='192.168.10.11', type='fail', weight=7} value i: 1

begin value: LoginEvent{userId='4', ip='192.168.10.11', type='fail', weight=7} value i: 0
after value: LoginEvent{userId='4', ip='192.168.10.11', type='fail', weight=7} value i: 0

begin value: LoginEvent{userId='4', ip='192.168.10.12', type='fail', weight=8} value i: 0
event: LoginEvent{userId='4', ip='192.168.10.10', type='fail', weight=6}
event: LoginEvent{userId='4', ip='192.168.10.11', type='fail', weight=7}
after value: LoginEvent{userId='4', ip='192.168.10.12', type='fail', weight=8} value i: 2

begin value: LoginEvent{userId='4', ip='192.168.10.12', type='fail', weight=8} value i: 0
event: LoginEvent{userId='4', ip='192.168.10.10', type='fail', weight=6}
event: LoginEvent{userId='4', ip='192.168.10.11', type='fail', weight=7}
after value: LoginEvent{userId='4', ip='192.168.10.12', type='fail', weight=8} value i: 2

begin value: LoginEvent{userId='4', ip='192.168.10.12', type='fail', weight=8} value i: 0
event: LoginEvent{userId='4', ip='192.168.10.11', type='fail', weight=7}
after value: LoginEvent{userId='4', ip='192.168.10.12', type='fail', weight=8} value i: 1

begin value: LoginEvent{userId='4', ip='192.168.10.12', type='fail', weight=8} value i: 0
event: LoginEvent{userId='4', ip='192.168.10.11', type='fail', weight=7}
after value: LoginEvent{userId='4', ip='192.168.10.12', type='fail', weight=8} value i: 1

begin value: LoginEvent{userId='4', ip='192.168.10.12', type='fail', weight=8} value i: 0
after value: LoginEvent{userId='4', ip='192.168.10.12', type='fail', weight=8} value i: 0
```

##### 简单条件

简单条件：继承IterativeCondition，只需要判断事件属性是否符合相应条件即可。

```java
.where(new SimpleCondition<LoginEvent>() {

    @Override
    public boolean filter(LoginEvent loginEvent) throws Exception {
        return loginEvent.getType().equals("fail");
    }
})
```

##### 组合条件

使用AND或OR对多个条件进行组合，当然也可以写在一个条件里面。

```java
.where(new SimpleCondition<LoginEvent>() {

    @Override
    public boolean filter(LoginEvent value) throws Exception {
        return value.getType().equals("fail");
    }
})
.or(new SimpleCondition<LoginEvent>() {
    @Override
    public boolean filter(LoginEvent value) {
        return value.getWeight() >= 6;
    }
})
.times(3)
```

匹配结果：


```bash
LoginWarning{userId='1', type='192.168.0.1', ip='fail'}
LoginWarning{userId='1', type='192.168.0.2', ip='fail'}
LoginWarning{userId='4', type='192.168.10.10', ip='fail'}
LoginWarning{userId='5', type='192.168.10.13', ip='success'}
```

##### 停止条件

可以理解为匹配跳过某个符合条件的事件。

```java
.oneOrMore()
.until(new SimpleCondition<LoginEvent>() {
    @Override
    public boolean filter(LoginEvent value) throws Exception {
        return value.getWeight() == 8 || value.getWeight() == 9;
    }
})
```

```bash
LoginWarning{userId='1', type='192.168.0.3', ip='fail'}
LoginWarning{userId='1', type='192.168.0.3', ip='fail'}
LoginWarning{userId='1', type='192.168.0.4', ip='fail'}
LoginWarning{userId='3', type='192.168.10.10', ip='fail'}
LoginWarning{userId='3', type='192.168.10.10', ip='fail'}
LoginWarning{userId='3', type='192.168.10.11', ip='fail'}
LoginWarning{userId='4', type='192.168.10.10', ip='fail'}
LoginWarning{userId='4', type='192.168.10.10', ip='fail'}
LoginWarning{userId='4', type='192.168.10.11', ip='fail'}
LoginWarning{userId='5', type='192.168.10.15', ip='success'}
```

### 组合模式

将单一模式组合成完整的模式序列。一个模式序列有一个初始模式begin开始。

```java
Pattern<Event, ?> start = Pattern.<Event>begin("start");
```

然后使用特定的临近条件将单一模式追加到初始模式及其他模式的后面。  
FlinkCEP支持事件之间的三种临近条件：
1. next() notNext() 严格临近 匹配到的事件严格地一个挨着一个出现，在事件之间没有任何未匹配到的事件。
2. followedBy() notFollowedBy() 宽松临近 忽略事件之间未匹配到的事件。
3. followedByAny() 非确定性宽松临近，进一步放宽连续性，允许额外的忽略一些匹配事件。

需要注意的是：

> 模式序列不能以notFollowedBy()结束。
> NOT模式不能先于optional模式。
> 模式序列只有一个时间约束。
> 如果在不同的单独模式上定义了多个时间约束，则以最小的时间约束为准。

```java
// 定义模式序列 fail fail weightHigh>8 weightHigh!=11
Pattern<LoginEvent, ?> start = Pattern.<LoginEvent>begin("start").where(new SimpleCondition<LoginEvent>() {
    @Override
    public boolean filter(LoginEvent value) throws Exception {
        return value.getType().equals("fail");
    }
});
Pattern<LoginEvent, ?> fail = start.next("fail").where(new SimpleCondition<LoginEvent>() {
    @Override
    public boolean filter(LoginEvent value) throws Exception {
        return value.getType().equals("fail");
    }
});
Pattern<LoginEvent, ?> weightHigh = fail.followedByAny("weightHigh").where(new SimpleCondition<LoginEvent>() {
    @Override
    public boolean filter(LoginEvent value) throws Exception {
        return value.getWeight() > 6;
    }
});
Pattern<LoginEvent, ?> newPattern = weightHigh.notNext("end").where(new SimpleCondition<LoginEvent>() {
    @Override
    public boolean filter(LoginEvent value) throws Exception {
        return value.getWeight() == 11;
    }
});

PatternStream<LoginEvent> patternStream2 = CEP.pattern(
        loginEventStream.keyBy(LoginEvent::getUserId),
        newPattern);

// DataStream<里面不能是List<>>
DataStream<String> newPatternStream = patternStream2.select((Map<String, List<LoginEvent>> pattern) -> {
    List<LoginWarning> loginWarnings = new ArrayList<LoginWarning>();
    if (null != pattern.get("start")) {
//                System.out.println("list: " + pattern.get("weightHigh"));
        LoginEvent first = pattern.get("start").get(0);
        if (null != first) {
            loginWarnings.add(new LoginWarning(first.getUserId(), first.getIp(), first.getType(), first.getWeight()));
        }
    }
    if (null != pattern.get("fail")) {
//                System.out.println("list: " + pattern.get("weightHigh"));
        LoginEvent first = pattern.get("fail").get(0);
        if (null != first) {
            loginWarnings.add(new LoginWarning(first.getUserId(), first.getIp(), first.getType(), first.getWeight()));
        }
    }
    if (null != pattern.get("weightHigh")) {
//                System.out.println("list: " + pattern.get("weightHigh"));
        LoginEvent first = pattern.get("weightHigh").get(0);
        if (null != first) {
            loginWarnings.add(new LoginWarning(first.getUserId(), first.getIp(), first.getType(), first.getWeight()));
        }
    }
    if (null != pattern.get("end")) {
//                System.out.println("list: " + pattern.get("end"));
        LoginEvent first = pattern.get("end").get(0);
        if (null != first) {
            loginWarnings.add(new LoginWarning(first.getUserId(), first.getIp(), first.getType(), first.getWeight()));
        }
    }
    return loginWarnings.toString();
});

newPatternStream.print();
```

匹配结果：

```bash
[LoginWarning{userId='1', type='192.168.0.1', ip='fail', weight='8'}, LoginWarning{userId='1', type='192.168.0.2', ip='fail', weight='9'}, LoginWarning{userId='1', type='192.168.0.3', ip='fail', weight='10'}]
[LoginWarning{userId='6', type='192.168.10.16', ip='fail', weight='6'}, LoginWarning{userId='6', type='192.168.10.17', ip='fail', weight='8'}, LoginWarning{userId='6', type='192.168.10.18', ip='fail', weight='8'}]
[LoginWarning{userId='6', type='192.168.10.16', ip='fail', weight='6'}, LoginWarning{userId='6', type='192.168.10.17', ip='fail', weight='8'}, LoginWarning{userId='6', type='192.168.10.19', ip='fail', weight='10'}]
[LoginWarning{userId='6', type='192.168.10.17', ip='fail', weight='8'}, LoginWarning{userId='6', type='192.168.10.18', ip='fail', weight='8'}, LoginWarning{userId='6', type='192.168.10.19', ip='fail', weight='10'}]
```

举例：  
如果模式是 a b，输入事件是 a c b1 b2 。  
1. 严格临近：a与b之间应为{}空，所以匹配不到。c 在 a 的后面使得 a 事件被丢弃。
2. 宽松临近：c会被忽略或者被穿透，{a b1}，跳过匹配不到的事件，直到下一个能匹配到的事件。
3. 非确定性宽松临近：{a b1}, {a b2} 可以穿透已经匹配过的事件，比如b1。

循环模式（oneOrMore() 和 times()）默认是宽松临近，可以调用consecutive()方法显式地设为严格临近，也可以调用allowCombinations()方法设置为非确定性宽松临近。

#### consecutive()

```java
Pattern.<Event>begin("start").where(new SimpleCondition<Event>() {
  @Override
  public boolean filter(Event value) throws Exception {
    return value.getName().equals("c");
  }
})
// 此处是宽松临近 第一个匹配到的a 只能穿透非a事件，不能穿透其他a事件
.followedBy("middle").where(new SimpleCondition<Event>() {
  @Override
  public boolean filter(Event value) throws Exception {
    return value.getName().equals("a");
  }
}).oneOrMore().consecutive() // 第二个和之后匹配到的a 为严格临近
.followedBy("end1").where(new SimpleCondition<Event>() {
  @Override
  public boolean filter(Event value) throws Exception {
    return value.getName().equals("b");
  }
});
```

输入事件：c d a1 a2 a3 d a4 b
1. 使用consecutive()【next()】：{c a1 b} {c a1 a2 b} {c a1 a2 a3 b}
2. 不使用consecutive()：{c a1 b} {c a1 a2 b} {c a1 a2 a3 b} {c a1 a2 a3 a4 b}

#### allowCombinations()

```java
Pattern.<Event>begin("start").where(new SimpleCondition<Event>() {
  @Override
  public boolean filter(Event value) throws Exception {
    return value.getName().equals("c");
  }
})
// 此处是宽松临近 第一个匹配到的a 只能穿透非a事件，不能穿透其他a事件
.followedBy("middle").where(new SimpleCondition<Event>() {
  @Override
  public boolean filter(Event value) throws Exception {
    return value.getName().equals("a");
  }
}).oneOrMore().allowCombinations() // 第二个和之后匹配到的a 为非确定宽松临近，可以穿透a和非a事件
.followedBy("end1").where(new SimpleCondition<Event>() {
  @Override
  public boolean filter(Event value) throws Exception {
    return value.getName().equals("b");
  }
});
```

输入事件：c d a1 a2 a3 d a4 b
1. 使用allowCombinations()【followedByAny()】：{c a1 b} {c a1 a2 b} {c a1 a2 a3 b} {c a1 a2 a4 b} {c a1 a2 a3 a4 b} {c a1 a3 b} {c a1 a3 a4 b} {c a1 a4 b} 
2. 不使用allowCombinations()：{c a1 b} {c a1 a2 b} {c a1 a2 a3 b} {c a1 a2 a3 a4 b}

### 组模式或模式组

把模式序列作为begin和模式追加的参数，将返回一个GroupPattern，也是一个模式。

```java
public static <X> Pattern<X, X> begin(final String name){...}

public static <T, F extends T> GroupPattern<T, F> begin(Pattern<T, F> group){...}

public class GroupPattern<T, F extends T> extends Pattern<T, F> {...}
```

比如：

```java
Pattern<Event, ?> start = Pattern.begin(
    Pattern.<Event>begin("start").where(...).followedBy("start_middle").where(...)
);

// 严格临近
Pattern<Event, ?> strict = start.next(
    Pattern.<Event>begin("next_start").where(...).followedBy("next_middle").where(...)
).times(3);

// 宽松临近
Pattern<Event, ?> relaxed = start.followedBy(
    Pattern.<Event>begin("followedby_start").where(...).followedBy("followedby_middle").where(...)
).oneOrMore();

// 非确定性宽松临近
Pattern<Event, ?> nonDetermin = start.followedByAny(
    Pattern.<Event>begin("followedbyany_start").where(...).followedBy("followedbyany_middle").where(...)
).optional();
```

within(time)

定义事件序列与模式匹配的最大时间间隔。 如果未完成的事件序列超过此时间，则将其丢弃。

### 匹配后的跳过策略

```java
public static <X> Pattern<X, X> begin(final String name, final AfterMatchSkipStrategy afterMatchSkipStrategy) {...}

public static <T, F extends T> GroupPattern<T, F> begin(final Pattern<T, F> group, final AfterMatchSkipStrategy afterMatchSkipStrategy) {...}
```

5种跳过策略：  
NO_SKIP: 所有可能的匹配都会被发出。  
SKIP_TO_NEXT: 丢弃与开始匹配到的事件相同的事件，发出开始匹配到的事件，即直接跳到下一个模式匹配到的事件，以此类推。  
SKIP_PAST_LAST_EVENT: 丢弃匹配开始后但结束之前匹配到的事件。  
SKIP_TO_FIRST[PatternName]: 丢弃匹配开始后但在PatternName模式匹配到的第一个事件之前匹配到的事件。  
SKIP_TO_LAST[PatternName]: 丢弃匹配开始后但在PatternName模式匹配到的最后一个事件之前匹配到的事件。

SKIP_TO_FIRST和SKIP_TO_LAST需要指定一个模式名称。

例如模式 b+ c，输入事件流 b1 b2 b3 c：

1. NO_SKIP 不会丢弃任何匹配到的事件
b1 b2 b3 c
b2 b3 c
b3 c

2. SKIP_TO_NEXT 会丢弃b1开头的其它匹配到的事件，这里只有一个b1开头的
b1 b2 b3 c
b2 b3 c
b3 c

3. SKIP_PAST_LAST_EVENT 会丢弃开始后结束前所有匹配到的事件，即只有	b1 b2 b3 c
b1 b2 b3 c

4. SKIP_TO_FIRST[b] 会丢弃b1之前匹配到的事件
b1 b2 b3 c
b2 b3 c
b3 c

5. SKIP_TO_LAST[b] 会丢弃中间匹配到的事件
b1 b2 b3 c
b3 c

模式 (a | b | c) (b | c) c+.greedy d，输入事件流：a b c1 c2 c3 d
1. NO_SKIP 
a b c1 c2 c3 d
b c1 c2 c3 d
c1 c2 c3 d

2. SKIP_TO_FIRST[c*]
a b c1 c2 c3 d
c1 c2 c3 d

模式 a b+，输入事件流：
1. NO_SKIP
a b1
a b1 b2
a b1 b2 b3

2. SKIP_TO_NEXT
a b1

```java
public static NoSkipStrategy noSkip() {
	return NoSkipStrategy.INSTANCE;
}

public static AfterMatchSkipStrategy skipToNext() {
	return SkipToNextStrategy.INSTANCE;
}

public static SkipPastLastStrategy skipPastLastEvent() {
	return SkipPastLastStrategy.INSTANCE;
}

public static SkipToLastStrategy skipToLast(String patternName) {
	return new SkipToLastStrategy(patternName, false);
}

public static SkipToFirstStrategy skipToFirst(String patternName) {
	return new SkipToFirstStrategy(patternName, false);
}

AfterMatchSkipStrategy skipStrategy = ...
Pattern.begin("patternName", skipStrategy);
```

需要注意的是：

> SKIP_TO_FIRST/LAST两种跳过策略可以设置抛出异常。

```java
AfterMatchSkipStrategy.skipToFirst(patternName).throwExceptionOnMiss()

public abstract SkipToElementStrategy throwExceptionOnMiss();
```

### 检索模式【动词】

即通过模式名称找到模式匹配到的事件。

指定模式序列之后，把它应用到输入事件流中去匹配潜在的事件。
1. 创建一个PatternStream。
2. 给一个输入事件流。输入流可以是keyed即分组的，也可以不分组。
3. 一个模式或模式序列
4. 可以自定义一个事件比较器（可选的）

```java
DataStream<Event> input = ...
Pattern<Event, ?> pattern = ...
EventComparator<Event> comparator = ... // optional

PatternStream<Event> patternStream = CEP.pattern(input, pattern, comparator);
```

需要注意的是：

> non-keyed的Job只有一个并发度。

#### 从多个模式中进行选择

官方推荐的方式是使用PatternProcessFunction。

```java
class MyPatternProcessFunction<IN, OUT> extends PatternProcessFunction<IN, OUT> {
	// IN 是Event事件即POJO match是模式序列中匹配到的key-value，key是模式序列中的模式名称，value是有序的POJO列表（oneToMany() and times()可能会匹配到多个事件，否则一般只有一个）
	// PatternProcessFunction能够访问Context对象，这样就可以拿到处理时间和事件时间，还可以使用side-output发出数据。
    @Override
    public void processMatch(Map<String, List<IN>> match, Context ctx, Collector<OUT> out) throws Exception;
        IN startEvent = match.get("start").get(0);
        IN endEvent = match.get("end").get(0);
        out.collect(OUT(startEvent, endEvent));
    }
}
```

简单一点也可以使用PatternSelectFunction。

```java
patternStream.select(new PatternSelectFunction<LoginEvent, Object>() {
	// LoginEvent是IN  Object是OUT 
    @Override
    public Object select(Map<String, List<LoginEvent>> match) throws Exception {
        return null;
    }
});
```

#### 处理超时

当我们使用within关键字限制窗口长度时，难免会有事件因为超时被窗口丢弃。  
可以使用TimedOutPartialMatchHandler接口对超时部分的事件匹配进行处理。

```java
class MyPatternProcessFunction<IN, OUT> extends PatternProcessFunction<IN, OUT> implements TimedOutPartialMatchHandler<IN> {
    @Override
    public void processMatch(Map<String, List<IN>> match, Context ctx, Collector<OUT> out) throws Exception;
        ...
    }

    @Override
    public void processTimedOutMatch(Map<String, List<IN>> match, Context ctx) throws Exception;
        IN startEvent = match.get("start").get(0);
        ctx.output(outputTag, T(startEvent));
    }
}
```

PatternProcessFunction是Flink1.8介绍和推荐的API，也可以使用旧版本的API如select/flatSelect。

```java
PatternStream<Event> patternStream = CEP.pattern(input, pattern);

OutputTag<String> outputTag = new OutputTag<String>("side-output"){};

SingleOutputStreamOperator<ComplexEvent> flatResult = patternStream.flatSelect(
    outputTag,
    new PatternFlatTimeoutFunction<Event, TimeoutEvent>() {
        public void timeout(
                Map<String, List<Event>> pattern,
                long timeoutTimestamp,
                Collector<TimeoutEvent> out) throws Exception {
            out.collect(new TimeoutEvent());
        }
    },
    new PatternFlatSelectFunction<Event, ComplexEvent>() {
        public void flatSelect(Map<String, List<IN>> pattern, Collector<OUT> out) throws Exception {
            out.collect(new ComplexEvent());
        }
    }
);

DataStream<TimeoutEvent> timeoutFlatResult = flatResult.getSideOutput(outputTag);
```

### CEP库里的时间

#### 处理事件时间延迟

在使用事件时间时，需要对事件进行正确的排序，在事件到来时先放到缓冲区buffer里面，这些事件是已经根据事件时间从小到大排好序了的。  
当水印到来时，缓冲区buffer里所有事件时间比水印时间戳小的事件会被处理。  
这就意味着水印之间的事件会根据事件时间顺序来处理。

值得注意的是：

> CEP库「假定」使用事件时间时，水印是正确的。

从而「可以定义」事件时间小于上一次水印时间戳的事件为迟到事件。  
迟到的事件不会被进一步处理【被窗口丢弃】。  
不过可以使用sideOutput对这些迟到的事件进行额外的收集处理。

```java
PatternStream<Event> patternStream = CEP.pattern(input, pattern);

OutputTag<String> lateDataOutputTag = new OutputTag<String>("late-data"){};

SingleOutputStreamOperator<ComplexEvent> result = patternStream
    .sideOutputLateData(lateDataOutputTag)
    .select(
        new PatternSelectFunction<Event, ComplexEvent>() {...}
    );

DataStream<String> lateData = result.getSideOutput(lateDataOutputTag);
```

#### 时间上下文

```java
/**
 * Enables access to time related characteristics such as current processing time or timestamp of
 * currently processed element. Used in {@link PatternProcessFunction} and
 * {@link org.apache.flink.cep.pattern.conditions.IterativeCondition}
 */
@PublicEvolving
public interface TimeContext {

	/**
	 * Timestamp of the element currently being processed.
	 *
	 * <p>In case of {@link org.apache.flink.streaming.api.TimeCharacteristic#ProcessingTime} this
	 * will be set to the time when event entered the cep operator.
	 */
	long timestamp();

	/** Returns the current processing time. */
	long currentProcessingTime();
}
```

#### Example

```java
StreamExecutionEnvironment env = ...
env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

DataStream<Event> input = ...

DataStream<Event> partitionedInput = input.keyBy(new KeySelector<Event, Integer>() {
	@Override
	public Integer getKey(Event value) throws Exception {
		return value.getId();
	}
});

Pattern<Event, ?> pattern = Pattern.<Event>begin("start")
	.next("middle").where(new SimpleCondition<Event>() {
		@Override
		public boolean filter(Event value) throws Exception {
			return value.getName().equals("error");
		}
	}).followedBy("end").where(new SimpleCondition<Event>() {
		@Override
		public boolean filter(Event value) throws Exception {
			return value.getName().equals("critical");
		}
	}).within(Time.seconds(10));

PatternStream<Event> patternStream = CEP.pattern(partitionedInput, pattern);

DataStream<Alert> alerts = patternStream.select(new PatternSelectFunction<Event, Alert>() {
	@Override
	public Alert select(Map<String, List<Event>> pattern) throws Exception {
		return createAlert(pattern);
	}
});
```