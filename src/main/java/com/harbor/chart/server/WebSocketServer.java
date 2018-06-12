package com.harbor.chart.server;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.OnClose;  
import javax.websocket.OnError;  
import javax.websocket.OnMessage;  
import javax.websocket.OnOpen;  
import javax.websocket.Session;  
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

/**
 * @author szy
 * 
 */

@ServerEndpoint(value = "/websocket")
@Component
public class WebSocketServer {  
	
	 private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
	 
    //静态变量，用来记录当前在线连接数。应该把如何 它设计成线程安全的？  
    private static int onlineCount = 0;  
    
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。  
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<WebSocketServer>();  
      
    //与某个客户端的连接会话，需要通过它来给客户端发送数据  
    private Session session;  
      
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    
	/**
	 * 连接建立成功调用的方法
	 */
    @OnOpen  
    public void onOpen(Session session) {  
        this.session = session;  
        webSocketSet.add(this);     //加入set中  
        addOnlineCount();           //在线数加1  
		logger.info("[WebSocketServer.onOpen] {}", "有新连接加入！当前在线人数为" + getOnlineCount() + "当前时间:" +dateFormat.format(new Date()));
		//System.out.println("有新连接加入！当前在线人数为"+getOnlineCount());
    }  

    /** 
     * 连接关闭调用的方法 
     */  
    @OnClose  
    public void onClose() {     	
        webSocketSet.remove(this);  //从set中删除  
        subOnlineCount();           //在线数减1  
		logger.info("[WebSocketServer.onClose] {}", "有连接退出！当前在线人数为" +getOnlineCount() +"当前时间:" +dateFormat.format(new Date()));
        //System.out.println("有连接退出！当前在线人数为" + getOnlineCount()); 
    }  
  
	/**
	 * 收到客户端消息后调用的方法 
	 * @param message  客户端发送过来的消息
	 */
    @OnMessage  
    public void onMessage(String message, Session session) {  
		logger.info("[WebSocketServer.onMessage] {}", session.getId()+ "客户端的发送消息======内容:" + message + "当前时间:" + dateFormat.format(new Date()));
        //System.out.println("【" + session.getId() + "】客户端的发送消息======内容【" + message + "】");
        
		//单发消息
//		try {
//			JSONObject jsonObject = new JSONObject();
//			jsonObject.put("time", dateFormat.format(new Date()));
//			jsonObject.put("content", message);
//			sendMessage(jsonObject.toJSONString());
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		//群发消息
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("time", dateFormat.format(new Date()));
			jsonObject.put("content", message);
			sendInfo(jsonObject.toJSONString());
		} catch (IOException e) {
			e.printStackTrace();
		} 
    }  
  
    /** 
     *  
     * @param session 
     * @param error 
     */  
    @OnError  
    public void onError(Session session, Throwable error) {  
        //error.printStackTrace();  
		logger.error("[WebSocketServer.onError] {}", session.getId()+"错误消息:" + error ,"当前时间:" +dateFormat.format(new Date()));
    }  
  
  
    /**
     * 服务端消息主动推送
     * @param message
     * @throws IOException
     */
    public  void sendMessage(String message) throws IOException {  
        this.session.getBasicRemote().sendText(message);  
    }  
  
  
    /** 
     * 群发自定义消息 
     * */  
    public static void sendInfo(String message) throws IOException {  
        for (WebSocketServer item : webSocketSet) {  
            try {  
                item.sendMessage(message);  
            } catch (IOException e) {  
                continue;  
            }  
        }  
    }  
  
    public static synchronized int getOnlineCount() {  
        return onlineCount;  
    }  
  
    public static synchronized void addOnlineCount() {  
    	WebSocketServer.onlineCount++;  
    }  
  
    public static synchronized void subOnlineCount() {  
    	WebSocketServer.onlineCount--;  
    }  
    

}