package updateConsumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

public class Worker {

    public static void main(String[] args) throws Exception {
        System.out.println(System.getProperty("user.dir"));
        String queueName = getQueueName();

        // Creating connection to server
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        ThreadFactory threadFactory = r -> new Thread(r, "exampleThreadName");
//        factory.setThreadFactory(threadFactory);
//        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2, threadFactory);
        ExecutorService executor = Executors.newSingleThreadExecutor(threadFactory);
        Connection connection = factory.newConnection(executor);
        Channel channel = connection.createChannel();

        // Declare queue and publish message
        boolean durable = true;
        channel.queueDeclare(queueName, durable, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        int prefetchCount = 1;
        channel.basicQos(prefetchCount);

        MyDeliverCallback deliverCallback = new MyDeliverCallback(channel);

        // Buffering the messages until we're ready to use them
        boolean autoAck = false;
        String consumerTagName = channel.basicConsume(queueName, autoAck, deliverCallback, consumerTag -> {
        });

        // Check if pause consumer
        while (true) {
            if (!queueName.equals(getQueueName())) {
                System.out.println("queueName: " + queueName);
                queueName = getQueueName();
                System.out.println("queueName Updated: " + queueName);
                if (queueName.equals("pause")) {
                    System.out.println("Pausing...");
                    channel.basicCancel(consumerTagName);
                    System.out.println("PAUSED!!!");
                } else if (queueName.equals("stop")) {
                    System.out.println("Stopping...");
                    deliverCallback.closeConnection(consumerTagName);
                    System.out.println("Confirmed");
                    break;
                } else if(queueName.equals("renew")) {
                    channel.close();
                    Connection con = channel.getConnection();
                    channel = con.createChannel();
                    channel.queueDeclare(queueName, durable, false, false, null);
                    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
                    channel.basicQos(prefetchCount);
                    consumerTagName = channel.basicConsume(queueName, autoAck, deliverCallback, consumerTag -> {
                    });
                } else {
                    System.out.println("Resuming...");
                    consumerTagName = channel.basicConsume(queueName, autoAck, deliverCallback, consumerTag -> {
                    });
                }
            }
            Thread.sleep(2000);
        }
    }

    private static String getQueueName() throws IOException {
        return new String(Files.readAllBytes(Paths.get("rabbitmq-v5/src/main/resources/updateConsumer").resolve("queueName.txt")), StandardCharsets.UTF_8);
    }
}
