#Removing python3 imcompleted files
```
    apt-get remove python3.4
    rm -rf ver3
    pip3 unistall jupyter
```

#Installing python3(pip3, jupyter)
```
    apt-get update
    apt-get install python3.4
    apt-get install python3-pip
        apt-cache search [Pkg Name] // Searching pkg
    sudo pip3 install virtualenv
    virtualenv ipy
    source ./ipy/bin/activate
    sudo pip3 install jupyter // 꼭 가상환경 내부에서 설치 시도해야함
    
    pip3 install scipy // science http://freeprog.tistory.com/63
    pip3 install matplotlib
```
pip3 install --upgrade [module] : Upgrading [module]

#pip others(optional)
sudo apt-get install python-pip python-dev build-essential 
sudo pip install --upgrade pip 
sudo pip install --upgrade virtualenv 
sudo apt-get install python-dev libffi-dev libssl-dev -> SNIM 에러 발생시

pip3 list로 설치된 목록을 확인할 수 있음
    일일히 list를 찾아가면서 다 삭제해야함

#Removing openjdk (https://opentutorials.org/module/516/5558)
sudo apt-get --purge remove openjdk-*


#Installing Oracle Java(1.8.0_102) - 2 ways
```
    wget --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u102-b14/jdk-8u102-linux-x64.tar.gz
    export JAVA_HOME=/usr/local/java/jdk
    export CLASS_PATH=.:$JAVA_HOME/jre/lib/ext:$JAVA_HOME/lib/tools.jar
    PATH=$PATH:$JAVA_HOME/bin
```   
1. http://blog.daum.net/ivnoon/14

2. https://opentutorials.org/module/516/5558
```
    sudo update-alternatives --install "/usr/bin/java" "java" "/usr/local/java/bin/java" 1;
    sudo update-alternatives --set java /usr/local/java/bin/java
```
_Other ways choice Java Version (If you successed changing version upper way, not exceed below way)_
            _http://rockball.tistory.com/entry/Java-Version-%EB%B3%80%EA%B2%BD_
    java -version
    ls -al /usr/bin | grep java
    rm -rf /etc/alternatives/java
    ln -s /usr/java/jdk1.8.0_102/bin/java /etc/alternatives/java
    java -version

#Installing Scala(2.12.0) -> based on java 8,
_Also, existed 2.11.8_
scala-2.12.0.tgz
```
    export SCALA_HOME=/usr/local/scala
    PATH=$PATH:$SCALA_HOME/bin
```
apt-get install scala를 하면 최신버전으로 설치되지 않음(2.9.2)


#Installing sbt(0.13.12)
Needless sbt .deb file
```
    echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
    sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
    sudo apt-get update
        에러발생시, sudo apt-get -f install
                    sudo apt-get autoremove
                    sudo apt-get install apt-transport-https
    sudo apt-get update 
    sudo apt-get install sbt
```


#Installing spark(2.0.2)
_Also, existed 2.0.1_
wget http://d3kbcqa49mib13.cloudfront.net/spark-2.0.1-bin-hadoop2.7.tgz
spark-2.0.1-bin-hadoop2.7.tgz
```
    export SPARK_HOME=/usr/local/spark
    PATH=$PATH:$SPARK_HOME/bin
```

# Removing Maven(2.2.1)
apt-get remove --purge maven2

#Installing Maven(3.3.9)
http://maven.apache.org/download.cgi
apache-maven-3.3.9-bin.tar.gz
```
    MAVEN_HOME=/usr/local/maven
    PATH=$PATH:$MAVEN_HOME/bin
```
#Installing jblas
git clone https://github.com/mikiobraun/jblas
way 1.
```
spark-shell --packages org.jblas:jblas:1.2.4-SNAPSHOT
```
way 2.
```
    mvn package
    cp jblas..jar classpath ext/ adding
```

#Installing scala on Jupyter
```
git clone https://github.com/alexarchambault/jupyter-scala.git
./jupyter-scala (on scala 2.11.* version)
```
if jupyter kernelspec list, scala211 existed, it's done.


