package updateConsumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Worker {

    public static void main(String[] args) throws Exception {
        System.out.println(System.getProperty("user.dir"));
        String queueName = getQueueName();

        // Creating connection to server
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // Declare queue and publish message
        boolean durable = true;
        channel.queueDeclare(queueName, durable, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        int prefetchCount = 1;
        channel.basicQos(prefetchCount);

        // Buffering the messages until we're ready to use them
        boolean autoAck = false;
        String consumerTagName = channel.basicConsume(queueName, autoAck, new MyDeliverCallback(channel), consumerTag -> { });

        // Check if queueName was updated
        while(true) {
            if(!queueName.equals(getQueueName())) {
                System.out.println("queueName: " + queueName);
                queueName = getQueueName();
                System.out.println("queueName Updated: " + queueName);
                System.out.println("consumerTagName: " + consumerTagName);
                channel.basicCancel(consumerTagName);
                consumerTagName = channel.basicConsume(queueName, autoAck, new MyDeliverCallback(channel), consumerTag -> { });
                System.out.println("consumerTagName Updated: " + consumerTagName);
            }
            Thread.sleep(5000);
        }
    }

    private static String getQueueName() throws IOException {
        return new String(Files.readAllBytes(Paths.get("rabbitmq-v5/src/main/resources/updateConsumer").resolve("queueName.txt")), StandardCharsets.UTF_8);
    }
}
