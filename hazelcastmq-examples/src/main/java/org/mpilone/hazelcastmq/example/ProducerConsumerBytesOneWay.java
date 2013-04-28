package org.mpilone.hazelcastmq.example;

import java.util.Arrays;

import javax.jms.*;

import org.mpilone.hazelcastmq.HazelcastMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * Example of sending a request bytes message and consuming the message from the
 * queue.
 */
public class ProducerConsumerBytesOneWay {

  private final Logger log = LoggerFactory.getLogger(getClass());

  public static void main(String[] args) throws Exception {
    new ProducerConsumerBytesOneWay();
  }

  /**
   * Constructs the example.
   * 
   * @throws JMSException
   */
  public ProducerConsumerBytesOneWay() throws JMSException {

    // Create a Hazelcast instance.
    Config config = new Config();
    HazelcastInstance hazelcast = Hazelcast.newHazelcastInstance(config);

    try {
      // Setup the connection factory.
      HazelcastMQConnectionFactory connectionFactory = new HazelcastMQConnectionFactory();
      connectionFactory.setHazelcast(hazelcast);

      // Create a connection, session, and destinations.
      Connection connection = connectionFactory.createConnection();
      connection.start();
      Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
      Destination requestDest = session.createQueue("foo.bar");

      // Create a request producer and reply consumer.
      MessageProducer producer1 = session.createProducer(requestDest);
      MessageConsumer consumer1 = session.createConsumer(requestDest);

      byte[] data = new byte[] { 8, 6, 7, 5, 3, 0, 9 };

      BytesMessage msg = session.createBytesMessage();
      msg.writeBytes(data);
      producer1.send(msg);
      
      session.commit();

      msg = (BytesMessage) consumer1.receive(2000);
      Assert.notNull(msg, "Did not get required message.");

      data = new byte[(int) msg.getBodyLength()];
      msg.readBytes(data);

      log.info("Got message: " + Arrays.toString(data));

      producer1.close();
      consumer1.close();

      // Cleanup.
      session.close();
      connection.close();
    }
    finally {
      Hazelcast.shutdownAll();
    }
  }
}
