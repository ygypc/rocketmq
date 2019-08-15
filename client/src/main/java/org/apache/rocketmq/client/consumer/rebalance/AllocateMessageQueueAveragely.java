/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.client.consumer.rebalance;

import java.util.ArrayList;
import java.util.List;
import org.apache.rocketmq.client.consumer.AllocateMessageQueueStrategy;
import org.apache.rocketmq.client.log.ClientLogger;
import org.apache.rocketmq.common.message.MessageQueue;
import org.slf4j.Logger;

/**
 * Average Hashing queue algorithm
 */
public class AllocateMessageQueueAveragely implements AllocateMessageQueueStrategy {
    private final Logger log = ClientLogger.getLog();

    /**
     *
     * @param consumerGroup current consumer group
     * @param currentCID current consumer id
     * @param mqAll message queue set in current topic
     * @param cidAll consumer set in current consumer group
     * @return
     */
    @Override
    public List<MessageQueue> allocate(String consumerGroup, String currentCID, List<MessageQueue> mqAll,
        List<String> cidAll) {
        if (currentCID == null || currentCID.length() < 1) {
            throw new IllegalArgumentException("currentCID is empty");
        }
        if (mqAll == null || mqAll.isEmpty()) {
            throw new IllegalArgumentException("mqAll is null or mqAll empty");
        }
        if (cidAll == null || cidAll.isEmpty()) {
            throw new IllegalArgumentException("cidAll is null or cidAll empty");
        }

        List<MessageQueue> result = new ArrayList<MessageQueue>();
        if (!cidAll.contains(currentCID)) {
            log.info("[BUG] ConsumerGroup: {} The consumerId: {} not in cidAll: {}",
                consumerGroup,
                currentCID,
                cidAll);
            return result;
        }
        //计算当前消费者在消费者集合(List<String> cidAll)中下标的位置(index)
        int index = cidAll.indexOf(currentCID);
        //计算当前消息队列(Message Queue)中的消息是否能被消费者集合(cidAll)平均消费掉
        int mod = mqAll.size() % cidAll.size();

        int averageSize =
            mqAll.size() <= cidAll.size() ? 1 : (mod > 0 && index < mod ? mqAll.size() / cidAll.size()
                + 1 : mqAll.size() / cidAll.size());
   /*     int averageSize1 ;
        //如果消费者的数量 >= 消息的数量, 当前消费者消耗的消息数量为1
        if(cidAll.size()>=mqAll.size()){
            averageSize1 = 1;
        } else {
            //如果消息不能被消费者平均消费掉,
            // 且当前消费者在消费者集合中的下标(index) < 平均消费后的余数mod ,
            // 则当前消费者消费的数量为
            if(mod > 0 && index < mod){
                averageSize1 = mqAll.size() / cidAll.size()+ 1;
            } else {
                //mod==0 && index==mod 可以被各消费者平均消费,平均消费数量
                averageSize1 = mqAll.size() / cidAll.size();
            }

        }*/

        int startIndex = (mod > 0 && index < mod) ? index * averageSize : index * averageSize + mod;
        int range = Math.min(averageSize, mqAll.size() - startIndex);
        for (int i = 0; i < range; i++) {
            result.add(mqAll.get((startIndex + i) % mqAll.size()));
        }
        return result;
    }

    @Override
    public String getName() {
        return "AVG";
    }

    public static void main(String[] args){
        String currentCID="5";
        List<String> mqAll = new ArrayList<String>();
        for(int i=0;i<16;i++){
            mqAll.add(i+"");
        }
       List<String> cidAll = new ArrayList<String>();
        for(int i=0;i<6;i++){
            cidAll.add(i+"");
        }
        //计算当前消费者在消费者集合(List<String> cidAll)中下标的位置(index)
        int index = cidAll.indexOf(currentCID);
        //计算当前消息队列(Message Queue)中的消息是否能被消费者集合(cidAll)平均消费掉
        int mod = mqAll.size() % cidAll.size();

        int averageSize ;
        //如果消费者的数量 >= 消息的数量, 当前消费者消耗的消息数量为1
        if(cidAll.size()>=mqAll.size()){
            averageSize = 1;
        } else {
            //如果消息不能被消费者平均消费掉,
            // 且当前消费者在消费者集合中的下标(index) < 平均消费后的余数mod ,
            // 则当前消费者消费的数量为
            if(mod > 0 && index < mod){
                averageSize = mqAll.size() / cidAll.size()+ 1;
            } else {
                //mod==0 && index==mod 可以被各消费者平均消费,平均消费数量
                averageSize = mqAll.size() / cidAll.size();
            }

        }
        System.out.println("index="+index);
        System.out.println("mod="+mod);
        int startIndex = (mod > 0 && index < mod) ? index * averageSize : index * averageSize + mod;
        System.out.println("startIndex="+startIndex);
        int range = Math.min(averageSize, mqAll.size() - startIndex);
        System.out.println("range="+range);
        for (int i = 0; i < range; i++) {
            System.out.println(mqAll.get((startIndex + i) % mqAll.size()));
        }
    }
}
