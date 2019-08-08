### 


### 入门Prometheus+Grafana

用于flink的metrics监控

#### 相关文档和下载

上生产可以直接使用docker部署和配置。  

【普罗米修斯😯】  
With Docker【略】  
[https://prometheus.io/docs/introduction/overview/](https://prometheus.io/docs/introduction/overview/)
[https://grafana.com/docs/](https://grafana.com/docs/)
[https://grafana.com/docs/installation/mac/](https://grafana.com/docs/installation/mac/)
[https://grafana.com/docs/installation/rpm/](https://grafana.com/docs/installation/rpm/)

[https://github.com/prometheus/prometheus/releases/download/v2.10.0/prometheus-2.10.0.darwin-amd64.tar.gz](https://github.com/prometheus/prometheus/releases/download/v2.10.0/prometheus-2.10.0.darwin-amd64.tar.gz)
[https://github.com/prometheus/prometheus/releases/download/v2.10.0/prometheus-2.10.0.linux-amd64.tar.gz](https://github.com/prometheus/prometheus/releases/download/v2.10.0/prometheus-2.10.0.linux-amd64.tar.gz)

[https://github.com/prometheus/alertmanager/releases/download/v0.18.0/alertmanager-0.18.0.darwin-amd64.tar.gz](https://github.com/prometheus/alertmanager/releases/download/v0.18.0/alertmanager-0.18.0.darwin-amd64.tar.gz)
[https://github.com/prometheus/alertmanager/releases/download/v0.18.0/alertmanager-0.18.0.linux-amd64.tar.gz](https://github.com/prometheus/alertmanager/releases/download/v0.18.0/alertmanager-0.18.0.linux-amd64.tar.gz)

[https://github.com/prometheus/mysqld_exporter/releases/download/v0.11.0/mysqld_exporter-0.11.0.darwin-amd64.tar.gz](https://github.com/prometheus/mysqld_exporter/releases/download/v0.11.0/mysqld_exporter-0.11.0.darwin-amd64.tar.gz)
[https://github.com/prometheus/mysqld_exporter/releases/download/v0.11.0/mysqld_exporter-0.11.0.linux-amd64.tar.gz](https://github.com/prometheus/mysqld_exporter/releases/download/v0.11.0/mysqld_exporter-0.11.0.linux-amd64.tar.gz)

#### Prometheus

On Mac:
```bash
$ wget https://github.com/prometheus/prometheus/releases/download/v2.10.0/prometheus-2.10.0.darwin-amd64.tar.gz
$ tar -zxvf prometheus-2.10.0.darwin-amd64.tar.gz 
```

##### 配置Prometheus监控Prometheus自身情况

prometheus.yml

```yaml
# my global config
global:
  scrape_interval:     15s # Set the scrape interval to every 15 seconds. Default is every 1 minute.
  evaluation_interval: 15s # Evaluate rules every 15 seconds. The default is every 1 minute.
  # scrape_timeout is set to the global default (10s).
  external_labels:
    monitor: 'Everywhere-Monitor'

# Alertmanager configuration
alerting:
  alertmanagers:
  - static_configs:
    - targets:
      # - alertmanager:9093

# Load rules once and periodically evaluate them according to the global 'evaluation_interval'.
rule_files:
  # - "first_rules.yml"
  # - "second_rules.yml"

# A scrape configuration containing exactly one endpoint to scrape:
# Here it's Prometheus itself.
scrape_configs:
  # The job name is added as a label `job=<job_name>` to any timeseries scraped from this config.
  - job_name: 'prometheus'
    scrape_interval: 5s
    # metrics_path defaults to '/metrics'
    # scheme defaults to 'http'.

    static_configs:
    - targets: ['localhost:9090']

```

##### 启动Prometheus

```bash
$ ./prometheus --config.file=prometheus.yml --web.enable-lifecycle

$ ls data/
lock    wal
```

加上--web.enable-lifecycle，启用远程热加载配置文件。   
--web.enable-lifecycle     Enable shutdown and reload via HTTP request.

```bash
$ curl -X POST http://localhost:9090/-/reload
Lifecycle APIs are not enabledl

$ rm data/*
$ ./prometheus --config.file=prometheus.yml --web.enable-lifecycle
```

##### http://localhost:9090/

[http://localhost:9090/graph](http://localhost:9090/graph)    
[http://localhost:9090/metrics](http://localhost:9090/metrics)

![image](http://images.icocoro.me/images/new/20190626000.png)
![image](http://images.icocoro.me/images/new/20190626001.png)
![image](http://images.icocoro.me/images/new/20190626002.png)

##### 安装客户端提供metrics接口[参考](https://www.cnblogs.com/chenqionghe/p/10494868.html)

Prometheus有一个官方Go客户端库，我们可以使用它来监控Go应用程序。  
Prometheus exporter是为硬件以及类Unix内核的操作系统提供插件化的metrics指标收集和报告的项目。

1. 通过golang客户端提供metrics

```bash
$ go version
go version go1.12.7 darwin/amd64

$ vi ~/.bash_profile
export GOPATH=/Users/shaozhipeng/Development/golang/client
$ source ~/.bash_profile

$ pwd
/Users/shaozhipeng/Development/golang/client/src
$ git clone https://github.com/prometheus/client_golang.git
$ mkdir -p $GOPATH/src/golang.org/x
$ cd !$
cd src/golang.org/x
$ git clone https://github.com/golang/net.git
$ git clone https://github.com/golang/sys.git
$ git clone https://github.com/golang/tools.git

$ go get -u -v github.com/prometheus/client_golang/prometheus
$ cd $GOPATH/src/client_golang/examples/random
$ go build -o random main.go
```

运行3个示例metrics接口：

```bash
localhost:random shaozhipeng$ ls
main.go random
localhost:random shaozhipeng$ ./random -listen-address=:8080 &
[1] 25747
localhost:random shaozhipeng$ ./random -listen-address=:8081 &
[2] 25750
localhost:random shaozhipeng$ ./random -listen-address=:8082 &
[3] 25751
```

2. 通过node exporter提供metrics

docker安装运行。

```bash
$ docker run -d --name=node-exporter -p 9100:9100 prom/node-exporter

$ docker ps
CONTAINER ID        IMAGE                COMMAND                  CREATED             STATUS                  PORTS                               NAMES
dc6a60c6cbca        prom/node-exporter   "/bin/node_exporter"     6 minutes ago       Up 6 minutes            0.0.0.0:9100->9100/tcp              node-exporter
```

##### 修改prometheus.yml并重新加载

注意targets里的hostname:port前面不要有http://前缀。

```yaml
    static_configs:
    - targets: ['localhost:9090']
    - targets: ['localhost:8080', 'localhost:8081','localhost:8082']
      labels:
        group: 'client-golang'
    - targets: ['localhost:9100']
      labels:
        group: 'client-node-exporter'
```

```bash
$ curl -X POST http://localhost:9090/-/reload
```

##### http://localhost:9090/targets

![image](http://images.icocoro.me/images/new/20190626003.png)

##### docker安装运行pushgateway

[gitbooks](https://songjiayang.gitbooks.io/prometheus/content/pushgateway/how.html)

```bash
$ docker run -d -p 9091:9091 --name pushgateway prom/pushgateway

$ docker ps
CONTAINER ID        IMAGE                COMMAND                  CREATED             STATUS                  PORTS                               NAMES
29ac7b91693e        prom/pushgateway     "/bin/pushgateway"       26 seconds ago      Up 25 seconds           0.0.0.0:9091->9091/tcp              pushgateway
dc6a60c6cbca        prom/node-exporter   "/bin/node_exporter"     About an hour ago   Up About an hour        0.0.0.0:9100->9100/tcp              node-exporter
```

##### http://localhost:9091/

![image](http://images.icocoro.me/images/new/20190626004.png)

##### 发送一个或多个指标到pushgateway

```bash
$ echo "jobpay_metric 100" | curl --data-binary @- http://localhost:9091/metrics/job/jobpay

$ cat <<EOF | curl --data-binary @- http://localhost:9091/metrics/job/jobpay/instance/test
# 订单统计
order_metric{label="pay_biz"} 8800
# 失败 成功 总数
fail_cnt 20
suc_cnt 1191
total_cnt 1211
EOF
```

##### 修改prometheus.yml并重新加载

```yaml
  - job_name: 'jobpay'
    scrape_interval: 5s

    static_configs:
    - targets: ['localhost:9091']
      labels:
        group: 'pushgateway'
```

[http://localhost:9091/metrics](http://localhost:9091/metrics)可以查看的到。

```text
fail_cnt{instance="test",job="jobpay"} 20
...
# TYPE jobpay_metric untyped
jobpay_metric{instance="",job="jobpay"} 100
# TYPE order_metric untyped
order_metric{instance="test",job="jobpay",label="pay_biz"} 8800
...
# TYPE push_time_seconds gauge
push_time_seconds{instance="",job="jobpay"} 1.5627427826181512e+09
push_time_seconds{instance="test",job="jobpay"} 1.5627427881227534e+09
...
# TYPE suc_cnt untyped
suc_cnt{instance="test",job="jobpay"} 1191
# TYPE total_cnt untyped
total_cnt{instance="test",job="jobpay"} 1211
```

![image](http://images.icocoro.me/images/new/20190626005.png)

#### Grafana

On Mac:  

```bash
wget https://dl.grafana.com/oss/release/grafana-6.2.5.darwin-amd64.tar.gz 
tar -zxvf grafana-6.2.5.darwin-amd64.tar.gz 
```

##### 配置
```bash
localhost:conf shaozhipeng$ ls
defaults.ini    ldap.toml       provisioning    sample.ini

cp sample.ini custom.ini
```

注意自定义配置custom.ini，会覆盖defaults.ini里的所有配置。

##### 使用默认配置启动Grafana

```bash
./bin/grafana-server web
```

##### http://localhost:3000

会提示修改密码，直接修改后保存即可。

![image](http://images.icocoro.me/images/new/20190626006.png)

##### 配置prometheus datasource

[adding-the-data-source-to-grafana](https://grafana.com/docs/features/datasources/prometheus/#adding-the-data-source-to-grafana)

1. 页面直接点击Add data source
2. 选择Prometheus
3. 默认设置
4. Save & Test保存并测试
5. Dashboards import

![image](http://images.icocoro.me/images/new/20190626007.png)
![image](http://images.icocoro.me/images/new/20190626008.png)
![image](http://images.icocoro.me/images/new/20190626009.png)
![image](http://images.icocoro.me/images/new/201906260010.png)
![image](http://images.icocoro.me/images/new/201906260011.png)
![image](http://images.icocoro.me/images/new/201906260012.png)
![image](http://images.icocoro.me/images/new/201906260013.png)
![image](http://images.icocoro.me/images/new/201906260014.png)

##### 自定义查询

多发几条数据进行测试。右键可以编辑时间线步长。
![image](http://images.icocoro.me/images/new/201906260015.png)
![image](http://images.icocoro.me/images/new/201906260016.png)
![image](http://images.icocoro.me/images/new/201906260017.png)
![image](http://images.icocoro.me/images/new/201906260018.png)

#### Prometheus Alertmanager