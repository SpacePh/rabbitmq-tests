import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Send {

    private final static String QUEUE_NAME = "hello_v5";

    public static void main(String[] args) throws Exception {

        // Creating connection to server
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try(Connection connection = factory.newConnection();
        Channel channel = connection.createChannel()) {
            // Declare queue and publish message
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String message = "Hello World! New Version";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}
