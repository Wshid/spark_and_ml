# ~/.profile: executed by the command interpreter for login shells.
# This file is not read by bash(1), if ~/.bash_profile or ~/.bash_login
# exists.
# see /usr/share/doc/bash/examples/startup-files for examples.
# the files are located in the bash-doc package.

# the default umask is set in /etc/profile; for setting the umask
# for ssh logins, install and configure the libpam-umask package.
#umask 022

# if running bash
if [ -n "$BASH_VERSION" ]; then
    # include .bashrc if it exists
    if [ -f "$HOME/.bashrc" ]; then
	. "$HOME/.bashrc"
    fi
fi

# set PATH so it includes user's private bin if it exists
if [ -d "$HOME/bin" ] ; then
    PATH="$HOME/bin:$PATH"
fi
[ "$BASH_VERSION" ] && npm() { if [ "$*" == "config get prefix" ]; then which node | sed "s/bin\/node//"; else $(which npm) "$@"; fi } # hack: avoid slow npm sanity check in nvm
[ -s "/home/ubuntu/.nvm/nvm.sh" ] && . "/home/ubuntu/.nvm/nvm.sh" # This loads nvm
unset npm # end hack

#jupyter-notebook with pyspark // using way : $pyspark
export PYSPARK_PYTHON=/home/ubuntu/workspace/practice/python3/ipy/bin/python3
export PYSPARK_DRIVER_PYTHON=/home/ubuntu/workspace/practice/python3/ipy/bin/jupyter
export PYSPARK_DRIVER_PYTHON_OPTS="notebook --NotebookApp.open_browser=False --NotebookApp.ip='0.0.0.0' --NotebookApp.port=8081"

#test
#export PYSPARK_SUBMIT_ARGS="--master local[2]"

#alias
alias ipy="source /home/ubuntu/workspace/practice/python3/ipy/bin/activate"
alias jup="jupyter-notebook --ip=0.0.0.0 --port=8081 --no-browser --notebook-dir='/home/ubuntu/workspace/practice/python3'"

#JAVA
export JAVA_HOME=/usr/local/java
export CLASS_PATH=.:$JAVA_HOME/jre/lib/ext:$JAVA_HOME/lib/tools.jar

#SCALA
export SCALA_HOME=/usr/local/scala

#SPARK
export SPARK_HOME=/usr/local/spark
#export SPARK_CLASSPATH=/home/ubuntu/workspace/Downloads/netlib-java-lib/netlib-native_ref-linux-x86_64-1.1-natives.jar

#MAVEN
export MAVEN_HOME=/usr/local/maven

PATH=$PATH:$JAVA_HOME/bin:$SCALA_HOME/bin:$SPARK_HOME/bin:$MAVEN_HOME/bin

#export PYSPARK_SUBMIT_ARGS="--master local[*] pyspark-shell"