package ripple.interview.testClient;

import java.util.Scanner;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        Logger logger = LoggerFactory.getLogger(App.class);
        Client client = ClientBuilder.newClient( new ClientConfig());
        WebTarget apiTarget1 = client.target("http://localhost:8080/ripple/api");
        WebTarget apiTarget2 = client.target("http://localhost:8081/ripple/api");
        WebTarget ping = apiTarget1.path("ping/Alice");

        
        // ping server 1
        Invocation.Builder invocBuilder =  ping.request(MediaType.TEXT_PLAIN);
        Response response = invocBuilder.get();
        if(response.getStatus() != 200) {
            logger.error("Server1 didn't respond");
            return;
        }
        String reply = response.readEntity(String.class);
        logger.info(reply);

        // ping server2
        ping = apiTarget2.path("ping/Bob");
        invocBuilder =  ping.request(MediaType.TEXT_PLAIN);
        response = invocBuilder.get();
        if(response.getStatus() != 200) {
            logger.error("Server1 didn't respond");
            return;
        }
        reply = response.readEntity(String.class);
        logger.info(reply);

        Scanner sc=new Scanner(System.in);
        sc.nextLine();
        
        // create a new customer and an account 
        WebTarget createTarget = apiTarget1.path("newCustomer");
        invocBuilder =  createTarget.request(MediaType.APPLICATION_FORM_URLENCODED_TYPE).accept(MediaType.APPLICATION_JSON);
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<String, String>();
        formData.add("firstName", "Alice");
        formData.add("lastName", "Foo");
        formData.add("phoneNo", "4151234568");
        formData.add("email", "alicefoo@att.com");
        response = invocBuilder.post(Entity.form(formData));
        if(response.getStatus() != 200) {
            logger.error("Failed to create customer");
            return;
        }
 
        reply = response.readEntity(String.class);
        logger.info(reply);
        
        // create a new customer Bob and an account 
        createTarget = apiTarget2.path("newCustomer");
        invocBuilder =  createTarget.request(MediaType.APPLICATION_FORM_URLENCODED_TYPE).accept(MediaType.APPLICATION_JSON);
        formData = new MultivaluedHashMap<String, String>();
        formData.add("firstName", "Bob");
        formData.add("lastName", "Foo");
        formData.add("phoneNo", "4151234569");
        formData.add("email", "bobfoo@att.com");
        response = invocBuilder.post(Entity.form(formData));
        if(response.getStatus() != 200) {
            logger.error("Failed to create customer");
            return;
        }
 
        reply = response.readEntity(String.class);
        logger.info(reply);
        sc.nextLine();

        // get a transaction id
        WebTarget txnTarget = apiTarget1.path("nextTxnId");
        invocBuilder =  txnTarget.request();
        response = invocBuilder.get();
        int txnId = response.readEntity(Integer.class);
        logger.info("Initiating transaction " + txnId);

        // debit Alice's account by 100
        WebTarget debitTarget = apiTarget1.path("debit");
        invocBuilder =  debitTarget.request(MediaType.APPLICATION_FORM_URLENCODED_TYPE).accept(MediaType.APPLICATION_JSON);
        formData = new MultivaluedHashMap<String, String>();
        formData.add("phoneNo", "4151234568");
        formData.add("amt", "100");
        formData.add("txnId", "" + txnId);
        response = invocBuilder.post(Entity.form(formData));
        if(response.getStatus() != 200) {
            logger.error("Failed to debit Alice's account");
            return;
        }
 
        reply = response.readEntity(String.class);
        logger.info("Alice's balance is " + reply);
        
        // credit Bob's account by 100
        WebTarget creditTarget = apiTarget2.path("credit");
        invocBuilder =  creditTarget.request(MediaType.APPLICATION_FORM_URLENCODED_TYPE).accept(MediaType.APPLICATION_JSON);
        formData = new MultivaluedHashMap<String, String>();
        formData.add("phoneNo", "4151234569");
        formData.add("amt", "100");
        formData.add("txnId", "" + txnId);
        response = invocBuilder.post(Entity.form(formData));
        if(response.getStatus() != 200) {
            logger.error("Failed to credit Bob's account");
            return;
        }
 
        reply = response.readEntity(String.class);
        logger.info("Bob's balance is " + reply);
        sc.nextLine();

        // commit the transaction
        WebTarget commitTarget = apiTarget1.path("commit/" + txnId);
        invocBuilder =  commitTarget.request().accept(MediaType.APPLICATION_JSON);
        response = invocBuilder.post(Entity.text(null));
        if(response.getStatus() != 200) {
            logger.error("Failed to commit transaction");
            return;
        }
 
        reply = response.readEntity(String.class);
        logger.info("Server1 committed: " + reply);

        commitTarget = apiTarget2.path("commit/" + txnId);
        invocBuilder =  commitTarget.request().accept(MediaType.APPLICATION_JSON);
        response = invocBuilder.post(Entity.text(null));
        if(response.getStatus() != 200) {
            logger.error("Failed to commit transaction");
            return;
        }
 
        reply = response.readEntity(String.class);
        logger.info("Server2 committed: " + reply);
        logger.info("Transaction " + txnId + " is completed successfully");
        client.close();
    }
}
