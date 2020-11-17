package updateConsumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MyDeliverCallback implements DeliverCallback {

    private Channel channel;

    private final BooleanHelper booleanHelper;

    public MyDeliverCallback(Channel channel) {
        this.channel = channel;
        this.booleanHelper = new BooleanHelper();
    }

    @Override
    public void handle(String consumerTag, Delivery delivery) throws IOException {
        synchronized (booleanHelper) {
            booleanHelper.setProcessing(true);
            booleanHelper.notify();
        }
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        System.out.println(" [x] Received '" + message + "'");
        try {
            doWork(message);
        } finally {
            System.out.println(" [x] Done");
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            synchronized (booleanHelper) {
                booleanHelper.setProcessing(false);
                booleanHelper.notify();
            }
        }
    }

    private void doWork(String task) {
        for (char ch : task.toCharArray()) {
            if (ch == '.') {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException _ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void closeConnection(String consumerTagName){
        try {
            channel.basicCancel(consumerTagName);
            synchronized (booleanHelper) {
                while (booleanHelper.isProcessing()) {
                    booleanHelper.wait();
                }
            }
            channel.getConnection().close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class BooleanHelper {
        private boolean isProcessing;

        private BooleanHelper() {
            this.isProcessing = false;
        }

        private boolean isProcessing() {
            return isProcessing;
        }

        private void setProcessing(boolean processing) {
            isProcessing = processing;
        }
    }
}
