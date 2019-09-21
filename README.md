# SpringBoot集成分布式ID生成系统TinyId

SpringBoot Jpa集成分布式ID生成系统TinyId的示例。

## 部署TinyId

### **一、安装Mysql**

```bash
$ sudo apt-get update
$ sudo apt-get install mysql-server

## 配置root远程访问，方便测试
$ vim /etc/mysql/mysql.conf.d/mysqld.cnf
# bind-address          = 127.0.0.1
$ sudo service mysql restart
$ sudo -u root -p
mysql> GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'root' WITH GRANT OPTION;
Query OK, 0 rows affected, 1 warning (0.00 sec)
```

### **二、安装JDK1.8**

```bash
$ sudo apt-get update && apt-get upgrade
$ sudo apt install openjdk-8-jdk
$ sudo java -version
openjdk version "1.8.0_222"
OpenJDK Runtime Environment (build 1.8.0_222-8u222-b10-1ubuntu1~16.04.1-b10)
OpenJDK 64-Bit Server VM (build 25.222-b10, mixed mode)
```

### **三、创建数据库，导入数据表**

```bash
$ cd /tmp
## 上传${tinyid}/tinyid-server/db.sql
$ ll
drwxrwxrwt  9 root root 4096 Sep 20 10:54 ./
drwxr-xr-x 23 root root 4096 Sep 20 10:02 ../
-rw-r--r--  1 root root 2472 Sep 19 18:15 db.sql
...
$ mysql -u root -p
## 创建tinyid数据库
mysql> CREATE DATABASE tinyid_db CHARACTER SET utf8 COLLATE utf8_general_ci;
Query OK, 1 row affected (0.00 sec)
## 切换到tinyid数据库
mysql> use tinyid_db;
Database changed
## 执行初始化SQL脚本
mysql> source /tmp/db.sql;
Query OK, 0 rows affected (0.01 sec)
...
mysql> SHOW TABLES;
+---------------------+
| Tables_in_tinyid_db |
+---------------------+
| tiny_id_info        |
| tiny_id_token       |
+---------------------+
2 rows in set (0.01 sec)
```

### **四、部署TinyId**

```bash
$ mkdir -p /data/tinyid/config
$ cd /data/tinyid
## 上传tinyid-server-***.jar
$ ll
total 17652
drwxr-xr-x 3 root root     4096 Sep 20 11:04 ./
drwxr-xr-x 3 root root     4096 Sep 20 11:02 ../
drwxr-xr-x 2 root root     4096 Sep 20 11:03 config/
-rw-r--r-- 1 root root 18059416 Sep 19 19:16 tinyid-server-0.1.0-SNAPSHOT.jar
$ ln -s tinyid-server-0.1.0-SNAPSHOT.jar current.jar

## 配置数据库连接
$ vim /data/tinyid/config/application.properties
datasource.tinyid.primary.url=jdbc:mysql://localhost:3306/tinyid_db?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8
datasource.tinyid.primary.username=root
datasource.tinyid.primary.password=root

## 创建启动脚本
$ vim /etc/systemd/system/tinyid.service
[Unit]
Description=TinyId Service Daemon
[Service]
Type=simple
ExecStart=/usr/bin/java -jar /data/tinyid/current.jar
Restart=always
WorkingDirectory=/data/tinyid/
[Install]
WantedBy=multi-user.target
$ sudo systemctl enable tinyid.service
$ sudo systemctl start tinyid.service
$ sudo systemctl status tinyid.service
● tinyid.service - TinyId Service Daemon
   Loaded: loaded (/etc/systemd/system/tinyid.service; enabled; vendor preset: enabled)
   Active: active (running) since Fri 2019-09-20 11:12:31 CST; 35s ago
```

### **五、测试**

