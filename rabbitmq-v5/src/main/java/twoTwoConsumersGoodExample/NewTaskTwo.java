package twoTwoConsumersGoodExample;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.util.Scanner;

public class NewTaskTwo {
    private final static String QUEUE_NAME = "task_queue_v5";

    public static void main(String[] args) throws Exception {

        // Creating connection to server
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try(Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()) {
            // Declare queue and publish message
            boolean durable = true;
            channel.queueDeclare(QUEUE_NAME, durable, false, false, null);

            Scanner scanner = new Scanner(System.in);
            System.out.print("Message: ");
            String message = scanner.nextLine().trim();
            while(!message.equals("close")) {
                channel.basicPublish("", QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
                System.out.println(" [x] Sent '" + message + "'");

                System.out.print("Message: ");
                message = scanner.nextLine().trim();
            }
        }
    }
}
