package es.jovenesadventistas.oacore.controller;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class ChatSocketHandler extends TextWebSocketHandler {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	
	private static final List<WebSocketSession> chatters = new CopyOnWriteArrayList<>();
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
    	String userName = session.getPrincipal().getName();
    	logger.info("received message: " 
    			+ message.getPayload() + " from " 
    			+ userName);
    	broadcast(userName + ": " + message.getPayload());
    }
    
    @Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    	super.afterConnectionEstablished(session);
    	session.sendMessage(new TextMessage("Welcome to chat; there are " + (1+chatters.size()) + " chatters"));
    	chatters.add(session);
    	broadcast(session.getPrincipal().getName() + " has joined!");
	}
    
    @Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		super.afterConnectionClosed(session, status);
    	chatters.remove(session);
    	broadcast(session.getPrincipal().getName() + " has left");
	}

	private void broadcast(String message) {
		logger.info("broadcasting: " + message);
    	for (WebSocketSession s : chatters) {
    		try {
    			s.sendMessage(new TextMessage(message));
    		} catch (Exception e) {
    			logger.warn("Could not send out message to " 
    					+ s.getPrincipal().getName() + ":", e);
    		}
    	}
    }
}