```bash
nextId:
curl 'http://localhost:9999/tinyid/id/nextId?bizType=test&token=0f673adf80504e2eaa552f5d791b644c'
response:{"data":[2],"code":200,"message":""}

nextId Simple:
curl 'http://localhost:9999/tinyid/id/nextIdSimple?bizType=test&token=0f673adf80504e2eaa552f5d791b644c'
response: 3

with batchSize:
curl 'http://localhost:9999/tinyid/id/nextIdSimple?bizType=test&token=0f673adf80504e2eaa552f5d791b644c&batchSize=10'
response: 4,5,6,7,8,9,10,11,12,13

Get nextId like 1,3,5,7,9...
bizType=test_odd : delta is 2 and remainder is 1
curl 'http://localhost:9999/tinyid/id/nextIdSimple?bizType=test_odd&batchSize=10&token=0f673adf80504e2eaa552f5d791b644c'
response: 3,5,7,9,11,13,15,17,19,21
```

## **SpringBoot集成TinyID**

> 使用自定义Jpa Id Generator来集成TinyId，达到自动生成分布式ID的目的。

#### 1.创建自定义Id生成器TinyIdGenerator

```java
package me.itlearner.sample.idgenerator;

import com.xiaoju.uemc.tinyid.client.utils.TinyId;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.util.Properties;

/**
* TinyId 分布式ID生成器
*
* @author gaochen
* @date 2019/9/20
*/
public class TinyIdGenerator implements IdentifierGenerator, Configurable {

    private String idGroup;

    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        idGroup = params.getProperty("idGroup");
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        return TinyId.nextId(idGroup);
    }
}
```

#### 使用自定义生成器

```java
package me.itlearner.sample.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
* 测试用户
*
* @author gaochen
* @date 2019/9/20
*/
@Data
@Entity
@Table
public class SampleUser {

    @Id
    @GeneratedValue(generator = "user-id-generator")
    @GenericGenerator(name = "user-id-generator",
            parameters = @Parameter(name = "idGroup", value = "user"),
            strategy = "me.itlearner.sample.idgenerator.TinyIdGenerator")
    private Long id;

    private String username;

    private String password;

}
```

在TinyId数据库中插入对应的类型为user的分布式ID

> step步长尽量调小一点，可以减少重启业务服务的时候号段的损失。

```sql
-- 插入初始tinyId,
INSERT INTO tinyid_db.tiny_id_info (id, biz_type, begin_id, max_id, step, delta, remainder, create_time, update_time, version) VALUES (3, 'user', 1, 0, 100, 1, 0, '2019-09-20 13:52:58', '2019-09-20 13:52:58', 0);
INSERT INTO tinyid_db.tiny_id_token (id, token, biz_type, remark, create_time, update_time) VALUES (3, '0f673adf80504e2eaa552f5d791b644c', 'user', '1', '2019-09-20 13:52:58', '2019-09-20 13:52:58');
```

#### 写个测试类测试下(插入1000个用户)

```java
package me.itlearner.sample.entity;

import lombok.extern.slf4j.Slf4j;
import me.itlearner.sample.SampleUserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
* TODO
*
* @author gaochen
* @date 2019/9/20
*/
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class SampleUserTest {

    @Autowired
    private SampleUserRepository sampleUserRepository;

    @Test
    public void test() throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            new Thread(() -> {
                SampleUser user1 = new SampleUser();
                user1.setUsername("test");
                user1.setPassword("test");

                SampleUser sampleUser = sampleUserRepository.save(user1);

                log.info("user id : {}", sampleUser.getId());
            }).start();
        }

        Thread.sleep(10000);
    }

}
```

结果显示，插入1000个用户，id是不重复的呈现递增的状况，且顺序不是递增的（模拟了分布式系统的情况）。

![](https://i.loli.net/2019/09/21/4ZWp5BrNxfKAdji.png)

## 补充

在实际使用过程中， 要仔细确认系统已有ID的最大值，例如用户ID序列已经到达了10000，那么begin_id应该设置为10000，max_id可以先设置为10000，TinyId会根据step自动的增加号段。

# 参考

- [TinyId Wiki](https://github.com/didi/tinyid/wiki)