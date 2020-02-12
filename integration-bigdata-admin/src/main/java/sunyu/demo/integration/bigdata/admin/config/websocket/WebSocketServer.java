package sunyu.demo.integration.bigdata.admin.config.websocket;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/websocket/{sid}")
@Component
public class WebSocketServer {
    public static Map<String, Session> sessionMap = new ConcurrentHashMap<>();
    private static Map<String, String> sessionSidMap = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        StaticLog.info("WebSocket 连接成功，sid：{}", sid);
        sessionMap.put(sid, session);
        sessionSidMap.put(session.getId(), sid);
    }

    @OnClose
    public void onClose(Session session) {
        if (session != null && StrUtil.isNotBlank(session.getId())) {
            sessionMap.remove(sessionSidMap.get(session.getId()));
            sessionSidMap.remove(session.getId());
            StaticLog.info("WebSocket 连接关闭");
        }
    }

    public static void sendMessage(String sid, String message) {
        try {
            sessionMap.get(sid).getBasicRemote().sendText(message);
        } catch (IOException e) {
            StaticLog.error(e);
        }
    }

}
