package cleo.primer.util;
import org.apache.log4j.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;


public class CleoJetty {
    private static final int DEFAULT_HTTP_PORT = 5000;
    private static final String WAR_LOCATION = "target";
    private static Logger logger = Logger.getLogger(CleoJetty.class);

    public static void main(String[] args) {
        try {
            Server server = new Server(DEFAULT_HTTP_PORT);

            WebAppContext webAppContext = new WebAppContext();
            webAppContext.setContextPath("/");
            webAppContext.setWar(WAR_LOCATION);

            webAppContext.setServer(server);
            server.setHandler(webAppContext);
            server.start();
        } catch (Exception e) {
            logger.error("Error when starting", e);
        }
    }
}
