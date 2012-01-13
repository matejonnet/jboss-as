/**
 *
 */
package org.jboss.as.paas.test.deltacloud;

//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.fail;
//
//import java.io.IOException;
//import java.net.ConnectException;
//import java.net.URL;
//import java.net.URLConnection;
//
//import org.apache.deltacloud.client.DeltaCloudClient;
//import org.apache.deltacloud.client.DeltaCloudClientException;
//import org.apache.deltacloud.client.DeltaCloudClientImpl;
//import org.apache.deltacloud.client.Image;
//import org.apache.deltacloud.client.Instance;
//import org.apache.deltacloud.client.internal.test.context.MockIntegrationTestContext;
//import org.junit.Before;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DeltaCloudClientTest {

    // public static final String DELTACLOUD_URL = "http://localhost:3001";
    // public static final String DELTACLOUD_USER = "mockuser";
    // public static final String DELTACLOUD_PASSWORD = "mockpassword";
    // private static final long TIMEOUT = 5000;
    //
    // private DeltaCloudClient client;
    // private Instance testInstance;
    //
    // private MockIntegrationTestContext testSetup;
    //
    // @Before
    // public void setUp() throws IOException, DeltaCloudClientException {
    // this.testSetup = new MockIntegrationTestContext();
    // testSetup.setUp();
    // }
    //
    // public void CreateInstanceTest() {
    //
    //
    // }
    //
    //
    // public void setUp() throws IOException, DeltaCloudClientException {
    // ensureDeltaCloudIsRunning();
    // this.client = new DeltaCloudClientImpl(DELTACLOUD_URL, DELTACLOUD_USER,
    // DELTACLOUD_PASSWORD);
    // Image image = getFirstImage(client);
    // this.testInstance = createTestInstance(image);
    // }
    //
    // private Instance createTestInstance(Image image) throws
    // DeltaCloudClientException {
    // assertNotNull(image);
    // Instance instance = client.createInstance(image.getId());
    // return instance;
    // }
    //
    // public void ensureDeltaCloudIsRunning() throws IOException {
    // try {
    // URLConnection connection = new URL(DELTACLOUD_URL).openConnection();
    // connection.connect();
    // } catch (ConnectException e) {
    // fail("Local DeltaCloud instance is not running. Please start a DeltaCloud instance before running these tests.");
    // }
    // }
}
