/*
 * HTTP Event Capture to RFC5424 CFE_16
 * Copyright (C) 2021  Suomen Kanuuna Oy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 * Additional permission under GNU Affero General Public License version 3
 * section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with other code, such other code is not for that reason alone subject to any
 * of the requirements of the GNU Affero GPL version 3 as long as this Program
 * is the same Program as licensed from Suomen Kanuuna Oy without any additional
 * modifications.
 *
 * Supplemented terms under GNU Affero General Public License version 3
 * section 7
 *
 * Origin of the software must be attributed to Suomen Kanuuna Oy. Any modified
 * versions must be marked as "Modified version of" The Program.
 *
 * Names of the licensors and authors may not be used for publicity purposes.
 *
 * No rights are granted for use of trade names, trademarks, or service marks
 * which are in The Program if any.
 *
 * Licensee must indemnify licensors and authors for any liability that these
 * contractual assumptions impose on licensors and authors.
 *
 * To the extent this program is licensed as part of the Commercial versions of
 * Teragrep, the applicable Commercial License may apply to this file if you as
 * a licensee so wish it.
 */

package com.teragrep.cfe_16.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

import com.teragrep.cfe_16.service.HECService;

@TestPropertySource(properties = { 
        "syslog.server.host=127.0.0.1", 
        "syslog.server.port=1236",
        "syslog.server.protocol=TCP", 
        "max.channels=1000000", 
        "max.ack.value=1000000", 
        "max.ack.age=20000", 
        "max.session.age=30000", 
        "poll.time=30000", 
        "spring.devtools.add-properties=false", 
        "server.print.times=true" 
        })
@SpringBootTest
public class SendEventsIT implements Runnable {
    @Autowired
    private HECService service;    

    private ServerSocket serverSocket;
    private Thread thread;
    
    private static int NUMBER_OF_EVENTS_TO_BE_SENT = 1;
    
    private volatile int numberOfRequestsMade;
    private AtomicInteger numberOfRequestsMadeAI ;
    
    private MockHttpServletRequest request1;
    private String eventInJson;
    private String channel1;
    private Selector selector;
    private final static int SERVER_PORT = 1236;
    private CountDownLatch countDownLatch = new CountDownLatch(0);
    
    @BeforeEach
    public void init() throws IOException {
        this.thread = new Thread(this);
        this.numberOfRequestsMade = 0;
        this.numberOfRequestsMadeAI = new AtomicInteger(0);
        this.serverSocket = new ServerSocket(SERVER_PORT);
        this.selector = Selector.open();
        
        this.request1 = new MockHttpServletRequest();
        this.request1.addHeader("Authorization", "AUTH_TOKEN_11111");
        this.channel1 = "CHANNEL_11111";
        this.eventInJson = "{\"sourcetype\":\"access\", \"source\":\"/var/log/access.log\", \"event\": {\"message\":\"Access log test message 1\"}} {\"sourcetype\":\"access\", \"source\":\"/var/log/access.log\", \"event\": {\"message\":\"Access log test message 2\"}}";
      
        this.thread.start();
    }
    
    @AfterEach
    public void shutdown() {
        this.thread.interrupt();
    }
    
    public void run() {    	
    	while (true) {
            try {
                Socket socket = this.serverSocket.accept();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    System.out.println(line);
                    this.numberOfRequestsMade++;
                    countDownLatch.countDown();
                }
                bufferedReader.close();
                socket.close();
                if (this.numberOfRequestsMade % 5 == 0) {
                    this.serverSocket.close();
                    this.serverSocket = new ServerSocket(SERVER_PORT);
                }
            } catch (InterruptedIOException e) { 
                break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } 
    }
    
    @Test
    public void sendEventsTest() throws IOException, InterruptedException, ExecutionException {
    	NUMBER_OF_EVENTS_TO_BE_SENT = 100;
    	countDownLatch = new CountDownLatch(NUMBER_OF_EVENTS_TO_BE_SENT);
    	ExecutorService es = Executors.newFixedThreadPool(8);
    	List<CompletableFuture<String> > futures = new ArrayList<>();
    	
        for (int i = 0; i < NUMBER_OF_EVENTS_TO_BE_SENT; i++) {
            CompletableFuture<String> f = CompletableFuture.supplyAsync(new Supplier<String>() {
            	public String get() {
            		return service.sendEvents(request1, channel1, eventInJson).toString();
            	}
            	
			});
            futures.add(f);
            
        }
        List<String> supposedResponses = new ArrayList<String>();
        for (int i = 0; i < NUMBER_OF_EVENTS_TO_BE_SENT; i++) {
        	final String supposedResponse = "{\"text\":\"Success\",\"code\":0,\"ackID\":" + i + "}";
        	supposedResponses.add(supposedResponse);
        }
        int countFuture = 0;
        for (Future<String> f : futures) {        	
			final String actualResponse = f.get();
        	System.out.println(" actualResponse " + actualResponse);
        	assertTrue("Service should return JSON object with fields 'text', 'code' and 'ackID' (ackID should be " + countFuture + ")",
        			supposedResponses.contains(actualResponse));
        	countFuture++;
		}
        
        countDownLatch.await(1, TimeUnit.SECONDS);
        assertEquals("Number of events received should match the number of sent ones",
                NUMBER_OF_EVENTS_TO_BE_SENT * 2, 
                this.numberOfRequestsMade);
        es.shutdownNow();
    }
    
    // @Test
    public void send1EventTest() throws IOException, InterruptedException {
    	NUMBER_OF_EVENTS_TO_BE_SENT = 1;
    	countDownLatch = new CountDownLatch(1);
        String supposedResponse = "{\"text\":\"Success\",\"code\":0,\"ackID\":" + 0 + "}";
            assertEquals("Service should return JSON object with fields 'text', 'code' and 'ackID' (ackID should be " + 0 + ")",
                    service.sendEvents(request1, channel1, eventInJson).toString(),
                    supposedResponse);

        countDownLatch.await(5, TimeUnit.SECONDS);
        assertEquals("Number of events received should match the number of sent ones",
                 2, 
                this.numberOfRequestsMade);
    }
}
