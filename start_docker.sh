#!/usr/bin/env bash

# the default node number is 3
N=${1:-3}

dir=`pwd`
sudo rm -rf logs
# build image
sudo docker build -t olzhabay/passwordcrackerinhadoop .

# start hadoop master container
sudo docker rm -f hadoop-master &> /dev/null
echo "start hadoop-master container..."
sudo docker run -itd \
                --net=hadoop \
                -p 50070:50070 \
                -p 8088:8088 \
                -p 50030:50030 \
                --name hadoop-master \
                --hostname hadoop-master \
                olzhabay/passwordcrackerinhadoop &> /dev/null

# start hadoop slave container
i=1
while [ $i -lt $N ]
do
  sudo docker rm -f hadoop-slave$i &> /dev/null
  echo "start hadoop-slave$i container..."
  sudo docker run -itd \
                  --net=hadoop \
                  --name hadoop-slave$i \
                  --hostname hadoop-slave$i \
									-v $dir/logs:/usr/local/hadoop/logs \
                  olzhabay/hadoop-compiled &> /dev/null
  i=$(( $i + 1 ))
done

sleep 2

# start hadoop
sudo docker exec -it hadoop-master start-all.sh
# get into hadoop master container
sudo docker exec -it hadoop-master bash