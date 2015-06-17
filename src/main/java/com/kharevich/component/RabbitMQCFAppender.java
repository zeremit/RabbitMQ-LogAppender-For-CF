package com.kharevich.component;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.cloud.service.common.AmqpServiceInfo;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.management.ServiceNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

/**
 * Created by zeremit on 15.6.15.
 */
@Component
public class RabbitMQCFAppender extends AppenderSkeleton{

    private static final String LOG_APMQ_SERVICE_NAME = "log_service";

    private ConnectionFactory connectionFactory;

    private Connection connection = null;
    private Channel channel = null;

    private String identifier = null;
    private String exchange = "amqp-exchange";
    private String type = "direct";
    private boolean durable = false;
    private String queue = "amqp-queue";
    private String routingKey = "";

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isDurable() {
        return durable;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    private ExecutorService threadPool = Executors.newSingleThreadExecutor();


    /**
     * Submits LoggingEvent for publishing if it reaches severity threshold.
     * @param loggingEvent
     */
    @Override
    protected void append(LoggingEvent loggingEvent) {
        System.out.println(loggingEvent.getMessage());
        if ( isAsSevereAsThreshold(loggingEvent.getLevel())) {
            threadPool.submit( new AppenderTask(loggingEvent) );
        }
    }

    /**
     * Creates the connection, channel to RabbitMQ. Declares exchange and queue
     * @see AppenderSkeleton
     */
    @Override
    public void activateOptions() {
        super.activateOptions();

        //== creating connection
        try {
            this.createConnection();
        } catch (IOException ioe) {
            errorHandler.error(ioe.getMessage(), ioe, ErrorCode.GENERIC_FAILURE);
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (ServiceNotFoundException e) {
            e.printStackTrace();
        }

        //== creating channel
        try {
            this.createChannel();
        } catch (IOException ioe) {
            errorHandler.error(ioe.getMessage(), ioe, ErrorCode.GENERIC_FAILURE);
        }

        //== create exchange
        try {
            this.createExchange();
        } catch (Exception ioe) {
            errorHandler.error(ioe.getMessage(), ioe, ErrorCode.GENERIC_FAILURE);
        }

        //== create queue
        try {
            this.createQueue();
        } catch (Exception ioe) {
            errorHandler.error(ioe.getMessage(), ioe, ErrorCode.GENERIC_FAILURE);
        }
    }

    /**
     * Sets the ConnectionFactory parameters
     */
    private void setFactoryConfiguration() throws ServiceNotFoundException {
        CloudFactory cloudFactory = new CloudFactory();
        Cloud cloud = cloudFactory.getCloud();
        ServiceInfo serviceInfo = null;
        String serviceName = System.getenv(LOG_APMQ_SERVICE_NAME);
        if(serviceName!=null){
            serviceInfo = cloud.getServiceInfo(serviceName);
        }else{
            List<ServiceInfo> infos = cloud.getServiceInfos(ConnectionFactory.class);
            //if bind amqp only one service
            if(infos.size()==1) {
                serviceInfo = infos.get(0);
            }
        }
        if(serviceInfo==null) {
            throw new ServiceNotFoundException("Log service not found");
        }
        String serviceID = serviceInfo.getId();
        connectionFactory = cloud.getServiceConnector(serviceID, ConnectionFactory.class, null);
    }

    /**
     * Declares the exchange on RabbitMQ server according to properties set
     * @throws IOException
     */
    private void createExchange() throws IOException {
        if (this.channel != null && this.channel.isOpen()) {
            synchronized (this.channel) {
                this.channel.exchangeDeclare(this.exchange, this.type, this.durable);
            }
        }
    }


    /**
     * Declares and binds queue on rabbitMQ server according to properties
     * @throws IOException
     */
    private void createQueue() throws IOException {
        if (this.channel != null && this.channel.isOpen()) {
            synchronized (this.channel) {
                this.channel.queueDeclare(this.queue, false, false, false, null);
                this.channel.queueBind(this.queue, this.exchange, this.routingKey);
            }
        }
    }

    /**
     * Creates channel on RabbitMQ server
     * @return
     * @throws IOException
     */
    private Channel createChannel() throws IOException {
        if (this.channel == null || !this.channel.isOpen() && (this.connection != null && this.connection.isOpen()) ) {
            this.channel = this.connection.createChannel(false);
        }
        return this.channel;
    }

    /**
     * Creates connection to RabbitMQ server according to properties
     * @return
     * @throws IOException
     */
    private Connection createConnection() throws IOException, TimeoutException, ServiceNotFoundException {
        setFactoryConfiguration();
        if (this.connection == null || !this.connection.isOpen()) {
            this.connection = connectionFactory.createConnection();
        }

        return this.connection;
    }


    /**
     * Closes the channel and connection to RabbitMQ when shutting down the appender
     */
//    @Override
    public void close() {
        if (channel != null && channel.isOpen()) {
            try {
                channel.close();
            } catch (IOException ioe) {
                errorHandler.error(ioe.getMessage(), ioe, ErrorCode.CLOSE_FAILURE);
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }

        if (connection != null && connection.isOpen()) {
            this.connection.close();
        }
    }


    /**
     * Ensures that a Layout property is required
     * @return
     */
//    @Override
    public boolean requiresLayout() {
        return true;
    }


    /**
     * Simple Callable class that publishes messages to RabbitMQ server
     */
    class AppenderTask implements Callable<LoggingEvent> {
        LoggingEvent loggingEvent;

        AppenderTask(LoggingEvent loggingEvent) {
            this.loggingEvent = loggingEvent;
        }

        /**
         * Method is called by ExecutorService and publishes message on RabbitMQ
         * @return
         * @throws Exception
         */
//        @Override
        public LoggingEvent call() throws Exception {
            String payload = layout.format(loggingEvent);
            String id = String.format("%s:%s", identifier, System.currentTimeMillis());


            AMQP.BasicProperties.Builder b = new AMQP.BasicProperties().builder();
            b.appId(identifier)
                    .type(loggingEvent.getLevel().toString())
                    .correlationId(id)
                    .contentType("text/json");

            createChannel().basicPublish(exchange, routingKey, b.build(), payload.toString().getBytes());

            return loggingEvent;
        }
    }
}
