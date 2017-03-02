package controller;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.util.ByteSequence;
import original.JposClient;

import javax.jms.*;
import javax.jms.IllegalStateException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

/**
 * Created by nifras on 2/18/17.
 */
public class PaymentController implements MessageListener{

    private volatile String subscriptionName;
    private volatile Connection connection;
    private volatile Session session;
    private volatile MessageProducer messageProducer;
    private volatile String topicName;
    private volatile Topic topic;
    private volatile Destination destination;
    private String operatorID;
    private JposClient jposClient;
    private static int ackMode;
    public static String messageBrokerUrl;
    private boolean isConnected;
    private boolean isAnswered;
    private MessageConsumer messageConsumer;
    static {
        //messageBrokerUrl = Constant.configuration.getURL();////ActiveMQConnection.DEFAULT_BROKER_URL;//"tcp://localhost:61616";
        messageBrokerUrl ="tcp://localhost:61616";
        // messageBrokerUrl ="tcp://104.131.100.20:61616";
        ackMode = Session.AUTO_ACKNOWLEDGE;

    }


    public PaymentController(String subscriptionName, String topicName, JposClient jposClient) throws JMSException {
        this.subscriptionName = subscriptionName;
        this.topicName = topicName;
        this.jposClient = jposClient;
         this.create();

    }


//    public Operator(String operatorID, String subscriptionName, String topicName) throws JMSException {
//        this.subscriptionName = subscriptionName;
//        this.topicName = topicName;
//        this.operatorID = operatorID;
//        this.create();
//
//    }

    public void create() {

        try {
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("admin","admin",messageBrokerUrl);
            connection = connectionFactory.createConnection();
            connection.setClientID(subscriptionName);
            boolean transacted = false;
            session =connection.createSession(transacted, ackMode);

            destination = this.session.createTopic(topicName);
            topic = session.createTopic(topicName);
            messageProducer = session.createProducer(topic);
            messageConsumer = session.createConsumer(getDestination());//createDurableSubscriber(getTopic(), getSubscriptionName());//Constant.operatorID);
            messageConsumer.setMessageListener(this);

            messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            connection.start();
            isConnected =true;
        }
        catch (JMSException e){
            //   e.printStackTrace();
            if(e instanceof InvalidClientIDException)
                isAnswered = true;
            isConnected = false;
        }


    }

    public void sendMessage(String reply) {
        try {

            String encReply = SecurityController.encrypt(reply);
            TextMessage response = getSession().createTextMessage();
            response.setText(encReply);
            response.setJMSCorrelationID("d3w2f323rgnjavwkleir1u2");
            getMessageProducer().send(response);

        } catch (IllegalStateException e) {

        } catch (NullPointerException e) {

        } catch (JMSException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onMessage(Message message) {
        String producerID = null;
        String correlationID = null;
        String messageText = "";
        try {
            if (message instanceof TextMessage) {
                //System.out.println("Object: "+message.toString());
                TextMessage txtMsg = (TextMessage) message;
                messageText = txtMsg.getText();
                messageText = SecurityController.decrypt(messageText);
/*                String[] jsoNmessage = {};//jsonFormatController.readJSONmessage(messageText);
                messageText = jsoNmessage[0];
                String owner = jsoNmessage[1];*/


                System.out.println("Recieving......:      " + messageText);
                String destination = message.getJMSDestination().toString();
                destination = destination.substring(destination.indexOf('.') + 1);
                //                System.out.println("destination: "+ destination);
                producerID = destination;
                correlationID = message.getJMSCorrelationID();



            } else if (message instanceof ActiveMQBytesMessage) {

                //System.out.println(text.getText());
                ActiveMQBytesMessage activeMQBytesMessage = (ActiveMQBytesMessage) message;

                String destination = ((ActiveMQBytesMessage) message).getDestination().getPhysicalName();

                destination = destination.substring(destination.indexOf('.') + 1);

                //                System.out.println("destination: "+ destination);
                producerID = destination;


                ByteSequence byteSequence = activeMQBytesMessage.getContent();
                byte[] bytes = byteSequence.getData();
                 messageText = new String(bytes, StandardCharsets.UTF_8);
                System.out.println(messageText);
                messageText = SecurityController.decrypt(messageText);
/*                String[] jsoNmessage = {};//jsonFormatController.readJSONmessage(messageText);

                messageText = jsoNmessage[0];
                String owner = jsoNmessage[1];*/
                System.out.println("Recieving......:      " + messageText);
                correlationID = message.getJMSCorrelationID();


            }

            if(correlationID==null)
                jposClient.execute(messageText);
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getMessageBrokerUrl() {
        return messageBrokerUrl;
    }


    public void setSubscriptionName(String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public MessageProducer getMessageProducer() {
        return messageProducer;
    }

    public void setMessageProducer(MessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }


    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public void closeConnection()  {
        try{
            connection.close();
        }
        catch (JMSException e){
            e.printStackTrace();
        }
    }

    public Destination getDestination() {
        return destination;
    }

    public Topic getTopic() {
        return topic;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getOperatorID() {
        return operatorID;
    }

    public void setOperatorID(String operatorID) {
        this.operatorID = operatorID;
    }

    public boolean isAnswered() {
        return isAnswered;
    }

    public void setAnswered(boolean answered) {
        isAnswered = answered;
    }


}
